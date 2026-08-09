<script setup lang="ts">
import { ref, computed, onMounted, reactive } from 'vue';
import { ElMessage, UploadProps } from 'element-plus';
import { Search, Plus } from '@element-plus/icons-vue'
import { addProduct, getProductInfo, getSpecificProduct, searchProductByTitle } from "@/api/product";
import { useRouter } from 'vue-router';
import { addBookToCart, deleteCart, getCart } from "@/api/cart";
import { getUserDetail } from "@/api/accounts";
import { createOrder, payOrder } from '@/api/order'
import { logout } from '@/utils/auth'
import { uploadImage } from '@/api/upload'
import { resolveImageUrl } from '@/utils/image'
import { watch } from 'vue';


const router = useRouter();

// 获得用户信息
const username = String(sessionStorage.getItem('username'));
const userRole = ref("")
const userAvatar = ref("");

// 购物车提交窗口是否可见
const centerDialogVisible1 = ref(false);
// 创建商品窗口是否可见
const centerDialogVisible2 = ref(false);
const CHECKOUT_INFO_KEY = 'EMall_checkout_info';
const bookTypes = [
  {
    "value": "Romance",
    "label": "言情"
  },
  {
    "value": "Science",
    "label": "科普"
  },
  {
    "value": "Magical",
    "label": "奇幻"
  },
  {
    "value": "Fairy",
    "label": "童话"
  },
  {
    "value": "Sports",
    "label": "体育"
  },
  {
    "value": "Philosophy",
    "label": "哲学"
  },
]
const checkoutInfo = reactive({
  cartItemIds: <string[]>[],
  shippingAddress: {
    name: '',
    phone: '',
    detail: ''
  },
  paymentMethod: 'MOCK'
});
// 获得商品模块
interface ProductVO {
  id: number,
  title: string,
  price: number,
  rate: number,
  description: string,
  cover: string,
  detail: string,
  bookType: string,
  specifications: Specification[];
  stockpile: Stockpile;
}

interface Specification {
  id: number,
  item: string,
  value: string,
  product_id: number;
}
interface Stockpile {
  id: number;
  product_id: number;
  amount: number;
  frozen: number;
}

// 已有的bookList
const bookList = ref<ProductVO[]>([]);
const selectedCategory = ref('');
// 监听弹窗，回填结算缓存
watch(() => centerDialogVisible1.value, (opened) => {
  if (opened) {
    const cache = localStorage.getItem(CHECKOUT_INFO_KEY);
    if (cache) {
      const data = JSON.parse(cache);
      checkoutInfo.shippingAddress.name = data.shippingAddress?.name || '';
      checkoutInfo.shippingAddress.phone = data.shippingAddress?.phone || '';
      checkoutInfo.shippingAddress.detail = data.shippingAddress?.detail || '';
    }
  }
});
// 创建的book信息
interface CreateProductVO {
  title: string,
  price: number,
  rate: number,
  description: string,
  cover: string,
  detail: string,
  bookType: string,
  specifications: Specifications[];
}

interface Specifications {
  item: string,
  value: string,
}

const author = ref<string>('')
const ISBN = ref<string>('')
const bindingAndLayout = ref<string>('')// 装帧
const pages = ref<string>('')
const publishingHouse = ref<string>('')
const publicationDate = ref<string>('')

const bookInfo = reactive<CreateProductVO>({
  title: '',
  price: 0,
  rate: 0,
  description: '',
  cover: '',
  detail: '',
  bookType: '',
  specifications: [],
})
// 添加规格
const addSpecification = (Item: string, Value: string) => {
  bookInfo.specifications.push({
    item: Item,
    value: Value,
  });
}

// 购物车的书籍信息
interface CartItems {
  cartItemId: number,
  productId: number,
  title: string,
  price: number,
  description: string,
  cover: string,
  detail: string,
  quantity: number,
}

interface CartInfo {
  cartItemVOList: CartItems[],
  total: number,
  totalAmount: number,
}

const cartData = ref<CartInfo>({
  cartItemVOList: [],
  total: 0,
  totalAmount: 0,
});

const selectedCartItems = ref<CartItems[]>([]);

