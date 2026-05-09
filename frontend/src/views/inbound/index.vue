<template>
  <div class="movement-page">
    <div class="split-panel">
      <!-- 左侧：商品列表 -->
      <div class="panel-left">
        <div class="panel-left-header">
          <n-input v-model:value="searchQuery" placeholder="搜索商品名称或 SKU..." clearable>
            <template #prefix><n-icon :component="SearchOutline" /></template>
          </n-input>
        </div>
        <div class="product-list">
          <div
            v-for="p in filteredProducts" :key="p.id"
            class="product-row"
            :class="{
              selected: selectedProduct?.id === p.id,
              depleted: p.currentQuantity <= 0
            }"
            @click="selectProduct(p)"
          >
            <div class="pr-main">
              <span class="pr-name">{{ p.itemName }}</span>
              <span class="pr-sku">{{ p.skuCode }}</span>
            </div>
            <n-tag
              :type="p.currentQuantity <= 0 ? 'error' : p.currentQuantity <= 10 ? 'warning' : 'success'"
              size="small" round
            >
              {{ p.currentQuantity <= 0 ? '已耗尽' : p.currentQuantity + ' ' + (p.unit || '件') }}
            </n-tag>
          </div>
          <n-empty v-if="!filteredProducts.length" description="暂无商品" style="padding: 40px 0;" />
        </div>
      </div>

      <!-- 右侧：操作面板 + 历史 -->
      <div class="panel-right">
        <!-- 操作区 -->
        <div class="op-card">
          <template v-if="selectedProduct">
            <div class="op-product-bar">
              <div class="op-product-info">
                <span class="op-product-name">{{ selectedProduct.itemName }}</span>
                <span class="op-product-sku">{{ selectedProduct.skuCode }}</span>
              </div>
              <n-tag
                :type="selectedProduct.currentQuantity <= 10 ? 'warning' : 'success'"
                :bordered="false" size="small"
              >
                库存 {{ selectedProduct.currentQuantity }} {{ selectedProduct.unit || '件' }}
              </n-tag>
            </div>

            <div class="op-tabs">
              <button class="op-tab" :class="{ active: mode === 'in' }" @click="mode = 'in'">
                <n-icon :component="LogInOutline" /> 入库
              </button>
              <button class="op-tab op-tab-out" :class="{ active: mode === 'out' }" @click="mode = 'out'">
                <n-icon :component="LogOutOutline" /> 出库
              </button>
            </div>

            <div class="op-input-row">
              <n-input-number
                v-model:value="amount"
                :min="1"
                :max="mode === 'out' ? selectedProduct.currentQuantity : undefined"
                placeholder="数量"
                style="width: 100%;"
                size="large"
                :status="mode === 'out' && amount > selectedProduct.currentQuantity ? 'error' : undefined"
              />
            </div>
            <div class="quick-btns" v-if="mode === 'in'">
              <n-button size="small" secondary @click="amount += 10">+10</n-button>
              <n-button size="small" secondary @click="amount += 50">+50</n-button>
              <n-button size="small" secondary @click="amount += 100">+100</n-button>
            </div>
            <div class="quick-btns" v-else>
              <n-button size="small" secondary @click="amount = Math.min(amount + 10, selectedProduct.currentQuantity)">+10</n-button>
              <n-button size="small" secondary @click="amount = Math.min(amount + 50, selectedProduct.currentQuantity)">+50</n-button>
              <n-button size="small" secondary @click="amount = selectedProduct.currentQuantity">全部</n-button>
            </div>

            <div class="op-preview" v-if="amount > 0">
              <span class="preview-before">{{ selectedProduct.currentQuantity }}</span>
              <span class="preview-arrow">→</span>
              <span
                class="preview-after"
                :class="{ danger: mode === 'out' && selectedProduct.currentQuantity - amount < 0 }"
              >
                {{ mode === 'in'
                  ? selectedProduct.currentQuantity + amount
                  : selectedProduct.currentQuantity - amount }}
              </span>
              <span class="preview-diff" :class="mode === 'out' ? 'diff-out' : 'diff-in'">
                {{ mode === 'in' ? '+' : '-' }}{{ amount }}
              </span>
            </div>
            <div class="op-warn-text" v-if="mode === 'out' && amount > selectedProduct.currentQuantity">
              出库数量超出当前库存
            </div>

            <n-button
              :type="mode === 'in' ? 'primary' : 'error'"
              block
              size="large"
              :loading="submitting"
              :disabled="mode === 'out' && (amount > selectedProduct.currentQuantity || selectedProduct.currentQuantity <= 0)"
              @click="execute"
              style="margin-top: 14px;"
            >
              确认{{ mode === 'in' ? '入库' : '出库' }} {{ amount }} {{ selectedProduct.unit || '件' }}
            </n-button>
          </template>
          <n-empty v-else description="从左侧选择商品开始操作" style="padding: 32px 0;" />
        </div>

        <!-- 历史记录 -->
        <div class="history-card">
          <div class="history-head">
            <span class="history-title">最近操作记录</span>
            <div class="history-tabs">
              <button class="htab" :class="{ active: historyTab === 'in' }" @click="historyTab = 'in'">入库</button>
              <button class="htab" :class="{ active: historyTab === 'out' }" @click="historyTab = 'out'">出库</button>
            </div>
          </div>
          <div class="history-list" v-if="currentHistory.length">
            <div class="history-item" v-for="r in currentHistory" :key="r.id">
              <span class="hi-name">{{ r.itemName }}</span>
              <span class="hi-qty" :class="historyTab === 'in' ? 'qty-in' : 'qty-out'">
                {{ historyTab === 'in' ? '+' : '-' }}{{ r.quantity }}
              </span>
              <span class="hi-time">{{ r.time }}</span>
            </div>
          </div>
          <n-empty v-else description="暂无记录" size="small" style="padding: 20px 0;" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useMessage } from 'naive-ui'
