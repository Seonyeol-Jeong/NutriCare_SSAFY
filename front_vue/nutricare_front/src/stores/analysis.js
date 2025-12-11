import { ref } from 'vue'
import { defineStore } from 'pinia'
import axios from '@/api/axios'

export const useAnalysisStore = defineStore('analysis', () => {
  const user_analysis_results = ref([])
  const user_diet_recommendations = ref([])
  const user_photos = ref([])

  const user_analysis_result = ref({})
  const user_diet_recommendation = ref({})
  const user_photo = ref({})

  async function uploadPhoto(file) {
    if (!file) throw new Error('파일이 없습니다.')

    const formData = new FormData()
    formData.append('file', file)

    const response = await axios.post('/user-photos', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })

    const data = response.data
    user_photo.value = data
    user_analysis_result.value = {
      photoId: data.photoId,
      diagnosis: data.diagnosis,
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
      // 응답이 없으면 빈 객체 (204 No Content 대비)
      user_analysis_result.value = response.data || {}
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
    const response = await axios.get('/user-photos/me')
    user_photos.value = response.data || []
    return user_photos.value
  }

  return {
    user_analysis_results,
    user_diet_recommendations,
    user_photos,
    user_analysis_result,
    user_diet_recommendation,
    user_photo,
    fetchPhoto,
    uploadPhoto,
    fetchUserAnalysisResults,
    fetchAnalysisResultByPhotoId,
    fetchUserPhotos,
  }
})
