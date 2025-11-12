<template>
  <div class="counselor-list-page">
    <h1>상담사 목록</h1>
    <div v-if="loading" class="loading">
      <p>로딩 중...</p>
    </div>
    <div v-else-if="error" class="error">
      <p>{{ error }}</p>
    </div>
    <div v-else>
      <ul>
        <li v-for="counselor in counselors" :key="counselor.id">
          <router-link :to="'/counselor/' + counselor.id">
            {{ counselor.name }}
          </router-link>
        </li>
      </ul>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue';
import { getCounselors } from '@/api/counselor'; // 상담사 목록을 불러오는 API

export default {
  name: 'CounselorListPage',
  setup() {
    const counselors = ref([]);
    const loading = ref(true);
    const error = ref(null);

    const fetchCounselors = async () => {
      try {
        loading.value = true;
        error.value = null;
        counselors.value = await getCounselors(); // 상담사 목록 API 호출
      } catch (err) {
        error.value = '상담사 목록을 불러오는 데 실패했습니다.';
        console.error(err);
      } finally {
        loading.value = false;
      }
    };

    onMounted(() => {
      fetchCounselors();
    });

    return {
      counselors,
      loading,
      error
    };
  }
};
</script>

<style scoped>
/* 스타일링 코드 */
</style>
