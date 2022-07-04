<template>
  <div class="app-container">
    <div class="head-container">
      <div v-if="crud.props.searchToggle">
        <el-input v-model="query.filter" class="filter-item" clearable placeholder="全表模糊搜索" size="small"
                  style="width: 200px;" @keyup.enter.native="crud.toQuery"/>
        <rrOperation/>
      </div>
      <crudOperation>
        <el-button
          slot="left"
          :disabled="crud.selections.length === 0"
          :loading="delLoading"
          class="filter-item"
          icon="el-icon-delete"
          size="mini"
          type="danger"
          @click="doDelete(crud.selections)"
        >
          强退
        </el-button>
      </crudOperation>
    </div>
    <!--表格渲染-->
    <el-table ref="table" v-loading="crud.loading" :data="crud.data" style="width: 100%;"
              @selection-change="crud.selectionChangeHandler">
      <el-table-column type="selection" width="55"/>
      <el-table-column label="用户名" prop="userName"/>
      <el-table-column label="用户昵称" prop="nickName"/>
      <el-table-column label="部门" prop="dept"/>
      <el-table-column label="登录IP" prop="ip"/>
      <el-table-column :show-overflow-tooltip="true" label="登录地点" prop="address"/>
      <el-table-column label="浏览器" prop="browser"/>
      <el-table-column label="登录时间" prop="loginTime">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.loginTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column fixed="right" label="操作" width="70px">
        <template slot-scope="scope">
          <el-popover
            :ref="scope.$index"
            v-permission="['admin']"
            placement="top"
            width="180"
          >
            <p>确定强制退出该用户吗？</p>
            <div style="text-align: right; margin: 0">
              <el-button size="mini" type="text" @click="$refs[scope.$index].doClose()">取消</el-button>
              <el-button :loading="delLoading" size="mini" type="primary"
                         @click="delMethod(scope.row.key, scope.$index)">确定
              </el-button>
            </div>
            <el-button slot="reference" size="mini" type="text">强退</el-button>
          </el-popover>
        </template>
      </el-table-column>
    </el-table>
    <!--分页组件-->
    <pagination/>
  </div>
</template>

<script>
import {del} from '@/api/monitor/online'
import CRUD, {crud, header, presenter} from '@crud/crud'
import rrOperation from '@crud/RR.operation'
import crudOperation from '@crud/CRUD.operation'
import pagination from '@crud/Pagination'

export default {
  name: 'OnlineUser',
  components: {pagination, crudOperation, rrOperation},
  cruds() {
    return CRUD({url: 'auth/online', title: '在线用户'})
  },
  mixins: [presenter(), header(), crud()],
  data() {
    return {
      delLoading: false,
      permission: {}
    }
  },
  created() {
    this.crud.msg.del = '强退成功！'
    this.crud.optShow = {
      add: false,
      edit: false,
      del: false,
      download: true
    }
  },
  methods: {
    doDelete(datas) {
      this.$confirm(`确认强退选中的${datas.length}个用户?`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.delMethod(datas)
      }).catch(() => {
      })
    },
    // 踢出用户
    delMethod(key, index) {
      const ids = []
      if (key instanceof Array) {
        key.forEach(val => {
          ids.push(val.key)
        })
      } else ids.push(key)
      this.delLoading = true
      del(ids).then(() => {
        this.delLoading = false
        if (this.$refs[index]) {
          this.$refs[index].doClose()
        }
        this.crud.dleChangePage(1)
        this.crud.delSuccessNotify()
        this.crud.toQuery()
      }).catch(() => {
        this.delLoading = false
        if (this.$refs[index]) {
          this.$refs[index].doClose()
        }
      })
    }
  }
}
</script>
