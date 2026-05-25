<script setup lang="ts">
import { ref, onMounted, reactive, computed, watch } from 'vue';
import { ElMessage, UploadProps } from 'element-plus';
import { Back, Service, Delete, Plus, Comment, Star, ShoppingCart, Promotion, MessageBox } from '@element-plus/icons-vue'
import { deleteProduct, getSpecificProduct, getStockpile, updateProduct } from "@/api/product";
import { useRoute, useRouter } from "vue-router";
import { addBookToCart } from "@/api/cart";
import {getComments, postComments} from "@/api/comment";

const route = useRoute();
const myUsername = sessionStorage.getItem("username");

// 获得书籍Id号
const bookId = Number(route.params.bookId);
const router = useRouter();

// 进入页面触发获取商品信息和库存数量
onMounted(async () => {
  console.log("userName: " + myUsername);
  fetchBookData(bookId);
  if (bookData.value) {
    console.log("bookId:", bookData.value);
  }
  fetchAmount(bookId);
  fetchComments(bookId);
})

// 售后框是否出现
const centerDialogVisible = ref(false)
// 修改信息框是否出现
const centerDialogVisible2 = ref(false)
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

// 售后框用户书写内容
const afterSaleComment = ref('');

// 指定书本内容
interface ProductVO {
  id: number,
  title: string,
  price: number,
  rate: number,
  description: string,
  cover: string,
  detail: string,
  discountNumber: number,
  bookType: string,
  soldQuantity: number,
  specifications: Specifications[];
  stockpile: Stockpile;
  recommendCount: number;
  comments: Comment[];
}

interface Specifications {
  id: number | null;
  item: string,
  value: string,
  product_id: number | null;
}

interface Comment {
  id: number;
  productId: number;
  userId: number;
  username: string;
  avatar: string;
  content: string;
  rating: number;
  createdAt: string | null;
  updatedAt: string | null;
}

const comments = ref<Comment[]>([]);

interface Stockpile {
  id: number | null;
  product_id: number | null;
  amount: number;
  frozen: number;
}

const bookData = ref<ProductVO>();
// 剩余库存
const amount = ref(0);

// 购买数量
const buyNum = ref(0);

interface PostCommentInfo{
  productId: number,
  username: string | null,
  content: string,
  rating: number
}

const postComment = reactive<PostCommentInfo>({
  productId: bookId,
  username: myUsername,
  content: "",
  rating: 0,
})


const commentList = ref([

]);

// 修改数据
interface UpdateInfo {
  id: number;
  title: string,
  price: number,
  rate: number,
  description: string,
  cover: string,
  detail: string,
  discountNumber: number,
  bookType: string,
  recommendCount: number,
  specifications: Specifications[];
  stockpile: Stockpile;
}

const bookInfo = reactive<UpdateInfo>({
  id: 0,
  title: '',
  price: 0,
  rate: 0,
  description: '',
  cover: '',
  detail: '',
  discountNumber: 1.0,
  bookType: '',
  specifications: [],
  recommendCount: 0,
  stockpile: { id: 0, product_id: 0, amount: 0, frozen: 0 },
});
// 使用 watch 来监听 bookData 的变化
watch(bookData, (newValue) => {
  if (newValue) {
    bookInfo.id = newValue.id;
    bookInfo.title = newValue.title;
    bookInfo.price = newValue.price;
    bookInfo.rate = newValue.rate;
    bookInfo.description = newValue.description;
    bookInfo.cover = newValue.cover;
    bookInfo.detail = newValue.detail;
    bookInfo.discountNumber = newValue.discountNumber;
    bookInfo.bookType = newValue.bookType;
    bookInfo.specifications = newValue.specifications;
    bookInfo.recommendCount = newValue.recommendCount;
    bookInfo.stockpile = newValue.stockpile;
  }
}, { immediate: true }); // 立即执行一次，确保初始值正确

