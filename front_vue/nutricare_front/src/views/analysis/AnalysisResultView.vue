<template>
  <div class="content">
    <AnalysisResult />
  </div>
</template>

<script setup>
import AnalysisResult from '@/components/analysis/AnalysisResult.vue'
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
