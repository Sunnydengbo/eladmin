<template>
  <div class="app-container">
    <!--工具栏-->
    <div class="head-container">
      <div v-if="crud.props.searchToggle">
        <!-- 搜索 -->
        <el-input v-model="query.appName" class="filter-item" clearable placeholder="输入应用名称查询" style="width: 200px"
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
        <template slot="right">
          <el-button
            v-permission="['admin','deploy:add']"
            :disabled="!selectIndex"
            class="filter-item"
            icon="el-icon-upload"
            size="mini"
            type="primary"
            @click="sysRestore"
          >系统还原
          </el-button>
          <el-button
            v-permission="['admin','deploy:add']"
            :disabled="!selectIndex"
            class="filter-item"
            icon="el-icon-upload"
            size="mini"
            type="primary"
            @click="serverStatus"
          >状态查询
          </el-button>
          <el-button
            v-permission="['admin','deploy:add']"
            :disabled="!selectIndex"
            class="filter-item"
            icon="el-icon-upload"
            size="mini"
            type="success"
            @click="startServer"
          >启动
          </el-button>
          <el-button
            v-permission="['admin','deploy:add']"
            :disabled="!selectIndex"
            class="filter-item"
            icon="el-icon-upload"
            size="mini"
            type="danger"
            @click="stopServer"
          >停止
          </el-button>
          <el-button
            v-permission="['admin','deploy:add']"
            :disabled="!selectIndex"
            class="filter-item"
            icon="el-icon-upload"
            size="mini"
            type="warning"
            @click="deploy"
          >一键部署
          </el-button>
        </template>
      </crudOperation>
    </div>
    <!--表单组件-->
    <el-dialog :before-close="crud.cancelCU" :close-on-click-modal="false" :title="crud.status.title"
               :visible.sync="crud.status.cu > 0" append-to-body width="500px">
      <el-form ref="form" :model="form" :rules="rules" label-width="80px" size="small">
        <el-form-item label="应用" prop="app.id">
          <el-select v-model.number="form.app.id" placeholder="请选择" style="width: 370px">
            <el-option v-for="item in apps" :key="item.id" :label="item.name" :value="item.id"/>
          </el-select>
        </el-form-item>
        <el-form-item label="服务器" prop="deploys">
          <el-select v-model="form.deploys" multiple placeholder="请选择" style="width: 370px">
            <el-option v-for="item in servers" :key="item.id" :label="item.name" :value="item.id"/>
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="text" @click="crud.cancelCU">取消</el-button>
        <el-button :loading="crud.status.cu === 2" type="primary" @click="crud.submitCU">确认</el-button>
      </div>
    </el-dialog>
    <!--统还原组件-->
    <fForm :key="times" ref="sysRestore" :app-name="appName"/>
    <dForm ref="deploy"/>
    <!--表格渲染-->
    <el-table ref="table" v-loading="crud.loading" :data="crud.data" highlight-current-row stripe style="width: 100%"
              @selection-change="handleCurrentChange">
      <el-table-column type="selection" width="55"/>
      <el-table-column label="应用名称" prop="app.name"/>
      <el-table-column label="服务器列表" prop="servers"/>
      <el-table-column label="部署日期" prop="createTime">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column v-permission="['admin','deploy:edit','deploy:del']" align="center" label="操作" width="150px">
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
import crudDeploy from '@/api/mnt/deploy'
import dForm from './deploy'
import fForm from './sysRestore'
import CRUD, {crud, form, header, presenter} from '@crud/crud'
import rrOperation from '@crud/RR.operation'
import crudOperation from '@crud/CRUD.operation'
import udOperation from '@crud/UD.operation'
import pagination from '@crud/Pagination'

const defaultForm = {id: null, app: {id: null}, deploys: []}
export default {
  name: 'Deploy',
  components: {dForm, fForm, pagination, crudOperation, rrOperation, udOperation},
  cruds() {
    return CRUD({title: '部署', url: 'api/deploy', crudMethod: {...crudDeploy}})
  },
  mixins: [presenter(), header(), form(defaultForm), crud()],
  data() {
    return {
      currentRow: {}, selectIndex: '', appName: '', urlHistory: '',
      times: 0, appId: '', deployId: '', apps: [], servers: [],
      permission: {
        add: ['admin', 'deploy:add'],
        edit: ['admin', 'deploy:edit'],
        del: ['admin', 'deploy:del']
      },
      rules: {
        'app.id': [
          {required: true, message: '应用不能为空', trigger: 'blur', type: 'number'}
        ],
        deploys: [
          {required: true, message: '服务器不能为空', trigger: 'blur'}
        ]
      }
    }
  },
  methods: {
    [CRUD.HOOK.beforeRefresh]() {
      this.selectIndex = ''
      return true
    },
    // 新增编辑前做的操作
    [CRUD.HOOK.beforeToCU](crud, form) {
      this.initSelect()
      const deploys = []
      form.deploys.forEach(function (deploy, index) {
        deploys.push(deploy.id)
      })
      this.form.deploys = deploys
    },
    // 提交前
    [CRUD.HOOK.beforeSubmit]() {
      const deploys = []
      this.form.deploys.forEach(function (data, index) {
        const deploy = {id: data}
        deploys.push(deploy)
      })
      this.form.deploys = deploys
      return true
    },
    deploy() {
      this.$refs.deploy.dialog = true
      this.$refs.deploy.deployInfo = this.currentRow
    },
    sysRestore() {
      this.$refs.sysRestore.dialog = true
    },
    handleCurrentChange(selection) {
      this.crud.selections = selection
      if (selection.length === 1) {
        const row = selection[0]
        this.selectIndex = row.id
        this.currentRow = row
        this.appName = row.app.name
        this.times = this.times + 1
        this.appId = row.appId
        this.deployId = row.id
      } else {
        this.currentRow = {}
        this.selectIndex = ''
      }
    },
    startServer() {
      crudDeploy.startServer(JSON.stringify(this.currentRow))
        .then(res => {
        })
        .catch(err => {
          console.log('error:' + err.response.data.message)
        })
    },
    stopServer() {
      crudDeploy.stopServer(JSON.stringify(this.currentRow))
        .then(res => {
        })
        .catch(err => {
          console.log('error:' + err.response.data.message)
        })
    },
    serverStatus() {
      crudDeploy.serverStatus(JSON.stringify(this.currentRow))
        .then(res => {
        })
        .catch(err => {
          console.log('error:' + err.response.data.message)
        })
    },
    initSelect() {
      crudDeploy.getApps().then(res => {
        this.apps = res.content
      })
      crudDeploy.getServers().then(res => {
        this.servers = res.content
      })
    }
  }
}
</script>

<style scoped>
</style>
