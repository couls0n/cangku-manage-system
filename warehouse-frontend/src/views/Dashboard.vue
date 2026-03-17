<template>
  <div class="page-shell">
    <div class="page-header">
      <div>
        <h2>仓储运营驾驶舱</h2>
        <p>从业务库存到主机安全告警，把核心状态汇聚到一张总览页里。</p>
      </div>
      <div class="soft-tag">当前账号：{{ currentUser.realName || currentUser.username }}</div>
    </div>

    <div class="metric-grid">
      <div class="metric-card" v-for="card in cards" :key="card.label">
        <div class="label">{{ card.label }}</div>
        <div class="value">{{ card.value }}</div>
        <div class="muted-text" style="margin-top: 10px;">{{ card.hint }}</div>
      </div>
    </div>

    <el-row :gutter="18">
      <el-col :span="14">
        <el-card class="data-card">
          <div slot="header" class="panel-toolbar">
            <div class="left-tools">
              <strong>快捷操作</strong>
            </div>
          </div>
          <el-row :gutter="14">
            <el-col :span="12" v-for="action in quickActions" :key="action.title" style="margin-bottom: 14px;">
              <div class="action-tile" @click="$router.push(action.path)">
                <div class="action-icon" :style="{ background: action.color }">
                  <i :class="action.icon"></i>
                </div>
                <div>
                  <div class="action-title">{{ action.title }}</div>
                  <div class="muted-text">{{ action.desc }}</div>
                </div>
              </div>
            </el-col>
          </el-row>
        </el-card>
      </el-col>

      <el-col :span="10">
        <el-card class="data-card">
          <div slot="header" class="panel-toolbar">
            <div class="left-tools">
              <strong>安全态势摘要</strong>
            </div>
            <el-button v-if="isAdmin" type="text" @click="$router.push('/security')">进入安全中心</el-button>
          </div>
          <div v-if="securityVisible">
            <div class="security-stat">
              <span>累计安全事件</span>
              <strong>{{ security.totalEvents }}</strong>
            </div>
            <div class="security-stat">
              <span>未关闭告警</span>
              <strong>{{ security.openAlerts }}</strong>
            </div>
            <div class="progress-item" v-for="item in progressItems" :key="item.label">
              <div class="progress-label">
                <span>{{ item.label }}</span>
                <span>{{ item.value }}</span>
              </div>
              <el-progress :percentage="item.percent" :stroke-width="10" :show-text="false" :color="item.color"></el-progress>
            </div>
          </div>
          <div v-else class="muted-text">
            当前账号不是管理员，安全中心将只在管理员账号下展示。
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import request from '@/utils/request'
import { mapGetters, mapState } from 'vuex'

export default {
  name: 'Dashboard',
  data() {
    return {
      stats: {
        warehouseCount: 0,
        productCount: 0,
        stockCount: 0,
        inboundCount: 0,
        outboundCount: 0
      },
      security: {
        totalEvents: 0,
        openAlerts: 0,
        severityDistribution: { LOW: 0, MEDIUM: 0, HIGH: 0 }
      }
    }
  },
  computed: {
    ...mapState(['user']),
    ...mapGetters(['isAdmin']),
    currentUser() {
      return this.user || {}
    },
    securityVisible() {
      return this.isAdmin
    },
    cards() {
      return [
        { label: '仓库数量', value: this.stats.warehouseCount, hint: '可管理仓库总量' },
        { label: '商品数量', value: this.stats.productCount, hint: '已录入商品档案' },
        { label: '库存记录', value: this.stats.stockCount, hint: '当前库存条目' },
        { label: '订单总量', value: this.stats.inboundCount + this.stats.outboundCount, hint: '入库单 + 出库单' }
      ]
    },
    quickActions() {
      return [
        { title: '仓库管理', desc: '维护仓库档案与负责人信息', path: '/warehouse', icon: 'el-icon-office-building', color: 'linear-gradient(135deg,#3b82f6,#06b6d4)' },
        { title: '库存管理', desc: '查看库存分布和冻结数量', path: '/stock', icon: 'el-icon-box', color: 'linear-gradient(135deg,#22c55e,#14b8a6)' },
        { title: '入库单', desc: '登记采购入库与到货处理', path: '/inbound', icon: 'el-icon-bottom', color: 'linear-gradient(135deg,#f59e0b,#f97316)' },
        { title: '安全中心', desc: '查看 eBPF 事件和越权告警', path: '/security', icon: 'el-icon-warning', color: 'linear-gradient(135deg,#ef4444,#dc2626)' }
      ].filter(item => item.path !== '/security' || this.isAdmin)
    },
    progressItems() {
      const total = Math.max(this.security.totalEvents || 1, 1)
      return [
        { label: '高危事件', value: this.security.severityDistribution.HIGH || 0, percent: Math.min(100, Math.round(((this.security.severityDistribution.HIGH || 0) / total) * 100)), color: '#dc2626' },
        { label: '中危事件', value: this.security.severityDistribution.MEDIUM || 0, percent: Math.min(100, Math.round(((this.security.severityDistribution.MEDIUM || 0) / total) * 100)), color: '#d97706' },
        { label: '低危事件', value: this.security.severityDistribution.LOW || 0, percent: Math.min(100, Math.round(((this.security.severityDistribution.LOW || 0) / total) * 100)), color: '#16a34a' }
      ]
    }
  },
  created() {
    this.loadStats()
  },
  methods: {
    async loadStats() {
      try {
        const requests = [
          request.get('/warehouse/list'),
          request.get('/product/list'),
          request.get('/stock/list'),
          request.get('/inbound/list'),
          request.get('/outbound/list')
        ]
        if (this.isAdmin) {
          requests.push(request.get('/security/dashboard'))
        }
        const results = await Promise.all(requests)
        this.stats.warehouseCount = results[0].data.length
        this.stats.productCount = results[1].data.length
        this.stats.stockCount = results[2].data.length
        this.stats.inboundCount = results[3].data.length
        this.stats.outboundCount = results[4].data.length
        if (this.isAdmin && results[5]) {
          this.security = results[5].data
        }
      } catch (error) {
        console.error(error)
      }
    }
  }
}
</script>

<style scoped>
.action-tile {
  display: flex;
  gap: 14px;
  align-items: center;
  padding: 18px;
  border-radius: 18px;
  background: #f8fafc;
  border: 1px solid rgba(15, 23, 42, 0.06);
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.action-tile:hover {
  transform: translateY(-2px);
  box-shadow: 0 16px 24px rgba(15, 23, 42, 0.08);
}

.action-icon {
  width: 52px;
  height: 52px;
  border-radius: 16px;
  display: grid;
  place-items: center;
  color: #fff;
  font-size: 22px;
}

.action-title {
  font-weight: 700;
  margin-bottom: 6px;
}

.security-stat {
  display: flex;
  justify-content: space-between;
  padding: 14px 0;
  border-bottom: 1px solid rgba(15, 23, 42, 0.06);
}

.security-stat strong {
  font-size: 24px;
}

.progress-item {
  margin-top: 18px;
}

.progress-label {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}
</style>
