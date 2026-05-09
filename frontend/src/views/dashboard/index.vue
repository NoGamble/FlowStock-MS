<template>
  <div class="dashboard">

    <!-- 主内容区：左栏（热力图+柱形+指标卡）+ 右栏（饼图+预警） -->
    <div class="charts-row">

      <!-- 左栏 -->
      <div class="left-col">

        <!-- 热力图卡 -->
        <div class="chart-box chart-trend">
          <div class="chart-header">
            <h3 class="chart-title">出入库操作记录</h3>
            <div class="heatmap-controls">
              <span class="heatmap-summary">选定 <strong>{{ visibleOps }}</strong> 次 · 全年 <strong>{{ totalOps }}</strong> 次</span>
              <div class="range-tabs">
                <button
                  v-for="r in ranges" :key="r.value"
                  class="range-tab" :class="{ active: activeRange === r.value }"
                  @click="setRange(r.value)"
                >{{ r.label }}</button>
              </div>
              <div class="heatmap-legend">
                <span class="legend-label">少</span>
                <span v-for="i in 5" :key="i" class="legend-cell" :style="{ background: heatColors[i-1] }" />
                <span class="legend-label">多</span>
              </div>
            </div>
          </div>

          <div class="heatmap-outer" ref="heatmapOuter">
            <div class="heatmap-scroll">
              <div class="heatmap-months-row">
                <div class="weekday-spacer" />
                <div class="months-track">
                  <span v-for="m in monthLabels" :key="m.key" :style="{ left: m.offset + 'px' }">{{ m.label }}</span>
                </div>
              </div>
              <div class="heatmap-body">
                <div class="weekday-labels">
                  <span>一</span><span>三</span><span>五</span><span>日</span>
                </div>
                <div class="heatmap-grid">
                  <div v-for="(week, wi) in visibleWeeks" :key="wi" class="heatmap-col">
                    <div
                      v-for="(day, di) in week" :key="di"
                      class="heatmap-cell"
                      :class="{ future: day.date > today }"
                      :style="{ background: day.date > today ? 'transparent' : cellColor(day.count) }"
                      @mouseenter="showTooltip($event, day)"
                      @mouseleave="hideTooltip"
                    />
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 近14天柱形卡 -->
        <div class="chart-box recent-bar-card">
          <div class="chart-header">
            <h3 class="chart-title">近14天每日操作量</h3>
            <span class="heatmap-summary">合计 <strong>{{ recentDays.reduce((s,d)=>s+d.count,0) }}</strong> 次</span>
          </div>
          <div class="recent-bars">
            <div
              v-for="d in recentDays" :key="d.date"
              class="recent-bar-col"
              @mouseenter="showTooltip($event, d)"
              @mouseleave="hideTooltip"
            >
              <div class="recent-bar-track">
                <div
                  class="recent-bar-fill"
                  :style="{
                    height: d.count ? Math.max(6, d.count / maxRecent * 100) + '%' : '3px',
                    background: d.count ? '#0052D9' : '#eef2ff'
                  }"
                />
              </div>
              <div class="recent-bar-label">{{ d.date.slice(5) }}</div>
            </div>
          </div>
        </div>

        <!-- 指标卡 -->
        <div class="metric-strip">
          <div class="metric-card" v-for="m in metrics" :key="m.label">
            <div class="metric-icon-wrap" :style="{ background: m.iconBg }">
              <n-icon size="18" :component="m.icon" :style="{ color: m.iconColor }" />
            </div>
            <div class="metric-body">
              <div class="metric-label">{{ m.label }}</div>
              <div class="metric-value">
                <n-number-animation :from="0" :to="m.value" />
              </div>
            </div>
            <div class="metric-watermark">{{ m.sub }}</div>
            <div class="metric-bar" :style="{ background: m.iconColor }" />
          </div>
        </div>

      </div>

      <!-- 右栏 -->
      <div class="right-panel">
        <!-- 饼图卡 -->
        <div class="chart-box chart-pie">
          <div class="chart-header">
            <h3 class="chart-title">库存占比</h3>
          </div>
          <v-chart class="pie-chart" :option="pieOption" autoresize />
          <div class="pie-legend">
            <div class="pie-legend-item" v-for="(item, i) in pieItems" :key="item.name">
              <span class="pie-dot" :style="{ background: pieColors[i % pieColors.length] }" />
              <span class="pie-name">{{ item.name }}</span>
              <span class="pie-val">{{ item.value }}</span>
            </div>
          </div>
        </div>

        <!-- 低库存预警卡 -->
        <div class="chart-box warn-box" v-if="warningItems.length">
          <div class="chart-header">
            <h3 class="chart-title warn-title">低库存预警</h3>
            <span class="warn-badge">{{ warningItems.length }}</span>
          </div>
          <div class="warn-item" v-for="item in warningItems" :key="item.id" @click="router.push('/inbound')">
            <span class="warn-name">{{ item.name }}</span>
            <span class="warn-count">{{ item.stock }}</span>
          </div>
        </div>
      </div>

    </div>

    <!-- 快捷操作 -->
    <div class="quick-actions">
      <div
        class="quick-action-btn"
        v-for="action in fastActions"
        :key="action.name"
        @click="router.push(action.path)"
      >
        <n-icon size="20" :component="action.icon" />
        <span>{{ action.name }}</span>
      </div>
    </div>

    <!-- 自定义 tooltip -->
    <div
      class="heat-tooltip"
      v-show="tooltip.visible"
      :style="{ left: tooltip.x + 'px', top: tooltip.y + 'px' }"
    >
      <div class="tt-date">{{ tooltip.date }}</div>
      <div class="tt-count">{{ tooltip.count }} 次操作</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, markRaw, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { getProductList } from '@/api/product'
