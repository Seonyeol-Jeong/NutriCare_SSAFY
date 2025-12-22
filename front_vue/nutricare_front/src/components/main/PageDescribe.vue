<template>
  <section class="page-describe container">
    <h1 class="brand-title">NutriCare</h1>
    <div class="hero">
      <div class="hero-visual">
        <div ref="trackRef" class="hero-track" role="list" aria-label="슬라이드 목록" @scroll="handleScroll"
          @wheel.prevent="handleWheel">
          <div class="hero-slide" v-for="(slide, idx) in slides" :key="slide.id" role="listitem">
            <div class="hero-image">
              <img
                :src="slide.image"
                :alt="slide.title"
                class="hero-photo"
                :class="{ active: idx === currentIndex }"
              />
            </div>
          </div>
        </div>

        <div class="hero-text-content">
          <transition name="fade" mode="out-in">
            <div :key="currentIndex" class="text-wrapper">
              <span class="slide-tag">{{ slides[currentIndex].title }}</span>
              <p class="slide-description">{{ slides[currentIndex].description }}</p>
            </div>
          </transition>
        </div>

        <div class="hero-dots" role="tablist" aria-label="슬라이드 선택">
          <button v-for="(slide, idx) in slides" :key="slide.id" class="dot" :class="{ active: idx === currentIndex }"
            type="button" :aria-label="`슬라이드 ${idx + 1}`" @click="goToSlide(idx)"></button>
        </div>
      </div>
    </div>


    <div class="cta">
      <button type="button" @click="goToAnalysis">분석 하기</button>
    </div>

  </section>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import IntroImg1 from '@/assets/IntroImg1.png'
import IntroImg2 from '@/assets/IntroImg2.png'
import IntroImg3 from '@/assets/IntroImg3.png'

const router = useRouter()

const slides = ref([
  { 
    id: 1, 
    title: 'AI 분석', 
    description: `당신의 건강 상태를
AI로 한눈에 분석하세요.`, 
    image: IntroImg1 
  },
  { 
    id: 2, 
    title: '맞춤 식단', 
    description: `나만을 위한 맞춤형 AI 식단으로
건강한 습관을 시작하세요.`, 
    image: IntroImg2 
  },
  { 
    id: 3, 
    title: '스마트 케어', 
    description: `데이터로 증명하는
스마트한 영양 관리를 경험하세요.`, 
    image: IntroImg3 
  },
])

const currentIndex = ref(0)
const trackRef = ref(null)
const wheelLock = ref(false)

function goToSlide(idx) {
  if (!trackRef.value) return
  const slideWidth = trackRef.value.clientWidth
  trackRef.value.scrollTo({
    left: slideWidth * idx,
    behavior: 'smooth',
  })
  currentIndex.value = idx
}

function handleScroll() {
  if (!trackRef.value) return
  const slideWidth = trackRef.value.clientWidth || 1
  const nextIndex = Math.round(trackRef.value.scrollLeft / slideWidth)
  currentIndex.value = Math.min(Math.max(nextIndex, 0), slides.value.length - 1)
}

function handleWheel(event) {
  if (wheelLock.value || !trackRef.value) return
  const delta = Math.abs(event.deltaY) > Math.abs(event.deltaX) ? event.deltaY : event.deltaX
  if (delta === 0) return

  const next = delta > 0 ? currentIndex.value + 1 : currentIndex.value - 1
  const clamped = Math.min(Math.max(next, 0), slides.value.length - 1)
  if (clamped === currentIndex.value) return

  wheelLock.value = true
  goToSlide(clamped)
  setTimeout(() => {
    wheelLock.value = false
  }, 400)
}

function goToAnalysis() {
  router.push({ name: 'analysisUpload' }).catch(() => { })
}
</script>

<style scoped>
.page-describe {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 40px;
  padding: 60px 20px 80px;
  background: #f8f5eb;
  box-sizing: border-box;
  border-radius: 20px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.05);
}

.brand-title {
  font-size: 42px;
  font-weight: 800;
  color: #5a45b0;
  margin-bottom: 0;
  letter-spacing: -1px;
}

.hero {
  width: 100%;
  max-width: 1000px;
  display: flex;
  justify-content: center;
}

.hero-visual {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
}

.hero-track {
  display: flex;
  width: 100%;
  overflow-x: auto;
  scroll-snap-type: x mandatory;
  scroll-behavior: smooth;
  padding: 0;
  overscroll-behavior-x: contain;
}

.hero-track::-webkit-scrollbar {
  display: none;
}

.hero-slide {
  flex: 0 0 100%;
  scroll-snap-align: center;
}

.hero-image {
  width: 100%;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 450px;
  background: white;
  border-radius: 24px;
  overflow: hidden;
  box-shadow: 0 15px 35px rgba(0,0,0,0.1);
}

.hero-photo {
  width: 100%;
  height: 100%;
  object-fit: cover;
  position: absolute;
  top: 0;
  left: 0;
  opacity: 0;
  transform: scale(1.1);
  transition: opacity 0.8s ease-out, transform 1.2s ease-out;
}

.hero-photo.active {
  opacity: 1; /* 오버레이가 없으므로 투명도 1로 복구 */
  transform: scale(1);
}

/* 텍스트 영역 스타일 */
.hero-text-content {
  min-height: 120px; /* 텍스트 변경 시 높이 흔들림 방지 */
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 20px;
  text-align: center;
}

.text-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.slide-tag {
  display: inline-block;
  padding: 6px 16px;
  background: rgba(90, 69, 176, 0.1);
  color: #5a45b0;
  border-radius: 50px;
  font-size: 14px;
  font-weight: 700;
}

.slide-description {
  font-size: 28px;
  font-weight: 800;
  color: #2c3e50;
  line-height: 1.4;
  white-space: pre-line;
  margin: 0;
}

/* 페이드 애니메이션 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease, transform 0.3s ease;
}

.fade-enter-from {
  opacity: 0;
  transform: translateY(10px);
}

.fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

.hero-dots {
  display: flex;
  gap: 8px;
  margin-top: 10px;
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #dcd6f7;
  border: none;
  cursor: pointer;
  transition: all 0.3s ease;
  padding: 0;
}

.dot.active {
  width: 24px;
  border-radius: 10px;
  background: #5a45b0;
}

.cta {
  margin-top: 20px;
}

.cta button {
  padding: 16px 48px;
  background: #5a45b0;
  color: white;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  font-size: 18px;
  font-weight: 700;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(90, 69, 176, 0.3);
}

.cta button:hover {
  background: #4a389d;
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(90, 69, 176, 0.4);
}

@media (max-width: 768px) {
  .brand-title { font-size: 32px; }
  .slide-description { font-size: 24px; }
  .hero-image { min-height: 350px; }
  .cta button { padding: 14px 36px; font-size: 16px; }
}
</style>
