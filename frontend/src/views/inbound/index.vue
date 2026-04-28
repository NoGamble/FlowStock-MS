<template>
  <div class="inbound-page">
    <div class="stat-strip">
      <div class="stat-item">
        <div class="stat-num">{{ tableData.length }}</div>
        <div class="stat-desc">可入库商品</div>
      </div>
      <div class="stat-divider" />
      <div class="stat-item">
        <div class="stat-num">{{ totalStock }}</div>
        <div class="stat-desc">库存总量</div>
      </div>
      <div class="stat-divider" />
      <div class="stat-item stat-warn">
        <div class="stat-num">{{ lowStockCount }}</div>
        <div class="stat-desc">库存告急</div>
      </div>
    </div>

    <div class="table-toolbar">
      <div class="toolbar-left">
        <h2 class="page-title">入库操作台</h2>
        <n-input
          v-model:value="searchQuery"
          placeholder="搜索名称或 SKU..."
          clearable
          class="search-input"
        >
          <template #prefix>
            <n-icon :component="SearchOutline" />
          </template>
        </n-input>
      </div>
      <n-button type="info" secondary @click="fetchData">刷新</n-button>
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
        <n-empty description="暂无商品数据">
          <template #extra>
            <n-button type="primary" @click="$router.push('/product')">去新增商品</n-button>
          </template>
        </n-empty>
      </template>
    </n-data-table>

    <n-modal v-model:show="showModal" preset="card" title="商品入库" style="width: 420px;">
      <template v-if="selectedRow">
        <div class="modal-product-info">
          <span class="mpi-name">{{ selectedRow.itemName }}</span>
          <n-tag :type="selectedRow.currentQuantity <= 10 ? 'error' : 'success'" size="small" round>
            当前库存 {{ selectedRow.currentQuantity }} 件
          </n-tag>
        </div>
        <div class="mpi-preview" v-if="formData.amount > 0">
          <span class="mpi-arrow">{{ selectedRow.currentQuantity }}</span>
          <span class="mpi-arrow-icon">→</span>
          <span class="mpi-after">{{ selectedRow.currentQuantity + formData.amount }}</span>
          <span class="mpi-diff">(+{{ formData.amount }})</span>
        </div>
      </template>

      <n-form ref="formRef" :model="formData" :rules="rules" label-placement="top" style="margin-top: 16px;">
        <n-form-item label="入库数量" path="amount">
          <n-input-number v-model:value="formData.amount" :min="1" placeholder="请输入此次入库数量" style="width: 100%;" />
        </n-form-item>
        <div class="quick-btns">
          <n-button size="tiny" secondary @click="formData.amount += 10">+10</n-button>
          <n-button size="tiny" secondary @click="formData.amount += 50">+50</n-button>
          <n-button size="tiny" secondary @click="formData.amount += 100">+100</n-button>
        </div>
      </n-form>

      <template #footer>
        <n-space justify="end">
          <n-button @click="showModal = false">取消</n-button>
          <n-button type="primary" :loading="submitLoading" @click="handleSubmit">确认入库</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, h, onMounted, computed } from 'vue'
import { NButton, NSpace, useMessage, NIcon, NTag } from 'naive-ui'
import { LogInOutline, SearchOutline } from '@vicons/ionicons5'
import { getProductList } from '@/api/product'
import { inboundAction } from '@/api/movement'

const message = useMessage()
const loading = ref(false)
const tableData = ref([])
const searchQuery = ref('')
const pagination = ref({ pageSize: 10 })

const totalStock = computed(() =>
  tableData.value.reduce((sum, p) => sum + (p.currentQuantity || 0), 0)
)
const lowStockCount = computed(() =>
  tableData.value.filter(p => (p.currentQuantity || 0) <= 10).length
)

const filteredData = computed(() => {
  if (!searchQuery.value) return tableData.value
  const q = searchQuery.value.toLowerCase()
  return tableData.value.filter(row =>
    (row.itemName || '').toLowerCase().includes(q) ||
    (row.skuCode || '').toLowerCase().includes(q)
  )
})

const columns = [
  { title: 'ID', key: 'id', width: 60 },
  { title: 'SKU 编码', key: 'skuCode', width: 150, ellipsis: { tooltip: true } },
  { title: '商品名称', key: 'itemName', width: 200, ellipsis: { tooltip: true } },
  {
    title: '当前库存', key: 'currentQuantity', width: 130,
    render(row) {
      if (row.currentQuantity <= 10) {
        return h(NTag, { type: 'error', size: 'small', round: true }, { default: () => row.currentQuantity + ' (告急)' })
      }
      return h(NTag, { type: 'success', size: 'small', round: true }, { default: () => row.currentQuantity })
    }
  },
  { title: '单位', key: 'unit', width: 70 },
  {
    title: '操作', key: 'actions', width: 120,
    render(row) {
      return h(
        NButton,
        { size: 'small', type: 'primary', onClick: () => openModal(row) },
        {
          default: () => '办理入库',
          icon: () => h(NIcon, null, { default: () => h(LogInOutline) })
        }
      )
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
const selectedRow = ref(null)
const formData = ref({ amount: 1 })
const rules = { amount: { type: 'number', required: true, message: '请输入入库数量', trigger: 'blur' } }

const openModal = (row) => {
  selectedRow.value = row
  formData.value.amount = 1
  showModal.value = true
}

const handleSubmit = (e) => {
  e.preventDefault()
  formRef.value?.validate(async (errors) => {
    if (!errors) {
      submitLoading.value = true
      try {
        await inboundAction({ itemId: selectedRow.value.id, amount: formData.value.amount })
        message.success(`成功入库 ${formData.value.amount} 件 ${selectedRow.value.itemName}`)
        showModal.value = false
        fetchData()
      } catch (error) {
        message.error('入库操作失败')
      } finally {
        submitLoading.value = false
      }
    }
  })
}

onMounted(() => fetchData())
</script>

<style scoped>
.inbound-page { padding-top: 24px; }

/* --- stat strip --- */
.stat-strip {
  display: flex;
  align-items: center;
  gap: 0;
  background: #fff;
  border-radius: 10px;
  padding: 20px 32px;
  margin-bottom: 20px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
}
.stat-item { flex: 1; }
.stat-num {
  font-size: 28px;
  font-weight: 600;
  color: #1a1a2e;
  line-height: 1;
  margin-bottom: 6px;
}
.stat-desc {
  font-size: 13px;
  color: #999;
}
.stat-warn .stat-num { color: #d03050; }
.stat-divider {
  width: 1px;
  height: 40px;
  background: #f0f0f0;
}

/* --- toolbar --- */
.table-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.toolbar-left {
  display: flex;
  align-items: center;
  gap: 16px;
}
.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0;
}
.search-input { width: 240px; }

/* --- modal --- */
.modal-product-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  background: #f9fafb;
  border-radius: 8px;
}
.mpi-name { font-size: 15px; font-weight: 600; color: #1a1a2e; }
.mpi-preview {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 12px;
  padding: 10px 16px;
  background: #e8f0fe;
  border-radius: 8px;
  font-size: 18px;
  font-weight: 600;
}
.mpi-arrow { color: #666; }
.mpi-arrow-icon { color: #999; font-size: 14px; }
.mpi-after { color: #0052D9; }
.mpi-diff { color: #18a058; font-size: 13px; }
.quick-btns { display: flex; gap: 8px; margin-top: 4px; }
</style>
