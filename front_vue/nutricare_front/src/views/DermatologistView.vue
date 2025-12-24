<template>
  <div class="layout">
    <Header />
    <main class="main-content">
      <div class="map-container">
        <h2 class="title">내 주변 피부과 찾기</h2>
        <div class="content-wrapper">
          <!-- 왼쪽 영역: 리스트 또는 상세 정보 -->
          <div class="side-panel">
            
            <!-- 1. 상세 정보 뷰 -->
            <div v-if="selectedPlace" class="detail-view">
              <button @click="selectedPlace = null" class="back-btn">
                <i class="bi bi-arrow-left"></i> 목록으로 돌아가기
              </button>
              
              <div v-if="isLoadingDetail" class="detail-loading">
                <div class="spinner-border text-primary" role="status"></div>
                <p>상세 정보를 불러오는 중...</p>
              </div>

              <div v-else class="detail-content">
                <!-- 장소 사진 (첫 번째 사진 사용) -->
                <div v-if="detailData.photos && detailData.photos.length > 0" class="place-photo">
                  <img :src="detailData.photos[0].getURI()" alt="Place Photo" />
                </div>

                <div class="detail-header">
                  <div class="detail-title-row">
                    <h3 class="detail-name">{{ detailData.displayName }}</h3>
                    <span class="detail-dist" v-if="detailData.distanceFormatted">{{ detailData.distanceFormatted }}</span>
                  </div>
                  <div class="detail-rating" v-if="detailData.rating">
                    <span class="star">★</span>
                    <span class="score">{{ detailData.rating }}</span>
                    <span class="count">({{ detailData.userRatingCount }}개의 리뷰)</span>
                  </div>
                </div>

                <div class="detail-info-list">
                  <div class="info-item">
                    <i class="bi bi-geo-alt-fill"></i>
                    <span>{{ detailData.formattedAddress }}</span>
                  </div>
                  <div class="info-item" v-if="detailData.nationalPhoneNumber">
                    <i class="bi bi-telephone-fill"></i>
                    <a :href="'tel:' + detailData.nationalPhoneNumber">{{ detailData.nationalPhoneNumber }}</a>
                  </div>
                  <div class="info-item" v-if="detailData.websiteUri">
                    <i class="bi bi-globe"></i>
                    <a :href="detailData.websiteUri" target="_blank">웹사이트 방문</a>
                  </div>
                  <div class="info-item">
                    <i class="bi bi-clock-fill"></i>
                    <span :class="['status', detailData.businessStatus === 'OPERATIONAL' ? 'open' : 'closed']">
                      {{ detailData.businessStatus === 'OPERATIONAL' ? '영업 중' : '영업 종료/정보 없음' }}
                    </span>
                  </div>
                </div>

                <!-- 리뷰 섹션 -->
                <div class="reviews-section">
                  <h4>방문자 리뷰</h4>
                  <div v-if="!detailData.reviews || detailData.reviews.length === 0" class="no-reviews">
                    등록된 리뷰가 없습니다.
                  </div>
                  <div v-else class="review-item" v-for="(review, idx) in detailData.reviews" :key="idx">
                    <div class="review-meta">
                      <span class="review-author">{{ review.authorAttribution.displayName }}</span>
                      <span class="review-stars">★ {{ review.rating }}</span>
                    </div>
                    <p class="review-text">{{ review.text }}</p>
                    <span class="review-date">{{ review.relativePublishTimeDescription }}</span>
                  </div>
                </div>
              </div>
            </div>

      <!-- 목록 영역 -->
      <div class="list-wrapper">
        <!-- 정렬 옵션 -->
        <div class="sort-options" v-if="placesList.length > 0">
          <button 
            :class="{ active: currentSort === 'distance' }" 
            @click="sortPlaces('distance')"
          >
            거리순
          </button>
          <button 
            :class="{ active: currentSort === 'rating' }" 
            @click="sortPlaces('rating')"
          >
            평점순
          </button>
          <button 
            :class="{ active: currentSort === 'review' }" 
            @click="sortPlaces('review')"
          >
            리뷰순
          </button>
        </div>

        <div v-if="errorMessage" class="error-msg">
          {{ errorMessage }}
        </div>
        <div v-else-if="placesList.length === 0" class="no-result">
          검색 중이거나 결과가 없습니다...
        </div>
              <ul v-else class="places-list">
                <li 
                  v-for="place in placesList" 
                  :key="place.id" 
                  class="place-item"
                  @click="handlePlaceSelect(place)"
                >
                                <div class="place-info">
                                  <div class="place-header-row">
                                    <strong class="place-name">{{ place.displayName }}</strong>
                                    <span class="place-dist">{{ place.distanceFormatted }}</span>
                                  </div>
                                  <p class="place-addr">{{ place.formattedAddress }}</p>
                                  <div class="place-meta">
                                    <span v-if="place.rating" class="place-rating">
                                      ★ {{ place.rating }} ({{ place.userRatingCount || 0 }})
                                    </span>
                                    <span v-if="place.businessStatus === 'OPERATIONAL'" class="place-status open">영업중</span>
                                  </div>
                                </div>                </li>
              </ul>
            </div>
          </div>

          <!-- 오른쪽 영역: 지도 -->
          <div class="map-wrapper">
            <div id="map" class="map-view"></div>
          </div>
        </div>
      </div>
    </main>
    <Footer />
  </div>
