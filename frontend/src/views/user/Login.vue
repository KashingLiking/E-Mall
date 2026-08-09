<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="auth-brand">
        <h1>📚 EMall书城</h1>
        <p>欢迎回来，请登录你的账号</p>
      </div>
      <el-form :model="loginForm" :rules="rules" ref="loginFormRef" label-width="80px" label-position="top">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="loginForm.username" placeholder="请输入用户名" size="large" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" show-password size="large" />
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="loginForm.rememberMe">记住我</el-checkbox>
        </el-form-item>
        <el-form-item>
          <div class="auth-actions">
            <el-button type="primary" size="large" @click="handleLogin">登 录</el-button>
            <el-button link type="primary" @click="goToRegister">注册账号</el-button>
          </div>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { login } from '@/api/accounts';

const router = useRouter();
const loginForm = ref({
  username: '',
  password: '',
  rememberMe: false
});
const loginFormRef = ref(null);

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
};

const handleLogin = async () => {
  try {
    await loginFormRef.value.validate();

    const res = await login({
      username: loginForm.value.username.trim(),
      password: loginForm.value.password
    });

    const token = res;
    if (!token) throw new Error('未获取到token');

    sessionStorage.setItem('token', token);
    sessionStorage.setItem('username', loginForm.value.username);

    await router.replace('/store-detail');
    ElMessage.success('登录成功');

  } catch (error) {
    let errorMessage = '登录失败，请检查用户名和密码';
    if (error instanceof Error) {
      errorMessage = error.message || errorMessage;
    }
    if (typeof error === 'object' && error?.response?.data?.message) {
      errorMessage = error.response.data.message;
    }
    ElMessage.error(errorMessage);
  }
}

const goToRegister = () => router.push('/register');
</script>
