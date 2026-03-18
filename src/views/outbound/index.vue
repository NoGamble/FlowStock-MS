<template>
  <div class="page-container">
    <n-card title="出库操作台" :bordered="false" class="page-card">
      <template #header-extra>
        <n-button type="info" secondary @click="fetchData">刷新最新状态</n-button>
      </template>

      <!-- 目前后端没有出库流水查询的GET接口，因此前端布局为展示【可出库商品池】，点击出库呼出操作面板 -->
      <n-data-table
        :columns="columns"
        :data="tableData"
        :loading="loading"
        :pagination="pagination"
        :bordered="false"
      />
    </n-card>

    <!-- 出库弹窗 -->
    <n-modal v-model:show="showModal" preset="card" title="商品领用 / 出库" style="width: 450px;">
      <n-alert v-if="selectedRow" title="操作目标" type="warning" :bordered="false" style="margin-bottom: 20px;">
        正在出库 「{{ selectedRow.itemName }}」 (当前可用库存: {{ selectedRow.currentQuantity }})
      </n-alert>
      
      <n-form ref="formRef" :model="formData" :rules="rules" label-placement="left" label-width="100">
        <n-form-item label="出库数量" path="amount">
          <n-input-number 
            v-model:value="formData.amount" 
            :min="1" 
            :max="selectedRow ? selectedRow.currentQuantity : 9999" 
            placeholder="请输入此次出库数量" 
            style="width: 100%;" 
          />
        </n-form-item>
      </n-form>
      
      <template #footer>
        <n-space justify="end">
          <n-button @click="showModal = false">取消</n-button>
          <n-button type="primary" color="#d03050" :loading="submitLoading" @click="handleSubmit">执行出库</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, h, onMounted } from 'vue'
import { NButton, NSpace, useMessage, NIcon, NTag } from 'naive-ui'
import { LogOutOutline } from '@vicons/ionicons5'
import { getProductList } from '@/api/product'
import { outboundAction } from '@/api/movement'

const message = useMessage()
const loading = ref(false)
const tableData = ref([])

const pagination = ref({ pageSize: 10 })

// 表格列定义
const columns = [
  { title: '商品 ID', key: 'id', width: 80 },
  { title: '商品名称', key: 'itemName' },
  { 
    title: '当前库存量', 
    key: 'currentQuantity',
    render(row) {
      if (row.currentQuantity === 0) {
        return h(NTag, { type: 'error', size: 'small', round: true }, { default: () => '库存已耗尽' })
      }
      return h(NTag, { type: 'success', size: 'small', round: true }, { default: () => row.currentQuantity })
    }
  },
  {
    title: '操作',
    key: 'actions',
    width: 120,
    render(row) {
      return h(
        NButton,
        { 
          size: 'small', 
          type: 'error', 
          disabled: row.currentQuantity <= 0,
          onClick: () => openModal(row) 
        },
        { 
          default: () => '办理出库',
          icon: () => h(NIcon, null, { default: () => h(LogOutOutline) })
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

// 弹窗逻辑
const showModal = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)
const selectedRow = ref(null)

const formData = ref({ amount: 1 })
const rules = { amount: { type: 'number', required: true, message: '请输入出库数量', trigger: 'blur' } }

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
        await outboundAction({
          itemId: selectedRow.value.id,
          amount: formData.value.amount
        })
        message.success(`成功出库 ${formData.value.amount} 件 ${selectedRow.value.itemName}`)
        showModal.value = false
        fetchData() // 刷新列表查看最新库存
      } catch (error) {
        // 后端可能抛出业务异常(如库存不足)
        message.error('出库操作失败, 可能库存不足或网络异常')
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
