// payment.js
import apiClient from './apiClient';

export function requestPayment(data) {
    return apiClient.post('/payments/request', data).then(r => r.data);
}

export function getMyPayments() {
    return apiClient.get('/payments/my').then(r => r.data);
}

export function refundPayment(id) {
    return apiClient.post(`/payments/${id}/refund`).then(r => r.data);
}

// 관리자용
export function getAllPayments(status) {
    return apiClient.get('/admin/payments', { params: { status } }).then(r => r.data);
}

export function getMonthlyStats(year) {
    return apiClient.get('/admin/payments/stats/monthly', { params: { year } }).then(r => r.data);
}
