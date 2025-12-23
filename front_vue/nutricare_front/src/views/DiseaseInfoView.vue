<template>
  <div class="layout">
    <Header />
    <main class="main-content">
      <div class="disease-info-container">
        <header class="page-header">
          <h2>피부 질환 백과</h2>
          <p>주요 피부 질환에 대한 정확한 정보를 확인하세요.</p>
        </header>

        <div class="tabs-wrapper">
          <div class="tabs">
            <button 
              v-for="disease in diseases" 
              :key="disease.id"
              class="tab-btn"
              :class="{ active: currentTab === disease.id }"
              @click="currentTab = disease.id"
            >
              {{ disease.name }}
            </button>
          </div>
        </div>

        <div class="content-area">
          <Transition name="fade" mode="out-in">
            <section :key="currentTab" class="disease-content">
              <div class="disease-header">
                <h3>{{ currentData.name }}</h3>
                <span class="eng-name">{{ currentData.engName }}</span>
              </div>

              <!-- 대표 이미지 -->
              <div class="disease-image-container" v-if="currentData.image">
                <img :src="currentData.image" :alt="currentData.name" class="disease-img" />
              </div>

              <article class="info-section">
                <h4>정의</h4>
                <p>{{ currentData.definition }}</p>
              </article>

              <article class="info-section">
                <h4>원인</h4>
                <p>{{ currentData.causes }}</p>
              </article>

              <article class="info-section">
                <h4>증상</h4>
                <p>{{ currentData.symptoms }}</p>
              </article>

              <article class="info-section">
                <h4>진단</h4>
                <p>{{ currentData.diagnosis }}</p>
              </article>

              <article class="info-section">
                <h4>치료</h4>
                <p v-html="currentData.treatment"></p>
              </article>

              <article class="info-section highlight">
                <h4>주의사항 및 경과</h4>
                <p>{{ currentData.caution }}</p>
              </article>
            </section>
          </Transition>
        </div>
      </div>
    </main>
    <Footer />
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import Header from '@/components/common/Header.vue'
import Footer from '@/components/common/Footer.vue'

// 질환별 이미지 import
import AtopyImg from '@/assets/아토피.jpg'
import PsoriasisImg from '@/assets/건선.jpg'
import AcneImg from '@/assets/여드름.jpg'
import RosaceaImg from '@/assets/주사.jpg'
import SeborrheaImg from '@/assets/지루.jpg'

const currentTab = ref('atopy')

const diseases = [
  { id: 'atopy', name: '아토피' },
  { id: 'psoriasis', name: '건선' },
  { id: 'acne', name: '여드름' },
  { id: 'rosacea', name: '주사(딸기코)' },
  { id: 'seborrhea', name: '지루성 피부염' },
]

