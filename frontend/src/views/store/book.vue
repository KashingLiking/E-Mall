<script setup lang="ts">
import { ref, onMounted, reactive, watch } from 'vue';
import { ElMessage, UploadProps, type UploadInstance } from 'element-plus';
import { Back, Delete, Plus } from '@element-plus/icons-vue'
import AppNav from '@/components/AppNav.vue'
import { deleteProduct, getSpecificProduct, updateProduct } from "@/api/product";
import { useRoute, useRouter } from "vue-router";
import { addBookToCart } from "@/api/cart";
import { uploadImage } from '@/api/upload'
import { resolveImageUrl } from '@/utils/image'

const route = useRoute();
const router = useRouter();
const bookId = Number(route.params.bookId);
const userRole = ref(sessionStorage.getItem('userRole') || 'user');
const coverUploadRef = ref<UploadInstance>();
const coverUploadKey = ref(0);

const centerDialogVisible2 = ref(false);

const bookTypes = [
  { value: "Romance", label: "言情" },
  { value: "Science", label: "科普" },
  { value: "Magical", label: "奇幻" },
  { value: "Fairy", label: "童话" },
  { value: "Sports", label: "体育" },
  { value: "Philosophy", label: "哲学" },
];

interface ProductVO {
  id: number,
  title: string,
  price: number,
  rate: number,
  description: string,
  cover: string,
  detail: string,
  bookType: string,
  soldQuantity: number,
  specifications: Specifications[];
  stockpile: Stockpile;
}

interface Specifications {
  id: number | null;
  item: string,
  value: string,
  product_id: number | null;
}

interface Stockpile {
  id: number | null;
  product_id: number | null;
  amount: number;
  frozen: number;
}

const bookData = ref<ProductVO>();
const buyNum = ref(1);

interface UpdateInfo {
  id: number;
  title: string,
  price: number,
  rate: number,
  description: string,
  cover: string,
  detail: string,
  bookType: string,
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
  bookType: '',
  specifications: [],
  stockpile: { id: 0, product_id: 0, amount: 0, frozen: 0 },
});

watch(bookData, (newValue) => {
  if (newValue) {
    bookInfo.id = newValue.id;
    bookInfo.title = newValue.title;
    bookInfo.price = newValue.price;
    bookInfo.rate = newValue.rate;
    bookInfo.description = newValue.description;
    bookInfo.cover = newValue.cover;
    bookInfo.detail = newValue.detail;
    bookInfo.bookType = newValue.bookType;
    bookInfo.specifications = newValue.specifications;
    bookInfo.stockpile = newValue.stockpile;
    loadSpecFields(newValue.specifications);
  }
}, { immediate: true });

onMounted(() => {
  fetchBookData(bookId);
});

function fetchBookData(id: number) {
  getSpecificProduct(id)
    .then((res) => {
      bookData.value = res.data;
    })
    .catch(() => {
      ElMessage.error('商品获取失败');
    });
}

function returnBack() {
  router.push('/store-detail');
}

function submitChange() {
  // 把独立字段转成 specifications，过滤空值
  const existingSpecs = bookInfo.specifications.filter(
    s => !SPEC_FIELDS.some(f => f.key === s.item)
  );
  const specFields = SPEC_FIELDS
    .filter(f => (f.ref as any).value.trim())
    .map(f => ({ id: null, item: f.key, value: (f.ref as any).value.trim(), product_id: bookId }));
  bookInfo.specifications = [...existingSpecs, ...specFields];

  updateProduct(bookInfo).then((res) => {
    if (res.data.code === '200') {
      ElMessage.success('修改成功！');
      fetchBookData(bookId);
    } else {
      ElMessage.error(res.data.msg || '修改失败');
    }
    centerDialogVisible2.value = false;
  }).catch(() => {
    ElMessage.error('修改商品信息失败！');
  });
}

function addToCart() {
  addBookToCart(bookId, buyNum.value).then((res) => {
    if (res.data.code === '200') {
      ElMessage.success('加入购物车成功！');
    } else {
      ElMessage.error(res.data.msg || '加入购物车失败');
    }
  }).catch(() => {
    ElMessage.error('加入购物车失败！');
  });
}

