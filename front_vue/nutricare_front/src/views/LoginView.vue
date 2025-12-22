<template>
  <div class="page">
    <Header />
    <main class="content">
      <h3>로그인</h3>
      <p>로그인 폼을 여기에 구현하세요. (TODO)</p>
    </main>
    <Footer />
  </div>
</template>

<script setup>
import Header from '@/components/common/Header.vue'
import Footer from '@/components/common/Footer.vue'
</script>

<style scoped>
.page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.content {
  flex: 1;
  padding: 24px 16px;
}
</style>
