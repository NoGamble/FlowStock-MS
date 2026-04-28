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
      <n-layout-content content-style="padding: 0 24px 24px; background-color: #f5f6f8; min-height: 100vh;">
        <router-view v-slot="{ Component }">
          <transition name="fade-slide" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
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
  ChevronForwardOutline
} from '@vicons/ionicons5'

const router = useRouter()
const route = useRoute()

const collapsed = ref(localStorage.getItem('sidebar-collapsed') === 'true')

watch(collapsed, (val) => {
  localStorage.setItem('sidebar-collapsed', String(val))
})

const activeKey = computed(() => {
  return route.path.replace('/', '') || 'dashboard'
})

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
