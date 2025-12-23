<template>
  <section class="board-create">
    <div class="card">
      <div class="card-header">
        <h2>새 게시글 작성</h2>
      </div>
      <div class="card-body">
        <form @submit.prevent="onSubmit">
          <div class="mb-3">
            <label for="category" class="form-label">카테고리</label>
            <div class="d-flex gap-2 align-items-center mb-2">
              <select id="category" v-model="form.category" class="form-select" required>
                <option disabled value="">카테고리 선택</option>
                <option value="주사">주사</option>
                <option value="여드름">여드름</option>
                <option value="건선">건선</option>
                <option value="지루">지루</option>
              </select>
              <button type="button" class="btn btn-outline-primary text-nowrap" @click="openAnalysisModal">
                내 분석 기록 불러오기
              </button>
            </div>
          </div>

          <div class="mb-3">
            <label for="title" class="form-label">제목</label>
            <input
              id="title"
              class="form-control"
              v-model="form.title"
              type="text"
              placeholder="제목을 입력하세요."
              required
            />
          </div>

          <div class="mb-3">
            <label class="form-label">내용</label>
            
            <!-- 분석 결과 미리보기 영역 -->
            <div v-if="selectedAnalysisHtml" class="selected-analysis-preview">
              <div class="preview-header">
                <span>첨부된 분석 결과</span>
                <button type="button" class="btn-remove" @click="removeSelectedAnalysis">
                  <i class="bi bi-x-circle"></i> 삭제
                </button>
              </div>
              <div class="preview-content" v-html="selectedAnalysisHtml"></div>
            </div>

            <textarea
              id="content"
              class="form-control"
              v-model="form.content"
              placeholder="내용을 입력하세요."
              rows="10"
              :required="!selectedAnalysisHtml"
            ></textarea>
          </div>
          
          <!-- 파일 첨부 로직은 유지하되, 실제 API 연동은 추가 작업 필요 -->
          <div class="mb-3">
            <label for="formFile" class="form-label">파일 첨부 (선택)</label>
            <input class="form-control" type="file" id="formFile" @change="onFileChange" multiple>
            <div class="form-text" v-if="fileName">{{ fileName }}</div>
          </div>

          <div v-if="hasError" class="alert alert-danger mt-3" role="alert">
            {{ hasError }}
          </div>

          <div class="actions d-flex justify-content-end gap-2 mt-4">
            <button type="button" class="btn btn-outline-secondary" @click="goList">목록</button>
            <button type="submit" class="btn btn-primary" :disabled="isLoading">
              <span v-if="isLoading" class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>
              {{ isLoading ? '등록 중...' : '등록' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- 분석 기록 선택 모달 -->
    <div v-if="showAnalysisModal" class="modal-overlay" @click.self="closeAnalysisModal">
      <div class="modal-window">
        <div class="modal-header">
          <h3>분석 기록 선택</h3>
          <button class="close-btn" @click="closeAnalysisModal">&times;</button>
        </div>
        <div class="modal-body">
          <div v-if="analysisLoading" class="text-center p-4">
            <div class="spinner-border text-primary" role="status"></div>
          </div>
          <div v-else-if="analysisList.length === 0" class="text-center p-4">
            <p>분석 기록이 없습니다.</p>
          </div>
          <ul v-else class="analysis-list">
            <li v-for="item in analysisList" :key="item.photoId" class="analysis-item" @click="selectAnalysis(item)">
              <div class="item-thumb">
                <img :src="item.photoUrl" alt="썸네일" />
              </div>
              <div class="item-info">
                <div class="item-diagnosis">{{ item.diagnosisName || '분석 결과 없음' }}</div>
                <div class="item-date">{{ new Date(item.createdAt).toLocaleDateString() }}</div>
              </div>
            </li>
          </ul>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref, reactive, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useBoardStore } from '@/stores/board';
import { useAnalysisStore } from '@/stores/analysis';
import { storeToRefs } from 'pinia';
import { useUserStore } from '@/stores/user';

const router = useRouter();
const boardStore = useBoardStore();
const analysisStore = useAnalysisStore();
const userStore = useUserStore();

const { isLoading, hasError } = storeToRefs(boardStore);
const { userInfo } = storeToRefs(userStore);

// 모달 관련 상태
const showAnalysisModal = ref(false);
const analysisLoading = ref(false);
const analysisList = ref([]);
const selectedAnalysisHtml = ref(''); // 선택된 분석 결과 HTML 저장

const form = reactive({
  category: '',
  title: '',
  content: '',
  images: [], 
});

const fileName = ref('');

// 파일 선택 시 처리 로직
function onFileChange(event) {
  const files = Array.from(event.target.files || []);
  form.images = files;
  fileName.value = files.map((f) => f.name).join(', ');
}

// 목록으로 돌아가기
function goList() {
  router.push({ name: 'boardList' }).catch(() => {});
}

// 모달 열기 및 데이터 로드
async function openAnalysisModal() {
  showAnalysisModal.value = true;
  analysisLoading.value = true;
  try {
    const photos = await analysisStore.fetchUserPhotos();
    // 최신순 정렬
    analysisList.value = [...photos].sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
  } catch (e) {
    console.error("분석 기록 로드 실패:", e);
    alert("분석 기록을 불러오는데 실패했습니다.");
    showAnalysisModal.value = false;
  } finally {
    analysisLoading.value = false;
  }
}

function closeAnalysisModal() {
  showAnalysisModal.value = false;
}

// 분석 기록 선택 시 HTML 생성 및 상태 저장
function selectAnalysis(item) {
  const diagnosis = item.analysisResult?.diagnosisName || item.analysisResult?.diagnosis_name || '';
  
  // 카테고리 자동 설정
  if (['주사', '여드름', '건선', '지루'].includes(diagnosis)) {
    form.category = diagnosis;
  } else if (diagnosis.includes('지루성')) {
    form.category = '지루';
  }

  // 제목 자동 입력
  if (!form.title) {
    form.title = `[분석 공유] ${diagnosis} 진단 결과 공유합니다.`;
  }

  // 데이터 준비
  const result = item.analysisResult || {};
  const dateStr = new Date(item.createdAt).toLocaleDateString();
  
  const probs = [
    { label: '주사', val: result.prob_rosacea, color: '#FF9F43' },
    { label: '여드름', val: result.prob_acne, color: '#FFE66D' },
    { label: '건선', val: result.prob_gunsun, color: '#FF6B6B' },
    { label: '지루성 피부염', val: result.prob_seborr, color: '#1A535C' },
    { label: '아토피', val: result.prob_atopy, color: '#4ECDC4' },
    { label: '정상', val: result.prob_normal, color: '#6ab04c' },
  ].filter(p => p.val !== undefined && p.val !== null);

  probs.sort((a, b) => b.val - a.val);

  // 가로형 HTML 컨텐츠 생성 (높이 최소화 버전)
  let htmlContent = `
    <div class="analysis-card-preview" style="background-color: #fdfdfd; border: 1px solid #e0e0e0; border-radius: 12px; padding: 12px; max-width: 760px; margin: 0 auto 20px auto; font-family: sans-serif; display: flex; gap: 16px; align-items: stretch; box-sizing: border-box; white-space: normal;">
  `;

  // 왼쪽: 이미지 영역 (고정 크기 120px)
  if (item.photoUrl) {
    htmlContent += `
      <div style="flex: 0 0 120px; width: 120px; border-radius: 8px; overflow: hidden; border: 1px solid #eee; background:#f8f8f8; display:flex; align-items:center; justify-content:center;">
        <img src="${item.photoUrl}" alt="분석 이미지" style="width: 100%; height: 100%; object-fit: cover;" />
      </div>
    `;
  }

  // 오른쪽: 정보 영역
  htmlContent += `
    <div style="flex: 1; display: flex; flex-direction: column; justify-content: space-between;">
      <div style="margin-bottom: 8px; border-bottom: 1px solid #f0f0f0; padding-bottom: 6px;">
         <span style="font-size: 15px; font-weight: 700; color: #333;">AI 분석 리포트: <span style="color:#6b55c7;">${diagnosis}</span></span>
         <span style="font-size: 11px; color: #999; margin-left: 8px;">${dateStr}</span>
      </div>
      
      <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 6px 20px;">
  `;

  // 상위 6개 모두 표시하되 2열로 콤팩트하게
  probs.forEach(p => {
    const percent = (p.val * 100).toFixed(1);
    const width = percent < 1 ? '1%' : `${percent}%`; 

    htmlContent += `
      <div style="width: 100%;">
        <div style="display: flex; justify-content: space-between; font-size: 11px; margin-bottom: 2px; color: #666;">
          <span>${p.label}</span>
          <span style="font-weight:600; color:#333;">${percent}%</span>
        </div>
        <div style="width: 100%; height: 4px; background-color: #eee; border-radius: 2px; overflow: hidden;">
          <div style="width: ${width}; height: 100%; background-color: ${p.color}; border-radius: 2px;"></div>
        </div>
      </div>
    `;
  });

  htmlContent += `</div></div></div>`; 

  selectedAnalysisHtml.value = htmlContent;
  closeAnalysisModal();
}

function removeSelectedAnalysis() {
  selectedAnalysisHtml.value = '';
}

// 폼 제출
async function onSubmit() {
  // 내용이나 분석 결과 중 하나는 있어야 함
  if (!form.title || (!form.content && !selectedAnalysisHtml.value) || !form.category) {
    alert('카테고리, 제목, 내용은 필수입니다.');
    return;
  }

  let newBoardId;
  try {
    // 1. 게시글 텍스트 내용 생성 (분석 결과 HTML + 사용자 작성 글)
    // 두 내용을 합쳐서 content 필드에 저장
    const finalContent = selectedAnalysisHtml.value 
      ? `${selectedAnalysisHtml.value}<br><br>${form.content}`
      : form.content;

    const payload = {
      title: form.title,
      content: finalContent,
      category: form.category,
      userName: userInfo.value?.name, 
    };
    newBoardId = await boardStore.createBoard(payload);
  } catch (error) {
    alert('게시글 생성에 실패했습니다. 다시 시도해주세요.');
    console.error('게시글 생성 실패:', error);
    return; // 게시글 생성 실패 시 중단
  }

  // 2. 이미지가 있으면, 생성된 게시글 ID를 이용해 이미지 업로드
  if (form.images.length > 0 && newBoardId) {
    try {
      await boardStore.uploadBoardImages({
        boardId: newBoardId.boardId, // 백엔드 응답에 따라 boardId 필드 사용
        files: form.images,
      });
    } catch (error) {
      alert(
        '게시글은 등록되었지만 이미지 업로드에 실패했습니다. 게시글 수정에서 다시 시도해주세요.'
      );
      console.error('이미지 업로드 실패:', error);
      // 이미지가 실패해도 목록으로 이동은 함
    }
  }

  // 3. 모든 과정 완료 후 목록으로 이동
  await boardStore.fetchBoards(); // 새 글 포함된 목록을 다시 불러옴
  goList();
}
</script>

<style scoped>
.board-create {
  max-width: 800px;
  margin: 40px auto;
  padding: 0 16px;
}
.form-text {
  font-size: 0.875em;
  color: #6c757d;
}

/* Modal Styles */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-window {
  background: white;
  width: 90%;
  max-width: 500px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
  display: flex;
  flex-direction: column;
  max-height: 80vh;
}

.modal-header {
  padding: 16px;
  border-bottom: 1px solid #eee;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h3 {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 600;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #666;
}

.modal-body {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.analysis-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.analysis-item {
  display: flex;
  gap: 12px;
  padding: 12px;
  border: 1px solid #eee;
  border-radius: 8px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.analysis-item:hover {
  background-color: #f8f9fa;
  border-color: #6b55c7;
}

.item-thumb {
  width: 60px;
  height: 60px;
  border-radius: 6px;
  overflow: hidden;
  background: #eee;
  flex-shrink: 0;
}

.item-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.item-info {
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.item-diagnosis {
  font-weight: 600;
  font-size: 16px;
  color: #333;
  margin-bottom: 4px;
}

.item-date {
  font-size: 13px;
  color: #888;
}

/* Analysis Preview Style */
.selected-analysis-preview {
  margin-bottom: 16px;
  border: 1px solid #eaddff;
  border-radius: 8px;
  background-color: #fcfaff;
  overflow: hidden;
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 16px;
  background-color: #f3f0ff;
  border-bottom: 1px solid #eaddff;
  color: #6b55c7;
  font-weight: 600;
  font-size: 14px;
}

.btn-remove {
  background: none;
  border: none;
  color: #e74c3c;
  cursor: pointer;
  font-size: 13px;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background 0.2s;
}

.btn-remove:hover {
  background-color: rgba(231, 76, 60, 0.1);
}

.preview-content {
  padding: 16px;
  max-height: 300px;
  overflow-y: auto;
}
</style>