import { getInboundRecords, getOutboundRecords } from '@/api/movement'
import {
  CubeOutline, LogInOutline, LogOutOutline, ClipboardOutline,
  AddCircleOutline, WarningOutline, BarChartOutline, TrendingUpOutline
} from '@vicons/ionicons5'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { PieChart } from 'echarts/charts'
import { TitleComponent, TooltipComponent, LegendComponent, GridComponent, DatasetComponent } from 'echarts/components'
import VChart from 'vue-echarts'

use([CanvasRenderer, PieChart, GridComponent, TitleComponent, TooltipComponent, LegendComponent, DatasetComponent])

const router = useRouter()
const message = useMessage()

// ── 指标卡 ───────────────────────────────────────────────
const metrics = ref([
  { label: '商品种类', sub: 'Product Types',  value: 0, icon: markRaw(CubeOutline),       iconBg: '#e8f0fe', iconColor: '#0052D9' },
  { label: '总库存',   sub: 'Total Stock',    value: 0, icon: markRaw(BarChartOutline),   iconBg: '#e8faf0', iconColor: '#18a058' },
  { label: '今日入库', sub: 'Inbound Today',  value: 0, icon: markRaw(TrendingUpOutline), iconBg: '#fff7e6', iconColor: '#f0a020' },
  { label: '今日出库', sub: 'Outbound Today', value: 0, icon: markRaw(LogOutOutline),     iconBg: '#fef0f0', iconColor: '#d03050' },
])

// ── 热力图 ───────────────────────────────────────────────
const heatColors = ['#eef2ff', '#c7d7fa', '#91b4f5', '#4d84ef', '#0052D9']
const today = new Date().toISOString().slice(0, 10)

const ranges = [
  { label: '3个月', value: 13 },
  { label: '6个月', value: 26 },
  { label: '12个月', value: 52 },
]
const activeRange = ref(52)

function dateStr(d) { return d.toISOString().slice(0, 10) }

function buildGrid() {
  const end = new Date()
  const daysToSat = (6 - end.getDay() + 7) % 7
  const gridEnd = new Date(end)
  gridEnd.setDate(gridEnd.getDate() + daysToSat)
  const weeks = []
  for (let w = 51; w >= 0; w--) {
    const week = []
    for (let d = 0; d <= 6; d++) {
      const date = new Date(gridEnd)
      date.setDate(gridEnd.getDate() - w * 7 - (6 - d))
      week.push({ date: dateStr(date), count: 0 })
    }
    weeks.push(week)
  }
  return weeks
}

const heatmapWeeks = ref(buildGrid())
const totalOps = ref(0)

