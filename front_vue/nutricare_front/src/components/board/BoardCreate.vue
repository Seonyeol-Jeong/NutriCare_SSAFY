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
            <select id="category" v-model="form.category" class="form-select" required>
              <option disabled value="">카테고리 선택</option>
              <option value="주사">주사</option>
              <option value="여드름">여드름</option>
              <option value="건선">건선</option>
              <option value="지루">지루</option>
            </select>
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
            <label for="content" class="form-label">내용</label>
            <textarea
              id="content"
              class="form-control"
              v-model="form.content"
              placeholder="내용을 입력하세요."
              rows="10"
              required
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
  </section>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { useBoardStore } from '@/stores/board';
import { storeToRefs } from 'pinia';
import { useUserStore } from '@/stores/user';

const router = useRouter();
const boardStore = useBoardStore();
const userStore = useUserStore(); // Import user store

const { isLoading, hasError } = storeToRefs(boardStore);
const { userInfo } = storeToRefs(userStore); // Get userInfo

const form = reactive({
  category: '',
  title: '',
  content: '',
  images: [], // 실제 파일 데이터 또는 Base64 인코딩 데이터
});

const fileName = ref('');

// 파일 선택 시 처리 로직
function onFileChange(event) {
  const files = Array.from(event.target.files || []);
  form.images = files; // 나중에 API에 맞게 수정 필요 (e.g., Base64 or FormData)
  fileName.value = files.map((f) => f.name).join(', ');
}

// 목록으로 돌아가기
function goList() {
  router.push({ name: 'boardList' }).catch(() => {});
}

// 폼 제출
async function onSubmit() {
  if (!form.title || !form.content || !form.category) {
    alert('카테고리, 제목, 내용은 필수입니다.');
    return;
  }

  try {
    // 1. 게시글 텍스트 내용 생성
    const payload = {
      title: form.title,
      content: form.content,
      category: form.category,
      userName: userInfo.value?.name, // Add userName to the payload
    };
    const newBoard = await boardStore.createBoard(payload);

    // 2. 이미지가 있으면, 생성된 게시글 ID를 이용해 이미지 업로드
    if (form.images.length > 0 && newBoard && newBoard.boardId) {
      await boardStore.uploadBoardImages({
        boardId: newBoard.boardId,
        files: form.images,
      });
    }

    // 3. 모든 과정 완료 후 목록으로 이동
    await boardStore.fetchBoards(); // 새 글 포함된 목록을 다시 불러옴
    goList();
  } catch (error) {
    // createBoard 또는 uploadBoardImages에서 발생한 에러 처리
    alert('게시글 등록에 실패했습니다. 다시 시도해주세요.');
    console.error(error); // 디버깅을 위해 콘솔에 에러 출력
  }
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
</style>