const handleCartSelectionChange = (rows: CartItems[]) => {
  selectedCartItems.value = rows;
};

const openCheckout = () => {
  if (selectedCartItems.value.length === 0) {
    ElMessage.warning('请先勾选要结账的商品');
    return;
  }
  centerDialogVisible1.value = true;
};

// 获取用户信息
const fetchUserInfo = async () => {
  try {
    getUserDetail(username).then((res) => {
      console.log("user detail:", res.data.data);
      userRole.value = res.data.data.role;
      userAvatar.value = resolveImageUrl(res.data.data.avatar);
      // 写入sessionStorage
      sessionStorage.setItem('userRole', userRole.value);
      sessionStorage.setItem('userAvatar', userAvatar.value);
    });
  } catch (error) {
    console.error("获取用户信息失败！", error);
  }
}

// 获取已有书籍列表
const fetchBookList = async () => {
  try {
    getProductInfo().then((res) => {
      console.log("book res:", res);
      console.log("book res.data:", res.data);
      bookList.value = res.data.data;
    });
  } catch (error) {
    console.error("书籍列表获取失败！", error);
  }
};

// 获取购物车（内涵商品信息、分别购买数量、总数目和总价）
const fetchCart = async () => {
  try {
    getCart().then((res) => {
      console.log("cart res:", res);
      console.log("cart res.data:", res.data);
      cartData.value = res.data.data;
    });
  } catch (error) {
    console.error('获取购物车失败！', error);
  }
};

// 计算已勾选商品总价
const sumPrice = computed(() => {
  const total = selectedCartItems.value.reduce((accumulator, currentItem) => {
    return accumulator + currentItem.price * currentItem.quantity;
  }, 0);

  cartData.value.total = total;
  cartData.value.totalAmount = selectedCartItems.value.length;

  return total;
});

// 搜索模块
const searchInput = ref('');
const searchDialogVisible = ref(false);
const searchResults = ref<any[]>([]);

async function clickSearch() {
  const keyword = searchInput.value.trim();
  if (!keyword) {
    ElMessage.warning('请输入书籍名称');
    return;
  }
  const res = await searchProductByTitle(keyword);
  const results = res.data?.data ?? [];
  if (results.length === 0) {
    ElMessage.error('未找到相关书籍');
    return;
  }
  searchResults.value = results;
  searchDialogVisible.value = true;
}

// 添加商品模块
function addBook() {
  if (!bookInfo.title?.trim() || !bookInfo.bookType || bookInfo.price === null || bookInfo.price === undefined || Number(bookInfo.price) <= 0) {
    ElMessage.warning('请填写书名、分类和价格');
    return;
  }
  // 重置规格，只写入有内容的选填项
  bookInfo.specifications = [];
  const specs: Array<[string, string]> = [
    ['作者', author.value],
    ['ISBN', ISBN.value],
    ['装帧', bindingAndLayout.value],
    ['页数', pages.value],
    ['出版社', publishingHouse.value],
    ['出版日期', publicationDate.value ? String(publicationDate.value) : ''],
  ];
  specs.forEach(([item, value]) => {
    if (value && String(value).trim()) {
      addSpecification(item, String(value).trim());
    }
  });
  // 评分选填，未填时交给后端默认 0
  if (!bookInfo.rate) {
    bookInfo.rate = 0;
  }
  try {
    addProduct(bookInfo).then((res) => {
      if (res.data.code === '200') {
        ElMessage({
          message: "创建成功！",
          type: 'success',
          center: true,
        })
        fetchBookList();
      } else if (res.data.code === '400') {
        ElMessage({
          message: res.data.msg,
          type: 'error',
          center: true,
        })
      }
      centerDialogVisible2.value = false;
    })
  } catch (error) {
    ElMessage.error('添加商品失败！');
  }
}

// 点击头像进入更改个人信息
function clickAvatar() {
  router.push('/dashboard');
}

function handleLogout() {
  logout();
}

// 点击商品下加号触发加入购物车
function clickBuy(id: number) {
  addBookToCart(id, 1).then((res) => {
    if (res.data.code === '200') {
      ElMessage.success('已加入购物车！');
      // 只刷新侧边栏购物车，不要整页 reload（否则会跳回第一个分类「言情」）
      fetchCart();
    } else if (res.data.code === '405') {
      ElMessage.error('库存不足！');
    } else {
      ElMessage.error(res.data.msg || '加入购物车失败！');
    }
  }).catch(() => {
    ElMessage.error('加入购物车失败！');
  })
}

