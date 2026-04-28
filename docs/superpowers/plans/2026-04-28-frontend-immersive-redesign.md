# Frontend Immersive Layout Redesign — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for enforcement.

**Goal:** Transform the FlowStock frontend from a standard admin-panel look into an immersive control-console with split-panel operations, refined typography, and zero modal-based workflows.

**Architecture:** Vue 3 SPA with Naive UI. Layout refactored to remove top header and add collapsible sidebar. Each operation page (inbound/outbound) gets a two-column split-panel layout. `alert()` calls in the Axios interceptor are replaced with a standalone Naive UI message instance. No new dependencies.

**Tech Stack:** Vue 3 (Composition API), Naive UI, ECharts (via vue-echarts), Axios, Vue Router

---

### Task 1: Fix Axios interceptor — replace alert() with Naive UI message

**Files:**
- Modify: `frontend/src/api/request.js`

- [ ] **Step 1: Create a standalone message instance and replace alert() calls**

Naive UI's `useMessage()` must be called inside `setup()`. Since the Axios interceptor runs outside Vue, we create a message instance via `createDiscreteApi`:

Replace the current `request.js` with:

```js
import axios from 'axios'
import { createDiscreteApi } from 'naive-ui'

const { message } = createDiscreteApi(['message'])

const service = axios.create({
  baseURL: '',
  timeout: 15000
})

service.interceptors.request.use(
  config => {
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

service.interceptors.response.use(
    response => {
      const res = response.data;

      if (res.code !== 200) {
        message.error(res.message || '执行出错');
        return Promise.reject(new Error(res.message || 'Error'));
      }
      return res.data;
    },
    error => {
      console.error('网络请求异常:', error);
      message.error('网络连接失败，请检查后端服务是否启动');
      return Promise.reject(error);
    }
)

export default service
```

- [ ] **Step 2: Verify no alert() remains**

Run: `grep -rn "alert(" frontend/src/` — should return no matches.

- [ ] **Step 3: Test with a forced error**

Temporarily change the backend URL in `request.js` baseURL to `http://localhost:9999`, run `cd frontend && npm run dev`, open the browser, navigate to any page, and confirm the error appears as a Naive UI toast notification (not a browser `alert()` dialog). Revert the baseURL change.

- [ ] **Step 4: Commit**

```bash
git add frontend/src/api/request.js
git commit -m "fix: replace alert() with Naive UI message in Axios interceptor"
```

---

### Task 2: Update App.vue theme overrides

**Files:**
- Modify: `frontend/src/App.vue`

- [ ] **Step 1: Adjust theme for more refined look**

Replace the `themeOverrides` in App.vue:

```js
const themeOverrides = {
  common: {
    primaryColor: '#0052D9',
    primaryColorHover: '#266FE8',
    primaryColorPressed: '#003CAB',
    borderRadius: '8px',
    fontSize: '14px',
    fontFamily: '-apple-system, BlinkMacSystemFont, "Segoe UI", "PingFang SC", "Hiragino Sans GB", sans-serif'
  },
  Card: {
    borderRadius: '8px',
    boxShadow: '0 1px 3px rgba(0, 0, 0, 0.06)'
  }
}
```

- [ ] **Step 2: Commit**

```bash
git add frontend/src/App.vue
git commit -m "style: refine global theme — increased border radius, lighter card shadow, system font stack"
```

---

### Task 3: Refactor MainLayout — remove header, add sidebar collapse

**Files:**
- Modify: `frontend/src/layouts/MainLayout.vue`

The n-layout-sider in Naive UI supports `collapsed` prop natively via v-model. We add the binding, remove `<n-layout-header>`, speed up transitions, and add a version label.

- [ ] **Step 1: Rewrite MainLayout template**

Replace the entire file:

```vue
<template>
  <n-layout has-sider style="height: 100vh;">
    <n-layout-sider
      bordered
      collapse-mode="width"
      :collapsed-width="64"
      :width="200"
      :collapsed="collapsed"
      style="background-color: #001529;"
    >
      <div class="logo">
        <span v-if="!collapsed" class="logo-text">FlowStock</span>
        <span v-else class="logo-text-short">FS</span>
      </div>
      <n-menu
        inverted
        :options="menuOptions"
        :value="activeKey"
        :collapsed="collapsed"
        @update:value="handleMenuClick"
      />
      <div class="sidebar-footer">
        <div class="collapse-btn" @click="collapsed = !collapsed">
          <n-icon :component="collapsed ? ChevronForwardOutline : ChevronBackOutline" />
        </div>
        <span v-if="!collapsed" class="version-text">v1.0</span>
      </div>
    </n-layout-sider>

    <n-layout>
      <n-layout-content content-style="padding: 0 24px 24px; background-color: #f5f6f8; min-height: 100vh;">
        <router-view v-slot="{ Component }">
          <transition name="fade-slide" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </n-layout-content>
    </n-layout>
  </n-layout>
</template>

<script setup>
import { ref, computed, h, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { NIcon } from 'naive-ui'
import {
  PieChartOutline,
  CubeOutline,
  LogInOutline,
  LogOutOutline,
  ClipboardOutline,
  ChevronBackOutline,
  ChevronForwardOutline
} from '@vicons/ionicons5'

const router = useRouter()
const route = useRoute()

const collapsed = ref(localStorage.getItem('sidebar-collapsed') === 'true')

watch(collapsed, (val) => {
  localStorage.setItem('sidebar-collapsed', String(val))
})

const activeKey = computed(() => {
  return route.path.replace('/', '') || 'dashboard'
})

function renderIcon(icon) {
  return () => h(NIcon, null, { default: () => h(icon) })
}

const menuOptions = [
  { label: '主页', key: 'dashboard', icon: renderIcon(PieChartOutline) },
  { label: '商品管理', key: 'product', icon: renderIcon(CubeOutline) },
  { label: '入库管理', key: 'inbound', icon: renderIcon(LogInOutline) },
  { label: '出库管理', key: 'outbound', icon: renderIcon(LogOutOutline) },
  { label: '库存盘点', key: 'stocktake', icon: renderIcon(ClipboardOutline) }
]

const handleMenuClick = (key) => {
  router.push(`/${key}`)
}
</script>

<style scoped>
.logo {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  white-space: nowrap;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}
.logo-text {
  color: white;
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 0.5px;
}
.logo-text-short {
  color: white;
  font-size: 16px;
  font-weight: 700;
}
.sidebar-footer {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 12px 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
}
.collapse-btn {
  color: rgba(255, 255, 255, 0.55);
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: color 0.15s;
}
.collapse-btn:hover {
  color: rgba(255, 255, 255, 0.9);
}
.version-text {
  color: rgba(255, 255, 255, 0.25);
  font-size: 11px;
  margin-top: 4px;
}

.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.2s cubic-bezier(0.25, 0.8, 0.25, 1);
}
.fade-slide-enter-from {
  opacity: 0;
  transform: translateX(8px);
}
.fade-slide-leave-to {
  opacity: 0;
  transform: translateX(-8px);
}
</style>
```

