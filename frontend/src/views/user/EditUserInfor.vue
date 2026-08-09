<template>
  <div class="profile-page">
    <el-card class="profile-card" style="max-width: 560px">
      <div class="auth-brand" style="margin-bottom: 20px">
        <h1 style="font-size: 22px">✏️ 修改个人信息</h1>
      </div>
      <el-form :model="userInfo" :rules="rules" ref="userFormRef" label-width="100px" label-position="top">
        <div class="hidden-fields">
          <el-input v-model="userInfo.role" type="hidden" />
        </div>

        <el-form-item label="头像">
          <el-upload
            class="avatar-uploader"
            action="#"
            :show-file-list="false"
            :before-upload="beforeAvatarUpload"
            accept="image/jpeg,image/png,image/webp"
          >
            <img v-if="userInfo.avatar" :src="resolveImageUrl(userInfo.avatar)" class="avatar-preview" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
          <div class="upload-tip">点击上传头像，支持 JPG/PNG/WEBP，小于 2MB</div>
        </el-form-item>

        <el-form-item label="用户名" prop="username">
          <el-input v-model="userInfo.username" disabled />
        </el-form-item>

        <el-form-item label="真实姓名" prop="name">
          <el-input
              v-model="userInfo.name"
              placeholder="请输入真实姓名"
              clearable
          />
        </el-form-item>

        <el-form-item label="手机号码" prop="telephone">
          <el-input
              v-model="userInfo.telephone"
              placeholder="11位手机号码"
              :formatter="formatPhone"
              clearable
          />
        </el-form-item>

        <el-form-item label="电子邮箱" prop="email">
          <el-input
              v-model="userInfo.email"
              placeholder="name@example.com"
              clearable
          />
        </el-form-item>

        <el-form-item label="所在地区" prop="location">
          <el-cascader
              v-model="userInfo.location"
              :options="regionOptions"
              placeholder="省/市/区"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" round size="large" @click="submitForm" :loading="submitting" style="width: 100%">
            {{ submitting ? '提交中...' : '保存修改' }}
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';
import { useRouter } from 'vue-router';
import { regionData } from 'element-china-area-data';
import { fetchUserDetail, updateUser } from '@/api/accounts';
import { uploadImage } from '@/api/upload';
import { resolveImageUrl } from '@/utils/image';

const router = useRouter();

// 表单验证规则
const phoneRegex = /^1[3-9]\d{9}$/;
const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

const rules = {
  name: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  telephone: [
    {
      validator: (_, value, callback) => {
        if (value && !phoneRegex.test(value)) {
          callback(new Error('手机号格式不正确'));
        } else {
          callback();
        }
      },
      trigger: 'blur'
    }
  ],
  email: [
    {
      validator: (_, value, callback) => {
        if (value && !emailRegex.test(value)) {
          callback(new Error('邮箱格式不正确'));
        } else {
          callback();
        }
      },
      trigger: 'blur'
    }
  ]
};

// 响应式数据
const userInfo = ref({
  username: '',
  name: '',
  avatar: '',
  role: 'user',
  telephone: '',
  email: '',
  location: []
});

// 地区选择配置
const regionOptions = ref(regionData);

// 手机号格式化
const formatPhone = (value) => {
  return value.replace(/\D/g, '').slice(0, 11);
};

// 头像上传
const beforeAvatarUpload = (file) => {
  const okTypes = ['image/jpeg', 'image/png', 'image/webp'];
  if (!okTypes.includes(file.type)) {
    ElMessage.error('仅支持 JPG/PNG/WEBP 图片');
    return false;
  }
  if (file.size / 1024 / 1024 > 2) {
    ElMessage.error('图片不应超过 2MB');
    return false;
  }
  uploadImage(file).then((res) => {
    userInfo.value.avatar = res.data.data;
    ElMessage.success('头像上传成功');
  }).catch(() => {
    ElMessage.error('头像上传失败');
  });
  return false; // 阻止 el-upload 自动上传
};

// 提交处理
const submitting = ref(false);
const submitForm = async () => {
  submitting.value = true;
  try {
    // 构造符合API规范的payload（不传password，避免将bcrypt哈希再次加密）
    const payload = {
      username: userInfo.value.username,
      name: userInfo.value.name||null,
      avatar: userInfo.value.avatar||null,
      role: userInfo.value.role||null,
      telephone: userInfo.value.telephone || null,
      email: userInfo.value.email || null,
      location: Array.isArray(userInfo.value.location)
          ? userInfo.value.location.join(',')
          : null
    };
    console.log("完整请求数据:", JSON.stringify(payload, null, 2));
    // 使用已封装的updateUser方法
    const data = await updateUser(payload);
    console.log("data in edit: "+data);

    // 严格响应验证
    if (data.data.code == 200) {
      ElMessage.success('信息更新成功');
      setTimeout(() => router.push('/dashboard'), 1500);
    } else {
      ElMessage.error(data.data.msg || '更新失败');
    }
  } catch (error) {
    // 增强错误提示
    const msg = error.response?.data?.message
        || error.message
        || '请求异常'
    ElMessage.error(`操作失败: ${msg}`)
    console.error('完整错误:', error)
  } finally {
    submitting.value = false;
  }
};

// 初始化加载用户数据
onMounted(async () => {
  try {
    // 从localStorage获取username
    const username = sessionStorage.getItem('username');
    if (!username) {
      ElMessage.error('请先登录');
      return router.push('/login');
    }
    const token = sessionStorage.getItem('token');
    console.log('token in edit:'+token);
    console.log('username in edit:'+username);
    // 获取用户详情数据
    const response = await fetchUserDetail(username,token);
    const data = response.data;
    userInfo.value = {
      ...data,
      location: data.location ? data.location.split(',') : []
    };
    console.log(userInfo.value);
  } catch (error) {
    ElMessage.error('获取用户信息失败');
  }
});
</script>

<style scoped>
.hidden-fields {
  display: none;
}

.el-cascader {
  width: 100%;
}

.avatar-uploader :deep(.el-upload) {
  border: 1px dashed #d9d9d9;
  border-radius: 50%;
  cursor: pointer;
  width: 80px;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.avatar-uploader :deep(.el-upload:hover) {
  border-color: #409eff;
}

.avatar-preview {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 50%;
}

.avatar-uploader-icon {
  font-size: 24px;
  color: #8c939d;
}

.upload-tip {
  font-size: 12px;
  color: #999;
  margin-top: 6px;
}
</style>