// 获得商品信息
function fetchBookData(bookId: number) {
  getSpecificProduct(bookId)
      .then((res) => {
        console.log("res.data:", res.data);
        bookData.value = res.data;
        console.log("bookData!", bookData);
      })
      .catch((error) => {
        console.error("书籍获取失败！", error);
      });
}
// 获得库存
function fetchAmount(bookId: number) {
  try {
    getStockpile(bookId).then((res) => {
      amount.value = res.data;
    });
  } catch (error) {
    ElMessage.error('库存获取失败，请稍后再试！');
  }
}
//获得评论
function fetchComments(bookId: number) {
  try{
    getComments(bookId).then((res) => {
      console.log("res:", res);
      comments.value = res.data.data;
      console.log("res.data.data: ", res.data.data);
      console.log("comment.data:", comments);
    })
  } catch (error) {
    console.log(error);
  }
}
// 返回主页
function returnBack() {
  router.push('/store-detail');
}
// 购物数量数值修改
const handleChange = (value: number | undefined) => {
  console.log(value)
}

function submitChange() {
  try {
    updateProduct(bookInfo).then((res) => {
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
    centerDialogVisible2.value = false;
  } catch (error) {
    ElMessage.error('修改商品信息失败！')
  }
}

function addToCart() {
  // 此处修改购物车商品数量
  try {
    addBookToCart(bookId, buyNum.value).then((res) => {
      if (res.data.code === '200') {  //类型守卫，它检查 res.data 对象中是否存在名为 code 的属性
        ElMessage({
          message: "加入购物车成功！",
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
    centerDialogVisible2.value = false;
  } catch (error) {
    ElMessage.error('加入购物车失败！');
  }
}

function afterSale() {
  ElMessage.success('售后按钮已点击！');
  centerDialogVisible.value = true;
}

function sendAfterSale() {
  ElMessage.success('提交售后请求成功！');
  centerDialogVisible.value = false;
}

function deleteBook() {
  try {
    deleteProduct(bookId).then((res) => {
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
    ElMessage.error('删除失败！')
  }
  router.push('/store-detail');
  ElMessage.success('删除按钮已点击！');
}

function sendMessage() {
  postComments(postComment).then((res) => {
    if (res.data.code === '200') {
      ElMessage.success("发送成功！");
      postComment.content = "";
      postComment.rating = 0;
    }else{
      ElMessage.error("发送失败！");
    }
  })
}

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

const displayRate = computed({
  get() {
    return bookData.value?.rate || 0;
  },
  set(value) {
    if (bookData.value) {
      bookData.value.rate = value;
    }
  }
});

// 处理specifications的增减
const deleteRow = (index: number) => {
  bookInfo.specifications.splice(index, 1)
}

const newItem = ref('');
const newValue = ref('');
const onAddItem = () => {
  bookInfo.specifications.push({
    id: null,
    item: newItem.value,
    value: newValue.value,
    product_id: bookId,
  })
  newItem.value = '';
  newValue.value = '';
  ElMessage.success("添加成功！");
}
</script>

<template>
  <el-container>
    <el-header class="bar-header">
      <div style="display: flex">
        <div style="color: black; font-weight: bold; font-size: x-large; margin-left: 10px; margin-top: 10px">番茄商店</div>
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
        <div style="margin-top: 20px" @click="afterSale">售后</div>
        <el-icon style="margin-left: 30px; margin-top: 25px">
          <MessageBox />
        </el-icon>
        <div style="margin-top: 20px" @click="">消息</div>
        <el-button class="submit-change" type="warning" @click="centerDialogVisible2 = true" size="default"
                   style="margin-left: 60px; margin-top: 15px; font-size: small">更改商品信息</el-button>
      </div>
    </el-header>
    <el-container style="height: 700px">
      <el-aside style="height: 100%; width: 700px">
        <div @click="returnBack" style="margin-top: 40px;margin-left: 40px">
          <el-icon style="vertical-align: middle; font-size: xx-large">
            <Back />
          </el-icon>
        </div>
        <div style="display: flex; margin-top: 25px;margin-left: 45px">
          <div style="color:#b8b7b7;">商品</div>
          <div style="margin-left: 8px">/ {{ bookData?.title || '' }}</div>
        </div>
        <div style="margin-top: 80px; margin-left: 45px; font-size: xx-large; font-weight: bolder">{{ bookData?.title || '' }}</div>
        <div style="
          display: flex;
          width: 80%;
          margin-left: 45px;
          margin-top: 20px;
          justify-content: space-between;
          align-items: center;
        ">
          <div v-if="bookInfo.discountNumber >= 1" style="font-size: large; font-weight: bold;color: #ffa502;">￥{{ (bookData?.price || 0) }}</div>
          <div style="display: flex; align-items: center;">
            <div v-if="bookInfo.discountNumber < 1" style="font-size: large; font-weight: bold;color: #ffa502;text-decoration: line-through">￥{{ ((bookData?.price || 0)/bookInfo.discountNumber).toFixed(2) }}</div>
            <div v-if="bookInfo.discountNumber < 1" style="font-size: large; font-weight: bold;color: #ffa502">￥{{ (bookData?.price || 0) }}</div>
          </div>
          <!-- <el-rate v-model="displayRate" disabled show-score text-color="#ff9900" score-template="{value} points" /> -->
          <el-text style="color:#ff6b81;">商品评分: {{ bookData?.rate }}</el-text>
          <el-text style="color:#2ed573;">剩余库存: {{ bookData?.stockpile.amount }}</el-text>
          <el-text style="color:#b8b7b7;">已售数量: {{ bookData?.soldQuantity }}</el-text>
        </div>
        <div style="height: 100px; margin-top: 50px; width: 90%; margin-left: 45px; display: flex; align-items: center;">
          <el-text class="description">{{ bookData?.detail }}</el-text>
        </div>
        <div style="display:flex; margin-left: 45px; margin-top: 60px">
          <el-input-number v-model="buyNum" :min="1" :max="10000" @change="handleChange" />
          <el-button class="add-to-cart" @click="addToCart" size="large"
                     style="margin-left: 50px; background-color: #2ed573; color: white; font-size: large">加入购物车</el-button>
          <el-button type="danger" size="large" :icon="Delete" circle style="margin-left: 50px" @click="deleteBook" />
        </div>
        <div style="display: flex; margin-top: 30px; margin-left: 45px">
          <el-text style="margin-left: 15px; margin-top: 30px" v-for="spec in bookData?.specifications?.slice(0, 6)" :key="spec.id">
            <strong>{{ spec.item }}:</strong><br> {{ spec.value }}
          </el-text>
        </div>
      </el-aside>
      <el-main>
        <img :style="{ opacity: (bookInfo.discountNumber<1.0) ? 1 : 0 }" src="https://lab-blue-whale.oss-cn-nanjing.aliyuncs.com/%E6%89%93%E6%8A%98%E4%BC%98%E6%83%A0.png" alt="图片描述" style=" margin-top:10px;margin-left: 90px;width: 110px;height: 100px;z-index: 999">
        <el-image
            style="width: 550px; height: 600px; margin-left: 90px; margin-top: -90px; z-index: -1"
            :src="bookData?.cover"
            :fit="'contain'"
        />
        <img v-if="bookInfo.discountNumber<1.0" src="https://lab-blue-whale.oss-cn-nanjing.aliyuncs.com/%E7%88%86%E7%82%B8%E6%B0%94%E6%B3%A1.png" alt="图片描述" style=" margin-top:-300px;margin-left: 400px;width: 300px;height: 300px;z-index: 999">
        <div v-if="bookInfo.discountNumber<1.0" style="margin-top: -195px; margin-left: 490px;font-size: xxx-large;font-weight: bolder">{{bookInfo.discountNumber*100}}折</div>
      </el-main>
    </el-container>
    <!--商品评价-->
    <el-container style="height: 200px;display: flex; flex-direction: column">
      <div style="display: flex; height: 70px">
        <div style="color: black; font-weight: bold; font-size: xx-large; margin-left: 40px; margin-top: 10px">商品评价</div>
        <div style="font-size: x-large; margin-left: 10px"><el-icon>
          <Comment />
        </el-icon></div>
        <div style="color: black; font-size: large; margin-left: 900px; margin-top: 20px">推荐商品人数：{{ bookInfo.recommendCount }}</div>
      </div>
      <!--评价-->
      <div style="width: 96%;margin-left: 2%;margin-top: 10px">
        <div style="display: flex;height: 50px; margin-top: 10px">
          <el-avatar :size="50" :src="avatarUrl" />
          <el-input v-model="postComment.content" style="width: 80%; height: 40px;margin-top: 5px;margin-left: 8px" placeholder="说点什么吧~" clearable />
          <el-rate size="large" style="width: 10%;margin-top: 5px;margin-left: 10px" v-model=postComment.rating />
          <el-button class="send-comment" @click="sendMessage"
                     style="margin-left: 5px;margin-top: 5px;height: 40px;width: 60px;border-radius: 10px;background-color: #565656;color: white;font-size: large"><el-icon>
            <Promotion />
          </el-icon></el-button>
        </div>
        <el-card class="comment-card" v-for="(comment, id) in comments" :key="id" style="margin-top: 30px; height: 200px;border-radius: 20px">
          <div style="height: 30px;display: flex;">
            <div class="username" style="font-weight: bolder;width: 85%">{{ comment.username }} :</div>
            <el-rate
                v-model="comment.rating"
                disabled
                show-score
                text-color="#ff9900"
                score-template="{value} 星"
            />
          </div>
          <div class="comment-content" style="height: 105px">{{ comment.content }}</div>
          <div class="comment-time" style="font-size: small;color: #b8b7b7">{{ comment.createdAt }}</div>
        </el-card>
      </div>
    </el-container>
    <el-backtop :right="50" :bottom="50" style="color: #565656" />
  </el-container>

  <!--售后弹窗部分-->
  <el-dialog v-model="centerDialogVisible" title="请写售后原因" width="800" align-center>
    <el-input v-model="afterSaleComment" style="width: 750px" :rows="12" type="textarea" placeholder="Please input" />
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="centerDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="sendAfterSale">
          提交
        </el-button>
      </div>
    </template>
  </el-dialog>

  <!--修改信息弹出窗口-->
  <el-dialog v-model="centerDialogVisible2" title="修改商品信息" width="1000" center>
    <span>
      <el-main>
        <el-form :model="bookInfo" label-width="auto" style="margin-top: 10px">
          <div style="display: flex;">
            <el-form-item label="商品名称" style="width: 300px; display: flex">
              <el-input v-model="bookInfo.title" placeholder="请输入商品名称" />
            </el-form-item>
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
            <el-form-item label="打折（0<x<=1）" style="width: 180px; display: flex; margin-left: 40px">
              <el-input v-model="bookInfo.discountNumber" placeholder="请输入商品打几折" />
            </el-form-item>
          </div>
          <div style="display: flex; margin-top: 25px">
            <el-form-item label="商品价格" style="width: 300px;">
              <el-input v-model="bookInfo.price" placeholder="请输入商品价格（单位：元）" />
            </el-form-item>
            <el-form-item label="商品评分" style="width: 300px">
              <el-rate v-model="bookInfo.rate" allow-half />
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
          </div>
          <el-form-item label="基本信息">
            <el-table :data="bookInfo.specifications" style="width: 100%" max-height="200">
              <el-table-column prop="item" label="Item" width="250" />
              <el-table-column prop="value" label="Value" width="250" />
              <el-table-column fixed="right" label="Operations" min-width="50">
                <template #default="scope">
                  <el-button link type="primary" size="small" @click.prevent="deleteRow(scope.$index)">
                    Remove
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
            <div style="display: flex">
              <el-input v-model="newItem" style="width: 250px" placeholder="请输入新的Item" />
              <el-input v-model="newValue" style="width: 250px;margin-left: 10px" placeholder="请输入新的Value" />
              <el-button class="mt-4" style="width: 200px;margin-left: 10px" @click="onAddItem">
                Add Item
              </el-button>
            </div>
          </el-form-item>
          <el-form-item label="商品详细说明" style="width: 900px; margin-top: 25px">
            <el-input v-model="bookInfo.detail" type="textarea" placeholder="请输入商品详细说明" :rows="4" />
          </el-form-item>
          <el-form-item label="库存" style="width: 300px">
            <el-input v-model="bookInfo.stockpile.amount" placeholder="请输入库存" />
          </el-form-item>
          <el-form-item label="推荐人数" style="width: 300px">
            <el-input v-model="bookInfo.recommendCount" placeholder="请输入推荐人数" />
          </el-form-item>
        </el-form>
      </el-main>
    </span>
    <template #footer>
      <div class="dialog-footer" style="width: 1000px;">
        <el-button @click="submitChange" type="primary">
          确定
        </el-button>
        <el-button @click="centerDialogVisible2 = false">取消</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped>
.bar-header {
  border-bottom: 1px solid #ddd;
}

.description {
  font-size: 18px;
  overflow: hidden;
  text-overflow: ellipsis;
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

.send-comment:hover {
  transform: translateY(-5px);
}
</style>