Key changes from original:
- Added `collapsed` ref backed by `localStorage`
- Removed `<n-layout-header>` entirely
- Added sidebar footer with collapse toggle button and version text
- Changed logo to show "FS" when collapsed, "FlowStock" when expanded
- Added `:collapsed` to `<n-menu>`
- Lightened content background from `#f0f2f5` to `#f5f6f8`
- Sped up transition from 0.3s to 0.2s
- Shortened translate distance from 10px to 8px

- [ ] **Step 2: Verify layout renders**

Run: `cd frontend && npm run dev`. Open browser, confirm:
- Sidebar shows with logo at top, menu items, collapse button and "v1.0" at bottom
- No top header bar
- Click collapse button → sidebar shrinks to 64px icons only, "FS" logo shown
- Refresh page → collapse state persists
- Navigate between pages → transition works, menu highlights active item

- [ ] **Step 3: Commit**

```bash
git add frontend/src/layouts/MainLayout.vue
git commit -m "feat: collapsible sidebar, remove top header, add version label"
```

---

### Task 4: Redesign Dashboard

**Files:**
- Modify: `frontend/src/views/dashboard/index.vue`

- [ ] **Step 1: Rewrite dashboard template — metric strip at top, restructured content**

Replace the entire file:

```vue
<template>
  <div class="dashboard">
    <!-- Top metric strip -->
    <div class="metric-strip">
      <div class="metric-item" v-for="m in metrics" :key="m.label">
        <div class="metric-label">{{ m.label }}</div>
        <div class="metric-value">
          <n-number-animation :from="0" :to="m.value" />
        </div>
      </div>
    </div>

    <!-- Charts row -->
    <div class="charts-row">
      <div class="chart-box chart-trend">
        <div class="chart-header">
          <h3 class="chart-title">出入库动态趋势</h3>
          <n-tag size="small" type="default" :bordered="false">演示数据</n-tag>
        </div>
        <v-chart class="chart" :option="trendOption" autoresize />
      </div>
      <div class="chart-box chart-pie">
        <div class="chart-header">
          <h3 class="chart-title">库存分类占比</h3>
        </div>
        <v-chart class="chart" :option="pieOption" autoresize />
        <!-- Low stock alerts below pie -->
        <div class="low-stock" v-if="warningItems.length">
          <div class="low-stock-header">低库存预警</div>
          <div
            class="low-stock-item"
            v-for="item in warningItems"
            :key="item.id"
            @click="router.push('/inbound')"
          >
            <span class="low-stock-name">{{ item.name }}</span>
            <span class="low-stock-count">仅剩 {{ item.stock }} 件</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Quick actions -->
    <div class="quick-actions">
      <div
        class="quick-action-btn"
        v-for="action in fastActions"
        :key="action.name"
        @click="router.push(action.path)"
      >
        <n-icon size="22" :component="action.icon" />
        <span>{{ action.name }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, markRaw } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { getProductList } from '@/api/product'
import {
  CubeOutline, LogInOutline, LogOutOutline, ClipboardOutline, AddCircleOutline
} from '@vicons/ionicons5'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, PieChart } from 'echarts/charts'
import {
  TitleComponent, TooltipComponent, LegendComponent,
  GridComponent, DatasetComponent
} from 'echarts/components'
import VChart from 'vue-echarts'

use([
  CanvasRenderer, LineChart, PieChart, GridComponent,
  TitleComponent, TooltipComponent, LegendComponent, DatasetComponent
])

const router = useRouter()
const message = useMessage()

const metrics = ref([
  { label: '商品种类', value: 0 },
  { label: '总库存 (件)', value: 0 },
  { label: '今日入库', value: 0 },
  { label: '今日出库', value: 0 }
])

const warningItems = ref([])

const fastActions = [
  { name: '录入商品', path: '/product', icon: markRaw(AddCircleOutline) },
  { name: '处理入库', path: '/inbound', icon: markRaw(LogInOutline) },
  { name: '处理出库', path: '/outbound', icon: markRaw(LogOutOutline) },
  { name: '发起盘点', path: '/stocktake', icon: markRaw(ClipboardOutline) }
]

const fetchDashboardData = async () => {
  try {
    const products = await getProductList() || []

    const totalTypes = products.length
    const totalStock = products.reduce((sum, p) => sum + (p.currentQuantity || 0), 0)

    metrics.value[0].value = totalTypes
    metrics.value[1].value = totalStock

    // Pie data: top 5 + others
    const sorted = [...products].sort((a, b) => (b.currentQuantity || 0) - (a.currentQuantity || 0))
    const top5 = sorted.slice(0, 5)
    let othersStock = 0
    if (sorted.length > 5) {
      othersStock = sorted.slice(5).reduce((sum, p) => sum + (p.currentQuantity || 0), 0)
    }
    const pieData = top5.map(p => ({ value: p.currentQuantity || 0, name: p.itemName }))
    if (othersStock > 0) pieData.push({ value: othersStock, name: '其他' })
    pieOption.value.series[0].data = pieData

    // Low stock: <= 10
    warningItems.value = sorted
      .filter(p => (p.currentQuantity || 0) <= 10)
      .map(p => ({
        id: p.id,
        name: p.itemName,
        stock: p.currentQuantity || 0
      }))
  } catch (err) {
    message.error('获取仪表盘数据失败')
  }
}

onMounted(() => fetchDashboardData())

const trendOption = ref({
  tooltip: { trigger: 'axis' },
  legend: { data: ['入库数量', '出库数量'], bottom: 0 },
  grid: { left: '3%', right: '4%', bottom: '10%', containLabel: true },
  xAxis: { type: 'category', boundaryGap: false, data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'] },
  yAxis: { type: 'value' },
  series: [
    {
      name: '入库数量', type: 'line', smooth: true,
      data: [120, 132, 101, 134, 90, 230, 210],
      itemStyle: { color: '#18a058' },
      areaStyle: {
        color: {
          type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [{ offset: 0, color: 'rgba(24,160,88,0.25)' }, { offset: 1, color: 'rgba(24,160,88,0.02)' }]
        }
      }
    },
    {
      name: '出库数量', type: 'line', smooth: true,
      data: [220, 182, 191, 234, 290, 330, 310],
      itemStyle: { color: '#2080f0' },
      areaStyle: {
        color: {
          type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [{ offset: 0, color: 'rgba(32,128,240,0.25)' }, { offset: 1, color: 'rgba(32,128,240,0.02)' }]
        }
      }
    }
  ]
})

const pieOption = ref({
  tooltip: { trigger: 'item' },
  legend: { top: 'bottom' },
  series: [{
    name: '库存分布', type: 'pie',
    radius: ['40%', '70%'],
    avoidLabelOverlap: false,
    itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
    label: { show: false },
    emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold' } },
    labelLine: { show: false },
    data: []
  }]
})
</script>

<style scoped>
.dashboard {
  padding-top: 24px;
}

/* Metric strip */
.metric-strip {
  display: flex;
  gap: 1px;
  background: #e8f0fe;
  border-radius: 10px;
  overflow: hidden;
  margin-bottom: 20px;
}
.metric-item {
  flex: 1;
  padding: 20px 24px;
  background: #e8f0fe;
}
.metric-label {
  font-size: 13px;
  color: #5a7fb5;
  margin-bottom: 4px;
}
.metric-value {
  font-size: 30px;
  font-weight: 600;
  color: #1a2d4a;
}

/* Charts */
.charts-row {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
}
.chart-box {
  background: #fff;
  border-radius: 10px;
  padding: 20px;
}
.chart-trend {
  flex: 2;
}
.chart-pie {
  flex: 1;
  display: flex;
  flex-direction: column;
}
.chart-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
.chart-title {
  font-size: 15px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0;
}
.chart {
  width: 100%;
  flex: 1;
  min-height: 280px;
}

/* Low stock */
.low-stock {
  margin-top: 12px;
  border-top: 1px solid #f0f0f0;
  padding-top: 12px;
}
.low-stock-header {
  font-size: 12px;
  font-weight: 600;
  color: #d03050;
  margin-bottom: 8px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}
.low-stock-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 0;
  cursor: pointer;
  border-radius: 4px;
  padding: 6px 8px;
  transition: background 0.15s;
}
.low-stock-item:hover {
  background: #fef0f0;
}
.low-stock-name {
  font-size: 13px;
  color: #333;
}
.low-stock-count {
  font-size: 13px;
  font-weight: 600;
  color: #d03050;
}

/* Quick actions */
.quick-actions {
  display: flex;
  gap: 12px;
}
.quick-action-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 14px 0;
  background: #fff;
  border-radius: 10px;
  font-size: 14px;
  color: #333;
  cursor: pointer;
  transition: transform 0.15s, box-shadow 0.15s;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
}
.quick-action-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
  color: #0052D9;
}
</style>
```

