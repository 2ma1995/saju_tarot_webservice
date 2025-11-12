<template>
  <div class="review-card">
    <div class="card-header">
      <span class="rating">⭐ {{ review.rating }}</span>
      <button class="favorite-btn">♥</button>
    </div>

    <div class="counselor-image">
      <img :src="review.counselorImage || '/placeholder.png'" alt="상담사">
    </div>

    <div class="card-content">
      <h4>{{ review.counselorName }}</h4>
      <div class="tags">
        <span v-for="tag in review.tags" :key="tag" class="tag">
          {{ tag }}
        </span>
      </div>

      <div class="review-count">
        <p>review({{ review.reviewCount }})</p>
      </div>

      <div class="reviews">
        <p v-for="(text, idx) in review.recentReviews" :key="idx" class="review-text">
          · {{ text }}
        </p>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ReviewCard',
  props: {
    review: {
      type: Object,
      required: true
    }
  }
};
</script>

<style scoped>
.review-card {
  border: 1px solid #ddd;
  border-radius: 10px;
  padding: 15px;
  background: white;
}

.card-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 15px;
}

.rating {
  color: #ffb800;
  font-weight: bold;
}

.favorite-btn {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  color: #ff4444;
}

.counselor-image {
  width: 100%;
  aspect-ratio: 1;
  background: #e0e0e0;
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 15px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.counselor-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.card-content h4 {
  margin: 10px 0;
  font-size: 16px;
}

.tags {
  margin: 10px 0;
}

.tag {
  display: inline-block;
  padding: 3px 10px;
  background: #f0f0f0;
  border-radius: 15px;
  font-size: 12px;
  margin-right: 5px;
}

.review-count {
  margin: 10px 0;
  font-size: 12px;
  color: #666;
}

.reviews {
  margin-top: 10px;
}

.review-text {
  font-size: 12px;
  color: #666;
  margin: 5px 0;
}
</style>