<template>
  <div class="page-container">
    <n-card title="仪表盘" :bordered="false" class="page-card">
      <n-p>这里是统计与看板区域。可以放置漂亮的 Echarts 图表、今日入库/出库数字卡片等。</n-p>
    </n-card>
  </div>
</template>

<style scoped>
.page-container {
  height: 100%;
}
.page-card {
  border-radius: 8px; /* 增加大圆角，提升颜值 */
  box-shadow: 0 1px 2px -2px rgba(0, 0, 0, 0.08), 0 3px 6px 0 rgba(0, 0, 0, 0.06), 0 5px 12px 4px rgba(0, 0, 0, 0.04);
}
</style>