- [ ] **Step 2: Verify dashboard renders correctly**

Run: `cd frontend && npm run dev`. Open browser to `/dashboard`:
- Metric strip shows 4 metrics with number animations
- Line chart and pie chart display side by side
- If products exist in DB, pie chart shows distribution and low-stock list appears
- Quick action buttons at bottom, hover shows lift effect
- Clicking a low-stock item navigates to `/inbound`

- [ ] **Step 3: Commit**

```bash
git add frontend/src/views/dashboard/index.vue
git commit -m "feat: redesigned dashboard — metric strip, refined charts, compact low-stock, hover quick-actions"
```

---

### Task 5: Redesign Product Management

**Files:**
- Modify: `frontend/src/views/product/index.vue`

- [ ] **Step 1: Rewrite product page — search, SKU/unit columns, color tags, empty state**

Replace the entire file:

```vue
<template>
  <div class="product-page">
    <div class="page-top">
      <h2 class="page-title">商品管理</h2>
      <div class="top-actions">
        <n-input
          v-model:value="searchQuery"
          placeholder="搜索商品名称或 SKU..."
          clearable
          style="width: 260px;"
        >
          <template #prefix>
            <n-icon :component="SearchOutline" />
          </template>
        </n-input>
        <n-button type="primary" @click="openCreateModal">
          <template #icon><n-icon :component="AddOutline" /></template>
          新增商品
        </n-button>
      </div>
    </div>

    <n-data-table
      :columns="columns"
      :data="filteredData"
      :loading="loading"
      :pagination="pagination"
      :bordered="false"
      :bottom-bordered="true"
      striped
      size="large"
    >
      <template #empty>
        <n-empty description="还没有任何商品，点击右上角「新增商品」开始">
          <template #extra>
            <n-button type="primary" @click="openCreateModal">新增商品</n-button>
          </template>
        </n-empty>
      </template>
    </n-data-table>

    <n-modal v-model:show="showModal" preset="card" :title="modalTitle" style="width: 480px;">
      <n-form ref="formRef" :model="formData" :rules="rules" label-placement="left" label-width="100">
        <n-form-item label="商品名称" path="itemName">
          <n-input v-model:value="formData.itemName" placeholder="请输入商品名称" />
        </n-form-item>
        <n-form-item label="SKU 编码" path="skuCode">
          <n-input v-model:value="formData.skuCode" placeholder="如 PRD-00001" />
        </n-form-item>
        <n-form-item label="单位" path="unit">
          <n-input v-model:value="formData.unit" placeholder="如 件、个、箱" />
        </n-form-item>
        <n-form-item v-if="isCreate" label="初始库存" path="currentQuantity">
          <n-input-number v-model:value="formData.currentQuantity" :min="0" placeholder="0" style="width: 100%;" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showModal = false">取消</n-button>
          <n-button type="primary" :loading="submitLoading" @click="handleSubmit">确认</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, h, onMounted, computed } from 'vue'
import { NButton, NSpace, useMessage, NPopconfirm, NIcon, NTag } from 'naive-ui'
import { AddOutline, SearchOutline } from '@vicons/ionicons5'
import { getProductList, createProduct, updateProduct, deleteProduct } from '@/api/product'

const message = useMessage()
const loading = ref(false)
const tableData = ref([])
const searchQuery = ref('')
const pagination = ref({ pageSize: 10 })

const filteredData = computed(() => {
  if (!searchQuery.value) return tableData.value
  const q = searchQuery.value.toLowerCase()
  return tableData.value.filter(row =>
    (row.itemName || '').toLowerCase().includes(q) ||
    (row.skuCode || '').toLowerCase().includes(q)
  )
})

function stockTag(quantity) {
  if (quantity >= 50) return h(NTag, { type: 'success', size: 'small', round: true }, { default: () => quantity })
  if (quantity >= 10) return h(NTag, { type: 'warning', size: 'small', round: true }, { default: () => quantity })
  return h(NTag, { type: 'error', size: 'small', round: true }, { default: () => quantity })
}

const columns = [
  { title: 'ID', key: 'id', width: 60 },
  { title: 'SKU 编码', key: 'skuCode', width: 140, ellipsis: { tooltip: true } },
  { title: '商品名称', key: 'itemName', ellipsis: { tooltip: true } },
  {
    title: '库存量', key: 'currentQuantity', width: 100,
    render(row) { return stockTag(row.currentQuantity) }
  },
  { title: '单位', key: 'unit', width: 70 },
  {
    title: '操作', key: 'actions', width: 140,
    render(row) {
      return h(NSpace, {}, {
        default: () => [
          h(NButton, { size: 'small', type: 'info', tertiary: true, onClick: () => openEditModal(row) }, { default: () => '编辑' }),
          h(NPopconfirm, { onPositiveClick: () => handleDelete(row.id) }, {
            trigger: () => h(NButton, { size: 'small', type: 'error', tertiary: true }, { default: () => '删除' }),
            default: () => '确定要删除该商品吗？'
          })
        ]
      })
    }
  }
]

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getProductList()
    tableData.value = res || []
  } catch (err) {
    message.error('获取商品列表失败')
  } finally {
    loading.value = false
  }
}

const showModal = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)
const isCreate = ref(true)
const formData = ref({ id: null, itemName: '', skuCode: '', unit: '', currentQuantity: 0 })

const rules = {
  itemName: { required: true, message: '请输入商品名称', trigger: 'blur' }
}

const modalTitle = computed(() => isCreate.value ? '新增商品' : '编辑商品信息')

const openCreateModal = () => {
  isCreate.value = true
  formData.value = { id: null, itemName: '', skuCode: '', unit: '', currentQuantity: 0 }
  showModal.value = true
}

const openEditModal = (row) => {
  isCreate.value = false
  formData.value = {
    id: row.id,
    itemName: row.itemName,
    skuCode: row.skuCode || '',
    unit: row.unit || '',
    currentQuantity: row.currentQuantity
  }
  showModal.value = true
}

const handleSubmit = (e) => {
  e.preventDefault()
  formRef.value?.validate(async (errors) => {
    if (!errors) {
      submitLoading.value = true
      try {
        if (isCreate.value) {
          await createProduct({
            itemName: formData.value.itemName,
            skuCode: formData.value.skuCode,
            unit: formData.value.unit,
            currentQuantity: formData.value.currentQuantity
          })
          message.success('商品创建成功')
        } else {
          await updateProduct(formData.value.id, {
            itemName: formData.value.itemName,
            skuCode: formData.value.skuCode,
            unit: formData.value.unit
          })
          message.success('商品信息更新成功')
        }
        showModal.value = false
        fetchData()
      } catch (err) {
        message.error('操作失败')
      } finally {
        submitLoading.value = false
      }
    }
  })
}

const handleDelete = async (id) => {
  try {
    await deleteProduct(id)
    message.success('商品删除成功')
    fetchData()
  } catch (err) {
    message.error('删除失败')
  }
}

onMounted(() => fetchData())
</script>

<style scoped>
.product-page {
  padding-top: 24px;
}
.page-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0;
}
.top-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}
</style>
```

