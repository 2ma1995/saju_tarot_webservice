<template>
  <div class="page">
    <h1>🖼️ 프로필 관리</h1>
    <p class="sub-desc">상담사 프로필을 등록하거나 수정할 수 있습니다.</p>

    <div v-if="loading" class="loading"><div class="spinner"></div></div>
    <div v-else class="form-wrap">

      <!-- 프로필 이미지 -->
      <div class="image-section">
        <div class="avatar-preview" :style="imageUrl ? { backgroundImage: `url(${imageUrl})` } : {}">
          <span v-if="!imageUrl">{{ userInitial }}</span>
        </div>
        <div class="image-actions">
          <label class="btn-upload" :class="{ loading: uploadingImage }">
            {{ uploadingImage ? '업로드 중...' : '📷 이미지 변경' }}
            <input type="file" accept="image/*" @change="handleImageChange" :disabled="uploadingImage" style="display:none" />
          </label>
          <p class="img-hint">JPG, PNG 권장 (최대 5MB)</p>
        </div>
      </div>

      <!-- 프로필 폼 -->
      <div class="form-section">
        <div class="form-group">
          <label>자기소개</label>
          <textarea v-model="form.bio" placeholder="나의 상담 스타일, 강점, 전문 분야를 소개해주세요." rows="4" class="form-input"></textarea>
        </div>

        <div class="form-group">
          <label>경력</label>
          <input v-model="form.experience" placeholder="예: 타로 상담 8년, 심리 상담사 자격증 보유" class="form-input" />
        </div>

        <div class="form-group">
          <label>태그 <span class="hint">(쉼표로 구분, 예: 타로, 연애, 진로)</span></label>
          <input v-model="tagsInput" placeholder="태그1, 태그2, 태그3" class="form-input" />
          <div v-if="parsedTags.length" class="tag-preview">
            <span v-for="t in parsedTags" :key="t" class="tag-chip">{{ t }}</span>
          </div>
        </div>

        <div v-if="saveMsg" class="save-msg" :class="saveError ? 'error' : 'success'">{{ saveMsg }}</div>

        <button class="btn-save" @click="saveProfile" :disabled="saving">
          {{ saving ? '저장 중...' : '💾 프로필 저장' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue';
import { getCurrentUser } from '@/api/auth';
import { updateProfile, getProfile, uploadProfileImage } from '@/api/profile';

export default {
  name: 'ProfileManagePage',
  setup() {
    const currentUser = getCurrentUser();
    const loading = ref(true);
    const imageUrl = ref('');
    const uploadingImage = ref(false);
    const saving = ref(false);
    const saveMsg = ref('');
    const saveError = ref(false);
    const tagsInput = ref('');

    const form = ref({
      bio: '',
      experience: '',
      tags: [],
      imageUrl: ''
    });

    const userInitial = computed(() => (currentUser?.name || '?').charAt(0));
    const parsedTags = computed(() =>
      tagsInput.value.split(',').map(t => t.trim()).filter(Boolean)
    );

    onMounted(async () => {
      if (currentUser?.id) {
        try {
          const profile = await getProfile(currentUser.id);
          form.value.bio = profile.bio || '';
          form.value.experience = profile.experience || '';
          form.value.imageUrl = profile.imageUrl || '';
          imageUrl.value = profile.imageUrl || '';
          tagsInput.value = (profile.tags || []).join(', ');
        } catch {}
      }
      loading.value = false;
    });

    const handleImageChange = async (e) => {
      const file = e.target.files?.[0];
      if (!file) return;
      uploadingImage.value = true;
      try {
        const result = await uploadProfileImage(file);
        imageUrl.value = result.imageUrl;
        form.value.imageUrl = result.imageUrl;
      } catch (err) {
        alert(err.response?.data?.message || '이미지 업로드에 실패했습니다.');
      } finally {
        uploadingImage.value = false;
      }
    };

    const saveProfile = async () => {
      saving.value = true;
      saveMsg.value = '';
      try {
        await updateProfile({
          bio: form.value.bio,
          experience: form.value.experience,
          tags: parsedTags.value,
          imageUrl: form.value.imageUrl
        });
        saveMsg.value = '✅ 프로필이 저장되었습니다!';
        saveError.value = false;
      } catch (e) {
        saveMsg.value = e.response?.data?.message || '저장에 실패했습니다.';
        saveError.value = true;
      } finally {
        saving.value = false;
      }
    };

    return { loading, form, imageUrl, uploadingImage, saving, saveMsg, saveError, tagsInput, userInitial, parsedTags, handleImageChange, saveProfile };
  }
};
</script>

<style scoped>
.page { background: #0d0d1a; min-height: 100vh; padding: 32px 8%; color: white; max-width: 820px; }
h1 { font-size: 1.8rem; margin-bottom: 6px; }
.sub-desc { color: rgba(255,255,255,0.5); font-size: 0.95rem; margin-bottom: 36px; }
.loading { display: flex; justify-content: center; padding: 60px; }
.spinner { width: 36px; height: 36px; border: 3px solid rgba(167,139,250,0.2); border-top-color: #a78bfa; border-radius: 50%; animation: spin 0.8s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

.form-wrap { display: flex; flex-direction: column; gap: 32px; }

/* 이미지 */
.image-section { display: flex; align-items: center; gap: 28px; background: rgba(255,255,255,0.05); border: 1px solid rgba(255,255,255,0.1); border-radius: 16px; padding: 24px; }
.avatar-preview { width: 100px; height: 100px; border-radius: 50%; background: linear-gradient(135deg, #7c3aed, #4f46e5); display: flex; align-items: center; justify-content: center; font-size: 2.4rem; font-weight: 700; flex-shrink: 0; background-size: cover; background-position: center; }
.image-actions { display: flex; flex-direction: column; gap: 8px; }
.btn-upload { display: inline-block; padding: 10px 20px; background: rgba(124,58,237,0.2); border: 1px solid rgba(124,58,237,0.5); color: #c4b5fd; border-radius: 10px; cursor: pointer; font-size: 0.9rem; transition: all 0.2s; }
.btn-upload:hover { background: rgba(124,58,237,0.35); }
.btn-upload.loading { opacity: 0.6; cursor: not-allowed; }
.img-hint { color: rgba(255,255,255,0.38); font-size: 0.78rem; }

/* 폼 */
.form-section { background: rgba(255,255,255,0.05); border: 1px solid rgba(255,255,255,0.1); border-radius: 16px; padding: 28px; display: flex; flex-direction: column; gap: 22px; }
.form-group { display: flex; flex-direction: column; gap: 8px; }
.form-group label { font-size: 0.9rem; color: rgba(255,255,255,0.75); font-weight: 500; }
.hint { color: rgba(255,255,255,0.38); font-weight: 400; font-size: 0.8rem; }
.form-input { padding: 12px 14px; background: rgba(255,255,255,0.08); border: 1px solid rgba(255,255,255,0.15); border-radius: 10px; color: white; outline: none; font-size: 0.95rem; resize: vertical; transition: border-color 0.2s; }
.form-input::placeholder { color: rgba(255,255,255,0.3); }
.form-input:focus { border-color: rgba(124,58,237,0.6); }

/* 태그 */
.tag-preview { display: flex; flex-wrap: wrap; gap: 6px; margin-top: 6px; }
.tag-chip { padding: 4px 12px; background: rgba(124,58,237,0.2); border: 1px solid rgba(124,58,237,0.4); border-radius: 20px; font-size: 0.8rem; color: #c4b5fd; }

/* 저장 */
.save-msg { padding: 10px 14px; border-radius: 8px; font-size: 0.88rem; }
.save-msg.success { background: rgba(34,197,94,0.15); color: #86efac; border: 1px solid rgba(34,197,94,0.3); }
.save-msg.error { background: rgba(239,68,68,0.15); color: #fca5a5; border: 1px solid rgba(239,68,68,0.3); }
.btn-save { padding: 13px 28px; background: linear-gradient(135deg, #7c3aed, #4f46e5); color: white; border: none; border-radius: 10px; font-size: 1rem; font-weight: 600; cursor: pointer; align-self: flex-start; transition: opacity 0.2s; }
.btn-save:disabled { opacity: 0.5; cursor: not-allowed; }
</style>
