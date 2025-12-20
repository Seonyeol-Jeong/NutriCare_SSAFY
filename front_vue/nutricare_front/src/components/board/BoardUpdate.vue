<template>
  <section class="board-update">
    <div class="card shadow-sm">
      <div class="card-header bg-white py-3">
        <h2 class="mb-0 h5">게시글 수정</h2>
      </div>
      <div class="card-body p-4">
        <form @submit.prevent="onSubmit">
          <!-- 카테고리 선택 -->
          <div class="mb-3">
            <label for="category" class="form-label">카테고리</label>
            <select id="category" class="form-select" v-model="form.category" required>
              <option value="" disabled>카테고리를 선택하세요</option>
              <option value="주사">주사</option>
              <option value="여드름">여드름</option>
              <option value="건선">건선</option>
              <option value="지루">지루</option>
            </select>
          </div>

          <!-- 제목 입력 -->
          <div class="mb-3">
            <label for="title" class="form-label">제목</label>
            <input
              type="text"
              id="title"
              class="form-control"
              v-model="form.title"
              placeholder="제목을 입력하세요"
              required
            />
          </div>

          <!-- 내용 입력 -->
          <div class="mb-3">
            <label for="content" class="form-label">내용</label>
            <textarea
              id="content"
              class="form-control"
              v-model="form.content"
              placeholder="내용을 입력하세요"
              rows="10"
              required
            ></textarea>
          </div>

          <!-- 이미지 업로드 -->
          <div class="mb-4">
            <label for="images" class="form-label">이미지 추가 (선택)</label>
            <input
              type="file"
              id="images"
              class="form-control"
              multiple
              accept="image/*"
              @change="handleFileChange"
            />
            <div class="form-text">새로운 이미지를 추가할 수 있습니다.</div>
          </div>

          <!-- 새로 추가할 이미지 미리보기 -->
          <div v-if="newImagesPreview.length > 0" class="mb-4">
            <h6 class="mb-2">새로 추가할 이미지</h6>
            <div class="row g-2">
              <div v-for="(img, index) in newImagesPreview" :key="index" class="col-md-3 col-6 position-relative">
                <div class="image-wrapper">
                  <img :src="img.url" class="img-fluid rounded border" alt="New Upload Preview" />
                  <button
                    type="button"
                    class="delete-btn"
                    aria-label="Remove"
                    @click="removeNewImage(index)"
                  >×</button>
                </div>
              </div>
            </div>
          </div>

          <!-- 기존 이미지 미리보기 및 삭제 -->
          <div v-if="existingImages.length > 0" class="mb-4">
            <h6 class="mb-2">기존 이미지</h6>
            <div class="row g-2">
              <div v-for="img in existingImages" :key="img.imageId" class="col-md-3 col-6 position-relative">
                <div class="image-wrapper">
                  <img :src="img.imageUrl" class="img-fluid rounded border" :alt="img.imageUrl" />
                  <button
                    type="button"
                    class="delete-btn"
                    aria-label="Remove"
                    @click="removeExistingImage(img.imageId)"
                  >×</button>
                </div>
              </div>
            </div>
          </div>

          <!-- 버튼 영역 -->
          <div class="d-flex justify-content-end gap-2">
            <button type="button" class="btn btn-outline-secondary" @click="goBack">
              취소
            </button>
            <button type="submit" class="btn btn-primary" :disabled="isSubmitting">
              {{ isSubmitting ? '저장 중...' : '수정 완료' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useBoardStore } from '@/stores/board'
import axios from '@/api/axios'

const router = useRouter()
const route = useRoute()
const boardStore = useBoardStore()

const boardId = route.params.id
const isSubmitting = ref(false)
const existingImages = ref([])
const newFiles = ref([])

const form = reactive({
  title: '',
  content: '',
  category: '',
})

// 초기 데이터 로드
onMounted(async () => {
  if (!boardId) {
    alert('잘못된 접근입니다.')
    router.back()
    return
  }

  // 스토어에서 데이터 가져오기 (캐시된 데이터가 있을 수 있으나 최신 상태 보장을 위해 호출 권장)
  if (!boardStore.board || boardStore.board.boardId != boardId) {
    await boardStore.fetchBoardById(boardId)
  }
  
  const board = boardStore.board

  if (board) {
    form.title = board.title
    form.content = board.content
    form.category = board.category || '자유'
    existingImages.value = board.images || []
  } else {
    alert('게시글 정보를 불러올 수 없습니다.')
    router.back()
  }
})

// 파일 선택 핸들러
const newImagesPreview = ref([])

function handleFileChange(event) {
  const files = Array.from(event.target.files)
  if (files.length === 0) return

  // 기존 선택된 파일에 추가하거나, 새로 덮어쓰기 정책 결정. 여기서는 추가.
  newFiles.value = [...newFiles.value, ...files]

  files.forEach(file => {
    const url = URL.createObjectURL(file)
    newImagesPreview.value.push({
      url: url,
      file: file // 나중에 삭제 시 식별을 위해
    })
  })
  
  // input value 초기화 (같은 파일 다시 선택 가능하게)
  event.target.value = ''
}

// 새 이미지 목록에서 삭제
function removeNewImage(index) {
  const item = newImagesPreview.value[index]
  URL.revokeObjectURL(item.url) // 메모리 해제
  
  newImagesPreview.value.splice(index, 1)
  newFiles.value.splice(index, 1)
}

// 기존 이미지 삭제 핸들러 (API 호출)
async function removeExistingImage(imageId) {
  if (!confirm('이미지를 삭제하시겠습니까?')) return

  try {
    // boardStore에는 deleteImage가 없으므로 직접 axios 호출하거나 store에 추가 필요
    // 여기서는 직접 호출 예시
    await axios.delete(`/board-images/${imageId}`)
    
    // UI 반영
    existingImages.value = existingImages.value.filter(img => img.imageId !== imageId)
  } catch (error) {
    console.error('이미지 삭제 실패:', error)
    alert('이미지 삭제에 실패했습니다.')
  }
}

// 폼 제출 핸들러
async function onSubmit() {
  if (!form.title || !form.content) {
    alert('제목과 내용을 모두 입력해주세요.')
    return
  }

  isSubmitting.value = true
  try {
    // 1. 게시글 텍스트 정보 수정
    await boardStore.updateBoard(boardId, {
      title: form.title,
      content: form.content,
      category: form.category,
    })

    // 2. 새 이미지 업로드 (있을 경우)
    if (newFiles.value.length > 0) {
      await boardStore.uploadBoardImages({
        boardId: boardId,
        files: newFiles.value
      })
    }

    alert('게시글이 수정되었습니다.')
    router.push({ name: 'boardDetail', params: { id: boardId } })
  } catch (error) {
    console.error('게시글 수정 실패:', error)
    alert('게시글 수정 중 오류가 발생했습니다.')
  } finally {
    isSubmitting.value = false
  }
}

function goBack() {
  router.back()
}
</script>

<style scoped>
.board-update {
  max-width: 900px;
  margin: 40px auto;
  padding: 0 16px;
}

.image-wrapper {
  position: relative;
  width: 100%;
  padding-top: 100%; /* 1:1 Aspect Ratio */
  overflow: hidden;
  border-radius: 0.25rem;
}

.image-wrapper img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.delete-btn {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background-color: #dc3545; /* Bootstrap danger color */
  color: white;
  border: none;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 18px;
  line-height: 1;
  cursor: pointer;
  z-index: 10;
  padding: 0;
  transition: background-color 0.2s;
}

.delete-btn:hover {
  background-color: #bb2d3b;
}

/* 부트스트랩 기본 스타일 오버라이드 혹은 보조 */
.form-label {
  font-weight: 600;
  color: #555;
}
</style>