function deleteBook() {
  deleteProduct(bookId).then((res) => {
    if (res.data.code === '200') {
      ElMessage.success('删除成功！');
      router.push('/store-detail');
    } else {
      ElMessage.error(res.data.msg || '删除失败');
    }
  }).catch(() => {
    ElMessage.error('删除失败！');
  });
}

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
  uploadImage(rawFile).then((res: any) => {
    const path = res?.data?.data;
    if (res?.data?.code === '200' && path) {
      bookInfo.cover = path;
      // 清掉 el-upload 内部已选文件，避免 limit/缓存导致要再选一次
      coverUploadRef.value?.clearFiles();
      coverUploadKey.value += 1;
      ElMessage.success('封面上传成功，请点击「确定」保存');
    } else {
      ElMessage.error(res?.data?.msg || '上传失败');
    }
  }).catch(() => {
    ElMessage.error('上传失败，请确认已登录且后端已启动');
  });
  return false;
};

const deleteRow = (index: number) => {
  bookInfo.specifications.splice(index, 1);
};

const newItem = ref('');
const newValue = ref('');
const onAddItem = () => {
  if (!newItem.value.trim() || !newValue.value.trim()) {
    ElMessage.warning('请填写规格名称和值');
    return;
  }
  bookInfo.specifications.push({
    id: null,
    item: newItem.value,
    value: newValue.value,
    product_id: bookId,
  });
  newItem.value = '';
  newValue.value = '';
};

// 独立规格字段，从 specifications 中读取初始值
const editAuthor = ref('');
const editISBN = ref('');
const editBinding = ref('');
const editPages = ref('');
const editPublisher = ref('');
const editPubDate = ref('');

const SPEC_FIELDS: { key: string; ref: ReturnType<typeof ref<string>> }[] = [
  { key: '作者', ref: editAuthor },
  { key: 'ISBN', ref: editISBN },
  { key: '装帧', ref: editBinding },
  { key: '页数', ref: editPages },
  { key: '出版社', ref: editPublisher },
  { key: '出版日期', ref: editPubDate },
];

function loadSpecFields(specs: Specifications[]) {
  SPEC_FIELDS.forEach(f => {
    const found = specs?.find(s => s.item === f.key);
    (f.ref as any).value = found ? found.value : '';
  });
}
</script>

