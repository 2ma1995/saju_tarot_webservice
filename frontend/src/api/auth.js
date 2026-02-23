// auth.js
import apiClient from './apiClient';

// 로그인
export function loginUser(credentials) {
    return apiClient.post('/auth/login', credentials)
        .then(response => {
            const { accessToken, refreshToken, user } = response.data;
            localStorage.setItem('accessToken', accessToken);
            localStorage.setItem('refreshToken', refreshToken);
            localStorage.setItem('user', JSON.stringify(user));
            return response.data;
        });
}

// 회원가입
export function signupUser(data) {
    return apiClient.post('/users/signup', data).then(r => r.data);
}

// 로그아웃
export function logoutUser() {
    return apiClient.post('/auth/logout').finally(() => {
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        localStorage.removeItem('user');
    });
}

// 현재 사용자 정보 가져오기
export function getCurrentUser() {
    const user = localStorage.getItem('user');
    return user ? JSON.parse(user) : null;
}

// 로그인 여부 확인
export function isLoggedIn() {
    return !!localStorage.getItem('accessToken');
}

// 관리자 여부 확인
export function isAdmin() {
    const user = getCurrentUser();
    return user?.role === 'ADMIN';
}
