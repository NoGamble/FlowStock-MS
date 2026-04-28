# Frontend Immersive Layout Redesign — Design Spec

**Date:** 2026-04-28
**Context:** 作业展示用的库存管理系统前端全面优化，目标是视觉精致、操作流畅、不显AI痕迹。

## 1. Goals

- 从"标准后台模板"升级为"沉浸式操作控制台"
- 减少弹窗，改用内联/分屏操作
- Dashboard 图表接入真实数据（可降级为演示数据）
- 修复 `alert()` 残留，统一 Naive UI 的 message 组件
- 配色克制，不出现大面积渐变、玻璃态等 AI 常用手法

## 2. Layout & Navigation

### 2.1 Sidebar
- 保留左侧深色侧边栏（`#001529`），菜单项不变（5个）
- **新增折叠功能**：底部加折叠按钮（箭头图标），收起时仅显示图标（64px），展开 200px
- **底部加版本号**小字：`v1.0`
- 折叠状态存 localStorage，刷新后保持

### 2.2 Content Area
- **去掉顶部标题栏**（当前 MainLayout 里的 `<n-layout-header>`）
- 页面标题融入各页面内容区第一行
- **去卡片化**：内容不包在 `n-card` 里，直接铺白色背景；图表区用极浅灰（`#f9fafb`）区分
- 页面过渡动画从 0.3s 加速到 0.2s

## 3. Per-Page Design

### 3.1 Dashboard（首页）

- **顶部指标带**：4 个指标横向排列，大号数字（保留 `n-number-animation`）+ 小字标签。指标：商品种类、总库存件数、今日入库、今日出库。去掉"较上周"趋势箭头（无真实数据支撑）。整行用很浅的品牌蓝底（`#e8f0fe`）。
- **折线图**：优先从后端统计接口获取真实出入库趋势数据；如果没有接口，保留静态演示数据但要标注"演示"。保持配色（绿入/蓝出）。
- **饼图**：沿用现有 top5 + 其他逻辑（数据已从 `getProductList` 计算得来），保持空心环样式（`radius: ['40%', '70%']`）。
- **低库存预警**：移到饼图下方同列，紧凑列表，每行商品名 + 红色库存数字，点击跳转入库页。
- **快捷操作**：4 个图标按钮横排在页面底部，hover 时图标轻微上浮（CSS `transform: translateY(-2px)`）。

### 3.2 商品管理

- **搜索框**：表格上方，输入实时过滤（前端 `filter`，匹配 `itemName` 和 `skuCode`）
- **表格列**：ID | SKU编码 | 商品名称 | 库存量（颜色标签）| 单位 | 操作
- **库存标签规则**：≥50 绿色，10-49 黄色，<10 红色
- **新增弹窗**：增加 SKU 编码和单位字段（`n-form-item`），当前 entity 已支持
- **编辑弹窗**：仅允许改名称、SKU、单位，库存量不显示在编辑表单中
- **空状态**：无商品时显示 Naive UI empty + 引导文字

### 3.3 入库操作台

- **分屏布局**：左栏 60% 宽（商品列表 + 搜索），右栏 40%（操作面板 + 最近入库历史）
- 左栏表格每行有"入库"按钮；点击后右栏更新
- 右栏操作面板：提示当前操作的商品名和库存数；数量输入框 + 快捷按钮（+10、+50、+100）；实时预览"库存变化：50 → 65"
- 右栏底部：最近 10 条入库记录（需后端提供 GET 接口，或前端从现有的 `getAllInboundRecords` 间接获取）
- **不弹窗**：一切在分屏内完成

### 3.4 出库操作台

- **分屏布局**：与入库对称，出库历史替代入库历史
- 库存为 0 的商品行变灰，按钮禁用
- 数量输入框实时预览，超出库存时输入框变红并提示
- 快捷按钮（-10、-50）
- 右栏底部显示最近 10 条出库记录

