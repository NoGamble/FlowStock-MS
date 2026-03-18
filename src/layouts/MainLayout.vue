<template>
  <n-layout has-sider style="height: 100vh;">
    <!-- 左侧菜单栏 -->
    <n-layout-sider
      bordered
      collapse-mode="width"
      :collapsed-width="64"
      :width="200"
      style="background-color: #001529;"
    >
      <div class="logo">
        <span class="logo-text">FlowStock</span>
      </div>
      <n-menu
        inverted
        :options="menuOptions"
        :value="activeKey"
        @update:value="handleMenuClick"
      />
    </n-layout-sider>

    <!-- 右侧主体内容 -->
    <n-layout>
      <!-- 顶部 Header -->
      <n-layout-header bordered class="header">
        <n-h2 style="margin: 0; font-weight: 500;">库存控制管理系统</n-h2>
      </n-layout-header>

      <!-- 动态路由内容区 -->
      <n-layout-content content-style="padding: 24px; background-color: #f0f2f5; min-height: calc(100vh - 64px);">
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
import { computed, h } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { NIcon } from 'naive-ui'
import { 
  PieChartOutline,
  CubeOutline,
  LogInOutline,
  LogOutOutline,
  ClipboardOutline
} from '@vicons/ionicons5'

const router = useRouter()
const route = useRoute()

// 根据当前路由地址高亮对应菜单
const activeKey = computed(() => {
  return route.path.replace('/', '') || 'dashboard'
})

// 图标渲染辅助函数
function renderIcon(icon) {
  return () => h(NIcon, null, { default: () => h(icon) })
}

// 菜单配置
const menuOptions = [
  { label: '主页', key: 'dashboard', icon: renderIcon(PieChartOutline) },
  { label: '商品管理', key: 'product', icon: renderIcon(CubeOutline) },
  { label: '入库管理', key: 'inbound', icon: renderIcon(LogInOutline) },
  { label: '出库管理', key: 'outbound', icon: renderIcon(LogOutOutline) },
  { label: '库存盘点', key: 'stocktake', icon: renderIcon(ClipboardOutline) }
]

// 菜单点击事件：由于开启了多地址路由，直接 push
const handleMenuClick = (key) => {
  router.push(`/${key}`)
}
</script>

<style scoped>
.logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  white-space: nowrap;
}
.logo-text {
  color: white;
  font-size: 20px;
  font-weight: bold;
  letter-spacing: 1px;
}
.header {
  height: 64px;
  padding: 0 24px;
  display: flex;
  align-items: center;
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0,21,41,0.08); /* 增加一点阴影提升质感 */
  z-index: 10;
}

/* 优雅的切页动画 */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
}
.fade-slide-enter-from {
  opacity: 0;
  transform: translateX(10px);
}
.fade-slide-leave-to {
  opacity: 0;
  transform: translateX(-10px);
}
</style>