</template>

<script setup>
import { onMounted, ref, reactive } from 'vue';
import Header from '@/components/common/Header.vue'
import Footer from '@/components/common/Footer.vue'

let map = null;
let infowindow = null;
let markers = []; 

const placesList = ref([]);
const errorMessage = ref('');

// 상세 정보 관련 상태
const selectedPlace = ref(null);
const isLoadingDetail = ref(false);
const detailData = reactive({
  displayName: '',
  formattedAddress: '',
  rating: null,
  userRatingCount: 0,
  nationalPhoneNumber: '',
  websiteUri: '',
  businessStatus: '',
  reviews: [],
  photos: [],
  distanceFormatted: '' // 거리 정보 추가
});

const FIXED_LOCATION = {
  lat: 36.35528417,
  lng: 127.2981534
};

onMounted(async () => {
  try {
    await initMap();
  } catch (error) {
    console.error("Map Initialization Error:", error);
    errorMessage.value = "지도 로드 중 오류가 발생했습니다: " + error.message;
  }
});

const initMap = async () => {
  const { Map } = await google.maps.importLibrary("maps");
  const { Place } = await google.maps.importLibrary("places");
  const { Marker } = await google.maps.importLibrary("marker"); 
  const { spherical } = await google.maps.importLibrary("geometry"); // geometry 라이브러리 로드

  const myLocation = new google.maps.LatLng(FIXED_LOCATION.lat, FIXED_LOCATION.lng);

  map = new Map(document.getElementById("map"), {
    center: myLocation,
    zoom: 14,
    mapTypeControl: false,
    fullscreenControl: false,
    streetViewControl: false,
  });

  new Marker({
    position: myLocation,
    map: map,
    title: "삼성화재 유성연수원",
    icon: "http://maps.google.com/mapfiles/ms/icons/blue-dot.png"
  });

  infowindow = new google.maps.InfoWindow();

  const request = {
    textQuery: "피부과",
    fields: ['id', 'location'], 
    locationBias: {
      center: myLocation,
      radius: 5000, 
    },
    maxResultCount: 20,
    language: 'ko',
  };

  try {
    const { places } = await Place.searchByText(request);
    
    if (places.length > 0) {
      for(const place of places) {
        await place.fetchFields({ fields: ['displayName', 'formattedAddress', 'rating', 'userRatingCount', 'businessStatus', 'location'] });
        
        // 거리 계산 로직 추가
        if (place.location) {
          const distanceMeters = spherical.computeDistanceBetween(myLocation, place.location);
          place.distanceMeters = distanceMeters; // 정렬용 숫자 데이터
          place.distanceFormatted = formatDistance(distanceMeters);
        } else {
          place.distanceMeters = Infinity; // 위치 없으면 맨 뒤로
          place.distanceFormatted = '';
        }
      }

      placesList.value = places; 
      
      // 초기 정렬 (거리순)
      sortPlaces('distance');

      errorMessage.value = '';
      places.forEach(place => createMarker(place, Marker));
    } else {
      errorMessage.value = '반경 내에 검색된 피부과가 없습니다.';
    }
  } catch (e) {
    console.error("Place Search Failed:", e);
    errorMessage.value = `검색 실패: ${e.message}`;
  }
};

