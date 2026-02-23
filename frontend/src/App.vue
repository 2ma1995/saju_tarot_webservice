<template>
  <div id="app">
    <!-- 상단 헤더 추가 -->
    <header>
      <nav>
        <ul>
          <li><router-link to="/">홈</router-link></li>
          <li><router-link to="/counselors">상담사 목록</router-link></li>
          <li><router-link to="/counselor">test</router-link></li>
        </ul>
      </nav>
    </header>

    <!-- 라우터 뷰: 페이지 내용이 여기에 렌더링됨 -->
    <router-view />
  </div>
</template>

<script>
export default {
  name: 'App'
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'Noto Sans KR', sans-serif;
  background: #f5f5f5;
}

#app {
  min-height: 100vh;
}

/* 헤더 스타일링 */
header {
  background: #333;
  color: white;
  padding: 20px;
  text-align: center;
}

nav ul {
  list-style-type: none;
  display: flex;
  justify-content: center;
}

nav li {
  margin: 0 15px;
}

nav a {
  color: white;
  text-decoration: none;
}

nav a:hover {
  text-decoration: underline;
}
</style>
