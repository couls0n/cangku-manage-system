<template>
  <div class="page-shell">
    <div class="page-header">
      <div>
        <h2>商品管理</h2>
        <p>维护商品档案、品类、品牌、规格、价格与启停状态。</p>
      </div>
      <div class="soft-tag">商品台账</div>
    </div>

    <el-card class="data-card">
      <div class="panel-toolbar">
        <div class="left-tools">
          <el-input v-model="searchName" clearable placeholder="搜索商品名称" style="width: 220px;" @keyup.enter.native="handleSearch" />
          <el-select v-model="searchCategoryId" clearable filterable placeholder="筛选品类" style="width: 180px;">
            <el-option v-for="item in categoryList" :key="item.id" :label="item.categoryName" :value="item.id" />
          </el-select>
          <el-button type="primary" icon="el-icon-search" @click="handleSearch">查询</el-button>
        </div>
        <div class="right-tools" v-if="isAdmin">
          <el-button type="primary" icon="el-icon-plus" @click="handleAdd">新增商品</el-button>
        </div>
      </div>

      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="productCode" label="商品编码" min-width="120" />
        <el-table-column prop="productName" label="商品名称" min-width="160" />
        <el-table-column prop="categoryName" label="品类" min-width="120" />
        <el-table-column prop="brand" label="品牌" min-width="110" />
        <el-table-column prop="specification" label="规格" min-width="120" />
        <el-table-column prop="unit" label="单位" width="80" />
        <el-table-column prop="price" label="单价" width="100" />
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

    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="680px">
      <el-form ref="form" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="14">
          <el-col :span="12"><el-form-item label="商品编码" prop="productCode"><el-input v-model="form.productCode" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="商品名称" prop="productName"><el-input v-model="form.productName" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="14">
          <el-col :span="12">
            <el-form-item label="品类" prop="categoryId">
              <el-select v-model="form.categoryId" filterable style="width:100%;">
                <el-option v-for="item in categoryList" :key="item.id" :label="item.categoryName" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12"><el-form-item label="品牌"><el-input v-model="form.brand" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="14">
          <el-col :span="12"><el-form-item label="规格"><el-input v-model="form.specification" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="单位"><el-input v-model="form.unit" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="14">
          <el-col :span="12"><el-form-item label="单价"><el-input-number v-model="form.price" :min="0" :precision="2" style="width:100%;" /></el-form-item></el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-radio-group v-model="form.status">
                <el-radio :label="1">启用</el-radio>
                <el-radio :label="0">禁用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
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
import { mapGetters } from 'vuex'

export default {
  name: 'Product',
  data() {
    return {
      searchName: '',
      searchCategoryId: null,
      categoryList: [],
      tableData: [],
      loading: false,
      current: 1,
      size: 10,
      total: 0,
      dialogVisible: false,
      dialogTitle: '',
      form: {},
      rules: {
        productCode: [{ required: true, message: '请输入商品编码', trigger: 'blur' }],
        productName: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
        categoryId: [{ required: true, message: '请选择品类', trigger: 'change' }]
      }
    }
  },
  computed: {
    ...mapGetters(['isAdmin'])
  },
  created() {
    this.loadCategory()
    this.loadData()
  },
  methods: {
    async loadCategory() {
      const res = await request.get('/category/list')
      this.categoryList = res.data
    },
    async loadData() {
      this.loading = true
      try {
        const res = await request.get('/product/page', {
          params: {
            current: this.current,
            size: this.size,
            productName: this.searchName,
            categoryId: this.searchCategoryId
          }
        })
        this.tableData = res.data.records.map(item => ({
          ...item,
          categoryName: (this.categoryList.find(category => category.id === item.categoryId) || {}).categoryName || ''
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
      this.dialogTitle = '新增商品'
      this.form = { status: 1, price: 0 }
      this.dialogVisible = true
    },
    handleEdit(row) {
      this.dialogTitle = '编辑商品'
      this.form = { ...row }
      this.dialogVisible = true
    },
    handleDelete(id) {
      this.$confirm('确认删除该商品吗？', '提示', { type: 'warning' }).then(async () => {
        await request.delete(`/product/${id}`)
        this.$message.success('删除成功')
        this.loadData()
      }).catch(() => {})
    },
    handleSubmit() {
      this.$refs.form.validate(async valid => {
        if (!valid) return
        if (this.form.id) {
          await request.put('/product', this.form)
          this.$message.success('商品已更新')
        } else {
          await request.post('/product', this.form)
          this.$message.success('商品已创建')
        }
        this.dialogVisible = false
        this.loadData()
      })
    }
  }
}
</script>
