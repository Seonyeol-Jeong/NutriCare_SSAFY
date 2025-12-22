<template>
  <section class="board-container">
    <header class="board-header">
      <h2 class="title">나의 활동</h2>
      <p class="subtitle">내가 작성한 게시글을 한곳에서 관리하세요.</p>
    </header>

    <div v-if="isLoading" class="loading-area">
      <div class="spinner"></div>
    </div>

    <div v-else-if="hasError" class="error-msg">
      ⚠️ {{ hasError }}
    </div>

    <template v-else>
      <div class="toolbar right-aligned">
        <div class="filters">
          <select v-model="categoryFilter" class="custom-select">
            <option value="">전체 카테고리</option>
            <option v-for="cat in categories" :key="cat" :value="cat">{{ cat }}</option>
          </select>
          <select v-model="sortKey" class="custom-select">
            <option value="created_at_desc">최신순</option>
            <option value="created_at_asc">오래된순</option>
            <option value="views_desc">인기순</option>
          </select>
        </div>
      </div>

      <div class="post-list">
        <div v-if="pagedPosts.length === 0" class="empty-state">
          <p>아직 작성한 게시글이 없습니다. 첫 글을 작성해보세요!</p>
        </div>
        
        <article 
          v-for="post in pagedPosts" 
          :key="post.boardId" 
          class="post-card" 
          @click="goDetail(post.boardId)"
        >
          <div class="card-content">
            <div class="card-header-row">
              <span class="category-badge">{{ post.category }}</span>
              <span class="date">{{ formatDate(post.createdAt) }}</span>
            </div>
            <h3 class="post-title">{{ post.title }}</h3>
            <div class="card-footer-row">
              <span class="author">by Me</span>
              <div class="meta-stats">
                <span>👁️ {{ post.viewCount }}</span>
              </div>
            </div>
          </div>
        </article>
      </div>

      <!-- 페이지네이션 -->
      <div class="pagination">
        <button 
          class="page-btn nav-btn" 
          :disabled="page === 1" 
          @click="page--"
        >
          &lt;
        </button>
        <button
          v-for="p in totalPages"
          :key="p"
          class="page-btn"
          :class="{ active: p === page }"
          @click="page = p"
        >
          {{ p }}
        </button>
        <button 
          class="page-btn nav-btn" 
          :disabled="page === totalPages" 
          @click="page++"
        >
          &gt;
        </button>
      </div>
    </template>
  </section>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useBoardStore } from '@/stores/board';
import { storeToRefs } from 'pinia';

const router = useRouter();
const boardStore = useBoardStore();

const { myBoards, isLoading, hasError } = storeToRefs(boardStore);

onMounted(() => {
  boardStore.fetchMyBoards();
});

const categories = computed(() => [...new Set(myBoards.value.map((p) => p.category).filter(Boolean))]);
const categoryFilter = ref('');
const sortKey = ref('created_at_desc');

const page = ref(1);
const pageSize = 8;

const filteredSortedPosts = computed(() => {
  let arr = [...myBoards.value];
  if (categoryFilter.value) {
    arr = arr.filter((p) => p.category === categoryFilter.value);
  }
  switch (sortKey.value) {
    case 'views_desc':
      arr.sort((a, b) => b.viewCount - a.viewCount);
      break;
    case 'views_asc':
      arr.sort((a, b) => a.viewCount - b.viewCount);
      break;
    case 'created_at_asc':
      arr.sort((a, b) => new Date(a.createdAt) - new Date(b.createdAt));
      break;
    case 'created_at_desc':
    default:
      arr.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
      break;
  }
  return arr;
});

const totalPages = computed(() => Math.ceil(filteredSortedPosts.value.length / pageSize) || 1);

const pagedPosts = computed(() => {
  const start = (page.value - 1) * pageSize;
  return filteredSortedPosts.value.slice(start, start + pageSize);
});

const formatDate = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleDateString('ko-KR', { month: 'long', day: 'numeric' });
};

function goDetail(id) {
  router.push({ name: 'boardDetail', params: { id } }).catch(() => {});
}
</script>

<style scoped>
.board-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 40px 20px;
  background-color: #f8f9fa;
  min-height: 80vh;
}

.board-header {
  text-align: center;
  margin-bottom: 40px;
}

.title {
  font-size: 32px;
  font-weight: 800;
  color: #333;
  margin-bottom: 8px;
}

.subtitle {
  color: #666;
  font-size: 16px;
}

/* Toolbar */
.toolbar {
  display: flex;
  margin-bottom: 24px;
}

.toolbar.right-aligned {
  justify-content: flex-end;
}

.filters {
  display: flex;
  gap: 12px;
}

.custom-select {
  padding: 8px 16px;
  border: 1px solid #ddd;
  border-radius: 8px;
  background-color: white;
  color: #555;
  outline: none;
  cursor: pointer;
  font-size: 14px;
}

.custom-select:focus {
  border-color: #6b55c7;
}

/* Post List */
.post-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.post-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  cursor: pointer;
  transition: all 0.2s ease;
  border: 1px solid transparent;
}

.post-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.08);
  border-color: #eaddff;
}

.card-header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.category-badge {
  background-color: #f3f0ff;
  color: #6b55c7;
  padding: 4px 12px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 700;
}

.date {
  font-size: 13px;
  color: #999;
}

.post-title {
  margin: 0 0 16px 0;
  font-size: 18px;
  font-weight: 700;
  color: #2c3e50;
  line-height: 1.4;
}

.card-footer-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
  color: #666;
}

.meta-stats {
  display: flex;
  gap: 12px;
}

.empty-state {
  text-align: center;
  padding: 60px;
  background: white;
  border-radius: 12px;
  color: #888;
}

/* Pagination */
.pagination {
  display: flex;
  justify-content: center;
  gap: 8px;
  margin-top: 40px;
}

.page-btn {
  width: 36px;
  height: 36px;
  border: none;
  background: white;
  border-radius: 8px;
  color: #555;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.2s;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
}

.page-btn:hover:not(:disabled) {
  background: #f3f0ff;
  color: #6b55c7;
}

.page-btn.active {
  background: #6b55c7;
  color: white;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* Loading */
.loading-area {
  display: flex;
  justify-content: center;
  padding: 60px;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #6b55c7;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

@media (max-width: 600px) {
  .filters {
    justify-content: space-between;
    width: 100%;
  }
}
</style>
