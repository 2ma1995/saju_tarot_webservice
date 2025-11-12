<template>
  <div class="counselor-detail-page">
    <!-- 로딩 중 -->
    <div v-if="loading" class="loading">
      <p>로딩 중...</p>
    </div>

    <!-- 에러 발생 -->
    <div v-else-if="error" class="error">
      <p>{{ error }}</p>
    </div>

    <!-- 데이터 로드 완료 -->
    <template v-else>
      <CounselorProfile v-if="counselor" :counselor="counselor" />

      <div class="ad-banner">
        <div v-for="n in 8" :key="n" class="ad-box">
          <div class="placeholder">광고</div>
        </div>
      </div>

      <ReviewSection
          title="인기 상담사"
          :counselors="popularCounselors"
          type="popular"
      />

      <div class="related-sections">
        <RelatedCounselors
            v-for="category in categories"
            :key="category.type"
            :title="category.title"
            :counselors="category.counselors"
        />
      </div>

      <button class="view-all-btn" @click="goToList">상담사 전체보기</button>
    </template>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import CounselorProfile from '@/components/CounselorProfile.vue';
import ReviewSection from '@/components/ReviewSection.vue';
import RelatedCounselors from '@/components/RelatedCounselors.vue';
import {
  getCounselorDetail,
  getPopularCounselors,
  getNewCounselors,
  searchCounselorsByTag
} from '@/api/counselor';

export default {
  name: 'CounselorDetailPage',
  components: {
    CounselorProfile,
    ReviewSection,
    RelatedCounselors
  },
  setup() {
    const route = useRoute();
    const router = useRouter();

    const counselor = ref(null);
    const popularCounselors = ref([]);
    const loading = ref(true);
    const error = ref(null);

    const categories = ref([
      { type: 'tarot', title: '타로 상담사', counselors: [] },
      { type: 'saju', title: '사주 상담사', counselors: [] },
      { type: 'popular', title: '인기 상담사', counselors: [] },
      { type: 'new', title: '신규 상담사', counselors: [] }
    ]);

    // 데이터 로드
    const fetchData = async () => {
      try {
        loading.value = true;
        error.value = null;

        const counselorId = route.params.id;

        // API 병렬 호출
        const [counselorData, popularData, newData, tarotData, sajuData] = await Promise.all([
          getCounselorDetail(counselorId),
          getPopularCounselors(),
          getNewCounselors(),
          searchCounselorsByTag('타로'),
          searchCounselorsByTag('사주')
        ]);

        counselor.value = counselorData;
        popularCounselors.value = popularData;

        categories.value[0].counselors = tarotData;
        categories.value[1].counselors = sajuData;
        categories.value[2].counselors = popularData;
        categories.value[3].counselors = newData;

      } catch (err) {
        console.error('데이터 로드 실패:', err);
        error.value = '데이터를 불러오는데 실패했습니다.';
      } finally {
        loading.value = false;
      }
    };

    // 상담사 전체보기 이동
    const goToList = () => {
      router.push('/counselors');
    };

    onMounted(() => {
      fetchData();
    });

    return {
      counselor,
      popularCounselors,
      categories,
      loading,
      error,
      goToList
    };
  }
};
</script>

<style scoped>
/* 스타일링 코드 */
</style>
