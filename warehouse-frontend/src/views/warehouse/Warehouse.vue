<template>
  <div class="page-shell">
    <div class="page-header">
      <div>
        <h2>仓库管理</h2>
        <p>维护仓库主数据、负责人、联系电话和启停状态。</p>
      </div>
      <div class="soft-tag">仓库视图</div>
    </div>

    <el-card class="data-card">
      <div class="panel-toolbar">
        <div class="left-tools">
          <el-input v-model="searchName" clearable placeholder="搜索仓库名称" style="width: 220px;" @keyup.enter.native="handleSearch" />
          <el-button type="primary" icon="el-icon-search" @click="handleSearch">查询</el-button>
        </div>
        <div class="right-tools" v-if="isAdmin">
          <el-button type="primary" icon="el-icon-plus" @click="handleAdd">新增仓库</el-button>
        </div>
      </div>

      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="warehouseCode" label="仓库编码" min-width="120" />
        <el-table-column prop="warehouseName" label="仓库名称" min-width="160" />
        <el-table-column prop="manager" label="负责人" min-width="120" />
        <el-table-column prop="phone" label="联系电话" min-width="130" />
        <el-table-column prop="address" label="地址" min-width="220" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template slot-scope="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="isAdmin" label="操作" width="170" fixed="right">
          <template slot-scope="{ row }">
            <el-button type="text" @click="handleEdit(row)">编辑</el-button>
            <el-button type="text" style="color:#dc2626;" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        style="margin-top:18px;text-align:right;"
        layout="total, prev, pager, next"
        :current-page.sync="current"
        :page-size="size"
        :total="total"
        @current-change="loadData"
      />
    </el-card>

    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="620px">
      <el-form ref="form" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="14">
          <el-col :span="12"><el-form-item label="仓库编码" prop="warehouseCode"><el-input v-model="form.warehouseCode" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="仓库名称" prop="warehouseName"><el-input v-model="form.warehouseName" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="14">
          <el-col :span="12"><el-form-item label="负责人"><el-input v-model="form.manager" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="联系电话"><el-input v-model="form.phone" /></el-form-item></el-col>
        </el-row>
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
  name: 'Warehouse',
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
        warehouseCode: [{ required: true, message: '请输入仓库编码', trigger: 'blur' }],
        warehouseName: [{ required: true, message: '请输入仓库名称', trigger: 'blur' }]
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
        const res = await request.get('/warehouse/page', {
          params: { current: this.current, size: this.size, warehouseName: this.searchName }
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
      this.dialogTitle = '新增仓库'
      this.form = { status: 1 }
      this.dialogVisible = true
    },
    handleEdit(row) {
      this.dialogTitle = '编辑仓库'
      this.form = { ...row }
      this.dialogVisible = true
    },
    handleDelete(id) {
      this.$confirm('确认删除该仓库吗？', '提示', { type: 'warning' }).then(async () => {
        await request.delete(`/warehouse/${id}`)
        this.$message.success('删除成功')
        this.loadData()
      }).catch(() => {})
    },
    handleSubmit() {
      this.$refs.form.validate(async valid => {
        if (!valid) return
        if (this.form.id) {
          await request.put('/warehouse', this.form)
          this.$message.success('仓库已更新')
        } else {
          await request.post('/warehouse', this.form)
          this.$message.success('仓库已创建')
        }
        this.dialogVisible = false
        this.loadData()
      })
    }
  }
}
</script>
