<template>
  <div class="login-page">
    <div class="auth-card">
      <div class="logo">🔮</div>
      <h1>회원가입</h1>
      <p class="subtitle">사주타로 서비스에 오신 걸 환영합니다</p>

      <form @submit.prevent="handleSignup">
        <div class="form-group">
          <label>이름</label>
          <input v-model="form.name" type="text" placeholder="이름" required />
        </div>
        <div class="form-group">
          <label>닉네임</label>
          <input v-model="form.nickname" type="text" placeholder="닉네임" required />
        </div>
        <div class="form-group">
          <label>이메일</label>
          <input v-model="form.email" type="email" placeholder="이메일" required />
        </div>
        <div class="form-group">
          <label>비밀번호</label>
          <input v-model="form.password" type="password" placeholder="비밀번호 (8자 이상)" required />
        </div>
        <div class="form-group">
          <label>전화번호</label>
          <input v-model="form.phone" type="text" placeholder="010-0000-0000" required />
        </div>

        <div v-if="errorMsg" class="error-msg">{{ errorMsg }}</div>
        <div v-if="successMsg" class="success-msg">{{ successMsg }}</div>

        <button type="submit" class="btn-primary" :disabled="loading">
          {{ loading ? '처리 중...' : '회원가입' }}
        </button>
      </form>

      <p class="switch-auth">
        이미 계정이 있으신가요?
        <router-link to="/login">로그인</router-link>
      </p>
    </div>
  </div>
</template>

<script>
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { signupUser } from '@/api/auth';

export default {
  name: 'SignupPage',
  setup() {
    const router = useRouter();
    const form = reactive({ name: '', nickname: '', email: '', password: '', phone: '' });
    const errorMsg = ref('');
    const successMsg = ref('');
    const loading = ref(false);

    const handleSignup = async () => {
      loading.value = true;
      errorMsg.value = '';
      try {
        await signupUser(form);
        successMsg.value = '회원가입 완료! 로그인 페이지로 이동합니다...';
        setTimeout(() => router.push('/login'), 1500);
      } catch (err) {
        errorMsg.value = err.response?.data?.message || '회원가입에 실패했습니다.';
      } finally {
        loading.value = false;
      }
    };

    return { form, errorMsg, successMsg, loading, handleSignup };
  }
};
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1a0533 0%, #2d1b69 50%, #0f3460 100%);
}
.auth-card {
  background: rgba(255,255,255,0.05);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255,255,255,0.1);
  border-radius: 20px;
  padding: 48px 40px;
  width: 100%;
  max-width: 420px;
  color: white;
  text-align: center;
}
.logo { font-size: 3rem; margin-bottom: 8px; }
h1 { font-size: 1.8rem; font-weight: 700; margin-bottom: 6px; }
.subtitle { color: rgba(255,255,255,0.6); margin-bottom: 32px; font-size: 0.9rem; }
.form-group { margin-bottom: 14px; text-align: left; }
.form-group label { display: block; margin-bottom: 5px; font-size: 0.85rem; color: rgba(255,255,255,0.8); }
.form-group input {
  width: 100%; padding: 11px 14px;
  background: rgba(255,255,255,0.1);
  border: 1px solid rgba(255,255,255,0.2);
  border-radius: 10px; color: white; font-size: 0.95rem;
  outline: none; box-sizing: border-box;
}
.form-group input:focus { border-color: #a78bfa; }
.form-group input::placeholder { color: rgba(255,255,255,0.4); }
.error-msg { background: rgba(239,68,68,0.2); border: 1px solid rgba(239,68,68,0.4); color: #fca5a5; padding: 10px; border-radius: 8px; margin-bottom: 12px; font-size: 0.85rem; }
.success-msg { background: rgba(34,197,94,0.2); border: 1px solid rgba(34,197,94,0.4); color: #86efac; padding: 10px; border-radius: 8px; margin-bottom: 12px; font-size: 0.85rem; }
.btn-primary { width: 100%; padding: 14px; background: linear-gradient(135deg, #7c3aed, #4f46e5); color: white; border: none; border-radius: 10px; font-size: 1rem; font-weight: 600; cursor: pointer; margin-top: 8px; transition: opacity 0.2s; }
.btn-primary:disabled { opacity: 0.6; cursor: not-allowed; }
.switch-auth { margin-top: 20px; font-size: 0.9rem; color: rgba(255,255,255,0.6); }
.switch-auth a { color: #a78bfa; text-decoration: none; font-weight: 600; }
</style>
