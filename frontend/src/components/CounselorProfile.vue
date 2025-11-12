<template>
  <div class="counselor-profile">
    <!-- 상단 헤더 -->
    <div class="header">
      <div class="nav-icons">
        <button @click="goBack">←</button>
        <span>빛을 상담사</span>
      </div>
      <div class="action-icons">
        <button>🏠</button>
        <button>⚙️</button>
      </div>
    </div>

    <!-- 메인 프로필 -->
    <div class="profile-main">
      <div class="profile-image">
        <img :src="counselor.imageUrl || '/placeholder.png'" alt="프로필">
      </div>
      <button class="booking-btn">나의의 상담사 찾기</button>
    </div>

    <!-- 상담사 정보 -->
    <div class="profile-info">
      <p>{{ counselor.bio }}</p>
      <div class="tags">
        <span v-for="tag in counselor.tags" :key="tag" class="tag">
          #{{ tag }}
        </span>
      </div>
    </div>

    <!-- 광고 시간 섹션 -->
    <div class="ad-section">
      <h3>인기 상담사</h3>
      <div class="ad-grid">
        <div v-for="n in 5" :key="n" class="ad-card">
          <div class="ad-image">📷</div>
          <p>상담사 광고 시간</p>
        </div>
      </div>
      <button class="more-ads-btn">인기 상담사 전체 보기</button>
    </div>
  </div>
</template>

<script>
import { useRouter } from 'vue-router';

export default {
  name: 'CounselorProfile',
  props: {
    counselor: {
      type: Object,
      required: true
    }
  },
  setup() {
    const router = useRouter();

    const goBack = () => {
      router.back();
    };

    return {
      goBack
    };
  }
};
</script>

<style scoped>
/* 스타일링 코드 */
</style>
