<template>
  <div>
    <div v-if="query.dictName === ''">
      <div class="my-code">点击字典查看详情</div>
    </div>
    <div v-else>
      <!--工具栏-->
      <div class="head-container">
        <div v-if="crud.props.searchToggle">
          <!-- 搜索 -->
          <el-input
            v-model="query.label"
            class="filter-item"
            clearable
            placeholder="输入字典标签查询"
            size="small"
            style="width: 200px;"
            @keyup.enter.native="toQuery"
          />
          <rrOperation />
        </div>
      </div>
      <!--表单组件-->
      <el-dialog
        :before-close="crud.cancelCU"
        :close-on-click-modal="false"
        :title="crud.status.title"
        :visible="crud.status.cu > 0"
        append-to-body
        width="500px"
      >
        <el-form ref="form" :model="form" :rules="rules" label-width="80px" size="small">
          <el-form-item label="字典标签" prop="label">
            <el-input v-model="form.label" style="width: 370px;" />
          </el-form-item>
          <el-form-item label="字典值" prop="value">
            <el-input v-model="form.value" style="width: 370px;" />
          </el-form-item>
          <el-form-item label="排序" prop="dictSort">
            <el-input-number
              v-model.number="form.dictSort"
              :max="999"
              :min="0"
              controls-position="right"
              style="width: 370px;"
            />
          </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
          <el-button type="text" @click="crud.cancelCU">取消</el-button>
          <el-button :loading="crud.status.cu === 2" type="primary" @click="crud.submitCU">确认</el-button>
        </div>
      </el-dialog>
      <!--表格渲染-->
      <el-table
        ref="table"
        v-loading="crud.loading"
        :data="crud.data"
        highlight-current-row
        style="width: 100%;"
        @selection-change="crud.selectionChangeHandler"
      >
        <el-table-column label="所属字典">
          {{ query.dictName }}
        </el-table-column>
        <el-table-column label="字典标签" prop="label" />
        <el-table-column label="字典值" prop="value" />
        <el-table-column label="排序" prop="dictSort" />
        <el-table-column
          v-permission="['admin','dict:edit','dict:del']"
          align="center"
          fixed="right"
          label="操作"
          width="130px"
        >
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
import crudDictDetail from '@/api/system/dictDetail'

import CRUD, { form, header, presenter } from '@crud/crud'
import pagination from '@crud/Pagination'
import rrOperation from '@crud/RR.operation'
import udOperation from '@crud/UD.operation'

const defaultForm = { id: null, label: null, value: null, dictSort: 999 }

export default {
  components: { pagination, rrOperation, udOperation },
  cruds() {
    return [
      CRUD({
        title: '字典详情', url: 'api/dictDetail', query: { dictName: '' }, sort: ['dictSort,asc', 'id,desc'],
        crudMethod: { ...crudDictDetail },
        optShow: {
          add: true,
          edit: true,
          del: true,
          reset: false
        },
        queryOnPresenterCreated: false
      })
    ]
  },
  mixins: [
    presenter(),
    header(),
    form(function() {
      return Object.assign({ dict: { id: this.dictId }}, defaultForm)
    })],
  data() {
    return {
      dictId: null,
      rules: {
        label: [
          { required: true, message: '请输入字典标签', trigger: 'blur' }
        ],
        value: [
          { required: true, message: '请输入字典值', trigger: 'blur' }
        ],
        dictSort: [
          { required: true, message: '请输入序号', trigger: 'blur', type: 'number' }
        ]
      },
      permission: {
        add: ['admin', 'dict:add'],
        edit: ['admin', 'dict:edit'],
        del: ['admin', 'dict:del']
      }
    }
  }
}
</script>

<style lang="scss" rel="stylesheet/scss" scoped>
/deep/ .el-input-number .el-input__inner {
  text-align: left;
}
</style>
