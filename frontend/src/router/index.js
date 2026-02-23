import { createRouter, createWebHistory } from 'vue-router';
import { isLoggedIn, isAdmin } from '@/api/auth';

import HomePage from '@/views/HomePage.vue';
import LoginPage from '@/views/LoginPage.vue';
import SignupPage from '@/views/SignupPage.vue';
import CounselorListPage from '@/views/CounselorListPage.vue';
import CounselorDetailPage from '@/views/CounselorDetailPage.vue';
import MyPage from '@/views/MyPage.vue';
import AdminDashboard from '@/views/AdminDashboard.vue';
import CounselorDashboard from '@/views/CounselorDashboard.vue';
import ProfileManagePage from '@/views/ProfileManagePage.vue';

const routes = [
    { path: '/', name: 'Home', component: HomePage },
    { path: '/login', name: 'Login', component: LoginPage },
    { path: '/signup', name: 'Signup', component: SignupPage },
    { path: '/counselors', name: 'Counselors', component: CounselorListPage },
    { path: '/counselors/:id', name: 'CounselorDetail', component: CounselorDetailPage },
    {
        path: '/mypage',
        name: 'MyPage',
        component: MyPage,
        meta: { requiresAuth: true }
    },
    {
        path: '/admin',
        name: 'Admin',
        component: AdminDashboard,
        meta: { requiresAuth: true, requiresAdmin: true }
    },
    {
        path: '/counselor-dashboard',
        name: 'CounselorDashboard',
        component: CounselorDashboard,
        meta: { requiresAuth: true, requiresCounselor: true }
    },
    {
        path: '/profile-manage',
        name: 'ProfileManage',
        component: ProfileManagePage,
        meta: { requiresAuth: true, requiresCounselor: true }
    }
];

const router = createRouter({
    history: createWebHistory(),
    routes,
    scrollBehavior: () => ({ top: 0 })
});

// 라우터 가드: 인증 및 권한 확인
router.beforeEach((to) => {
    if (to.meta.requiresAuth && !isLoggedIn()) {
        return '/login';
    }
    if (to.meta.requiresAdmin && !isAdmin()) {
        return '/';
    }
    const user = JSON.parse(localStorage.getItem('user') || 'null');
    if (to.meta.requiresCounselor && user?.role !== 'COUNSELOR') {
        return '/';
    }
});

export default router;
