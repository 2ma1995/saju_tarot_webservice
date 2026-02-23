// favorite.js
import apiClient from './apiClient';

// 즐겨찾기 추가
export function addFavorite(counselorId) {
    return apiClient.post(`/favorites/${counselorId}`).then(r => r.data);
}

// 즐겨찾기 해제
export function removeFavorite(counselorId) {
    return apiClient.delete(`/favorites/${counselorId}`).then(r => r.data);
}

// 내 즐겨찾기 목록 조회
export function getMyFavorites() {
    return apiClient.get('/favorites').then(r => r.data);
}
