<template>
  <div style="display: inline-block">
    <el-dialog
      :before-close="cancel"
      :close-on-click-modal="false"
      :title="title"
      :visible.sync="dialog"
      append-to-body
      width="500px"
      @close="cancel"
    >
      <el-form ref="form" :model="form" :rules="rules" label-width="88px" size="small">
        <el-form-item label="旧密码" prop="oldPass">
          <el-input v-model="form.oldPass" auto-complete="on" style="width: 370px;" type="password" />
        </el-form-item>
        <el-form-item label="新密码" prop="newPass">
          <el-input v-model="form.newPass" auto-complete="on" style="width: 370px;" type="password" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPass">
          <el-input v-model="form.confirmPass" auto-complete="on" style="width: 370px;" type="password" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="text" @click="cancel">取消</el-button>
        <el-button :loading="loading" type="primary" @click="doSubmit">确认</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import store from '@/store'
import { updatePass } from '@/api/system/user'

export default {
  data() {
    const confirmPass = (rule, value, callback) => {
      if (value) {
        if (this.form.newPass !== value) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      } else {
        callback(new Error('请再次输入密码'))
      }
    }
    return {
      loading: false, dialog: false, title: '修改密码', form: { oldPass: '', newPass: '', confirmPass: '' },
      rules: {
        oldPass: [
          { required: true, message: '请输入旧密码', trigger: 'blur' }
        ],
        newPass: [
          { required: true, message: '请输入新密码', trigger: 'blur' },
          { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
        ],
        confirmPass: [
          { required: true, validator: confirmPass, trigger: 'blur' }
        ]
      }
    }
  },
  methods: {
    cancel() {
      this.resetForm()
    },
    doSubmit() {
      this.$refs['form'].validate((valid) => {
        if (valid) {
          this.loading = true
          updatePass(this.form).then(res => {
            this.resetForm()
            this.$notify({
              title: '密码修改成功，请重新登录',
              type: 'success',
              duration: 1500
            })
            setTimeout(() => {
              store.dispatch('LogOut').then(() => {
                location.reload() // 为了重新实例化vue-router对象 避免bug
              })
            }, 1500)
          }).catch(err => {
            this.loading = false
            console.log(err.response.data.message)
          })
        } else {
          return false
        }
      })
    },
    resetForm() {
      this.dialog = false
      this.$refs['form'].resetFields()
      this.form = { oldPass: '', newPass: '', confirmPass: '' }
    }
  }
}
</script>

<style scoped>

</style>
