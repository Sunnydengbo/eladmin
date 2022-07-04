<template>
  <el-dialog :visible.sync="dialog" append-to-body title="执行日志" width="88%">
    <!-- 搜索 -->
    <div class="head-container">
      <el-input
        v-model="query.jobName"
        class="filter-item"
        clearable
        placeholder="输入任务名称搜索"
        size="small"
        style="width: 200px;"
        @keyup.enter.native="toQuery"
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
      <el-select
        v-model="query.isSuccess"
        class="filter-item"
        clearable
        placeholder="日志状态"
        size="small"
        style="width: 110px"
        @change="toQuery"
      >
        <el-option v-for="item in enabledTypeOptions" :key="item.key" :label="item.display_name" :value="item.key" />
      </el-select>
      <el-button class="filter-item" icon="el-icon-search" size="mini" type="success" @click="toQuery">搜索</el-button>
      <!-- 导出 -->
      <div style="display: inline-block;">
        <el-button
          :loading="downloadLoading"
          class="filter-item"
          icon="el-icon-download"
          size="mini"
          type="warning"
          @click="downloadMethod"
        >导出
        </el-button>
      </div>
    </div>
    <!--表格渲染-->
    <el-table v-loading="loading" :data="data" style="width: 100%;margin-top: -10px;">
      <el-table-column :show-overflow-tooltip="true" label="任务名称" prop="jobName" />
      <el-table-column :show-overflow-tooltip="true" label="Bean名称" prop="beanName" />
      <el-table-column :show-overflow-tooltip="true" label="执行方法" prop="methodName" />
      <el-table-column :show-overflow-tooltip="true" label="参数" prop="params" width="120px" />
      <el-table-column :show-overflow-tooltip="true" label="cron表达式" prop="cronExpression" />
      <el-table-column label="异常详情" prop="createTime" width="110px">
        <template slot-scope="scope">
          <el-button
            v-show="scope.row.exceptionDetail"
            size="mini"
            type="text"
            @click="info(scope.row.exceptionDetail)"
          >查看详情
          </el-button>
        </template>
      </el-table-column>
      <el-table-column :show-overflow-tooltip="true" align="center" label="耗时(毫秒)" prop="time" width="100px" />
      <el-table-column align="center" label="状态" prop="isSuccess" width="80px">
        <template slot-scope="scope">
          <el-tag :type="scope.row.isSuccess ? 'success' : 'danger'">{{ scope.row.isSuccess ? '成功' : '失败' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column :show-overflow-tooltip="true" label="创建日期" prop="createTime">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog :visible.sync="errorDialog" append-to-body title="异常详情" width="85%">
      <pre v-highlightjs="errorInfo"><code class="java" /></pre>
    </el-dialog>
    <!--分页组件-->
    <el-pagination
      :current-page="page + 1"
      :page-size="6"
      :total="total"
      layout="total, prev, pager, next"
      style="margin-top:8px;"
      @size-change="sizeChange"
      @current-change="pageChange"
    />
  </el-dialog>
</template>

<script>
import crud from '@/mixins/crud'

export default {
  mixins: [crud],
  data() {
    return {
      title: '任务日志',
      errorInfo: '', errorDialog: false,
      enabledTypeOptions: [
        { key: 'true', display_name: '成功' },
        { key: 'false', display_name: '失败' }
      ]
    }
  },
  methods: {
    doInit() {
      this.$nextTick(() => {
        this.init()
      })
    },
    // 获取数据前设置好接口地址
    beforeInit() {
      this.url = 'api/jobs/logs'
      this.size = 6
      return true
    },
    // 异常详情
    info(errorInfo) {
      this.errorInfo = errorInfo
      this.errorDialog = true
    }
  }
}
</script>

<style scoped>
.java.hljs {
  color: #444;
  background: #ffffff !important;
}

/deep/ .el-dialog__body {
  padding: 0 20px 10px 20px !important;
}
</style>