### 3.5 库存盘点

- **表格/时间线切换**：顶部 tab 按钮，默认表格视图；时间线视图将盘点记录按时间纵向排列，每条显示时间、商品、系统量→实盘量、盈亏标签
- **执行盘点面板**：侧边滑出或固定右侧面板（窄，约 360px），内联完成——选择商品 → 输入实盘数量 → 实时盈亏预览 → 确认。不弹窗。
- 历史表格与现有一致（盘盈盘亏标签列保留）

## 4. Cross-Cutting Improvements

### 4.1 Theme & Color
- 主色保持 `#0052D9`，但减少使用——仅用于关键操作按钮和选中态
- 正文/标题用深灰系（`#1a1a2e`, `#333`）
- 全局圆角 `8px`（Naive UI themeOverrides）
- 卡片阴影更浅更散：`0 1px 3px rgba(0,0,0,0.06)`

### 4.2 Typography
- 字体栈：`-apple-system, BlinkMacSystemFont, "Segoe UI", "PingFang SC", "Hiragino Sans GB", sans-serif`
- Dashboard 大数字用 `font-weight: 600`，正文 `400`

### 4.3 Error Handling
- **修复 `request.js`**：把响应拦截器里的 `alert()` 替换为 Naive UI `useMessage()`。由于拦截器在 Vue 实例外，需要创建一个 message 实例或通过事件总线处理。
- 所有用户可见错误统一走 message.error / message.warning

### 4.4 Loading & Empty States
- 表格 loading 改用 Naive UI `n-skeleton` 或表格内置 loading（已有）
- 空数据表格显示 `n-empty` 占位 + 对应引导文字

### 4.5 Animations
- Dashboard 统计数字：保留 `n-number-animation`
- 入库/出库成功后，受影响的库存数字可以闪烁或滚动更新
- 快捷操作按钮 hover：`transform: translateY(-2px); transition: 0.15s`
- 分屏面板切换时加一个很轻微的 slide 过渡

## 5. Backend Considerations

以下前端改动依赖后端支持，如果后端不改，前端可降级处理：

| 需求 | 前端降级方案 |
|------|-------------|
| Dashboard 折线图真实数据（需要统计接口） | 保留静态演示数据，标签写"演示数据" |
| 入库/出库历史记录列表（GET /api/stock-movements/inbound, /outbound） | 如果后端没暴露 GET 接口，右栏底部历史区显示"暂无记录" |
| 商品搜索需要 SKU 字段 | 后端 entity 已有 `skuCode`，前端需要确保返回字段包含它 |

## 6. Files to Modify

```
frontend/src/layouts/MainLayout.vue     — 去掉 header，加折叠，加版本号
frontend/src/views/dashboard/index.vue  — 指标带重排，图表接真实数据，预警移位置
frontend/src/views/product/index.vue    — 搜索栏，新列，库存标签，空状态
frontend/src/views/inbound/index.vue    — 分屏布局，操作面板，历史列表
frontend/src/views/outbound/index.vue   — 分屏布局，对称设计
frontend/src/views/stocktake/index.vue  — 时间线视图，内联执行面板
frontend/src/App.vue                    — 主题覆盖调整
frontend/src/api/request.js             — alert → message
frontend/src/api/movement.js            — 可能需要新增获取历史的 API 调用
```

## 7. Verification

1. `cd frontend && npm run dev`，确认所有页面无控制台报错
2. Dashboard：统计数字动画正常，饼图根据商品列表生成正确
3. 入库/出库：分屏操作，左栏搜索过滤正常，右栏操作成功后有库存变化反馈
4. 盘点：表格/时间线切换正常，执行盘点后记录刷新
5. 侧边栏折叠：点击折叠/展开，刷新页面后状态保持
6. 空状态：删除所有商品后，商品管理页显示 empty + 引导文字
7. 错误提示：触发一个错误操作，确认走的是 `message.error` 而非 `alert()`
