<template>
  <div class="page" v-if="!loading">
    <button class="back-btn" @click="$router.back()">← 목록으로</button>

    <div v-if="counselor" class="detail-wrap">
      <!-- 프로필 헤더 -->
      <div class="profile-header">
        <div class="avatar-big">{{ counselor.name?.charAt(0) }}</div>
        <div class="profile-info">
          <h1>{{ counselor.name }}</h1>
          <p class="nick">@{{ counselor.nickname }}</p>
          <div class="meta">
            <span>⭐ {{ counselor.averageRating?.toFixed(1) || '신규' }}</span>
            <span>💬 리뷰 {{ counselor.reviewCount || 0 }}개</span>
          </div>
          <p class="bio">{{ counselor.bio || '소개가 없습니다.' }}</p>
        </div>
        <div class="price-box">
          <p class="price-label">상담 요금</p>
          <p class="price">{{ serviceItems[0]?.price?.toLocaleString() || '문의' }}원</p>
          <button
            v-if="isLoggedIn"
            class="btn-fav"
            :class="{ active: isFavorite }"
            @click="toggleFavorite"
          >
            {{ isFavorite ? '❤️ 즐겨찾기 해제' : '🤍 즐겨찾기' }}
          </button>
          <button class="btn-reserve" @click="showReservation = true" :disabled="!isLoggedIn">
            {{ isLoggedIn ? '예약하기' : '로그인 후 예약' }}
          </button>
        </div>
      </div>

      <!-- 예약 패널 -->
      <div v-if="showReservation" class="reserve-panel">
        <h3>📅 날짜 선택</h3>
        <input type="date" v-model="selectedDate" @change="loadSchedules" class="date-input" :min="today" />

        <div v-if="schedules.length" class="schedule-grid">
          <button
            v-for="s in schedules"
            :key="s.id"
            class="schedule-btn"
            :class="{ active: selectedSchedule === s.id }"
            @click="selectedSchedule = s.id"
          >
            {{ formatTime(s.startTime) }}
          </button>
        </div>
        <p v-else-if="selectedDate" class="no-schedule">선택 가능한 시간이 없습니다.</p>

        <div v-if="selectedSchedule" class="service-select">
          <h3>🔮 서비스 선택</h3>
          <div class="service-grid">
            <div
              v-for="item in serviceItems"
              :key="item.id"
              class="service-item"
              :class="{ active: selectedService === item.id }"
              @click="selectedService = item.id"
            >
              <strong>{{ item.name }}</strong>
              <span>{{ item.price?.toLocaleString() }}원</span>
            </div>
          </div>
        </div>

        <textarea v-if="selectedService" v-model="note" placeholder="상담 메모 (선택)" class="note-input"></textarea>

        <button v-if="selectedService" class="btn-confirm" @click="confirmReservation" :disabled="reserving">
          {{ reserving ? '예약 중...' : '예약 확정' }}
        </button>

        <div v-if="reserveMsg" class="reserve-msg" :class="reserveError ? 'error' : 'success'">{{ reserveMsg }}</div>
      </div>

      <!-- 서비스 목록 -->
      <div class="section">
        <h2>제공 서비스</h2>
        <div class="service-list">
          <div v-for="item in serviceItems" :key="item.id" class="service-card">
            <span class="service-type">{{ item.serviceType === 'TAROT' ? '🃏 타로' : item.serviceType === 'SAJU' ? '☯️ 사주' : '✨ 기타' }}</span>
            <strong>{{ item.name }}</strong>
            <span class="service-price">{{ item.price?.toLocaleString() }}원</span>
          </div>
        </div>
      </div>
    </div>
  </div>
  <div v-else class="loading"><div class="spinner"></div></div>
</template>

<script>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { getCounselorDetail, getSchedules } from '@/api/counselor';
import { createReservation } from '@/api/reservation';
import { isLoggedIn } from '@/api/auth';
import { addFavorite, removeFavorite, getMyFavorites } from '@/api/favorite';
import apiClient from '@/api/apiClient';

