<template>
  <div class="page">
    <h1>🔐 관리자 대시보드</h1>

    <!-- 탭 -->
    <div class="tabs">
      <button :class="{ active: tab === 'users' }" @click="tab = 'users'; fetchUsers()">👥 사용자 관리</button>
      <button :class="{ active: tab === 'payments' }" @click="tab = 'payments'; fetchPayments()">💳 결제 관리</button>
      <button :class="{ active: tab === 'reviews' }" @click="tab = 'reviews'; fetchReviews()">✍️ 후기 관리</button>
      <button :class="{ active: tab === 'roles' }" @click="tab = 'roles'; fetchPendingRoles()">🧑‍💼 상담사 신청</button>
    </div>

    <!-- 사용자 관리 -->
    <div v-if="tab === 'users'">
      <div class="toolbar">
        <select v-model="roleFilter" @change="fetchUsers">
          <option value="">전체 역할</option>
          <option value="USER">USER</option>
          <option value="COUNSELOR">COUNSELOR</option>
          <option value="ADMIN">ADMIN</option>
        </select>
        <select v-model="activeFilter" @change="fetchUsers">
          <option value="">활성 상태</option>
          <option value="true">활성</option>
          <option value="false">비활성</option>
        </select>
      </div>

      <div v-if="loadingUsers" class="loading"><div class="spinner"></div></div>
      <div v-else class="table-wrap">
        <table>
          <thead>
            <tr><th>ID</th><th>이름</th><th>이메일</th><th>역할</th><th>상태</th><th>관리</th></tr>
          </thead>
          <tbody>
            <tr v-for="u in users" :key="u.id">
              <td>{{ u.id }}</td>
              <td>{{ u.name }}</td>
              <td>{{ u.email }}</td>
              <td>
                <select :value="u.role" @change="changeRole(u.id, $event.target.value)" class="role-select">
                  <option value="USER">USER</option>
                  <option value="COUNSELOR">COUNSELOR</option>
                  <option value="ADMIN">ADMIN</option>
                </select>
              </td>
              <td><span class="badge" :class="u.isActive ? 'green' : 'red'">{{ u.isActive ? '활성' : '비활성' }}</span></td>
              <td>
                <button v-if="u.isActive" class="btn-sm red" @click="deactivate(u.id)">비활성화</button>
              </td>
            </tr>
          </tbody>
        </table>
        <!-- 페이지네이션 -->
        <div class="pagination">
          <button @click="userPage--; fetchUsers()" :disabled="userPage === 0">‹</button>
          <span>{{ userPage + 1 }} / {{ userTotalPages }}</span>
          <button @click="userPage++; fetchUsers()" :disabled="userPage >= userTotalPages - 1">›</button>
        </div>
      </div>
    </div>

    <!-- 결제 관리 -->
    <div v-if="tab === 'payments'">
      <div class="toolbar">
        <select v-model="payStatusFilter" @change="fetchPayments">
          <option value="">전체 상태</option>
          <option value="PAID">결제완료</option>
          <option value="REFUND">환불됨</option>
          <option value="PENDING">대기중</option>
        </select>
      </div>

      <div v-if="loadingPayments" class="loading"><div class="spinner"></div></div>
      <div v-else class="table-wrap">
        <table>
          <thead>
            <tr><th>ID</th><th>거래ID</th><th>금액</th><th>방법</th><th>상태</th><th>결제일시</th></tr>
          </thead>
          <tbody>
            <tr v-for="p in adminPayments" :key="p.id">
              <td>{{ p.id }}</td>
              <td class="tx-id">{{ p.transactionId }}</td>
              <td>{{ p.amount?.toLocaleString() }}원</td>
              <td>{{ p.method }}</td>
              <td><span class="badge" :class="{ green: p.paymentStatus === 'PAID', red: p.paymentStatus === 'REFUND', blue: p.paymentStatus === 'PENDING' }">{{ p.paymentStatus }}</span></td>
              <td>{{ p.paidAt ? new Date(p.paidAt).toLocaleString('ko-KR') : '-' }}</td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 월간 통계 -->
      <div class="monthly-stats">
        <div class="stats-header">
          <h3>📊 월간 결제 통계</h3>
          <div class="stats-controls">
            <select v-model="statsYear" @change="fetchMonthlyStats">
              <option v-for="y in statYears" :key="y" :value="y">{{ y }}년</option>
            </select>
          </div>
        </div>
        <div v-if="loadingStats" class="loading"><div class="spinner"></div></div>
        <div v-else-if="monthlyStats.length" class="stats-grid">
          <div v-for="s in monthlyStats" :key="s.month" class="stat-item">
            <div class="stat-month">{{ s.month }}월</div>
            <div class="stat-bar-wrap">
              <div class="stat-bar" :style="{ width: statBarWidth(s.totalAmount) + '%' }"></div>
            </div>
            <div class="stat-val">{{ (s.totalAmount || 0).toLocaleString() }}원</div>
          </div>
        </div>
        <div v-else class="empty">통계 데이터가 없습니다.</div>
      </div>
    </div>

    <!-- 후기 관리 -->
    <div v-if="tab === 'reviews'">
      <div v-if="loadingReviews" class="loading"><div class="spinner"></div></div>
      <div v-else class="table-wrap">
        <table>
          <thead>
            <tr><th>ID</th><th>상담사</th><th>작성자</th><th>점수</th><th>내용</th><th>작성일</th><th>관리</th></tr>
          </thead>
          <tbody>
            <tr v-for="rv in allReviews" :key="rv.reviewId">
              <td>{{ rv.reviewId }}</td>
              <td>{{ rv.counselorName || '-' }}</td>
              <td>{{ rv.userName || '-' }}</td>
              <td><span class="star-score">{{ '★'.repeat(rv.score) }}</span></td>
              <td class="review-text">{{ rv.comment }}</td>
              <td>{{ rv.createdAt ? new Date(rv.createdAt).toLocaleDateString('ko-KR') : '-' }}</td>
              <td>
                <button class="btn-sm red" @click="removeReview(rv.reviewId)">삭제</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 상담사 신청 관리 -->
    <div v-if="tab === 'roles'">
      <div v-if="loadingRoles" class="loading"><div class="spinner"></div></div>
      <div v-else-if="pendingRoles.length === 0" class="empty-roles">
        ⏳ 대기 중인 신청이 없습니다.
      </div>
      <div v-else class="role-list">
        <div v-for="req in pendingRoles" :key="req.requestId" class="role-req-card">
          <div class="role-req-info">
            <div class="role-req-name">👤 {{ req.userName }}</div>
            <div class="role-req-meta">
              <span>📧 {{ req.userEmail }}</span>
              <span v-if="req.userNumber">📞 {{ req.userNumber }}</span>
              <span>📅 {{ req.requestedAt ? new Date(req.requestedAt).toLocaleDateString('ko-KR') : '-' }}</span>
            </div>
          </div>
          <div class="role-req-actions">
            <button class="btn-approve" @click="handleApprove(req.requestId)">✅ 승인</button>
            <button class="btn-reject" @click="handleReject(req.requestId)">❌ 거절</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue';
