<template>
  <div class="app-container">
    <!--工具栏-->
    <div class="head-container">
      <div v-if="crud.props.searchToggle">
        <!-- 搜索 -->
        <el-input v-model="query.id" class="filter-item" clearable placeholder="输入名称或IP搜索" style="width: 200px"
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
      <crudOperation :permission="permission"/>
    </div>
    <!--表单组件-->
    <el-dialog :before-close="crud.cancelCU" :close-on-click-modal="false" :title="crud.status.title"
               :visible.sync="crud.status.cu > 0" append-to-body width="470px">
      <el-form ref="form" :model="form" :rules="rules" label-width="55px" size="small">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" style="width: 370px"/>
        </el-form-item>
        <el-form-item label="IP" prop="ip">
          <el-input v-model="form.ip" style="width: 370px"/>
        </el-form-item>
        <el-form-item label="端口" prop="port">
          <el-input-number v-model.number="form.port" controls-position="right" style="width: 370px;"/>
        </el-form-item>
        <el-form-item label="账号" prop="account">
          <el-input v-model="form.account" style="width: 370px"/>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" style="width: 200px" type="password"/>
          <el-button :loading="loading" style="align: right;" type="success" @click="testConnectServer">测试连接</el-button>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="text" @click="crud.cancelCU">取消</el-button>
        <el-button :loading="crud.status.cu === 2" type="primary" @click="crud.submitCU">确认</el-button>
      </div>
    </el-dialog>
    <!--表格渲染-->
    <el-table ref="table" v-loading="crud.loading" :data="crud.data" style="width: 100%"
              @selection-change="crud.selectionChangeHandler">
      <el-table-column type="selection" width="55"/>
      <el-table-column label="名称" prop="name"/>
      <el-table-column label="IP" prop="ip"/>
      <el-table-column label="端口" prop="port"/>
      <el-table-column label="账号" prop="account"/>
      <el-table-column label="创建日期" prop="createTime">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column v-permission="['admin','serverDeploy:edit','serverDeploy:del']" align="center" label="操作"
                       width="150px">
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

import crudServer from '@/api/mnt/serverDeploy'
import {testServerConnect} from '@/api/mnt/connect'
import {validateIP} from '@/utils/validate'
import CRUD, {crud, form, header, presenter} from '@crud/crud'
import rrOperation from '@crud/RR.operation'
import crudOperation from '@crud/CRUD.operation'
import udOperation from '@crud/UD.operation'
import pagination from '@crud/Pagination'

const defaultForm = {id: null, name: null, ip: null, port: 22, account: 'root', password: null}
export default {
  name: 'Server',
  components: {pagination, crudOperation, rrOperation, udOperation},
  cruds() {
    return CRUD({title: '服务器', url: 'api/serverDeploy', crudMethod: {...crudServer}})
  },
  mixins: [presenter(), header(), form(defaultForm), crud()],
  data() {
    return {
      accountList: [],
      accountMap: {},
      loading: false,
      permission: {
        add: ['admin', 'serverDeploy:add'],
        edit: ['admin', 'serverDeploy:edit'],
        del: ['admin', 'serverDeploy:del']
      },
      rules: {
        name: [
          {required: true, message: '请输入名称', trigger: 'blur'}
        ],
        ip: [
          {required: true, message: '请输入IP', trigger: 'blur'},
          {validator: validateIP, trigger: 'change'}
        ],
        port: [
          {required: true, message: '请输入端口', trigger: 'blur', type: 'number'}
        ],
        account: [
          {required: true, message: '请输入账号', trigger: 'blur'}
        ],
        password: [
          {required: true, message: '请输入密码', trigger: 'blur'}
        ]
      }
    }
  },
  methods: {
    testConnectServer() {
      this.$refs['form'].validate((valid) => {
        if (valid) {
          this.loading = true
          testServerConnect(this.form).then((res) => {
            this.loading = false
            this.$notify({
              title: res ? '连接成功' : '连接失败',
              type: res ? 'success' : 'error',
              duration: 2500
            })
          }).catch(() => {
            this.loading = false
          })
        }
      })
    }
  }
}
</script>

<style lang="scss" rel="stylesheet/scss" scoped>
/deep/ .el-input-number .el-input__inner {
  text-align: left;
}
</style>
