<template>
  <section class="board-detail-container">
    <div v-if="isLoading" class="loading-area">
      <div class="spinner-border text-primary" role="status"></div>
    </div>

    <div v-else-if="hasError" class="alert-box error">
      {{ hasError }}
    </div>

    <div v-else-if="!board" class="alert-box info">
      게시글을 찾을 수 없습니다.
    </div>

    <template v-else>
      <!-- 게시글 본문 영역 -->
      <article class="post-card">
        <header class="post-header">
          <span class="category-badge">{{ board.category }}</span>
          <h1 class="post-title">{{ board.title }}</h1>
          <div class="post-meta">
            <div class="author-info">
              <span class="author-name">{{ board.userName }}</span>
            </div>
            <div class="meta-divider"></div>
            <span class="meta-date">{{ formatDate(board.createdAt) }}</span>
            <div class="meta-divider"></div>
            <span class="meta-views">조회 {{ board.viewCount }}</span>
          </div>
        </header>

        <div class="post-content" v-html="board.content"></div>
        
        <!-- 첨부 이미지 갤러리 (본문 외 별도 첨부) -->
        <div class="image-gallery" v-if="board.images && board.images.length">
          <template v-for="img in board.images" :key="img.imageId">
            <div class="gallery-item" v-if="img.imageUrl">
              <img :src="img.imageUrl" :alt="getFileNameFromUrl(img.imageUrl)" @error="handleImageError" />
            </div>
          </template>
        </div>

        <div class="post-footer">
          <div class="action-buttons">
            <button class="btn-list" @click="goList">
              <i class="bi bi-list"></i> 목록
            </button>
            <div v-if="isAuthor" class="author-actions">
              <button class="btn-edit" @click="editBoard">수정</button>
              <button class="btn-delete" @click="deleteConfirm">삭제</button>
            </div>
          </div>
        </div>
      </article>

      <!-- 댓글 영역 -->
      <section class="comments-section">
        <h3 class="comments-title">댓글 <span class="count">{{ comments.length }}</span></h3>
        
        <!-- 댓글 작성 폼 -->
        <form @submit.prevent="handleCommentSubmit" class="comment-form">
          <div class="input-wrapper">
            <textarea
              v-model="newCommentContent"
              placeholder="따뜻한 댓글을 남겨주세요..."
              rows="1"
              @input="autoResize"
              required
            ></textarea>
            <button type="submit" class="btn-submit" :disabled="!newCommentContent.trim()">등록</button>
          </div>
        </form>

        <!-- 댓글 목록 -->
        <div v-if="isCommentLoading" class="loading-comments">
          <div class="spinner-border spinner-border-sm text-secondary" role="status"></div>
        </div>
        
        <ul v-else class="comment-list">
          <li v-if="comments.length === 0" class="empty-comments">
            첫 번째 댓글을 남겨보세요!
          </li>
          <li v-for="comment in comments" :key="comment.commentId" class="comment-item">
            <div class="comment-header">
              <span class="comment-author">{{ comment.userName }}</span>
              <span class="comment-date">{{ formatDate(comment.createdAt) }}</span>
            </div>
            
            <div class="comment-body">
              <div v-if="editingCommentId !== comment.commentId" class="comment-text">
                {{ comment.content }}
              </div>
              
              <!-- 댓글 수정 폼 -->
              <div v-else class="comment-edit-form">
                <textarea v-model="editingCommentContent" rows="2"></textarea>
                <div class="edit-actions">
                  <button class="btn-cancel" @click="toggleEditMode(null)">취소</button>
                  <button class="btn-save" @click="handleCommentUpdate(comment)">저장</button>
                </div>
              </div>
            </div>

            <div v-if="userStore.userInfo?.userId === comment.userId && editingCommentId !== comment.commentId" class="comment-actions">
              <button @click="toggleEditMode(comment)">수정</button>
              <button @click="handleCommentDelete(comment.commentId)" class="text-danger">삭제</button>
            </div>
          </li>
        </ul>
      </section>
    </template>
  </section>
</template>

<script setup>
import { ref, onMounted, computed, watch, onUnmounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useBoardStore } from '@/stores/board';
import { useUserStore } from '@/stores/user';
import { useCommentStore } from '@/stores/comment';
import { useVoiceStore } from '@/stores/voice';
import { storeToRefs } from 'pinia';