// 정렬 상태 및 함수
const currentSort = ref('distance'); // distance, rating, review

const sortPlaces = (criteria) => {
  currentSort.value = criteria;
  
  placesList.value.sort((a, b) => {
    if (criteria === 'distance') {
      return (a.distanceMeters || Infinity) - (b.distanceMeters || Infinity);
    } else if (criteria === 'rating') {
      return (b.rating || 0) - (a.rating || 0); // 높은 순
    } else if (criteria === 'review') {
      return (b.userRatingCount || 0) - (a.userRatingCount || 0); // 많은 순
    }
    return 0;
  });
};

// 거리 포맷팅 헬퍼 함수
const formatDistance = (meters) => {
  if (meters >= 1000) {
    return (meters / 1000).toFixed(1) + "km";
  }
  return Math.round(meters) + "m";
};

const createMarker = (place, MarkerClass) => {
  if (!place.location) return;

  const marker = new MarkerClass({
    map: map,
    position: place.location,
    title: place.displayName,
  });
  
  markers.push(marker);

  marker.addListener("click", () => {
    handlePlaceSelect(place);
  });
};

// 장소 선택 처리 (리스트 클릭 또는 마커 클릭)
const handlePlaceSelect = async (place) => {
  selectedPlace.value = place;
  isLoadingDetail.value = true;
  
  // 지도 이동
  map.setCenter(place.location);
  map.setZoom(17);

  // 인포윈도우 표시
  infowindow.setContent(`
    <div style="padding:5px; color:black; font-weight:bold;">${place.displayName}</div>
  `);
  infowindow.open(map, markers.find(m => m.title === place.displayName));

  try {
    // 상세 정보 fetch (리뷰, 전화번호, 홈페이지, 영업시간, 사진 등)
    await place.fetchFields({
      fields: [
        'displayName', 'formattedAddress', 'rating', 'userRatingCount', 
        'nationalPhoneNumber', 'websiteURI', 'businessStatus', 
        'reviews', 'regularOpeningHours', 'photos'
      ]
    });

    // 상태 업데이트
    detailData.displayName = place.displayName;
    detailData.formattedAddress = place.formattedAddress;
    detailData.rating = place.rating;
    detailData.userRatingCount = place.userRatingCount;
    detailData.nationalPhoneNumber = place.nationalPhoneNumber;
    detailData.websiteUri = place.websiteURI; // API는 websiteURI, 내부 데이터는 기존 변수명 유지 혹은 일치
    detailData.businessStatus = place.businessStatus;
    detailData.reviews = place.reviews || [];
    detailData.photos = place.photos || [];
    detailData.distanceFormatted = place.distanceFormatted; // 거리 정보 전달

  } catch (e) {
    console.error("Fetch Details Failed:", e);
  } finally {
    isLoadingDetail.value = false;
  }
};

const moveToLocation = (place) => {
  handlePlaceSelect(place);
};
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

.map-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 40px 20px;
  display: flex;
  flex-direction: column;
  height: calc(100vh - 80px);
}

.title {
  font-size: 28px;
  font-weight: 700;
  color: #2d3748;
  margin-bottom: 24px;
  text-align: center;
}

.content-wrapper {
  display: flex;
  flex: 1;
  gap: 20px;
  height: 100%;
  overflow: hidden; 
}

/* 사이드 패널 (리스트 & 상세정보) */
.side-panel {
  width: 400px;
  background: white;
  border-radius: 16px;
  border: 1px solid #edf2f7;
  display: flex;
  flex-direction: column;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
  overflow: hidden;
}

.list-wrapper, .detail-view {
  flex: 1;
  overflow-y: auto;
}

/* 정렬 바 스타일 */
.sort-options {
  display: flex;
  padding: 10px;
  gap: 8px;
  border-bottom: 1px solid #edf2f7;
  background: #fff;
  position: sticky;
  top: 0;
  z-index: 10;
}