async function handleCheckout() {
  localStorage.setItem(CHECKOUT_INFO_KEY, JSON.stringify(checkoutInfo));
  if (checkoutInfo.shippingAddress.name.trim() === '') {
    ElMessage.error('请填写收货人！')
    return;
  }
  if (checkoutInfo.shippingAddress.phone.trim() === '') {
    ElMessage.error('请填写手机号！')
    return;
  }
  if (checkoutInfo.shippingAddress.detail.trim() === '') {
    ElMessage.error('请填写地址！')
    return;
  }
  if (selectedCartItems.value.length === 0) {
    ElMessage.warning('请先勾选要结账的商品');
    return;
  }
  try {
    checkoutInfo.cartItemIds = selectedCartItems.value.map(item => String(item.cartItemId));
    checkoutInfo.paymentMethod = 'MOCK';
    const orderResp = await createOrder(checkoutInfo);
    if (orderResp.data?.code === '200' && orderResp.data.data) {
      const orderId = orderResp.data.data.orderId;
      const payResp = await payOrder(orderId);
      if (payResp.data?.code === '200') {
        ElMessage.success('支付成功');
        centerDialogVisible1.value = false;
        selectedCartItems.value = [];
        await fetchCart();
        router.push('/orders');
      } else {
        ElMessage.error(payResp.data?.msg || '支付失败');
      }
    } else {
      ElMessage.error(orderResp.data?.msg || '订单创建失败');
    }
  } catch {
    ElMessage.error('下单或支付失败，请稍后再试');
  }
}

const goToBookDetail = (bookId: number) => {
  router.push('/book-detail/' + bookId);
}

// 购物车删除行
const deleteRow = (cartItemId: number) => {
  try {
    deleteCart(cartItemId).then((res) => {
      if (res.data.code === '200') {  //类型守卫，它检查 res.data 对象中是否存在名为 code 的属性
        ElMessage({
          message: "删除成功！",
          type: 'success',
          center: true,
        })
        // 刷新购物车
        selectedCartItems.value = selectedCartItems.value.filter(item => item.cartItemId !== cartItemId);
        fetchCart();
      } else if (res.data.code === '400') {
        ElMessage({
          message: res.data.msg,
          type: 'error',
          center: true,
        })
      }
    })
  } catch (error) {
    ElMessage.error("删除失败！");
  }
};

// 处理商品图片上传
const beforeImageUpload: UploadProps['beforeUpload'] = (rawFile) => {
  const okTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/webp'];
  if (!okTypes.includes(rawFile.type)) {
    ElMessage.error('仅支持 JPG/PNG/WEBP 图片');
    return false;
  }
  if (rawFile.size / 1024 / 1024 > 2) {
    ElMessage.error('图片不应超过 2MB');
    return false;
  }
  customUpload(rawFile);
  return false;
};

async function customUpload(file: File) {
  try {
    const res: any = await uploadImage(file);
    const path = res?.data?.data;
    if (res?.data?.code === '200' && path) {
      bookInfo.cover = path;
      ElMessage.success('封面上传成功');
    } else {
      ElMessage.error(res?.data?.msg || '上传失败');
    }
  } catch (e) {
    ElMessage.error('上传失败，请确认已登录且后端已启动');
  }
}

onMounted(() => {
  fetchUserInfo();
  fetchBookList();
  fetchCart();
})
// 点击进入订单列表
function goToOrderList() {
  router.push('/orders');
}
</script>