<template>
  <el-container direction="vertical">
    <AppNav />
    <div class="book-detail-page">
      <div class="book-detail-back" @click="returnBack">
        <el-icon><Back /></el-icon>
        <span>返回商城</span>
      </div>
      <div class="book-breadcrumb">
        <span class="muted">商品</span>
        <span>/ {{ bookData?.title || '' }}</span>
      </div>
      <div class="book-detail-layout">
        <div class="book-detail-left">
          <h1 class="book-title">{{ bookData?.title || '' }}</h1>
          <div class="book-meta-row">
            <div class="book-price-display">￥{{ bookData?.price ?? 0 }}</div>
            <el-text style="color:#ff6b81;">商品评分: {{ bookData?.rate ?? 0 }}</el-text>
            <el-text style="color:#2ed573;">剩余库存: {{ bookData?.stockpile?.amount ?? 0 }}</el-text>
            <el-text style="color:#b8b7b7;">已售数量: {{ bookData?.soldQuantity ?? 0 }}</el-text>
          </div>
          <div class="book-desc">
            <el-text class="description">{{ bookData?.detail }}</el-text>
          </div>
          <div class="book-actions">
            <el-input-number v-model="buyNum" :min="1" :max="10000" />
            <el-button class="add-to-cart" type="success" @click="addToCart" size="large" round>加入购物车</el-button>
            <el-button
              v-if="userRole === 'admin'"
              type="warning"
              size="large"
              round
              @click="centerDialogVisible2 = true"
            >
              修改商品
            </el-button>
            <el-button
              v-if="userRole === 'admin'"
              type="danger"
              size="large"
              :icon="Delete"
              circle
              @click="deleteBook"
            />
          </div>
          <div class="book-specs">
            <el-text class="spec-item" v-for="spec in bookData?.specifications?.slice(0, 6)" :key="spec.id ?? spec.item">
              <strong>{{ spec.item }}:</strong><br> {{ spec.value }}
            </el-text>
          </div>
        </div>
        <div class="book-detail-right">
          <el-image class="book-cover" :src="resolveImageUrl(bookData?.cover)" fit="contain" />
        </div>
      </div>
    </div>
    <el-backtop :right="50" :bottom="50" />
  </el-container>

  <el-dialog v-model="centerDialogVisible2" title="修改商品信息" width="560" center>
    <el-form :model="bookInfo" label-width="80px" label-position="top">
      <el-form-item label="书名" required>
        <el-input v-model="bookInfo.title" placeholder="请输入书名" />
      </el-form-item>
      <el-form-item label="分类" required>
        <el-select v-model="bookInfo.bookType" placeholder="请选择分类" style="width: 100%">
          <el-option v-for="item in bookTypes" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="价格" required>
        <el-input v-model="bookInfo.price" placeholder="单位：元" type="number" />
      </el-form-item>
      <el-collapse>
        <el-collapse-item title="更多信息（选填）" name="more">
          <el-form-item label="作者">
            <el-input v-model="editAuthor" placeholder="选填" />
          </el-form-item>
          <el-form-item label="评分">
            <el-rate v-model="bookInfo.rate" allow-half />
          </el-form-item>
          <el-form-item label="ISBN">
            <el-input v-model="editISBN" placeholder="选填" />
          </el-form-item>
          <el-form-item label="封面">
            <el-upload
              :key="coverUploadKey"
              ref="coverUploadRef"
              class="avatar-uploader"
              :show-file-list="false"
              :before-upload="beforeImageUpload"
              accept="image/jpeg,image/png,image/webp"
            >
              <img v-if="bookInfo.cover" :src="resolveImageUrl(bookInfo.cover)" class="avatar" />
              <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
            </el-upload>
            <div class="upload-tip">点击图片可更换封面，选好后还需点下方「确定」才会保存</div>
          </el-form-item>
          <el-form-item label="详细说明">
            <el-input v-model="bookInfo.detail" type="textarea" :rows="3" placeholder="选填" />
          </el-form-item>
          <el-form-item label="装帧">
            <el-input v-model="editBinding" placeholder="选填" />
          </el-form-item>
          <el-form-item label="出版社">
            <el-input v-model="editPublisher" placeholder="选填" />
          </el-form-item>
          <el-form-item label="页数">
            <el-input v-model="editPages" placeholder="选填" />
          </el-form-item>
          <el-form-item label="出版日期">
            <el-input v-model="editPubDate" placeholder="选填" />
          </el-form-item>
          <el-form-item label="库存">
            <el-input v-model="bookInfo.stockpile.amount" placeholder="请输入库存" type="number" />
          </el-form-item>
        </el-collapse-item>
      </el-collapse>
    </el-form>
    <template #footer>
      <el-button @click="centerDialogVisible2 = false">取消</el-button>
      <el-button type="primary" @click="submitChange">确定</el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.book-detail-page {
  padding: 24px 32px 40px;
  background: var(--em-bg);
  min-height: calc(100vh - 64px);
}

.book-detail-back {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: var(--em-primary);
  font-weight: 600;
  cursor: pointer;
  margin-bottom: 12px;
  transition: opacity 0.2s;
}

.book-detail-back:hover { opacity: 0.75; }

.book-breadcrumb {
  display: flex;
  gap: 8px;
  color: var(--em-text-muted);
  margin-bottom: 24px;
}

.book-breadcrumb .muted { color: #94a3b8; }

.book-detail-layout {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 40px;
  background: #fff;
  border-radius: var(--em-radius-lg);
  padding: 36px;
  box-shadow: var(--em-shadow-sm);
}

.book-title {
  font-size: 28px;
  font-weight: 700;
  color: var(--em-primary);
  margin: 0 0 16px;
}

.book-meta-row {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  align-items: center;
}

.book-price-display {
  font-size: 24px;
  font-weight: bold;
  color: #ffa502;
}

.book-desc {
  margin-top: 24px;
  padding: 16px;
  background: #f8fafc;
  border-radius: 10px;
}

.description {
  font-size: 16px;
  line-height: 1.6;
  color: var(--em-text-muted);
}

.book-actions {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-top: 28px;
  flex-wrap: wrap;
}

.book-specs {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  margin-top: 24px;
}

.spec-item {
  padding: 10px 14px;
  background: #f1f5f9;
  border-radius: 8px;
  font-size: 13px;
}

.book-detail-right {
  display: flex;
  justify-content: center;
  align-items: center;
}

.book-cover {
  width: 100%;
  max-width: 420px;
  height: 520px;
  border-radius: 12px;
  box-shadow: var(--em-shadow-sm);
}

.avatar-uploader {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  height: 120px;
  width: 120px;
}

.avatar {
  width: 120px;
  height: 120px;
  object-fit: cover;
  display: block;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 120px;
  height: 120px;
  text-align: center;
  line-height: 120px;
}

.upload-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
}
</style>
