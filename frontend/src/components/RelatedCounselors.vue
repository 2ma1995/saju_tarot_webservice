<template>
  <div class="related-counselors">
    <h3>{{ title }}</h3>
    <div class="counselor-grid">
      <ReviewCard
          v-for="counselor in counselors"
          :key="counselor.id"
          :review="counselor"
      />
    </div>
  </div>
</template>

<script>
import ReviewCard from './ReviewCard.vue';

export default {
  name: 'RelatedCounselors',
  components: { ReviewCard },
  props: {
    title: String,
    counselors: Array
  }
}
</script>

<style scoped>
.related-counselors {
  margin: 50px 0;
}

.related-counselors h3 {
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 2px solid #333;
}

.counselor-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}
</style>