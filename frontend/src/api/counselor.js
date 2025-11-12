// counselor.js
import apiClient from './apiClient';

// 상담사 목록 가져오기
export function fetchCounselors() {
    return apiClient.get('/counselors')  // API 경로에 맞게 수정
        .then(response => response.data)   // 응답 데이터 리턴
        .catch(error => {
            console.error('Error fetching counselors:', error);
            throw error;
        });
}
