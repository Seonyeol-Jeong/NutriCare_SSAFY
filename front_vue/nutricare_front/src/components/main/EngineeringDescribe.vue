<template>
  <section class="engineering-describe container">
    <div class="text-hero">
      <div class="hero-mark">
        <img :src="logoImg" alt="NutriCare Logo" class="hero-logo" />
      </div>
      <div class="hero-copy">
        <h2 class="headline">핵심 기술로 AI·분석을 결합한 맞춤 헬스케어</h2>
        <p class="body">
          NutriCare는 최신 AI 모델과 데이터 기반 분석을 결합해 개인 맞춤 건강 및 피부 솔루션을 제공합니다.
          식습관, 라이프스타일, 피부 상태를 통합 분석하여 사용자에게 최적화된 인사이트를 전달합니다.
        </p>
        <button class="primary" type="button" @click="goToTech">핵심 기술 소개</button>
      </div>
    </div>

    <div class="feature-grid">
      <article
        v-for="feature in features"
        :key="feature.id"
        class="feature-card"
      >
        <div class="feature-visual">
          <img :src="feature.image" :alt="feature.name" class="feature-img" />
        </div>
        <div class="feature-body">
          <p class="feature-name">{{ feature.name }}</p>
          <p class="feature-desc">{{ feature.desc }}</p>
        </div>
      </article>
    </div>
  </section>
</template>

<script setup>
import { useRouter } from 'vue-router'
import logoImg from '@/assets/Logo.png'
import joonhoImg from '@/assets/jonnho.png'
import seonyeolImg from '@/assets/seonyeol.png'

const router = useRouter()

const features = [
  {
    id: 1,
    title: '장준호',
    name: '장준호',
    image: joonhoImg,
    desc: '주요 기술 소개',
  },
  {
    id: 2,
    title: '정선열',
    name: '정선열',
    image: seonyeolImg,
    desc: '주요 기술 소개',
  },
]

function goToTech() {
  // TODO: 실제 기술 상세 라우트 이름으로 교체
  router.push({ name: 'engineeringDescribe' }).catch(() => {})
}
</script>

<style scoped>
.engineering-describe {
  display: flex;
  flex-direction: column;
  gap: 28px;
  padding: 32px 16px 48px;
  background: #f8f5eb;
  box-sizing: border-box;
}

.text-hero {
  display: grid;
  grid-template-columns: 240px 1fr;
  gap: 24px;
  align-items: center;
  background: #fff;
  border: 1px solid #e0e0e0;
  padding: 24px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
}

.hero-mark {
  width: 200px;
  height: 200px;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
  box-sizing: border-box;
  overflow: hidden;
}

.hero-logo {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}

.hero-copy {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.eyebrow {
  margin: 0;
  color: #7a7a7a;
  font-size: 13px;
}

.headline {
  margin: 0;
  font-size: 22px;
  color: #2d2d2d;
  line-height: 1.35;
}

.body {
  margin: 0;
  color: #4b4b4b;
  line-height: 1.6;
}

.primary {
  align-self: flex-start;
  padding: 10px 18px;
  background: #ece4ff;
  color: #5a45b0;
  border: 1px solid #d7cfff;
  border-radius: 6px;
  font-weight: 600;
  cursor: pointer;
}

.primary:hover {
  background: #e2d8ff;
}

.feature-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 16px;
}

.feature-card {
  background: #fff;
  border: 1px solid #e0e0e0;
  display: grid;
  grid-template-columns: 1fr;
  grid-template-rows: 200px auto;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
  overflow: hidden;
}

.feature-visual {
  background: #fff;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
}

.feature-img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  display: block;
}

.feature-body {
  padding: 14px 16px 16px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.feature-name {
  margin: 0;
  font-weight: 700;
  color: #2d2d2d;
}

.feature-desc {
  margin: 0;
  color: #4b4b4b;
  font-size: 14px;
}

@media (max-width: 768px) {
  .text-hero {
    grid-template-columns: 1fr;
    justify-items: center;
  }

  .hero-copy {
    width: 100%;
  }

  .hero-mark {
    width: 160px;
    height: 160px;
  }
}
</style>