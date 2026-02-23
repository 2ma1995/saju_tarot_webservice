<template>
  <div id="app">
    <!-- 네비게이션 -->
    <nav class="navbar">
      <router-link to="/" class="logo">🔮 사주타로</router-link>
      <div class="nav-links">
        <router-link to="/counselors">상담사 찾기</router-link>
        <template v-if="loggedIn">
          <router-link to="/mypage">마이페이지</router-link>
          <router-link v-if="isCounselor" to="/counselor-dashboard">🧑‍💼 대시보드</router-link>
          <router-link v-if="isCounselor" to="/profile-manage">🛠️ 프로필</router-link>
          <router-link v-if="isAdminUser" to="/admin">관리자</router-link>

          <!-- 알림 벨 -->
          <div class="notif-wrap" ref="notifWrap">
            <button class="btn-bell" @click="toggleNotif" :class="{ active: showNotif }">
              🔔
              <span v-if="unreadCount > 0" class="badge-count">{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
            </button>

            <!-- 알림 드롭다운 -->
            <div v-if="showNotif" class="notif-dropdown">
              <div class="notif-header">
                <span>알림</span>
                <button v-if="unreadCount > 0" class="btn-read-all" @click="readAll">모두 읽음</button>
              </div>
              <div v-if="loadingNotif" class="notif-loading"><div class="mini-spinner"></div></div>
              <div v-else-if="notifications.length === 0" class="notif-empty">새 알림이 없습니다.</div>
              <div v-else class="notif-list">
                <div
                  v-for="n in notifications"
                  :key="n.id"
                  class="notif-item"
                  :class="{ unread: !n.isRead }"
                  @click="readItem(n)"
                >
                  <div class="notif-dot" v-if="!n.isRead"></div>
                  <div class="notif-body">
                    <p class="notif-msg">{{ n.message }}</p>
                    <p class="notif-time">{{ formatTime(n.createdAt) }}</p>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <button class="btn-logout" @click="handleLogout">로그아웃</button>
        </template>
        <template v-else>
          <router-link to="/login" class="btn-nav-login">로그인</router-link>
          <router-link to="/signup" class="btn-nav-signup">회원가입</router-link>
        </template>
      </div>
    </nav>

    <router-view />
  </div>
</template>

<script>
import { ref, watch, onMounted, onUnmounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { isLoggedIn, isAdmin, logoutUser } from '@/api/auth';
import { getMyNotifications, markRead, markAllRead, getUnreadCount } from '@/api/notification';

export default {
  name: 'App',
  setup() {
    const route = useRoute();
    const router = useRouter();
    const loggedIn = ref(isLoggedIn());
    const isAdminUser = ref(isAdmin());
    const isCounselor = ref(JSON.parse(localStorage.getItem('user') || 'null')?.role === 'COUNSELOR');

    // 알림 상태
    const showNotif = ref(false);
    const notifications = ref([]);
    const unreadCount = ref(0);
    const loadingNotif = ref(false);
    const notifWrap = ref(null);

    // 라우트 변경마다 로그인 상태 갱신
    watch(() => route.path, () => {
      loggedIn.value = isLoggedIn();
      isAdminUser.value = isAdmin();
      const u = JSON.parse(localStorage.getItem('user') || 'null');
      isCounselor.value = u?.role === 'COUNSELOR';
      showNotif.value = false;
    });

    const fetchUnreadCount = async () => {
      if (!isLoggedIn()) return;
      try {
        const data = await getUnreadCount();
        unreadCount.value = data.count || 0;
      } catch {}
    };

    const fetchNotifications = async () => {
      loadingNotif.value = true;
      try {
        const data = await getMyNotifications(0, 20);
        notifications.value = data.content || data || [];
      } catch {
        notifications.value = [];
      } finally {
        loadingNotif.value = false;
      }
    };

    const toggleNotif = async () => {
      showNotif.value = !showNotif.value;
      if (showNotif.value) {
        await fetchNotifications();
      }
    };

    const readItem = async (n) => {
      if (!n.isRead) {
        try {
          await markRead(n.id);
          n.isRead = true;
          unreadCount.value = Math.max(0, unreadCount.value - 1);
        } catch {}
      }
    };

    const readAll = async () => {
      try {
        await markAllRead();
        notifications.value.forEach(n => n.isRead = true);
        unreadCount.value = 0;
      } catch {}
    };

    const handleLogout = async () => {
      try { await logoutUser(); } catch {}
      loggedIn.value = false;
      unreadCount.value = 0;
      router.push('/login');
    };

    const formatTime = (dt) => {
      if (!dt) return '';
      const diff = Date.now() - new Date(dt).getTime();
      const min = Math.floor(diff / 60000);
      if (min < 1) return '방금 전';
      if (min < 60) return `${min}분 전`;
      const hr = Math.floor(min / 60);
      if (hr < 24) return `${hr}시간 전`;
      return new Date(dt).toLocaleDateString('ko-KR');
    };

    // 외부 클릭 시 드롭다운 닫기
    const onClickOutside = (e) => {
      if (notifWrap.value && !notifWrap.value.contains(e.target)) {
        showNotif.value = false;
      }
    };

    // 미확인 개수 주기적 갱신 (60초)
    let pollTimer = null;
    onMounted(() => {
      fetchUnreadCount();
      pollTimer = setInterval(fetchUnreadCount, 60000);
      document.addEventListener('click', onClickOutside);
    });
    onUnmounted(() => {
      clearInterval(pollTimer);
      document.removeEventListener('click', onClickOutside);
    });

    return { loggedIn, isAdminUser, isCounselor, handleLogout, showNotif, notifications, unreadCount, loadingNotif, notifWrap, toggleNotif, readItem, readAll, formatTime };
  }
};
</script>

<style>
* { margin: 0; padding: 0; box-sizing: border-box; }
body { font-family: 'Pretendard', 'Noto Sans KR', sans-serif; background: #0d0d1a; }
input, select, textarea, button { font-family: inherit; }

/* 네비게이션 */
.navbar {
  position: sticky; top: 0; z-index: 100;
  display: flex; align-items: center; justify-content: space-between;
  padding: 0 8%;
  height: 64px;
  background: rgba(13,13,26,0.9);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid rgba(255,255,255,0.08);
}
.logo { color: white; text-decoration: none; font-size: 1.2rem; font-weight: 700; letter-spacing: -0.5px; }
.nav-links { display: flex; align-items: center; gap: 24px; }
.nav-links a { color: rgba(255,255,255,0.75); text-decoration: none; font-size: 0.95rem; transition: color 0.2s; }
.nav-links a:hover, .nav-links a.router-link-active { color: white; }
.btn-logout { background: none; border: 1px solid rgba(255,255,255,0.2); color: rgba(255,255,255,0.7); padding: 7px 16px; border-radius: 8px; cursor: pointer; font-size: 0.9rem; }
.btn-nav-login { padding: 7px 16px; border: 1px solid rgba(255,255,255,0.25); border-radius: 8px; }
.btn-nav-signup { padding: 7px 16px; background: linear-gradient(135deg, #7c3aed, #4f46e5); border-radius: 8px; color: white !important; font-weight: 600; }

/* 알림 벨 */
.notif-wrap { position: relative; }
.btn-bell { position: relative; background: none; border: 1px solid rgba(255,255,255,0.2); color: rgba(255,255,255,0.8); width: 38px; height: 38px; border-radius: 50%; cursor: pointer; font-size: 1rem; display: flex; align-items: center; justify-content: center; transition: all 0.2s; }
.btn-bell:hover, .btn-bell.active { background: rgba(167,139,250,0.15); border-color: rgba(167,139,250,0.5); }
.badge-count { position: absolute; top: -4px; right: -4px; background: #ef4444; color: white; border-radius: 20px; font-size: 0.65rem; font-weight: 700; padding: 1px 5px; min-width: 17px; text-align: center; line-height: 1.5; }

/* 알림 드롭다운 */
.notif-dropdown { position: absolute; top: calc(100% + 10px); right: 0; width: 320px; background: #1a1a2e; border: 1px solid rgba(255,255,255,0.13); border-radius: 14px; box-shadow: 0 16px 48px rgba(0,0,0,0.5); overflow: hidden; z-index: 200; animation: fadeInDown 0.15s ease; }
@keyframes fadeInDown { from { opacity: 0; transform: translateY(-6px); } to { opacity: 1; transform: translateY(0); } }
.notif-header { display: flex; justify-content: space-between; align-items: center; padding: 14px 18px; border-bottom: 1px solid rgba(255,255,255,0.08); font-size: 0.95rem; font-weight: 600; color: white; }
.btn-read-all { background: none; border: none; color: #a78bfa; cursor: pointer; font-size: 0.8rem; }
.notif-loading { display: flex; justify-content: center; padding: 28px; }
.mini-spinner { width: 22px; height: 22px; border: 2px solid rgba(167,139,250,0.2); border-top-color: #a78bfa; border-radius: 50%; animation: spin 0.7s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
.notif-empty { text-align: center; padding: 28px; color: rgba(255,255,255,0.4); font-size: 0.9rem; }
.notif-list { max-height: 340px; overflow-y: auto; }
.notif-item { display: flex; align-items: flex-start; gap: 10px; padding: 13px 18px; cursor: pointer; transition: background 0.15s; border-bottom: 1px solid rgba(255,255,255,0.05); }
.notif-item:last-child { border-bottom: none; }
.notif-item:hover { background: rgba(255,255,255,0.05); }
.notif-item.unread { background: rgba(124,58,237,0.08); }
.notif-dot { width: 8px; height: 8px; border-radius: 50%; background: #a78bfa; flex-shrink: 0; margin-top: 5px; }
.notif-body { flex: 1; }
.notif-msg { font-size: 0.88rem; color: rgba(255,255,255,0.9); line-height: 1.5; margin-bottom: 3px; }
.notif-time { font-size: 0.75rem; color: rgba(255,255,255,0.4); }

@media (max-width: 768px) {
  .navbar { padding: 0 4%; }
  .nav-links { gap: 12px; }
  .nav-links a { font-size: 0.85rem; }
  .notif-dropdown { width: 280px; right: -60px; }
}
</style>
