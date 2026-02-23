// role.js
import apiClient from './apiClient';

// 상담사 역할 신청
export function requestCounselorRole() {
    return apiClient.post('/roles/request').then(r => r.data);
}

// 신청 대기 목록 (관리자)
export function getPendingRequests() {
    return apiClient.get('/roles/requests').then(r => r.data);
}

// 승인 (관리자)
export function approveRequest(requestId) {
    return apiClient.post(`/roles/approve/${requestId}`).then(r => r.data);
}

// 거절 (관리자)
export function rejectRequest(requestId) {
    return apiClient.post(`/roles/reject/${requestId}`).then(r => r.data);
}
