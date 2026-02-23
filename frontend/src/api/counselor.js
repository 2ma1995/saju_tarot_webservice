// counselor.js
import apiClient from './apiClient';

export function getCounselors(params = {}) {
    return apiClient.get('/counselors', { params }).then(r => r.data);
}

export function getCounselorDetail(id) {
    return apiClient.get(`/counselors/profile/${id}`).then(r => r.data);
}

export function getSchedules(counselorId, date) {
    return apiClient.get(`/schedules/counselor/${counselorId}`, {
        params: date ? { date } : {}
    }).then(r => r.data);
}

export function getServiceItems(counselorId) {
    return apiClient.get(`/service-items/counselor/${counselorId}`).then(r => r.data);
}

export function getReviews(counselorId) {
    return apiClient.get(`/reviews/counselor/${counselorId}`).then(r => r.data);
}
