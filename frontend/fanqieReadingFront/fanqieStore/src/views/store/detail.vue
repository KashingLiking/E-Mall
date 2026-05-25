<script setup lang="ts">
import { ref, computed, onMounted, reactive } from 'vue';
import { ElMessage, UploadProps } from 'element-plus';
import { Search, BellFilled, ShoppingCart, Plus, ShoppingCartFull, Delete } from '@element-plus/icons-vue'
import { addProduct, getProductInfo, getSpecificProduct } from "@/api/product";
import { addAdvertisement, deleteAdvertisement, getAdvertisement, updateAdvertisement } from "@/api/advertisement";
import { useRouter } from 'vue-router';
import { addBookToCart, deleteCart, getCart } from "@/api/cart";
import { getUserDetail } from "@/api/accounts";
import { createOrder, payOrder } from '@/api/order'
import { watch } from 'vue';
import { nextTick } from 'vue';


const router = useRouter();

// 获得用户信息
const username = String(sessionStorage.getItem('username'));
const userRole = ref("")
const userAvatar = ref("");

// 购物车提交窗口是否可见
const centerDialogVisible1 = ref(false);
// 创建商品窗口是否可见
const centerDialogVisible2 = ref(false);
// 创建广告窗口是否可见
const centerDialogVisible3 = ref(false);
// 修改广告窗口是否可见
const centerDialogVisible4 = ref(false);
const payDialogVisible = ref(false)    // 支付弹窗
const payForm = ref('')
const payLoading = ref(false)
const CHECKOUT_INFO_KEY = 'tomato_checkout_info';
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
    zipCode: '',
    detail: ''
  },
  paymentMethod: 'ALIPAY'
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
// 监听弹窗，回填结算缓存
watch(() => centerDialogVisible1.value, (opened) => {
  if (opened) {
    const cache = localStorage.getItem(CHECKOUT_INFO_KEY);
    if (cache) {
      const data = JSON.parse(cache);
      checkoutInfo.shippingAddress.name = data.shippingAddress?.name || '';
      checkoutInfo.shippingAddress.phone = data.shippingAddress?.phone || '';
      checkoutInfo.shippingAddress.zipCode = data.shippingAddress?.zipCode || '';
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
const subtitle = ref<string>('')
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

// 已有的广告模块
interface Advertisement {
  id: number,
  title: string,
  content: string,
  imageUrl: string,
  productId: string,
}
// 已有的广告
const advertisementList = ref<Advertisement[]>([]);

// 创建广告的接口
interface CreateAdvertisement {
  title: string,
  content: string,
  imgUrl: string,
  productId: string,
}
// 创建的广告
const createAdvertisementInfo = reactive<CreateAdvertisement>({
  title: '',
  content: '',
  imgUrl: '',
  productId: '',
})

// 更新广告的接口
interface UpdateAdvertisement {
  id: number,
  title: string,
  content: string,
  imgUrl: string,
  productId: string,
}

// 更新广告的信息
const updateAdvertisementInfo = reactive<UpdateAdvertisement>({
  id: 0,
  title: '',
  content: '',
  imgUrl: '',
  productId: '',
})

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

// 获取用户信息
const fetchUserInfo = async () => {
  try {
    getUserDetail(username).then((res) => {
      console.log("user detail:", res.data.data);
      userRole.value = res.data.data.role;
      userAvatar.value = res.data.data.avatar;
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

// 获取广告
const fetchAdvertisement = async () => {
  try {
    getAdvertisement().then((res) => {
      console.log("advertisement res:", res);
      console.log("advertisement res.data:", res.data);
      advertisementList.value = res.data.data;
    });
  } catch (error) {
    ElMessage.error('广告获取失败，请稍后再试！');
  }
};

// 计算购物车内商品总价
const sumPrice = computed(() => {
  const total = cartData.value.cartItemVOList.reduce((accumulator, currentItem) => {
    return accumulator + currentItem.price * currentItem.quantity;
  }, 0);

  // 更新购物车的 total 和 totalAmount
  cartData.value.total = total;
  cartData.value.totalAmount = cartData.value.cartItemVOList.length;

  return total;
});

// 搜索模块
const searchInput = ref('');
function clickSearch() {
  try {
    getSpecificProduct(Number(searchInput.value)).then((res) => {
      // 此处应该修改，先确定有没有该书籍
      if (res.data == null) {
        ElMessage.error('该书籍不存在！');
      } else {
        router.push('/book-detail/' + searchInput.value);
      }
      searchInput.value = '';
    })
  } catch (error) {
    ElMessage.error('该书籍不存在！');
  }
}

// 添加商品模块
function addBook() {
  ElMessage.success('添加商品按钮已点击！');
  // 处理specifications部分
  addSpecification('作者', author.value);
  addSpecification('副标题', subtitle.value);
  addSpecification('ISBN', ISBN.value);
  addSpecification('装帧', bindingAndLayout.value);
  addSpecification('页数', pages.value);
  addSpecification('出版社', publishingHouse.value);
  addSpecification('出版日期', publicationDate.value);
  try {
    addProduct(bookInfo).then((res) => {
      if (res.data.code === '200') {  //类型守卫，它检查 res.data 对象中是否存在名为 code 的属性
        ElMessage({
          message: "创建成功！",
          type: 'success',
          center: true,
        })
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
  centerDialogVisible2.value = false;
}

// 添加广告模块
function createAdvertisement() {
  ElMessage.success('添加广告按钮已点击！');
  try {
    addAdvertisement(createAdvertisementInfo).then(res => {
      if (res.data.code === '200') {  //类型守卫，它检查 res.data 对象中是否存在名为 code 的属性
        ElMessage({
          message: "创建成功！",
          type: 'success',
          center: true,
        })
      } else if (res.data.code === '400') {
        ElMessage({
          message: res.data.msg,
          type: 'error',
          center: true,
        })
      }
      centerDialogVisible3.value = false;
    })
  } catch (error) {
    ElMessage.error('添加广告失败！');
  }
  centerDialogVisible3.value = false;
}

// 删除广告模块
function deleteThisAdvertisement(id: number) {
  try {
    console.log("正在删除：", id);
    deleteAdvertisement(id).then((res) => {
      if (res.data.code === '200') {  //类型守卫，它检查 res.data 对象中是否存在名为 code 的属性
        ElMessage({
          message: "删除成功！",
          type: 'success',
          center: true,
        })
      } else if (res.data.code === '400') {
        ElMessage({
          message: res.data.msg,
          type: 'error',
          center: true,
        })
      }
    })
  } catch (error) {
    ElMessage.error('删除失败');
  }
}

// 点击广告以修改广告
function clickAdvertisement(id: number) {
  updateAdvertisementInfo.id = id;
  centerDialogVisible4.value = true;
  const formattedMessage = `正在修改广告位： ${id}`;
  ElMessage.success(formattedMessage);
}

// 点击确定修改广告
function updateAdvertisementClick() {
  ElMessage.success("确定已点击！")
  try {
    updateAdvertisement(updateAdvertisementInfo).then(res => {
      if (res.data.code === '200') {  //类型守卫，它检查 res.data 对象中是否存在名为 code 的属性
        ElMessage({
          message: "修改成功！",
          type: 'success',
          center: true,
        })
      } else if (res.data.code === '400') {
        ElMessage({
          message: res.data.msg,
          type: 'error',
          center: true,
        })
      }
    })
  } catch (error) {
    ElMessage.error("修改广告信息失败");
  }
  centerDialogVisible4.value = false;
}

// 点击头像进入更改个人信息
function clickAvatar() {
  // ElMessage.success('头像已点击');
  // 跳转到个人信息页面
  router.push('/dashboard');
}

// 查看个人信息
function clickMessage() {
  ElMessage.success('消息按钮已点击！');
}
// 点击商品下加号触发加入购物车
function clickBuy(id: number) {
  addBookToCart(id, 1).then((res) => {
    if (res.data.code === '200') {
      ElMessage.success('已加入购物车！');
      // 刷新页面
      location.reload();
    }else if (res.data.code === '405') {
      ElMessage.error('库存不足！');
    }else{
      ElMessage.error('加入购物车失败！');
    }
  })
}

// 调用支付
async function handleCheckout() {
  localStorage.setItem(CHECKOUT_INFO_KEY, JSON.stringify(checkoutInfo));
  if (checkoutInfo.shippingAddress.name.trim() === '') {
    ElMessage.error('请填写收货人！')
    return;
  }
  // 可加入其他收货信息验证
  try {
    // 1. 获取所有购物车项id
    checkoutInfo.cartItemIds = cartData.value.cartItemVOList.map(item => String(item.cartItemId));
    checkoutInfo.paymentMethod = 'ALIPAY';
    // 2. 创建订单
    const orderResp = await createOrder(checkoutInfo);
    if (orderResp.data && orderResp.data.code === "200" && orderResp.data.data) {
      const orderId = orderResp.data.data.orderId;
      centerDialogVisible1.value = false; // 关闭结算弹窗
      // 3. 弹出"支付"对话框
      payDialogVisible.value = true;
      payLoading.value = true;
      // 4. 拉取支付宝表单
      const payResp = await payOrder(orderId);
      payLoading.value = false;
      debugger
      if (payResp.data && payResp.data.code === "200" && payResp.data.data && payResp.data.data.paymentForm) {
        // 1. 替换form的id（防止有重名）
        payForm.value = payResp.data.data.paymentForm.replace(
            '<form name="punchout_form"',
            '<form id="alipaysubmit"'
        );
        // 2. 自动提交
        nextTick(() => {
          const form = document.getElementById('alipaysubmit') as HTMLFormElement | null;
          if (form) form.submit();
        });
      } else {
        payForm.value = '<p style="color:red">拉取支付失败！</p>';
      }
    } else {
      ElMessage.error(orderResp.data?.message || '订单创建失败!')
    }
  } catch (e) {
    payLoading.value = false;
    payForm.value = '<p style="color:red">下单或支付失败！</p>'
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
  if (rawFile.type !== 'image/jpeg') {
    ElMessage.error('头像必须是jpg/jpeg格式!');
    return false;
  } else if (rawFile.size / 1024 / 1024 > 2) {
    ElMessage.error('图片不应超过2MB!');
    return false;
  }
  customUpload(rawFile);
  return false;
};
function customUpload(file: File) {
  // 需要从后端传入工具类
  // uploadFile(file).then((res) => {
  //   handleUploadSuccess(res);
  // }).catch((err) => {
  //   handleUploadError(err);
  // })
}

// 在页面加载时获取所有书籍、广告和购物车
onMounted(() => {
  fetchUserInfo();
  fetchBookList();
  fetchCart();
  fetchAdvertisement();
})
// 点击进入订单列表
function goToOrderList() {
  router.push('/orders');
}
</script>

<template>
  <el-container>
    <el-aside width="300px" style="
    background-color: #565656;
    position: fixed;
    top: 0;
    left: 0;
    height: 100vh;
    z-index: 1100;
    box-shadow: 2px 0 8px rgba(0,0,0,0.15);
    overflow-y: auto;">
      <el-card style="width: 100%; height: 80px; background-color: #2e2d2e; border: 0px solid black;">
        <div class="card-header">
          <span class="title" style="color: white; font-weight: bold;font-size: x-large;margin-left: 10px;margin-top: 1px">番茄书城</span>
        </div>
      </el-card>
      <div style="margin-left: 10px;margin-right: 10px;margin-top: 20px">
        <div style="display:flex">
          <!--购物车模块-->
          <!-- <el-icon style="vertical-align: middle; height:25px; color: white">
            <shopping-cart />
          </el-icon> -->
          <div style="color: white; font-weight: bold; margin-left: 10px">🛒 购物车</div>
          <!-- 点击进入订单列表 -->
          <el-button link type="primary" size="large" style="margin-left: 100px;font-weight: bold; color: #ff9900" @click="goToOrderList">
            📒 订单管理
          </el-button>
        </div>
        <el-table :data="cartData.cartItemVOList" style="width: 100%; margin-top: 20px;" max-height="500" class="transparent-table">
          <el-table-column prop="title" label="名称" width="90" />
          <el-table-column prop="price" label="价格" width="70" />
          <el-table-column prop="quantity" label="数量" width="60" />
          <el-table-column prop="remove" label="操作" min-width="60">
            <template #default="scope">
              <el-button link type="warning" size="small" @click.prevent="deleteRow(scope.row.cartItemId)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <div style="margin-top: 20px; margin-left: 10px; color: white">合计: {{ sumPrice }}元</div>
        <el-button @click="centerDialogVisible1 = true;"
                   style="color: white; background-color: #909399; border-color: #909399; margin-top: 50px; margin-left: 75px; width: 100px">
          结账
        </el-button>
      </div>
    </el-aside>
    <el-container style="display: flex; flex-direction: column;margin-left: 300px;">
      <div class="page">
        <el-card>
          <!--卡片首部模块-->
          <template #header>
            <div class="card-header">
              <span class="title" style="font-weight: bold;font-size: large;margin-left: 10px;margin-top: 6px">主页</span>
              <el-input v-model="searchInput" style="max-width: 600px; margin-top: 2px; margin-left: 30%;" placeholder="请输入书籍ID">
                <template #append>
                  <el-button type="primary" class="search" @click="clickSearch">
                    <el-icon style="vertical-align: middle">
                      <Search />
                    </el-icon>
                  </el-button>
                </template>
              </el-input>
              <el-avatar :size="40" :src="userAvatar" style="margin-left: 50px" @click="clickAvatar" />
              <!--              <img  src="https://lab-blue-whale.oss-cn-nanjing.aliyuncs.com/VIP%E6%9C%AA%E7%82%B9%E4%BA%AE%E7%89%88.png" alt="图片描述"-->
              <!--                style="margin-top: 15px;margin-left: -15px;width: 25px;height: 25px">-->
              <el-button class="search" @click="clickMessage" style="margin-left: 20px; height: 40px; width: 40px; border-radius: 40%">
                <el-icon style="vertical-align: middle">
                  <BellFilled />
                </el-icon>
              </el-button>
            </div>
          </template>
          <!--卡片推荐模块-->
          <div style="display: flex">
            <div style="font-weight: bold; font-size: xx-large">今日推荐</div>
            <el-button v-if="userRole == 'admin'" @click="centerDialogVisible3 = true;" type="info" style="margin-left: 900px; margin-top: 8px">添加广告</el-button>
          </div>
          <div style="margin-top: 10px">
            <el-carousel :interval="4000" type="card" height="300px">
              <el-carousel-item v-for="item in advertisementList" :key="item.productId" @click="clickAdvertisement(item.id)">
                <div style="height: 100%; width: 100%; display: flex">
                  <div style="width: 50%">
                    <div class="advertisement-book-name" style="height: 35%; width: 90%; margin-top: 20px;margin-left: 20px">{{ item.title }}</div>
                    <el-text class="advertisement-book-content" style="margin-left: 20px; width: 80%; height: 115px">{{ item.content }}</el-text>
                    <div style="display: flex">
                      <el-button type="warning" disabled style="margin-top: 10px">BUY NOW!</el-button>
                      <el-button type="danger" :icon="Delete" circle style="margin-left: 150px;margin-top: 10px"
                                 @click.stop="deleteThisAdvertisement(item.id)" />
                    </div>
                  </div>
                  <div style="width: 50%">
                    <el-image :src="item.imgUrl" :alt="item.title" fit="cover" style="height: 80%; width: 70%; margin-left: 25px; margin-top: 25px">
                      <template #error>
                        <div class="image-error">图片加载失败</div>
                      </template>
                    </el-image>
                  </div>
                </div>
              </el-carousel-item>
            </el-carousel>
          </div>
          <div style="margin-top: 70px; height: 60px; display: flex">
            <div style="font-weight: bold; font-size: xx-large">商品列表</div>
            <el-button v-if="userRole == 'admin'" @click="centerDialogVisible2 = true;" type="info"
                       style="margin-left: 900px; margin-top: 15px">添加商品</el-button>
          </div>
        </el-card>
      </div>
      <!--      商品列表       -->
      <div style="width: 100%; height: 100%; margin-left: 0px">
        <el-tabs type="border-card" style="margin: 0 0 0 0">
          <el-tab-pane v-for="category in bookTypes" :key="category.value" :label="category.label">
            <div class="product-list">
              <el-row :gutter="20" style="margin-top: 20px">
                <el-col
                    v-for="book in bookList.filter(book => book.bookType === category.value)"
                    :key="book.id"
                    :xs="24"
                    :sm="12"
                    :md="6"
                    :lg="6"
                    :xl="6"
                >
                  <el-card @click="goToBookDetail(book.id)" class="book-card" shadow="hover" style="height: 360px;width: 250px; margin-top: 20px">
                    <template #header>
                      <div class="book-header">
                        <el-image class="book-image" :src="book.cover" :alt="book.title" fit="cover">
                          <template #error>
                            <div class="image-error">图片加载失败</div>
                          </template>
                        </el-image>
                      </div>
                    </template>
                    <div class="book-name">{{ book.title }}</div>
                    <el-rate v-model="book.rate" disabled show-score text-color="#ff9900" score-template="{value} points" />
                    <div style="display: flex">
                      <div class="book-price">￥{{ book.price }}</div>
                      <el-button class="buy-button" @click.stop="clickBuy(book.id)" style="margin-left: 20px; height: 40px; width: 40px; border-radius: 50%">
                        <el-icon style="vertical-align: middle">
                          <Plus />
                        </el-icon>
                      </el-button>
                    </div>
                  </el-card>
                </el-col>
              </el-row>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-container>
    <el-backtop :right="50" :bottom="50" style="color: #565656" />
  </el-container>

  <!--购物车提交弹出窗口-->
  <el-dialog v-model="centerDialogVisible1" title="结算信息" width="500" center>
    <el-form :model="checkoutInfo" label-width="80px">
      <el-form-item label="收货人">
        <el-input v-model="checkoutInfo.shippingAddress.name" />
      </el-form-item>
      <el-form-item label="手机号">
        <el-input v-model="checkoutInfo.shippingAddress.phone" />
      </el-form-item>
      <el-form-item label="邮编">
        <el-input v-model="checkoutInfo.shippingAddress.zipCode" />
      </el-form-item>
      <el-form-item label="详细地址">
        <el-input v-model="checkoutInfo.shippingAddress.detail" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="centerDialogVisible1 = false">取消</el-button>
      <el-button type="primary" @click="handleCheckout">提交订单并支付宝支付</el-button>
    </template>
  </el-dialog>

  <!--创建商品弹出窗口-->
  <el-dialog v-model="centerDialogVisible2" title="创建商品" width="1000" center>
    <span style="display: flex">
      <div>
        <el-main>
          <el-form :model="bookInfo" label-width="auto" style="margin-top: 10px">
            <div style="display: flex;">
              <el-form-item label="商品名称" style="width: 300px; display: flex">
                <el-input v-model="bookInfo.title" placeholder="请输入商品名称" />
              </el-form-item>
              <el-form-item label="副标题" style="width: 300px; display: flex">
                <el-input v-model="subtitle" placeholder="请输入副标题" />
              </el-form-item>
              <el-form-item label="作者" style="width: 300px; display: flex">
                <el-input v-model="author" placeholder="请输入作者名字" />
              </el-form-item>
            </div>
            <div style="display: flex; margin-top: 25px">
              <el-form-item label="商品类型">
                <el-select v-model="bookInfo.bookType" placeholder="请选择" style="width: 240px">
                  <el-option
                      v-for="item in bookTypes"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value"
                  />
                </el-select>
              </el-form-item>
            </div>
            <div style="display: flex; margin-top: 25px">
              <el-form-item label="商品价格" style="width: 300px;">
                <el-input v-model="bookInfo.price" placeholder="请输入商品价格（单位：元）" />
              </el-form-item>
              <el-form-item label="商品评分" style="width: 300px">
                <el-rate v-model="bookInfo.rate" allow-half />
              </el-form-item>
              <el-form-item label="ISBN" style="width: 300px">
                <el-input v-model="ISBN" placeholder="请输入书籍ISBN" />
              </el-form-item>
            </div>
            <el-form-item label="商品描述" style="width: 900px; margin-top: 25px">
              <el-input v-model="bookInfo.description" type="textarea" placeholder="请输入商品描述" :rows="2" />
            </el-form-item>
            <div style="display: flex; margin-top: 25px">
              <el-form-item label="商品图片">
                <el-upload class="avatar-uploader" :show-file-list="false" :before-upload="beforeImageUpload" :limit="1">
                  <img v-if="bookInfo.cover" :src="bookInfo.cover" class="avatar" />
                  <el-icon v-else class="avatar-uploader-icon">
                    <Plus />
                  </el-icon>
                </el-upload>
              </el-form-item>
              <div>
                <div style="display: flex; margin-top: 15px">
                  <el-form-item label="装帧" style="width: 250px;">
                    <el-input v-model="bindingAndLayout" placeholder="装帧" />
                  </el-form-item>
                  <el-form-item label="出版社" style="width: 400px;">
                    <el-input v-model="publishingHouse" placeholder="出版社" />
                  </el-form-item>
                </div>
                <div style="display: flex; margin-top: 15px">
                  <el-form-item label="页数" style="width: 250px;">
                    <el-input v-model="pages" placeholder="页数" />
                  </el-form-item>
                  <el-form-item label="出版日期">
                    <el-date-picker v-model="publicationDate" type="date" placeholder="选择日期" :size="'default'" />
                  </el-form-item>
                </div>
              </div>
            </div>
            <el-form-item label="商品详细说明" style="width: 900px; margin-top: 25px">
              <el-input v-model="bookInfo.detail" type="textarea" placeholder="请输入商品详细说明" :rows="5" />
            </el-form-item>
          </el-form>
        </el-main>
      </div>
    </span>
    <template #footer>
      <div class="dialog-footer" style="width: 1000px;">
        <el-button @click="addBook" type="primary">
          确定
        </el-button>
        <el-button @click="centerDialogVisible2 = false">取消</el-button>
      </div>
    </template>
  </el-dialog>

  <!--创建广告弹出窗口-->
  <el-dialog v-model="centerDialogVisible3" title="创建广告" width="800" center>
    <span style="display: flex">
      <div>
        <el-main>
          <el-form :model="createAdvertisementInfo" label-width="auto" style="margin-top: 10px">
            <el-form-item label="广告标题" style="width: 300px; display: flex">
              <el-input v-model="createAdvertisementInfo.title" placeholder="请输入广告标题" />
            </el-form-item>
            <el-form-item label="广告图片">
              <el-upload class="avatar-uploader" :show-file-list="false" :before-upload="beforeImageUpload" :limit="1">
                <img v-if="createAdvertisementInfo.imgUrl" :src="createAdvertisementInfo.imgUrl" class="avatar" />
                <el-icon v-else class="avatar-uploader-icon">
                  <Plus />
                </el-icon>
              </el-upload>
            </el-form-item>
            <el-form-item label="广告内容" style="width: 700px; margin-top: 25px">
              <el-input v-model="createAdvertisementInfo.content" type="textarea" placeholder="请输入广告内容" :rows="3" />
            </el-form-item>
            <el-form-item label="商品Id" style="width: 300px; display: flex">
              <el-input v-model="createAdvertisementInfo.productId" placeholder="请输入商品Id" />
            </el-form-item>
          </el-form>
        </el-main>
      </div>
    </span>
    <template #footer>
      <div class="dialog-footer" style="width: 800px;">
        <el-button @click="createAdvertisement" type="primary">
          确定
        </el-button>
        <el-button @click="centerDialogVisible3 = false">取消</el-button>
      </div>
    </template>
  </el-dialog>

  <!--更新广告弹出窗口-->
  <el-dialog v-model="centerDialogVisible4" title="更新广告" width="800" center>
    <span style="display: flex">
      <div>
        <el-main>
          <el-form :model="updateAdvertisementInfo" label-width="auto" style="margin-top: 10px">
            <el-form-item label="广告标题" style="width: 300px; display: flex">
              <el-input v-model="updateAdvertisementInfo.title" placeholder="请输入广告标题" />
            </el-form-item>
            <el-form-item label="广告图片">
              <el-upload class="avatar-uploader" :show-file-list="false" :before-upload="beforeImageUpload" :limit="1">
                <img v-if="updateAdvertisementInfo.imgUrl" :src="createAdvertisementInfo.imgUrl" class="avatar" />
                <el-icon v-else class="avatar-uploader-icon">
                  <Plus />
                </el-icon>
              </el-upload>
            </el-form-item>
            <el-form-item label="广告内容" style="width: 700px; margin-top: 25px">
              <el-input v-model="updateAdvertisementInfo.content" type="textarea" placeholder="请输入广告内容" :rows="3" />
            </el-form-item>
            <el-form-item label="商品Id" style="width: 300px; display: flex">
              <el-input v-model="updateAdvertisementInfo.productId" placeholder="请输入商品Id" />
            </el-form-item>
          </el-form>
        </el-main>
      </div>
    </span>
    <template #footer>
      <div class="dialog-footer" style="width: 800px;">
        <el-button @click="updateAdvertisementClick" type="primary">
          确定
        </el-button>
        <el-button @click="centerDialogVisible4 = false">取消</el-button>
      </div>
    </template>
  </el-dialog>
  <el-dialog v-model="payDialogVisible" title="支付宝沙箱支付" width="430px" :close-on-click-modal="false">
    <div v-if="payLoading" style="text-align:center">
      <el-icon>
        <Loading />
      </el-icon> 正在跳转支付宝，请耐心等待...
    </div>
    <div v-else>
      <div v-html="payForm"></div>
      <p v-if="!payForm">如未成功跳转，请检查浏览器弹窗拦截或重试</p>
    </div>
  </el-dialog>
</template>

<style scoped>
.page {
  /* height: 1000px; */
  width: 100%;
}

.card-header {
  height: 40px;
  display: flex;
}

.el-carousel__item h3 {
  color: #475669;
  opacity: 0.75;
  line-height: 250px;
  margin: 0;
  text-align: center;
}

.el-carousel__item:nth-child(2n) {
  background-color: #565656;
}

.el-carousel__item:nth-child(2n + 1) {
  background-color: #dddddd;
}

/* 激活标签项颜色 */
::v-deep(.el-tabs__item.is-active) {
  color: #373229 !important;
}

/* 激活项 hover 效果 */
::v-deep(.el-tabs__item.is-active:hover) {
  color: #f2b405 !important;
}

/* 非激活标签项 hover 效果 */
::v-deep(.el-tabs__item:not(.is-active):hover) {
  color: #ea9805 !important;
}

.book-card {
  transition: transform 0.3s ease;
}

.book-card:hover {
  transform: translateY(-5px);
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
}

.book-name {
  font-weight: bold;
  font-size: 18px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.advertisement-book-name {
  font-weight: bold;
  font-size: xx-large;
  word-break: break-word;
  /* 允许长单词换行 */
  overflow: hidden;
  /* 溢出部分隐藏 */
  text-overflow: ellipsis;
  /* 溢出部分显示省略号 */
  display: -webkit-box;
  /* 使用 Webkit 内核的 box 布局 */
  -webkit-box-orient: vertical;
  /* 垂直排列 */
  -webkit-line-clamp: 2;
  /* 限制显示 2 行 */
  line-height: 1.5;
  /* 设置行高，确保文字不重叠 */
}

.advertisement-book-content {
  word-break: break-word;
  /* 允许长单词换行 */
  overflow: hidden;
  /* 溢出部分隐藏 */
  text-overflow: ellipsis;
  /* 溢出部分显示省略号 */
  display: -webkit-box;
  /* 使用 Webkit 内核的 box 布局 */
  -webkit-box-orient: vertical;
  /* 垂直排列 */
  -webkit-line-clamp: 5;
  /* 限制显示 5 行 */
  line-height: 1.5;
  /* 设置行高，确保文字不重叠 */
}

.book-price {
  width: 150px;
  margin-top: 5px;
  font-size: 16px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.transparent-table {
  --el-table-bg-color: rgba(0, 0, 0, 0);
  /* 表格背景色透明 */
  --el-table-border-color: rgba(0, 0, 0, 0.2);
  /* 表格边框颜色半透明 */
  --el-table-header-bg-color: rgba(0, 0, 0, 0);
  /* 表头背景色透明 */
  --el-table-header-font-color: #ffffff;
  /* 表头文字颜色 */
  --el-table-text-color: #ffffff;
  /* 表格文字颜色 */
  --el-table-tr-bg-color: rgba(0, 0, 0, 0);
  /* 表格行背景色透明 */
  --el-table-current-row-bg-color: rgba(0, 0, 0, 0.2);
  /* 当前行背景色半透明 */
  --el-table-hover-row-bg-color: rgba(0, 0, 0, 0.1);
  /* 悬停行背景色半透明 */
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