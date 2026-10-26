import { createRouter, createWebHashHistory } from 'vue-router';
import { useUserStore } from '@/stores/user';

/**
 * 用 hash 模式：打包后由 Spring Boot 直接托管静态资源，
 * 刷新 /goods/3 这类深层地址不会 404，也不需要后端加 forward 规则。
 */
const routes = [
  {
    path: '/',
    name: 'market',
    component: () => import('@/views/MarketView.vue')
  },
  {
    path: '/goods/:id',
    name: 'goods-detail',
    component: () => import('@/views/GoodsDetailView.vue'),
    props: true
  },
  {
    path: '/sell',
    name: 'sell',
    component: () => import('@/views/SellView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/sell/:id',
    name: 'sell-edit',
    component: () => import('@/views/SellView.vue'),
    props: true,
    meta: { requiresAuth: true }
  },
  {
    path: '/my/goods',
    name: 'my-goods',
    component: () => import('@/views/MyGoodsView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/my/orders',
    name: 'my-orders',
    component: () => import('@/views/OrdersView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/me',
    name: 'profile',
    component: () => import('@/views/ProfileView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/LoginView.vue'),
    meta: { layout: 'bare', guestOnly: true }
  },
  {
    path: '/register',
    name: 'register',
    component: () => import('@/views/RegisterView.vue'),
    meta: { layout: 'bare', guestOnly: true }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'not-found',
    component: () => import('@/views/NotFoundView.vue')
  }
];

const router = createRouter({
  history: createWebHashHistory(),
  routes,
  scrollBehavior(to, from, saved) {
    return saved || { top: 0 };
  }
});

router.beforeEach((to) => {
  const user = useUserStore();

  if (to.meta.requiresAuth && !user.isLoggedIn) {
    return { name: 'login', query: to.fullPath === '/' ? {} : { redirect: to.fullPath } };
  }
  if (to.meta.guestOnly && user.isLoggedIn) {
    return { name: 'market' };
  }
  return true;
});

export default router;
