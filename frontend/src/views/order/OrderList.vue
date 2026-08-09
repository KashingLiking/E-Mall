<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getOrderList } from '@/api/order'
import AppNav from '@/components/AppNav.vue'

const router = useRouter()
const goStore = () => router.push('/store-detail')

const orders = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const loading = ref(false)

const statusMap: Record<string, { label: string; type: string }> = {
  PAID: { label: '已支付', type: 'success' },
  WAITING_PAYMENT: { label: '待支付', type: 'warning' },
  PENDING: { label: '待审核', type: 'info' },
  REFUNDED: { label: '已退款', type: 'danger' },
  COMPLETED: { label: '已完成', type: 'success' },
  SHIPPED: { label: '已发货', type: 'primary' },
}

const fetchOrders = async () => {
  loading.value = true
  try {
    const res = await getOrderList({ page: currentPage.value, size: pageSize.value })
    if (res.data.data && Array.isArray(res.data.data.list)) {
      orders.value = res.data.data.list
      total.value = res.data.data.total || 0
    } else {
      orders.value = []
      total.value = 0
    }
  } finally {
    loading.value = false
  }
}

onMounted(fetchOrders)

function handlePageChange(page: number) {
  currentPage.value = page
  fetchOrders()
}
</script>

<template>
  <el-container direction="vertical">
    <AppNav />
    <div class="page-shell">
      <h2 class="page-title">📋 我的订单</h2>
      <div class="content-card order-list">
        <el-table :data="orders" style="width:100%;" v-loading="loading" stripe size="default">
          <el-table-column prop="orderId" label="订单ID" width="120" align="center" />
          <el-table-column label="金额" width="120" align="center">
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
          <el-table-column prop="createTime" label="创建时间" min-width="180" align="center"
            :formatter="(row) => row.createTime ? row.createTime.replace('T', ' ') : '-'"
          />
        </el-table>
        <div class="pagination-wrap">
          <el-pagination
            background
            layout="prev, pager, next, total"
            :current-page="currentPage"
            :page-size="pageSize"
            :total="total"
            @current-change="handlePageChange"
            hide-on-single-page
          />
        </div>
      </div>
      <div class="order-footer">
        <el-button type="primary" @click="goStore">继续购物</el-button>
      </div>
    </div>
  </el-container>
</template>

<style scoped>
.order-list {
  padding: 20px 24px 12px;
  background: #fff;
}

.pagination-wrap {
  padding: 20px 0 8px;
  text-align: right;
}

.order-footer {
  padding: 20px 0 8px;
  text-align: center;
}

:deep(.el-table th) {
  background: #f8fafc !important;
  color: var(--em-primary);
  font-weight: 600;
}
</style>
