<template>
  <div class="app-container">
    <!--工具栏-->
    <div class="head-container">
      <div v-if="crud.props.searchToggle">
        <!-- 搜索 -->
        <el-input v-model="query.blurry" class="filter-item" clearable placeholder="模糊搜索" style="width: 200px"
                  @keyup.enter.native="crud.toQuery"/>
        <el-date-picker
          v-model="query.createTime"
          :default-time="['00:00:00','23:59:59']"
          class="date-item"
          end-placeholder="结束日期"
          range-separator=":"
          size="small"
          start-placeholder="开始日期"
          type="daterange"
          value-format="yyyy-MM-dd HH:mm:ss"
        />
        <rrOperation/>
      </div>
      <crudOperation :permission="permission">
        <el-button
          slot="right"
          v-permission="['admin','database:add']"
          :disabled="!selectIndex"
          class="filter-item"
          icon="el-icon-upload"
          size="mini"
          type="warning"
          @click="execute"
        >执行脚本
        </el-button>
      </crudOperation>
    </div>
    <!--表单组件-->
    <eForm ref="execute" :database-info="currentRow"/>
    <el-dialog :before-close="crud.cancelCU" :close-on-click-modal="false" :title="crud.status.title"
               :visible.sync="crud.status.cu > 0" append-to-body width="530px">
      <el-form ref="form" :model="form" :rules="rules" label-width="100px" size="small">
        <el-form-item label="连接名称" prop="name">
          <el-input v-model="form.name" style="width: 370px"/>
        </el-form-item>
        <el-form-item label="JDBC地址" prop="jdbcUrl">
          <el-input v-model="form.jdbcUrl" style="width: 300px"/>
          <el-button :loading="loading" type="success" @click="testConnectDatabase">测试</el-button>
        </el-form-item>
        <el-form-item label="用户" prop="userName">
          <el-input v-model="form.userName" style="width: 370px"/>
        </el-form-item>
        <el-form-item label="密码" prop="pwd">
          <el-input v-model="form.pwd" style="width: 370px" type="password"/>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="text" @click="crud.cancelCU">取消</el-button>
        <el-button :loading="crud.status.cu === 2" type="primary" @click="crud.submitCU">确认</el-button>
      </div>
    </el-dialog>
    <!--表格渲染-->
    <el-table ref="table" v-loading="crud.loading" :data="crud.data" highlight-current-row stripe style="width: 100%"
              @selection-change="handleCurrentChange">
      <el-table-column type="selection" width="55"/>
      <el-table-column label="数据库名称" prop="name" width="130px"/>
      <el-table-column label="连接地址" prop="jdbcUrl"/>
      <el-table-column label="用户名" prop="userName" width="200px"/>
      <el-table-column label="创建日期" prop="createTime" width="200px">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column v-permission="['admin','database:edit','database:del']" align="center" label="操作" width="150px">
        <template slot-scope="scope">
          <udOperation
            :data="scope.row"
            :permission="permission"
          />
        </template>
      </el-table-column>
    </el-table>
    <!--分页组件-->
    <pagination/>
  </div>
</template>

<script>
import crudDatabase from '@/api/mnt/database'
import {testDbConnect} from '@/api/mnt/connect'
import eForm from './execute'
import CRUD, {crud, form, header, presenter} from '@crud/crud'
import rrOperation from '@crud/RR.operation'
import crudOperation from '@crud/CRUD.operation'
import udOperation from '@crud/UD.operation'
import pagination from '@crud/Pagination'

const defaultForm = {id: null, name: null, jdbcUrl: 'jdbc:mysql://', userName: null, pwd: null}
export default {
  name: 'DataBase',
  components: {eForm, pagination, crudOperation, rrOperation, udOperation},
  cruds() {
    return CRUD({title: '数据库', url: 'api/database', crudMethod: {...crudDatabase}})
  },
  mixins: [presenter(), header(), form(defaultForm), crud()],
  data() {
    return {
      currentRow: {},
      selectIndex: '',
      databaseInfo: '',
      loading: false,
      permission: {
        add: ['admin', 'database:add'],
        edit: ['admin', 'database:edit'],
        del: ['admin', 'database:del']
      },
      rules: {
        name: [
          {required: true, message: '请输入数据库名称', trigger: 'blur'}
        ],
        jdbcUrl: [
          {required: true, message: '请输入数据库连接地址', trigger: 'blur'}
        ],
        userName: [
          {required: true, message: '请输入用户名', trigger: 'blur'}
        ],
        pwd: [
          {required: true, message: '请输入数据库密码', trigger: 'blur'}
        ]
      }
    }
  },
  methods: {
    testConnectDatabase() {
      this.$refs['form'].validate((valid) => {
        if (valid) {
          this.loading = true
          testDbConnect(this.form).then((res) => {
            this.loading = false
            this.crud.notify(res ? '连接成功' : '连接失败', res ? 'success' : 'error')
          }).catch(() => {
            this.loading = false
          })
        }
      })
    },
    execute() {
      this.$refs.execute.dialog = true
    },
    handleCurrentChange(selection) {
      this.crud.selections = selection
      if (selection.length === 1) {
        const row = selection[0]
        this.selectIndex = row.id
        this.currentRow = row
      } else {
        this.currentRow = {}
        this.selectIndex = ''
      }
    }
  }
}
</script>

<style scoped>
</style>
