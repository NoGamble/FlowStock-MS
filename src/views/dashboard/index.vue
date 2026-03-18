<template>
  <div class="page-container">
    <!-- 顶部数据统计卡片 -->
    <n-grid :x-gap="16" :y-gap="16" cols="1 s:2 m:4" responsive="screen">
      <n-grid-item v-for="(stat, index) in statsCards" :key="index">
        <n-card class="stat-card" hoverable>
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-title">{{ stat.title }}</div>
              <div class="stat-value">
                <n-number-animation :from="0" :to="stat.value" />
              </div>
              <div class="stat-trend" :class="stat.trend > 0 ? 'up' : 'down'">
                <n-icon :component="stat.trend > 0 ? ArrowUpOutline : ArrowDownOutline" />
                <span>{{ Math.abs(stat.trend) }}% 较上周</span>
              </div>
            </div>
            <div class="stat-icon" :style="{ backgroundColor: stat.bgColor, color: stat.color }">
              <n-icon size="28" :component="stat.icon" />
            </div>
          </div>
        </n-card>
      </n-grid-item>
    </n-grid>

    <!-- 中间图表区 -->
    <n-grid :x-gap="16" :y-gap="16" cols="1 m:3" responsive="screen" style="margin-top: 16px;">
      <!-- 左侧：近7天出入库趋势 (占 2 份宽度) -->
      <n-grid-item span="2">
        <n-card title="出入库动态趋势" class="chart-card" hoverable>
          <v-chart class="chart" :option="trendOption" autoresize />
        </n-card>
      </n-grid-item>

      <!-- 右侧：库存占比分布 (占 1 份宽度) -->
      <n-grid-item span="1">
        <n-card title="库存分类占比" class="chart-card" hoverable>
          <v-chart class="chart" :option="pieOption" autoresize />
        </n-card>
      </n-grid-item>
    </n-grid>

    <!-- 底部功能与预警 -->
    <n-grid :x-gap="16" :y-gap="16" cols="1 m:2" responsive="screen" style="margin-top: 16px;">
      <!-- 快捷操作 -->
      <n-grid-item>
        <n-card title="从这里开始" class="action-card" hoverable>
          <n-grid :x-gap="12" :y-gap="12" cols="2">
            <n-grid-item v-for="action in fastActions" :key="action.name">
              <n-button 
                block 
                secondary 
                type="primary" 
                size="large"
                @click="router.push(action.path)"
                class="action-btn"
              >
                <template #icon>
                  <n-icon :component="action.icon" />
                </template>
                {{ action.name }}
              </n-button>
            </n-grid-item>
          </n-grid>
        </n-card>
      </n-grid-item>

      <!-- 库存预警 -->
      <n-grid-item>
        <n-card title="低库存预警" class="alert-card" hoverable header-style="color: #d03050;">
          <template #header-extra>
            <n-tag type="error" round size="small">需处理</n-tag>
          </template>
          <n-list hoverable clickable>
            <n-list-item v-for="item in warningItems" :key="item.id">
              <n-thing :title="item.name" :description="`编号：${item.code}`">
                <template #avatar>
                  <n-avatar color="#fff0f6" style="color: #d03050">警</n-avatar>
                </template>
              </n-thing>
              <template #suffix>
                <div style="text-align: right;">
                  <div style="color: #d03050; font-weight: bold;">仅剩 {{ item.stock }} 件</div>
                  <n-button text type="primary" size="small" style="margin-top: 4px;">立即补货</n-button>
                </div>
              </template>
            </n-list-item>
          </n-list>
        </n-card>
      </n-grid-item>
    </n-grid>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useThemeVars, useMessage } from 'naive-ui'
import { getProductList } from '@/api/product'
import { 
  CubeOutline, LogInOutline, LogOutOutline, ClipboardOutline,
  ArrowUpOutline, ArrowDownOutline, AlertCircleOutline, AddCircleOutline
} from '@vicons/ionicons5'

// Echarts 按需引入
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, PieChart } from 'echarts/charts'
import { 
  TitleComponent, TooltipComponent, LegendComponent, 
  GridComponent, DatasetComponent 
} from 'echarts/components'
import VChart from 'vue-echarts'

// 注册 ECharts 核心组件
use([
  CanvasRenderer, LineChart, PieChart, GridComponent,
  TitleComponent, TooltipComponent, LegendComponent, DatasetComponent
])

const router = useRouter()
const themeVars = useThemeVars() // 获取 naive ui 当前主题色
const message = useMessage()

// 1. 顶部统计数据
const statsCards = ref([
  { title: '商品总种类 (种)', value: 0, trend: 0, icon: CubeOutline, color: '#2080f0', bgColor: '#e6f3fc' },
  { title: '总计库存 (件)', value: 0, trend: 0, icon: ClipboardOutline, color: '#18a058', bgColor: '#e7f5ee' },
  { title: '近期入库参考', value: 0, trend: 0, icon: LogInOutline, color: '#f0a020', bgColor: '#fdf5e8' },
  { title: '近期出库参考', value: 0, trend: 0, icon: LogOutOutline, color: '#d03050', bgColor: '#fbebef' },
])

