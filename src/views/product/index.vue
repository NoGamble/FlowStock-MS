<template>
  <div class="page-container">
    <n-card title="商品管理" :bordered="false" class="page-card">
      <template #header-extra>
        <n-button type="primary" @click="openCreateModal">
          <template #icon>
            <n-icon :component="AddOutline" />
          </template>
          新增商品
        </n-button>
      </template>

      <!-- 数据表格 -->
      <n-data-table
        :columns="columns"
        :data="tableData"
        :loading="loading"
        :pagination="pagination"
        :bordered="false"
        :bottom-bordered="true"
        striped
        size="large"
      />
    </n-card>

    <!-- 新增 / 修改商品弹窗 -->
    <n-modal v-model:show="showModal" preset="card" :title="modalTitle" style="width: 500px;">
      <n-form ref="formRef" :model="formData" :rules="rules" label-placement="left" label-width="100">
        <n-form-item label="商品名称" path="itemName">
          <n-input v-model:value="formData.itemName" placeholder="请输入商品名称" />
        </n-form-item>
        <n-form-item label="当前库存" path="currentQuantity" v-if="isCreate">
          <n-input-number v-model:value="formData.currentQuantity" placeholder="初始库存数量" :min="0" style="width: 100%;" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showModal = false">取消</n-button>
          <n-button type="primary" :loading="submitLoading" @click="handleSubmit">确认提交</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, h, onMounted, computed } from 'vue'
import { NButton, NSpace, useMessage, NPopconfirm, NIcon } from 'naive-ui'
import { AddOutline } from '@vicons/ionicons5'
import { getProductList, createProduct, updateProduct, deleteProduct } from '@/api/product'

const message = useMessage()
const loading = ref(false)
const tableData = ref([])

// 表格分页配置
const pagination = ref({ pageSize: 10 })

// 表格列定义
const columns = [
  { title: '商品 ID', key: 'id', width: 80 },
  { title: '商品名称', key: 'itemName' },
  { title: '当前库存量', key: 'currentQuantity' },
  {
    title: '操作',
    key: 'actions',
    width: 150,
    render(row) {
      return h(
        NSpace,
        {},
        {
          default: () => [
            h(
              NButton,
              { size: 'small', type: 'info', tertiary: true, onClick: () => openEditModal(row) },
              { default: () => '编辑' }
            ),
            h(
              NPopconfirm,
              { onPositiveClick: () => handleDelete(row.id) },
              {
                trigger: () => h(NButton, { size: 'small', type: 'error', tertiary: true }, { default: () => '删除' }),
                default: () => '确定要删除该商品吗？'
              }
            )
          ]
        }
      )
    }
  }
]

// --- 数据获取 ---
const fetchData = async () => {
  loading.value = true
  try {
    const res = await getProductList()
    // 后端返回的直接就是 List<Inventory>，axios拦截器已经剥了最外层 response
    tableData.value = res || [] 
  } catch (err) {
    message.error('获取商品列表失败')
  } finally {
    loading.value = false
  }
}

// --- 弹窗与表单逻辑 ---
const showModal = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)
const isCreate = ref(true)

const formData = ref({
  id: null,
  itemName: '',
  currentQuantity: 0
})

const rules = {
  itemName: { required: true, message: '请输入商品名称', trigger: 'blur' }
}

const modalTitle = computed(() => isCreate.value ? '新增商品' : '修改商品信息')

const openCreateModal = () => {
  isCreate.value = true
  formData.value = { id: null, itemName: '', currentQuantity: 0 }
  showModal.value = true
}

const openEditModal = (row) => {
  isCreate.value = false
  // 浅拷贝当前行数据
  formData.value = { id: row.id, itemName: row.itemName, currentQuantity: row.currentQuantity }
  showModal.value = true
}

const handleSubmit = (e) => {
  e.preventDefault()
  formRef.value?.validate(async (errors) => {
    if (!errors) {
      submitLoading.value = true
      try {
        if (isCreate.value) {
          // 发起创建请求
          await createProduct({ 
            itemName: formData.value.itemName, 
            currentQuantity: formData.value.currentQuantity 
          })
          message.success('商品创建成功')
        } else {
          // 发起更新请求 (根据你的后端逻辑，虽然前端传了 currentQuantity，但后端只更新了 itemName，这合情合理)
          await updateProduct(formData.value.id, { itemName: formData.value.itemName })
          message.success('商品信息更新成功')
        }
        showModal.value = false
        fetchData() // 刷新列表
      } catch (error) {
        message.error('操作失败')
      } finally {
        submitLoading.value = false
      }
    }
  })
}

// --- 删除逻辑 ---
const handleDelete = async (id) => {
  try {
    await deleteProduct(id)
    message.success('商品删除成功')
    fetchData()
  } catch (error) {
    message.error('删除失败')
  }
}

// 页面加载时抓取数据
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
