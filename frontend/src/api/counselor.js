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
        params: { date }
    }).then(r => r.data);
}
