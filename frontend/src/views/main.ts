import { createApp } from 'vue';
import App from './App.vue';
import router from '@/router'; // 引入路由配置
import ElementPlus from 'element-plus'; // UI 组件库
import 'element-plus/dist/index.css'; // 引入 Element Plus 样式
import './style.css'; // 引入全局样式（如果有）

// 创建 Vue 应用
const app = createApp(App);

app.use(router); // 注册路由
app.use(ElementPlus); // 注册 Element Plus

app.mount('#app'); // 挂载到 index.html 里的 #app
