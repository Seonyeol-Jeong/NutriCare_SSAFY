import { defineStore } from 'pinia';
import axios from '@/api/axios';

export const useCommentStore = defineStore('comment', {
  state: () => ({
    comments: [],
    loading: false,
    error: null,
  }),
  getters: {
    commentsForBoard: (state) => state.comments,
    isLoading: (state) => state.loading,
    hasError: (state) => state.error,
  },
  actions: {
    // 특정 게시글의 댓글 목록 조회
    async fetchComments(boardId) {
      if (!boardId) return;
      this.loading = true;
      this.error = null;
      try {
        const response = await axios.get(`/boards/${boardId}/comments`);
        if (response.status === 204) {
            this.comments = [];
        } else {
            this.comments = response.data;
        }
      } catch (error) {
        console.error(`Error fetching comments for board ${boardId}:`, error);
        this.error = '댓글을 불러오는 데 실패했습니다.';
      } finally {
        this.loading = false;
      }
    },

    // 댓글 작성
    async createComment({ boardId, content, userName }) {
      if (!boardId || !content || !userName) return; // userName 유효성 검사 추가
      this.loading = true; // 개별 액션 로딩 상태는 다르게 관리할 수도 있음
      try {
        await axios.post(`/boards/${boardId}/comments`, { content, userName }); // userName 포함
        await this.fetchComments(boardId); // 댓글 작성 후 목록 새로고침
      } catch (error) {
        console.error('Error creating comment:', error);
        throw new Error('댓글 작성에 실패했습니다.');
      } finally {
        this.loading = false;
      }
    },

    // 댓글 수정
    async updateComment({ boardId, commentId, content }) {
      this.loading = true;
      try {
        await axios.put(`/comments/${commentId}`, { content });
        await this.fetchComments(boardId); // 댓글 수정 후 목록 새로고침
      } catch (error) {
        console.error('Error updating comment:', error);
        throw new Error('댓글 수정에 실패했습니다.');
      } finally {
        this.loading = false;
      }
    },

    // 댓글 삭제
    async deleteComment({ boardId, commentId }) {
      this.loading = true;
      try {
        await axios.delete(`/comments/${commentId}`);
        await this.fetchComments(boardId); // 댓글 삭제 후 목록 새로고침
      } catch (error) {
        console.error('Error deleting comment:', error);
        throw new Error('댓글 삭제에 실패했습니다.');
      } finally {
        this.loading = false;
      }
    },
  },
});
