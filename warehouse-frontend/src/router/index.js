import Vue from 'vue'
import VueRouter from 'vue-router'
import Login from '@/views/Login.vue'
import Layout from '@/views/Layout.vue'
import Dashboard from '@/views/Dashboard.vue'
import User from '@/views/system/User.vue'
import Warehouse from '@/views/warehouse/Warehouse.vue'
import Category from '@/views/product/Category.vue'
import Product from '@/views/product/Product.vue'
import Stock from '@/views/stock/Stock.vue'
import Inbound from '@/views/order/Inbound.vue'
import Outbound from '@/views/order/Outbound.vue'
import Supplier from '@/views/partner/Supplier.vue'
import Customer from '@/views/partner/Customer.vue'
import SecurityCenter from '@/views/security/SecurityCenter.vue'

Vue.use(VueRouter)

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', name: 'Dashboard', component: Dashboard, meta: { title: '驾驶舱', icon: 'el-icon-s-home' } },
      { path: 'warehouse', name: 'Warehouse', component: Warehouse, meta: { title: '仓库管理', icon: 'el-icon-office-building' } },
      { path: 'stock', name: 'Stock', component: Stock, meta: { title: '库存管理', icon: 'el-icon-box' } },
      { path: 'inbound', name: 'Inbound', component: Inbound, meta: { title: '入库单', icon: 'el-icon-bottom' } },
      { path: 'outbound', name: 'Outbound', component: Outbound, meta: { title: '出库单', icon: 'el-icon-top' } },
      { path: 'category', name: 'Category', component: Category, meta: { title: '品类管理', icon: 'el-icon-menu' } },
      { path: 'product', name: 'Product', component: Product, meta: { title: '商品管理', icon: 'el-icon-goods' } },
      { path: 'supplier', name: 'Supplier', component: Supplier, meta: { title: '供应商', icon: 'el-icon-truck' } },
      { path: 'customer', name: 'Customer', component: Customer, meta: { title: '客户管理', icon: 'el-icon-user-solid' } },
      { path: 'user', name: 'User', component: User, meta: { title: '用户与权限', icon: 'el-icon-s-custom', adminOnly: true } },
      { path: 'security', name: 'SecurityCenter', component: SecurityCenter, meta: { title: '安全中心', icon: 'el-icon-warning', adminOnly: true } }
    ]
  }
]

const router = new VueRouter({
  routes
})

router.beforeEach((to, from, next) => {
  const auth = JSON.parse(sessionStorage.getItem('auth') || 'null')
  const token = auth && auth.token
  const user = auth && auth.user

  if (to.path !== '/login' && !token) {
    next('/login')
    return
  }
  if (to.meta && to.meta.adminOnly && (!user || user.role !== 2)) {
    next('/dashboard')
    return
  }
  next()
})

export default router
