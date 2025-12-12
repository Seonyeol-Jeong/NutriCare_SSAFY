<template>
  <div class="layout">
    <div class="background-image"></div>
    <Header />
    <main class="content">
      <RouterView />
    </main>
    <Footer />
  </div>
</template>

<script setup>
import Header from '@/components/common/Header.vue'
import Footer from '@/components/common/Footer.vue'
import BackgroundImgUrl from '@/assets/BackgroundImg.png'

const bgImageStyle = `url(${BackgroundImgUrl})`
</script>

<style scoped>
.background-image {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  z-index: -1;
  background-image: v-bind(bgImageStyle);
  background-size: cover;
  background-position: center;
  filter: blur(8px);
  transform: scale(1.1); /* Prevents blurred edges from showing */
}

.layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.content {
  flex: 1;
  background: transparent; /* Make content background transparent */
}
</style>
