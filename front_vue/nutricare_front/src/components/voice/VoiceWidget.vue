<template>
  <div class="voice-widget">
    <div v-if="store.isRecording || store.isProcessing || showResult" class="status-bubble">
      {{ store.resultText }}
    </div>

    <button 
      class="mic-btn" 
      :class="{ 'recording': store.isRecording, 'processing': store.isProcessing }"
      @mousedown="start" 
      @mouseup="stop"
      @touchstart.prevent="start" 
      @touchend.prevent="stop"
    >
      <span v-if="!store.isProcessing">🎙️</span>
      <span v-else>⏳</span>
    </button>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, onUnmounted } from 'vue';
import { useVoiceStore } from '@/stores/voice';
import { useRouter } from 'vue-router';

const store = useVoiceStore();
const router = useRouter();
const showResult = ref(false);

// New ref to manage keyboard toggle state
const isKeyboardActive = ref(false);

const start = () => {
  showResult.value = true;
  store.startRecording();
};

const stop = () => {
  store.stopRecording(router);
  // 3초 뒤에 결과 메시지 숨기기
  setTimeout(() => {
    if (!store.isRecording && !store.isProcessing) {
      showResult.value = false;
    }
  }, 3000);
};

// Keyboard event handler
const handleKeydown = (event) => {
  if (event.key === 'F4' && !isKeyboardActive.value) {
    event.preventDefault(); // Prevent default F4 behavior
    isKeyboardActive.value = true; // Mark as active to prevent repeat toggles on hold

    if (!store.isRecording) {
      start();
    } else {
      stop();
    }
  }
};

const handleKeyup = (event) => {
  if (event.key === 'F4') {
    isKeyboardActive.value = false; // Reset active state on key release
  }
};

// Add/remove event listeners
onMounted(() => {
  window.addEventListener('keydown', handleKeydown);
  window.addEventListener('keyup', handleKeyup);
});

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeydown);
  window.removeEventListener('keyup', handleKeyup);
});
</script>

<style scoped>
.voice-widget {
  position: fixed;
  bottom: 30px;
  right: 30px;
  z-index: 9999; /* 제일 위에 뜨도록 */
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 10px;
}

.mic-btn {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background-color: #4CAF50; /* 기본 초록색 */
  border: none;
  font-size: 30px;
  cursor: pointer;
  box-shadow: 0 4px 10px rgba(0,0,0,0.3);
  transition: all 0.3s ease;
  display: flex;
  justify-content: center;
  align-items: center;
}

/* 녹음 중일 때 애니메이션 (빨간색 + 쿵쾅거림) */
.mic-btn.recording {
  background-color: #ff4444;
  transform: scale(1.1);
  animation: pulse 1s infinite;
}

.mic-btn.processing {
  background-color: #FFC107; /* 처리중 노란색 */
}

.status-bubble {
  background-color: rgba(0, 0, 0, 0.8);
  color: white;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 14px;
  margin-bottom: 5px;
  white-space: nowrap;
}

@keyframes pulse {
  0% { box-shadow: 0 0 0 0 rgba(255, 68, 68, 0.7); }
  70% { box-shadow: 0 0 0 15px rgba(255, 68, 68, 0); }
  100% { box-shadow: 0 0 0 0 rgba(255, 68, 68, 0); }
}
</style>