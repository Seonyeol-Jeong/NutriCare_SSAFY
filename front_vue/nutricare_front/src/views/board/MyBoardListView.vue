<template>
  <div class="layout">
    <Header />
    <main class="content">
      <MyBoardList />
    </main>
    <Footer />
  </div>
</template>

<script setup>
import Header from '@/components/common/Header.vue';
import Footer from '@/components/common/Footer.vue';
import MyBoardList from '@/components/board/MyBoardList.vue';
</script>

<style scoped>
.layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.content {
  flex: 1;
  background: transparent;
}
</style>