<template>
  <el-container>
    <el-aside width="280px" class="store-sidebar">
      <div class="sidebar-brand">
        <span>📚 EMall书城</span>
      </div>
      <div class="sidebar-cart">
        <div class="sidebar-cart-header">
          <div class="sidebar-cart-title">🛒 购物车</div>
          <div style="display:flex;flex-direction:column;gap:4px;align-items:flex-end">
            <el-button link type="warning" class="order-link" @click="goToOrderList">
              📒 订单管理
            </el-button>
            <el-button link type="warning" class="order-link" @click="router.push('/seckill')">
              ⚡ 秒杀专区
            </el-button>
          </div>
        </div>
        <el-table
          :data="cartData.cartItemVOList"
          style="width: 100%; margin-top: 20px;"
          max-height="500"
          class="transparent-table"
          @selection-change="handleCartSelectionChange"
        >
          <el-table-column type="selection" width="40" />
          <el-table-column prop="title" label="名称" width="70" />
          <el-table-column prop="price" label="价格" width="55" />
          <el-table-column prop="quantity" label="数量" width="50" />
          <el-table-column prop="remove" label="操作" min-width="50">
            <template #default="scope">
              <el-button link type="warning" size="small" @click.prevent="deleteRow(scope.row.cartItemId)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="cart-total">已选合计: <strong>{{ sumPrice }}</strong> 元</div>
        <el-button class="checkout-btn" type="warning" round @click="openCheckout">
          去结账
        </el-button>
      </div>
    </el-aside>
    <el-container class="store-main">
      <div class="main-toolbar-bar">
        <span class="page-label">主页</span>
        <el-input v-model="searchInput" class="search-input" placeholder="搜索书籍名称" @keyup.enter="clickSearch">
          <template #append>
            <el-button type="primary" @click="clickSearch">
              <el-icon><Search /></el-icon>
            </el-button>
          </template>
        </el-input>
        <el-avatar :size="40" :src="userAvatar" class="user-avatar-btn" @click="clickAvatar" />
        <el-button round plain type="danger" size="small" @click="handleLogout">退出登录</el-button>
      </div>
      <div class="product-tabs-wrap">
        <div class="product-wrap-header">
          <h2 class="section-title">📖 商品列表</h2>
          <el-button v-if="userRole == 'admin'" @click="centerDialogVisible2 = true;" type="primary" plain round>添加商品</el-button>
        </div>
        <div class="category-layout">
          <div class="category-sidebar">
            <div
              class="category-item"
              :class="{ active: selectedCategory === '' }"
              @click="selectedCategory = ''"
            >全部</div>
            <div
              v-for="category in bookTypes"
              :key="category.value"
              class="category-item"
              :class="{ active: selectedCategory === category.value }"
              @click="selectedCategory = category.value"
            >{{ category.label }}</div>
          </div>
          <div class="product-list">
            <el-row :gutter="20">
              <el-col
                v-for="book in bookList.filter(b => selectedCategory === '' || b.bookType === selectedCategory)"
                :key="book.id"
                :xs="24" :sm="12" :md="8" :lg="6" :xl="6"
              >
                <el-card @click="goToBookDetail(book.id)" class="book-card" shadow="hover">
                  <template #header>
                    <div class="book-header">
                      <el-image class="book-image" :src="resolveImageUrl(book.cover)" :alt="book.title" fit="cover">
                        <template #error>
                          <div class="image-error">图片加载失败</div>
                        </template>
                      </el-image>
                    </div>
                  </template>
                  <div class="book-name">{{ book.title }}</div>
                  <el-rate v-model="book.rate" disabled show-score text-color="#ff9900" score-template="{value} points" />
                  <div class="book-card-footer">
                    <div class="book-price">￥{{ book.price }}</div>
                    <el-button class="buy-button" type="warning" circle @click.stop="clickBuy(book.id)">
                      <el-icon><Plus /></el-icon>
                    </el-button>
                  </div>
                </el-card>
              </el-col>
            </el-row>
          </div>
        </div>
      </div>
    </el-container>
    <el-backtop :right="50" :bottom="50" style="color: #565656" />
  </el-container>

  <!--购物车提交弹出窗口-->
  <el-dialog v-model="centerDialogVisible1" title="结算信息" width="500" center>
    <el-form :model="checkoutInfo" label-width="80px">
      <el-form-item label="姓名">
        <el-input v-model="checkoutInfo.shippingAddress.name" />
      </el-form-item>
      <el-form-item label="手机号">
        <el-input v-model="checkoutInfo.shippingAddress.phone" />
      </el-form-item>
      <el-form-item label="地址">
        <el-input v-model="checkoutInfo.shippingAddress.detail" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="centerDialogVisible1 = false">取消</el-button>
      <el-button type="primary" @click="handleCheckout">提交订单并支付</el-button>
    </template>
  </el-dialog>

  <!--创建商品弹出窗口-->
  <el-dialog v-model="centerDialogVisible2" title="创建商品" width="560" center>
    <el-form :model="bookInfo" label-width="80px" label-position="top">
      <el-form-item label="书名" required>
        <el-input v-model="bookInfo.title" placeholder="请输入书名" />
      </el-form-item>
      <el-form-item label="分类" required>
        <el-select v-model="bookInfo.bookType" placeholder="请选择分类" style="width: 100%">
          <el-option
            v-for="item in bookTypes"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="价格" required>
        <el-input v-model="bookInfo.price" placeholder="单位：元" type="number" />
      </el-form-item>

      <el-collapse>
        <el-collapse-item title="更多信息（选填）" name="more">
          <el-form-item label="作者">
            <el-input v-model="author" placeholder="选填" />
          </el-form-item>
          <el-form-item label="评分">
            <el-rate v-model="bookInfo.rate" allow-half />
          </el-form-item>
          <el-form-item label="ISBN">
            <el-input v-model="ISBN" placeholder="选填" />
          </el-form-item>
          <el-form-item label="封面">
            <el-upload class="avatar-uploader" :show-file-list="false" :before-upload="beforeImageUpload" accept="image/jpeg,image/png,image/webp">
              <img v-if="bookInfo.cover" :src="resolveImageUrl(bookInfo.cover)" class="avatar" />
              <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
            </el-upload>
          </el-form-item>
          <el-form-item label="详细说明">
            <el-input v-model="bookInfo.detail" type="textarea" :rows="3" placeholder="选填" />
          </el-form-item>
          <el-form-item label="装帧">
            <el-input v-model="bindingAndLayout" placeholder="选填" />
          </el-form-item>
          <el-form-item label="出版社">
            <el-input v-model="publishingHouse" placeholder="选填" />
          </el-form-item>
          <el-form-item label="页数">
            <el-input v-model="pages" placeholder="选填" />
          </el-form-item>
          <el-form-item label="出版日期">
            <el-date-picker v-model="publicationDate" type="date" placeholder="选填" style="width: 100%" />
          </el-form-item>
        </el-collapse-item>
      </el-collapse>
    </el-form>
    <template #footer>
      <el-button @click="centerDialogVisible2 = false">取消</el-button>
      <el-button type="primary" @click="addBook">确定</el-button>
    </template>
  </el-dialog>

  <!-- 搜索结果弹窗 -->
  <el-dialog v-model="searchDialogVisible" title="搜索结果" width="600px" :close-on-click-modal="true">
    <el-table :data="searchResults" style="width: 100%">
      <el-table-column label="封面" width="70">
        <template #default="scope">
          <el-image :src="resolveImageUrl(scope.row.cover)" fit="cover" style="width:48px;height:60px;border-radius:4px">
            <template #error><div style="width:48px;height:60px;background:#f5f5f5;display:flex;align-items:center;justify-content:center;font-size:10px;color:#999">无图</div></template>
          </el-image>
        </template>
      </el-table-column>
      <el-table-column prop="title" label="书名" min-width="160" show-overflow-tooltip />
      <el-table-column prop="price" label="价格" width="80">
        <template #default="scope">¥{{ scope.row.price }}</template>
      </el-table-column>
      <el-table-column label="操作" width="130">
        <template #default="scope">
          <el-button size="small" type="primary" plain @click="goToBookDetail(scope.row.id); searchDialogVisible = false">查看详情</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-dialog>
