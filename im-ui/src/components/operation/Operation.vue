<template>
  <el-dialog class="setting" title="操作" :visible.sync="visible"  width="500px" :before-close="handleClose">
    <el-container>
      <el-tabs v-moel="activeTab" tab-position="left" style="height: 360px;" @tab-click="handleTabClick">
        <el-tab-pane label="消息通知" name="notify">
          <div>待开发</div>
        </el-tab-pane>
        <el-tab-pane label="修改密码" name="modifyPwd">
          <el-form :model="pwdForm" status-icon :rules="rules" ref="pwdForm" label-width="80px">
            <el-form-item label="旧密码" prop="oldPassword">
              <el-input type="password" v-model="pwdForm.oldPassword" autocomplete="off"></el-input>
            </el-form-item>
            <el-form-item label="新密码" prop="newPassWord">
              <el-input type="password" v-model="pwdForm.newPassWord" autocomplete="off"></el-input>
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPwd">
              <el-input type="password" v-model="pwdForm.confirmPwd" autocomplete="off"></el-input>
            </el-form-item>
            <el-form-item>
              <el-button type="success" @click="resetPwd('pwdForm')">确认</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="项目源码" name="sourceCode">
          <div>
            <a href="https://gitee.com/houtianyun/qy-im" target="_blank">https://gitee.com/houtianyun/qy-im</a>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-container>
  </el-dialog>
</template>

<script>
export default {
  name: "Operation",
  props: {
    visible: {
      type: Boolean
    }
  },
  data() {
    let checkPassword = (rule, value, callback) => {
      if (value === '') {
        return callback(new Error('请输入密码'));
      }
      callback();
    };

    let checkConfirmPassword = (rule, value, callback) => {
      if (value === '') {
        return callback(new Error('请输入密码'));
      }
      if (value !== this.pwdForm.newPassWord) {
        return callback(new Error('两次密码输入不一致'));
      }
      callback();
    };

    return {
      activeTab: '',
      pwdForm: {
        oldPassword: '',
        newPassWord: '',
        confirmPwd: ''
      },
      rules: {
        oldPwd: [{
          validator: checkPassword,
          trigger: 'blur'
        }],
        newPwd: [{
          validator: checkPassword,
          trigger: 'blur'
        }],
        confirmPwd: [{
          validator: checkConfirmPassword,
          trigger: 'blur'
        }],
      }
    }
  },
  methods: {
    handleClose() {
      this.$emit("close");
    },
    handleTabClick(tab, event) {
      this.activeTab = tab.name;
    },
    resetPwd(formName) {
      this.$refs[formName].validate((valid) => {
        if (valid) {
          this.$http({
            url: "/user/modifyPassword",
            method: 'post',
            data: this.pwdForm
          }).then((data) => {
            this.$message.success("修改成功!");
            this.$wsApi.closeWebSocket();
            sessionStorage.removeItem("token");
            location.href = "/";
          }).catch(() => {
          })
        }
      });
    }
  }
}
</script>

<style lang="scss">

</style>