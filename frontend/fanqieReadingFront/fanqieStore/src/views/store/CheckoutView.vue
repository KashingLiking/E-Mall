<template>
  <div class="checkout-container">
    <el-card class="checkout-card">
      <template #header>
        <div class="checkout-header">
          <h2>订单结算</h2>
          <p class="subtext">书香为伴，静心沉淀每一页的温柔</p>
        </div>
      </template>

      <div class="order-summary">
        <h3>订单摘要</h3>
        <el-divider />

        <el-table :data="cartItems" style="width: 100%" border>
          <el-table-column prop="title" label="商品名称" width="180" />
          <el-table-column prop="price" label="单价" width="100" />
          <el-table-column prop="quantity" label="数量" width="80" />
          <el-table-column label="小计" width="120">
            <template #default="scope">
              {{ scope.row.price * scope.row.quantity }} 元
            </template>
          </el-table-column>
        </el-table>

        <div class="total-amount">
          <p>共计书籍: {{ totalQuantity }} 本</p>
          <p class="total-price">合计金额: {{ totalPrice }} 元</p>
        </div>
      </div>

      <div class="payment-method">
        <h3>支付方式</h3>
        <el-divider />

        <el-radio-group v-model="paymentMethod">
          <el-radio label="alipay">
            <el-icon><Apple /></el-icon> 支付宝
          </el-radio>
        </el-radio-group>
      </div>

      <div class="shipping-address">
        <h3>收货地址</h3>
        <el-divider />

        <el-form-item :model="addressForm" label-width="80px">
          <el-form-item label="收货人">
            <el-input v-model="addressForm.name" placeholder="请填写收货人的姓名" />
          </el-form-item>
          <el-form-item label="联系电话">
            <el-input v-model="addressForm.phone" placeholder="便于联系您的号码" />
          </el-form-item>
          <el-form-item label="邮编">
            <el-input v-model="addressForm.zipCode" placeholder="请填写邮政编码" />
          </el-form-item>
          <el-form-item label="详细地址">
            <el-input
                v-model="addressForm.detail"
                type="textarea"
                placeholder="请写下这个美好旅程的目的地"
                :rows="2"
            />
          </el-form-item>
        </el-form-item>
      </div>

      <div class="checkout-actions">
        <el-button type="primary" @click="confirmOrder" size="large">
          确认订单 ✧ 踏上旅途
        </el-button>
        <el-button @click="goBack" size="large">
          回到书架
        </el-button>
      </div>
    </el-card>
    <div v-html="paymentForm" style="display: none"></div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed,nextTick} from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { Apple, ChatLineRound, CreditCard } from '@element-plus/icons-vue';
import { createOrder, payOrder } from '@/api/order';

const router = useRouter();
// 在script setup顶部添加
const paymentForm = ref<any>(null);

// 模拟购物车数据
const cartItems = ref([
  {
    productId: '1',
    title: '雷锋的故事',
    price: 111,
    quantity: 1,
  },
  {
    productId: '2',
    title: '书1',
    price: 111,
    quantity: 2,
  },
  {
    productId: '3',
    title: '书2',
    price: 111,
    quantity: 1,
  },
]);

// 计算总书籍数量
const totalQuantity = computed(() => {
  return cartItems.value.reduce((total, item) => total + item.quantity, 0);
});

// 计算合计金额
const totalPrice = computed(() => {
  return cartItems.value.reduce((total, item) => total + (item.price * item.quantity), 0);
});

// 支付方式
const paymentMethod = ref('alipay');

// 地址表单
const addressForm = ref({
  name: '',
  phone: '',
  detail: '',
  zipCode: ''
});

// 提交订单 + 发起支付
const confirmOrder = async () => {
  if (!addressForm.value.name || !addressForm.value.phone || !addressForm.value.detail||!addressForm.value.zipCode) {
    ElMessage.error('请填写完整的收货信息 ✿');
    return;
  }

  try {
    const checkoutData = {
      cartItemIds: cartItems.value.map(item => item.productId),
      shipping_address: {
        name: addressForm.value.name,
        phone: addressForm.value.phone,
        zipCode: addressForm.value.zipCode,  // 添加邮编
        detail: addressForm.value.detail
      },
      payment_method: paymentMethod.value.toUpperCase()
    };

    const { data: orderData } = await createOrder(checkoutData);
    const orderId = orderData.orderId;

    const { data: paymentData } = await payOrder(orderId);
    paymentForm.value = paymentData.data.paymentForm;


    nextTick(() => {
      const form = document.querySelector('form');
      if (form) {
        form.submit();
      }
    });
    ElMessage.success('订单已提交，正在跳转至支付宝 ✧');

  } catch (error) {
    console.error('支付出错：', error);
    ElMessage.error('订单提交失败，请稍后再试~');
  }
};

// 返回
const goBack = () => {
  router.go(-1);
};
</script>

<style scoped>
.checkout-container {
  padding: 40px 20px;
  max-width: 1000px;
  margin: 0 auto;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  background-color: #f5f7fa;
}

.checkout-card {
  margin-top: 20px;
  border-radius: 16px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.checkout-header {
  text-align: center;
  padding: 20px 0;
  background-color: #f0f2f5;
}

.checkout-header h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
}

.order-summary h3,
.payment-method h3,
.shipping-address h3 {
  margin-bottom: 10px;
  font-size: 20px;
  color: #303133;
}

.order-summary,
.payment-method,
.shipping-address {
  margin-bottom: 30px;
  padding: 0 20px;
}

.el-table {
  border-radius: 8px;
  overflow: hidden;
}

.total-amount {
  text-align: right;
  margin-top: 20px;
  font-size: 16px;
  padding: 0 10px;
  color: #606266;
}

.total-price {
  font-size: 22px;
  font-weight: 700;
  color: #f56c6c;
  margin-top: 8px;
}

.checkout-actions {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 40px;
  padding-bottom: 20px;
}

.checkout-actions .el-button {
  padding: 12px 30px;
  border-radius: 24px;
  font-size: 16px;
}

.el-button--primary {
  background-color: #409EFF;
  border-color: #409EFF;
}

.el-button--primary:hover {
  background-color: #66b1ff;
  border-color: #66b1ff;
}

.el-radio {
  display: flex;
  align-items: center;
  margin-right: 30px;
  font-size: 16px;
  margin-top: 10px;
}

.el-radio svg {
  margin-right: 8px;
}

.el-form-item {
  margin-bottom: 20px;
}

.el-input,
.el-textarea {
  border-radius: 8px;
}

@media (max-width: 768px) {
  .checkout-container {
    padding: 20px 10px;
  }

  .checkout-actions {
    flex-direction: column;
    gap: 12px;
  }

  .el-table-column {
    font-size: 14px;
  }
}

</style>