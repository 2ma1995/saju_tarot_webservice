// reservation.js
import apiClient from './apiClient';

export function createReservation(data) {
    return apiClient.post('/reservations', data).then(r => r.data);
}

export function getMyReservations() {
    return apiClient.get('/reservations/my').then(r => r.data);
}

export function cancelReservation(id) {
    return apiClient.delete(`/reservations/${id}`).then(r => r.data);
}
