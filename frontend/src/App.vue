<template>
  <n-config-provider :theme-overrides="themeOverrides">
    <n-message-provider>
      <n-dialog-provider>
        
        <router-view />

      </n-dialog-provider>
    </n-message-provider>
    <n-global-style />
  </n-config-provider>
</template>

<script setup>
// 定制全局主题颜色，提升系统颜值
const themeOverrides = {
  common: {
    primaryColor: '#0052D9', // 使用经典的科技蓝/可信蓝
    primaryColorHover: '#266FE8',
    primaryColorPressed: '#003CAB',
    borderRadius: '6px' // 增加全局圆角
  }
}
</script>