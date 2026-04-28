<template>
  <div class="stocktake-page">
    <div class="split-panel">
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

        <n-data-table
          v-if="viewMode === 'table'"
          :columns="columns"
          :data="tableData"
          :loading="loading"
          :pagination="{ pageSize: 10 }"
          :bordered="false"
          size="small"
        />

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
import { NButton, useMessage, NTag, NPopconfirm, NIcon, NTimeline, NTimelineItem, NButtonGroup } from 'naive-ui'
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
.timeline-view { padding: 8px 0; }
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
.preview-row strong { font-size: 15px; }
</style>
