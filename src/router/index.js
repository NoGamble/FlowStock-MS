import { createRouter, createWebHistory } from 'vue-router'
import MainLayout from '../layouts/MainLayout.vue'

const routes = [
  {
    path: '/',
    component: MainLayout,
    redirect: '/dashboard', // 默认重定向到仪表盘
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/dashboard/index.vue'),
        meta: { title: '仪表盘' }
      },
      {
        path: 'product',
        name: 'Product',
        component: () => import('../views/product/index.vue'),
        meta: { title: '商品管理' }
      },
      {
        path: 'inbound',
        name: 'Inbound',
        component: () => import('../views/inbound/index.vue'),
        meta: { title: '入库管理' }
      },
      {
        path: 'outbound',
        name: 'Outbound',
        component: () => import('../views/outbound/index.vue'),
        meta: { title: '出库管理' }
      },
      {
        path: 'inventory-check',
        name: 'InventoryCheck',
        component: () => import('../views/inventory-check/index.vue'),
        meta: { title: '库存盘点' }
      }
    ]
  },
  // 如果未来需要独立整页（如登录页），可以与 '/' 同级写在这里
  // {
  //   path: '/login',
  //   component: () => import('../views/login/index.vue')
  // }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫：动态修改页面标题
router.beforeEach((to, from, next) => {
  if (to.meta.title) {
    document.title = `${to.meta.title} - FlowStock`
  }
  next()
})

export default router
