// counselorDashboard.js
import apiClient from './apiClient';

// 오늘 일정 대시보드
export function getTodayDashboard() {
    return apiClient.get('/counselors/dashboard/today').then(r => r.data);
}

// 월간 대시보드
export function getMonthlyDashboard(year, month) {
    const params = {};
    if (year) params.year = year;
    if (month) params.month = month;
    return apiClient.get('/counselors/dashboard', { params }).then(r => r.data);
}
