<template>
  <section class="board-detail">
    <div class="card shadow-sm">
      <div class="card-header bg-light py-3">
        <h2 class="mb-0 h5">게시글 상세</h2>
      </div>
      <div class="card-body">
        <div v-if="isLoading" class="loading-spinner">
          <div class="spinner-border" role="status">
            <span class="visually-hidden">Loading...</span>
          </div>
        </div>

        <div v-else-if="hasError" class="alert alert-danger" role="alert">
          {{ hasError }}
        </div>

        <div v-else-if="!board" class="alert alert-info" role="alert">
          게시글을 찾을 수 없습니다.
        </div>

        <template v-else>
          <div class="d-flex justify-content-between align-items-baseline mb-3">
            <h3 class="card-title">{{ board.title }}</h3>
            <div class="text-muted small">
              <span class="badge bg-secondary me-2">{{ board.category }}</span>
              <span>작성자: {{ board.userName }}</span>
              <span class="mx-1">·</span>
              <span>조회수 {{ board.viewCount }}</span>
              <span class="mx-1">·</span>
              <span>{{ formatDate(board.createdAt) }}</span>
            </div>
          </div>
          <hr />

          <div class="content mb-4" v-html="board.content"></div>
          
          <div class="images-preview mb-4 row g-2" v-if="board.images && board.images.length">
            <div class="col-md-4 col-sm-6 col-12" v-for="img in board.images" :key="img.imageId">
              <img :src="img.imageUrl" class="img-fluid rounded shadow-sm" alt="게시글 이미지" />
            </div>
          </div>

          <div class="actions d-flex justify-content-end gap-2 mt-4">
            <button type="button" class="btn btn-outline-secondary" @click="goList">목록</button>
            <template v-if="isAuthor">
              <button type="button" class="btn btn-primary" @click="editBoard">수정</button>
              <button type="button" class="btn btn-danger" @click="deleteConfirm">삭제</button>
            </template>
          </div>
        </template>
      </div>
    </div>

    <!-- Comment Section -->
    <div class="card mt-4 shadow-sm">
      <div class="card-header bg-light py-3">
        <h3 class="mb-0 h6">댓글</h3>
      </div>
      <div class="card-body">
        <!-- Comment Create Form -->
        <form @submit.prevent="handleCommentSubmit" class="mb-4">
          <div class="input-group">
            <textarea
              v-model="newCommentContent"
              class="form-control"
              placeholder="댓글을 입력하세요..."
              rows="2"
              required
            ></textarea>
            <button class="btn btn-outline-primary" type="submit">등록</button>
          </div>
        </form>

        <!-- Comment List -->
        <div v-if="isCommentLoading" class="text-center">
          <div class="spinner-border spinner-border-sm" role="status">
            <span class="visually-hidden">Loading comments...</span>
          </div>
        </div>
        <ul v-else class="list-group list-group-flush">
          <li v-if="comments.length === 0" class="list-group-item text-center text-muted">
            작성된 댓글이 없습니다.
          </li>
          <li v-for="comment in comments" :key="comment.commentId" class="list-group-item">
            <div class="d-flex justify-content-between">
              <div>
                <span class="fw-bold me-2">{{ comment.userName }}</span>
                <span class="text-muted small">{{ formatDate(comment.createdAt) }}</span>
              </div>
              <div v-if="userStore.userInfo?.userId === comment.userId" class="actions">
                <button class="btn btn-sm btn-link text-decoration-none" @click="toggleEditMode(comment)">수정</button>
                <button class="btn btn-sm btn-link text-decoration-none text-danger" @click="handleCommentDelete(comment.commentId)">삭제</button>
              </div>
            </div>
            <div v-if="editingCommentId !== comment.commentId" class="mt-2">
              {{ comment.content }}
            </div>
            <div v-else class="mt-2">
              <form @submit.prevent="handleCommentUpdate(comment)">
                <textarea v-model="editingCommentContent" class="form-control form-control-sm" rows="2"></textarea>
                <div class="d-flex justify-content-end gap-2 mt-2">
                  <button type="button" class="btn btn-sm btn-outline-secondary" @click="toggleEditMode(null)">취소</button>
                  <button type="submit" class="btn btn-sm btn-primary">저장</button>
                </div>
              </form>
            </div>
          </li>
        </ul>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useBoardStore } from '@/stores/board';
