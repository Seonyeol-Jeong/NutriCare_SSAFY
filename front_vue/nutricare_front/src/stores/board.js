import { defineStore } from 'pinia';
import axios from '@/api/axios'; // 커스텀 axios 인스턴스 임포트

export const useBoardStore = defineStore('board', {
  state: () => ({
    boards: [],
    board: null,
    myBoards: [],
    loading: false,
    error: null,
  }),
  getters: {
    allBoards: (state) => state.boards,
    getBoardById: (state) => state.board,
    myBoardList: (state) => state.myBoards,
    isLoading: (state) => state.loading,
    hasError: (state) => state.error,
  },
  actions: {
    // 모든 게시글 조회
    async fetchBoards() {
      this.loading = true;
      this.error = null;
      this.boards = []; // Clear state before fetching
      try {
        const response = await axios.get('/boards');
        if (response.status === 204) {
          this.boards = [];
        } else {
          this.boards = response.data;
        }
      } catch (error) {
        console.error('Error fetching boards:', error);
        this.error = '게시글을 불러오는 데 실패했습니다.';
      } finally {
        this.loading = false;
      }
    },

    // ID로 특정 게시글 조회
    async fetchBoardById(id) {
      this.loading = true;
      this.error = null;
      this.board = null; // 이전 데이터 초기화
      try {
        const response = await axios.get(`/boards/${id}`);
        if (response.status === 204) {
          this.board = null;
        } else {
          this.board = response.data;
        }
      } catch (error) {
        console.error(`Error fetching board ${id}:`, error);
        this.error = '게시글을 불러오는 데 실패했습니다.';
      } finally {
        this.loading = false;
      }
    },

    // 내 게시글 목록 조회
    async fetchMyBoards() {
      this.loading = true;
      this.error = null;
      this.myBoards = []; // Clear state before fetching
      try {
        const response = await axios.get('/boards/me');
        if (response.status === 204) {
          this.myBoards = [];
        } else {
          this.myBoards = response.data;
        }
      } catch (error) {
        console.error('Error fetching my boards:', error);
        this.error = '내 게시글을 불러오는 데 실패했습니다.';
      } finally {
        this.loading = false;
      }
    },

    // 게시글 생성
    async createBoard(boardData) {
      this.loading = true;
      this.error = null;
      try {
        const response = await axios.post('/boards', boardData);
        // 성공 시, 생성된 게시글 데이터를 반환하여 컴포넌트에서 boardId를 사용할 수 있게 함
        return response.data;
      } catch (error) {
        console.error('Error creating board:', error);
        this.error = '게시글 생성에 실패했습니다.';
        throw error;
      } finally {
        this.loading = false;
      }
    },

    // 게시글 이미지 업로드
    async uploadBoardImages({ boardId, files }) {
      if (!files || files.length === 0) {
        return; // 업로드할 파일이 없으면 종료
      }
      this.loading = true;
      this.error = null;
      
      const formData = new FormData();
      formData.append('boardId', boardId);
      files.forEach(file => {
        formData.append('file', file);
      });

      try {
        await axios.post('/board-images', formData, {
          headers: { 'Content-Type': 'multipart/form-data' }
        });
      } catch (error) {
        console.error('Error uploading board images:', error);
        this.error = '이미지 업로드에 실패했습니다.';
        throw error; 
      } finally {
        this.loading = false;
      }
    },

    // 게시글 수정
    async updateBoard(id, boardData) {
      this.loading = true;
      this.error = null;
      try {
        const response = await axios.put(`/boards/${id}`, boardData);
        // 성공 시, 해당 게시글 상세 정보 새로고침
        await this.fetchBoardById(id);
        return response;
      } catch (error) {
        console.error(`Error updating board ${id}:`, error);
        this.error = '게시글 수정에 실패했습니다.';
        throw error;
      } finally {
        this.loading = false;
      }
    },

    // 게시글 삭제
    async deleteBoard(id) {
      this.loading = true;
      this.error = null;
      try {
        const response = await axios.delete(`/boards/${id}`);
        // 성공 시, 목록 새로고침
        await this.fetchBoards();
        return response;
      } catch (error) {
        console.error(`Error deleting board ${id}:`, error);
        this.error = '게시글 삭제에 실패했습니다.';
        throw error;
      } finally {
        this.loading = false;
      }
    },
  },
});