</template>

<style scoped>
.store-sidebar {
  background: linear-gradient(180deg, var(--em-primary) 0%, #152a45 100%);
  position: fixed;
  top: 0;
  left: 0;
  height: 100vh;
  z-index: 1100;
  box-shadow: 4px 0 20px rgba(30, 58, 95, 0.2);
  overflow-y: auto;
}

.sidebar-brand {
  padding: 24px 20px;
  font-size: 20px;
  font-weight: 700;
  color: #fff;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.sidebar-cart {
  padding: 16px 14px 24px;
}

.sidebar-cart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.sidebar-cart-title {
  color: #fff;
  font-weight: 600;
}

.order-link {
  font-weight: 600 !important;
  color: var(--em-accent) !important;
}

.cart-total {
  margin-top: 16px;
  color: rgba(255, 255, 255, 0.9);
  font-size: 15px;
}

.cart-total strong {
  color: var(--em-accent);
  font-size: 18px;
}

.checkout-btn {
  width: 100%;
  margin-top: 16px;
  font-weight: 600;
}

.store-main {
  margin-left: 280px;
  flex-direction: column;
  background: var(--em-bg);
  height: 100vh;
  overflow: hidden;
  padding: 24px 28px 24px;
  display: flex;
  box-sizing: border-box;
}

.main-toolbar-bar {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
  background: #fff;
  border-radius: var(--em-radius-lg);
  box-shadow: var(--em-shadow-sm);
  padding: 14px 20px;
  margin-bottom: 16px;
}

.product-wrap-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px 16px 20px;
  border-bottom: 1px solid var(--el-border-color-light);
}

