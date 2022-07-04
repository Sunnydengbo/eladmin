<template>
  <div v-if="crud.props.searchToggle">
    <el-input
      v-model="query.blurry"
      class="filter-item"
      clearable
      placeholder="请输入你要搜索的内容"
      size="small"
      style="width: 200px;"
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
</template>

<script>
import { header } from '@crud/crud'
import rrOperation from '@crud/RR.operation'

export default {
  components: { rrOperation },
  mixins: [header()]
}
</script>
