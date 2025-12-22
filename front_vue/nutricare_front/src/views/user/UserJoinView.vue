<template>
  <div class="page">
    <Header />
    <main class="content">
      <UserJoinForm />
    </main>
    <Footer />
  </div>
</template>

<script setup>
import Header from '@/components/common/Header.vue'
import Footer from '@/components/common/Footer.vue'
import UserJoinForm from '@/components/user/UserJoinForm.vue'
</script>

<style scoped>
.page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f8f5eb;
}

.content {
  flex: 1;
  display: flex;
  justify-content: center;
  padding: 32px 16px;
}
</style>
