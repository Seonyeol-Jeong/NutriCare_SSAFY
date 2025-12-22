<template>
  <div class="content">
    <AnalysisList />
  </div>
</template>

<script setup>
import AnalysisList from '@/components/analysis/AnalysisList.vue'
</script>

<style scoped>
.content {
  flex: 1;
  display: flex;
  justify-content: center;
  padding: 32px 16px;
  background: #f8f5eb;
}
</style>