import { SearchOutline, LogInOutline, LogOutOutline } from '@vicons/ionicons5'
import { getProductList } from '@/api/product'
import { inboundAction, outboundAction, getInboundRecords, getOutboundRecords } from '@/api/movement'

const route = useRoute()
const message = useMessage()
const loading = ref(false)
const products = ref([])
const searchQuery = ref('')
const selectedProduct = ref(null)
const amount = ref(1)
const mode = ref(route.meta?.defaultMode || 'in')

watch(() => route.meta?.defaultMode, (v) => {
  if (v) { mode.value = v; selectedProduct.value = null; amount.value = 1 }
})
const submitting = ref(false)
const historyTab = ref('in')

// 历史记录（从后端加载，带商品名 join）
const inboundHistory = ref([])
const outboundHistory = ref([])

const currentHistory = computed(() =>
  historyTab.value === 'in' ? inboundHistory.value : outboundHistory.value
)

const filteredProducts = computed(() => {
  const q = searchQuery.value.toLowerCase()
  if (!q) return products.value
  return products.value.filter(p =>
    (p.itemName || '').toLowerCase().includes(q) ||
    (p.skuCode || '').toLowerCase().includes(q)
  )
})

function selectProduct(p) {
  if (p.currentQuantity <= 0 && mode.value === 'out') return
  selectedProduct.value = p
  amount.value = 1
}

function formatTime(t) {
  if (!t) return ''
  return t.replace('T', ' ').slice(0, 16)
}

// 产品 id → name 映射，用于历史记录展示
const productMap = computed(() => {
  const m = {}
  products.value.forEach(p => { m[p.id] = p })
  return m
})

async function loadHistory() {
  try {
    const [inRes, outRes] = await Promise.all([getInboundRecords(), getOutboundRecords()])
    // 后端返回的是 DTO，没有商品名，需要用 itemId 查 productMap
    inboundHistory.value = (inRes || [])
      .sort((a, b) => b.id - a.id)
      .slice(0, 20)
      .map(r => ({
        id: r.id,
        itemName: productMap.value[r.itemId]?.itemName || `商品#${r.itemId}`,
        quantity: r.quantity,
        time: formatTime(r.inboundTime)
      }))
    outboundHistory.value = (outRes || [])
      .sort((a, b) => b.id - a.id)
      .slice(0, 20)
      .map(r => ({
        id: r.id,
        itemName: productMap.value[r.itemId]?.itemName || `商品#${r.itemId}`,
        quantity: r.quantity,
        time: formatTime(r.outboundTime)
      }))
  } catch { /* 静默 */ }
}

async function fetchData() {
  loading.value = true
  try {
    products.value = await getProductList() || []
  } catch {
    message.error('获取商品列表失败')
  } finally {
    loading.value = false
  }
}

async function execute() {
  if (!selectedProduct.value) return
  submitting.value = true
  try {
    if (mode.value === 'in') {
      await inboundAction({ itemId: selectedProduct.value.id, amount: amount.value })
      message.success(`入库成功：${selectedProduct.value.itemName} +${amount.value}`)
      inboundHistory.value.unshift({
        id: Date.now(),
        itemName: selectedProduct.value.itemName,
        quantity: amount.value,
        time: formatTime(new Date().toISOString())
      })
      if (inboundHistory.value.length > 20) inboundHistory.value.pop()
      selectedProduct.value = { ...selectedProduct.value, currentQuantity: selectedProduct.value.currentQuantity + amount.value }
      const idx = products.value.findIndex(p => p.id === selectedProduct.value.id)
      if (idx >= 0) products.value[idx] = { ...products.value[idx], currentQuantity: selectedProduct.value.currentQuantity }
    } else {
      await outboundAction({ itemId: selectedProduct.value.id, amount: amount.value })
      message.success(`出库成功：${selectedProduct.value.itemName} -${amount.value}`)
      outboundHistory.value.unshift({
        id: Date.now(),
        itemName: selectedProduct.value.itemName,
        quantity: amount.value,
        time: formatTime(new Date().toISOString())
      })
      if (outboundHistory.value.length > 20) outboundHistory.value.pop()
      selectedProduct.value = { ...selectedProduct.value, currentQuantity: selectedProduct.value.currentQuantity - amount.value }
      const idx = products.value.findIndex(p => p.id === selectedProduct.value.id)
      if (idx >= 0) products.value[idx] = { ...products.value[idx], currentQuantity: selectedProduct.value.currentQuantity }
    }
    amount.value = 1
  } catch {
    message.error('操作失败')
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  await fetchData()
  await loadHistory()
})
</script>

