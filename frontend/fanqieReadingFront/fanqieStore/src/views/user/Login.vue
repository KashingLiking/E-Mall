<template>
  <div class="login-container">
    <el-card class="login-card">
      <h2>登录</h2>
      <el-form :model="loginForm" :rules="rules" ref="loginFormRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="loginForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="loginForm.rememberMe">记住我</el-checkbox>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin">登录</el-button>
          <el-button type="text" @click="goToRegister">注册</el-button>
          <el-button type="text" @click="goToForgotPassword">忘记密码？</el-button>
        </el-form-item>
      </el-form>
    </el-card>
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

    console.log("登录响应:", res);

    // 根据实际后端响应结构调整
    const token = res; // 直接获取token字符串
    if (!token) throw new Error('未获取到token');

    sessionStorage.setItem('token', token);
    sessionStorage.setItem('username', loginForm.value.username);

    await router.replace('/store-detail');
    ElMessage.success('登录成功');

  } catch (error) {
    console.error('登录错误:', error);

    // 更安全的错误处理
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

const goToRegister = () => {
  router.push('/register');
};
const goToForgotPassword = () => {
  router.push('/forgot-password');
};

</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f5f5f5;
}

.login-card {
  width: 400px;
  padding: 20px;
  text-align: center;
}
</style>
