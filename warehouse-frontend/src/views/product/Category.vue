<template>
  <div class="page-shell">
    <div class="page-header">
      <div>
        <h2>品类管理</h2>
        <p>维护商品品类、排序、状态与备注信息。</p>
      </div>
      <div class="soft-tag">商品主数据</div>
    </div>

    <el-card class="data-card">
      <div class="panel-toolbar">
        <div class="left-tools">
          <el-input v-model="searchName" clearable placeholder="搜索品类名称" style="width: 220px;" @keyup.enter.native="handleSearch" />
          <el-button type="primary" icon="el-icon-search" @click="handleSearch">查询</el-button>
        </div>
        <div class="right-tools" v-if="isAdmin">
          <el-button type="primary" icon="el-icon-plus" @click="handleAdd">新增品类</el-button>
        </div>
      </div>

      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="categoryName" label="品类名称" min-width="160" />
        <el-table-column prop="sort" label="排序" width="90" />
        <el-table-column prop="remark" label="备注" min-width="220" show-overflow-tooltip />
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

      <el-pagination
        style="margin-top:18px;text-align:right;"
        layout="total, prev, pager, next"
        :current-page.sync="current"
        :page-size="size"
        :total="total"
        @current-change="loadData"
      />
    </el-card>

    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="560px">
      <el-form ref="form" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="品类名称" prop="categoryName"><el-input v-model="form.categoryName" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" /></el-form-item>
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
  name: 'Category',
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
        categoryName: [{ required: true, message: '请输入品类名称', trigger: 'blur' }]
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
        const res = await request.get('/category/page', {
          params: { current: this.current, size: this.size, categoryName: this.searchName }
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
      this.dialogTitle = '新增品类'
      this.form = { status: 1, sort: 0 }
      this.dialogVisible = true
    },
    handleEdit(row) {
      this.dialogTitle = '编辑品类'
      this.form = { ...row }
      this.dialogVisible = true
    },
    handleDelete(id) {
      this.$confirm('确认删除该品类吗？', '提示', { type: 'warning' }).then(async () => {
        await request.delete(`/category/${id}`)
        this.$message.success('删除成功')
        this.loadData()
      }).catch(() => {})
    },
    handleSubmit() {
      this.$refs.form.validate(async valid => {
        if (!valid) return
        if (this.form.id) {
          await request.put('/category', this.form)
          this.$message.success('品类已更新')
        } else {
          await request.post('/category', this.form)
          this.$message.success('品类已创建')
        }
        this.dialogVisible = false
        this.loadData()
      })
    }
  }
}
</script>
