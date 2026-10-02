package com.murthinext.ae2pr.block.assembly_line;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.network.NetworkHooks;

import com.murthinext.ae2pr.ModBlocks;
import com.murthinext.ae2pr.ModTags;

/**
 * 赛特斯石英机器部件方块（输入总线 / 输入仓 / 输出总线）。
 * <p>
 * 扳手右键旋转；空手或非扳手右键打开对应界面：总线提供单类物品存储，输入仓提供单类流体存储。
 */
public class CertusMachinePartBlock extends Block implements EntityBlock {

    /** 扳手旋转顺序 */
    private static final Direction[] ROTATION_ORDER = { Direction.DOWN, Direction.UP, Direction.NORTH,
            Direction.SOUTH, Direction.WEST, Direction.EAST };

    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty FORMED = BooleanProperty.create("formed");

    public CertusMachinePartBlock() {
        super(Properties.of()
                .mapColor(MapColor.METAL)
                .strength(3.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops());
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(FORMED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, FORMED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState()
                .setValue(FACING, context.getNearestLookingDirection().getOpposite())
                .setValue(FORMED, false);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.is(ModBlocks.CERTUS_QUARTZ_INPUT_HATCH.get())
                ? new FluidHatchBlockEntity(pos, state)
                : new ItemBusBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        return (tickLevel, pos, tickState, blockEntity) -> {
            if (blockEntity instanceof FluidHatchBlockEntity hatch) {
                hatch.serverTick();
            } else if (blockEntity instanceof ItemBusBlockEntity bus) {
                bus.serverTick();
            }
        };
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.is(ModTags.WRENCHES)) {
            return rotate(level, pos, state);
        }
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            openMenu(serverPlayer, level, pos, state);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /** 非扳手右键打开部件界面：总线为物品存储，输入仓为流体存储。 */
    private static void openMenu(ServerPlayer player, Level level, BlockPos pos, BlockState state) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        MenuProvider provider = null;
        if (blockEntity instanceof ItemBusBlockEntity bus) {
            provider = new SimpleMenuProvider((id, inventory, p) -> new ItemBusMenu(id, inventory, bus),
                    state.getBlock().getName());
        } else if (blockEntity instanceof FluidHatchBlockEntity hatch) {
            provider = new SimpleMenuProvider((id, inventory, p) -> new FluidHatchMenu(id, inventory, hatch),
                    state.getBlock().getName());
        }
        if (provider != null) {
            NetworkHooks.openScreen(player, provider, pos);
        }
    }

    /** 扳手旋转：仅未成型时允许，避免误操作破坏已建成的机器。 */
    private static InteractionResult rotate(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (state.getValue(FORMED)) {
            return InteractionResult.PASS;
        }
        level.setBlock(pos, state.setValue(FACING, nextFacing(state.getValue(FACING))), Block.UPDATE_ALL);
        level.playSound(null, pos, SoundEvents.ITEM_FRAME_ROTATE_ITEM, SoundSource.BLOCKS, 0.8F, 1.0F);
        return InteractionResult.SUCCESS;
    }

    private static Direction nextFacing(Direction current) {
        for (int i = 0; i < ROTATION_ORDER.length; i++) {
            if (ROTATION_ORDER[i] == current) {
                return ROTATION_ORDER[(i + 1) % ROTATION_ORDER.length];
            }
        }
        return Direction.NORTH;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        // 破坏时把机器存储掉落出来（成型状态切换不触发）；大堆叠按 64 一组散落，罐内流体不回收
        if (!state.is(newState.getBlock()) && !level.isClientSide) {
            if (level.getBlockEntity(pos) instanceof ItemBusBlockEntity bus) {
                dropContents(level, pos, bus.getStorage());
            } else if (level.getBlockEntity(pos) instanceof FluidHatchBlockEntity hatch) {
                dropContents(level, pos, hatch.getInputSlot());
                dropContents(level, pos, hatch.getOutputSlot());
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    /** 把物品处理器内容按 64 一组散落到地面。 */
    private static void dropContents(Level level, BlockPos pos, IItemHandler handler) {
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack stack = handler.getStackInSlot(slot);
            while (!stack.isEmpty()) {
                Block.popResource(level, pos, stack.split(Math.min(stack.getCount(), 64)));
            }
        }
    }
}
