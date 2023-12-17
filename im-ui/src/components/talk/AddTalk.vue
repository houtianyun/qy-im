<template>
  <el-dialog title="发布轻说" :visible.sync="visible"  width="44%" :before-close="handleClose">
    <el-form :rules="rules" ref="ruleForm"  label-width="auto"
             :model="form" class="form-box">
      <el-form-item label="内容：" prop="content" label-width="120px" class="form-item">
        <el-input class="form-content" type="textarea" :autosize="{ minRows: 6 }" placeholder="请输入说说内容"
                  v-model="form.content">
        </el-input>
      </el-form-item>
      <el-form-item label="公开范围：" prop="scope" label-width="120px" class="form-item">
        <el-select v-model="form.scope" placeholder="请选择">
          <el-option
              v-for="item in options"
              :key="item.value"
              :label="item.label"
              :value="item.value">
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="是否匿名：" prop="anonymous" label-width="120px" class="form-item">
        <el-radio v-model="form.anonymous" :label="true">是</el-radio>
        <el-radio v-model="form.anonymous" :label="false">否</el-radio>
      </el-form-item>
      <el-form-item label="关联图片：" prop="imgUrl" label-width="120px" class="form-item">
        <batch-image-upload class="form-content"
                            :action="imageAction"
                            :showLoading="true"
                            :maxSize="maxSize"
                            :fileTypes="['image/jpeg', 'image/png', 'image/jpg','image/webp', 'image/gif']"
                            @success="handleUploadImageSuccess"
                            @remove="handleRemove"
                            ref="imageUploader">
        </batch-image-upload>
      </el-form-item>
      <el-form-item>

          <el-button type="primary" @click="submitForm('ruleForm')">发布</el-button>
          <el-button @click="resetForm('ruleForm')">清空</el-button>

      </el-form-item>
    </el-form>
  </el-dialog>
</template>

<script>
import BatchFileUpload from "@/components/common/BatchFileUpload";
import BatchImageUpload from "@/components/common/BatchImageUpload";
import HeadImage from "@/components/common/HeadImage";

export default {
  name: "AddTalk",
  components: {
    HeadImage,
    BatchFileUpload,
    BatchImageUpload
  },
  props: {
    visible: {
      type: Boolean
    },
  },
  data() {
    return {
      maxSize: 5 * 1024 * 1024,
      imageList: [],
      uploadHeaders: {"accessToken":sessionStorage.getItem('accessToken')},
      fileTypes: ['image/jpeg', 'image/png', 'image/jpg','image/webp', 'image/gif'],
      rules: {
        content: [
          { required: true, message: '请输入内容', trigger: 'blue' }
        ],
        scope: [
          { required: true, message: '请选择公开范围', trigger: 'blue' }
        ],
      },
      form: {
        content: '',
        scope: 2,
        anonymous: false,
      },
      options: [
        {value: 1, label: "私密"},
        {value: 2, label: "好友可见"},
        {value: 3, label: "群友可见"},
        {value: 4, label: "公开"}
      ]
    }
  },
  methods: {
    handleClose() {
      this.$emit("close");
    },
    clearImages () {
      //this.$refs.imageUploader.clearImages();
      this.$refs.imageUploader.$emit("removeImages")
    },
    handleUploadImageSuccess(res) {
      this.imageList.push({ url: res.data.originUrl});
    },
    handleRemove(file) {
      this.imageList.forEach((item, index) => {
        if (item.url === file.response.data.originUrl) {
          this.imageList.splice(index, 1);
        }
      });
    },
    submitForm(formName) {
      let talkParam = this.form;
      talkParam.imgUrls = this.imageList.map(obj => {return obj.url});
      console.log("talkParam", talkParam);
      this.$refs[formName].validate((valid) => {
        if (valid) {
          this.$http({
            url: "/talk/add",
            method: 'post',
            data: talkParam
          }).then((data) => {
            this.$message.success("发布成功");
            this.resetForm('ruleForm');
            this.imageList = [];
            this.clearImages();
            this.$emit("refresh");
          }).finally(() => {
            this.$emit("close");
          })
        }
      });
    },
    resetForm(formName) {
      this.$refs[formName].resetFields();
    },
  },
  computed: {
    imageAction() {
      return `${process.env.VUE_APP_BASE_API}/image/upload`;
    },
  },
}
</script>

<style lang="scss">
.form-box {
  .form-item {
    .el-form-item__content {
      text-align: left;
    }
  }
}

</style>