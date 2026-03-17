<template>
  <div class="page-shell">
    <div class="page-header">
      <div>
        <h2>客户管理</h2>
        <p>维护客户主数据、联系人与合作状态。</p>
      </div>
      <div class="soft-tag">客户档案</div>
    </div>

    <el-card class="data-card">
      <div class="panel-toolbar">
        <div class="left-tools">
          <el-input v-model="searchName" clearable placeholder="搜索客户名称" style="width:220px;" @keyup.enter.native="handleSearch" />
          <el-button type="primary" icon="el-icon-search" @click="handleSearch">查询</el-button>
        </div>
        <div class="right-tools" v-if="isAdmin">
          <el-button type="primary" icon="el-icon-plus" @click="handleAdd">新增客户</el-button>
        </div>
      </div>

      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="customerCode" label="客户编码" min-width="120" />
        <el-table-column prop="customerName" label="客户名称" min-width="160" />
        <el-table-column prop="contactPerson" label="联系人" min-width="120" />
        <el-table-column prop="phone" label="手机号" min-width="130" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column label="状态" width="100">
          <template slot-scope="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="isAdmin" label="操作" width="170">
          <template slot-scope="{ row }">
            <el-button type="text" @click="handleEdit(row)">编辑</el-button>
            <el-button type="text" style="color:#dc2626;" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination style="margin-top:18px;text-align:right;" layout="total, prev, pager, next" :current-page.sync="current" :page-size="size" :total="total" @current-change="loadData" />
    </el-card>

    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="620px">
      <el-form ref="form" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="14">
          <el-col :span="12"><el-form-item label="编码" prop="customerCode"><el-input v-model="form.customerCode" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="名称" prop="customerName"><el-input v-model="form.customerName" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="14">
          <el-col :span="12"><el-form-item label="联系人"><el-input v-model="form.contactPerson" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
        <el-form-item label="地址"><el-input v-model="form.address" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" /></el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
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
import { mapGetters } from 'vuex'

export default {
  name: 'Customer',
  data() {
    return {
      searchName: '',
      loading: false,
      current: 1,
      size: 10,
      total: 0,
      tableData: [],
      dialogVisible: false,
      dialogTitle: '',
      form: {},
      rules: {
        customerCode: [{ required: true, message: '请输入客户编码', trigger: 'blur' }],
        customerName: [{ required: true, message: '请输入客户名称', trigger: 'blur' }]
      }
    }
  },
  computed: {
    ...mapGetters(['isAdmin'])
  },
  created() {
    this.loadData()
  },
  methods: {
    async loadData() {
      this.loading = true
      try {
        const res = await request.get('/customer/page', {
          params: { current: this.current, size: this.size, customerName: this.searchName }
        })
        this.tableData = res.data.records
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
      this.dialogTitle = '新增客户'
      this.form = { status: 1 }
      this.dialogVisible = true
    },
    handleEdit(row) {
      this.dialogTitle = '编辑客户'
      this.form = { ...row }
      this.dialogVisible = true
    },
    handleDelete(id) {
      this.$confirm('确认删除该客户吗？', '提示', { type: 'warning' }).then(async () => {
        await request.delete(`/customer/${id}`)
        this.$message.success('删除成功')
        this.loadData()
      }).catch(() => {})
    },
    handleSubmit() {
      this.$refs.form.validate(async valid => {
        if (!valid) return
        if (this.form.id) {
          await request.put('/customer', this.form)
          this.$message.success('客户已更新')
        } else {
          await request.post('/customer', this.form)
          this.$message.success('客户已创建')
        }
        this.dialogVisible = false
        this.loadData()
      })
    }
  }
}
</script>
