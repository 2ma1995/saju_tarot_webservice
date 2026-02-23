<template>
  <div class="home">
    <!-- 히어로 섹션 -->
    <section class="hero">
      <div class="hero-content">
        <div class="hero-badge">✨ 프리미엄 운세 상담</div>
        <h1>나의 운명을 <br><span class="gradient-text">읽어드립니다</span></h1>
        <p>전문 상담사와 함께하는 사주, 타로 상담<br>당신의 과거, 현재, 미래를 이야기합니다</p>
        <div class="hero-btns">
          <router-link to="/counselors" class="btn-primary">상담사 찾기</router-link>
          <router-link to="/login" class="btn-outline" v-if="!loggedIn">로그인</router-link>
          <router-link to="/mypage" class="btn-outline" v-else>마이페이지</router-link>
        </div>
      </div>
      <div class="hero-visual">🔮</div>
    </section>

    <!-- 기능 소개 -->
    <section class="features">
      <div class="feature-grid">
        <div class="feature-card">
          <div class="feature-icon">🌟</div>
          <h3>사주 분석</h3>
          <p>생년월일시를 바탕으로 사주를 분석하고 운명을 읽어드립니다</p>
        </div>
        <div class="feature-card">
          <div class="feature-icon">🃏</div>
          <h3>타로 상담</h3>
          <p>78장의 타로 카드로 현재 상황과 미래를 안내해드립니다</p>
        </div>
        <div class="feature-card">
          <div class="feature-icon">💫</div>
          <h3>전문 상담사</h3>
          <p>검증된 전문 상담사들과 1:1 맞춤 상담을 진행합니다</p>
        </div>
        <div class="feature-card">
          <div class="feature-icon">🔒</div>
          <h3>안전한 결제</h3>
          <p>Toss Payments를 통한 안전하고 편리한 결제 시스템</p>
        </div>
      </div>
    </section>

    <!-- 인기 상담사 미리보기 -->
    <section class="popular" v-if="counselors.length">
      <div class="section-header">
        <h2>인기 상담사</h2>
        <router-link to="/counselors" class="see-all">전체보기 →</router-link>
      </div>
      <div class="counselor-grid">
        <div
          v-for="c in counselors.slice(0, 4)"
          :key="c.id"
          class="counselor-card"
          @click="$router.push('/counselors/' + c.id)"
        >
          <div class="counselor-avatar">{{ c.name?.charAt(0) || '?' }}</div>
          <h4>{{ c.name }}</h4>
          <p class="counselor-nick">{{ c.nickname }}</p>
          <div class="rating">⭐ {{ c.averageRating?.toFixed(1) || '신규' }}</div>
        </div>
      </div>
    </section>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue';
import { isLoggedIn } from '@/api/auth';
import { getCounselors } from '@/api/counselor';

export default {
  name: 'HomePage',
  setup() {
    const loggedIn = ref(isLoggedIn());
    const counselors = ref([]);

    onMounted(async () => {
      try {
        const data = await getCounselors({ sort: 'rating', size: 4 });
        counselors.value = data.content || data;
      } catch {}
    });

    return { loggedIn, counselors };
  }
};
</script>

<style scoped>
.home { background: #0d0d1a; min-height: 100vh; color: white; }

/* 히어로 */
.hero {
  display: flex; align-items: center; justify-content: space-between;
  padding: 80px 10%; min-height: 60vh;
  background: linear-gradient(135deg, #1a0533 0%, #0f3460 100%);
}
.hero-badge {
  display: inline-block; padding: 6px 16px;
  background: rgba(167,139,250,0.2); border: 1px solid rgba(167,139,250,0.4);
  border-radius: 20px; color: #a78bfa; font-size: 0.85rem; margin-bottom: 16px;
}
.hero h1 { font-size: 3rem; font-weight: 800; line-height: 1.2; margin-bottom: 16px; }
.gradient-text { background: linear-gradient(135deg, #a78bfa, #60a5fa); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
.hero p { color: rgba(255,255,255,0.7); font-size: 1.1rem; line-height: 1.7; margin-bottom: 32px; }
.hero-btns { display: flex; gap: 12px; }
.btn-primary { padding: 14px 28px; background: linear-gradient(135deg, #7c3aed, #4f46e5); color: white; border-radius: 10px; text-decoration: none; font-weight: 600; }
.btn-outline { padding: 14px 28px; border: 1px solid rgba(255,255,255,0.3); color: white; border-radius: 10px; text-decoration: none; font-weight: 600; }
.hero-visual { font-size: 8rem; animation: float 3s ease-in-out infinite; }
@keyframes float { 0%,100%{transform:translateY(0)} 50%{transform:translateY(-20px)} }

/* 기능 섹션 */
.features { padding: 80px 10%; }
.feature-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 24px; }
.feature-card { background: rgba(255,255,255,0.05); border: 1px solid rgba(255,255,255,0.1); border-radius: 16px; padding: 28px; text-align: center; transition: transform 0.2s; }
.feature-card:hover { transform: translateY(-4px); }
.feature-icon { font-size: 2.5rem; margin-bottom: 12px; }
.feature-card h3 { margin-bottom: 8px; font-size: 1.1rem; }
.feature-card p { color: rgba(255,255,255,0.6); font-size: 0.9rem; line-height: 1.6; }

/* 인기 상담사 */
.popular { padding: 0 10% 80px; }
.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.section-header h2 { font-size: 1.5rem; }
.see-all { color: #a78bfa; text-decoration: none; font-size: 0.9rem; }
.counselor-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }
.counselor-card { background: rgba(255,255,255,0.05); border: 1px solid rgba(255,255,255,0.1); border-radius: 16px; padding: 24px; text-align: center; cursor: pointer; transition: all 0.2s; }
.counselor-card:hover { background: rgba(167,139,250,0.1); border-color: rgba(167,139,250,0.4); transform: translateY(-2px); }
.counselor-avatar { width: 60px; height: 60px; background: linear-gradient(135deg, #7c3aed, #4f46e5); border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 1.5rem; font-weight: 700; margin: 0 auto 12px; }
.counselor-card h4 { font-size: 1rem; margin-bottom: 4px; }
.counselor-nick { color: rgba(255,255,255,0.5); font-size: 0.85rem; margin-bottom: 8px; }
.rating { color: #fbbf24; font-size: 0.9rem; }

@media (max-width: 768px) {
  .hero { flex-direction: column; padding: 40px 5%; text-align: center; }
  .hero h1 { font-size: 2rem; }
  .hero-visual { font-size: 5rem; margin-top: 20px; }
  .feature-grid, .counselor-grid { grid-template-columns: repeat(2, 1fr); }
}
</style>