// 获取真实数据并更新仪表盘
const fetchDashboardData = async () => {
  try {
    // 目前后端只有获取全量商品列表的接口可用，我们就用它来渲染“商品种类”、“总库存”和“库存饼图”“低库存预警”
    const products = await getProductList() || []
    
    // 1. 计算总卡片
    const totalTypes = products.length
    const totalStock = products.reduce((sum, item) => sum + (item.currentQuantity || 0), 0)
    
    statsCards.value[0].value = totalTypes // 商品总种类
    statsCards.value[1].value = totalStock // 总件数

    // 2. 更新库存饼图（取库存量排名前 5 的，其他的归为“其他”）
    const sortedProducts = [...products].sort((a, b) => (b.currentQuantity || 0) - (a.currentQuantity || 0))
    const top5 = sortedProducts.slice(0, 5)
    let othersStock = 0
    if (sortedProducts.length > 5) {
      othersStock = sortedProducts.slice(5).reduce((sum, item) => sum + (item.currentQuantity || 0), 0)
    }
    
    const pieData = top5.map(p => ({ value: p.currentQuantity || 0, name: p.itemName }))
    if (othersStock > 0) {
      pieData.push({ value: othersStock, name: '其他商品汇总' })
    }
    
    // 更新饼图响应式数据
    pieOption.value.series[0].data = pieData

    // 3. 更新低库存预警 (找出库存 <= 10 的商品)
    const lowStockThreshold = 10
    const warnings = sortedProducts
      .filter(p => (p.currentQuantity || 0) <= lowStockThreshold)
      .map(p => ({
        id: p.id,
        name: p.itemName,
        code: `PRD-${String(p.id).padStart(5, '0')}`,
        stock: p.currentQuantity || 0
      }))
    
    warningItems.value = warnings

  } catch (error) {
    message.error('无法连接后端获取仪表盘数据')
    console.error(error)
  }
}

// 页面挂载时拉取真实数据
onMounted(() => {
  fetchDashboardData()
})

// 2. 快捷操作配置
const fastActions = [
  { name: '录入新商品', path: '/product', icon: AddCircleOutline },
  { name: '处理入库', path: '/inbound', icon: LogInOutline },
  { name: '处理出库', path: '/outbound', icon: LogOutOutline },
  { name: '发起盘点', path: '/stocktake', icon: ClipboardOutline },
]

// 3. 预警列表模拟数据
const warningItems = ref([
  { id: 1, name: '雷蛇机械键盘 V3', code: 'PRD-10023', stock: 3 },
  { id: 2, name: '罗技炼狱鼠标', code: 'PRD-10045', stock: 1 },
  { id: 3, name: '27寸护眼显示器', code: 'PRD-10088', stock: 4 },
])

// 4. 图表配置 (折线图)
const trendOption = ref({
  tooltip: { trigger: 'axis' },
  legend: { data: ['入库数量', '出库数量'], bottom: 0 },
  grid: { left: '3%', right: '4%', bottom: '10%', containLabel: true },
  xAxis: { type: 'category', boundaryGap: false, data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'] },
  yAxis: { type: 'value' },
  series: [
    {
      name: '入库数量',
      type: 'line',
      smooth: true,
      data: [120, 132, 101, 134, 90, 230, 210],
      itemStyle: { color: '#18a058' },
      areaStyle: {
        color: {
          type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [{ offset: 0, color: 'rgba(24,160,88,0.3)' }, { offset: 1, color: 'rgba(24,160,88,0.05)' }]
        }
      }
    },
    {
      name: '出库数量',
      type: 'line',
      smooth: true,
      data: [220, 182, 191, 234, 290, 330, 310],
      itemStyle: { color: '#2080f0' },
      areaStyle: {
        color: {
          type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [{ offset: 0, color: 'rgba(32,128,240,0.3)' }, { offset: 1, color: 'rgba(32,128,240,0.05)' }]
        }
      }
    }
  ]
})

// 5. 图表配置 (饼图)
const pieOption = ref({
  tooltip: { trigger: 'item' },
  legend: { top: 'bottom' },
  series: [
    {
      name: '库存分布',
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
      label: { show: false, position: 'center' },
      emphasis: {
        label: { show: true, fontSize: 16, fontWeight: 'bold' }
      },
      labelLine: { show: false },
      data: [
        { value: 1048, name: '电子外设' },
        { value: 735, name: '办公耗材' },
        { value: 580, name: '文具用品' },
        { value: 484, name: '电脑整机' },
        { value: 300, name: '其他' }
      ]
    }
  ]
})
</script>

<style scoped>
.page-container {
  padding-bottom: 24px;
}

/* 统一的卡片阴影和圆角设置，提升质感 */
.n-card {
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.03);
}

/* 顶部数据卡片样式 */
.stat-card {
  height: 120px;
}
.stat-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.stat-info {
  display: flex;
  flex-direction: column;
}
.stat-title {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}
.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #333;
  line-height: 1.2;
}
.stat-trend {
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 4px;
}
.stat-trend.up { color: #18a058; }
.stat-trend.down { color: #d03050; }

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 图表区域样式 */
.chart-card {
  height: 400px;
}
.chart {
  height: 320px;
  width: 100%;
}

/* 底部区域 */
.action-card {
  height: 320px;
}
.action-btn {
  height: 80px;
  font-size: 16px;
  border-radius: 8px;
}
.alert-card {
  height: 320px;
  overflow-y: auto;
}
</style>
