import request from './request'

export function login(data) {
  return request.post('/api/users/login', data)
}

export function register(data) {
  return request.post('/api/users/register', data)
}

export function getProfile(userId) {
  return request.get('/api/users/profile', { params: { userId } })
}

export function updateProfile(userId, data) {
  return request.put(`/api/users/profile/${userId}`, data)
}
