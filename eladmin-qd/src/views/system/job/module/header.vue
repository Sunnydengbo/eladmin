<template>
  <div
    v-if="crud.props.searchToggle"
  >
    <el-input
      v-model="query.name"
      class="filter-item"
      clearable
      placeholder="输入岗位名称搜索"
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
    <el-select
      v-model="query.enabled"
      class="filter-item"
      clearable
      placeholder="状态"
      size="small"
      style="width: 90px"
      @change="crud.toQuery"
    >
      <el-option v-for="item in dict.dict.job_status" :key="item.value" :label="item.label" :value="item.value" />
    </el-select>
    <rrOperation />
  </div>
</template>

<script>
import { header } from '@crud/crud'
import rrOperation from '@crud/RR.operation'

export default {
  components: { rrOperation },
  mixins: [header()],
  props: {
    dict: {
      type: Object,
      required: true
    },
    permission: {
      type: Object,
      required: true
    }
  }
}
</script>
