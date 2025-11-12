// auth.js
import apiClient from './apiClient';

// 로그인 API 호출
export function loginUser(credentials) {
    return apiClient.post('/auth/login', credentials)
        .then(response => {
            const { token } = response.data;  // 응답에서 JWT 토큰 추출
            localStorage.setItem('accessToken', token);  // 토큰을 localStorage에 저장
            return response;
        })
        .catch(error => {
            console.error('Login error:', error);
            throw error;
        });
}
