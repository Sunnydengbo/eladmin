<template>
  <div class="app-container">
    <!--工具栏-->
    <div class="head-container">
      <div v-if="crud.props.searchToggle">
        <!-- 搜索 -->
        <el-input v-model="query.blurry" class="filter-item" clearable placeholder="模糊搜索" size="small"
                  style="width: 200px;" @keyup.enter.native="toQuery"/>
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
    <!--表单渲染-->
    <el-dialog :before-close="crud.cancelCU" :close-on-click-modal="false" :title="crud.status.title"
               :visible.sync="crud.status.cu > 0" append-to-body width="580px">
      <el-form ref="form" :inline="true" :model="form" :rules="rules" label-width="80px" size="small">
        <el-form-item label="菜单类型" prop="type">
          <el-radio-group v-model="form.type" size="mini" style="width: 178px">
            <el-radio-button label="0">目录</el-radio-button>
            <el-radio-button label="1">菜单</el-radio-button>
            <el-radio-button label="2">按钮</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-show="form.type.toString() !== '2'" label="菜单图标" prop="icon">
          <el-popover
            placement="bottom-start"
            trigger="click"
            width="450"
            @show="$refs['iconSelect'].reset()"
          >
            <IconSelect ref="iconSelect" @selected="selected"/>
            <el-input slot="reference" v-model="form.icon" placeholder="点击选择图标" readonly style="width: 450px;">
              <svg-icon v-if="form.icon" slot="prefix" :icon-class="form.icon" class="el-input__icon"
                        style="height: 32px;width: 16px;"/>
              <i v-else slot="prefix" class="el-icon-search el-input__icon"/>
            </el-input>
          </el-popover>
        </el-form-item>
        <el-form-item v-show="form.type.toString() !== '2'" label="外链菜单" prop="iframe">
          <el-radio-group v-model="form.iframe" size="mini">
            <el-radio-button label="true">是</el-radio-button>
            <el-radio-button label="false">否</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-show="form.type.toString() === '1'" label="菜单缓存" prop="cache">
          <el-radio-group v-model="form.cache" size="mini">
            <el-radio-button label="true">是</el-radio-button>
            <el-radio-button label="false">否</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-show="form.type.toString() !== '2'" label="菜单可见" prop="hidden">
          <el-radio-group v-model="form.hidden" size="mini">
            <el-radio-button label="false">是</el-radio-button>
            <el-radio-button label="true">否</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="form.type.toString() !== '2'" label="菜单标题" prop="title">
          <el-input v-model="form.title" :style=" form.type.toString() === '0' ? 'width: 450px' : 'width: 178px'"
                    placeholder="菜单标题"/>
        </el-form-item>
        <el-form-item v-if="form.type.toString() === '2'" label="按钮名称" prop="title">
          <el-input v-model="form.title" placeholder="按钮名称" style="width: 178px;"/>
        </el-form-item>
        <el-form-item v-show="form.type.toString() !== '0'" label="权限标识" prop="permission">
          <el-input v-model="form.permission" :disabled="form.iframe" placeholder="权限标识" style="width: 178px;"/>
        </el-form-item>
        <el-form-item v-if="form.type.toString() !== '2'" label="路由地址" prop="path">
          <el-input v-model="form.path" placeholder="路由地址" style="width: 178px;"/>
        </el-form-item>
        <el-form-item label="菜单排序" prop="menuSort">
          <el-input-number v-model.number="form.menuSort" :max="999" :min="0" controls-position="right"
                           style="width: 178px;"/>
        </el-form-item>
        <el-form-item v-show="!form.iframe && form.type.toString() === '1'" label="组件名称" prop="componentName">
          <el-input v-model="form.componentName" placeholder="匹配组件内Name字段" style="width: 178px;"/>
        </el-form-item>
        <el-form-item v-show="!form.iframe && form.type.toString() === '1'" label="组件路径" prop="component">
          <el-input v-model="form.component" placeholder="组件路径" style="width: 178px;"/>
        </el-form-item>
        <el-form-item label="上级类目" prop="pid">
          <treeselect
            v-model="form.pid"
            :load-options="loadMenus"
            :options="menus"
            placeholder="选择上级类目"
            style="width: 450px;"
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
      :load="getMenus"
      :tree-props="{children: 'children', hasChildren: 'hasChildren'}"
      lazy
      row-key="id"
      @select="crud.selectChange"
      @select-all="crud.selectAllChange"
      @selection-change="crud.selectionChangeHandler"
    >
      <el-table-column type="selection" width="55"/>
      <el-table-column :show-overflow-tooltip="true" label="菜单标题" prop="title" width="125px"/>
      <el-table-column align="center" label="图标" prop="icon" width="60px">
        <template slot-scope="scope">
          <svg-icon :icon-class="scope.row.icon ? scope.row.icon : ''"/>
        </template>
      </el-table-column>
      <el-table-column align="center" label="排序" prop="menuSort">
        <template slot-scope="scope">
          {{ scope.row.menuSort }}
        </template>
      </el-table-column>
      <el-table-column :show-overflow-tooltip="true" label="权限标识" prop="permission"/>
      <el-table-column :show-overflow-tooltip="true" label="组件路径" prop="component"/>
      <el-table-column label="外链" prop="iframe" width="75px">
        <template slot-scope="scope">
          <span v-if="scope.row.iframe">是</span>
          <span v-else>否</span>
        </template>
      </el-table-column>
      <el-table-column label="缓存" prop="cache" width="75px">
        <template slot-scope="scope">
          <span v-if="scope.row.cache">是</span>
          <span v-else>否</span>
        </template>
      </el-table-column>
      <el-table-column label="可见" prop="hidden" width="75px">
        <template slot-scope="scope">
          <span v-if="scope.row.hidden">否</span>
          <span v-else>是</span>
        </template>
      </el-table-column>
      <el-table-column label="创建日期" prop="createTime" width="135px">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column v-permission="['admin','menu:edit','menu:del']" align="center" fixed="right" label="操作"
                       width="130px">
        <template slot-scope="scope">
          <udOperation
            :data="scope.row"
            :permission="permission"
            msg="确定删除吗,如果存在下级节点则一并删除，此操作不能撤销！"
          />
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script>
import crudMenu from '@/api/system/menu'
import IconSelect from '@/components/IconSelect'
import Treeselect, {LOAD_CHILDREN_OPTIONS} from '@riophae/vue-treeselect'
import '@riophae/vue-treeselect/dist/vue-treeselect.css'
import CRUD, {crud, form, header, presenter} from '@crud/crud'
import rrOperation from '@crud/RR.operation'
import crudOperation from '@crud/CRUD.operation'
import udOperation from '@crud/UD.operation'