const visibleWeeks = computed(() => heatmapWeeks.value.slice(52 - activeRange.value))

const visibleOps = computed(() =>
  visibleWeeks.value.flat().filter(d => d.date <= today).reduce((s, d) => s + d.count, 0)
)

// 近14天每日数据（用于底部迷你柱）
const recentDays = computed(() => {
  const days = []
  for (let i = 13; i >= 0; i--) {
    const d = new Date(); d.setDate(d.getDate() - i)
    const ds = dateStr(d)
    const found = heatmapWeeks.value.flat().find(x => x.date === ds)
    days.push({ date: ds, count: found ? found.count : 0 })
  }
  return days
})
const maxRecent = computed(() => Math.max(1, ...recentDays.value.map(d => d.count)))

// 月份标签：offset 按列宽动态计算，避免切换时段后标签偏移错位
const CELL_W = 14 // cell(~11px) + gap(3px)
const monthLabels = computed(() => {
  const labels = []
  let lastMonth = -1
  visibleWeeks.value.forEach((week, wi) => {
    const m = new Date(week[0].date).getMonth()
    if (m !== lastMonth) {
      labels.push({ label: `${m + 1}月`, offset: wi * CELL_W, key: week[0].date })
      lastMonth = m
    }
  })
  return labels
})

function cellColor(count) {
  if (count === 0) return heatColors[0]
  if (count <= 2)  return heatColors[1]
  if (count <= 5)  return heatColors[2]
  if (count <= 10) return heatColors[3]
  return heatColors[4]
}

function setRange(v) { activeRange.value = v }

async function buildHeatmap() {
  try {
    const [inbound, outbound] = await Promise.all([getInboundRecords(), getOutboundRecords()])
    const countMap = {}
    const add = (records, field) => (records || []).forEach(r => {
      const d = r[field]?.slice(0, 10)
      if (d) countMap[d] = (countMap[d] || 0) + 1
    })
    add(inbound, 'inboundTime')
    add(outbound, 'outboundTime')
    heatmapWeeks.value.forEach(week => week.forEach(day => { day.count = countMap[day.date] || 0 }))
    totalOps.value = Object.values(countMap).reduce((s, v) => s + v, 0)

    // 今日入库/出库次数
    metrics.value[2].value = (inbound || []).filter(r => r.inboundTime?.slice(0, 10) === today).length
    metrics.value[3].value = (outbound || []).filter(r => r.outboundTime?.slice(0, 10) === today).length
  } catch { /* 静默 */ }
}

// ── Tooltip ──────────────────────────────────────────────
const tooltip = ref({ visible: false, x: 0, y: 0, date: '', count: 0 })

function showTooltip(e, day) {
  if (day.date > today) return
  const rect = e.target.getBoundingClientRect()
  tooltip.value = {
    visible: true,
    x: rect.left + window.scrollX + rect.width / 2,
    y: rect.top + window.scrollY - 48,
    date: day.date,
    count: day.count,
  }
}
function hideTooltip() { tooltip.value.visible = false }

// ── 饼图 ─────────────────────────────────────────────────
const pieColors = ['#0052D9', '#18a058', '#f0a020', '#d03050', '#8a5cf5', '#06b6d4']
const pieItems = ref([])
const warningItems = ref([])

const pieOption = ref({
  color: pieColors,
  tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
  legend: { show: false },
  series: [{
    name: '库存分布', type: 'pie',
    radius: ['38%', '68%'],
    center: ['50%', '48%'],
    avoidLabelOverlap: true,
    itemStyle: { borderRadius: 5, borderColor: '#fff', borderWidth: 2 },
    label: { show: false },
    emphasis: { label: { show: true, fontSize: 13, fontWeight: 'bold' } },
    labelLine: { show: false },
    data: []
  }]
})

const fastActions = [
  { name: '录入商品', path: '/product',   icon: markRaw(AddCircleOutline) },
  { name: '处理入库', path: '/inbound',   icon: markRaw(LogInOutline) },
  { name: '处理出库', path: '/outbound',  icon: markRaw(LogOutOutline) },
  { name: '发起盘点', path: '/stocktake', icon: markRaw(ClipboardOutline) },
]

