<template>
  <div class="app-container">
    <div class="head-container">
      <Search />
      <crudOperation>
        <el-button
          slot="left"
          :loading="crud.delAllLoading"
          class="filter-item"
          icon="el-icon-delete"
          size="mini"
          type="danger"
          @click="confirmDelAll()"
        >
          清空
        </el-button>
      </crudOperation>
    </div>
    <!--表格渲染-->
    <el-table
      ref="table"
      v-loading="crud.loading"
      :data="crud.data"
      style="width: 100%;"
      @selection-change="crud.selectionChangeHandler"
    >
      <el-table-column type="expand">
        <template slot-scope="props">
          <el-form class="demo-table-expand" inline label-position="left">
            <el-form-item label="请求方法">
              <span>{{ props.row.method }}</span>
            </el-form-item>
            <el-form-item label="请求参数">
              <span>{{ props.row.params }}</span>
            </el-form-item>
          </el-form>
        </template>
      </el-table-column>
      <el-table-column label="用户名" prop="username" />
      <el-table-column label="IP" prop="requestIp" />
      <el-table-column :show-overflow-tooltip="true" label="IP来源" prop="address" />
      <el-table-column label="描述" prop="description" />
      <el-table-column label="浏览器" prop="browser" />
      <el-table-column label="创建日期" prop="createTime">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="异常详情" width="100px">
        <template slot-scope="scope">
          <el-button size="mini" type="text" @click="info(scope.row.id)">查看详情</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog :visible.sync="dialog" append-to-body title="异常详情" top="30px" width="85%">
      <pre v-highlightjs="errorInfo"><code class="java" /></pre>
    </el-dialog>
    <!--分页组件-->
    <pagination />
  </div>
</template>

<script>
import { delAllError, getErrDetail } from '@/api/monitor/log'
import Search from './search'
import CRUD, { presenter } from '@crud/crud'
import crudOperation from '@crud/CRUD.operation'
import pagination from '@crud/Pagination'

export default {
  name: 'ErrorLog',
  components: { Search, crudOperation, pagination },
  cruds() {
    return CRUD({ title: '异常日志', url: 'api/logs/error' })
  },
  mixins: [presenter()],
  data() {
    return {
      errorInfo: '', dialog: false
    }
  },
  created() {
    this.crud.optShow = {
      add: false,
      edit: false,
      del: false,
      download: true
    }
  },
  methods: {
    // 获取异常详情
    info(id) {
      this.dialog = true
      getErrDetail(id).then(res => {
        this.errorInfo = res.exception
      })
    },
    confirmDelAll() {
      this.$confirm(`确认清空所有异常日志吗?`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.crud.delAllLoading = true
        delAllError().then(res => {
          this.crud.delAllLoading = false
          this.crud.dleChangePage(1)
          this.crud.delSuccessNotify()
          this.crud.toQuery()
        }).catch(err => {
          this.crud.delAllLoading = false
          console.log(err.response.data.message)
        })
      }).catch(() => {
      })
    }
  }
}
</script>

<style scoped>
.demo-table-expand {
  font-size: 0;
}

.demo-table-expand label {
  width: 70px;
  color: #99a9bf;
}

.demo-table-expand .el-form-item {
  margin-right: 0;
  margin-bottom: 0;
  width: 100%;
}

.demo-table-expand .el-form-item__content {
  font-size: 12px;
}

/deep/ .el-dialog__body {
  padding: 0 20px 10px 20px !important;
}

.java.hljs {
  color: #444;
  background: #ffffff !important;
  height: 630px !important;
}
</style>
