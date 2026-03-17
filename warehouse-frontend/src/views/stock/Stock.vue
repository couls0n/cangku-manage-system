<template>
  <div class="page-shell">
    <div class="page-header">
      <div>
        <h2>库存管理</h2>
        <p>查看库存数量、冻结数量、批次与库位，支持管理员维护库存记录。</p>
      </div>
      <div class="soft-tag">库存明细</div>
    </div>

    <el-card class="data-card">
      <div class="panel-toolbar">
        <div class="left-tools">
          <el-select v-model="searchWarehouseId" clearable filterable placeholder="筛选仓库" style="width:180px;">
            <el-option v-for="item in warehouseList" :key="item.id" :label="item.warehouseName" :value="item.id" />
          </el-select>
          <el-select v-model="searchProductId" clearable filterable placeholder="筛选商品" style="width:220px;">
            <el-option v-for="item in productList" :key="item.id" :label="item.productName" :value="item.id" />
          </el-select>
          <el-button type="primary" icon="el-icon-search" @click="handleSearch">查询</el-button>
        </div>
        <div class="right-tools" v-if="isAdmin">
          <el-button type="primary" icon="el-icon-plus" @click="handleAdd">新增库存</el-button>
        </div>
      </div>

      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="warehouseName" label="仓库" min-width="140" />
        <el-table-column prop="productName" label="商品" min-width="160" />
        <el-table-column prop="batchNo" label="批次号" min-width="130" />
        <el-table-column prop="quantity" label="库存数量" width="110" />
        <el-table-column prop="frozenQuantity" label="冻结数量" width="110" />
        <el-table-column prop="location" label="库位" min-width="120" />
        <el-table-column prop="createTime" label="创建时间" width="170" />
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
      <el-form ref="form" :model="form" :rules="rules" label-width="96px">
        <el-form-item label="仓库" prop="warehouseId">
          <el-select v-model="form.warehouseId" filterable style="width:100%;">
            <el-option v-for="item in warehouseList" :key="item.id" :label="item.warehouseName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="商品" prop="productId">
          <el-select v-model="form.productId" filterable style="width:100%;">
            <el-option v-for="item in productList" :key="item.id" :label="item.productName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-row :gutter="14">
          <el-col :span="12"><el-form-item label="库存数量"><el-input-number v-model="form.quantity" :min="0" :precision="2" style="width:100%;" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="冻结数量"><el-input-number v-model="form.frozenQuantity" :min="0" :precision="2" style="width:100%;" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="14">
          <el-col :span="12"><el-form-item label="批次号"><el-input v-model="form.batchNo" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="库位"><el-input v-model="form.location" /></el-form-item></el-col>
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
import { mapGetters } from 'vuex'

export default {
  name: 'Stock',
  data() {
    return {
      searchWarehouseId: null,
      searchProductId: null,
      warehouseList: [],
      productList: [],
      tableData: [],
      loading: false,
      current: 1,
      size: 10,
      total: 0,
      dialogVisible: false,
      dialogTitle: '',
      form: {},
      rules: {
        warehouseId: [{ required: true, message: '请选择仓库', trigger: 'change' }],
        productId: [{ required: true, message: '请选择商品', trigger: 'change' }]
      }
    }
  },
  computed: {
    ...mapGetters(['isAdmin'])
  },
  created() {
    this.bootstrap()
  },
  methods: {
    async bootstrap() {
      await Promise.all([this.loadWarehouse(), this.loadProduct()])
      this.loadData()
    },
    async loadWarehouse() {
      const res = await request.get('/warehouse/list')
      this.warehouseList = res.data
    },
    async loadProduct() {
      const res = await request.get('/product/list')
      this.productList = res.data
    },
    async loadData() {
      this.loading = true
      try {
        const res = await request.get('/stock/page', {
          params: {
            current: this.current,
            size: this.size,
            warehouseId: this.searchWarehouseId,
            productId: this.searchProductId
          }
        })
        this.tableData = res.data.records.map(item => ({
          ...item,
          warehouseName: (this.warehouseList.find(warehouse => warehouse.id === item.warehouseId) || {}).warehouseName || '',
          productName: (this.productList.find(product => product.id === item.productId) || {}).productName || ''
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
      this.dialogTitle = '新增库存'
      this.form = { quantity: 0, frozenQuantity: 0 }
      this.dialogVisible = true
    },
    handleEdit(row) {
      this.dialogTitle = '编辑库存'
      this.form = { ...row }
      this.dialogVisible = true
    },
    handleDelete(id) {
      this.$confirm('确认删除该库存记录吗？', '提示', { type: 'warning' }).then(async () => {
        await request.delete(`/stock/${id}`)
        this.$message.success('删除成功')
        this.loadData()
      }).catch(() => {})
    },
    handleSubmit() {
      this.$refs.form.validate(async valid => {
        if (!valid) return
        if (this.form.id) {
          await request.put('/stock', this.form)
          this.$message.success('库存已更新')
        } else {
          await request.post('/stock', this.form)
          this.$message.success('库存已创建')
        }
        this.dialogVisible = false
        this.loadData()
      })
    }
  }
}
</script>
