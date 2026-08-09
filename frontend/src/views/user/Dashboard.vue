<template>
  <div class="profile-page">
    <el-card class="profile-card">
      <div class="profile-avatar-wrap">
        <el-avatar :src="resolveImageUrl(userInfo.avatar)" size="large" />
        <div class="user-role" style="margin-top: 10px">
          <el-tag effect="dark" type="warning">{{ userInfo.role === 'admin' ? '管理员' : '普通用户' }}</el-tag>
        </div>
      </div>
      <h3>个人信息</h3>
      <el-descriptions :column="1" border class="user-descriptions" labelStyle="width: 110px; font-weight: 600;">
        <el-descriptions-item label="用户名">{{ userInfo.username }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ userInfo.name }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ userInfo.email || '-' }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ userInfo.telephone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="所在地">{{ displayLocation(userInfo.location) }}</el-descriptions-item>
      </el-descriptions>
      <div class="profile-actions">
        <el-button round @click="goBack">返回主页</el-button>
        <el-button type="warning" round @click="goToEditProfile">修改信息</el-button>
        <el-button type="danger" round plain @click="handleLogout">退出登录</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { useRouter } from 'vue-router';
import { fetchUserDetail, updateUser } from '@/api/accounts';
import { logout } from '@/utils/auth';
import { resolveImageUrl } from '@/utils/image';
import { regionData } from 'element-china-area-data';

const router = useRouter();

const userInfo = ref({
  username: '',
  name: '',
  email: '',
  telephone: '',
  location: '',
  avatar: '',
  role: '',
  password: ''
});

// 把编码串（如 "11,1101,110101"）转成文字（如 "北京市 市辖区 东城区"）
function buildCodeMap(nodes, map = {}) {
  for (const node of nodes) {
    map[node.value] = node.label;
    if (node.children) buildCodeMap(node.children, map);
  }
  return map;
}
const codeMap = buildCodeMap(regionData);

function displayLocation(location) {
  if (!location) return '-';
  return location.split(',').map(code => codeMap[code] || code).join(' ');
}

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
    userInfo.value = {
      ...response.data,
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
  router.push('/store-detail');
};
const handleLogout = () => logout();
</script>

<style scoped>
.user-descriptions {
  margin-top: 8px;
}
</style>
