<template>
  <div class="say-main container">
    <div class="say-containner">
      <div class="cover">
        <img src="http://static.timery.xyz/image/shiyi/72d9854940be46579f098b49f9d9035c.jpg" alt="">
        <div class="info">说说</div>
        <a class="operateBtn" @click="handleShowAddTalk">
          <i class="el-icon-more"></i>
        </a>
      </div>
      <div class="contentBox">

      </div>
    </div>
    <add-talk :visible="addTalkVisible" @close="addTalkVisible = false"></add-talk>
  </div>
</template>

<script>
import AddTalk from "@/components/talk/AddTalk";
import HeadImage from "@/components/common/HeadImage";
import BatchFileUpload from "@/components/common/BatchFileUpload";

export default {
  name: "FriendActivity",
  components: {
    HeadImage,
    BatchFileUpload,
    AddTalk
  },
  data() {
    return {
      addTalkVisible: false,
    }
  },
  methods: {
    handleShowAddTalk() {
      this.addTalkVisible = true;
    }
  }
}
</script>

<style lang="scss">
.say-main {

  .say-containner {

    .cover {
      width: 72%;
      position: relative;
      margin: 0 auto;
      img {
        border-radius: 5px;
        width: 100%;
        height: 100%;
      }

      .info {
        position: absolute;
        top: 50%;
        left: 50%;
        font-size: 25px;
        font-weight: 700;
      }

      .operateBtn {
        position: absolute;
        top: 10px;
        right: 10px;
        display: inline-block;
        background-color: var(--say-background);
        padding: 0 5px;
        cursor: pointer;
      }
    }

    .contentBox {
      margin-top: 20px;
      width: 100%;
      color: var(--text-color);
    }
  }
}
</style>