<style scoped>
.movement-page {
  height: calc(100vh - 112px);
  display: flex;
  flex-direction: column;
}
.split-panel {
  display: flex;
  gap: 14px;
  flex: 1;
  min-height: 0;
}

/* 左侧商品列表 */
.panel-left {
  flex: 3;
  background: #fff;
  border-radius: 10px;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: 0;
}
.panel-left-header { flex-shrink: 0; }
.product-list { flex: 1; overflow-y: auto; display: flex; flex-direction: column; gap: 4px; }

.product-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.15s;
  border: 1px solid transparent;
}
.product-row:hover { background: #f5f6f8; }
.product-row.selected { background: #e8f0fe; border-color: #c7d7fa; }
.product-row.depleted { opacity: 0.45; cursor: default; }
.pr-main { display: flex; flex-direction: column; gap: 2px; min-width: 0; }
.pr-name { font-size: 14px; font-weight: 500; color: #1a1a2e; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.pr-sku { font-size: 11px; color: #bbb; }

/* 右侧 */
.panel-right {
  flex: 2;
  min-width: 300px;
  display: flex;
  flex-direction: column;
  gap: 14px;
  min-height: 0;
}

/* 操作卡 */
.op-card {
  background: #fff;
  border-radius: 10px;
  padding: 20px;
  flex-shrink: 0;
}
.op-product-bar {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 14px;
}
.op-product-info { display: flex; flex-direction: column; gap: 2px; }
.op-product-name { font-size: 16px; font-weight: 600; color: #1a1a2e; }
.op-product-sku { font-size: 11px; color: #bbb; }

.op-tabs {
  display: flex;
  gap: 6px;
  margin-bottom: 14px;
}
.op-tab {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 8px;
  border: 1px solid #e8eaf0;
  border-radius: 8px;
  background: #f5f6f8;
  color: #888;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.15s;
}
.op-tab.active { background: #e8f0fe; border-color: #91b4f5; color: #0052D9; font-weight: 600; }
.op-tab-out.active { background: #fef0f0; border-color: #f5b8c0; color: #d03050; }

.op-input-row { margin-bottom: 8px; }
.quick-btns { display: flex; flex-direction: row; gap: 6px; }

.op-preview {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 12px;
  padding: 10px 14px;
  background: #f9fafb;
  border-radius: 8px;
  font-size: 15px;
  font-weight: 600;
}
.preview-before { color: #aaa; }
.preview-arrow { color: #ccc; font-size: 13px; }
.preview-after { color: #0052D9; }
.preview-after.danger { color: #d03050; }
.preview-diff { font-size: 13px; font-weight: 600; }
.diff-in { color: #18a058; }
.diff-out { color: #d03050; }
.op-warn-text { font-size: 12px; color: #d03050; margin-top: 4px; }

/* 历史记录卡 */
.history-card {
  background: #fff;
  border-radius: 10px;
  padding: 16px;
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
}
.history-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
  flex-shrink: 0;
}
.history-title { font-size: 13px; font-weight: 600; color: #888; }
.history-tabs { display: flex; background: #f5f6f8; border-radius: 6px; padding: 2px; gap: 2px; }
.htab {
  border: none; background: transparent; cursor: pointer;
  font-size: 12px; color: #888; padding: 2px 10px; border-radius: 4px;
  transition: all 0.15s;
}
.htab.active { background: #fff; color: #0052D9; font-weight: 600; box-shadow: 0 1px 3px rgba(0,0,0,0.08); }

.history-list { overflow-y: auto; flex: 1; }
.history-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 0;
  border-bottom: 1px solid #f5f5f5;
  font-size: 13px;
}
.hi-name { flex: 1; color: #333; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.hi-qty { font-weight: 700; flex-shrink: 0; }
.qty-in { color: #18a058; }
.qty-out { color: #d03050; }
.hi-time { color: #aaa; font-size: 11px; white-space: nowrap; flex-shrink: 0; }
</style>