- [ ] **Step 2: Verify product page**

Run: `cd frontend && npm run dev`. Navigate to `/product`:
- Page title "商品管理" in content area (not card header)
- Search bar filters by name and SKU in real time
- Columns: ID, SKU编码, 商品名称, 库存量(tag), 单位, 操作
- Stock tags: green (≥50), yellow (10-49), red (<10)
- Create modal has name, SKU, unit, and initial quantity fields
- Edit modal has name, SKU, unit only (no quantity)
- Delete all products → empty state with guidance and action button

- [ ] **Step 3: Commit**

```bash
git add frontend/src/views/product/index.vue
git commit -m "feat: enhanced product page — search, SKU/unit columns, color-coded stock tags, empty state"
```

---

### Task 6: Redesign Inbound — split-panel layout

**Files:**
- Modify: `frontend/src/views/inbound/index.vue`

- [ ] **Step 1: Rewrite inbound page**

Replace the entire file:

```vue
<template>
  <div class="inbound-page">
    <h2 class="page-title">入库操作台</h2>

    <div class="split-panel">
      <!-- Left: Product list -->
      <div class="panel-left">
        <n-input
          v-model:value="searchQuery"
          placeholder="搜索商品..."
          clearable
          style="margin-bottom: 12px;"
        >
          <template #prefix><n-icon :component="SearchOutline" /></template>
        </n-input>
        <n-data-table
          :columns="productColumns"
          :data="filteredProducts"
          :loading="loading"
          :pagination="{ pageSize: 8 }"
          :bordered="false"
          size="small"
          :row-props="rowProps"
        />
      </div>

      <!-- Right: Operation panel + history -->
      <div class="panel-right">
        <div class="op-panel" v-if="selectedProduct">
          <div class="op-header">
            <n-tag type="info" :bordered="false">正在操作</n-tag>
            <span class="op-product-name">{{ selectedProduct.itemName }}</span>
          </div>
          <div class="op-stock-info">
            当前库存：<strong>{{ selectedProduct.currentQuantity }}</strong>
          </div>

          <div class="op-input-area">
            <n-input-number
              v-model:value="inboundAmount"
              :min="1"
              placeholder="入库数量"
              style="width: 100%;"
              size="large"
            />
            <div class="quick-btns">
              <n-button size="small" @click="addAmount(10)">+10</n-button>
              <n-button size="small" @click="addAmount(50)">+50</n-button>
              <n-button size="small" @click="addAmount(100)">+100</n-button>
            </div>
          </div>

          <div class="op-preview" v-if="inboundAmount > 0">
            库存变化：
            <span class="preview-from">{{ selectedProduct.currentQuantity }}</span>
            <n-icon :component="ArrowForwardOutline" size="14" />
            <span class="preview-to">{{ selectedProduct.currentQuantity + inboundAmount }}</span>
          </div>

          <n-button
            type="primary"
            block
            :loading="submitting"
            @click="executeInbound"
            style="margin-top: 12px;"
          >
            确认入库 {{ inboundAmount }} 件
          </n-button>
        </div>

        <div class="op-panel op-empty" v-else>
          <n-empty description="点击左侧商品开始入库" style="padding: 40px 0;" />
        </div>

        <div class="history-section">
          <div class="history-header">最近入库记录</div>
          <div class="history-list" v-if="recentRecords.length">
            <div class="history-item" v-for="r in recentRecords" :key="r.id">
              <span class="history-name">{{ r.inventory?.itemName || '—' }}</span>
              <span class="history-qty">+{{ r.quantity }}</span>
              <span class="history-time">{{ formatTime(r.inboundTime) }}</span>
            </div>
          </div>
          <n-empty v-else description="暂无记录" size="small" style="padding: 20px 0;" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, h, onMounted, computed } from 'vue'
import { NButton, NSpace, useMessage, NIcon, NTag } from 'naive-ui'
import { SearchOutline, ArrowForwardOutline, LogInOutline } from '@vicons/ionicons5'
import { getProductList } from '@/api/product'
import { inboundAction } from '@/api/movement'

const message = useMessage()
const loading = ref(false)
const products = ref([])
const searchQuery = ref('')
const selectedProduct = ref(null)
const inboundAmount = ref(1)
const submitting = ref(false)
const recentRecords = ref([])

const filteredProducts = computed(() => {
  if (!searchQuery.value) return products.value
  const q = searchQuery.value.toLowerCase()
  return products.value.filter(p => (p.itemName || '').toLowerCase().includes(q))
})

const productColumns = [
  { title: '商品名称', key: 'itemName', ellipsis: { tooltip: true } },
  {
    title: '库存', key: 'currentQuantity', width: 80,
    render(row) {
      if (row.currentQuantity <= 10)
        return h(NTag, { type: 'error', size: 'small', round: true }, { default: () => row.currentQuantity })
      return h(NTag, { type: 'success', size: 'small', round: true }, { default: () => row.currentQuantity })
    }
  },
  {
    title: '', key: 'action', width: 80,
    render(row) {
      return h(NButton, {
        size: 'small',
        type: row.id === selectedProduct.value?.id ? 'primary' : 'default',
        onClick: () => { selectedProduct.value = row; inboundAmount.value = 1 }
      }, { default: () => '入库' })
    }
  }
]

function rowProps(row) {
  return {
    style: row.id === selectedProduct.value?.id ? 'background: #e8f0fe;' : ''
  }
}

function addAmount(n) {
  inboundAmount.value += n
}

const executeInbound = async () => {
  if (!selectedProduct.value) return
  submitting.value = true
  try {
    await inboundAction({ itemId: selectedProduct.value.id, amount: inboundAmount.value })
    message.success(`成功入库 ${inboundAmount.value} 件 ${selectedProduct.value.itemName}`)

    // Add to local history
    recentRecords.value.unshift({
      id: Date.now(),
      inventory: { itemName: selectedProduct.value.itemName },
      quantity: inboundAmount.value,
      inboundTime: new Date().toISOString()
    })
    if (recentRecords.value.length > 10) recentRecords.value.pop()

    // Update local product stock
    const idx = products.value.findIndex(p => p.id === selectedProduct.value.id)
    if (idx >= 0) {
      products.value[idx] = {
        ...products.value[idx],
        currentQuantity: products.value[idx].currentQuantity + inboundAmount.value
      }
    }
    if (selectedProduct.value) {
      selectedProduct.value = {
        ...selectedProduct.value,
        currentQuantity: selectedProduct.value.currentQuantity + inboundAmount.value
      }
    }

    inboundAmount.value = 1
  } catch (err) {
    message.error('入库操作失败')
  } finally {
    submitting.value = false
  }
}

function formatTime(t) {
  if (!t) return ''
  return t.replace('T', ' ').substring(0, 19)
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getProductList()
    products.value = res || []
  } catch (err) {
    message.error('获取商品列表失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => fetchData())
</script>

<style scoped>
.inbound-page {
  padding-top: 24px;
  height: calc(100vh - 24px);
  display: flex;
  flex-direction: column;
}
.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0 0 16px;
  flex-shrink: 0;
}
.split-panel {
  display: flex;
  gap: 16px;
  flex: 1;
  min-height: 0;
}
.panel-left {
  flex: 3;
  background: #fff;
  border-radius: 10px;
  padding: 16px;
  overflow: auto;
}
.panel-right {
  flex: 2;
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-width: 320px;
}
.op-panel {
  background: #fff;
  border-radius: 10px;
  padding: 20px;
}
.op-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}
.op-product-name {
  font-weight: 600;
  font-size: 16px;
  color: #1a1a2e;
}
.op-stock-info {
  font-size: 13px;
  color: #666;
  margin-bottom: 16px;
}
.op-input-area {
  display: flex;
  gap: 8px;
  align-items: flex-start;
}
.quick-btns {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.op-preview {
  margin-top: 12px;
  font-size: 14px;
  color: #555;
  display: flex;
  align-items: center;
  gap: 6px;
}
.preview-from {
  color: #999;
  font-weight: 500;
}
.preview-to {
  color: #18a058;
  font-weight: 700;
  font-size: 18px;
}
.op-empty {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}
.history-section {
  background: #fff;
  border-radius: 10px;
  padding: 16px;
  flex: 1;
  overflow: auto;
}
.history-header {
  font-size: 13px;
  font-weight: 600;
  color: #888;
  margin-bottom: 8px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}
.history-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 0;
  border-bottom: 1px solid #f5f5f5;
  font-size: 13px;
}
.history-name {
  flex: 1;
  color: #333;
}
.history-qty {
  color: #18a058;
  font-weight: 600;
}
.history-time {
  color: #aaa;
  font-size: 12px;
  white-space: nowrap;
}
</style>
```

