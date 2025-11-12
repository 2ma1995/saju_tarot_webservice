import { createRouter, createWebHistory } from 'vue-router';

// 상담사 상세 페이지와 목록 페이지 컴포넌트 임포트
import CounselorListPage from "@/views/CounselorListPage.vue";
import CounselorDetailPage from "@/views/CounselorDetailPage.vue";
import HomePage from "@/views/HomePage.vue";

// 라우터 설정
const routes = [
    {
        path: '/',
        name: 'Home',
        component: HomePage // 홈 페이지 컴포넌트 (HomePage.vue)
    },
    {
        path: '/counselors',
        name: 'Counselors',
        component: CounselorListPage // 상담사 목록 페이지 컴포넌트
    },
    {
        path: '/counselor',  // :id로 상담사 상세 페이지로 이동
        name: 'CounselorDetail',
        component: CounselorDetailPage,
    }
];

// Vue Router 인스턴스를 생성
const router = createRouter({
    history: createWebHistory(),
    routes
});

export default router;
