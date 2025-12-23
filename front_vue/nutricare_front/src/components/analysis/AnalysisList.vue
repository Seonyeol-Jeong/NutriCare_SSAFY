<template>
  <section class="analysis-calendar-container">
    <header class="calendar-header">
      <div class="header-left">
        <h2 class="current-date">{{ year }}년 {{ month }}월</h2>
      </div>
      
      <div class="nav-buttons">
        <button @click="prevMonth" class="nav-btn"><i class="bi bi-chevron-left">&lt;</i></button>
        <button @click="goToday" class="nav-btn today-btn">오늘</button>
        <button @click="nextMonth" class="nav-btn"><i class="bi bi-chevron-right">&gt;</i></button>
      </div>
    </header>

    <div class="timeline-body">
      <!-- 타임라인 스크롤 영역 -->
      <div 
        class="timeline-scroll-area" 
        ref="scrollContainer"
        @wheel.prevent="handleWheel"
      >
        <div 
          v-for="(day, index) in daysInCurrentMonth" 
          :key="day.date.toISOString()" 
          class="timeline-day"
          :class="{ 
            'today': day.isToday,
            'has-event': day.items.length > 0
          }"
          @click="handleDateClick(day)"
        >
          <!-- 타임라인 노드 영역 -->
          <div 
            class="timeline-node-area"
            @click.stop="goToDateView(day)"
          >
            <span class="weekday-label" :class="{
              'sunday': day.date.getDay() === 0,
              'saturday': day.date.getDay() === 6
            }">{{ getDayName(day.date) }}</span>
            
            <div class="timeline-line-container">
              <div class="line-segment left" :class="{ 'start': index === 0 }"></div>
              <div class="node-circle">
                <span class="day-number">{{ day.date.getDate() }}</span>
              </div>
              <div class="line-segment right" :class="{ 'end': index === daysInCurrentMonth.length - 1 }"></div>
            </div>
          </div>

          <!-- 컨텐츠 영역 -->
          <div class="timeline-content">
            <div 
              v-for="item in getVisibleItems(day)" 
              :key="item.id" 
              class="event-thumbnail"
              @click.stop="openDetail(item.id)"
            >
              <img :src="item.thumbnail" alt="분석" />
            </div>
            
            <!-- 더보기 버튼 (접혀있을 때) -->
            <div 
              v-if="!isExpanded(day) && day.items.length > 3" 
              class="more-btn"
              @click.stop="toggleExpand(day)"
            >
              <span class="more-count">+{{ day.items.length - 3 }}</span>
              <i class="bi bi-chevron-down"></i>
            </div>

            <!-- 접기 버튼 (펼쳐져있을 때) -->
            <div 
              v-if="isExpanded(day) && day.items.length > 3" 
              class="fold-btn"
              @click.stop="toggleExpand(day)"
            >
              <i class="bi bi-chevron-up"></i>
            </div>

            <div v-if="day.items.length === 0" class="empty-slot"></div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 데이터 없음 안내 (월 전체에 데이터가 없을 경우) -->
    <div v-if="!isLoading && currentMonthItemsCount === 0" class="empty-week-notice">
      <p>이번 달에는 분석 기록이 없습니다.</p>
      <button class="upload-btn" @click="goToUpload">분석 하러 가기</button>
    </div>

    <!-- 상세 보기 모달 -->
    <AnalysisDetailModal 
      v-if="selectedPhotoId" 
      :photoId="selectedPhotoId" 
      @close="closeDetail" 
    />
  </section>
</template>

<script setup>
import { computed, ref, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useAnalysisStore } from '@/stores/analysis'
import AnalysisDetailModal from './AnalysisDetailModal.vue'

const router = useRouter()
const store = useAnalysisStore()
const scrollContainer = ref(null)

const isLoading = ref(false)
const selectedPhotoId = ref(null)
const currentDate = ref(new Date())
const expandedDays = ref(new Set()) // 확장된 날짜 키 저장

const weekDayNames = ['일', '월', '화', '수', '목', '금', '토']

onMounted(async () => {
  isLoading.value = true
  try {
    await store.fetchUserPhotos()
    scrollToToday()
  } finally {
    isLoading.value = false
  }
})

