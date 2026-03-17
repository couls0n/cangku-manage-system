<template>
  <div class="page-shell">
    <div class="page-header">
      <div>
        <h2>入库单管理</h2>
        <p>登记入库订单、供应商、仓库和入库状态。</p>
      </div>
      <div class="soft-tag">采购入库流程</div>
    </div>

    <el-card class="data-card">
      <div class="panel-toolbar">
        <div class="left-tools">
          <el-input v-model="searchOrderNo" clearable placeholder="搜索入库单号" style="width:220px;" @keyup.enter.native="handleSearch" />
          <el-select v-model="searchWarehouseId" clearable placeholder="筛选仓库" style="width:180px;">
            <el-option v-for="item in warehouseList" :key="item.id" :label="item.warehouseName" :value="item.id" />
          </el-select>
          <el-button type="primary" icon="el-icon-search" @click="handleSearch">查询</el-button>
        </div>
        <div class="right-tools">
          <el-button type="primary" icon="el-icon-plus" @click="handleAdd">新增入库单</el-button>
        </div>
      </div>

      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="orderNo" label="入库单号" min-width="150" />
        <el-table-column prop="warehouseName" label="仓库" min-width="140" />
        <el-table-column prop="supplierName" label="供应商" min-width="160" />
        <el-table-column prop="totalAmount" label="总金额" width="110" />
        <el-table-column prop="orderTime" label="入库时间" width="170" />
        <el-table-column label="状态" width="100">
          <template slot-scope="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220">
          <template slot-scope="{ row }">
            <el-button type="text" @click="handleEdit(row)" v-if="row.status !== 1">编辑</el-button>
            <el-button type="text" style="color:#16a34a;" @click="handleConfirm(row)" v-if="row.status === 0">确认</el-button>
            <el-button type="text" style="color:#dc2626;" @click="handleDelete(row.id)" v-if="row.status !== 1">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination style="margin-top:18px;text-align:right;" layout="total, prev, pager, next" :current-page.sync="current" :page-size="size" :total="total" @current-change="loadData" />
    </el-card>

    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="680px">
      <el-form ref="form" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="14">
          <el-col :span="12"><el-form-item label="入库单号" prop="orderNo"><el-input v-model="form.orderNo" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="仓库" prop="warehouseId"><el-select v-model="form.warehouseId" style="width:100%;"><el-option v-for="item in warehouseList" :key="item.id" :label="item.warehouseName" :value="item.id" /></el-select></el-form-item></el-col>
        </el-row>
        <el-row :gutter="14">
          <el-col :span="12"><el-form-item label="供应商" prop="supplierId"><el-select v-model="form.supplierId" style="width:100%;"><el-option v-for="item in supplierList" :key="item.id" :label="item.supplierName" :value="item.id" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="总金额"><el-input-number v-model="form.totalAmount" :min="0" :precision="2" style="width:100%;" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="14">
          <el-col :span="12"><el-form-item label="入库时间"><el-date-picker v-model="form.orderTime" type="datetime" style="width:100%;" value-format="yyyy-MM-dd HH:mm:ss" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="状态"><el-select v-model="form.status" style="width:100%;"><el-option label="待确认" :value="0" /><el-option label="已完成" :value="1" /><el-option label="已取消" :value="2" /></el-select></el-form-item></el-col>
        </el-row>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" /></el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">保存</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import request from '@/utils/request'

export default {
  name: 'Inbound',
  data() {
    return {
      searchOrderNo: '',
      searchWarehouseId: null,
      warehouseList: [],
      supplierList: [],
      tableData: [],
      loading: false,
      current: 1,
      size: 10,
      total: 0,
      dialogVisible: false,
      dialogTitle: '',
      form: {},
      rules: {
        orderNo: [{ required: true, message: '请输入入库单号', trigger: 'blur' }],
        warehouseId: [{ required: true, message: '请选择仓库', trigger: 'change' }]
      }
    }
  },
  created() {
    this.bootstrap()
  },
  methods: {
    statusType(status) {
      return { 0: 'warning', 1: 'success', 2: 'info' }[status] || 'info'
    },
    statusText(status) {
      return { 0: '待确认', 1: '已完成', 2: '已取消' }[status] || '未知'
    },
    async bootstrap() {
      await Promise.all([this.loadWarehouse(), this.loadSupplier()])
      this.loadData()
    },
    async loadWarehouse() {
      const res = await request.get('/warehouse/list')
      this.warehouseList = res.data
    },
    async loadSupplier() {
      const res = await request.get('/supplier/list')
      this.supplierList = res.data
    },
    async loadData() {
      this.loading = true
      try {
        const res = await request.get('/inbound/page', {
          params: { current: this.current, size: this.size, orderNo: this.searchOrderNo, warehouseId: this.searchWarehouseId }
        })
        this.tableData = res.data.records.map(item => ({
          ...item,
          warehouseName: (this.warehouseList.find(warehouse => warehouse.id === item.warehouseId) || {}).warehouseName || '',
          supplierName: (this.supplierList.find(supplier => supplier.id === item.supplierId) || {}).supplierName || ''
        }))
        this.total = res.data.total
      } finally {
        this.loading = false
      }
    },
    handleSearch() {
      this.current = 1
      this.loadData()
    },
    handleAdd() {
      this.dialogTitle = '新增入库单'
      this.form = {
        orderNo: `IN${Date.now()}`,
        status: 0,
        totalAmount: 0,
        orderTime: this.formatNow()
      }
      this.dialogVisible = true
    },
    handleEdit(row) {
      this.dialogTitle = '编辑入库单'
      this.form = { ...row }
      this.dialogVisible = true
    },
    handleConfirm(row) {
      this.$confirm('确认将该入库单标记为已完成吗？', '提示', { type: 'warning' }).then(async () => {
        await request.put('/inbound', { ...row, status: 1 })
        this.$message.success('入库单已确认')
        this.loadData()
      }).catch(() => {})
    },
    handleDelete(id) {
      this.$confirm('确认删除该入库单吗？', '提示', { type: 'warning' }).then(async () => {
        await request.delete(`/inbound/${id}`)
        this.$message.success('删除成功')
        this.loadData()
      }).catch(() => {})
    },
    handleSubmit() {
      this.$refs.form.validate(async valid => {
        if (!valid) return
        if (this.form.id) {
          await request.put('/inbound', this.form)
          this.$message.success('入库单已更新')
        } else {
          await request.post('/inbound', this.form)
          this.$message.success('入库单已创建')
        }
        this.dialogVisible = false
        this.loadData()
      })
    },
    formatNow() {
      const date = new Date()
      const pad = value => String(value).padStart(2, '0')
      return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
    }
  }
}
</script>
