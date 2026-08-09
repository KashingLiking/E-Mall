<script setup lang="ts">
import { House, ShoppingCart, List, Promotion } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { logout } from '@/utils/auth'

const router = useRouter()

const goStore = () => router.push('/store-detail')
const goOrders = () => router.push('/orders')
const goSeckill = () => router.push('/seckill')
const handleLogout = () => logout()
</script>

<template>
  <header class="app-nav">
    <div class="app-nav-brand" @click="goStore">📚 EMall书城</div>
    <nav class="app-nav-links">
      <div class="app-nav-item" @click="goStore">
        <el-icon><House /></el-icon>
        <span>商城</span>
      </div>
      <div class="app-nav-item" @click="goSeckill">
        <el-icon><Promotion /></el-icon>
        <span>秒杀</span>
      </div>
      <div class="app-nav-item" @click="goStore">
        <el-icon><ShoppingCart /></el-icon>
        <span>购物车</span>
      </div>
      <div class="app-nav-item" @click="goOrders">
        <el-icon><List /></el-icon>
        <span>订单</span>
      </div>
      <el-button type="danger" size="small" round plain style="margin-left: 8px" @click="handleLogout">
        退出登录
      </el-button>
    </nav>
  </header>
</template>