export default {
  name: 'CounselorDetailPage',
  setup() {
    const route = useRoute();
    const router = useRouter();
    const counselor = ref(null);
    const loading = ref(true);
    const showReservation = ref(false);
    const selectedDate = ref('');
    const schedules = ref([]);
    const selectedSchedule = ref(null);
    const serviceItems = ref([]);
    const selectedService = ref(null);
    const note = ref('');
    const reserving = ref(false);
    const reserveMsg = ref('');
    const reserveError = ref(false);
    const today = new Date().toISOString().split('T')[0];
    const isFavorite = ref(false);
    const loggedIn = isLoggedIn();

    onMounted(async () => {
      try {
        const id = route.params.id;
        counselor.value = await getCounselorDetail(id);
        // 서비스 아이템 조회
        try {
          const res = await apiClient.get(`/service-items/counselor/${id}`);
          serviceItems.value = res.data || [];
        } catch {}
        // 즐겨찾기 상태 조회
        if (loggedIn) {
          try {
            const favList = await getMyFavorites();
            const ids = new Set((favList || []).map(f => f.counselorId || f.id));
            isFavorite.value = ids.has(Number(id));
          } catch {}
        }
      } finally {
        loading.value = false;
      }
    });

    const toggleFavorite = async () => {
      const id = Number(route.params.id);
      try {
        if (isFavorite.value) {
          await removeFavorite(id);
          isFavorite.value = false;
        } else {
          await addFavorite(id);
          isFavorite.value = true;
        }
      } catch (e) {
        alert(e.response?.data?.message || '즐겨찾기 처리 실패');
      }
    };

    const loadSchedules = async () => {
      schedules.value = [];
      selectedSchedule.value = null;
      if (!selectedDate.value) return;
      try {
        const data = await getSchedules(route.params.id, selectedDate.value);
        schedules.value = (data || []).filter(s => s.available);
      } catch {}
    };

    const formatTime = (dt) => {
      if (!dt) return '';
      const d = new Date(dt);
      return d.toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' });
    };

    const confirmReservation = async () => {
      reserving.value = true;
      reserveMsg.value = '';
      try {
        await createReservation({
          counselorId: Number(route.params.id),
          scheduleId: selectedSchedule.value,
          serviceItemId: selectedService.value,
          note: note.value
        });
        reserveMsg.value = '✅ 예약이 완료되었습니다! 마이페이지에서 확인하세요.';
        reserveError.value = false;
        setTimeout(() => router.push('/mypage'), 2000);
      } catch (e) {
        reserveMsg.value = e.response?.data?.message || '예약에 실패했습니다.';
        reserveError.value = true;
      } finally {
        reserving.value = false;
      }
    };

    return {
      counselor, loading, showReservation, selectedDate, schedules,
      selectedSchedule, serviceItems, selectedService, note,
      reserving, reserveMsg, reserveError, today,
      isFavorite, isLoggedIn: loggedIn,
      loadSchedules, formatTime, confirmReservation, toggleFavorite
    };
  }
};
</script>