const diseaseData = {
  atopy: {
    name: '아토피성 피부염',
    engName: 'Atopic Dermatitis',
    image: AtopyImg,
    definition: '아토피성 피부염은 심한 가려움증을 동반하고 만성적으로 재발하는 피부 습진 질환입니다. 천식, 알레르기 비염, 만성 두드러기와 함께 대표적인 알레르기 질환 중 하나입니다. 소아, 청소년, 성인에 이르기까지 호전과 악화를 반복하며 만성적인 경과를 보입니다.',
    causes: '유전적인 소인(가족력), 환경적인 요인(공해, 인스턴트 식품), 면역학적 이상(IgE 증가), 피부 보호막의 이상 등 여러 원인이 복합적으로 작용합니다. 최근에는 실내외 공해나 식품 첨가물 등 환경 요인의 중요성이 강조되고 있습니다.',
    symptoms: '가장 큰 특징은 심한 가려움증과 외부 자극에 대한 민감한 반응입니다. 가려움증은 저녁에 심해지며, 긁어서 습진이 생기는 악순환이 반복됩니다. 연령에 따라 발생 부위가 다르며(유아기: 얼굴/몸통, 소아기: 팔다리 접히는 곳, 성인기: 얼굴/목/손발 등), 피부 건조와 태선화가 나타납니다.',
    diagnosis: '특정 검사 하나로 진단하기보다 환자의 특징적인 증상(가려움증, 특징적 병변 분포, 가족력, 만성 재발 경과)을 토대로 진단합니다. 혈액 검사(IgE), 피부 단자 검사, 음식물 알레르기 검사 등을 보조적으로 활용합니다.',
    treatment: '치료 원칙은 원인 유발 인자를 제거하고, 적절한 목욕 및 보습으로 피부를 청결하고튼튼하게 유지하는 것입니다.<br>약물 요법으로는 국소 스테로이드제, 면역조절제, 항히스타민제 등을 사용하며, 증상이 심할 경우 광선 치료나 면역 억제제 등의 전문 치료를 시행합니다.',
    caution: '호전과 악화를 반복하는 만성 질환이므로 꾸준한 관리가 필요합니다. 성장하면서 완화되기도 하지만, 천식이나 비염으로 이어지는 알레르기 행진을 보일 수도 있습니다. 피부 감염증이나 안구 증상 등의 합병증에 주의해야 합니다.'
  },
  psoriasis: {
    name: '건선',
    engName: 'Psoriasis',
    image: PsoriasisImg,
    definition: '피부에 경계가 뚜렷하며 다양한 크기의 은백색 비늘(인설)로 덮여 있는 홍반성 구진 및 판이 형성되는 만성 염증성 피부 질환입니다. 조직학적으로는 상피의 과다 증식이 특징입니다.',
    causes: '명확한 원인은 밝혀지지 않았으나 유전적 요인을 기반으로 개인의 생활 및 환경적 요인(피부 외상, 감염, 스트레스, 차고 건조한 기후, 약물 등)이 유발 인자로 작용하여 면역학적 이상을 일으키는 것으로 알려져 있습니다.',
    symptoms: '주로 팔꿈치, 무릎, 엉치뼈, 두피 등 자극을 많이 받는 부위에 대칭적으로 발생합니다. 붉은 좁쌀 같은 발진이 커지면서 합쳐지고, 그 위에 하얀 비늘이 겹겹이 쌓입니다. 인설을 제거하면 점상 출혈이 나타나는 것이 특징이며, 손발톱 병변이 동반되기도 합니다.',
    diagnosis: '특징적인 피부 변화와 신체 검진을 통해 진단하며, 필요 시 피부 병리 조직 검사를 시행하여 확진합니다.',
    treatment: '경증에는 국소 치료(연고 도포), 중증에는 전신 치료(약물 복용)나 광선 치료(자외선 조사)를 시행합니다. 최근에는 단파장 자외선 B 치료나 엑시머 레이저 등이 많이 사용됩니다.',
    caution: '피부 마찰이나 상처를 입지 않도록 주의해야 하며(때 밀기 금지), 목감기 등 감염을 피해야 합니다. 특히 겨울철 건조한 기후에 악화되기 쉬우므로 보습에 신경 써야 합니다. 재발을 완전히 막기는 어렵지만 꾸준한 관리로 좋은 상태를 유지할 수 있습니다.'
  },
  acne: {
    name: '여드름',
    engName: 'Acne',
    image: AcneImg,
    definition: '털을 만드는 모낭에 붙어 있는 피지선에 발생하는 만성 염증성 질환입니다. 피지가 밖으로 배출되지 못하고 모낭 주위에 갇히면 염증을 일으키는 박테리아가 번식하여 발생합니다.',
    causes: '호르몬 변화(사춘기 안드로겐, 성인 여성 프로게스테론), 세균 감염, 유전적 요인, 스트레스, 수면 부족, 유분이 많은 화장품 등이 원인입니다.',
    symptoms: '면포(블랙헤드/화이트헤드), 붉은 구진, 곪는 농포가 나타나며 심하면 결절이나 낭종이 생깁니다. 얼굴, 목, 등, 가슴 등 피지선이 발달한 곳에 주로 발생합니다.',
    diagnosis: '육안으로 증상 부위를 관찰하여 진단하며, 심한 경우 호르몬 검사를 시행하여 원인 질환을 감별하기도 합니다.',
    treatment: '바르는 약, 먹는 약, 외과적 치료(압출, 스케일링, 레이저 등)를 단독 또는 병행합니다. 막힌 모낭을 열어 피지 배출을 돕고 세균 증식과 염증을 억제하는 것이 원리입니다.',
    caution: '초기에 치료하지 않으면 붉은 자국, 색소 침착, 영구적인 흉터가 남을 수 있습니다. 성인 여드름은 치료 반응이 느리고 유병 기간이 길 수 있으므로 꾸준한 관리가 필요합니다.'
  },
  rosacea: {
    name: '주사 (딸기코)',
    engName: 'Rosacea',
    image: RosaceaImg,
    definition: '혈관이 자극에 의해 쉽게 늘어나고 잘 오므라들지 않아, 얼굴 중앙 부위에 지속적인 홍반, 구진, 고름집, 혈관 확장이 나타나는 만성 질환입니다.',
    causes: '확실하지 않으나 유전적 요인(체질), 만성적인 햇빛 노출, 내분비 이상, 음주, 카페인 과다 섭취, 헬리코박터 감염 등이 원인으로 꼽힙니다.',
    symptoms: '초기에는 간헐적인 안면 홍조와 화끈거림이 나타나다가, 점차 지속적인 홍반, 모세혈관 확장, 염증성 구진이 얼굴 중심부에 발생합니다. 심하면 코의 형태가 변형(딸기코)되기도 합니다.',
    diagnosis: '임상 양상을 통해 진단하며, 여드름이나 지루성 피부염과 구분해야 합니다. 필요 시 조직 검사를 시행합니다.',
    treatment: '국소 연고(항생제 등)와 전신 약물 요법(항생제, 피지 조절제)을 사용하며, 확장된 혈관에는 레이저 치료(혈관 레이저, IPL)가 효과적입니다.',
    caution: '혈관 확장을 유발하는 과도한 열(사우나), 한랭, 자외선, 음주, 맵고 뜨거운 음식을 피해야 합니다. 눈의 합병증이 동반될 수 있으므로 주의가 필요합니다.'
  },
  seborrhea: {
    name: '지루성 피부염',
    engName: 'Seborrheic Dermatitis',
    image: SeborrheaImg,
    definition: '피지 분비가 많은 부위(머리, 이마, 겨드랑이 등)에 발생하는 만성 염증성 피부 질환으로, 홍반과 가느다란 인설(비듬)이 특징입니다.',
    causes: '피지선의 과도한 활동, 호르몬, 곰팡이균(피티로스포륨), 신경 전달 물질 이상, 스트레스, 온도/습도 변화 등 다양한 요인이 추측됩니다.',
    symptoms: '두피의 비듬, 얼굴(눈썹, 코 주위)의 붉은 발진과 하얀 각질, 겨드랑이나 가슴의 홍반 등이 나타나며 가려움증을 동반합니다. 아침에 기름기가 많은데도 세안 후 각질이 일어나는 것이 특징입니다.',
    diagnosis: '병변의 모양과 분포를 통해 진단하며, 건선과의 감별이 필요할 수 있습니다.',
    treatment: '완치보다는 증상 완화에 중점을 둡니다. 두피에는 약용 샴푸(항진균제 등)를 사용하고, 얼굴에는 저자극성 보습제나 약한 스테로이드제를 사용합니다.',
    caution: '기름진 음식, 음주, 스트레스 등을 피하고 머리를 손톱으로 긁지 않도록 해야 합니다. 꾸준한 생활 습관 관리가 악화 방지에 중요합니다.'
  }
}

