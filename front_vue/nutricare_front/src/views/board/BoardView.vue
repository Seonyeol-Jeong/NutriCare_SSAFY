<template>
  <div class="layout">
    <Header />
    <main class="content">
      <RouterView />
    </main>
    <Footer />
  </div>
</template>

<script setup>
import Footer from '@/components/common/Footer.vue'
import Header from '@/components/common/Header.vue'
</script>

<style scoped>
.layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.content {
  flex: 1;
  background: #f8f5eb;
}
</style>
