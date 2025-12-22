<template>
  <section class="page-describe container">
    <h2 class="mb-4">NutriCare</h2>
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
  { id: 1, title: '건강한 인물 이미지 1', image: IntroImg1 },
  { id: 2, title: '건강한 인물 이미지 2', image: IntroImg2 },
  { id: 3, title: '건강한 인물 이미지 3', image: IntroImg3 },
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
  gap: 32px;
  padding: 32px 16px 48px;
  background: #f8f5eb;
  box-sizing: border-box;
  border-radius: 10px; /* Soften the corners */
  box-shadow: 0 0 20px rgba(0, 0, 0, 0.1); /* Soft, blurred shadow */
}

.hero {
  width: 100%;
  max-width: 1200px;
  display: flex;
  justify-content: center;
}

.hero-visual {
  width: 100%;
  border: none;
  background: transparent;
  padding: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.hero-track {
  display: flex;
  width: 100%;
  gap: 12px;
  overflow-x: auto;
  scroll-snap-type: x mandatory;
  scroll-behavior: smooth;
  padding: 0;
  overscroll-behavior-x: contain;
}

.hero-track::-webkit-scrollbar {
  height: 10px;
}

.hero-track::-webkit-scrollbar-track {
  background: #f1f1f1;
}

.hero-track::-webkit-scrollbar-thumb {
  background: #c8c8c8;
  border-radius: 999px;
}

.hero-slide {
  flex: 0 0 100%;
  scroll-snap-align: center;
}

.hero-image {
  width: 100%;
  position: relative;
  isolation: isolate;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 360px;
}
.hero-image p {
  position: relative;
  z-index: 1;
  color: #fff;
  font-size: 18px;
  font-weight: 700;
  text-align: center;
}

.hero-photo {
  width: 100%;
  height: auto;
  object-fit: contain;
  display: block;
  opacity: 0;
  transition: opacity 0.5s ease-in-out;
  margin: 0;
  padding: 0;
  mask-image: radial-gradient(circle, black 80%, rgba(0, 0, 0, 0) 100%);
  -webkit-mask-image: radial-gradient(circle, black 80%, rgba(0, 0, 0, 0) 100%); /* For Webkit browsers */
  mask-mode: alpha; /* Standard for mask-image */
}

.hero-photo.active {
  opacity: 1;
}

.hero-dots {
  display: none;
  gap: 12px;
}

.dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  border: 1px solid #bdbdbd;
  background: #efefef;
  cursor: pointer;
}

.dot.active {
  background: #777;
  border-color: #777;
}

.bullet-list {
  width: 100%;
  max-width: 720px;
  background: #fff;
  border: 1px solid #e0e0e0;
  padding: 20px 24px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
}

.bullet-list ol {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding-left: 20px;
  margin: 0 0 16px;
  color: #333;
  line-height: 1.5;
}

.cta {
  display: flex;
  justify-content: center;
}

.cta button {
  padding: 10px 20px;
  background: #ece4ff;
  color: #5a45b0;
  border: 1px solid #d7cfff;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
}

.cta button:hover {
  background: #e2d8ff;
}

@media (max-width: 768px) {
  .hero-image {
    max-height: none;
    min-height: 280px;
  }

  .hero-track {
    gap: 8px;
    grid-auto-columns: unset;
  }
}
</style>
