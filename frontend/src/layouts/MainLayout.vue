<template>
  <n-layout has-sider style="height: 100vh;">
    <n-layout-sider
      bordered
      collapse-mode="width"
      :collapsed-width="64"
      :width="200"
      :collapsed="collapsed"
      style="background-color: #001529;"
    >
      <div class="logo">
        <span v-if="!collapsed" class="logo-text">FlowStock</span>
        <span v-else class="logo-text-short">FS</span>
      </div>
      <n-menu
        inverted
        :options="menuOptions"
        :value="activeKey"
        :collapsed="collapsed"
        @update:value="handleMenuClick"
      />
      <div class="sidebar-footer">
        <div class="collapse-btn" @click="collapsed = !collapsed">
          <n-icon :component="collapsed ? ChevronForwardOutline : ChevronBackOutline" />
        </div>
        <span v-if="!collapsed" class="version-text">v1.0</span>
      </div>
    </n-layout-sider>

    <n-layout>
      <n-layout-content content-style="padding: 0; background-color: #f5f6f8; min-height: 100vh; display: flex; flex-direction: column;">
        <div class="top-bar">
          <div class="top-bar-left">
            <span class="top-bar-title">{{ pageTitle }}</span>
          </div>
          <div class="top-bar-right">
            <span class="top-bar-time">{{ currentTime }}</span>
            <div class="top-bar-avatar">
              <n-icon :component="PersonCircleOutline" size="22" />
            </div>
          </div>
        </div>
        <div class="page-content">
          <router-view v-slot="{ Component }">
            <transition name="fade-slide" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </div>
      </n-layout-content>
    </n-layout>
  </n-layout>
</template>

<script setup>
import { ref, computed, h, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { NIcon } from 'naive-ui'
import {
  PieChartOutline,
  CubeOutline,
  LogInOutline,
  LogOutOutline,
  ClipboardOutline,
  ChevronBackOutline,
  ChevronForwardOutline,
  PersonCircleOutline
} from '@vicons/ionicons5'

const router = useRouter()
const route = useRoute()

const collapsed = ref(localStorage.getItem('sidebar-collapsed') === 'true')

watch(collapsed, (val) => {
  localStorage.setItem('sidebar-collapsed', String(val))
})

const activeKey = computed(() => route.path.replace('/', '') || 'dashboard')

const pageTitleMap = {
  dashboard: '主页',
  product: '商品管理',
  inbound: '入库管理',
  outbound: '出库管理',
  stocktake: '库存盘点'
}

const pageTitle = computed(() => pageTitleMap[activeKey.value] || '')

const currentTime = ref('')
function updateTime() {
  const now = new Date()
  currentTime.value = now.toLocaleDateString('zh-CN', { month: 'long', day: 'numeric', weekday: 'short' })
}
updateTime()
setInterval(updateTime, 60000)

function renderIcon(icon) {
  return () => h(NIcon, null, { default: () => h(icon) })
}

const menuOptions = [
  { label: '主页', key: 'dashboard', icon: renderIcon(PieChartOutline) },
  { label: '商品管理', key: 'product', icon: renderIcon(CubeOutline) },
  { label: '入库管理', key: 'inbound', icon: renderIcon(LogInOutline) },
  { label: '出库管理', key: 'outbound', icon: renderIcon(LogOutOutline) },
  { label: '库存盘点', key: 'stocktake', icon: renderIcon(ClipboardOutline) }
]

const handleMenuClick = (key) => {
  router.push(`/${key}`)
}
</script>

<style scoped>
.logo {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  white-space: nowrap;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}
.logo-text {
  color: white;
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 0.5px;
}
.logo-text-short {
  color: white;
  font-size: 16px;
  font-weight: 700;
}
.sidebar-footer {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 12px 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
}
.collapse-btn {
  color: rgba(255, 255, 255, 0.55);
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: color 0.15s;
}
.collapse-btn:hover {
  color: rgba(255, 255, 255, 0.9);
}
.version-text {
  color: rgba(255, 255, 255, 0.25);
  font-size: 11px;
  margin-top: 4px;
}

.top-bar {
  height: 52px;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  flex-shrink: 0;
  position: sticky;
  top: 0;
  z-index: 10;
}
.top-bar-title {
  font-size: 15px;
  font-weight: 600;
  color: #1a1a2e;
}
.top-bar-right {
  display: flex;
  align-items: center;
  gap: 16px;
}
.top-bar-time {
  font-size: 13px;
  color: #999;
}
.top-bar-avatar {
  color: #888;
  cursor: pointer;
  display: flex;
  align-items: center;
  transition: color 0.15s;
}
.top-bar-avatar:hover { color: #0052D9; }
.page-content {
  padding: 20px 24px 24px;
  flex: 1;
}

.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.2s cubic-bezier(0.25, 0.8, 0.25, 1);
}
.fade-slide-enter-from {
  opacity: 0;
  transform: translateX(8px);
}
.fade-slide-leave-to {
  opacity: 0;
  transform: translateX(-8px);
}
</style>
