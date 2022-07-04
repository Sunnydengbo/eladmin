<template>
  <div class="app-container">
    <!--工具栏-->
    <div class="head-container">
      <div v-if="crud.props.searchToggle">
        <!-- 搜索 -->
        <el-input
          v-model="query.blurry"
          class="filter-item"
          clearable
          placeholder="输入搜索内容"
          style="width: 200px"
          @keyup.enter.native="crud.toQuery"
        />
        <el-date-picker
          v-model="query.deployDate"
          :default-time="['00:00:00','23:59:59']"
          class="date-item"
          end-placeholder="部署结束日期"
          range-separator=":"
          size="small"
          start-placeholder="部署开始日期"
          style="width: 240px"
          type="daterange"
          value-format="yyyy-MM-dd HH:mm:ss"
        />
        <rrOperation />
      </div>
      <crudOperation :permission="permission" />
    </div>
    <!--表格渲染-->
    <el-table
      ref="table"
      v-loading="crud.loading"
      :data="crud.data"
      style="width: 100%"
      @selection-change="crud.selectionChangeHandler"
    >
      <el-table-column type="selection" width="55" />
      <el-table-column label="应用名称" prop="appName" />
      <el-table-column label="部署IP" prop="ip" />
      <el-table-column label="部署人员" prop="deployUser" />
      <el-table-column label="部署时间" prop="deployDate">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.deployDate) }}</span>
        </template>
      </el-table-column>
      <el-table-column v-permission="['admin','deployHistory:del']" align="center" label="操作" width="100px">
        <template slot-scope="scope">
          <el-popover
            :ref="scope.row.id"
            v-permission="['admin','deployHistory:del']"
            placement="top"
            width="180"
          >
            <p>确定删除本条数据吗？</p>
            <div style="text-align: right; margin: 0">
              <el-button size="mini" type="text" @click="$refs[scope.row.id].doClose()">取消</el-button>
              <el-button :loading="delLoading" size="mini" type="primary" @click="delMethod(scope.row.id)">确定
              </el-button>
            </div>
            <el-button slot="reference" icon="el-icon-delete" size="mini" type="danger" />
          </el-popover>
        </template>
      </el-table-column>
    </el-table>
    <!--分页组件-->
    <pagination />
  </div>
</template>

<script>
import { del } from '@/api/mnt/deployHistory'
import CRUD, { header, presenter } from '@crud/crud'
import rrOperation from '@crud/RR.operation'
import crudOperation from '@crud/CRUD.operation'
import pagination from '@crud/Pagination'

export default {
  name: 'DeployHistory',
  components: { pagination, crudOperation, rrOperation },
  cruds() {
    return CRUD({ title: '部署历史', url: 'api/deployHistory', crudMethod: { del }})
  },
  mixins: [presenter(), header()],
  data() {
    return {
      delLoading: false,
      permission: {
        del: ['admin', 'deployHistory:del']
      }
    }
  },
  created() {
    this.crud.optShow = {
      add: false,
      edit: false,
      del: true,
      download: true
    }
  },
  methods: {
    delMethod(id) {
      this.delLoading = true
      del([id]).then(() => {
        this.delLoading = false
        this.$refs[id].doClose()
        this.crud.dleChangePage(1)
        this.crud.delSuccessNotify()
        this.crud.toQuery()
      }).catch(() => {
        this.delLoading = false
        this.$refs[id].doClose()
      })
    }
  }
}
</script>

<style scoped>
</style>
