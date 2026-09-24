---
navigation:
  title: ME 通式阈值发信器
  parent: index.md
  icon: multi_threshold_level_emitter
  position: 3
item_ids:
  - ae2pr:multi_threshold_level_emitter
---

# ME 通式阈值发信器

<ItemImage id="multi_threshold_level_emitter" scale="2" />

<ItemLink id="multi_threshold_level_emitter" />在 ExtendedAE 阈值发信器的基础上，使配置槽兼容<ItemLink id="filter_cell" />，用于同时监控多个触发项。

## 使用方式

1. 与普通发信器一样安装到线缆上，右键打开界面。
2. 界面左侧工具栏：红石模式、模糊模式，以及 **AND/OR** 组合模式切换。
3. 配置槽：
   - 未放入过滤元件时：按原版方式"标记"单个配置项，配合阈值与红石模式输出；
   - 放入过滤元件时：以过滤元件中配置的所有项为触发项。

## 判定规则

- 高电平（HIGH）：某触发项库存数量 ≥ 上限视为满足；
- 低电平（LOW）：某触发项库存数量 < 下限视为满足；
- 阈值期间内保持当前状态；
- **AND**：全部触发项满足才输出；**OR**：任一触发项满足即输出。

未插入过滤元件时，行为与原版阈值发信器一致。
