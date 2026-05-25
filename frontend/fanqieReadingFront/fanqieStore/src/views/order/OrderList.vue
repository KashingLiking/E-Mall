<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getOrderList, applyRefund, approveRefund, rejectRefund, updateOrder } from '@/api/order'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Star, ShoppingCart, Service, MessageBox } from '@element-plus/icons-vue'

const orders = ref([])
const userRole = ref('user')
const router = useRouter()
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const loading = ref(false)

const statusMap = {
  'PAID': { label: '已支付', type: 'success' },
  'WAITING_PAYMENT': { label: '待支付', type: 'warning' },
  'PENDING': { label: '待审核', type: 'info' },
  'REFUNDED': { label: '已退款', type: 'danger' },
  'COMPLETED': { label: '已完成', type: 'success' },
  'SHIPPED': { label: '已发货', type: 'primary' }
}

const refundDialogVisible = ref(false)
const refundRejectDialogVisible = ref(false)
const refundReason = ref('')
const rejectReason = ref('')
const selectedOrderId = ref<number | null>(null)

const fetchOrders = async () => {
  loading.value = true
  const res = await getOrderList({ page: currentPage.value, size: pageSize.value })
  if (res.data.data && Array.isArray(res.data.data.list)) {
    orders.value = res.data.data.list
    total.value = res.data.data.total || 0
  } else {
    orders.value = []
    total.value = 0
  }
  loading.value = false
}

onMounted(async () => {
  userRole.value = sessionStorage.getItem('userRole') || 'user'
  await fetchOrders()
})

function goDetail(orderId: number) {
  router.push(`/orders/${orderId}`)
}
function handlePageChange(p: number) {
  currentPage.value = p
  fetchOrders()
}
function returnBack() {
  router.push('/store-detail');
}

// 用户退款申请
function openRefundDialog(orderId: number) {
  selectedOrderId.value = orderId
  refundReason.value = ''
  refundDialogVisible.value = true
}
async function doRefund() {
  // if (!refundReason.value.trim()) {
  //     ElMessage.warning('请填写退款原因')
  //     return
  // }
  // 确认退款
  await ElMessageBox.confirm('确认申请退款？', '确认', { type: 'warning' })
  await applyRefund(selectedOrderId.value!, { reason: refundReason.value })
  ElMessage.success("退款申请已提交，等待审核")
  refundDialogVisible.value = false
  fetchOrders()
}

// 管理员审核通过
async function approve(orderId: number) {
  try {
    await ElMessageBox.confirm('确认批准退款并原路返回金额？', '确认', { type: 'warning' })
    await approveRefund(orderId)
    ElMessage.success("已完成退款")
    fetchOrders()
  } catch (err) {
    if (err !== 'cancel') ElMessage.error('操作被取消')
  }
}

// 管理员审核拒绝
function openRejectDialog(orderId: number) {
  selectedOrderId.value = orderId
  rejectReason.value = ''
  refundRejectDialogVisible.value = true
}
async function doRejectRefund() {
  if (!rejectReason.value.trim()) {
    ElMessage.warning('请填写拒绝原因')
    return
  }
  await rejectRefund(selectedOrderId.value!, { reason: rejectReason.value })
  ElMessage.success("已通知用户，退款未通过")
  refundRejectDialogVisible.value = false
  fetchOrders()
}
// 管理员发货
async function shipOrder(orderId: number) {
  try {
    await ElMessageBox.confirm('确认将该订单标记为【已发货】？', '发货操作', { type: 'warning' })
    await updateOrder(orderId, { status: 'SHIPPED' })  // 这里api方法会自动带token
    ElMessage.success('订单已标记为已发货！')
    fetchOrders()
  } catch (err) {
    if (err !== 'cancel') ElMessage.error('操作失败')
  }
}
</script>