- [ ] **Step 2: Verify inbound page**

Run: `cd frontend && npm run dev`. Navigate to `/inbound`:
- Split panel: left product list, right operation panel
- Click a product in left → right panel shows operation form with product name and current stock
- +10/+50/+100 quick-add buttons work
- Stock preview shows "50 → 65" style change
- Executing inbound updates local product stock and adds to history
- Search in left panel filters products

- [ ] **Step 3: Commit**

```bash
git add frontend/src/views/inbound/index.vue
git commit -m "feat: split-panel inbound — product browser, inline operation, quick-add, stock preview"
```

---

### Task 7: Redesign Outbound — symmetric split-panel

**Files:**
- Modify: `frontend/src/views/outbound/index.vue`

- [ ] **Step 1: Rewrite outbound page**

Replace the entire file:

```vue
<template>
  <div class="outbound-page">
    <h2 class="page-title">出库操作台</h2>

    <div class="split-panel">
      <!-- Left: Product list -->
      <div class="panel-left">
        <n-input
          v-model:value="searchQuery"
          placeholder="搜索商品..."
          clearable
          style="margin-bottom: 12px;"
        >
          <template #prefix><n-icon :component="SearchOutline" /></template>
        </n-input>
        <n-data-table
          :columns="productColumns"
          :data="filteredProducts"
          :loading="loading"
          :pagination="{ pageSize: 8 }"
          :bordered="false"
          size="small"
          :row-props="rowProps"
        />
      </div>

      <!-- Right: Operation panel + history -->
      <div class="panel-right">
        <div class="op-panel" v-if="selectedProduct">
          <div class="op-header">
            <n-tag type="warning" :bordered="false">正在出库</n-tag>
            <span class="op-product-name">{{ selectedProduct.itemName }}</span>
          </div>
          <div class="op-stock-info">
            可用库存：<strong>{{ selectedProduct.currentQuantity }}</strong>
          </div>

          <div class="op-input-area">
            <n-input-number
              v-model:value="outboundAmount"
              :min="1"
              :max="selectedProduct.currentQuantity"
              placeholder="出库数量"
              style="width: 100%;"
              size="large"
              :status="outboundAmount > selectedProduct.currentQuantity ? 'error' : undefined"
            />
            <div class="quick-btns">
              <n-button size="small" @click="subtractAmount(10)">-10</n-button>
              <n-button size="small" @click="subtractAmount(50)">-50</n-button>
            </div>
          </div>

          <div class="op-preview" v-if="outboundAmount > 0">
            库存变化：
            <span class="preview-from">{{ selectedProduct.currentQuantity }}</span>
            <n-icon :component="ArrowForwardOutline" size="14" />
            <span
              class="preview-to"
              :class="{ 'preview-danger': selectedProduct.currentQuantity - outboundAmount < 0 }"
            >
              {{ selectedProduct.currentQuantity - outboundAmount }}
            </span>
          </div>
          <div class="op-warning" v-if="outboundAmount > selectedProduct.currentQuantity">
            出库数量超出当前库存！
          </div>

          <n-button
            type="error"
            block
            :loading="submitting"
            :disabled="outboundAmount > selectedProduct.currentQuantity || selectedProduct.currentQuantity <= 0"
            @click="executeOutbound"
            style="margin-top: 12px;"
          >
            确认出库 {{ outboundAmount }} 件
          </n-button>
        </div>

        <div class="op-panel op-empty" v-else>
          <n-empty description="点击左侧商品开始出库" style="padding: 40px 0;" />
        </div>

        <div class="history-section">
          <div class="history-header">最近出库记录</div>
          <div class="history-list" v-if="recentRecords.length">
            <div class="history-item" v-for="r in recentRecords" :key="r.id">
              <span class="history-name">{{ r.inventory?.itemName || '—' }}</span>
              <span class="history-qty">-{{ r.quantity }}</span>
              <span class="history-time">{{ formatTime(r.outboundTime) }}</span>
            </div>
          </div>
          <n-empty v-else description="暂无记录" size="small" style="padding: 20px 0;" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, h, onMounted, computed } from 'vue'
import { NButton, useMessage, NIcon, NTag } from 'naive-ui'
import { SearchOutline, ArrowForwardOutline, LogOutOutline } from '@vicons/ionicons5'
import { getProductList } from '@/api/product'
import { outboundAction } from '@/api/movement'

const message = useMessage()
const loading = ref(false)
const products = ref([])
const searchQuery = ref('')
const selectedProduct = ref(null)
const outboundAmount = ref(1)
const submitting = ref(false)
const recentRecords = ref([])

const filteredProducts = computed(() => {
  if (!searchQuery.value) return products.value
  const q = searchQuery.value.toLowerCase()
  return products.value.filter(p => (p.itemName || '').toLowerCase().includes(q))
})

const productColumns = [
  { title: '商品名称', key: 'itemName', ellipsis: { tooltip: true } },
  {
    title: '库存', key: 'currentQuantity', width: 80,
    render(row) {
      if (row.currentQuantity <= 0)
        return h(NTag, { type: 'error', size: 'small', round: true }, { default: () => '已耗尽' })
      if (row.currentQuantity <= 10)
        return h(NTag, { type: 'error', size: 'small', round: true }, { default: () => row.currentQuantity })
      return h(NTag, { type: 'success', size: 'small', round: true }, { default: () => row.currentQuantity })
    }
  },
  {
    title: '', key: 'action', width: 80,
    render(row) {
      return h(NButton, {
        size: 'small',
        type: row.id === selectedProduct.value?.id ? 'error' : 'default',
        disabled: row.currentQuantity <= 0,
        onClick: () => { selectedProduct.value = row; outboundAmount.value = 1 }
      }, { default: () => '出库' })
    }
  }
]

function rowProps(row) {
  if (row.currentQuantity <= 0) return { style: 'opacity: 0.4;' }
  if (row.id === selectedProduct.value?.id) return { style: 'background: #fef0f0;' }
  return {}
}

function subtractAmount(n) {
  outboundAmount.value = Math.max(1, outboundAmount.value + n)
}

const executeOutbound = async () => {
  if (!selectedProduct.value || outboundAmount.value > selectedProduct.value.currentQuantity) return
  submitting.value = true
  try {
    await outboundAction({ itemId: selectedProduct.value.id, amount: outboundAmount.value })
    message.success(`成功出库 ${outboundAmount.value} 件 ${selectedProduct.value.itemName}`)

    recentRecords.value.unshift({
      id: Date.now(),
      inventory: { itemName: selectedProduct.value.itemName },
      quantity: outboundAmount.value,
      outboundTime: new Date().toISOString()
    })
    if (recentRecords.value.length > 10) recentRecords.value.pop()

    const idx = products.value.findIndex(p => p.id === selectedProduct.value.id)
    if (idx >= 0) {
      products.value[idx] = {
        ...products.value[idx],
        currentQuantity: products.value[idx].currentQuantity - outboundAmount.value
      }
    }
    if (selectedProduct.value) {
      selectedProduct.value = {
        ...selectedProduct.value,
        currentQuantity: selectedProduct.value.currentQuantity - outboundAmount.value
      }
    }

    outboundAmount.value = 1
  } catch (err) {
    message.error('出库操作失败，可能库存不足')
  } finally {
    submitting.value = false
  }
}

function formatTime(t) {
  if (!t) return ''
  return t.replace('T', ' ').substring(0, 19)
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getProductList()
    products.value = res || []
  } catch (err) {
    message.error('获取商品列表失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => fetchData())
</script>

<style scoped>
.outbound-page {
  padding-top: 24px;
  height: calc(100vh - 24px);
  display: flex;
  flex-direction: column;
}
.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0 0 16px;
  flex-shrink: 0;
}
.split-panel {
  display: flex;
  gap: 16px;
  flex: 1;
  min-height: 0;
}
.panel-left {
  flex: 3;
  background: #fff;
  border-radius: 10px;
  padding: 16px;
  overflow: auto;
}
.panel-right {
  flex: 2;
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-width: 320px;
}
.op-panel {
  background: #fff;
  border-radius: 10px;
  padding: 20px;
}
.op-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}
.op-product-name {
  font-weight: 600;
  font-size: 16px;
  color: #1a1a2e;
}
.op-stock-info {
  font-size: 13px;
  color: #666;
  margin-bottom: 16px;
}
.op-input-area {
  display: flex;
  gap: 8px;
  align-items: flex-start;
}
.quick-btns {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.op-preview {
  margin-top: 12px;
  font-size: 14px;
  color: #555;
  display: flex;
  align-items: center;
  gap: 6px;
}
.preview-from {
  color: #999;
  font-weight: 500;
}
.preview-to {
  color: #2080f0;
  font-weight: 700;
  font-size: 18px;
}
.preview-danger {
  color: #d03050;
}
.op-warning {
  color: #d03050;
  font-size: 12px;
  margin-top: 4px;
}
.op-empty {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}
.history-section {
  background: #fff;
  border-radius: 10px;
  padding: 16px;
  flex: 1;
  overflow: auto;
}
.history-header {
  font-size: 13px;
  font-weight: 600;
  color: #888;
  margin-bottom: 8px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}
.history-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 0;
  border-bottom: 1px solid #f5f5f5;
  font-size: 13px;
}
.history-name {
  flex: 1;
  color: #333;
}
.history-qty {
  color: #d03050;
  font-weight: 600;
}
.history-time {
  color: #aaa;
  font-size: 12px;
  white-space: nowrap;
}
</style>
```