.page-label {
  font-size: 18px;
  font-weight: 700;
  color: var(--em-primary);
  white-space: nowrap;
}

.search-input {
  flex: 1;
  max-width: 480px;
  margin: 0 auto;
}

.user-avatar-btn {
  cursor: pointer;
  border: 2px solid var(--em-border);
  transition: transform 0.2s;
}

.user-avatar-btn:hover {
  transform: scale(1.05);
}


.section-title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--em-primary);
}

.product-tabs-wrap {
  border-radius: var(--em-radius);
  overflow: hidden;
  box-shadow: var(--em-shadow-sm);
  background: #fff;
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.category-layout {
  display: flex;
  flex: 1;
  min-height: 0;
}

.category-sidebar {
  width: 100px;
  flex-shrink: 0;
  background: #f5f7fa;
  border-right: 1px solid var(--el-border-color-light);
  padding: 12px 0;
  overflow-y: auto;
}

.category-item {
  padding: 12px 16px;
  font-size: 14px;
  color: var(--em-text);
  cursor: pointer;
  transition: background 0.2s, color 0.2s;
  border-left: 3px solid transparent;
}

.category-item:hover {
  background: #eef2f8;
  color: var(--em-primary);
}

.category-item.active {
  background: #eef2f8;
  color: var(--em-primary);
  font-weight: 600;
  border-left-color: var(--em-primary);
}

.product-list {
  flex: 1;
  padding: 16px 20px;
  overflow-y: auto;
}



.book-card {
  transition: transform 0.25s ease, box-shadow 0.25s ease;
  border-radius: 12px;
  margin-top: 16px;
  cursor: pointer;
}

.book-card:hover {
  transform: translateY(-6px);
  box-shadow: var(--em-shadow-sm);
}

.book-card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
}

.book-header {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 190px;
}

.book-image {
  width: 180px;
  height: 180px;
  object-fit: cover;
  border-radius: 8px;
}

.book-name {
  font-weight: 600;
  font-size: 16px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  color: var(--em-text);
}

.book-price {
  font-size: 18px;
  font-weight: 700;
  color: #e67e22;
}

.transparent-table {
  --el-table-bg-color: transparent;
  --el-table-border-color: rgba(255, 255, 255, 0.15);
  --el-table-header-bg-color: transparent;
  --el-table-header-font-color: rgba(255, 255, 255, 0.8);
  --el-table-text-color: #ffffff;
  --el-table-tr-bg-color: transparent;
  --el-table-current-row-bg-color: transparent;
  --el-table-row-hover-bg-color: transparent;
  --el-table-hover-row-bg-color: transparent;
}

.transparent-table :deep(.el-table__body tr:hover > td.el-table__cell) {
  background-color: transparent !important;
}

.transparent-table :deep(.el-table__body tr.hover-row > td.el-table__cell) {
  background-color: transparent !important;
}

.avatar-uploader {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  height: 120px;
  width: 120px;
  transition: var(--el-transition-duration-fast);
}

.avatar-uploader:hover {
  border-color: #409eff;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 120px;
  height: 120px;
  text-align: center;
}
</style>