const fetchDashboardData = async () => {
  try {
    const products = await getProductList() || []
    metrics.value[0].value = products.length
    metrics.value[1].value = products.reduce((s, p) => s + (p.currentQuantity || 0), 0)

    const sorted = [...products].sort((a, b) => (b.currentQuantity || 0) - (a.currentQuantity || 0))
    const top5 = sorted.slice(0, 5)
    const othersStock = sorted.slice(5).reduce((s, p) => s + (p.currentQuantity || 0), 0)
    const pieData = top5.map(p => ({ value: p.currentQuantity || 0, name: p.itemName }))
    if (othersStock > 0) pieData.push({ value: othersStock, name: '其他' })
    pieOption.value.series[0].data = pieData
    pieItems.value = pieData

    warningItems.value = sorted
      .filter(p => (p.currentQuantity || 0) <= 10)
      .map(p => ({ id: p.id, name: p.itemName, stock: p.currentQuantity || 0 }))
  } catch {
    message.error('获取仪表盘数据失败')
  }
}

onMounted(() => { fetchDashboardData(); buildHeatmap() })
</script>

<style scoped>
.dashboard { }

/* ── 指标卡 ── */
.metric-strip {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}
.metric-card {
  background: #fff;
  border-radius: 10px;
  padding: 16px 18px;
  display: flex;
  align-items: center;
  gap: 14px;
  position: relative;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
}
.metric-icon-wrap {
  width: 40px; height: 40px;
  border-radius: 10px;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.metric-body { flex: 1; min-width: 0; }
.metric-label { font-size: 12px; color: #888; margin-bottom: 2px; }
.metric-value { font-size: 26px; font-weight: 700; color: #1a1a2e; line-height: 1.1; }
.metric-watermark {
  position: absolute;
  right: 12px;
  bottom: 6px;
  font-size: 22px;
  font-weight: 800;
  color: rgba(0, 0, 0, 0.1);
  letter-spacing: 0.5px;
  white-space: nowrap;
  pointer-events: none;
  user-select: none;
  text-transform: uppercase;
}
.metric-bar {
  position: absolute;
  left: 0; top: 0; bottom: 0;
  width: 3px;
  border-radius: 10px 0 0 10px;
}

/* ── 主布局 ── */
.charts-row {
  display: flex;
  gap: 14px;
  margin-bottom: 14px;
  align-items: flex-start;
}
.chart-box {
  background: #fff;
  border-radius: 10px;
  padding: 18px 20px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
}
.left-col { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 14px; }
.chart-trend { display: flex; flex-direction: column; }
.right-panel { width: 260px; flex-shrink: 0; display: flex; flex-direction: column; gap: 14px; }
.heatmap-outer { overflow-x: auto; }
.heatmap-scroll { display: inline-block; min-width: 100%; }
.heatmap-body { display: flex; gap: 4px; }

.chart-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
  flex-wrap: wrap;
  gap: 8px;
}
.chart-title { font-size: 14px; font-weight: 600; color: #1a1a2e; margin: 0; }

/* ── 热力图控件 ── */
.heatmap-controls { display: flex; align-items: center; gap: 12px; }
.range-tabs { display: flex; background: #f5f6f8; border-radius: 6px; padding: 2px; gap: 2px; }
.range-tab {
  border: none; background: transparent; cursor: pointer;
  font-size: 12px; color: #888; padding: 3px 10px; border-radius: 4px;
  transition: all 0.15s;
}
.range-tab.active { background: #fff; color: #0052D9; font-weight: 600; box-shadow: 0 1px 3px rgba(0,0,0,0.1); }
.heatmap-legend { display: flex; align-items: center; gap: 3px; }
.legend-label { font-size: 11px; color: #bbb; }
.legend-cell { width: 10px; height: 10px; border-radius: 2px; }

/* ── 热力图主体 ── */
.heatmap-outer { overflow-x: auto; }
.heatmap-scroll { display: inline-block; min-width: 100%; }

.heatmap-months-row {
  display: flex;
  margin-bottom: 4px;
}
.weekday-spacer { width: 20px; flex-shrink: 0; }
.months-track { position: relative; flex: 1; height: 16px; }
.months-track span { position: absolute; font-size: 11px; color: #aaa; white-space: nowrap; }

.heatmap-body { display: flex; gap: 4px; }
.weekday-labels {
  display: flex; flex-direction: column;
  width: 16px; flex-shrink: 0;
  padding-top: 2px;
}
.weekday-labels span {
  font-size: 10px; color: #bbb;
  flex: 1; display: flex; align-items: center;
}

.heatmap-grid { display: flex; gap: 3px; flex: 1; }
.heatmap-col { display: flex; flex-direction: column; gap: 3px; flex: 1; }
.heatmap-cell {
  flex: 1;
  min-height: 14px;
  max-height: 22px;
  border-radius: 3px;
  cursor: pointer;
  transition: transform 0.1s, box-shadow 0.1s;
}
.heatmap-cell:not(.future):hover {
  transform: scale(1.25);
  box-shadow: 0 2px 6px rgba(0,82,217,0.3);
  z-index: 1;
}
.heatmap-cell.future { background: transparent !important; cursor: default; }

.heatmap-summary { font-size: 12px; color: #aaa; white-space: nowrap; }
.heatmap-summary strong { color: #0052D9; font-weight: 600; }

/* ── 近14天柱形卡 ── */
.recent-bar-card { margin-bottom: 14px; }
.recent-bars {
  display: flex;
  gap: 8px;
  align-items: flex-end;
  height: 100px;
}
.recent-bar-col {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  height: 100%;
  cursor: pointer;
}
.recent-bar-track {
  flex: 1;
  width: 100%;
  display: flex;
  align-items: flex-end;
}
.recent-bar-fill {
  width: 100%;
  border-radius: 3px 3px 0 0;
  transition: opacity 0.15s, height 0.3s;
  min-height: 3px;
}
.recent-bar-col:hover .recent-bar-fill { opacity: 0.7; }
.recent-bar-label {
  font-size: 10px;
  color: #bbb;
  white-space: nowrap;
  flex-shrink: 0;
}

/* ── 自定义 Tooltip ── */
.heat-tooltip {
  position: fixed;
  z-index: 9999;
  background: #1a1a2e;
  color: #fff;
  border-radius: 6px;
  padding: 6px 10px;
  font-size: 12px;
  pointer-events: none;
  transform: translateX(-50%);
  white-space: nowrap;
  box-shadow: 0 4px 12px rgba(0,0,0,0.2);
}
.tt-date { color: #aaa; font-size: 11px; margin-bottom: 2px; }
.tt-count { font-weight: 600; }

/* ── 饼图 ── */
.chart-pie { display: flex; flex-direction: column; }
.pie-chart { width: 100%; height: 180px; }
.pie-legend { margin-top: 8px; display: flex; flex-direction: column; gap: 6px; }
.pie-legend-item {
  display: flex; align-items: center; gap: 8px;
  font-size: 12px;
}
.pie-dot { width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0; }
.pie-name { flex: 1; color: #555; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.pie-val { color: #1a1a2e; font-weight: 600; font-size: 13px; }

/* ── 预警卡 ── */
.warn-box { }
.warn-title { color: #d03050; }
.warn-badge {
  background: #d03050; color: #fff;
  font-size: 11px; font-weight: 700;
  border-radius: 10px; padding: 1px 7px;
}
.warn-item {
  display: flex; justify-content: space-between; align-items: center;
  padding: 6px 8px; border-radius: 6px; cursor: pointer;
  transition: background 0.15s;
}
.warn-item:hover { background: #fef0f0; }
.warn-name { font-size: 13px; color: #444; flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.warn-count {
  font-size: 13px; font-weight: 700; color: #d03050;
  background: #fef0f0; border-radius: 4px; padding: 1px 6px;
}

/* ── 快捷操作 ── */
.quick-actions { display: flex; gap: 12px; }
.quick-action-btn {
  flex: 1; display: flex; align-items: center; justify-content: center;
  gap: 8px; padding: 13px 0;
  background: #fff; border-radius: 10px;
  font-size: 14px; color: #555; cursor: pointer;
  transition: transform 0.15s, box-shadow 0.15s, color 0.15s;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
}
.quick-action-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.09);
  color: #0052D9;
}
</style>