- [ ] **Step 2: Verify outbound page**

Run: `cd frontend && npm run dev`. Navigate to `/outbound`:
- Split panel layout symmetric with inbound
- Products with 0 stock appear grayed out, button disabled
- Stock preview shows "50 → 40" style
- Exceeding stock shows red preview and warning, button disabled
- Executing outbound updates local state and adds to history

- [ ] **Step 3: Commit**

```bash
git add frontend/src/views/outbound/index.vue
git commit -m "feat: split-panel outbound — symmetric layout, stock validation, inline history"
```

---

### Task 8: Redesign Stocktake — timeline toggle + inline execution panel

**Files:**
- Modify: `frontend/src/views/stocktake/index.vue`

- [ ] **Step 1: Rewrite stocktake page**

Replace the entire file:

```vue
<template>
  <div class="stocktake-page">
    <div class="split-panel">
      <!-- Left: History section -->
      <div class="panel-left">
        <div class="panel-header">
          <h2 class="page-title">库存盘点</h2>
          <n-button-group>
            <n-button :type="viewMode === 'table' ? 'primary' : 'default'" size="small" @click="viewMode = 'table'">
              <template #icon><n-icon :component="GridOutline" /></template>
            </n-button>
            <n-button :type="viewMode === 'timeline' ? 'primary' : 'default'" size="small" @click="viewMode = 'timeline'">
              <template #icon><n-icon :component="TimeOutline" /></template>
            </n-button>
          </n-button-group>
        </div>

        <!-- Table view -->
        <n-data-table
          v-if="viewMode === 'table'"
          :columns="columns"
          :data="tableData"
          :loading="loading"
          :pagination="{ pageSize: 10 }"
          :bordered="false"
          size="small"
        />

        <!-- Timeline view -->
        <div class="timeline-view" v-else>
          <n-timeline>
            <n-timeline-item
              v-for="r in tableData"
              :key="r.id"
              :type="timelineType(r.diffQuantity)"
              :time="formatTime(r.stocktakeTime)"
            >
              <template #header>
                {{ r.itemName || '未知商品' }}
              </template>
              系统库存 <strong>{{ r.systemQuantity }}</strong> → 实盘 <strong>{{ r.actualQuantity }}</strong>
              <n-tag :type="diffTagType(r.diffQuantity)" size="tiny" style="margin-left: 8px;">
                {{ diffLabel(r.diffQuantity) }}
              </n-tag>
            </n-timeline-item>
          </n-timeline>
          <n-empty v-if="!tableData.length" description="暂无盘点记录" />
        </div>
      </div>

      <!-- Right: Execute panel -->
      <div class="panel-right">
        <div class="execute-panel">
          <h3 class="exec-title">执行盘点</h3>
          <n-form ref="formRef" :model="formData" :rules="rules" label-placement="top">
            <n-form-item label="选择商品" path="itemId">
              <n-select
                v-model:value="formData.itemId"
                :options="productOptions"
                placeholder="搜索并选择商品..."
                filterable
                clearable
              />
            </n-form-item>
            <n-form-item label="实际盘库数量" path="actualQuantity">
              <n-input-number
                v-model:value="formData.actualQuantity"
                :min="0"
                placeholder="输入仓库实际数量"
                style="width: 100%;"
              />
            </n-form-item>
          </n-form>

          <div class="exec-preview" v-if="formData.itemId !== null">
            <div class="preview-row">
              <span>系统账面</span>
              <strong>{{ systemStock }}</strong>
            </div>
            <div class="preview-row">
              <span>实际盘点</span>
              <strong>{{ formData.actualQuantity }}</strong>
            </div>
            <div class="preview-row">
              <span>盈亏</span>
              <strong :class="diffColor">{{ diffText }}</strong>
            </div>
          </div>

          <n-button
            type="primary"
            block
            :loading="submitLoading"
            @click="handleSubmit"
            style="margin-top: 16px;"
          >
            确认平账
          </n-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, h, onMounted, computed } from 'vue'
import { NButton, NSpace, useMessage, NTag, NPopconfirm, NIcon, NTimeline, NTimelineItem, NButtonGroup } from 'naive-ui'
import { GridOutline, TimeOutline } from '@vicons/ionicons5'
import { getStocktakeList, executeStocktake, deleteStocktakeRecord } from '@/api/stocktake'
import { getProductList } from '@/api/product'

const message = useMessage()
const loading = ref(false)
const tableData = ref([])
const productsInfo = ref([])
const viewMode = ref('table')
const submitLoading = ref(false)
const formRef = ref(null)
const formData = ref({ itemId: null, actualQuantity: 0 })

const rules = {
  itemId: { type: 'number', required: true, message: '请选择盘点商品', trigger: ['blur', 'change'] },
  actualQuantity: { type: 'number', required: true, message: '请输入实际查库数量', trigger: 'blur' }
}

const productOptions = computed(() =>
  productsInfo.value.map(p => ({
    label: `${p.itemName} (库存: ${p.currentQuantity})`,
    value: p.id
  }))
)

const systemStock = computed(() => {
  if (formData.value.itemId === null) return 0
  const p = productsInfo.value.find(x => x.id === formData.value.itemId)
  return p ? p.currentQuantity : 0
})

const diffValue = computed(() => formData.value.actualQuantity - systemStock.value)

const diffText = computed(() => {
  const d = diffValue.value
  if (d > 0) return `盘盈 +${d}`
  if (d < 0) return `盘亏 ${d}`
  return '账实相符'
})

const diffColor = computed(() => {
  const d = diffValue.value
  if (d > 0) return 'color: #18a058;'
  if (d < 0) return 'color: #d03050;'
  return 'color: #888;'
})

function diffTagType(diff) {
  if (diff === null || diff === undefined) return 'default'
  if (diff > 0) return 'success'
  if (diff < 0) return 'error'
  return 'default'
}

function diffLabel(diff) {
  if (diff === null || diff === undefined) return '未知'
  if (diff > 0) return `盘盈 +${diff}`
  if (diff < 0) return `盘亏 ${diff}`
  return '相符'
}

function timelineType(diff) {
  if (diff === null || diff === undefined) return 'default'
  if (diff > 0) return 'success'
  if (diff < 0) return 'error'
  return 'default'
}

function formatTime(t) {
  if (!t) return '-'
  return t.replace('T', ' ').substring(0, 19)
}

const columns = [
  { title: '记录号', key: 'id', width: 70 },
  { title: '商品名称', key: 'itemName', minWidth: 140, ellipsis: { tooltip: true } },
  { title: '盘点时间', key: 'stocktakeTime', width: 160, render(row) { return formatTime(row.stocktakeTime) } },
  { title: '系统库存', key: 'systemQuantity', width: 80 },
  { title: '实盘库存', key: 'actualQuantity', width: 80 },
  {
    title: '盈亏', key: 'diffQuantity', width: 100,
    render(row) {
      const diff = row.diffQuantity
      if (diff > 0) return h(NTag, { type: 'success', size: 'small' }, { default: () => `盘盈 +${diff}` })
      if (diff < 0) return h(NTag, { type: 'error', size: 'small' }, { default: () => `盘亏 ${diff}` })
      return h(NTag, { type: 'default', size: 'small' }, { default: () => '账实相符' })
    }
  },
  {
    title: '', key: 'actions', width: 60,
    render(row) {
      return h(NPopconfirm, { onPositiveClick: () => handleDelete(row.id) }, {
        trigger: () => h(NButton, { size: 'tiny', type: 'error', tertiary: true }, { default: () => '删除' }),
        default: () => '确认删除这条盘点历史吗？'
      })
    }
  }
]

const fetchData = async () => {
  loading.value = true
  try {
    const [productsRes, stocktakeRes] = await Promise.all([getProductList(), getStocktakeList()])
    productsInfo.value = productsRes || []
    tableData.value = stocktakeRes || []
  } catch (err) {
    message.error('获取盘点数据失败')
  } finally {
    loading.value = false
  }
}

const handleDelete = async (id) => {
  try {
    await deleteStocktakeRecord(id)
    message.success('记录删除成功')
    fetchData()
  } catch (err) {
    message.error('删除记录失败')
  }
}

const handleSubmit = (e) => {
  e.preventDefault()
  formRef.value?.validate(async (errors) => {
    if (!errors) {
      submitLoading.value = true
      try {
        await executeStocktake(formData.value.itemId, formData.value.actualQuantity)
        message.success('盘点执行成功，系统已自动平账')
        fetchData()
        formData.value = { itemId: null, actualQuantity: 0 }
      } catch (err) {
        message.error('盘点执行失败')
      } finally {
        submitLoading.value = false
      }
    }
  })
}

onMounted(() => fetchData())
</script>

<style scoped>
.stocktake-page {
  padding-top: 24px;
  height: calc(100vh - 24px);
  display: flex;
  flex-direction: column;
}
.split-panel {
  display: flex;
  gap: 16px;
  flex: 1;
  min-height: 0;
}
.panel-left {
  flex: 3;
  background: #fff;
  border-radius: 10px;
  padding: 20px;
  overflow: auto;
}
.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0;
}
.timeline-view {
  padding: 8px 0;
}
.panel-right {
  flex: 1;
  min-width: 300px;
}
.execute-panel {
  background: #fff;
  border-radius: 10px;
  padding: 20px;
  position: sticky;
  top: 0;
}
.exec-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0 0 16px;
}
.exec-preview {
  background: #f9fafb;
  border-radius: 8px;
  padding: 14px;
  margin-top: 12px;
}
.preview-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
  padding: 4px 0;
  color: #555;
}
.preview-row strong {
  font-size: 15px;
}
</style>
```

