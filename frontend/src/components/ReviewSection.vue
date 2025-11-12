<template>
  <div class="review-section">
    <h2>{{ title }}</h2>
    <div class="review-grid">
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
  name: 'ReviewSection',
  components: { ReviewCard },
  props: {
    title: String,
    counselors: Array,
    type: String
  }
}
</script>

<style scoped>
.review-section {
  margin: 40px 0;
}

.review-section h2 {
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 2px solid #333;
}

.review-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
  margin-top: 20px;
}
</style>