// crud交由presenter持有
const defaultForm = {
  id: null,
  title: null,
  menuSort: 999,
  path: null,
  component: null,
  componentName: null,
  iframe: false,
  roles: [],
  pid: 0,
  icon: null,
  cache: false,
  hidden: false,
  type: 0,
  permission: null
}
export default {
  name: 'Menu',
  components: {Treeselect, IconSelect, crudOperation, rrOperation, udOperation},
  cruds() {
    return CRUD({title: '菜单', url: 'api/menus', crudMethod: {...crudMenu}})
  },
  mixins: [presenter(), header(), form(defaultForm), crud()],
  data() {
    return {
      menus: [],
      permission: {
        add: ['admin', 'menu:add'],
        edit: ['admin', 'menu:edit'],
        del: ['admin', 'menu:del']
      },
      rules: {
        title: [
          {required: true, message: '请输入标题', trigger: 'blur'}
        ],
        path: [
          {required: true, message: '请输入地址', trigger: 'blur'}
        ]
      }
    }
  },
  methods: {
    // 新增与编辑前做的操作
    [CRUD.HOOK.afterToCU](crud, form) {
      this.menus = []
      if (form.id != null) {
        if (form.pid === null) {
          form.pid = 0
        }
        this.getSupDepts(form.id)
      } else {
        this.menus.push({id: 0, label: '顶级类目', children: null})
      }
    },
    getMenus(tree, treeNode, resolve) {
      const params = {pid: tree.id}
      setTimeout(() => {
        crudMenu.getMenus(params).then(res => {
          resolve(res.content)
        })
      }, 100)
    },
    getSupDepts(id) {
      crudMenu.getMenuSuperior(id).then(res => {
        const children = res.map(function (obj) {
          if (!obj.leaf && !obj.children) {
            obj.children = null
          }
          return obj
        })
        this.menus = [{id: 0, label: '顶级类目', children: children}]
      })
    },
    loadMenus({action, parentNode, callback}) {
      if (action === LOAD_CHILDREN_OPTIONS) {
        crudMenu.getMenusTree(parentNode.id).then(res => {
          parentNode.children = res.map(function (obj) {
            if (!obj.leaf) {
              obj.children = null
            }
            return obj
          })
          setTimeout(() => {
            callback()
          }, 100)
        })
      }
    },
    // 选中图标
    selected(name) {
      this.form.icon = name
    }
  }
}
</script>

<style lang="scss" rel="stylesheet/scss" scoped>
/deep/ .el-input-number .el-input__inner {
  text-align: left;
}

/deep/ .vue-treeselect__control, /deep/ .vue-treeselect__placeholder, /deep/ .vue-treeselect__single-value {
  height: 30px;
  line-height: 30px;
}
</style>
