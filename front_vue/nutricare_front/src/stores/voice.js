// src/stores/voice.js
import { defineStore } from 'pinia';
import axios from '@/api/axios'; // 프로젝트에 설정된 axios instance 사용
import { exportWAV } from '@/util/audioUtils'; // 수동 WAV 변환 유틸리티
import { useUserStore } from '@/stores/user'; // useUserStore 추가

// --- 한글/숫자 변환 헬퍼 ---
function convertKoreanNumber(numStr) {
  if (!isNaN(numStr)) {
    return parseInt(numStr, 10);
  }

  const korNum = { '일': 1, '이': 2, '삼': 3, '사': 4, '오': 5, '육': 6, '칠': 7, '팔': 8, '구': 9 };
  const korUnit = { '십': 10, '백': 100, '천': 1000 };

  let result = 0;
  let temp = 0;

  for (const char of numStr) {
    if (korNum[char]) {
      temp = korNum[char];
    } else if (korUnit[char]) {
      if (temp === 0) temp = 1;
      result += temp * korUnit[char];
      temp = 0;
    }
  }
  result += temp;

  return result > 0 ? result : null;
}

// --- 라우트 이름 한글 맵 ---
const routeNameMap = {
  'Home': '메인',
  'pageDescribe': '메인',
  'engineeringDescribe': '기술소개',
  'boardList': '게시판',
  'boardCreate': '게시글 작성',
  'boardDetail': '게시물',
  'boardUpdate': '게시글 수정',
  'analysis': '식단 분석',
  'analysisUpload': '식단 분석',
  'analysisResult': '분석 결과',
  'analysisList': '분석 목록',
  'analysisDetail': '분석 상세',
  'mypage': '마이페이지',
  'userLogin': '로그인',
  'userJoin': '회원가입',
  'userDetail': '내 정보',
  'myBoardList': '내 게시글',
  'updateProfile': '프로필 수정',
  'updatePassword': '비밀번호 변경',
};


// 명령어와 라우트 경로를 매핑합니다.
// **중요**: 구체적인 명령을 일반적인 명령보다 앞에 배치해야 합니다. (예: '게시글 작성' vs '게시판')
const commandRoutes = [
  // Board
  { commands: ['글쓰기', '게시글작성'], route: '/board/create' },
  { commands: ['내게시글', '내가쓴글', '내게식을'], route: '/user/myboards' },
  { commands: ['게시판', '커뮤니티'], route: '/board' },

  // Analysis
  { commands: ['분석업로드', '사진올리기', '업로드'], route: '/analysis/upload' },
  { commands: ['식단추천', '식단분석', '분석'], route: '/analysis' },

  // User
  { commands: ['마이페이지'], route: '/mypage' }, // '내정보' 제거
  { commands: ['로그인'], route: '/user/login' },
  { commands: ['회원가입'], route: '/user/join' },

  // Main
  { commands: ['기술소개', '기술'], route: '/engineeringDescribe' },
  { commands: ['메인', '홈', '처음으로'], route: '/' },
];