<style scoped>
.page { background: #0d0d1a; min-height: 100vh; padding: 32px 8%; color: white; }
.back-btn { background: none; border: 1px solid rgba(255,255,255,0.2); color: rgba(255,255,255,0.7); padding: 8px 16px; border-radius: 8px; cursor: pointer; margin-bottom: 24px; }
.loading { display: flex; justify-content: center; align-items: center; height: 100vh; background: #0d0d1a; }
.spinner { width: 40px; height: 40px; border: 3px solid rgba(167,139,250,0.2); border-top-color: #a78bfa; border-radius: 50%; animation: spin 0.8s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
.profile-header { display: flex; gap: 28px; align-items: flex-start; background: rgba(255,255,255,0.05); border: 1px solid rgba(255,255,255,0.1); border-radius: 20px; padding: 32px; margin-bottom: 24px; }
.avatar-big { width: 80px; height: 80px; border-radius: 50%; background: linear-gradient(135deg, #7c3aed, #4f46e5); display: flex; align-items: center; justify-content: center; font-size: 2rem; font-weight: 700; flex-shrink: 0; }
.profile-info { flex: 1; }
.profile-info h1 { font-size: 1.8rem; margin-bottom: 4px; }
.nick { color: rgba(255,255,255,0.5); font-size: 0.9rem; margin-bottom: 10px; }
.meta { display: flex; gap: 16px; color: #fbbf24; font-size: 0.9rem; margin-bottom: 12px; }
.meta span { color: rgba(255,255,255,0.8); }
.bio { color: rgba(255,255,255,0.7); line-height: 1.7; }
.price-box { text-align: center; min-width: 160px; }
.price-label { color: rgba(255,255,255,0.5); font-size: 0.85rem; margin-bottom: 4px; }
.price { font-size: 1.8rem; font-weight: 700; color: #a78bfa; margin-bottom: 12px; }
.btn-fav { width: 100%; padding: 10px; background: rgba(255,255,255,0.08); border: 1px solid rgba(255,255,255,0.2); border-radius: 10px; color: white; font-size: 0.95rem; cursor: pointer; margin-bottom: 8px; transition: all 0.2s; }
.btn-fav:hover { background: rgba(255,100,100,0.15); border-color: rgba(255,100,100,0.5); }
.btn-fav.active { background: rgba(255,100,100,0.2); border-color: rgba(255,100,100,0.6); }
.btn-reserve { width: 100%; padding: 12px; background: linear-gradient(135deg, #7c3aed, #4f46e5); color: white; border: none; border-radius: 10px; font-size: 1rem; font-weight: 600; cursor: pointer; }
.btn-reserve:disabled { opacity: 0.5; cursor: not-allowed; }
.reserve-panel { background: rgba(124,58,237,0.1); border: 1px solid rgba(124,58,237,0.3); border-radius: 16px; padding: 28px; margin-bottom: 24px; }
.reserve-panel h3 { margin-bottom: 14px; font-size: 1.1rem; }
.date-input { padding: 10px 14px; background: rgba(255,255,255,0.1); border: 1px solid rgba(255,255,255,0.2); border-radius: 8px; color: white; outline: none; margin-bottom: 16px; }
.schedule-grid { display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 20px; }
.schedule-btn { padding: 8px 16px; background: rgba(255,255,255,0.1); border: 1px solid rgba(255,255,255,0.2); border-radius: 8px; color: white; cursor: pointer; }
.schedule-btn.active { background: #7c3aed; border-color: #7c3aed; }
.no-schedule { color: rgba(255,255,255,0.5); font-size: 0.9rem; }
.service-grid, .service-list { display: flex; flex-direction: column; gap: 8px; margin-bottom: 16px; }
.service-item, .service-card { display: flex; justify-content: space-between; align-items: center; padding: 12px 16px; background: rgba(255,255,255,0.05); border: 1px solid rgba(255,255,255,0.1); border-radius: 10px; cursor: pointer; }
.service-item.active { border-color: #7c3aed; background: rgba(124,58,237,0.2); }
.service-type { color: rgba(255,255,255,0.5); font-size: 0.85rem; }
.service-price { color: #a78bfa; font-weight: 600; }
.note-input { width: 100%; padding: 12px; background: rgba(255,255,255,0.1); border: 1px solid rgba(255,255,255,0.2); border-radius: 8px; color: white; outline: none; resize: vertical; margin-bottom: 16px; box-sizing: border-box; }
.btn-confirm { width: 100%; padding: 14px; background: linear-gradient(135deg, #7c3aed, #4f46e5); color: white; border: none; border-radius: 10px; font-size: 1rem; font-weight: 600; cursor: pointer; }
.reserve-msg { margin-top: 12px; padding: 12px; border-radius: 8px; font-size: 0.9rem; }
.reserve-msg.success { background: rgba(34,197,94,0.2); color: #86efac; border: 1px solid rgba(34,197,94,0.4); }
.reserve-msg.error { background: rgba(239,68,68,0.2); color: #fca5a5; border: 1px solid rgba(239,68,68,0.4); }
.section { margin-top: 24px; }
.section h2 { font-size: 1.3rem; margin-bottom: 16px; }
</style>
