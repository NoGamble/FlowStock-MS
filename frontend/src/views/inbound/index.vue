<template>
  <div class="inbound-page">
    <h2 class="page-title">入库操作台</h2>

    <div class="split-panel">
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
import { NButton, useMessage, NIcon, NTag } from 'naive-ui'
import { SearchOutline, ArrowForwardOutline } from '@vicons/ionicons5'
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

function addAmount(n) { inboundAmount.value += n }

const executeInbound = async () => {
  if (!selectedProduct.value) return
  submitting.value = true
  try {
    await inboundAction({ itemId: selectedProduct.value.id, amount: inboundAmount.value })
    message.success(`成功入库 ${inboundAmount.value} 件 ${selectedProduct.value.itemName}`)

    recentRecords.value.unshift({
      id: Date.now(),
      inventory: { itemName: selectedProduct.value.itemName },
      quantity: inboundAmount.value,
      inboundTime: new Date().toISOString()
    })
    if (recentRecords.value.length > 10) recentRecords.value.pop()

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
.preview-from { color: #999; font-weight: 500; }
.preview-to { color: #18a058; font-weight: 700; font-size: 18px; }
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
.history-name { flex: 1; color: #333; }
.history-qty { color: #18a058; font-weight: 600; }
.history-time { color: #aaa; font-size: 12px; white-space: nowrap; }
</style>
