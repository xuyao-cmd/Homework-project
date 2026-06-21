import request from './request'

export function listNovels(params) {
  return request.get('/api/novels', { params })
}

export function recommendNovels() {
  return request.get('/api/novels/recommend')
}

export function getNovel(id) {
  return request.get(`/api/novels/${id}`)
}

export function getChapters(id) {
  return request.get(`/api/novels/${id}/chapters`)
}

export function readChapter(novelId, chapterId) {
  return request.get(`/api/novels/${novelId}/chapters/${chapterId}`)
}

export function publishNovel(data, adminUsername) {
  return request.post('/api/novels/publish', data, { params: { adminUsername } })
}

export function submitNovel(data) {
  return request.post('/api/novels/submit', data)
}

export function listPendingNovels(adminUsername) {
  return request.get('/api/novels/pending', { params: { adminUsername } })
}

export function approveNovel(id, adminUsername) {
  return request.post(`/api/novels/${id}/approve`, null, { params: { adminUsername } })
}
