// review.js
import apiClient from './apiClient';

// 후기 작성
export function createReview(data) {
    return apiClient.post('/reviews', data).then(r => r.data);
}

// 내 후기 조회
export function getMyReviews() {
    return apiClient.get('/reviews/my').then(r => r.data);
}

// 상담사별 후기 조회
export function getReviewsByCounselor(counselorId, page = 0, size = 10) {
    return apiClient.get(`/reviews/counselor/${counselorId}`, { params: { page, size } }).then(r => r.data);
}

// 전체 후기 조회 (관리자용)
export function getAllReviews() {
    return apiClient.get('/reviews').then(r => r.data);
}

// 후기 삭제 (관리자)
export function deleteReview(id) {
    return apiClient.delete(`/reviews/${id}`).then(r => r.data);
}