import { getAllUsers, changeUserRole, deactivateUser } from '@/api/admin';
import { getAllPayments, getMonthlyStats } from '@/api/payment';
import { getAllReviews, deleteReview } from '@/api/review';
import { getPendingRequests, approveRequest, rejectRequest } from '@/api/role';

export default {
  name: 'AdminDashboard',
  setup() {
    const tab = ref('users');
    const users = ref([]);
    const loadingUsers = ref(false);
    const roleFilter = ref('');
    const activeFilter = ref('');
    const userPage = ref(0);
    const userTotalPages = ref(1);

    const adminPayments = ref([]);
    const loadingPayments = ref(false);
    const payStatusFilter = ref('');
    const monthlyStats = ref([]);
    const statsYear = ref(new Date().getFullYear());
    const loadingStats = ref(false);
    const statYears = Array.from({ length: 3 }, (_, i) => new Date().getFullYear() - 1 + i);

    const allReviews = ref([]);
    const loadingReviews = ref(false);

    const pendingRoles = ref([]);
    const loadingRoles = ref(false);

    const fetchUsers = async () => {
      loadingUsers.value = true;
      try {
        const params = { page: userPage.value, size: 15 };
        if (roleFilter.value) params.role = roleFilter.value;
        if (activeFilter.value !== '') params.active = activeFilter.value;
        const data = await getAllUsers(params);
        users.value = data.content || data;
        userTotalPages.value = data.totalPages || 1;
      } catch { users.value = []; } finally { loadingUsers.value = false; }
    };

    const fetchPayments = async () => {
      loadingPayments.value = true;
      try {
        adminPayments.value = await getAllPayments(payStatusFilter.value || undefined);
      } catch { adminPayments.value = []; } finally { loadingPayments.value = false; }
    };

    const fetchReviews = async () => {
      loadingReviews.value = true;
      try {
        allReviews.value = await getAllReviews();
      } catch { allReviews.value = []; } finally { loadingReviews.value = false; }
    };

    const fetchMonthlyStats = async () => {
      loadingStats.value = true;
      try {
        monthlyStats.value = await getMonthlyStats(statsYear.value);
      } catch { monthlyStats.value = []; } finally { loadingStats.value = false; }
    };

    const removeReview = async (id) => {
      if (!confirm('이 후기를 삭제하시겠습니까?')) return;
      try {
        await deleteReview(id);
        allReviews.value = allReviews.value.filter(r => r.reviewId !== id);
      } catch (e) {
        alert(e.response?.data?.message || '후기 삭제 실패');
      }
    };

    const fetchPendingRoles = async () => {
      loadingRoles.value = true;
      try {
        pendingRoles.value = await getPendingRequests();
      } catch { pendingRoles.value = []; } finally { loadingRoles.value = false; }
    };

    const handleApprove = async (reqId) => {
      if (!confirm('승인하시겠습니까?')) return;
      try {
        await approveRequest(reqId);
        pendingRoles.value = pendingRoles.value.filter(r => r.requestId !== reqId);
      } catch (e) { alert(e.response?.data?.message || '승인 실패'); }
    };

    const handleReject = async (reqId) => {
      if (!confirm('거절하시겠습니까?')) return;
      try {
        await rejectRequest(reqId);
        pendingRoles.value = pendingRoles.value.filter(r => r.requestId !== reqId);
      } catch (e) { alert(e.response?.data?.message || '거절 실패'); }
    };

    const changeRole = async (userId, newRole) => {
      try { await changeUserRole(userId, newRole); } catch (e) { alert('역할 변경 실패'); }
    };

    const deactivate = async (userId) => {
      if (!confirm('사용자를 비활성화 하시겠습니까?')) return;
      try { await deactivateUser(userId); await fetchUsers(); } catch (e) { alert('비활성화 실패'); }
    };

    onMounted(fetchUsers);
    return { tab, users, loadingUsers, roleFilter, activeFilter, userPage, userTotalPages, adminPayments, loadingPayments, payStatusFilter, monthlyStats, statsYear, loadingStats, statYears, allReviews, loadingReviews, pendingRoles, loadingRoles, fetchUsers, fetchPayments, fetchMonthlyStats, fetchReviews, fetchPendingRoles, changeRole, deactivate, removeReview, handleApprove, handleReject,
      statBarWidth: (val) => { const max = Math.max(...monthlyStats.value.map(s => s.totalAmount || 0), 1); return Math.round((val / max) * 100); } };
  }
};
</script>

