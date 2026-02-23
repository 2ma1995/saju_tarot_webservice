<template>
  <div class="login-page">
    <div class="auth-card">
      <div class="logo">🔮</div>
      <h1>사주타로</h1>
      <p class="subtitle">나의 운명을 만나보세요</p>

      <form @submit.prevent="handleLogin">
        <div class="form-group">
          <label>이메일</label>
          <input v-model="email" type="email" placeholder="이메일을 입력하세요" required />
        </div>
        <div class="form-group">
          <label>비밀번호</label>
          <input v-model="password" type="password" placeholder="비밀번호를 입력하세요" required />
        </div>

        <div v-if="errorMsg" class="error-msg">{{ errorMsg }}</div>

        <button type="submit" class="btn-primary" :disabled="loading">
          {{ loading ? '로그인 중...' : '로그인' }}
        </button>
      </form>

      <p class="switch-auth">
        계정이 없으신가요?
        <router-link to="/signup">회원가입</router-link>
      </p>
    </div>
  </div>
</template>

<script>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { loginUser } from '@/api/auth';

export default {
  name: 'LoginPage',
  setup() {
    const router = useRouter();
    const email = ref('');
    const password = ref('');
    const errorMsg = ref('');
    const loading = ref(false);

    const handleLogin = async () => {
      loading.value = true;
      errorMsg.value = '';
      try {
        await loginUser({ email: email.value, password: password.value });
        router.push('/');
      } catch (err) {
        errorMsg.value = err.response?.data?.message || '로그인에 실패했습니다.';
      } finally {
        loading.value = false;
      }
    };

    return { email, password, errorMsg, loading, handleLogin };
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
  text-align: center;
  color: white;
}

.logo { font-size: 3rem; margin-bottom: 8px; }
h1 { font-size: 1.8rem; font-weight: 700; margin-bottom: 6px; }
.subtitle { color: rgba(255,255,255,0.6); margin-bottom: 32px; font-size: 0.9rem; }

.form-group {
  margin-bottom: 16px;
  text-align: left;
}

.form-group label {
  display: block;
  margin-bottom: 6px;
  font-size: 0.85rem;
  color: rgba(255,255,255,0.8);
}

.form-group input {
  width: 100%;
  padding: 12px 16px;
  background: rgba(255,255,255,0.1);
  border: 1px solid rgba(255,255,255,0.2);
  border-radius: 10px;
  color: white;
  font-size: 1rem;
  outline: none;
  box-sizing: border-box;
  transition: border-color 0.2s;
}

.form-group input:focus { border-color: #a78bfa; }
.form-group input::placeholder { color: rgba(255,255,255,0.4); }

.error-msg {
  background: rgba(239,68,68,0.2);
  border: 1px solid rgba(239,68,68,0.4);
  color: #fca5a5;
  padding: 10px;
  border-radius: 8px;
  margin-bottom: 16px;
  font-size: 0.85rem;
}

.btn-primary {
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, #7c3aed, #4f46e5);
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  margin-top: 8px;
  transition: opacity 0.2s;
}

.btn-primary:disabled { opacity: 0.6; cursor: not-allowed; }
.btn-primary:hover:not(:disabled) { opacity: 0.9; }

.switch-auth {
  margin-top: 20px;
  font-size: 0.9rem;
  color: rgba(255,255,255,0.6);
}

.switch-auth a { color: #a78bfa; text-decoration: none; font-weight: 600; }
</style>
