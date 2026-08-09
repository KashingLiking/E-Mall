import { createRouter, createWebHistory } from 'vue-router';


const routes = [
    { path: '/', redirect: '/login' },
    { path: '/login', component: () => import('@/views/user/login.vue') },
    { path: '/register', component: () => import('@/views/user/regsiter.vue') },
    { path: '/dashboard', component: () => import('@/views/user/dashboard.vue') },
    { path: '/edit-profile', component: () => import('@/views/user/edituserinfor.vue'), meta: { requiresAuth: true } },
    { path: '/store-detail', component: () => import('@/views/store/detail.vue') },
    { path: '/book-detail/:bookId', component: () => import('@/views/store/book.vue') },
    {
        path: '/orders',
        name: 'OrderList',
        component: () => import('@/views/order/OrderList.vue'),
        meta: { requiresAuth: true }
    },
    {
        path: '/seckill',
        name: 'SeckillList',
        component: () => import('@/views/seckill/SeckillList.vue')
    },
];

const router = createRouter({
    history: createWebHistory(),
    routes
});

router.beforeEach((to, from, next) => {
    const token = sessionStorage.getItem('token');
    if (to.meta.requiresAuth && !token) {
        next('/login');
    } else {
        next();
    }
});

export default router;
