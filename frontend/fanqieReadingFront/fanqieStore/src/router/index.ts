import { createRouter, createWebHistory } from 'vue-router';


const routes = [

    { path: '/', redirect: '/login' }, // 默认跳转到登录页
    { path: '/', name: 'Home', component: () => import('@/views/Home.vue'), meta: { requiresAuth: true } },
    { path: '/login', component: () => import('@/views/user/login.vue') },
    { path: '/register', component: () => import('@/views/user/regsiter.vue') }, // 确保这个页面存在
    { path: '/dashboard', component: () => import('@/views/user/dashboard.vue') },
    { path: '/edit-profile', component: () => import('@/views/user/edituserinfor.vue'), meta: { requiresAuth: true } },
    { path: '/forgot-password', component: () => import('@/views/user/forgetPassword.vue') },
    { path: '/store-detail', component: () => import('@/views/store/detail.vue') },
    { path: '/book-detail/:bookId', component: () => import('@/views/store/book.vue') },
    {
        path: '/orders',
        name: 'OrderList',
        component: () => import('@/views/order/OrderList.vue'),
        meta: { requiresAuth: true }
    },
];

const router = createRouter({
    history: createWebHistory(),
    routes
});
// 全局路由守卫：检查是否需要身份验证
router.beforeEach((to, from, next) => {
    const token = sessionStorage.getItem('token');  // 从 sessionStorage 获取 token
    if (to.meta.requiresAuth && !token) {
        // 如果目标路由需要认证且用户未登录，跳转到登录页
        next('/login');
    } else {
        next();  // 允许访问
    }
});
export default router;