// 날짜 포맷팅 관련
const year = computed(() => currentDate.value.getFullYear())
const month = computed(() => currentDate.value.getMonth() + 1)

const items = computed(() =>
  [...store.user_photos].map((p) => ({
    id: p.photoId,
    date: new Date(p.createdAt),
    thumbnail: p.photoUrl
  }))
)

// 현재 월의 전체 일자 데이터 생성
const daysInCurrentMonth = computed(() => {
  const yearVal = currentDate.value.getFullYear()
  const monthVal = currentDate.value.getMonth()
  const today = new Date()

  const lastDayOfMonth = new Date(yearVal, monthVal + 1, 0).getDate()
  
  const days = []
  for (let d = 1; d <= lastDayOfMonth; d++) {
    const dateObj = new Date(yearVal, monthVal, d)
    
    // 해당 날짜의 아이템 필터링
    const dayItems = items.value.filter(item => 
      item.date.getFullYear() === yearVal &&
      item.date.getMonth() === monthVal &&
      item.date.getDate() === d
    )

    days.push({
      date: dateObj,
      isToday: dateObj.toDateString() === today.toDateString(),
      items: dayItems
    })
  }
  return days
})

const currentMonthItemsCount = computed(() => {
  return daysInCurrentMonth.value.reduce((acc, day) => acc + day.items.length, 0)
})

function getDayName(date) {
  return weekDayNames[date.getDay()]
}

function getDayKey(day) {
  return day.date.toISOString().slice(0, 10) // YYYY-MM-DD
}

function isExpanded(day) {
  return expandedDays.value.has(getDayKey(day))
}

function toggleExpand(day) {
  const key = getDayKey(day)
  if (expandedDays.value.has(key)) {
    expandedDays.value.delete(key)
  } else {
    expandedDays.value.add(key)
  }
}

function getVisibleItems(day) {
  if (isExpanded(day)) {
    return day.items
  }
  // 기본 3개 노출
  return day.items.slice(0, 3)
}

// 네비게이션
function prevMonth() {
  const newDate = new Date(currentDate.value)
  newDate.setMonth(newDate.getMonth() - 1)
  currentDate.value = newDate
  expandedDays.value.clear() // 월 이동 시 확장 상태 초기화
}

function nextMonth() {
  const newDate = new Date(currentDate.value)
  newDate.setMonth(newDate.getMonth() + 1)
  currentDate.value = newDate
  expandedDays.value.clear()
}

function goToday() {
  currentDate.value = new Date()
  nextTick(() => {
    scrollToToday()
  })
}

function scrollToToday() {
  if (!scrollContainer.value) return
  
  // 오늘 날짜 요소를 찾아서 스크롤 (class="today" 검색)
  // DOM 업데이트 후 실행되어야 함
  setTimeout(() => {
    const todayEl = scrollContainer.value.querySelector('.today')
    if (todayEl) {
      const containerWidth = scrollContainer.value.clientWidth
      const elLeft = todayEl.offsetLeft
      const elWidth = todayEl.offsetWidth
      
      // 중앙 정렬
      scrollContainer.value.scrollTo({
        left: elLeft - (containerWidth / 2) + (elWidth / 2),
        behavior: 'smooth'
      })
    }
  }, 100)
}

function handleWheel(e) {
  if (scrollContainer.value) {
    scrollContainer.value.scrollLeft += e.deltaY
  }
}

function handleDateClick(day) {
  // 날짜 클릭 시 동작
}

function openDetail(id) {
  selectedPhotoId.value = id;
}

function closeDetail() {
  selectedPhotoId.value = null;
}

function goToUpload() {
  router.push({ name: 'analysisUpload' }).catch(() => {})
}
</script>

<style scoped>
/* Timeline Style */
.analysis-calendar-container {
  max-width: 1000px;
  width: 100%;
  margin: 40px auto; /* 상하 여백 추가 */
  padding: 30px; /* 내부 여백 추가 */
  font-family: 'Pretendard', sans-serif;
  background-color: white; /* 배경색 추가 */
  border-radius: 24px; /* 둥근 모서리 */
  box-shadow: 0 10px 40px rgba(0,0,0,0.04); /* 부드러운 그림자 */
  border: 1px solid #f0f0f0; /* 미세한 테두리 */
  overflow: hidden;
}

