<template>
  <div class="content">
    <AnalysisUpload />
  </div>
</template>

<script setup>
import AnalysisUpload from '@/components/analysis/AnalysisUpload.vue'
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
