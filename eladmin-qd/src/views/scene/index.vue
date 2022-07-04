<template>
  <div class="app-container">
    <!--工具栏-->
    <div class="head-container">
      <!--如果想在工具栏加入更多按钮，可以使用插槽方式， slot = 'left' or 'right'-->
      <crudOperation :permission="permission" />
      <!--表单组件-->
      <el-dialog :close-on-click-modal="false" :before-close="crud.cancelCU" :visible.sync="crud.status.cu > 0" :title="crud.status.title" width="500px">
        <el-form ref="form" :model="form" :rules="rules" size="small" label-width="80px">
          <el-form-item label="序号">
            <el-input v-model="form.id" style="width: 370px;" />
          </el-form-item>
          <el-form-item label="操作内容">
            <el-input v-model="form.content" style="width: 370px;" />
          </el-form-item>
          <el-form-item label="操作时间">
            <el-input v-model="form.time" style="width: 370px;" />
          </el-form-item>
          <el-form-item label="操作人">
            <el-input v-model="form.user" style="width: 370px;" />
          </el-form-item>
          <el-form-item label="ip">
            <el-input v-model="form.ip" style="width: 370px;" />
          </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
          <el-button type="text" @click="crud.cancelCU">取消</el-button>
          <el-button :loading="crud.cu === 2" type="primary" @click="crud.submitCU">确认</el-button>
        </div>
      </el-dialog>
      <!--表格渲染-->
      <el-table ref="table" v-loading="crud.loading" :data="crud.data" size="small" style="width: 100%;" @selection-change="crud.selectionChangeHandler">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="序号" />
        <el-table-column prop="content" label="操作内容" />
        <el-table-column prop="time" label="操作时间" />
        <el-table-column prop="user" label="操作人" />
        <el-table-column prop="ip" label="ip" />
        <el-table-column v-permission="['admin','log:edit','log:del']" label="操作" width="150px" align="center">
          <template slot-scope="scope">
            <udOperation
              :data="scope.row"
              :permission="permission"
            />
          </template>
        </el-table-column>
      </el-table>
      <!--分页组件-->
      <pagination />
    </div>
  </div>
</template>

<script>
import crudLog from '@/api/log'
import CRUD, { crud, form, header, presenter } from '@crud/crud'
import rrOperation from '@crud/RR.operation'
import crudOperation from '@crud/CRUD.operation'
import udOperation from '@crud/UD.operation'
import pagination from '@crud/Pagination'

const defaultForm = { id: null, content: null, time: null, user: null, ip: null }
export default {
  name: 'Log',
  components: { pagination, crudOperation, rrOperation, udOperation },
  mixins: [presenter(), header(), form(defaultForm), crud()],
  cruds() {
    return CRUD({ title: '/api/log', url: 'api/log', sort: 'id,desc', crudMethod: { ...crudLog }})
  },
  data() {
    return {
      permission: {
        add: ['admin', 'log:add'],
        edit: ['admin', 'log:edit'],
        del: ['admin', 'log:del']
      },
      rules: {}
    }
  },
  methods: {
    // 钩子：在获取表格数据之前执行，false 则代表不获取数据
    [CRUD.HOOK.beforeRefresh]() {
      return true
    }
  }
}
</script>

<style scoped>

</style>
