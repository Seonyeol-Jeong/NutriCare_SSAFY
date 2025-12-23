<template>
  <section class="engineering-container">
    <div class="tech-hero">
      <div class="hero-content">
        <div class="logo-wrapper">
          <img :src="logoImg" alt="NutriCare Logo" class="brand-logo" />
        </div>
        <h1 class="main-title">
          <span class="highlight">AI & Data</span> 기반<br />
          맞춤형 헬스케어 솔루션
        </h1>
        <p class="sub-text">
          NutriCare는 최신 딥러닝 모델과 빅데이터 분석을 통해<br class="desktop-br" />
          당신만의 건강과 피부에 최적화된 인사이트를 제공합니다.
        </p>
        <button class="hero-readme-btn" @click="goToReadme">
          ReadMe
        </button>
      </div>
    </div>

    <div class="team-section">
      <h2 class="section-title">Core Tech & Team</h2>
      <div class="feature-grid">
        <article
          v-for="feature in features"
          :key="feature.id"
          class="tech-card"
        >
          <div class="image-frame">
            <img :src="feature.image" :alt="feature.name" class="profile-img" />
          </div>
          <div class="card-info">
            <h3 class="name">{{ feature.name }}</h3>
            <p class="role">Lead Engineer</p>
            <p class="desc">{{ feature.desc }}</p>
          </div>
        </article>
      </div>
    </div>
  </section>
</template>

<script setup>
import logoImg from '@/assets/Logo.png'
import joonhoImg from '@/assets/jonnho.png'
import seonyeolImg from '@/assets/seonyeol.png'

const projectReadmeUrl = 'https://github.com/Seonyeol-Jeong/NutriCare_SSAFY?tab=readme-ov-file' // 여기에 실제 프로젝트 ReadMe 링크를 입력하세요

function goToReadme() {
  window.open(projectReadmeUrl, '_blank')
}

const features = [
  {
    id: 1,
    name: '장준호',
    image: joonhoImg,
    desc: 'SPACE.',
  },
  {
    id: 2,
    name: '정선열',
    image: seonyeolImg,
    desc: 'SPACE.',
  },
]
</script>

<style scoped>
.engineering-container {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 60px 20px;
}

/* Hero Section */
.tech-hero {
  text-align: center;
  max-width: 900px;
  margin: 0 auto 80px;
  background: linear-gradient(135deg, #fdfbf7 0%, #fff 100%);
  border-radius: 30px;
  padding: 60px 20px;
  box-shadow: 0 20px 40px rgba(0,0,0,0.03);
}

.logo-wrapper {
  margin-bottom: 24px;
}

.brand-logo {
  height: 60px;
  width: auto;
}

.main-title {
  font-size: 36px;
  font-weight: 800;
  color: #2c3e50;
  line-height: 1.3;
  margin-bottom: 16px;
}

.highlight {
  color: #6b55c7;
  position: relative;
  display: inline-block;
}

.highlight::after {
  content: '';
  position: absolute;
  bottom: 2px;
  left: 0;
  width: 100%;
  height: 8px;
  background-color: rgba(107, 85, 199, 0.15);
  z-index: -1;
  border-radius: 4px;
}

.sub-text {
  font-size: 18px;
  color: #666;
  line-height: 1.6;
  margin-bottom: 30px;
}

.hero-readme-btn {
  background-color: #6b55c7;
  color: white;
  border: none;
  padding: 14px 36px;
  font-size: 16px;
  font-weight: 700;
  border-radius: 50px;
  cursor: pointer;
  box-shadow: 0 10px 20px rgba(107, 85, 199, 0.2);
  transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.hero-readme-btn:hover {
  transform: translateY(-5px) scale(1.05);
  box-shadow: 0 15px 30px rgba(107, 85, 199, 0.3);
  background-color: #7c66d9;
}

.hero-readme-btn:active {
  transform: translateY(-2px) scale(0.98);
}

/* Team Section */
.team-section {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.section-title {
  font-size: 24px;
  font-weight: 700;
  color: #333;
  margin-bottom: 40px;
  position: relative;
  
  /* Added for visibility */
  background: rgba(255, 255, 255, 0.9);
  padding: 15px 40px 20px;
  border-radius: 50px;
  box-shadow: 0 5px 15px rgba(0,0,0,0.05);
  backdrop-filter: blur(5px);
}

.section-title::after {
  content: '';
  position: absolute;
  bottom: 10px;
  left: 50%;
  transform: translateX(-50%);
  width: 40px;
  height: 3px;
  background-color: #6b55c7;
  border-radius: 2px;
}

.feature-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 30px;
  width: 100%;
  max-width: 900px;
}

.tech-card {
  background: white;
  border-radius: 24px;
  padding: 40px 30px;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  box-shadow: 0 10px 30px rgba(0,0,0,0.05);
  transition: all 0.3s ease;
  border: 1px solid rgba(0,0,0,0.02);
}

.tech-card:hover {
  transform: translateY(-10px);
  box-shadow: 0 20px 40px rgba(107, 85, 199, 0.15);
}

.image-frame {
  width: 140px;
  height: 140px;
  border-radius: 50%; /* 원형 이미지 */
  overflow: hidden;
  margin-bottom: 24px;
  border: 4px solid #f8f9fa;
  box-shadow: 0 8px 20px rgba(0,0,0,0.08);
}

.profile-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.tech-card:hover .profile-img {
  transform: scale(1.1);
}

.card-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.name {
  font-size: 22px;
  font-weight: 800;
  color: #2d3748;
  margin: 0;
}

.role {
  font-size: 14px;
  font-weight: 600;
  color: #6b55c7;
  text-transform: uppercase;
  letter-spacing: 1px;
  margin-bottom: 8px;
}

.desc {
  font-size: 15px;
  color: #718096;
  line-height: 1.5;
  word-break: keep-all;
  margin-bottom: 20px;
}

.readme-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background-color: #f3f0ff;
  color: #6b55c7;
  text-decoration: none;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 700;
  transition: all 0.2s;
  border: 1px solid #eaddff;
}

.readme-btn:hover {
  background-color: #6b55c7;
  color: white;
  transform: scale(1.05);
}

@media (max-width: 768px) {
  .main-title {
    font-size: 28px;
  }
  .sub-text {
    font-size: 16px;
  }
  .desktop-br {
    display: none;
  }
  .tech-hero {
    padding: 40px 20px;
  }
  .hero-readme-btn {
    padding: 12px 28px;
    font-size: 14px;
  }
}
</style>