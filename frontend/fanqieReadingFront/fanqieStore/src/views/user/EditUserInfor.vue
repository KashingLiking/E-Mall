<template>
  <div class="edit-profile-container">
    <el-card class="edit-profile-card">
      <h2>修改个人信息</h2>
      <el-form :model="userInfo" :rules="rules" ref="userFormRef" label-width="100px">
        <!-- 隐藏字段保留接口必要参数 -->
        <div class="hidden-fields">
          <el-input v-model="userInfo.password" type="hidden" />
          <el-input v-model="userInfo.avatar" type="hidden" />
          <el-input v-model="userInfo.role" type="hidden" />
        </div>

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
          >
            <template #append>
              <el-button @click="sendSmsCode" :disabled="smsCooldown > 0">
                {{ smsCooldown > 0 ? `${smsCooldown}s` : '发送验证码' }}
              </el-button>
            </template>
          </el-input>
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
          <el-button
              type="primary"
              @click="submitForm"
              :loading="submitting"
          >
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
import { regionData } from 'element-china-area-data';
import {fetchUserDetail, getUserInfo, updateUser} from '@/api/accounts';
import {axios}   from "@/utils/request";

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
  password: '',    // 密码字段保留但不展示
  name: '',
  avatar: '',      // 头像URL字段
  role: 'user',    // 默认用户角色
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

// 提交处理
const submitting = ref(false);
const submitForm = async () => {
  submitting.value = true;
  try {
    // 构造符合API规范的payload
    const payload = {
      username: userInfo.value.username,
      password: userInfo.value.password||null,
      name: userInfo.value.name||null,
      avatar: userInfo.value.avatar||null,
      role: userInfo.value.role||null,
      telephone: userInfo.value.telephone || null, // 使用undefined而不是空字符串
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
    if (data.code == 200) {
      ElMessage.success('信息更新成功');
      setTimeout(() => router.push('/userinfo'), 1500);
    } else {
      ElMessage.error(data.msg || '更新失败');
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
    userInfo.value = response.data; // 假设返回的数据结构匹配
    console.log(userInfo.value);
  } catch (error) {
    ElMessage.error('获取用户信息失败');
  }
});
</script>

<style scoped>
.edit-profile-card {
  width: 600px;
  padding: 30px;
}

.hidden-fields {
  display: none;
}

.el-cascader {
  width: 100%;
}
</style>