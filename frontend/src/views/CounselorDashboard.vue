<template>
  <div class="page">
    <h1>🧑‍💼 상담사 대시보드</h1>

    <!-- 탭 -->
    <div class="tabs">
      <button :class="{ active: tab === 'today' }" @click="tab = 'today'; fetchToday()">📅 오늘 일정</button>
      <button :class="{ active: tab === 'monthly' }" @click="tab = 'monthly'; fetchMonthly()">📊 월간 통계</button>
    </div>

    <!-- 오늘 일정 -->
    <div v-if="tab === 'today'">
      <div v-if="loadingToday" class="loading"><div class="spinner"></div></div>
      <div v-else-if="!todayData" class="empty">데이터를 불러올 수 없습니다.</div>
      <div v-else>
        <!-- 요약 카드 -->
        <div class="summary-grid">
          <div class="summary-card">
            <div class="s-icon">📅</div>
            <div class="s-val">{{ todayData.totalReservations ?? 0 }}</div>
            <div class="s-label">오늘 총 예약</div>
          </div>
          <div class="summary-card green">
            <div class="s-icon">✅</div>
            <div class="s-val">{{ todayData.completed ?? 0 }}</div>
            <div class="s-label">완료</div>
          </div>
          <div class="summary-card blue">
            <div class="s-icon">⏳</div>
            <div class="s-val">{{ todayData.pending ?? 0 }}</div>
            <div class="s-label">대기 중</div>
          </div>
          <div class="summary-card red">
            <div class="s-icon">❌</div>
            <div class="s-val">{{ todayData.cancelled ?? 0 }}</div>
            <div class="s-label">취소</div>
          </div>
        </div>

        <!-- 오늘 일정 목록 -->
        <h2 class="section-title">오늘의 일정</h2>
        <div v-if="!todayData.todaySchedules?.length" class="empty">오늘 예약된 일정이 없습니다.</div>
        <div v-else class="schedule-list">
          <div v-for="s in todayData.todaySchedules" :key="s.reservationId" class="schedule-card">
            <div class="schedule-time">{{ s.startTime }} ~ {{ s.endTime }}</div>
            <div class="schedule-body">
              <div class="schedule-user">👤 {{ s.userName }}</div>
            </div>
            <span class="badge" :class="statusClass(s.status)">{{ statusLabel(s.status) }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 월간 통계 -->
    <div v-if="tab === 'monthly'">
      <div class="month-selector">
        <select v-model="selectedYear" @change="fetchMonthly">
          <option v-for="y in years" :key="y" :value="y">{{ y }}년</option>
        </select>
        <select v-model="selectedMonth" @change="fetchMonthly">
          <option v-for="m in 12" :key="m" :value="m">{{ m }}월</option>
        </select>
      </div>

      <div v-if="loadingMonthly" class="loading"><div class="spinner"></div></div>
      <div v-else-if="!monthlyData" class="empty">데이터를 불러올 수 없습니다.</div>
      <div v-else>
        <!-- 월간 요약 -->
        <div class="summary-grid">
          <div class="summary-card">
            <div class="s-icon">📊</div>
            <div class="s-val">{{ monthlyData.totalReservations ?? 0 }}</div>
            <div class="s-label">총 예약</div>
          </div>
          <div class="summary-card green">
            <div class="s-icon">✅</div>
            <div class="s-val">{{ monthlyData.completed ?? 0 }}</div>
            <div class="s-label">완료</div>
          </div>
          <div class="summary-card red">
            <div class="s-icon">❌</div>
            <div class="s-val">{{ monthlyData.cancelled ?? 0 }}</div>
            <div class="s-label">취소</div>
          </div>
        </div>

        <!-- 일별 통계 바 차트 -->
        <h2 class="section-title">일별 예약 현황</h2>
        <div v-if="!monthlyData.dailyStats?.length" class="empty">해당 월 데이터가 없습니다.</div>
        <div v-else class="daily-chart">
          <div
            v-for="d in monthlyData.dailyStats"
            :key="d.date"
            class="bar-wrap"
            :title="`${d.date}: ${d.count}건`"
          >
            <div
              class="bar"
              :style="{ height: barHeight(d.count) + 'px' }"
            >
              <span v-if="d.count > 0" class="bar-label">{{ d.count }}</span>
            </div>
            <div class="bar-date">{{ new Date(d.date).getDate() }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue';
import { getTodayDashboard, getMonthlyDashboard } from '@/api/counselorDashboard';

export default {
  name: 'CounselorDashboard',
  setup() {
    const tab = ref('today');

    // 오늘 일정
    const todayData = ref(null);
    const loadingToday = ref(false);

    // 월간 통계
    const monthlyData = ref(null);
    const loadingMonthly = ref(false);
    const now = new Date();
    const selectedYear = ref(now.getFullYear());
    const selectedMonth = ref(now.getMonth() + 1);
    const years = Array.from({ length: 3 }, (_, i) => now.getFullYear() - 1 + i);

    const fetchToday = async () => {
      loadingToday.value = true;
      try { todayData.value = await getTodayDashboard(); } catch { todayData.value = null; } finally { loadingToday.value = false; }
    };

    const fetchMonthly = async () => {
      loadingMonthly.value = true;
      try { monthlyData.value = await getMonthlyDashboard(selectedYear.value, selectedMonth.value); }
      catch { monthlyData.value = null; } finally { loadingMonthly.value = false; }
    };

    onMounted(fetchToday);

    const maxCount = computed(() => {
      if (!monthlyData.value?.dailyStats?.length) return 1;
      return Math.max(...monthlyData.value.dailyStats.map(d => d.count), 1);
    });

    const barHeight = (count) => Math.max((count / maxCount.value) * 100, count > 0 ? 6 : 0);

    const statusLabel = (s) => ({ RESERVED: '예약됨', CONFIRMED: '확정', COMPLETED: '완료', CANCELLED: '취소', PENDING: '대기' }[s] || s);
    const statusClass = (s) => ({ RESERVED: 'blue', CONFIRMED: 'green', COMPLETED: 'gray', CANCELLED: 'red', PENDING: 'yellow' }[s] || '');

    return { tab, todayData, loadingToday, monthlyData, loadingMonthly, selectedYear, selectedMonth, years, fetchToday, fetchMonthly, barHeight, statusLabel, statusClass };
  }
};
</script>

<style scoped>
.page { background: #0d0d1a; min-height: 100vh; padding: 32px 8%; color: white; }
h1 { font-size: 1.8rem; margin-bottom: 24px; }
.tabs { display: flex; gap: 8px; margin-bottom: 28px; }
.tabs button { padding: 10px 22px; background: rgba(255,255,255,0.08); border: 1px solid rgba(255,255,255,0.15); border-radius: 10px; color: rgba(255,255,255,0.7); cursor: pointer; font-size: 0.95rem; transition: all 0.2s; }
.tabs button.active { background: linear-gradient(135deg, #7c3aed, #4f46e5); border-color: transparent; color: white; }
.loading { display: flex; justify-content: center; padding: 60px; }
.spinner { width: 36px; height: 36px; border: 3px solid rgba(167,139,250,0.2); border-top-color: #a78bfa; border-radius: 50%; animation: spin 0.8s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
.empty { text-align: center; padding: 60px; color: rgba(255,255,255,0.4); }

/* 요약 카드 */
.summary-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(160px, 1fr)); gap: 16px; margin-bottom: 36px; }
.summary-card { background: rgba(255,255,255,0.06); border: 1px solid rgba(255,255,255,0.1); border-radius: 16px; padding: 24px; text-align: center; }
.summary-card.green { border-color: rgba(34,197,94,0.3); background: rgba(34,197,94,0.07); }
.summary-card.blue { border-color: rgba(59,130,246,0.3); background: rgba(59,130,246,0.07); }
.summary-card.red { border-color: rgba(239,68,68,0.3); background: rgba(239,68,68,0.07); }
.s-icon { font-size: 1.8rem; margin-bottom: 8px; }
.s-val { font-size: 2.2rem; font-weight: 700; color: #a78bfa; margin-bottom: 4px; }
.s-label { font-size: 0.82rem; color: rgba(255,255,255,0.5); }

/* 일정 목록 */
.section-title { font-size: 1.2rem; margin-bottom: 16px; }
.schedule-list { display: flex; flex-direction: column; gap: 12px; }
.schedule-card { background: rgba(255,255,255,0.05); border: 1px solid rgba(255,255,255,0.1); border-radius: 12px; padding: 16px 20px; display: flex; align-items: center; gap: 20px; }
.schedule-time { font-size: 0.95rem; font-weight: 600; color: #a78bfa; min-width: 130px; }
.schedule-body { flex: 1; }
.schedule-user { font-size: 0.9rem; }
.badge { padding: 4px 12px; border-radius: 20px; font-size: 0.78rem; font-weight: 600; white-space: nowrap; }
.badge.blue { background: rgba(59,130,246,0.2); color: #93c5fd; }
.badge.green { background: rgba(34,197,94,0.2); color: #86efac; }
.badge.gray { background: rgba(255,255,255,0.1); color: rgba(255,255,255,0.5); }
.badge.red { background: rgba(239,68,68,0.2); color: #fca5a5; }
.badge.yellow { background: rgba(251,191,36,0.2); color: #fde68a; }

/* 월간 통계 */
.month-selector { display: flex; gap: 10px; margin-bottom: 28px; }
.month-selector select { padding: 8px 14px; background: rgba(255,255,255,0.1); border: 1px solid rgba(255,255,255,0.2); border-radius: 8px; color: white; outline: none; cursor: pointer; }

/* 바 차트 */
.daily-chart { display: flex; align-items: flex-end; gap: 4px; height: 140px; padding: 12px 0 0; overflow-x: auto; }
.bar-wrap { display: flex; flex-direction: column; align-items: center; gap: 4px; flex: 1; min-width: 22px; }
.bar { background: linear-gradient(to top, #7c3aed, #a78bfa); border-radius: 4px 4px 0 0; width: 100%; min-height: 0; position: relative; display: flex; align-items: flex-start; justify-content: center; transition: height 0.3s; }
.bar-label { font-size: 0.65rem; color: white; margin-top: 2px; }
.bar-date { font-size: 0.7rem; color: rgba(255,255,255,0.45); }
</style>