.calendar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding: 0 10px;
}

.header-left {
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.current-date {
  font-size: 24px;
  font-weight: 800;
  margin: 0;
  color: #333;
}

.nav-buttons {
  display: flex;
  gap: 4px;
}

.nav-btn {
  background: white;
  border: 1px solid #ddd;
  padding: 6px 12px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.nav-btn:hover { background: #f5f5f5; }
.today-btn { font-weight: 600; color: #6b55c7; }

/* Timeline Body */
.timeline-body {
  width: 100%;
  position: relative;
  margin-bottom: 20px;
}

.timeline-scroll-area {
  display: flex; /* Flex container로 변경 */
  flex-wrap: nowrap;
  overflow-x: auto;
  width: 100%;
  min-height: 250px;
  padding: 20px 0;
  box-sizing: border-box;
  scrollbar-width: thin;
  scrollbar-color: #ddd transparent;
  scroll-snap-type: x mandatory;
}

.timeline-scroll-area::-webkit-scrollbar {
  height: 8px;
}
.timeline-scroll-area::-webkit-scrollbar-track {
  background: transparent;
}
.timeline-scroll-area::-webkit-scrollbar-thumb {
  background-color: #ddd;
  border-radius: 4px;
}

/* timeline-track 스타일 제거됨 */

.timeline-day {
  /* 부모(.timeline-scroll-area) 너비의 1/7 */
  flex: 0 0 calc(100% / 7);
  width: calc(100% / 7);
  min-width: 0; /* flex item 최소 너비 리셋 */
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  cursor: pointer;
  scroll-snap-align: start;
}

/* Node Area */
.timeline-node-area {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 16px;
}

.weekday-label {
  font-size: 13px;
  font-weight: 600;
  color: #888;
  margin-bottom: 8px;
}

.weekday-label.sunday { color: #e74c3c; }
.weekday-label.saturday { color: #3498db; }

.timeline-line-container {
  width: 100%;
  display: flex;
  align-items: center;
  position: relative;
  height: 32px;
}

.line-segment {
  flex: 1;
  height: 2px;
  background-color: #eee;
}

/* 시작과 끝 선 처리 */
.line-segment.left.start { background-color: transparent; }
.line-segment.right.end { background-color: transparent; }

.node-circle {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: white;
  border: 2px solid #e0e0e0;
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 2;
  transition: all 0.2s;
  flex-shrink: 0;
}

.day-number {
  font-size: 14px;
  font-weight: 600;
  color: #555;
}

/* Hover & Active States */
.timeline-day:hover .node-circle {
  border-color: #bfaee3;
  transform: scale(1.1);
}

.timeline-day.today .node-circle {
  background-color: #6b55c7;
  border-color: #6b55c7;
  box-shadow: 0 0 0 4px rgba(107, 85, 199, 0.2);
}

.timeline-day.today .day-number {
  color: white;
}

.timeline-day.has-event .node-circle {
  border-color: #6b55c7;
}

/* Content Area */
.timeline-content {
  width: 100%;
  padding: 0 4px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: center;
  /* max-height 제거, 자연스럽게 늘어나도록 함 */
}

.event-thumbnail {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid #eee;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
  transition: transform 0.2s;
  flex-shrink: 0;
}

.event-thumbnail:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 8px rgba(0,0,0,0.1);
}

.event-thumbnail img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.more-btn, .fold-btn {
  width: 56px;
  height: 32px;
  border-radius: 16px;
  background-color: #f5f5f5;
  color: #666;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid transparent;
}

.more-btn:hover, .fold-btn:hover {
  background-color: #e8e8e8;
  color: #333;
}

.more-count {
  color: #6b55c7;
  font-weight: 700;
}

.empty-slot {
  width: 56px;
  height: 56px;
  border-radius: 8px;
  background: transparent;
}

.empty-week-notice {
  text-align: center;
  padding: 60px;
  color: #888;
  background: #fafafa;
  border-radius: 12px;
  margin: 20px auto;
  max-width: 1000px;
}

.upload-btn {
  margin-top: 16px;
  padding: 8px 20px;
  background-color: #6b55c7;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  transition: background-color 0.2s;
}

.upload-btn:hover {
  background-color: #5a47a8;
}
</style>