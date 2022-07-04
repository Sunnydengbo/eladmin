<template>
  <div class="app-container">
    <!--工具栏-->
    <div class="head-container">
      <div v-if="crud.props.searchToggle">
        <!--搜索-->
        <el-input
          v-model="query.filename"
          class="filter-item"
          clearable
          placeholder="输入文件名"
          size="small"
          style="width: 200px;"
          @keyup.enter.native="crud.toQuery"
        />
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
        <rrOperation />
      </div>
      <crudOperation :permission="permission">
        <!-- 上传 -->
        <el-button
          slot="left"
          v-permission="['admin','pictures:add']"
          class="filter-item"
          icon="el-icon-upload"
          size="mini"
          type="primary"
          @click="dialog = true"
        >上传
        </el-button>
        <el-tooltip
          slot="right"
          class="item"
          content="使用同步功能需要在 https://sm.ms/login 中注册账号，并且在 application.yml 文件中修改 Secret Token"
          effect="dark"
          placement="top-start"
        >
          <el-button
            v-permission="['admin','pictures:add']"
            :loading="syncLoading"
            class="filter-item"
            icon="el-icon-refresh"
            size="mini"
            type="success"
            @click="sync"
          >同步
          </el-button>
        </el-tooltip>
      </crudOperation>
    </div>
    <!--上传图片-->
    <el-dialog :close-on-click-modal="false" :visible.sync="dialog" append-to-body width="600px" @close="doSubmit">
      <el-upload
        :action="imagesUploadApi"
        :before-remove="handleBeforeRemove"
        :file-list="fileList"
        :headers="headers"
        :on-error="handleError"
        :on-preview="handlePictureCardPreview"
        :on-success="handleSuccess"
        list-type="picture-card"
      >
        <i class="el-icon-plus" />
      </el-upload>
      <el-dialog :visible.sync="dialogVisible" append-to-body>
        <img :src="dialogImageUrl" alt="" width="100%">
      </el-dialog>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="doSubmit">确认</el-button>
      </div>
    </el-dialog>
    <!--表格渲染-->
    <el-table
      ref="table"
      v-loading="crud.loading"
      :data="crud.data"
      style="width: 100%;"
      @selection-change="crud.selectionChangeHandler"
    >
      <el-table-column type="selection" width="55" />
      <el-table-column label="文件名" prop="filename" width="200" />
      <el-table-column label="上传者" prop="username" />
      <el-table-column ref="table" :show-overflow-tooltip="true" label="缩略图" prop="url">
        <template slot-scope="{row}">
          <el-image
            :preview-src-list="[row.url]"
            :src="row.url"
            class="el-avatar"
            fit="contain"
            lazy
          />
        </template>
      </el-table-column>
      <el-table-column label="文件大小" prop="size" />
      <el-table-column label="高度" prop="height" />
      <el-table-column label="宽度" prop="width" />
      <el-table-column label="创建日期" prop="createTime">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
    </el-table>
    <!--分页组件-->
    <pagination />
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import crudPic from '@/api/tools/picture'
import CRUD, { crud, header, presenter } from '@crud/crud'
import { getToken } from '@/utils/auth'
import rrOperation from '@crud/RR.operation'
import crudOperation from '@crud/CRUD.operation'
import pagination from '@crud/Pagination'

export default {
  name: 'Pictures',
  components: { pagination, crudOperation, rrOperation },
  cruds() {
    return CRUD({ title: '图片', url: 'api/pictures', crudMethod: { ...crudPic }})
  },
  mixins: [presenter(), header(), crud()],
  data() {
    return {
      dialog: false,
      syncLoading: false,
      headers: {
        'Authorization': getToken()
      },
      permission: {
        del: ['admin', 'pictures:del']
      },
      dialogImageUrl: '',
      dialogVisible: false,
      fileList: [],
      pictures: []
    }
  },
  computed: {
    ...mapGetters([
      'imagesUploadApi'
    ])
  },
  created() {
    this.crud.optShow.add = false
    this.crud.optShow.edit = false
  },
  methods: {
    handleSuccess(response, file, fileList) {
      const uid = file.uid
      const id = response.id
      this.pictures.push({ uid, id })
    },
    handleBeforeRemove(file, fileList) {
      for (let i = 0; i < this.pictures.length; i++) {
        if (this.pictures[i].uid === file.uid) {
          crudPic.del(this.pictures[i].id).then(res => {
          })
          return true
        }
      }
    },
    handlePictureCardPreview(file) {
      this.dialogImageUrl = file.url
      this.dialogVisible = true
    },
    // 刷新列表数据
    doSubmit() {
      this.fileList = []
      this.dialogVisible = false
      this.dialogImageUrl = ''
      this.dialog = false
      this.crud.toQuery()
    },
    // 监听上传失败
    handleError(e, file, fileList) {
      const msg = JSON.parse(e.message)
      this.$notify({
        title: msg.message,
        type: 'error',
        duration: 2500
      })
    },
    sync() {
      this.syncLoading = true
      crudPic.sync().then(res => {
        this.crud.notify('同步成功', CRUD.NOTIFICATION_TYPE.SUCCESS)
        this.crud.toQuery()
        this.syncLoading = false
      })
    }
  }
}
</script>

<style scoped>

</style>
