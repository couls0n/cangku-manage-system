<template>
  <div class="page-shell">
    <div class="page-header">
      <div>
        <h2>用户与权限</h2>
        <p>管理登录账号、角色、所属仓库与启停状态。</p>
      </div>
      <div class="soft-tag">仅管理员可访问</div>
    </div>

    <el-card class="data-card">
      <div class="panel-toolbar">
        <div class="left-tools">
          <el-input v-model="searchName" clearable placeholder="搜索用户名" style="width: 220px;" @keyup.enter.native="handleSearch" />
          <el-button type="primary" icon="el-icon-search" @click="handleSearch">查询</el-button>
        </div>
        <div class="right-tools">
          <el-button type="primary" icon="el-icon-plus" @click="handleAdd">新增用户</el-button>
        </div>
      </div>

      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="username" label="用户名" min-width="130" />
        <el-table-column prop="realName" label="姓名" min-width="120" />
        <el-table-column prop="phone" label="手机号" min-width="130" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column label="角色" width="120">
          <template slot-scope="{ row }">
            <el-tag :type="row.role === 2 ? 'danger' : 'success'">{{ row.role === 2 ? '管理员' : '操作员' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="warehouseId" label="所属仓库" width="120" />
        <el-table-column label="状态" width="100">
          <template slot-scope="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template slot-scope="{ row }">
            <el-button type="text" @click="handleEdit(row)">编辑</el-button>
            <el-button type="text" style="color:#dc2626;" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        style="margin-top: 18px; text-align: right;"
        layout="total, prev, pager, next"
        :current-page.sync="current"
        :page-size="size"
        :total="total"
        @current-change="loadData"
      />
    </el-card>

    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="620px">
      <el-form ref="form" :model="form" :rules="rules" label-width="96px">
        <el-row :gutter="14">
          <el-col :span="12">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="form.username" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="form.id ? '新密码' : '密码'" :prop="form.id ? '' : 'password'">
              <el-input v-model="form.password" type="password" placeholder="编辑时留空表示不修改" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="14">
          <el-col :span="12">
            <el-form-item label="姓名" prop="realName">
              <el-input v-model="form.realName" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号">
              <el-input v-model="form.phone" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="14">
          <el-col :span="12">
            <el-form-item label="邮箱">
              <el-input v-model="form.email" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="仓库 ID" prop="warehouseId">
              <el-input-number v-model="form.warehouseId" :min="1" style="width:100%;" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="14">
          <el-col :span="12">
            <el-form-item label="角色">
              <el-radio-group v-model="form.role">
                <el-radio :label="1">操作员</el-radio>
                <el-radio :label="2">管理员</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-radio-group v-model="form.status">
                <el-radio :label="1">启用</el-radio>
                <el-radio :label="0">禁用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
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
  name: 'User',
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
        username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
        realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
        warehouseId: [{ required: true, message: '请输入所属仓库 ID', trigger: 'change' }]
      }
    }
  },
  created() {
    this.loadData()
  },
  methods: {
    async loadData() {
      this.loading = true
      try {
        const res = await request.get('/user/page', {
          params: { current: this.current, size: this.size, username: this.searchName }
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
      this.dialogTitle = '新增用户'
      this.form = { role: 1, status: 1, warehouseId: 1 }
      this.dialogVisible = true
    },
    handleEdit(row) {
      this.dialogTitle = '编辑用户'
      this.form = { ...row, password: '' }
      this.dialogVisible = true
    },
    handleDelete(id) {
      this.$confirm('确认删除该用户吗？', '提示', { type: 'warning' }).then(async () => {
        await request.delete(`/user/${id}`)
        this.$message.success('删除成功')
        this.loadData()
      }).catch(() => {})
    },
    handleSubmit() {
      this.$refs.form.validate(async valid => {
        if (!valid) return
        if (this.form.id) {
          await request.put('/user', this.form)
          this.$message.success('用户已更新')
        } else {
          await request.post('/user', this.form)
          this.$message.success('用户已创建')
        }
        this.dialogVisible = false
        this.loadData()
      })
    }
  }
}
</script>
