<template>
  <RouterView />
  <VoiceWidget/>
</template>

<script setup>
import VoiceWidget from './components/voice/VoiceWidget.vue';

</script>

<style>
body {
  font-family: 'Noto Sans KR', sans-serif;
}
</style>

<style scoped>
html, body {
  margin: 0;
  padding: 0;
}
</style>
