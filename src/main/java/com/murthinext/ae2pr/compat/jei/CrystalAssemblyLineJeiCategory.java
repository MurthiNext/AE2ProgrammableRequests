package com.murthinext.ae2pr.compat.jei;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;

import com.murthinext.ae2pr.Config;
import com.murthinext.ae2pr.ModBlocks;
import com.murthinext.ae2pr.ae2pr;
import com.murthinext.ae2pr.recipe.CrystalAssemblyLineRecipe;

/**
 * 水晶装配线配方的 JEI 分类。
 */
public class CrystalAssemblyLineJeiCategory implements IRecipeCategory<CrystalAssemblyLineRecipe> {

    public static final RecipeType<CrystalAssemblyLineRecipe> RECIPE_TYPE = RecipeType.create(
            ae2pr.MODID, "crystal_assembly_line", CrystalAssemblyLineRecipe.class);

    private static final int SLOT = 18;
    private static final int PADDING = 4;
    private static final int ITEM_COLS = 4;
    private static final int MAX_ITEM_INPUTS = ITEM_COLS * 4;
    private static final int MAX_FLUID_INPUTS = 4;
    private static final int GRID_HEIGHT = 4 * SLOT + PADDING * 2;
    private static final int FLUID_X = PADDING + ITEM_COLS * SLOT + PADDING;
    private static final int ARROW_WIDTH = 22;
    private static final int ARROW_HEIGHT = 16;
    private static final int ARROW_X = FLUID_X + SLOT + 8;
    private static final int ARROW_Y = (GRID_HEIGHT - ARROW_HEIGHT) / 2;
    private static final int OUTPUT_X = ARROW_X + ARROW_WIDTH + 8;
    private static final int OUTPUT_Y = (GRID_HEIGHT - SLOT) / 2;
    private static final int INFO_Y = GRID_HEIGHT + 4;
    private static final int WIDTH = OUTPUT_X + SLOT + PADDING;
    private static final int HEIGHT = INFO_Y + 3 * 10 + 2;
    private static final ResourceLocation ARROW = new ResourceLocation(ae2pr.MODID,
            "textures/gui/jei/recipe_arrow.png");

    private static final int COLOR_HINT = 0x808080;
    private static final int COLOR_TEXT = 0x404040;
    private static final NumberFormat NUMBER = NumberFormat.getIntegerInstance();

    private final IDrawable icon;

    public CrystalAssemblyLineJeiCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.CRYSTAL_ASSEMBLY_LINE.get()));
    }

    @Override
    public RecipeType<CrystalAssemblyLineRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("block.ae2pr.crystal_assembly_line");
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public ResourceLocation getRegistryName(CrystalAssemblyLineRecipe recipe) {
        return recipe.getId();
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, CrystalAssemblyLineRecipe recipe, IFocusGroup focuses) {
        // 物品输入
        List<CrystalAssemblyLineRecipe.ItemInput> itemInputs = recipe.getItemInputs();
        for (int i = 0; i < MAX_ITEM_INPUTS; i++) {
            IRecipeSlotBuilder slot = builder.addSlot(RecipeIngredientRole.INPUT,
                    PADDING + (i % ITEM_COLS) * SLOT, PADDING + (i / ITEM_COLS) * SLOT);
            slot.setStandardSlotBackground();
            if (i >= itemInputs.size()) {
                continue;
            }
            CrystalAssemblyLineRecipe.ItemInput input = itemInputs.get(i);
            final int inputIndex = i;
            // 消耗数量直接写入 ItemStack
            List<ItemStack> stacks = new ArrayList<>(input.ingredient().getItems().length);
            for (ItemStack stack : input.ingredient().getItems()) {
                stacks.add(stack.copyWithCount(input.count()));
            }
            slot.addItemStacks(stacks);
            slot.addRichTooltipCallback(
                    (view, tooltip) -> tooltip.add(orderTooltip(inputIndex, Config.assemblyItemsOrdered())));
        }

        // 流体输入
        List<FluidStack> fluidInputs = recipe.getFluidInputs();
        for (int i = 0; i < MAX_FLUID_INPUTS; i++) {
            IRecipeSlotBuilder slot = builder.addSlot(RecipeIngredientRole.INPUT,
                    FLUID_X, PADDING + i * SLOT);
            slot.setStandardSlotBackground();
            if (i >= fluidInputs.size()) {
                continue;
            }
            FluidStack fluid = fluidInputs.get(i);
            final int fluidIndex = i;
            slot.addFluidStack(fluid.getFluid(), fluid.getAmount())
                    .setFluidRenderer(fluid.getAmount(), true, 16, 16);
            slot.addRichTooltipCallback(
                    (view, tooltip) -> tooltip.add(orderTooltip(fluidIndex, Config.assemblyFluidsOrdered())));
        }

        // 产物
        List<ItemStack> outputs = recipe.getItemOutputs();
        IRecipeSlotBuilder outputSlot = builder.addSlot(RecipeIngredientRole.OUTPUT, OUTPUT_X, OUTPUT_Y);
        outputSlot.setStandardSlotBackground();
        if (!outputs.isEmpty()) {
            outputSlot.addItemStack(outputs.get(0));
        }
    }

    @Override
    public void draw(CrystalAssemblyLineRecipe recipe, IRecipeSlotsView slotsView, GuiGraphics graphics,
            double mouseX, double mouseY) {
        var font = Minecraft.getInstance().font;
        graphics.blit(ARROW, ARROW_X, ARROW_Y, 0, 0, ARROW_WIDTH, ARROW_HEIGHT);
        graphics.drawString(font, Component.translatable("jei.ae2pr.crystal_assembly_line.duration",
                recipe.getDuration()), PADDING, INFO_Y, COLOR_TEXT, false);
        graphics.drawString(font, Component.translatable("jei.ae2pr.crystal_assembly_line.energy",
                NUMBER.format(Config.assemblyEnergyPerParallel())), PADDING, INFO_Y + 10, COLOR_TEXT, false);
        int hidden = recipe.getItemInputs().size() - MAX_ITEM_INPUTS;
        if (hidden > 0) {
            graphics.drawString(font, Component.translatable("jei.ae2pr.crystal_assembly_line.more_inputs",
                    hidden), PADDING, INFO_Y + 20, COLOR_HINT, false);
        } else if (recipe.getItemOutputs().size() > 1) {
            graphics.drawString(font, Component.translatable("jei.ae2pr.crystal_assembly_line.more_outputs",
                    recipe.getItemOutputs().size() - 1), PADDING, INFO_Y + 20, COLOR_HINT, false);
        }
    }

    /** 有序时标注该输入的序号。 */
    private static Component orderTooltip(int index, boolean ordered) {
        if (!ordered) {
            return Component.translatable("jei.ae2pr.crystal_assembly_line.slot.unordered")
                    .withStyle(ChatFormatting.GRAY);
        }
        return Component.translatable("jei.ae2pr.crystal_assembly_line.slot.index", index + 1)
                .withStyle(ChatFormatting.GRAY);
    }
}
