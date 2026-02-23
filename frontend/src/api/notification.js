// notification.js
import apiClient from './apiClient';

// 내 알림 목록 조회
export function getMyNotifications(page = 0, size = 20) {
    return apiClient.get('/notifications/my', { params: { page, size } }).then(r => r.data);
}

// 단건 읽음 처리
export function markRead(id) {
    return apiClient.patch(`/notifications/${id}/read`).then(r => r.data);
}

// 전체 읽음 처리
export function markAllRead() {
    return apiClient.patch('/notifications/read-all').then(r => r.data);
}

// 안읽은 알림 개수
export function getUnreadCount() {
    return apiClient.get('/notifications/unread-count').then(r => r.data);
}