const router = useRouter();
const route = useRoute();
const boardStore = useBoardStore();
const userStore = useUserStore();
const commentStore = useCommentStore();
const voiceStore = useVoiceStore();

const { board, isLoading, hasError } = storeToRefs(boardStore);
const { comments, isLoading: isCommentLoading } = storeToRefs(commentStore);
const { userInfo } = storeToRefs(userStore);

const boardId = computed(() => route.params.id);

const newCommentContent = ref('');
const editingCommentId = ref(null);
const editingCommentContent = ref('');

const handleImageError = (e) => {
  e.target.style.display = 'none';
};

const getFileNameFromUrl = (url) => {
  if (!url) return '이미지';
  const parts = url.split('/');
  return parts[parts.length - 1];
};

// textarea 자동 높이 조절
const autoResize = (e) => {
  e.target.style.height = 'auto';
  e.target.style.height = e.target.scrollHeight + 'px';
};

// --- TTS Feature ---
watch(board, (newBoard) => {
  if (newBoard && newBoard.title && newBoard.content && route.query.speak === 'true') {
    const contentText = newBoard.content.replace(/<[^>]*>?/gm, '');
    const textToRead = `제목: ${newBoard.title}. 내용: ${contentText}`;
    voiceStore.speak(textToRead);
  }
});

onUnmounted(() => {
  voiceStore.cancelSpeak();
});

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
  return date.toLocaleDateString('ko-KR', { year: 'numeric', month: 'long', day: 'numeric', hour: '2-digit', minute:'2-digit' });
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
      userName: userInfo.value.name,
    });
    newCommentContent.value = '';
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
  }
  else {
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
    toggleEditMode(null);
  } catch (error) {
    alert(error.message);
  }
}
</script>

<style scoped>
.board-detail-container {
  max-width: 840px;
  margin: 40px auto;
  padding: 0 20px 60px;
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, system-ui, Roboto, sans-serif;
  color: #333;
}

/* Loading & Alert */
.loading-area, .alert-box {
  min-height: 200px;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}