const currentData = computed(() => diseaseData[currentTab.value])
</script>

<style scoped>
.layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f8f5eb; /* 배경색 통일 */
}

.main-content {
  flex: 1;
}

.disease-info-container {
  max-width: 900px;
  margin: 40px auto;
  padding: 0 20px 60px;
  font-family: 'Pretendard', sans-serif;
  color: #333;
}

.page-header {
  text-align: center;
  margin-bottom: 40px;
}

.page-header h2 {
  font-size: 32px;
  font-weight: 800;
  margin-bottom: 10px;
  color: #333;
}

.page-header p {
  color: #666;
  font-size: 16px;
}

/* Tabs */
.tabs-wrapper {
  margin-bottom: 40px;
  display: flex;
  justify-content: center;
}

.tabs {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: center;
  background: #f5f5f5;
  padding: 6px;
  border-radius: 50px;
}

.tab-btn {
  border: none;
  background: transparent;
  padding: 10px 24px;
  border-radius: 40px;
  font-size: 16px;
  font-weight: 600;
  color: #666;
  cursor: pointer;
  transition: all 0.2s ease;
}

.tab-btn:hover {
  color: #333;
  background: rgba(255,255,255,0.5);
}

.tab-btn.active {
  background: #6b55c7;
  color: white;
  box-shadow: 0 4px 10px rgba(107, 85, 199, 0.3);
}

/* Content */
.disease-content {
  background: white;
  border-radius: 20px;
  padding: 40px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.05);
  border: 1px solid #eee;
}

.disease-header {
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 2px solid #f0f0f0;
}

.disease-header h3 {
  font-size: 28px;
  font-weight: 800;
  color: #333;
  margin: 0 0 4px 0;
}

.eng-name {
  font-size: 14px;
  color: #888;
  font-weight: 500;
  letter-spacing: 0.5px;
}

/* Image Style */
.disease-image-container {
  text-align: center;
  margin-bottom: 32px;
}

.disease-img {
  max-width: 100%;
  max-height: 300px;
  border-radius: 16px;
  box-shadow: 0 4px 15px rgba(0,0,0,0.1);
  object-fit: contain;
}

.info-section {
  margin-bottom: 24px;
}

.info-section h4 {
  font-size: 18px;
  font-weight: 700;
  color: #6b55c7;
  margin-bottom: 8px;
}

.info-section p {
  font-size: 16px;
  line-height: 1.7;
  color: #444;
  margin: 0;
}

.info-section.highlight {
  background: #f8f5ff;
  padding: 24px;
  border-radius: 12px;
  border-left: 4px solid #6b55c7;
  margin-top: 32px;
}

/* Transition */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

@media (max-width: 600px) {
  .tabs {
    border-radius: 12px;
  }
  .tab-btn {
    flex: 1 1 auto;
    text-align: center;
    font-size: 14px;
    padding: 8px 12px;
  }
  .disease-content {
    padding: 24px;
  }
}
</style>