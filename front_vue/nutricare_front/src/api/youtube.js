import axios from 'axios';

// 환경 변수에서 API 키를 가져옵니다. 
// .env 파일에 VITE_YOUTUBE_API_KEY=자신의_API_KEY 형식으로 저장되어 있어야 합니다.
const API_KEY = import.meta.env.VITE_YOUTUBE_API_KEY; 

export async function searchRecipeVideo(menuName) {
  if (!API_KEY) {
    console.warn("YouTube API Key가 설정되지 않았습니다. .env 파일에 VITE_YOUTUBE_API_KEY를 설정해주세요.");
    return null;
  }

  try {
    const response = await axios.get('https://www.googleapis.com/youtube/v3/search', {
      params: {
        part: 'snippet',
        q: `${menuName} 레시피`, // 검색어
        key: API_KEY,
        maxResults: 1, // 가장 관련성 높은 1개만
        type: 'video',
      }
    });

    const items = response.data.items;
    if (items && items.length > 0) {
      const videoId = items[0].id.videoId;

      // 2. 비디오 상세 정보(통계) 조회
      const statsResponse = await axios.get('https://www.googleapis.com/youtube/v3/videos', {
        params: {
          part: 'snippet,statistics',
          id: videoId,
          key: API_KEY,
        }
      });

      const videoDetails = statsResponse.data.items[0];

      return {
        videoId: videoId,
        title: videoDetails.snippet.title,
        thumbnailUrl: videoDetails.snippet.thumbnails.high.url,
        embedUrl: `https://www.youtube.com/embed/${videoId}`, // 임베드용 URL
        viewCount: videoDetails.statistics.viewCount,
        likeCount: videoDetails.statistics.likeCount,
      };
    }
    return null;
  } catch (error) {
    console.error(`YouTube 검색 실패 (${menuName}):`, error);
    return null;
  }
}