<template>
  <el-container>
    <!-- 顶部导航栏 -->
    <el-header class="bar-header">
      <div style="display: flex">
        <div style="color: black; font-weight: bold; font-size: x-large; margin-left: 10px; margin-top: 10px" @click="returnBack">番茄商店</div>
        <el-icon style="margin-left: 850px; margin-top: 25px">
          <Star />
        </el-icon>
        <div style="margin-top: 20px" @click="">收藏夹</div>
        <el-icon style="margin-left: 30px; margin-top: 25px">
          <ShoppingCart />
        </el-icon>
        <div style="margin-top: 20px" @click="returnBack">购物车</div>
        <el-icon style="margin-left: 30px; margin-top: 25px">
          <Service />
        </el-icon>
        <div style="margin-top: 20px" @click="">售后</div>
        <el-icon style="margin-left: 30px; margin-top: 25px">
          <MessageBox />
        </el-icon>
        <div style="margin-top: 20px" @click="">消息</div>
      </div>
    </el-header>
    <!-- 订单列表 -->
    <div class="order-list">
      <h2 style="text-align: center;color: #ff9900;">订单管理</h2>
      <el-table :data="orders" style="width:100%;" v-loading="loading" border size="small"
                :header-cell-style="{ background: '#f5f5fa', color: '#333', fontWeight: 'bold' }"
                :row-class-name="({ row }) => row.status === 'REFUNDED' ? 'row-refunded' : ''" highlight-current-row>
        <el-table-column prop="orderId" label="订单ID" width="100" align="center" />
        <el-table-column label="金额" width="100" align="center">
          <template #default="scope">
            <span style="font-weight:600;color:#f56c6c;">￥{{ Number(scope.row.totalAmount).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="150" align="center">
          <template #default="scope">
            <el-tag :type="statusMap[scope.row.status]?.type || 'info'" disable-transitions>
              {{ statusMap[scope.row.status]?.label || scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="备注信息" align="center" />
        <el-table-column prop="createTime" label="创建时间" width="160" align="center" />
        <el-table-column label="操作" width="320" align="center">
          <template #default="scope">
            <!-- 管理员：PAID可发货 -->
            <el-button v-if="userRole === 'admin' && scope.row.status === 'PAID'" size="small" type="primary" @click="shipOrder(scope.row.orderId)">
              确认发货
            </el-button>
            <!-- 用户：支持退款 -->
            <el-button v-if="userRole === 'user' && (scope.row.status === 'SHIPPED' || scope.row.status === 'COMPLETED')" size="small" type="danger"
                       @click="openRefundDialog(scope.row.orderId)">申请退款</el-button>
            <!-- 管理员：PENDING可审核 -->
            <el-button v-if="userRole === 'admin' && scope.row.status === 'PENDING'" size="small" type="success"
                       @click="approve(scope.row.orderId)">同意退款</el-button>
            <el-button v-if="userRole === 'admin' && scope.row.status === 'PENDING'" size="small" type="danger"
                       @click="openRejectDialog(scope.row.orderId)">拒绝退款</el-button>
            <!-- 等待支付 -->
            <!-- <el-button v-if="scope.row.status === 'WAITING_PAYMENT'" size="small" type="info" plain disabled>未支付</el-button> -->
            <!-- <el-button v-if="scope.row.status === 'PENDING'" size="small" type="warning" plain disabled>待审核</el-button> -->
          </template>
        </el-table-column>
      </el-table>
      <div style="padding:20px 0;text-align:right">
        <el-pagination background layout="prev, pager, next, total" :current-page="currentPage" :page-size="pageSize" :total="total"
                       @current-change="handlePageChange" hide-on-single-page />
      </div>
    </div>

    <!-- 用户退款申请弹窗 -->
    <el-dialog v-model="refundDialogVisible" title="退款申请" width="420">
      <el-input v-model="refundReason" type="textarea" placeholder="请输入退款原因" :rows="4" />
      <template #footer>
        <el-button @click="refundDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="doRefund">提交</el-button>
      </template>
    </el-dialog>
    <!-- 管理员拒绝弹窗 -->
    <el-dialog v-model="refundRejectDialogVisible" title="拒绝退款" width="420">
      <el-input v-model="rejectReason" type="textarea" placeholder="请输入拒绝原因(将通知用户)" :rows="3" />
      <template #footer>
        <el-button @click="refundRejectDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="doRejectRefund">确定</el-button>
      </template>
    </el-dialog>
  </el-container>
</template>
<style scoped>
.order-list {
  height: calc(100vh - 64px);
  padding: 0px 20px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 32px 0 rgba(88, 122, 176, 0.10);
}

.el-table .row-refunded {
  background: #faf0f2;
  color: #eb3b3b;
}
</style>