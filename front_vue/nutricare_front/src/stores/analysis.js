import { ref } from 'vue'
import { defineStore } from 'pinia'
import axios from '@/api/axios'

export const useAnalysisStore = defineStore('analysis', () => {
  const user_analysis_results = ref([])
  const user_photos = ref([])
  const user_analysis_result = ref({})
  const user_photo = ref({})

  // 식단 추천 관련 상태
  const diet_recommendations = ref([])
  const diet_loading = ref(false)
  const diet_error = ref(null)


  async function uploadPhoto(file) {
    if (!file) throw new Error('파일이 없습니다.')

    const formData = new FormData()
    formData.append('file', file)

    const response = await axios.post('/user-photos', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })

    const data = response.data
    user_photo.value = data
    
    // 응답 데이터 구조에 따라 분석 결과 매핑
    // 예: { photoId: ..., result: { prob_gunsun: ..., diagnosis_name: ... } }
    if (data.result) {
      user_analysis_result.value = {
        ...data.result, // 상세 확률 정보 병합
        photoId: data.photoId,
        analysisId: data.analysisId,
        diagnosisName: data.result.diagnosis_name || data.diagnosis, // DTO @JsonProperty("diagnosis_name") 대응
      }
    } else {
      // 기존 하위 호환
      user_analysis_result.value = {
        photoId: data.photoId,
        diagnosisName: data.diagnosis,
        analysisId: data.analysisId,
      }
    }

    if (data) {
      user_photos.value = [...user_photos.value, data]
    }
    return data
  }

  async function fetchUserAnalysisResults() {
    const response = await axios.get('/analysis-results')
    user_analysis_results.value = response.data || []
    return user_analysis_results.value
  }

  async function fetchAnalysisResultByPhotoId(photoId) {
    if (!photoId) throw new Error('photoId가 필요합니다.')
    try {
      const response = await axios.get(`/analysis-results/photos/${photoId}`)
      const data = response.data || {}
      
      if (data.result) {
        user_analysis_result.value = {
          ...data.result,
          photoId: data.photoId,
          analysisId: data.analysisId,
          diagnosisName: data.result.diagnosis_name || data.diagnosis
        }
      } else {
        user_analysis_result.value = data
      }
      
      return user_analysis_result.value
    } catch (error) {
      console.error("분석 결과 조회 실패:", error)
      throw error
    }
  }

  async function fetchPhoto(photoId) {
    if (!photoId) return
    try {
      const response = await axios.get(`/user-photos/${photoId}`)
      user_photo.value = response.data || {}
      return user_photo.value
    } catch (error) {
      console.error("사진 조회 실패:", error)
      throw error
    }
  }

  async function fetchUserPhotos() {
    const photoResponse = await axios.get('/user-photos/me')
    const photos = photoResponse.data || []

    // 각 사진에 대한 분석 결과를 병렬로 가져옵니다.
    const photosWithAnalysis = await Promise.all(
      photos.map(async (photo) => {
        try {
          const analysisResponse = await axios.get(`/analysis-results/photos/${photo.photoId}`)
          const analysisData = analysisResponse.data || {}
          
          let resultToMerge = analysisData
          if (analysisData.result) {
            resultToMerge = {
              ...analysisData.result,
              photoId: analysisData.photoId,
              analysisId: analysisData.analysisId,
              diagnosisName: analysisData.result.diagnosis_name || analysisData.diagnosis
            }
          }

          // 사진 객체에 분석 결과를 병합합니다.
          return { ...photo, analysisResult: resultToMerge }
        } catch (error) {
          // 특정 사진의 분석 결과를 가져오지 못하더라도 다른 사진들은 계속 처리합니다.
          console.error(`ID ${photo.photoId}의 분석 결과 로딩 실패:`, error)
          // 분석 결과가 없는 경우를 위해 빈 객체를 할당합니다.
          return { ...photo, analysisResult: {} }
        }
      })
    )

    user_photos.value = photosWithAnalysis
    return photosWithAnalysis
  }

  /**
   * 추천 ID를 기반으로 식단 추천 정보 목록을 가져옵니다.
   * @param {string} recId - 조회할 추천의 ID
   */
  async function fetchDietRecommendationById(recId) {
    if (!recId) {
      diet_error.value = 'recId가 필요합니다.'
      return
    }

    diet_loading.value = true
    diet_error.value = null
    diet_recommendations.value = [] // 배열로 초기화

    try {
      const response = await axios.get(`/diet-recommendations/${recId}`)
      
      // 응답 데이터(배열)를 상태에 저장합니다.
      diet_recommendations.value = response.data || []
      
    } catch (e) {
      console.error(`식단 추천 정보 조회 실패 (recId: ${recId}):`, e)
      diet_error.value = '식단 추천 정보를 불러오는 데 실패했습니다.'
    } finally {
      diet_loading.value = false
    }
  }


  async function fetchRecommendationHeaderByAnalysisId(analysisId) {
    if (!analysisId) return null

    try {
      const response = await axios.get(`/diet-recommendations/analysis/${analysisId}`)
      return response.data
    } catch (e) {
      if (e.response && e.response.status === 404) {
        console.log(`analysisId ${analysisId}에 대한 기존 식단 추천이 없습니다.`)
        return null // 추천이 없으면 null 반환
      }
      // 그 외 다른 에러는 그대로 throw
      console.error('식단 추천 헤더 정보 조회 실패:', e)
      throw e
    }
  }

  async function createAndFetchDietRecommendation({ analysisId, memo }) {
    diet_loading.value = true
    diet_error.value = null
    try {
      // 1. 식단 추천 생성 요청 (recId 받기)
      const createResponse = await axios.post('/diet-recommendations/create', { analysisId, memo })
      const recId = createResponse.data?.recId

      if (!recId) {
        throw new Error("recId를 응답에서 찾을 수 없습니다.")
      }
      
      // 2. AI 식단 생성 요청 (POST /diet-recommendations/{recId})
      // 이것이 없으면 식단이 생성되지 않고 빈 리스트만 조회됩니다.
      // AI 생성은 시간이 오래 걸릴 수 있으므로 타임아웃을 120초로 개별 설정합니다.
      await axios.post(`/diet-recommendations/${recId}`, null, { timeout: 120000 })

      // 3. 받은 recId로 상세 식단 목록 조회
      // fetchDietRecommendationById 액션을 직접 호출하여 로직 재사용
      await fetchDietRecommendationById(recId)

    } catch (e) {
      console.error('식단 추천 생성 또는 조회 실패:', e)
      diet_error.value = '식단 추천을 생성하거나 불러오는 데 실패했습니다.'
      throw e
    } finally {
      diet_loading.value = false
    }
  }


  return {
    user_analysis_results,
    user_photos,
    user_analysis_result,
    user_photo,
    diet_recommendations,
    diet_loading,
    diet_error,
    fetchPhoto,
    uploadPhoto,
    fetchUserAnalysisResults,
    fetchAnalysisResultByPhotoId,
    fetchUserPhotos,
    fetchDietRecommendationById,
    createAndFetchDietRecommendation,
    fetchRecommendationHeaderByAnalysisId, // 새로 추가된 액션
  }
})