export const useVoiceStore = defineStore('voice', {
  state: () => ({
    isRecording: false,
    isProcessing: false,
    audioContext: null,
    processor: null,
    stream: null,
    audioChunks: [],
    resultText: '',
  }),
  actions: {
    // --- TTS Actions ---
    speak(text) {
      if (!window.speechSynthesis) return;
      // 진행 중인 다른 음성 출력을 취소
      window.speechSynthesis.cancel(); 
      
      const utterance = new SpeechSynthesisUtterance(text);
      utterance.lang = 'ko-KR';
      utterance.rate = 1.0;
      window.speechSynthesis.speak(utterance);
    },
    cancelSpeak() {
      if (!window.speechSynthesis) return;
      window.speechSynthesis.cancel();
    },

    // --- Recording Actions ---
    async startRecording() {
      try {
        this.audioContext = new (window.AudioContext || window.webkitAudioContext)({ sampleRate: 48000 });
        this.stream = await navigator.mediaDevices.getUserMedia({ audio: true });

        const source = this.audioContext.createMediaStreamSource(this.stream);
        this.processor = this.audioContext.createScriptProcessor(4096, 2, 1); // 2 in, 1 out

        this.audioChunks = []; // 버퍼 초기화

        this.processor.onaudioprocess = (e) => {
          if (!this.isRecording) return;
          const inputData = e.inputBuffer.getChannelData(0);
          this.audioChunks.push(new Float32Array(inputData));
        };

        source.connect(this.processor);
        this.processor.connect(this.audioContext.destination);

        this.isRecording = true;
        this.resultText = '듣고 있어요...';
      } catch (err) {
        console.error('마이크 권한 오류:', err);
        alert('마이크 사용 권한이 필요합니다.');
      }
    },

    async stopRecording(router) {
      if (!this.isRecording) return;
      this.isRecording = false;
      this.isProcessing = true;

      if (this.stream) this.stream.getTracks().forEach(track => track.stop());
      if (this.processor) this.processor.disconnect();
      if (this.audioContext && this.audioContext.state !== 'closed') this.audioContext.close();

      if (this.audioChunks.length === 0) {
        this.resultText = '녹음이 너무 짧습니다.';
        this.isProcessing = false;
        return;
      }

      this.resultText = '분석 중...';

      try {
        const totalLength = this.audioChunks.reduce((acc, chunk) => acc + chunk.length, 0);
        const tempContext = new (window.AudioContext || window.webkitAudioContext)({ sampleRate: 48000 });
        const audioBuffer = tempContext.createBuffer(1, totalLength, 48000);
        const channelData = audioBuffer.getChannelData(0);

        let offset = 0;
        for (const chunk of this.audioChunks) {
          channelData.set(chunk, offset);
          offset += chunk.length;
        }

        const wavBlob = exportWAV(audioBuffer);
        const formData = new FormData();
        formData.append('file', wavBlob, 'command.wav');

        const res = await axios.post('/voice/command', formData, {
          headers: { 'Content-Type': 'multipart/form-data' }
        });

        const commandText = res.data.text;
        this.resultText = `인식됨: "${commandText}"`;

        this.executeCommand(commandText, router);

      } catch (err) {
        console.error('음성 처리 또는 API 요청 에러:', err);
        this.resultText = '다시 말씀해 주세요.';
      } finally {
        this.isProcessing = false;
      }
    },

    async executeCommand(text, router) {
      if (!text) return;

      const command = text.replace(/\s+/g, '');
      const currentRouteName = router.currentRoute.value.name;

      // --- 게시물 번호로 이동하는 동적 라우팅 (특정 페이지에서만 활성화) ---
      if (currentRouteName === 'boardList' || currentRouteName === 'myBoardList') {
        const postMatch = command.match(/^([\d일이삼사오육칠팔구십백천]+)번?$/);
        if (postMatch && postMatch[1]) {
          const numberWord = postMatch[1];
          const postId = convertKoreanNumber(numberWord);
          if (postId) {
            const msg = `${postId}번 게시물로 이동합니다.`;
            this.resultText = msg;
            this.speak(msg);
            await router.push({ path: `/board/detail/${postId}`, query: { speak: 'true' } });
            return;
          }
        }
      }
      // -----------------------------------------------------------------

      // --- '내정보' 명령 처리 (로그인 상태에 따라 UserDetail 페이지로 이동) ---
      if (command.includes('내정보')) {
        const userStore = useUserStore();
        if (userStore.isLoggedIn && userStore.userId) {
          const msg = "내 정보 페이지로 이동합니다.";
          this.resultText = msg;
          this.speak(msg);
          await router.push(`/user/detail/${userStore.userId}`);
          return;
        } else {
          const msg = "내 정보는 로그인 후 이용 가능합니다. 로그인 페이지로 이동합니다.";
          this.resultText = msg;
          this.speak(msg);
          await router.push({ name: 'userLogin' });
          return;
        }
      }
      // ---------------------------------------------------------------------

      if (command.includes('뒤로')) {
        this.speak('이전 페이지로 이동했습니다.');
        router.go(-1);
        return;
      }

      if (command.includes('앞으로')) {
        this.speak('다음 페이지로 이동했습니다.');
        router.go(1);
        return;
      }

      if (command.includes('내려') || command.includes('아래로')) {
        this.speak('아래로 스크롤합니다.');
        window.scrollBy({ top: 500, behavior: 'smooth' });
        this.resultText = '아래로 스크롤합니다.';
        return;
      }

      if (command.includes('올려') || command.includes('위로')) {
        this.speak('위로 스크롤합니다.');
        window.scrollBy({ top: -500, behavior: 'smooth' });
        this.resultText = '위로 스크롤합니다.';
        return;
      }

      if (command.includes('맨위') || command.includes('상단')) {
        this.speak('페이지 맨 위로 이동합니다.');
        window.scrollTo({ top: 0, behavior: 'smooth' });
        return;
      }

      for (const routeInfo of commandRoutes) {
        for (const cmd of routeInfo.commands) {
          if (command.includes(cmd)) {
            await router.push(routeInfo.route);
            const destinationRouteName = router.currentRoute.value.name;
            const pageName = routeNameMap[destinationRouteName] || destinationRouteName;
            const msg = `${pageName} 페이지로 이동했습니다.`;
            this.speak(msg);
            return;
          }
        }
      }

      // 일치하는 명령어가 없는 경우
      const failMsg = "명령을 이해하지 못했습니다.";
      this.resultText = failMsg;
      this.speak(failMsg);
    }
  }
});