.alert-box.error { color: #e74c3c; }
.alert-box.info { color: #666; }

/* Post Card */
.post-card {
  background: #fff;
  border-radius: 20px;
  box-shadow: 0 10px 30px rgba(0,0,0,0.04);
  padding: 40px;
  margin-bottom: 40px;
  border: 1px solid rgba(0,0,0,0.02);
}

/* Header */
.post-header {
  text-align: center;
  margin-bottom: 40px;
  border-bottom: 1px solid #f0f0f0;
  padding-bottom: 30px;
}

.category-badge {
  display: inline-block;
  background-color: #f3f0ff;
  color: #6b55c7;
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 700;
  margin-bottom: 16px;
}

.post-title {
  font-size: 28px;
  font-weight: 800;
  color: #222;
  margin-bottom: 20px;
  line-height: 1.4;
  word-break: keep-all;
}

.post-meta {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px;
  font-size: 14px;
  color: #888;
}

.author-name {
  font-weight: 600;
  color: #555;
}

.meta-divider {
  width: 3px;
  height: 3px;
  background-color: #ddd;
  border-radius: 50%;
}

/* Content */
.post-content {
  font-size: 16px;
  line-height: 1.8;
  color: #444;
  margin-bottom: 40px;
  min-height: 100px;
  white-space: pre-wrap; /* 줄바꿈 보존 */
}

/* Deep selector for v-html content images */
.post-content :deep(img) {
  /* 일반 본문 이미지는 기존 스타일 유지하되, 분석 카드 내 이미지는 제외해야 함 */
  max-width: 100%;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
  margin: 20px 0;
}

/* 분석 카드 스타일 재정의 (가로형 레이아웃 보장) */
.post-content :deep(.analysis-card-preview) {
  /* 부모의 white-space: pre-wrap 상속 차단 -> 정상적인 HTML 레이아웃 동작 */
  white-space: normal !important;
  
  /* Flex 및 크기 강제 */
  width: 100% !important;
  max-width: 760px !important; 
  box-sizing: border-box !important;
  margin: 0 auto 32px !important;
  
  display: flex !important;
  flex-direction: row !important;
  align-items: stretch !important;
  gap: 16px !important;
}

/* 분석 카드 내부 이미지 스타일 강제 초기화 */
.post-content :deep(.analysis-card-preview img) {
  /* 기존 게시글 이미지 스타일 무력화 */
  margin: 0 !important; 
  box-shadow: none !important;
  border-radius: 0 !important;
  
  /* 120px 박스 안에 꽉 차게 */
  width: 100% !important;
  height: 100% !important;
  min-height: 100% !important;
  max-width: none !important; /* 부모 크기 제한 무시 방지 */
  object-fit: cover !important;
}

/* 이미지 컨테이너 (첫 번째 자식 div) 강제 스타일 */
.post-content :deep(.analysis-card-preview > div:first-child) {
  flex: 0 0 120px !important;
  width: 120px !important;
  height: 120px !important; /* 높이 고정 */
  min-width: 120px !important;
  overflow: hidden !important;
}

/* 모바일 대응 */
@media (max-width: 600px) {
  .post-content :deep(.analysis-card-preview) {
    flex-direction: column !important;
    align-items: flex-start !important;
  }
  
  .post-content :deep(.analysis-card-preview > div:first-child) {
    width: 100% !important;
    height: 180px !important;
    margin-bottom: 16px !important;
  }
}

/* Image Gallery */
.image-gallery {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
  margin-top: 30px;
}

.gallery-item img {
  width: 100%;
  height: 200px;
  object-fit: cover;
  border-radius: 12px;
  border: 1px solid #eee;
  transition: transform 0.2s;
}

.gallery-item img:hover {
  transform: scale(1.02);
}

/* Footer & Actions */
.post-footer {
  margin-top: 50px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

.action-buttons {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.btn-list, .btn-edit, .btn-delete {
  padding: 10px 20px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  border: none;
  transition: all 0.2s;
}

.btn-list {
  background-color: #f8f9fa;
  color: #555;
}
.btn-list:hover { background-color: #e9ecef; }

.author-actions {
  display: flex;
  gap: 10px;
}

.btn-edit {
  background-color: #eaddff;
  color: #6b55c7;
}
.btn-edit:hover { background-color: #d0c4f3; }

.btn-delete {
  background-color: #ffeaea;
  color: #e74c3c;
}
.btn-delete:hover { background-color: #fadbd8; }


/* Comments Section */
.comments-section {
  max-width: 760px;
  margin: 0 auto;
}

.comments-title {
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 20px;
  color: #333;
}

.comments-title .count {
  color: #6b55c7;
  margin-left: 4px;
}

.comment-form {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
  margin-bottom: 30px;
  border: 1px solid #eee;
}

.input-wrapper {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.comment-form textarea {
  flex: 1;
  border: none;
  background: transparent;
  resize: none;
  padding: 8px;
  outline: none;
  font-size: 14px;
  min-height: 40px;
}

.btn-submit {
  background-color: #6b55c7;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  flex-shrink: 0;
}
.btn-submit:disabled {
  background-color: #d1c8f0;
  cursor: not-allowed;
}

.comment-list {
  list-style: none;
  padding: 0;
}

.empty-comments {
  text-align: center;
  color: #999;
  padding: 30px 0;
  font-size: 14px;
}

.comment-item {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 16px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.02);
  border: 1px solid #f5f5f5;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 13px;
}

.comment-author {
  font-weight: 700;
  color: #333;
}

.comment-date {
  color: #999;
}

.comment-text {
  font-size: 14px;
  line-height: 1.6;
  color: #555;
  white-space: pre-wrap;
}

.comment-edit-form textarea {
  width: 100%;
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 10px;
  margin-bottom: 8px;
  font-size: 14px;
  resize: vertical;
}

.edit-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.btn-save, .btn-cancel {
  padding: 6px 12px;
  border-radius: 6px;
  font-size: 12px;
  cursor: pointer;
  border: none;
}
.btn-save { background: #6b55c7; color: white; } 
.btn-cancel { background: #f1f3f5; color: #555; }

.comment-actions {
  margin-top: 12px;
  display: flex;
  gap: 12px;
  font-size: 12px;
}

.comment-actions button {
  background: none;
  border: none;
  padding: 0;
  color: #888;
  cursor: pointer;
  text-decoration: underline;
}
.comment-actions button.text-danger { color: #e74c3c; }

@media (max-width: 600px) {
  .post-card { padding: 24px; }
  .post-title { font-size: 22px; }
  .image-gallery { grid-template-columns: 1fr; }
}
</style>
