<template>
  <div class="dashboard">
    <div class="metric-strip">
      <div class="metric-item" v-for="m in metrics" :key="m.label">
        <div class="metric-label">{{ m.label }}</div>
        <div class="metric-value">
          <n-number-animation :from="0" :to="m.value" />
        </div>
      </div>
    </div>

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

    metrics.value[0].value = products.length
    metrics.value[1].value = products.reduce((sum, p) => sum + (p.currentQuantity || 0), 0)

    const sorted = [...products].sort((a, b) => (b.currentQuantity || 0) - (a.currentQuantity || 0))
    const top5 = sorted.slice(0, 5)
    let othersStock = 0
    if (sorted.length > 5) {
      othersStock = sorted.slice(5).reduce((sum, p) => sum + (p.currentQuantity || 0), 0)
    }
    const pieData = top5.map(p => ({ value: p.currentQuantity || 0, name: p.itemName }))
    if (othersStock > 0) pieData.push({ value: othersStock, name: '其他' })
    pieOption.value.series[0].data = pieData

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
.chart-trend { flex: 2; }
.chart-pie { flex: 1; display: flex; flex-direction: column; }
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
.chart { width: 100%; flex: 1; min-height: 280px; }
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
  padding: 6px 8px;
  cursor: pointer;
  border-radius: 4px;
  transition: background 0.15s;
}
.low-stock-item:hover { background: #fef0f0; }
.low-stock-name { font-size: 13px; color: #333; }
.low-stock-count { font-size: 13px; font-weight: 600; color: #d03050; }
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
