<template>
  <div class="page">
    <!-- 필터 바 -->
    <div class="filter-bar">
      <h1>상담사 목록</h1>
      <div class="filters">
        <select v-model="sort" @change="fetchCounselors">
          <option value="rating">평점순</option>
          <option value="reviews">리뷰순</option>
          <option value="recent">최신순</option>
        </select>
        <input v-model="keyword" @keyup.enter="fetchCounselors" placeholder="상담사 이름/닉네임 검색" class="search-input" />
        <button @click="fetchCounselors" class="btn-search">검색</button>
      </div>
    </div>

    <div v-if="loading" class="loading">
      <div class="spinner"></div>
    </div>

    <div v-else-if="counselors.length === 0" class="empty">
      <p>🔍 상담사를 찾을 수 없습니다.</p>
    </div>

    <div v-else class="counselor-grid">
      <div
        v-for="c in counselors"
        :key="c.id"
        class="counselor-card"
        @click="$router.push('/counselors/' + c.id)"
      >
        <div class="avatar">{{ c.name?.charAt(0) || '?' }}</div>
        <div class="info">
          <h3>{{ c.name }}</h3>
          <p class="nick">@{{ c.nickname }}</p>
          <div class="rating">⭐ {{ c.averageRating?.toFixed(1) || '신규' }} &nbsp;·&nbsp; 리뷰 {{ c.reviewCount || 0 }}개</div>
        </div>
        <div class="card-actions" @click.stop>
          <button
            v-if="loggedIn"
            class="btn-fav"
            :class="{ active: favoriteIds.has(c.id) }"
            @click="toggleFavorite(c)"
            :title="favoriteIds.has(c.id) ? '즐겨찾기 해제' : '즐겨찾기 추가'"
          >
            {{ favoriteIds.has(c.id) ? '❤️' : '🤍' }}
          </button>
          <button class="btn-detail">상담 예약</button>
        </div>
      </div>
    </div>

    <!-- 페이지네이션 -->
    <div class="pagination" v-if="totalPages > 1">
      <button @click="changePage(page - 1)" :disabled="page === 0">‹</button>
      <span>{{ page + 1 }} / {{ totalPages }}</span>
      <button @click="changePage(page + 1)" :disabled="page >= totalPages - 1">›</button>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue';
import { getCounselors } from '@/api/counselor';
import { addFavorite, removeFavorite, getMyFavorites } from '@/api/favorite';
import { isLoggedIn } from '@/api/auth';

export default {
  name: 'CounselorListPage',
  setup() {
    const counselors = ref([]);
    const loading = ref(false);
    const sort = ref('rating');
    const keyword = ref('');
    const page = ref(0);
    const totalPages = ref(1);
    const favoriteIds = ref(new Set());
    const loggedIn = isLoggedIn();

    const fetchCounselors = async () => {
      loading.value = true;
      try {
        const data = await getCounselors({ sort: sort.value, page: page.value, size: 12, keyword: keyword.value || undefined });
        counselors.value = data.content || data;
        totalPages.value = data.totalPages || 1;
      } catch (e) {
        counselors.value = [];
      } finally {
        loading.value = false;
      }
    };

    const fetchFavorites = async () => {
      if (!loggedIn) return;
      try {
        const list = await getMyFavorites();
        favoriteIds.value = new Set((list || []).map(f => f.counselorId || f.id));
      } catch {}
    };

    const toggleFavorite = async (counselor) => {
      const id = counselor.id;
      try {
        if (favoriteIds.value.has(id)) {
          await removeFavorite(id);
          favoriteIds.value = new Set([...favoriteIds.value].filter(x => x !== id));
        } else {
          await addFavorite(id);
          favoriteIds.value = new Set([...favoriteIds.value, id]);
        }
      } catch (e) {
        alert(e.response?.data?.message || '즐겨찾기 처리 실패');
      }
    };

    const changePage = (p) => { page.value = p; fetchCounselors(); };

    onMounted(() => {
      fetchCounselors();
      fetchFavorites();
    });

    return { counselors, loading, sort, keyword, page, totalPages, favoriteIds, loggedIn, fetchCounselors, changePage, toggleFavorite };
  }
};
</script>

<style scoped>
.page { background: #0d0d1a; min-height: 100vh; padding: 32px 8%; color: white; }
.filter-bar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 32px; flex-wrap: wrap; gap: 16px; }
.filter-bar h1 { font-size: 1.8rem; }
.filters { display: flex; gap: 10px; align-items: center; }
.filters select { padding: 10px 14px; background: rgba(255,255,255,0.1); border: 1px solid rgba(255,255,255,0.2); border-radius: 8px; color: white; outline: none; cursor: pointer; }
.search-input { padding: 10px 14px; background: rgba(255,255,255,0.1); border: 1px solid rgba(255,255,255,0.2); border-radius: 8px; color: white; outline: none; width: 220px; }
.search-input::placeholder { color: rgba(255,255,255,0.4); }
.btn-search { padding: 10px 18px; background: #7c3aed; color: white; border: none; border-radius: 8px; cursor: pointer; }
.loading { display: flex; justify-content: center; padding: 80px; }
.spinner { width: 40px; height: 40px; border: 3px solid rgba(167,139,250,0.2); border-top-color: #a78bfa; border-radius: 50%; animation: spin 0.8s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
.empty { text-align: center; padding: 80px; color: rgba(255,255,255,0.5); font-size: 1.2rem; }
.counselor-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 20px; }
.counselor-card { background: rgba(255,255,255,0.05); border: 1px solid rgba(255,255,255,0.1); border-radius: 16px; padding: 24px; display: flex; align-items: center; gap: 16px; cursor: pointer; transition: all 0.2s; }
.counselor-card:hover { background: rgba(167,139,250,0.1); border-color: rgba(167,139,250,0.4); transform: translateY(-2px); }
.avatar { width: 56px; height: 56px; border-radius: 50%; background: linear-gradient(135deg, #7c3aed, #4f46e5); display: flex; align-items: center; justify-content: center; font-size: 1.4rem; font-weight: 700; flex-shrink: 0; }
.info { flex: 1; }
.info h3 { font-size: 1.05rem; margin-bottom: 2px; }
.nick { color: rgba(255,255,255,0.5); font-size: 0.85rem; margin-bottom: 6px; }
.rating { color: #fbbf24; font-size: 0.85rem; }
.card-actions { display: flex; flex-direction: column; gap: 8px; align-items: center; flex-shrink: 0; }
.btn-fav { width: 36px; height: 36px; border-radius: 50%; background: rgba(255,255,255,0.08); border: 1px solid rgba(255,255,255,0.15); font-size: 1.1rem; cursor: pointer; transition: all 0.2s; display: flex; align-items: center; justify-content: center; }
.btn-fav:hover { transform: scale(1.15); background: rgba(255,100,100,0.15); border-color: rgba(255,100,100,0.4); }
.btn-fav.active { background: rgba(255,100,100,0.2); border-color: rgba(255,100,100,0.5); }
.btn-detail { padding: 8px 16px; background: linear-gradient(135deg, #7c3aed, #4f46e5); color: white; border: none; border-radius: 8px; font-size: 0.85rem; cursor: pointer; white-space: nowrap; }
.pagination { display: flex; justify-content: center; align-items: center; gap: 20px; margin-top: 40px; }
.pagination button { width: 36px; height: 36px; background: rgba(255,255,255,0.1); border: 1px solid rgba(255,255,255,0.2); border-radius: 8px; color: white; font-size: 1.2rem; cursor: pointer; }
.pagination button:disabled { opacity: 0.3; cursor: not-allowed; }
</style>
