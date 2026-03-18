<template>
  <div class="page-container">
    <n-card title="入库管理" :bordered="false" class="page-card">
      <template #header-extra>
        <n-button type="primary">新增入库单</n-button>
      </template>
      <n-p>这里将用来实现：增改查商品入库记录。</n-p>
    </n-card>
  </div>
</template>

<style scoped>
.page-container { height: 100%; }
.page-card {
  border-radius: 8px;
  box-shadow: 0 1px 2px -2px rgba(0,0,0,0.08), 0 3px 6px 0 rgba(0,0,0,0.06), 0 5px 12px 4px rgba(0,0,0,0.04);
}
</style>
