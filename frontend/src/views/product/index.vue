<template>
  <div class="product-page">
    <div class="stat-strip">
      <div class="stat-item">
        <div class="stat-num">{{ tableData.length }}</div>
        <div class="stat-desc">商品种类</div>
      </div>
      <div class="stat-divider" />
      <div class="stat-item">
        <div class="stat-num">{{ totalStock }}</div>
        <div class="stat-desc">库存总量</div>
      </div>
      <div class="stat-divider" />
      <div class="stat-item stat-warn">
        <div class="stat-num">{{ lowStockCount }}</div>
        <div class="stat-desc">低库存预警</div>
      </div>
    </div>

    <div class="table-toolbar">
      <div class="toolbar-left">
        <h2 class="page-title">商品管理</h2>
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
      <n-button type="primary" @click="openCreateModal">
        <template #icon><n-icon :component="AddOutline" /></template>
        新增商品
      </n-button>
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

function stockTag(quantity) {
  if (quantity >= 50) return h(NTag, { type: 'success', size: 'small', round: true }, { default: () => quantity })
  if (quantity >= 10) return h(NTag, { type: 'warning', size: 'small', round: true }, { default: () => quantity })
  return h(NTag, { type: 'error', size: 'small', round: true }, { default: () => quantity })
}

const columns = [
  { title: 'ID', key: 'id', width: 60 },
  { title: 'SKU 编码', key: 'skuCode', width: 150, ellipsis: { tooltip: true } },
  { title: '商品名称', key: 'itemName', width: 220, ellipsis: { tooltip: true } },
  {
    title: '库存量', key: 'currentQuantity', width: 110,
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
.product-page { padding-top: 24px; }

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
</style>
