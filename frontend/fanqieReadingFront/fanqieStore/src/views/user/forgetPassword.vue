<template>
  <div class="forgot-password-container">
    <el-card class="forgot-password-card">
      <h2>找回密码</h2>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>

        <!-- 🌟 新增名字字段 -->
        <el-form-item label="真实姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入注册时的姓名" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleForgotPassword">找回密码</el-button>
          <el-button type="text" @click="goToLogin">返回登录</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { forgotPassword } from '@/api/accounts'; // 需要在 `@/api/user.js` 里创建 `forgotPassword` API 方法

const router = useRouter();

const form = ref({
  username: '',
  name: '',
});

const formRef = ref(null);


const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    {
      async validator(_, value) {
        const { data } = await checkUsernameExists(value)
        return data.exists || new Error('用户名不存在')
      },
      trigger: 'blur'
    }
  ],
  name: [ // 🌟 新增名字验证
    { required: true, message: '请输入注册时填写的姓名', trigger: 'blur' }
  ]
};

const handleForgotPassword = async () => {
  formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // 🌟 发送用户名和名字的组合
        const res = await forgotPassword({
          username: form.value.username,
          name: form.value.name
        });

        if (res.data.code === '200') {
          ElMessage.success(`您的密码是：${res.data.result.password}`);
        } else {
          ElMessage.error(res.data.msg || '身份验证失败');
        }
      } catch (error) {
        ElMessage.error('找回密码失败：' + error.response?.data?.message);
      }
    }
  });
};

const goToLogin = () => {
  router.push('/login');
};
</script>

<style scoped>
.forgot-password-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f5f5f5;
}

.forgot-password-card {
  width: 400px;
  padding: 20px;
  text-align: center;
}
</style>
