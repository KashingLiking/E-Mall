<template>
  <el-container direction="vertical">
    <AppNav />
    <div class="seckill-page">

      <!-- 管理员管理面板 -->
      <div v-if="userRole === 'admin'" class="admin-panel">
        <div class="admin-panel-header">
          <h3 class="panel-title">⚙️ 秒杀活动管理</h3>
          <el-button type="primary" round @click="openCreateDialog">+ 创建活动</el-button>
        </div>
        <el-table :data="activities" stripe size="default" style="width:100%">
          <el-table-column prop="id" label="ID" width="60" align="center" />
          <el-table-column prop="productTitle" label="商品" min-width="140" />
          <el-table-column label="秒杀价" width="100" align="center">
            <template #default="s">¥{{ s.row.seckillPrice }}</template>
          </el-table-column>
          <el-table-column prop="seckillStock" label="总库存" width="80" align="center" />
          <el-table-column prop="remainingStock" label="剩余" width="80" align="center" />
          <el-table-column label="开始时间" min-width="160">
            <template #default="s">{{ formatTime(s.row.startTime) }}</template>
          </el-table-column>
          <el-table-column label="结束时间" min-width="160">
            <template #default="s">{{ formatTime(s.row.endTime) }}</template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="90" align="center">
            <template #default="s">
              <el-tag :type="statusTagType(s.row.status)" size="small">{{ statusLabel(s.row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="140" align="center">
            <template #default="s">
              <el-button size="small" type="primary" plain @click="openEditDialog(s.row)">编辑</el-button>
              <el-button size="small" type="danger" plain @click="handleDelete(s.row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 秒杀活动卡片 -->
      <div class="seckill-header">
        <h2 class="seckill-title">⚡ 限时秒杀</h2>
        <span class="seckill-subtitle">限时特价，抢完即止</span>
      </div>

      <div v-if="activities.length === 0" class="empty-tip">暂无进行中的秒杀活动</div>

      <el-row :gutter="20" class="seckill-grid">
        <el-col
          v-for="act in activities"
          :key="act.id"
          :xs="24" :sm="12" :md="8" :lg="6"
        >
          <el-card class="seckill-card" shadow="hover">
            <div class="seckill-cover-wrap">
              <el-image
                class="seckill-cover"
                :src="resolveImageUrl(act.productCover)"
                fit="cover"
              >
                <template #error><div class="img-error">暂无封面</div></template>
              </el-image>
              <div class="seckill-badge">限时</div>
            </div>
            <div class="seckill-info">
              <div class="seckill-book-title">{{ act.productTitle }}</div>
              <div class="seckill-price-row">
                <span class="seckill-price">¥{{ act.seckillPrice }}</span>
                <span class="original-price">¥{{ act.originalPrice }}</span>
              </div>
              <div class="seckill-stock">剩余库存：{{ act.remainingStock ?? act.seckillStock }}</div>
              <div class="seckill-countdown" :class="countdownClass(act)">
                {{ countdowns[act.id!] || '计算中...' }}
              </div>
              <el-button
                class="seckill-btn"
                type="danger"
                round
                :disabled="!isActive(act) || (act.remainingStock ?? 1) <= 0"
                @click="handleBuy(act)"
              >
                {{ buyBtnLabel(act) }}
              </el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 创建/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑秒杀活动' : '创建秒杀活动'" width="480px" center>
      <el-form :model="form" label-width="90px" label-position="top">
        <el-form-item label="选择商品" required>
          <el-select v-model="form.productId" placeholder="请选择商品" style="width:100%" filterable>
            <el-option
              v-for="p in productList"
              :key="p.id"
              :label="p.title"
              :value="p.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="秒杀价格" required>
          <el-input v-model.number="form.seckillPrice" type="number" placeholder="元" />
        </el-form-item>
        <el-form-item label="秒杀库存" required>
          <el-input v-model.number="form.seckillStock" type="number" placeholder="件" />
        </el-form-item>
        <el-form-item label="开始时间" required>
          <el-date-picker
            v-model="form.startTime"
            type="datetime"
            placeholder="选择开始时间"
            style="width:100%"
            value-format="YYYY-MM-DDTHH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="结束时间" required>
          <el-date-picker
            v-model="form.endTime"
            type="datetime"
            placeholder="选择结束时间"
            style="width:100%"
            value-format="YYYY-MM-DDTHH:mm:ss"
          />
        </el-form-item>
        <el-form-item v-if="isEdit" label="活动状态">
          <el-select v-model="form.status" style="width:100%">
            <el-option label="待开始" value="PENDING" />
            <el-option label="进行中" value="ACTIVE" />
            <el-option label="已结束" value="ENDED" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </el-container>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import AppNav from '@/components/AppNav.vue'
import { resolveImageUrl } from '@/utils/image'
import { getProductInfo } from '@/api/product'
import {
  getSeckillActivities,
  createSeckillActivity,
  updateSeckillActivity,
  deleteSeckillActivity,
  buySeckill,
  type SeckillActivityVO
} from '@/api/seckill'
import { getUserDetail } from '@/api/accounts'

const router = useRouter()
const userRole = ref('')
const activities = ref<SeckillActivityVO[]>([])
const productList = ref<any[]>([])
const countdowns = ref<Record<number, string>>({})
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
let timer: ReturnType<typeof setInterval> | null = null

const form = ref<SeckillActivityVO & { id?: number }>({
  productId: undefined,
  seckillPrice: 0,
  seckillStock: 0,
  startTime: '',
  endTime: '',
  status: 'PENDING'
})

const fetchActivities = async () => {
  const res = await getSeckillActivities()
  activities.value = res.data.data || []
}

const fetchProducts = async () => {
  const res = await getProductInfo()
  productList.value = res.data.data || []
}

const updateCountdowns = () => {
  const now = Date.now()
  const map: Record<number, string> = {}
  activities.value.forEach(act => {
    const start = new Date(act.startTime).getTime()
    const end = new Date(act.endTime).getTime()
    if (now < start) {
      map[act.id!] = '距开始：' + formatDuration(start - now)
    } else if (now < end) {
      map[act.id!] = '距结束：' + formatDuration(end - now)
    } else {
      map[act.id!] = '已结束'
    }
  })
  countdowns.value = map
}

const formatDuration = (ms: number) => {
  const h = Math.floor(ms / 3600000)
  const m = Math.floor((ms % 3600000) / 60000)
  const s = Math.floor((ms % 60000) / 1000)
  return `${String(h).padStart(2,'0')}:${String(m).padStart(2,'0')}:${String(s).padStart(2,'0')}`
}

const formatTime = (t: string) => t ? t.replace('T', ' ') : '-'

const isActive = (act: SeckillActivityVO) => {
  const now = Date.now()
  return now >= new Date(act.startTime).getTime() && now < new Date(act.endTime).getTime()
}

const buyBtnLabel = (act: SeckillActivityVO) => {
  const now = Date.now()
  if (now < new Date(act.startTime).getTime()) return '未开始'
  if (now >= new Date(act.endTime).getTime()) return '已结束'
  if ((act.remainingStock ?? 1) <= 0) return '已售罄'
  return '立即抢购'
}

const countdownClass = (act: SeckillActivityVO) => {
  if (isActive(act)) return 'countdown-active'
  return 'countdown-inactive'
}

const statusLabel = (s: string) => ({ PENDING: '待开始', ACTIVE: '进行中', ENDED: '已结束' }[s] || s)
const statusTagType = (s: string) => ({ PENDING: 'info', ACTIVE: 'success', ENDED: 'danger' }[s] || 'info')

const handleBuy = async (act: SeckillActivityVO) => {
  if (!sessionStorage.getItem('token')) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  try {
    const res = await buySeckill(act.id!)
    if (res.data.code === '200') {
      ElMessage.success(res.data.data.message || '秒杀成功！')
      fetchActivities()
    } else {
      ElMessage.error(res.data.msg || '抢购失败')
    }
  } catch (e: any) {
    ElMessage.error(e.response?.data?.msg || '抢购失败，请稍后再试')
  }
}

const openCreateDialog = () => {
  isEdit.value = false
  form.value = { productId: undefined, seckillPrice: 0, seckillStock: 0, startTime: '', endTime: '', status: 'PENDING' }
  dialogVisible.value = true
}

const openEditDialog = (act: SeckillActivityVO) => {
  isEdit.value = true
  form.value = {
    id: act.id,
    productId: act.productId,
    seckillPrice: act.seckillPrice,
    seckillStock: act.seckillStock,
    startTime: act.startTime,
    endTime: act.endTime,
    status: act.status
  }
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!form.value.productId || !form.value.seckillPrice || !form.value.seckillStock || !form.value.startTime || !form.value.endTime) {
    ElMessage.warning('请填写所有必填项')
    return
  }
  submitting.value = true
  try {
    let res
    if (isEdit.value && form.value.id) {
      res = await updateSeckillActivity(form.value.id, form.value)
    } else {
      res = await createSeckillActivity(form.value)
    }
    if (res.data.code === '200') {
      ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
      dialogVisible.value = false
      fetchActivities()
    } else {
      ElMessage.error(res.data.msg || '操作失败')
    }
  } catch (e: any) {
    ElMessage.error(e.response?.data?.msg || '操作失败')
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (id: number) => {
  await ElMessageBox.confirm('确定删除该秒杀活动吗？', '提示', { type: 'warning' })
  try {
    await deleteSeckillActivity(id)
    ElMessage.success('删除成功')
    fetchActivities()
  } catch (e: any) {
    ElMessage.error(e.response?.data?.msg || '删除失败')
  }
}

onMounted(async () => {
  const username = sessionStorage.getItem('username')
  const token = sessionStorage.getItem('token')
  if (username && token) {
    try {
      const res = await getUserDetail(username)
      userRole.value = res.data.data?.role || 'user'
    } catch (e) {
      userRole.value = sessionStorage.getItem('userRole') || 'user'
    }
  }
  await fetchActivities()
  await fetchProducts()
  updateCountdowns()
  timer = setInterval(updateCountdowns, 1000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.seckill-page {
  padding: 24px 32px 40px;
  background: var(--em-bg);
  min-height: calc(100vh - 64px);
}

.admin-panel {
  background: #fff;
  border-radius: var(--em-radius-lg);
  box-shadow: var(--em-shadow-sm);
  padding: 20px 24px;
  margin-bottom: 28px;
}

.admin-panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.panel-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--em-primary);
}

.seckill-header {
  display: flex;
  align-items: baseline;
  gap: 12px;
  margin-bottom: 20px;
}

.seckill-title {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  color: var(--em-primary);
}

.seckill-subtitle {
  color: #999;
  font-size: 14px;
}

.empty-tip {
  text-align: center;
  color: #aaa;
  padding: 60px 0;
}

.seckill-grid {
  margin-top: 4px;
}

.seckill-card {
  border-radius: 12px;
  margin-bottom: 20px;
  cursor: default;
  transition: transform 0.2s, box-shadow 0.2s;
}

.seckill-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--em-shadow-sm);
}

.seckill-cover-wrap {
  position: relative;
  height: 180px;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #f8fafc;
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 12px;
}

.seckill-cover {
  width: 100%;
  height: 180px;
  object-fit: cover;
}

.seckill-badge {
  position: absolute;
  top: 8px;
  left: 8px;
  background: #e63946;
  color: #fff;
  font-size: 11px;
  font-weight: 700;
  padding: 2px 8px;
  border-radius: 4px;
}

.img-error {
  color: #ccc;
  font-size: 13px;
}

.seckill-info {
  padding: 0 4px;
}

.seckill-book-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--em-text);
  margin-bottom: 8px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.seckill-price-row {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 6px;
}

.seckill-price {
  font-size: 22px;
  font-weight: 700;
  color: #e63946;
}

.original-price {
  font-size: 13px;
  color: #aaa;
  text-decoration: line-through;
}

.seckill-stock {
  font-size: 12px;
  color: #888;
  margin-bottom: 6px;
}

.seckill-countdown {
  font-size: 13px;
  font-weight: 600;
  margin-bottom: 12px;
}

.countdown-active {
  color: #e63946;
}

.countdown-inactive {
  color: #aaa;
}

.seckill-btn {
  width: 100%;
  font-weight: 600;
}
</style>
