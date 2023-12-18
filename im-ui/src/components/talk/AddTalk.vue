<template>
  <el-dialog title="发布动态" :visible.sync="visible"  width="44%" :before-close="handleClose">
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
      <el-form-item label="角色选择：" prop="character" label-width="120px" class="form-item">
        <span class="character-item" v-on:click="openCharacterChooseDialog">
          <el-avatar :fit="fit" size="medium" icon="el-icon-user-solid" :src="form.avatar">
          </el-avatar>
        </span>
        <span class="nick-name">{{form.nickName}} <el-button @click="removeCharacter" class="del-btn" v-if="form.nickName" type="danger" icon="el-icon-delete" size="mini" circle></el-button></span>
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
    <el-dialog
        width="50%"
        title="选择角色"
        :visible.sync="chooseCharacterDialogVisible"
        :before-close="closeChooseCharacterDialog"
        append-to-body>
      <div class="agm-container">
        <div class="agm-l-box">
          <el-input width="200px" placeholder="搜索模板群聊" class="input-with-select" v-model="searchText" >
            <el-button slot="append" icon="el-icon-search" ></el-button>
          </el-input>
          <el-scrollbar style="height:400px;">
            <div v-for="(templateGroup,index) in templateGroupList" :key="templateGroup.id"
                 v-show="templateGroup.groupName.startsWith(searchText)"
                 class="template-group-box">
              <template-group-item :templateGroup="templateGroup" class="group-item-left"></template-group-item>
              <div class="group-item-right">
                <el-button :type="groupActiveIndex === index ? 'success' : ''" icon="el-icon-check" circle
                           @click="queryTemplateCharacter(templateGroup, index)" ></el-button>
              </div>
              <p style="clear:both;"></p>
            </div>
          </el-scrollbar>
        </div>
        <div class="agm-r-box">
          <el-input width="200px" placeholder="搜索模板人物" class="input-with-select" v-model="characterSearchText" >
            <el-button slot="append" icon="el-icon-search" ></el-button>
          </el-input>
          <el-scrollbar style="height:400px;">
            <div class="template-character-box" v-for="(templateCharacter,index) in templateCharacterList"
                 :key="templateCharacter.id" v-show="templateCharacter.name.startsWith(characterSearchText)">
              <template-character-item class="character-item-left" :templateCharacter = "templateCharacter"></template-character-item>
              <div class="character-item-right">
                <el-button :type="characterActiveIndex === index ? 'success' : ''" icon="el-icon-check" circle
                           @click="chooseTemplateCharacter(templateCharacter, index)"></el-button>
              </div>
              <p style="clear:both;"></p>
            </div>
          </el-scrollbar>
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
			<el-button @click="closeChooseCharacterDialog()">取 消</el-button>
			<el-button type="primary" @click="handleOk()">确 定</el-button>
		</span>
    </el-dialog>
  </el-dialog>
</template>

<script>
import BatchFileUpload from "@/components/common/BatchFileUpload";
import BatchImageUpload from "@/components/common/BatchImageUpload";
import HeadImage from "@/components/common/HeadImage";
import TemplateGroupItem from "@/components/group/TemplateGroupItem";
import TemplateCharacterItem from "@/components/group/TemplateCharacterItem";

export default {
  name: "AddTalk",
  components: {
    HeadImage,
    BatchFileUpload,
    BatchImageUpload,
    TemplateGroupItem,
    TemplateCharacterItem
  },
  props: {
    visible: {
      type: Boolean
    },
  },
  data() {
    return {
      fit: 'fill',
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
        nickName: '',
        avatar: ''
      },
      options: [
        {value: 1, label: "私密"},
        {value: 2, label: "好友可见"},
        {value: 3, label: "群友可见"},
        {value: 4, label: "公开"}
      ],
      chooseCharacterDialogVisible: false,
      searchText: "",
      characterSearchText: "",
      templateGroupList: [],
      templateCharacterList: [],
      groupActiveIndex: -1,
      characterActiveIndex: -1,
      templateCharacter: {},
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
      this.form.avatar = '';
      this.form.nickName = '';
    },
    openCharacterChooseDialog() {
      this.chooseCharacterDialogVisible = true;
      this.queryTemplateGroup();
    },
    closeChooseCharacterDialog() {
      this.chooseCharacterDialogVisible = false;
      this.templateGroupList = [];
      this.templateCharacterList = [];
      this.groupActiveIndex = -1;
      this.characterActiveIndex = -1;
    },
    queryTemplateGroup(){
      this.templateGroupList = []
      this.$http({
        url: "/templateGroup/list",
        method: 'get',
        params: ''
      }).then(data => {
        this.templateGroupList = data;
      })
    },
    queryTemplateCharacter(templateGroup, index) {
      this.groupActiveIndex = index;
      this.characterActiveIndex = -1;
      this.$http({
        url: `/templateCharacter/list/${templateGroup["id"]}`,
        method: 'get'
      }).then((data) => {
        this.templateCharacterList = data;
      });
    },
    chooseTemplateCharacter(templateCharacter, index) {
      this.characterActiveIndex = index;
      this.templateCharacter = templateCharacter;
    },
    handleOk() {
      this.form.nickName = this.templateCharacter.name;
      this.form.avatar = this.templateCharacter.avatar;
      this.closeChooseCharacterDialog();
    },
    removeCharacter() {
      this.form.avatar = '';
      this.form.nickName = '';
    }
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

  .character-item, nick-name {
    display: inline-block;
    height: 36px;
    line-height: 36px;
    vertical-align:middle;
    margin-right: 10px;
  }

  .nick-name {
    .del-btn {
      margin-left: 30px;
    }
  }
}

.agm-container {
  display: flex;

  .agm-l-box {
    flex: 1;
    border: #dddddd solid 1px;
    box-sizing: content-box;

    .template-group-box {
      width: 100%;

      .group-item-left {
        float: left;
      }
      .group-item-right {
        float: right;
        margin-right: 10px;
        height: 65px;
        line-height: 65px;
      }
    }
  }

  .agm-r-box {
    flex: 1;
    border: #dddddd solid 1px;

    .template-character-box {
      width: 100%;

      .character-item-left {
        float: left;
      }
      .character-item-right {
        float: right;
        margin-right: 10px;
        height: 65px;
        line-height: 65px;
      }
    }
  }
}

</style>