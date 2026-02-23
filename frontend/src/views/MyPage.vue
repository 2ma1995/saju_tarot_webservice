<template>
  <div class="page">
    <h1>마이페이지</h1>

    <!-- 탭 -->
    <div class="tabs">
      <button :class="{ active: tab === 'reservations' }" @click="tab = 'reservations'">📅 내 예약</button>
      <button :class="{ active: tab === 'payments' }" @click="tab = 'payments'">💳 결제 내역</button>
      <button :class="{ active: tab === 'favorites' }" @click="tab = 'favorites'; fetchFavorites()">❤️ 즐겨찾기</button>
      <button :class="{ active: tab === 'reviews' }" @click="tab = 'reviews'; fetchReviews()">✍️ 내 후기</button>
      <button :class="{ active: tab === 'role' }" @click="tab = 'role'">🧑‍💼 상담사 신청</button>
    </div>

    <!-- 내 예약 -->
    <div v-if="tab === 'reservations'">
      <div v-if="loading" class="loading"><div class="spinner"></div></div>
      <div v-else-if="reservations.length === 0" class="empty">예약 내역이 없습니다.</div>
      <div v-else class="card-list">
        <div v-for="r in reservations" :key="r.id" class="card">
          <div class="card-top">
            <div class="card-icon">📅</div>
            <div class="card-info">
              <h3>상담사 #{{ r.counselorId || '-' }}</h3>
              <p>{{ formatDate(r.reservationTime) }}</p>
              <p class="note" v-if="r.note">📝 {{ r.note }}</p>
            </div>
            <span class="badge" :class="statusClass(r.reservationStatus)">
              {{ statusLabel(r.reservationStatus) }}
            </span>
          </div>
          <div class="card-actions">
            <button v-if="r.reservationStatus === 'RESERVED'" class="btn-cancel" @click="cancelRes(r.id)">예약 취소</button>
            <button v-if="r.reservationStatus === 'COMPLETED' && !r.hasReview" class="btn-review" @click="openReviewModal(r)">✍️ 후기 작성</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 결제 내역 -->
    <div v-if="tab === 'payments'">
      <div v-if="loadingPay" class="loading"><div class="spinner"></div></div>
      <div v-else-if="payments.length === 0" class="empty">결제 내역이 없습니다.</div>
      <div v-else class="card-list">
        <div v-for="p in payments" :key="p.id" class="card">
          <div class="card-top">
            <div class="card-icon">💳</div>
            <div class="card-info">
              <h3>{{ p.amount?.toLocaleString() }}원</h3>
              <p>{{ p.method }} · {{ formatDate(p.paidAt) }}</p>
              <p class="tx">거래 ID: {{ p.transactionId }}</p>
            </div>
            <span class="badge" :class="payStatusClass(p.paymentStatus)">
              {{ payStatusLabel(p.paymentStatus) }}
            </span>
          </div>
          <div class="card-actions" v-if="p.paymentStatus === 'PAID'">
            <button class="btn-cancel" @click="refund(p.id)">환불 요청</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 즐겨찾기 -->
    <div v-if="tab === 'favorites'">
      <div v-if="loadingFav" class="loading"><div class="spinner"></div></div>
      <div v-else-if="favorites.length === 0" class="empty">즐겨찾기한 상담사가 없습니다.</div>
      <div v-else class="fav-grid">
        <div v-for="f in favorites" :key="f.counselorId || f.id" class="fav-card" @click="goToDetail(f)">
          <div class="fav-avatar">{{ (f.name || '?').charAt(0) }}</div>
          <div class="fav-info">
            <h3>{{ f.name }}</h3>
            <p class="fav-bio">{{ f.bio || '소개 없음' }}</p>
            <div class="fav-meta">
              <span>⭐ {{ f.rating?.toFixed(1) || f.averageRating?.toFixed(1) || '신규' }}</span>
              <span v-if="f.tags?.length" class="fav-tags">{{ f.tags.slice(0, 3).join(' · ') }}</span>
            </div>
          </div>
          <button class="btn-unfav" @click.stop="unfavorite(f)" title="즐겨찾기 해제">❤️</button>
        </div>
      </div>
    </div>

    <!-- 내 후기 -->
    <div v-if="tab === 'reviews'">
      <div v-if="loadingReviews" class="loading"><div class="spinner"></div></div>
      <div v-else-if="reviews.length === 0" class="empty">작성한 후기가 없습니다.</div>
      <div v-else class="card-list">
        <div v-for="rv in reviews" :key="rv.reviewId" class="card">
          <div class="card-top">
            <div class="card-icon">⭐</div>
            <div class="card-info">
              <h3>{{ rv.counselorName || '상담사' }}</h3>
              <div class="stars">
                <span v-for="i in 5" :key="i" :class="i <= rv.score ? 'star-on' : 'star-off'">★</span>
                <span class="score-text">{{ rv.score }}/5</span>
              </div>
              <p class="review-comment">{{ rv.comment }}</p>
              <p class="review-date">{{ formatDate(rv.createdAt) }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 상담사 신청 -->
    <div v-if="tab === 'role'" class="role-section">
      <div class="role-card">
        <div class="role-icon">🧑‍💼</div>
        <h2>상담사 전환 신청</h2>
        <p class="role-desc">상담사로 전환하시면 서비스를 제공하고 스케줄을 관리할 수 있습니다.<br/>관리자 승인 후 역할이 변경됩니다.</p>

        <div v-if="currentRole === 'COUNSELOR'" class="role-status counselor">
          ✅ 이미 상담사로 활동 중입니다.
        </div>
        <div v-else-if="roleApplied" class="role-status pending">
          ⏳ 상담사 신청이 접수되었습니다. 관리자 승인을 기다려주세요.
        </div>
        <div v-else>
          <button class="btn-apply" @click="applyRole" :disabled="applyingRole">
            {{ applyingRole ? '신청 중...' : '📋 상담사 신청하기' }}
          </button>
          <div v-if="roleMsg" class="role-msg" :class="roleError ? 'error' : 'success'">{{ roleMsg }}</div>
        </div>
      </div>
    </div>

    <!-- 후기 작성 모달 -->
    <div v-if="showReviewModal" class="modal-overlay" @click.self="showReviewModal = false">
      <div class="modal">
        <h2>후기 작성</h2>
        <p class="modal-sub">상담사 #{{ reviewForm.counselorId }} 대한 후기를 남겨주세요</p>

        <!-- 별점 선택 -->
        <div class="star-select">
          <span
            v-for="i in 5"
            :key="i"
            class="star-btn"
            :class="i <= reviewForm.score ? 'star-on' : 'star-off'"
            @click="reviewForm.score = i"
          >★</span>
          <span class="score-label">{{ reviewForm.score }}점</span>
        </div>

        <textarea
          v-model="reviewForm.comment"
          placeholder="상담 후기를 입력해주세요 (최소 10자)"
          class="review-textarea"
          rows="4"
        ></textarea>

        <div class="modal-actions">
          <button class="btn-cancel-plain" @click="showReviewModal = false">취소</button>
          <button class="btn-submit" @click="submitReview" :disabled="submittingReview">
            {{ submittingReview ? '등록 중...' : '후기 등록' }}
          </button>
        </div>

        <div v-if="reviewMsg" class="review-msg" :class="reviewError ? 'error' : 'success'">{{ reviewMsg }}</div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { getMyReservations, cancelReservation } from '@/api/reservation';
import { getMyPayments, refundPayment } from '@/api/payment';
import { getMyFavorites, removeFavorite } from '@/api/favorite';
import { createReview, getMyReviews } from '@/api/review';
import { requestCounselorRole } from '@/api/role';
import { getCurrentUser } from '@/api/auth';

export default {
  name: 'MyPage',
  setup() {
    const router = useRouter();
    const tab = ref('reservations');

    // 예약
    const reservations = ref([]);
    const loading = ref(false);

    // 결제
    const payments = ref([]);
    const loadingPay = ref(false);

    // 즐겨찾기
    const favorites = ref([]);
    const loadingFav = ref(false);

    // 내 후기
    const reviews = ref([]);
    const loadingReviews = ref(false);

    // 후기 작성 모달
    const showReviewModal = ref(false);
    const reviewForm = ref({ reservationId: null, counselorId: null, score: 5, comment: '' });
    const submittingReview = ref(false);
    const reviewMsg = ref('');
    const reviewError = ref(false);

    // 역할 신청
    const currentUser = getCurrentUser();
    const currentRole = ref(currentUser?.role || 'USER');
    const roleApplied = ref(false);
    const applyingRole = ref(false);
    const roleMsg = ref('');
    const roleError = ref(false);

    const fetchReservations = async () => {
      loading.value = true;
      try { reservations.value = await getMyReservations(); } catch { reservations.value = []; } finally { loading.value = false; }
    };

    const fetchPayments = async () => {
      loadingPay.value = true;
      try { payments.value = await getMyPayments(); } catch { payments.value = []; } finally { loadingPay.value = false; }
    };

    const fetchFavorites = async () => {
      loadingFav.value = true;
      try { favorites.value = await getMyFavorites(); } catch { favorites.value = []; } finally { loadingFav.value = false; }
    };

    const fetchReviews = async () => {
      loadingReviews.value = true;
      try { reviews.value = await getMyReviews(); } catch { reviews.value = []; } finally { loadingReviews.value = false; }
    };

    onMounted(() => { fetchReservations(); fetchPayments(); });

    const cancelRes = async (id) => {
      if (!confirm('예약을 취소하시겠습니까?')) return;
      try { await cancelReservation(id); await fetchReservations(); } catch (e) { alert(e.response?.data?.message || '취소 실패'); }
    };

    const refund = async (id) => {
      if (!confirm('환불을 요청하시겠습니까?')) return;
      try { await refundPayment(id); await fetchPayments(); alert('환불이 완료되었습니다.'); } catch (e) { alert(e.response?.data?.message || '환불 실패'); }
    };

    const unfavorite = async (f) => {
      const counselorId = f.counselorId || f.id;
      if (!confirm(`"${f.name}" 상담사를 즐겨찾기에서 해제하시겠습니까?`)) return;
      try {
        await removeFavorite(counselorId);
        favorites.value = favorites.value.filter(x => (x.counselorId || x.id) !== counselorId);
      } catch (e) {
        alert(e.response?.data?.message || '즐겨찾기 해제 실패');
      }
    };

    const goToDetail = (f) => {
      router.push(`/counselors/${f.counselorId || f.id}`);
    };

    const applyRole = async () => {
      applyingRole.value = true;
      roleMsg.value = '';
      try {
        await requestCounselorRole();
        roleApplied.value = true;
        roleMsg.value = '✅ 상담사 신청이 접수되었습니다!';
        roleError.value = false;
      } catch (e) {
        roleMsg.value = e.response?.data?.message || '신청에 실패했습니다.';
        roleError.value = true;
      } finally {
        applyingRole.value = false;
      }
    };

    const openReviewModal = (reservation) => {
      reviewForm.value = {
        reservationId: reservation.id,
        counselorId: reservation.counselorId,
        score: 5,
        comment: ''
      };
      reviewMsg.value = '';
      reviewError.value = false;
      showReviewModal.value = true;
    };

    const submitReview = async () => {
      if (reviewForm.value.comment.trim().length < 10) {
        reviewMsg.value = '후기를 10자 이상 입력해주세요.';
        reviewError.value = true;
        return;
      }
      submittingReview.value = true;
      reviewMsg.value = '';
      try {
        await createReview({
          reservationId: reviewForm.value.reservationId,
          counselorId: reviewForm.value.counselorId,
          score: reviewForm.value.score,
          comment: reviewForm.value.comment.trim()
        });
        reviewMsg.value = '✅ 후기가 등록되었습니다!';
        reviewError.value = false;
        // 해당 예약에 후기 완료 플래그 표시
        const res = reservations.value.find(r => r.id === reviewForm.value.reservationId);
        if (res) res.hasReview = true;
        setTimeout(() => { showReviewModal.value = false; }, 1500);
      } catch (e) {
        reviewMsg.value = e.response?.data?.message || '후기 등록에 실패했습니다.';
        reviewError.value = true;
      } finally {
        submittingReview.value = false;
      }
    };

    const formatDate = (dt) => dt ? new Date(dt).toLocaleString('ko-KR') : '-';
    const statusLabel = (s) => ({ RESERVED: '예약됨', CONFIRMED: '확정', COMPLETED: '완료', CANCELLED: '취소' }[s] || s);
    const statusClass = (s) => ({ RESERVED: 'blue', CONFIRMED: 'green', COMPLETED: 'gray', CANCELLED: 'red' }[s] || '');
    const payStatusLabel = (s) => ({ PAID: '결제완료', PENDING: '대기중', REFUND: '환불됨' }[s] || s);
    const payStatusClass = (s) => ({ PAID: 'green', PENDING: 'blue', REFUND: 'red' }[s] || '');

    return {
      tab, reservations, payments, favorites, reviews,
      loading, loadingPay, loadingFav, loadingReviews,
      showReviewModal, reviewForm, submittingReview, reviewMsg, reviewError,
      currentRole, roleApplied, applyingRole, roleMsg, roleError,
      cancelRes, refund, unfavorite, fetchFavorites, fetchReviews, goToDetail,
      openReviewModal, submitReview, applyRole,
      formatDate, statusLabel, statusClass, payStatusLabel, payStatusClass
    };
  }
};
</script>

<style scoped>
.page { background: #0d0d1a; min-height: 100vh; padding: 32px 8%; color: white; }
h1 { font-size: 1.8rem; margin-bottom: 24px; }
.tabs { display: flex; gap: 8px; margin-bottom: 28px; flex-wrap: wrap; }
.tabs button { padding: 10px 20px; background: rgba(255,255,255,0.08); border: 1px solid rgba(255,255,255,0.15); border-radius: 10px; color: rgba(255,255,255,0.7); cursor: pointer; font-size: 0.95rem; transition: all 0.2s; }
.tabs button.active { background: linear-gradient(135deg, #7c3aed, #4f46e5); border-color: transparent; color: white; }
.loading { display: flex; justify-content: center; padding: 60px; }
.spinner { width: 36px; height: 36px; border: 3px solid rgba(167,139,250,0.2); border-top-color: #a78bfa; border-radius: 50%; animation: spin 0.8s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
.empty { text-align: center; padding: 60px; color: rgba(255,255,255,0.4); font-size: 1rem; }

/* 카드 공통 */
.card-list { display: flex; flex-direction: column; gap: 14px; }
.card { background: rgba(255,255,255,0.05); border: 1px solid rgba(255,255,255,0.1); border-radius: 14px; padding: 20px 24px; }
.card-top { display: flex; gap: 16px; align-items: flex-start; }
.card-icon { font-size: 2rem; }
.card-info { flex: 1; }
.card-info h3 { font-size: 1rem; margin-bottom: 4px; }
.card-info p { color: rgba(255,255,255,0.6); font-size: 0.85rem; margin-bottom: 2px; }
.note, .tx { font-size: 0.8rem !important; }
.badge { padding: 4px 12px; border-radius: 20px; font-size: 0.8rem; font-weight: 600; white-space: nowrap; flex-shrink: 0; }
.badge.blue { background: rgba(59,130,246,0.2); color: #93c5fd; }
.badge.green { background: rgba(34,197,94,0.2); color: #86efac; }
.badge.gray { background: rgba(255,255,255,0.1); color: rgba(255,255,255,0.5); }
.badge.red { background: rgba(239,68,68,0.2); color: #fca5a5; }
.card-actions { margin-top: 14px; padding-top: 14px; border-top: 1px solid rgba(255,255,255,0.08); display: flex; gap: 8px; }
.btn-cancel { padding: 8px 18px; background: rgba(239,68,68,0.2); border: 1px solid rgba(239,68,68,0.4); color: #fca5a5; border-radius: 8px; cursor: pointer; font-size: 0.85rem; }
.btn-review { padding: 8px 18px; background: rgba(124,58,237,0.2); border: 1px solid rgba(124,58,237,0.5); color: #c4b5fd; border-radius: 8px; cursor: pointer; font-size: 0.85rem; }

/* 즐겨찾기 */
.fav-grid { display: flex; flex-direction: column; gap: 14px; }
.fav-card { background: rgba(255,255,255,0.05); border: 1px solid rgba(255,255,255,0.1); border-radius: 14px; padding: 20px 24px; display: flex; align-items: center; gap: 16px; cursor: pointer; transition: all 0.2s; }
.fav-card:hover { background: rgba(167,139,250,0.08); border-color: rgba(167,139,250,0.3); transform: translateY(-1px); }
.fav-avatar { width: 52px; height: 52px; border-radius: 50%; background: linear-gradient(135deg, #7c3aed, #4f46e5); display: flex; align-items: center; justify-content: center; font-size: 1.3rem; font-weight: 700; flex-shrink: 0; }
.fav-info { flex: 1; }
.fav-info h3 { font-size: 1rem; margin-bottom: 4px; }
.fav-bio { color: rgba(255,255,255,0.55); font-size: 0.85rem; margin-bottom: 6px; }
.fav-meta { display: flex; gap: 12px; align-items: center; font-size: 0.82rem; color: rgba(255,255,255,0.7); }
.fav-tags { color: rgba(167,139,250,0.9); }
.btn-unfav { width: 36px; height: 36px; border-radius: 50%; background: rgba(255,100,100,0.15); border: 1px solid rgba(255,100,100,0.35); font-size: 1rem; cursor: pointer; flex-shrink: 0; transition: all 0.2s; display: flex; align-items: center; justify-content: center; }
.btn-unfav:hover { transform: scale(1.15); background: rgba(255,100,100,0.3); }

/* 내 후기 */
.stars { display: flex; align-items: center; gap: 2px; margin: 4px 0 6px; }
.star-on { color: #fbbf24; font-size: 1rem; }
.star-off { color: rgba(255,255,255,0.2); font-size: 1rem; }
.score-text { color: rgba(255,255,255,0.5); font-size: 0.8rem; margin-left: 4px; }
.review-comment { color: rgba(255,255,255,0.8); font-size: 0.9rem; line-height: 1.6; margin-bottom: 4px; }
.review-date { color: rgba(255,255,255,0.38); font-size: 0.78rem; }

/* 후기 작성 모달 */
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.65); display: flex; align-items: center; justify-content: center; z-index: 500; backdrop-filter: blur(4px); }
.modal { background: #1a1a2e; border: 1px solid rgba(255,255,255,0.12); border-radius: 20px; padding: 36px; width: 100%; max-width: 480px; }
.modal h2 { font-size: 1.4rem; margin-bottom: 6px; }
.modal-sub { color: rgba(255,255,255,0.5); font-size: 0.9rem; margin-bottom: 24px; }
.star-select { display: flex; align-items: center; gap: 6px; margin-bottom: 20px; }
.star-btn { font-size: 2rem; cursor: pointer; transition: transform 0.1s; }
.star-btn:hover { transform: scale(1.2); }
.star-btn.star-on { color: #fbbf24; }
.star-btn.star-off { color: rgba(255,255,255,0.2); }
.score-label { color: rgba(255,255,255,0.6); font-size: 0.9rem; margin-left: 8px; }
.review-textarea { width: 100%; padding: 12px 14px; background: rgba(255,255,255,0.08); border: 1px solid rgba(255,255,255,0.15); border-radius: 10px; color: white; outline: none; resize: vertical; box-sizing: border-box; margin-bottom: 20px; font-size: 0.95rem; line-height: 1.6; }
.review-textarea::placeholder { color: rgba(255,255,255,0.35); }
.modal-actions { display: flex; gap: 10px; justify-content: flex-end; }
.btn-cancel-plain { padding: 10px 22px; background: rgba(255,255,255,0.08); border: 1px solid rgba(255,255,255,0.15); border-radius: 10px; color: rgba(255,255,255,0.7); cursor: pointer; }
.btn-submit { padding: 10px 24px; background: linear-gradient(135deg, #7c3aed, #4f46e5); border: none; border-radius: 10px; color: white; font-weight: 600; cursor: pointer; }
.btn-submit:disabled { opacity: 0.5; cursor: not-allowed; }
.review-msg { margin-top: 14px; padding: 10px 14px; border-radius: 8px; font-size: 0.88rem; }
.review-msg.success { background: rgba(34,197,94,0.15); color: #86efac; border: 1px solid rgba(34,197,94,0.3); }
.review-msg.error { background: rgba(239,68,68,0.15); color: #fca5a5; border: 1px solid rgba(239,68,68,0.3); }

/* 상담사 신청 */
.role-section { display: flex; justify-content: center; padding: 40px 0; }
.role-card { background: rgba(255,255,255,0.05); border: 1px solid rgba(255,255,255,0.1); border-radius: 20px; padding: 48px; text-align: center; max-width: 480px; width: 100%; }
.role-icon { font-size: 3rem; margin-bottom: 16px; }
.role-card h2 { font-size: 1.5rem; margin-bottom: 12px; }
.role-desc { color: rgba(255,255,255,0.6); font-size: 0.95rem; line-height: 1.7; margin-bottom: 32px; }
.role-status { padding: 16px 24px; border-radius: 12px; font-size: 0.95rem; font-weight: 500; }
.role-status.counselor { background: rgba(34,197,94,0.1); color: #86efac; border: 1px solid rgba(34,197,94,0.3); }
.role-status.pending { background: rgba(251,191,36,0.1); color: #fde68a; border: 1px solid rgba(251,191,36,0.3); }
.btn-apply { padding: 14px 36px; background: linear-gradient(135deg, #7c3aed, #4f46e5); color: white; border: none; border-radius: 12px; font-size: 1rem; font-weight: 600; cursor: pointer; transition: opacity 0.2s; }
.btn-apply:disabled { opacity: 0.5; cursor: not-allowed; }
.role-msg { margin-top: 16px; padding: 10px 14px; border-radius: 8px; font-size: 0.88rem; }
.role-msg.success { background: rgba(34,197,94,0.15); color: #86efac; border: 1px solid rgba(34,197,94,0.3); }
.role-msg.error { background: rgba(239,68,68,0.15); color: #fca5a5; border: 1px solid rgba(239,68,68,0.3); }
</style>