- [ ] **Step 2: Verify stocktake page**

Run: `cd frontend && npm run dev`. Navigate to `/stocktake`:
- Split panel: left side shows history with table/timeline toggle buttons
- Timeline view shows records in chronological order with type indicators
- Right side has inline execution form (product selector + quantity + preview + confirm)
- Executing a stocktake refreshes the history
- Deleting a record works as before

- [ ] **Step 3: Commit**

```bash
git add frontend/src/views/stocktake/index.vue
git commit -m "feat: stocktake timeline view + inline execution panel"
```

---

### Task 9: Final verification and cleanup

**Files:** None (verification only)

- [ ] **Step 1: Full walkthrough**

Run: `cd frontend && npm run dev`

Start the backend: `./mvnw spring-boot:run > backend.log 2>&1 &`

Verify every page and interaction:
1. **Sidebar** — collapse/expand persists across refresh, version "v1.0" visible
2. **Dashboard** — metric strip animates numbers, charts render, low-stock list shows (if data exists), quick-actions hover lifts
3. **Product** — search filters by name/SKU, create/edit with SKU/unit fields, stock color tags work, delete with confirmation, empty state shows when no products
4. **Inbound** — split-panel works, select product → right panel updates, +10/+50/+100 quick-add, stock preview ("50 → 65"), execute succeeds, history updates locally
5. **Outbound** — symmetric layout, 0-stock products grayed out, exceeded stock shows red warning + button disabled, execute succeeds
6. **Stocktake** — table/timeline toggle works, timeline shows records in order, inline execution previews diff, execute succeeds and refreshes
7. **Error handling** — trigger a network error (stop backend), confirm Naive UI toast appears, not browser `alert()`
8. **No console errors** — open DevTools console, confirm zero errors/warnings during normal navigation and operations

- [ ] **Step 2: Check for unused imports or dead code**

Run: `cd frontend && npm run build` — confirm build succeeds with no errors.

- [ ] **Step 3: Commit any final tweaks**

If any issues found during verification, fix and commit with a descriptive message.
