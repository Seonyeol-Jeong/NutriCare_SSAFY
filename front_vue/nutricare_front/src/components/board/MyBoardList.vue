<template>
  <section class="board-list">
    <h2>내 게시글</h2>

    <div v-if="isLoading" class="loading-spinner">
      <div class="spinner-border" role="status">
        <span class="visually-hidden">Loading...</span>
      </div>
    </div>

    <div v-else-if="hasError" class="alert alert-danger" role="alert">
      {{ hasError }}
    </div>

    <template v-else>
      <div class="controls d-flex justify-content-end align-items-center mb-3">
        <div class="d-flex gap-2">
          <select id="category" v-model="categoryFilter" class="form-select form-select-sm" aria-label="카테고리 필터">
            <option value="">전체 카테고리</option>
            <option v-for="cat in categories" :key="cat" :value="cat">{{ cat }}</option>
          </select>
          <select id="sort" v-model="sortKey" class="form-select form-select-sm" aria-label="정렬 순서">
            <option value="created_at_desc">최신순</option>
            <option value="created_at_asc">오래된순</option>
            <option value="views_desc">조회수 높은순</option>
            <option value="views_asc">조회수 낮은순</option>
          </select>
        </div>
      </div>

      <div class="table-wrap">
        <table class="table table-hover">
          <thead>
            <tr>
              <th scope="col">#</th>
              <th scope="col">카테고리</th>
              <th scope="col">제목</th>
              <th scope="col">작성자</th>
              <th scope="col">조회수</th>
              <th scope="col">작성일</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="pagedPosts.length === 0">
              <td colspan="6" class="text-center">작성한 게시글이 없습니다.</td>
            </tr>
            <tr v-for="post in pagedPosts" :key="post.boardId" @click="goDetail(post.boardId)">
              <td>{{ post.boardId }}</td>
              <td>{{ post.category }}</td>
              <td>{{ post.title }}</td>
              <td>{{ post.userName }}</td>
              <td>{{ post.viewCount }}</td>
              <td>{{ formatDate(post.createdAt) }}</td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="pagination-controls">
        <button class="btn btn-sm btn-outline-secondary" :disabled="page === 1" @click="page--">
          ‹
        </button>
        <span v-for="p in totalPages" :key="p">
          <button
            class="btn btn-sm"
            :class="{ 'btn-primary': p === page, 'btn-outline-secondary': p !== page }"
            @click="page = p"
          >
            {{ p }}
          </button>
        </span>
        <button class="btn btn-sm btn-outline-secondary" :disabled="page === totalPages" @click="page++">
          ›
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
const pageSize = 10;

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
  return date.toLocaleDateString('ko-KR');
};

function goDetail(id) {
  router.push({ name: 'boardDetail', params: { id } }).catch(() => {});
}
</script>

<style scoped>
.board-list {
  width: 100%;
  max-width: 900px;
  margin: 0 auto;
  padding: 40px 16px;
}

h2 {
  margin-bottom: 2rem;
  text-align: center;
}

.table-wrap {
  border: 1px solid #dee2e6;
  border-radius: 0.25rem;
  background: #fff;
}

.table {
  margin-bottom: 0;
}

.table tbody tr {
  cursor: pointer;
}

.pagination-controls {
  margin-top: 1.5rem;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 0.5rem;
}

.loading-spinner {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 300px;
}
</style>
