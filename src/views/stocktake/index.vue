<template>
  <div class="page-container">
    <n-card title="库存盘点记录" :bordered="false" class="page-card">
      <template #header-extra>
        <n-button type="primary" @click="openModal">发起新盘点</n-button>
      </template>

      <!-- 盘点历史数据表格 -->
      <n-data-table
        :columns="columns"
        :data="tableData"
        :loading="loading"
        :pagination="pagination"
        :bordered="false"
      />
    </n-card>

    <!-- 发起盘点弹窗 -->
    <n-modal v-model:show="showModal" preset="card" title="执行商品盘点" style="width: 500px;">
      <n-form ref="formRef" :model="formData" :rules="rules" label-placement="left" label-width="120">
        <n-form-item label="选择商品" path="itemId">
          <n-select 
            v-model:value="formData.itemId" 
            :options="productOptions" 
            placeholder="请选择要盘点的商品" 
            filterable 
            clearable
          />
        </n-form-item>
        
        <n-form-item label="实际盘库数量" path="actualQuantity">
          <n-input-number v-model:value="formData.actualQuantity" :min="0" placeholder="仓库目前实际剩余数量" style="width: 100%;" />
        </n-form-item>

        <n-alert v-if="formData.itemId" type="info" :bordered="false" style="margin-top: 10px;">
          当前系统账面库存：{{ getSystemStock(formData.itemId) }} 件
          <div v-if="formData.actualQuantity !== null">
            预估账面盈亏：
            <n-tag :type="getDiffColor()" size="small" style="margin-top: 4px">
              {{ getDiffText() }}
            </n-tag>
          </div>
        </n-alert>
      </n-form>
      
      <template #footer>
        <n-space justify="end">
          <n-button @click="showModal = false">取消</n-button>
          <n-button type="primary" :loading="submitLoading" @click="handleSubmit">执行平账与盘点</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, h, onMounted, computed } from 'vue'
import { NButton, NSpace, useMessage, NTag, NPopconfirm } from 'naive-ui'
import { getStocktakeList, executeStocktake, deleteStocktakeRecord } from '@/api/stocktake'
import { getProductList } from '@/api/product'

const message = useMessage()
const loading = ref(false)
const tableData = ref([])
const productsInfo = ref([]) // 用于存商品映射字典

const pagination = ref({ pageSize: 10 })

// 表格列定义
const columns = [
  { title: '记录号', key: 'id', width: 80 },
  { title: '商品名称', key: 'itemName', minWidth: 200 },
  { 
    title: '盘点时间', 
    key: 'stocktakeTime',
    width: 180,
    render(row) {
      if (!row.stocktakeTime) return '-'
      // 将 LocalDateTime 的 T 替换为空格，截取到秒
      return row.stocktakeTime.replace('T', ' ').substring(0, 19)
    }
  },
  { title: '系统账面库存', key: 'systemQuantity' },
  { title: '实际盘点库存', key: 'actualQuantity' },
  { 
    title: '盘盈盘亏', 
    key: 'diffQuantity',
    render(row) {
      const diff = row.diffQuantity
      if (diff === null || diff === undefined) return '未知'
      if (diff > 0) return h(NTag, { type: 'success', size: 'small' }, { default: () => `盘盈 +${diff}` })
      if (diff < 0) return h(NTag, { type: 'error', size: 'small' }, { default: () => `盘亏 ${diff}` })
      return h(NTag, { type: 'default', size: 'small' }, { default: () => '账实相符' })
    }
  },
  {
    title: '操作',
    key: 'actions',
    width: 120,
    render(row) {
      return h(
        NPopconfirm,
        { onPositiveClick: () => handleDelete(row.id) },
        {
          trigger: () => h(NButton, { size: 'small', type: 'error', tertiary: true }, { default: () => '删除该记录' }),
          default: () => '确认删除这条盘点历史吗？'
        }
      )
    }
  }
]

const fetchData = async () => {
  loading.value = true
  try {
    // 并发拉取两个接口（商品字典和盘点记录）
    const [productsRes, stocktakeRes] = await Promise.all([
      getProductList(),
      getStocktakeList()
    ])
    productsInfo.value = productsRes || []
    tableData.value = stocktakeRes || [] 
  } catch (err) {
    message.error('获取盘点历史数据失败')
  } finally {
    loading.value = false
  }
}

// 删除记录
const handleDelete = async (id) => {
  try {
    await deleteStocktakeRecord(id)
    message.success('记录删除成功')
    fetchData()
  } catch (error) {
    message.error('删除记录失败')
  }
}

// 弹窗与提交逻辑
const showModal = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)

const formData = ref({
  itemId: null,
  actualQuantity: 0
})

const rules = {
  itemId: { type: 'number', required: true, message: '请选择盘点商品', trigger: ['blur', 'change'] },
  actualQuantity: { type: 'number', required: true, message: '请输入实际查库数量', trigger: 'blur' }
}

const productOptions = computed(() => {
  return productsInfo.value.map(p => ({
    label: `${p.itemName} (库存: ${p.currentQuantity})`,
    value: p.id
  }))
})

const getSystemStock = (itemId) => {
  const p = productsInfo.value.find(x => x.id === itemId)
  return p ? p.currentQuantity : 0
}

const getDiffColor = () => {
  const diff = formData.value.actualQuantity - getSystemStock(formData.value.itemId)
  if (diff > 0) return 'success'
  if (diff < 0) return 'error'
  return 'default'
}

const getDiffText = () => {
  const diff = formData.value.actualQuantity - getSystemStock(formData.value.itemId)
  if (diff > 0) return `盘盈 +${diff} (将自动增加库存)`
  if (diff < 0) return `盘亏 ${diff} (将自动扣减库存)`
  return '账面相符，无须平账'
}

const openModal = () => {
  formData.value = { itemId: null, actualQuantity: 0 }
  showModal.value = true
}

const handleSubmit = (e) => {
  e.preventDefault()
  formRef.value?.validate(async (errors) => {
    if (!errors) {
      submitLoading.value = true
      try {
        await executeStocktake(formData.value.itemId, formData.value.actualQuantity)
        message.success('盘点执行成功，系统已自动平账')
        showModal.value = false
        fetchData() // 刷新列表
      } catch (error) {
        message.error('盘点执行失败')
      } finally {
        submitLoading.value = false
      }
    }
  })
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.page-container { height: 100%; }
.page-card {
  border-radius: 8px;
  box-shadow: 0 1px 2px -2px rgba(0,0,0,0.08), 0 3px 6px 0 rgba(0,0,0,0.06), 0 5px 12px 4px rgba(0,0,0,0.04);
}
</style>