.sort-options button {
  flex: 1;
  padding: 8px;
  border: 1px solid #e2e8f0;
  background: #f8fafc;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 600;
  color: #4a5568;
  cursor: pointer;
  transition: all 0.2s;
}

.sort-options button:hover {
  background: #edf2f7;
}

.sort-options button.active {
  background: #6b55c7;
  color: white;
  border-color: #6b55c7;
}

/* 상세 정보 뷰 스타일 */
.back-btn {
  width: 100%;
  padding: 15px;
  background: #f8f9fa;
  border: none;
  border-bottom: 1px solid #edf2f7;
  text-align: left;
  font-weight: 600;
  color: #6b55c7;
  cursor: pointer;
}

.detail-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: #718096;
}

.place-photo {
  width: 100%;
  height: 200px;
  overflow: hidden;
}
.place-photo img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.detail-content {
  padding: 20px;
}

.detail-header {
  margin-bottom: 20px;
}

.detail-name {
  font-size: 22px;
  font-weight: 800;
  color: #2d3748;
  margin-bottom: 8px;
}

.detail-rating {
  display: flex;
  align-items: center;
  gap: 5px;
}
.star { color: #f59e0b; font-size: 20px; }
.score { font-weight: 700; font-size: 18px; }
.count { color: #718096; font-size: 14px; }

.detail-info-list {
  margin-bottom: 30px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-item {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  color: #4a5568;
  font-size: 15px;
}
.info-item i { color: #6b55c7; }
.info-item a { color: #3182ce; text-decoration: none; }
.status.open { color: #48bb78; font-weight: bold; }
.status.closed { color: #e53e3e; font-weight: bold; }

/* 리뷰 섹션 */
.reviews-section h4 {
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 15px;
  padding-top: 20px;
  border-top: 1px solid #edf2f7;
}

.review-item {
  background: #f7fafc;
  padding: 15px;
  border-radius: 12px;
  margin-bottom: 15px;
}

.review-meta {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}
.review-author { font-weight: 700; font-size: 14px; }
.review-stars { color: #f59e0b; font-weight: 700; }
.review-text {
  font-size: 14px;
  line-height: 1.6;
  color: #4a5568;
  margin-bottom: 8px;
}
.review-date {
  font-size: 12px;
  color: #a0aec0;
}

/* 기본 목록 스타일 */
.places-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.place-item {
  padding: 20px;
  border-bottom: 1px solid #edf2f7;
  cursor: pointer;
  transition: background 0.2s;
}

.place-item:hover {
  background-color: #f7fafc;
}

.place-header-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 4px;
}

.place-name {
  font-size: 16px;
  color: #2d3748;
  display: block;
  flex: 1; /* 이름이 길어지면 줄바꿈 하되 거리는 밀리지 않게 */
}

.place-dist {
  font-size: 13px;
  color: #6b55c7;
  font-weight: 700;
  margin-left: 8px;
  white-space: nowrap;
}

.detail-title-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.detail-dist {
  font-size: 14px;
  color: #6b55c7;
  font-weight: 700;
  background-color: #f3f0ff;
  padding: 4px 8px;
  border-radius: 6px;
}

.place-addr {
  font-size: 13px;
  color: #718096;
  margin: 0 0 8px 0;
}

.place-meta {
  display: flex;
  gap: 10px;
  font-size: 13px;
}

.place-rating {
  color: #f59e0b;
  font-weight: 600;
}

.place-status.open {
  color: #48bb78;
  font-weight: 600;
}

.no-result {
  padding: 40px;
  text-align: center;
  color: #a0aec0;
}

.error-msg {
  padding: 20px;
  text-align: center;
  color: #e53e3e;
  font-weight: bold;
  background-color: #fff5f5;
  margin: 10px;
  border-radius: 8px;
}

/* 지도 스타일 */
.map-wrapper {
  flex: 1;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(0,0,0,0.1);
  border: 1px solid #edf2f7;
}

.map-view {
  width: 100%;
  height: 100%;
}

@media (max-width: 992px) {
  .content-wrapper {
    flex-direction: column-reverse; 
  }
  
  .side-panel {
    width: 100%;
    height: 40%;
  }

  .map-wrapper {
    height: 60%;
  }
}
</style>