<template>
  <el-container class="layout-shell">
    <el-aside width="252px" class="sidebar">
      <div class="brand">
        <div class="brand-mark">WM</div>
        <div>
          <div class="brand-title">仓储管理后台</div>
          <div class="brand-sub">业务治理与安全监控</div>
        </div>
      </div>

      <el-menu :default-active="$route.path" router class="nav-menu">
        <el-menu-item
          v-for="route in visibleRoutes"
          :key="route.path"
          :index="route.path"
        >
          <i :class="route.meta.icon"></i>
          <span slot="title">{{ route.meta.title }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container class="main-shell">
      <el-header class="topbar">
        <div>
          <div class="top-title">{{ $route.meta.title || '后台管理' }}</div>
          <div class="top-subtitle">业务视图与安全视图共用同一套权限体系</div>
        </div>
        <div class="top-actions">
          <div class="identity-card">
            <div class="identity-name">{{ currentUser.realName || currentUser.username || '未登录用户' }}</div>
            <div class="identity-meta">
              <span>{{ isAdmin ? '管理员' : '仓库操作员' }}</span>
              <span v-if="currentUser.warehouseId">仓库 #{{ currentUser.warehouseId }}</span>
            </div>
          </div>
          <el-button type="danger" plain size="mini" @click="handleLogout">退出登录</el-button>
        </div>
      </el-header>

      <el-main class="page-area">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script>
import { mapGetters, mapState } from 'vuex'

export default {
  name: 'Layout',
  computed: {
    ...mapState(['user']),
    ...mapGetters(['isAdmin']),
    currentUser() {
      return this.user || {}
    },
    visibleRoutes() {
      return this.$router.options.routes[1].children.filter(route => !(route.meta && route.meta.adminOnly && !this.isAdmin))
    }
  },
  methods: {
    handleLogout() {
      this.$confirm('确定要退出当前账号吗？', '退出确认', { type: 'warning' })
        .then(() => {
          this.$store.dispatch('logout')
          this.$router.push('/login')
          this.$message.success('已退出登录')
        })
        .catch(() => {})
    }
  }
}
</script>

<style scoped>
.layout-shell {
  height: 100%;
}

.sidebar {
  display: flex;
  flex-direction: column;
  padding: 18px;
  background: linear-gradient(180deg, #082f49, #0f172a);
  color: #fff;
}

.brand {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 12px 10px 22px;
}

.brand-mark {
  width: 46px;
  height: 46px;
  display: grid;
  place-items: center;
  border-radius: 14px;
  background: linear-gradient(135deg, #38bdf8, #22c55e);
  color: #082f49;
  font-weight: 800;
}

.brand-title {
  font-size: 17px;
  font-weight: 700;
}

.brand-sub {
  margin-top: 4px;
  color: rgba(255, 255, 255, 0.62);
  font-size: 12px;
}

.nav-menu {
  border-right: none;
  background: transparent;
}

.nav-menu:not(.el-menu--collapse) {
  width: 100%;
}

.nav-menu .el-menu-item {
  margin-bottom: 8px;
  border-radius: 14px;
  color: rgba(255, 255, 255, 0.8);
}

.nav-menu .el-menu-item.is-active {
  background: rgba(255, 255, 255, 0.12);
  color: #fff;
}

.nav-menu .el-menu-item:hover {
  background: rgba(255, 255, 255, 0.08);
  color: #fff;
}

.main-shell {
  background: #f4f7fb;
}

.topbar {
  height: 82px !important;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 28px;
  background: rgba(255, 255, 255, 0.92);
  border-bottom: 1px solid rgba(15, 23, 42, 0.06);
}

.top-title {
  font-size: 24px;
  font-weight: 700;
}

.top-subtitle {
  margin-top: 6px;
  color: #6b7280;
}

.top-actions {
  display: flex;
  align-items: center;
  gap: 14px;
}

.identity-card {
  padding: 10px 14px;
  border-radius: 16px;
  background: linear-gradient(135deg, #eff6ff, #ecfeff);
  border: 1px solid rgba(14, 116, 144, 0.16);
}

.identity-name {
  font-weight: 700;
}

.identity-meta {
  margin-top: 4px;
  color: #6b7280;
  font-size: 12px;
  display: flex;
  gap: 10px;
}

.page-area {
  padding: 24px 28px;
}
</style>
