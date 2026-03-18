<template>
  <n-layout has-sider style="height: 100vh;">
    
    <n-layout-sider
      bordered
      collapse-mode="width"
      :collapsed-width="64"
      :width="240"
      style="background-color: #001529"
    >
      <n-menu inverted :options="menuOptions" default-value="inventory" />
    </n-layout-sider>

    <n-layout>
      <n-layout-header bordered style="padding: 0 24px; height: 64px; display: flex; align-items: center; justify-content: flex-start;">
        <n-h2 style="margin: 0; font-weight: 500;">FlowStock 系统</n-h2>
      </n-layout-header>

      <n-layout-content content-style="padding: 24px;">
        <n-space vertical size="large" align="start" style="width: 100%;">
          <n-button type="primary" :loading="loading" @click="fetchData">
            刷新库存数据
          </n-button>
          
          <n-data-table
            :columns="columns"
            :data="inventoryList"
            :loading="loading"
            :bordered="true"
            style="width: 100%;" 
          />
        </n-space>
      </n-layout-content>
    </n-layout>

  </n-layout>
</template>

<script setup>
import { ref, h, onMounted } from 'vue'
import axios from 'axios'
import { useMessage, NIcon } from 'naive-ui'
import { CubeOutline, SettingsOutline } from '@vicons/ionicons5'

const columns = [
  { title: 'ID', key: 'id', width: 80 },
  { title: '商品名称', key: 'itemName' },
  { title: '库存数量', key: 'currentQuantity' }
]

const message = useMessage()
const inventoryList = ref([])
const loading = ref(false)

const menuOptions = [
  { label: '库存管理', key: 'inventory', icon: () => h(NIcon, null, { default: () => h(CubeOutline) }) },
  { label: '用户设置', key: 'settings', icon: () => h(NIcon, null, { default: () => h(SettingsOutline) }) }
]

const fetchData = async (showSuccess = true) => {
  if (loading.value) return
  loading.value = true
  try {
    const res = await axios.get('/api/products')
    inventoryList.value = res.data
    if (showSuccess) {
      message.success('数据抓取成功！')
    }
  } catch (error) {
    message.error('无法连接到后端，请检查 IDEA 运行状态')
    console.error(error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchData(false)
})
</script>