<template>
  <div class="dashboard-container">
    <el-card class="dashboard-card">
      <div class="avatar-wrapper">
        <el-avatar :src="userInfo.avatar" size="large" class="user-avatar" />
        <div class="user-role">
          <el-tag>{{ userInfo.role === 'admin' ? '管理员' : userInfo.role }}</el-tag>
        </div>
      </div>
      <h3 style="text-align: center;color: #E6A23C; ">个人信息</h3>
      <el-descriptions :column="1" border class="user-descriptions" labelStyle="width: 110px;">
        <el-descriptions-item label="用户名">
          {{ userInfo.username }}
        </el-descriptions-item>
        <el-descriptions-item label="姓名">
          {{ userInfo.name }}
        </el-descriptions-item>
        <el-descriptions-item label="邮箱">
          {{ userInfo.email }}
        </el-descriptions-item>
        <el-descriptions-item label="手机号">
          {{ userInfo.telephone }}
        </el-descriptions-item>
        <el-descriptions-item label="所在地">
          {{ displayLocation(userInfo.location) }}
        </el-descriptions-item>
        <el-descriptions-item label="会员等级">
          <el-tag effect="light" type="info">{{ userInfo.membershipLevel }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="累计消费">
          <el-tag effect="plain" type="success">￥{{ userInfo.totalSpent?.toFixed(2) ?? '0.00' }}</el-tag>
        </el-descriptions-item>
        <!-- <el-descriptions-item label="密码">
          <el-tag  type="success">已设置</el-tag>
        </el-descriptions-item> -->
      </el-descriptions>
      <div style="margin-top: 20px; text-align: center;">
        <el-button @click="goBack">返回主页</el-button>
        <el-button type="warning" @click="goToEditProfile" style="margin-left: 10px;">修改信息</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { useRouter } from 'vue-router';
import { fetchUserDetail, getUserInfo, updateUser } from '@/api/accounts';

const router = useRouter();

const userInfo = ref({
  username: '',
  name: '',
  email: '',
  telephone: '',
  location: '',
  avatar: '',
  role: '',
  membershipLevel: '',
  totalSpent: 0,
  password: ''
});

// 若需要支持省市区显示友好化，可补充字典
function displayLocation(location) {
  // location存储格式例如"11,1101,110101"
  // 可按你项目实际情况映射地名，这里先原样展示
  return location || '-';
}

const updateUserInfo = async () => {
  try {
    await updateUser(userInfo.value);
    ElMessage.success('信息更新成功');
  } catch (error) {
    ElMessage.error('更新失败');
  }
};
const goToEditProfile = () => {
  router.push('/edit-profile');
};

const loadUserData = async () => {
  try {
    const username = sessionStorage.getItem('username');
    if (!username) {
      ElMessage.error('请先登录');
      return router.push('/login');
    }
    const token = sessionStorage.getItem('token');
    const response = await fetchUserDetail(username, token);
    // 字段名适配
    userInfo.value = {
      ...response.data,
      membershipLevel: response.data.membershipLevel || response.data.membership_level || '',
      totalSpent: response.data.totalSpent || response.data.total_spent || 0,
      avatar: response.data.avatar || '',
      role: response.data.role || '',
      password: response.data.password || ''
    };
  } catch (error) {
    ElMessage.error('获取用户信息失败');
  }
};

onMounted(() => {
  loadUserData();
});
const goBack = () => {
  router.back();
};
</script>

<style scoped>
.dashboard-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
}

.dashboard-card {
  width: 456px;
  padding: 28px 30px 20px 30px;
  box-shadow: 0 8px 24px 0 rgba(0, 0, 0, .08);
}

.avatar-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 18px;
}

.user-avatar {
  width: 80px;
  height: 80px;
  border: 2px solid #f0f2f5;
  box-shadow: 0 2px 6px 0 rgba(0, 0, 0, 0.09);
}

.user-role {
  margin-top: 8px;
}

.user-descriptions {
  margin-top: 12px;
}
</style>