<style scoped>
.page { background: #0d0d1a; min-height: 100vh; padding: 32px 5%; color: white; }
h1 { font-size: 1.8rem; margin-bottom: 24px; }
.tabs { display: flex; gap: 8px; margin-bottom: 24px; }
.tabs button { padding: 10px 22px; background: rgba(255,255,255,0.08); border: 1px solid rgba(255,255,255,0.15); border-radius: 10px; color: rgba(255,255,255,0.7); cursor: pointer; }
.tabs button.active { background: linear-gradient(135deg, #7c3aed, #4f46e5); border-color: transparent; color: white; }
.toolbar { display: flex; gap: 10px; margin-bottom: 16px; }
.toolbar select { padding: 8px 12px; background: rgba(255,255,255,0.1); border: 1px solid rgba(255,255,255,0.2); border-radius: 8px; color: white; outline: none; cursor: pointer; }
.loading { display: flex; justify-content: center; padding: 60px; }
.spinner { width: 36px; height: 36px; border: 3px solid rgba(167,139,250,0.2); border-top-color: #a78bfa; border-radius: 50%; animation: spin 0.8s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
.table-wrap { overflow-x: auto; }
table { width: 100%; border-collapse: collapse; }
th { text-align: left; padding: 12px 14px; background: rgba(255,255,255,0.07); font-size: 0.85rem; color: rgba(255,255,255,0.6); border-bottom: 1px solid rgba(255,255,255,0.1); }
td { padding: 12px 14px; border-bottom: 1px solid rgba(255,255,255,0.06); font-size: 0.9rem; }
.tx-id { font-size: 0.75rem; color: rgba(255,255,255,0.5); }
.role-select { background: rgba(255,255,255,0.1); border: 1px solid rgba(255,255,255,0.2); border-radius: 6px; color: white; padding: 4px 8px; outline: none; cursor: pointer; }
.badge { padding: 3px 10px; border-radius: 20px; font-size: 0.78rem; font-weight: 600; }
.badge.green { background: rgba(34,197,94,0.2); color: #86efac; }
.badge.red { background: rgba(239,68,68,0.2); color: #fca5a5; }
.badge.blue { background: rgba(59,130,246,0.2); color: #93c5fd; }
.btn-sm { padding: 4px 12px; border-radius: 6px; font-size: 0.8rem; cursor: pointer; border: 1px solid; }
.btn-sm.red { background: rgba(239,68,68,0.1); border-color: rgba(239,68,68,0.4); color: #fca5a5; }
.pagination { display: flex; justify-content: center; align-items: center; gap: 16px; margin-top: 20px; }
.pagination button { width: 32px; height: 32px; background: rgba(255,255,255,0.1); border: 1px solid rgba(255,255,255,0.2); border-radius: 6px; color: white; cursor: pointer; }
.pagination button:disabled { opacity: 0.3; cursor: not-allowed; }
.pagination span { color: rgba(255,255,255,0.7); font-size: 0.9rem; }
.star-score { color: #fbbf24; letter-spacing: 1px; }
.review-text { max-width: 220px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; color: rgba(255,255,255,0.75); font-size: 0.85rem; }

/* 역할 신청 */
.empty-roles { text-align: center; padding: 60px; color: rgba(255,255,255,0.4); }
.role-list { display: flex; flex-direction: column; gap: 12px; }
.role-req-card { background: rgba(255,255,255,0.05); border: 1px solid rgba(255,255,255,0.1); border-radius: 12px; padding: 18px 22px; display: flex; align-items: center; justify-content: space-between; gap: 16px; }
.role-req-info { flex: 1; }
.role-req-name { font-size: 1rem; font-weight: 600; margin-bottom: 6px; }
.role-req-meta { display: flex; gap: 16px; flex-wrap: wrap; color: rgba(255,255,255,0.55); font-size: 0.83rem; }
.role-req-actions { display: flex; gap: 8px; flex-shrink: 0; }
.btn-approve { padding: 8px 18px; background: rgba(34,197,94,0.15); border: 1px solid rgba(34,197,94,0.4); color: #86efac; border-radius: 8px; cursor: pointer; font-size: 0.85rem; }
.btn-reject { padding: 8px 18px; background: rgba(239,68,68,0.15); border: 1px solid rgba(239,68,68,0.4); color: #fca5a5; border-radius: 8px; cursor: pointer; font-size: 0.85rem; }

/* 월간 통계 */
.monthly-stats { margin-top: 28px; }
.stats-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.stats-header h3 { font-size: 1.05rem; }
.stats-controls select { padding: 6px 12px; background: rgba(255,255,255,0.1); border: 1px solid rgba(255,255,255,0.2); border-radius: 8px; color: white; outline: none; cursor: pointer; }
.stats-grid { display: flex; flex-direction: column; gap: 10px; }
.stat-item { display: flex; align-items: center; gap: 12px; }
.stat-month { width: 32px; color: rgba(255,255,255,0.6); font-size: 0.85rem; flex-shrink: 0; }
.stat-bar-wrap { flex: 1; background: rgba(255,255,255,0.06); border-radius: 6px; height: 10px; overflow: hidden; }
.stat-bar { height: 100%; background: linear-gradient(to right, #7c3aed, #a78bfa); border-radius: 6px; transition: width 0.4s; }
.stat-val { min-width: 90px; text-align: right; font-size: 0.82rem; color: rgba(255,255,255,0.7); }
</style>
