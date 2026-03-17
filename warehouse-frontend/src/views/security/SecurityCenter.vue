<template>
  <div class="page-shell">
    <div class="page-header">
      <div>
        <h2>安全中心 / eBPF 监控</h2>
        <p>统一查看 Java 业务侧拦截告警与 Linux eBPF 采集上报的异常行为。</p>
      </div>
      <el-button type="primary" @click="loadAll">刷新安全数据</el-button>
    </div>

    <div class="metric-grid">
      <div class="metric-card">
        <div class="label">累计安全事件</div>
        <div class="value">{{ dashboard.totalEvents || 0 }}</div>
      </div>
      <div class="metric-card">
        <div class="label">未关闭告警</div>
        <div class="value">{{ dashboard.openAlerts || 0 }}</div>
      </div>
      <div class="metric-card">
        <div class="label">高危事件</div>
        <div class="value">{{ dashboard.severityDistribution.HIGH || 0 }}</div>
      </div>
      <div class="metric-card">
        <div class="label">执行类异常</div>
        <div class="value">{{ dashboard.eventTypeDistribution.EXEC || 0 }}</div>
      </div>
    </div>

    <el-row :gutter="18">
      <el-col :span="10">
        <el-card class="data-card">
          <div slot="header"><strong>手动注入测试事件</strong></div>
          <el-form :model="ingestForm" label-width="90px">
            <el-form-item label="事件类型">
              <el-select v-model="ingestForm.eventType" style="width: 100%;">
                <el-option label="EXEC" value="EXEC" />
                <el-option label="FILE" value="FILE" />
                <el-option label="NETWORK" value="NETWORK" />
                <el-option label="APP" value="APP" />
              </el-select>
            </el-form-item>
            <el-form-item label="严重级别">
              <el-select v-model="ingestForm.severity" style="width: 100%;">
                <el-option label="HIGH" value="HIGH" />
                <el-option label="MEDIUM" value="MEDIUM" />
                <el-option label="LOW" value="LOW" />
              </el-select>
            </el-form-item>
            <el-form-item label="系统调用">
              <el-input v-model="ingestForm.syscallName" />
            </el-form-item>
            <el-form-item label="目标">
              <el-input v-model="ingestForm.targetPath" />
            </el-form-item>
            <el-form-item label="摘要">
              <el-input v-model="ingestForm.summary" />
            </el-form-item>
            <el-form-item label="详情">
              <el-input v-model="ingestForm.detail" type="textarea" :rows="4" />
            </el-form-item>
            <el-button type="primary" @click="sendEbpfEvent">发送测试事件</el-button>
          </el-form>
        </el-card>
      </el-col>

      <el-col :span="14">
        <el-card class="data-card">
          <div slot="header"><strong>严重度分布</strong></div>
          <div class="progress-item" v-for="item in severityBars" :key="item.label">
            <div class="progress-label">
              <span>{{ item.label }}</span>
              <span>{{ item.value }}</span>
            </div>
            <el-progress :percentage="item.percent" :stroke-width="10" :show-text="false" :color="item.color" />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="18">
      <el-col :span="12">
        <el-card class="data-card">
          <div slot="header"><strong>最新告警</strong></div>
          <el-table :data="alerts" stripe>
            <el-table-column prop="title" label="告警标题" min-width="180" />
            <el-table-column label="级别" width="100">
              <template slot-scope="{ row }">
                <el-tag :type="tagType(row.severity)">{{ row.severity }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="content" label="内容" min-width="220" show-overflow-tooltip />
            <el-table-column prop="hitCount" label="命中次数" width="100" />
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="data-card">
          <div slot="header"><strong>最新事件</strong></div>
          <el-table :data="events" stripe>
            <el-table-column prop="eventType" label="类型" width="90" />
            <el-table-column prop="summary" label="事件摘要" min-width="180" show-overflow-tooltip />
            <el-table-column prop="syscallName" label="系统调用" width="110" />
            <el-table-column label="级别" width="90">
              <template slot-scope="{ row }">
                <el-tag :type="tagType(row.severity)">{{ row.severity }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="时间" width="170" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import request from '@/utils/request'

export default {
  name: 'SecurityCenter',
  data() {
    return {
      dashboard: {
        totalEvents: 0,
        openAlerts: 0,
        severityDistribution: { LOW: 0, MEDIUM: 0, HIGH: 0 },
        eventTypeDistribution: { EXEC: 0, FILE: 0, NETWORK: 0, APP: 0 }
      },
      alerts: [],
      events: [],
      ingestForm: {
        eventType: 'EXEC',
        severity: 'HIGH',
        processName: 'java',
        syscallName: 'execve',
        targetPath: '/bin/sh',
        summary: 'manual monitor verification',
        detail: 'manual event from visual security center'
      }
    }
  },
  computed: {
    severityBars() {
      const total = Math.max(this.dashboard.totalEvents || 1, 1)
      return [
        { label: '高危', value: this.dashboard.severityDistribution.HIGH || 0, percent: Math.round(((this.dashboard.severityDistribution.HIGH || 0) / total) * 100), color: '#dc2626' },
        { label: '中危', value: this.dashboard.severityDistribution.MEDIUM || 0, percent: Math.round(((this.dashboard.severityDistribution.MEDIUM || 0) / total) * 100), color: '#d97706' },
        { label: '低危', value: this.dashboard.severityDistribution.LOW || 0, percent: Math.round(((this.dashboard.severityDistribution.LOW || 0) / total) * 100), color: '#16a34a' }
      ]
    }
  },
  created() {
    this.loadAll()
  },
  methods: {
    tagType(level) {
      return { HIGH: 'danger', MEDIUM: 'warning', LOW: 'success' }[level] || 'info'
    },
    async loadAll() {
      try {
        const [dashboard, alerts, events] = await Promise.all([
          request.get('/security/dashboard'),
          request.get('/security/alerts'),
          request.get('/security/events')
        ])
        this.dashboard = dashboard.data
        this.alerts = alerts.data
        this.events = events.data
      } catch (error) {
        console.error(error)
      }
    },
    async sendEbpfEvent() {
      try {
        await request.post('/security/ebpf/ingest', {
          ...this.ingestForm,
          processId: Date.now() % 100000
        }, {
          headers: {
            'X-EBPF-KEY': 'warehouse-ebpf-agent-key'
          }
        })
        this.$message.success('测试事件已发送')
        this.loadAll()
      } catch (error) {
        console.error(error)
      }
    }
  }
}
</script>

<style scoped>
.progress-item {
  margin-bottom: 18px;
}

.progress-label {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}
</style>
