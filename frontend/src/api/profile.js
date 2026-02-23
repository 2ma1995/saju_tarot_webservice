// profile.js
import apiClient from './apiClient';

// 프로필 등록/수정
export function updateProfile(data) {
    return apiClient.put('/counselors/profile', data).then(r => r.data);
}

// 프로필 조회
export function getProfile(counselorId) {
    return apiClient.get(`/counselors/profile/${counselorId}`).then(r => r.data);
}

// 프로필 이미지 업로드
export function uploadProfileImage(file) {
    const formData = new FormData();
    formData.append('file', file);
    return apiClient.post('/counselors/profile/image', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
    }).then(r => r.data);
}

// 태그로 상담사 검색
export function searchByTag(tag) {
    return apiClient.get('/counselors/profile/search', { params: { tag } }).then(r => r.data);
}