import { useUserStore } from '@/stores/user';
import { useCommentStore } from '@/stores/comment';
import { storeToRefs } from 'pinia';

const router = useRouter();
const route = useRoute();
const boardStore = useBoardStore();
const userStore = useUserStore();
const commentStore = useCommentStore();

const { board, isLoading, hasError } = storeToRefs(boardStore);
const { comments, isLoading: isCommentLoading } = storeToRefs(commentStore);
const { userInfo } = storeToRefs(userStore); // userInfo를 storeToRefs에서 가져옴

const boardId = computed(() => route.params.id);

const newCommentContent = ref('');
const editingCommentId = ref(null);
const editingCommentContent = ref('');

watch(boardId, (newId) => {
  if (newId) {
    boardStore.fetchBoardById(newId);
    commentStore.fetchComments(newId);
  } else {
    boardStore.board = null;
    commentStore.comments = [];
  }
}, { immediate: true });

const isAuthor = computed(() => {
  return userInfo.value && board.value && userInfo.value.userId === board.value.userId;
});

const formatDate = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleString('ko-KR');
};

function goList() {
  router.push({ name: 'boardList' }).catch(() => {});
}

function editBoard() {
  router.push({ name: 'boardUpdate', params: { id: board.value.boardId } }).catch(() => {});
}

async function deleteConfirm() {
  if (confirm('정말로 이 게시글을 삭제하시겠습니까?')) {
    try {
      await boardStore.deleteBoard(board.value.boardId);
      alert('게시글이 삭제되었습니다.');
      router.push({ name: 'boardList' });
    } catch (error) {
      alert('게시글 삭제에 실패했습니다.');
    }
  }
}

async function handleCommentSubmit() {
  if (!newCommentContent.value.trim()) return;
  if (!userInfo.value?.name) {
    alert('로그인 후 댓글을 작성할 수 있습니다.');
    return;
  }
  try {
    await commentStore.createComment({
      boardId: boardId.value,
      content: newCommentContent.value,
      userName: userInfo.value.name, // userName 추가
    });
    newCommentContent.value = ''; // Reset input
  } catch (error) {
    alert(error.message);
  }
}

async function handleCommentDelete(commentId) {
  if (confirm('댓글을 삭제하시겠습니까?')) {
    try {
      await commentStore.deleteComment({
        boardId: boardId.value,
        commentId,
      });
    } catch (error) {
      alert(error.message);
    }
  }
}

function toggleEditMode(comment) {
  if (comment && editingCommentId.value !== comment.commentId) {
    editingCommentId.value = comment.commentId;
    editingCommentContent.value = comment.content;
  } else {
    editingCommentId.value = null;
    editingCommentContent.value = '';
  }
}

async function handleCommentUpdate(comment) {
  if (!editingCommentContent.value.trim()) return;
  try {
    await commentStore.updateComment({
      boardId: boardId.value,
      commentId: comment.commentId,
      content: editingCommentContent.value,
    });
    toggleEditMode(null); // Exit edit mode
  } catch (error) {
    alert(error.message);
  }
}
</script>

<style scoped>
.board-detail {
  max-width: 900px;
  margin: 40px auto;
  padding: 0 16px;
}

.card-header h2 {
  font-size: 1.25rem;
  font-weight: 600;
}

.content {
  min-height: 200px;
  line-height: 1.6;
  white-space: pre-wrap; /* Preserve newlines and spaces */
}

.images-preview img {
  max-height: 200px; /* Limit height of preview images */
  object-fit: cover;
  width: 100%;
}

.loading-spinner, .alert {
  min-height: 150px;
  display: flex;
  justify-content: center;
  align-items: center;
}
</style>
