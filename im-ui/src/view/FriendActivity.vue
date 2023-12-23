<template>
  <div class="say-main container">
    <div class="say-container">
      <div class="cover">
        <img src="http://static.timery.xyz/image/shiyi/72d9854940be46579f098b49f9d9035c.jpg" alt="">
        <div class="info">心语</div>
        <a class="operateBtn" @click="handleShowAddTalk">
          <i class="el-icon-more"></i>
        </a>
      </div>
      <div class="contentBox">
        <div class="sayItem" v-for="(item, index) in talkList" :key="index">
          <avatar :url="item.avatar" :userId="item.userId"></avatar>
          <div class="rightBox">
            <div class="nickname">
              {{ item.nickName }}
            </div>
            <p class="content" v-highlight v-html="item.content"></p>
            <v-row class="talk-images" v-if="item.imgUrls">
              <v-col
                  :cols="12" :sm="6" :md="4"
                  v-for="(img, idx) of item.imgUrls"
                  :key="idx"
              >
                <v-img
                    class="images-items"
                    :src="img"
                    aspect-ratio="1"
                    max-height="200"
                    @click.prevent="previewImg(img, item.imgUrls)"
                />
              </v-col>
            </v-row>
            <div class="bottomBox">
              <div v-if="item.address" class="address">
                <a>
                  <i class="el-icon-location-outline"></i> {{ item.address }}
                </a>
              </div>
              <span class="time">
                <a>
                  <i class="el-icon-time"></i> {{ item.createTime }}
                </a>
              </span>
              <div class="operate" ref="operate">
                <span class="like" v-if="!item.isLike" @click="sayLike(item)">
                  <i class="el-icon-star-on"></i> 赞
                </span>
                <span class="like" v-else @click="cancelLike(item)">
                  <i class="el-icon-star-on" style="color: yellow"></i> 取消
                </span>
                <span class="fgx"></span>
                <span class="commentBtn" @click="handleShowCommentBox(null, item.id, index)">
                  <i class="el-icon-chat-dot-square"></i> 评论
                </span>
                <span class="delBtn" v-if="item.isOwner">
                  <el-popconfirm
                      confirm-button-text='确认'
                      cancel-button-text='取消'
                      icon="el-icon-info"
                      icon-color="red"
                      title="确认删除当前动态吗？"
                      @confirm="delTalk(item, index)"
                  >
                    <el-button slot="reference" icon="el-icon-delete-solid" size="mini" circle @click.stop></el-button>
                  </el-popconfirm>

                </span>
                <span class="editBtn" v-if="item.isOwner" @click="editTalk(item)">
                  <i class="el-icon-edit"></i>
                </span>
                <span class="fgx"></span>
                <span class="setBtn">
                  <i class="el-icon-setting"></i>
                </span>
              </div>
              <a class="operateBtn" @click="handleShowOperate(index)">
                <i class="el-icon-more"></i>
              </a>
            </div>
          </div>
        </div>
        <pagination :totalPage="page.totalPage" :pageNo="page.pageNo" @changePage="handlePage"></pagination>
      </div>
    </div>
    <add-talk :visible.sync="addTalkVisible"
              v-if="addTalkVisible"
              @close="closeAddTalkDialog"
              @refresh="refreshTalkList"
              :talkId="curTalk.id">
    </add-talk>
<!--    <image-preview :img="images"></image-preview>-->
  </div>
</template>

<script>
import AddTalk from "@/components/talk/AddTalk";
import HeadImage from "@/components/common/HeadImage";
import BatchFileUpload from "@/components/common/BatchFileUpload";
import Avatar from "@/components/common/Avatar";
import Pagination from "@/components/pagination/Pagination";
/*import ImagePreview from "@/components/imagePreview/ImagePreview";*/

export default {
  name: "FriendActivity",
  components: {
    HeadImage,
    BatchFileUpload,
    AddTalk,
    Avatar,
    Pagination,
    /*ImagePreview*/
  },
  data() {
    return {
      addTalkVisible: false,
      page: {
        pageNo: 1,
        pageSize: 10,
        totalPage: 0,
      },
      talkList: [],
      images: {},
      lastIndex: null,
      curTalk: {}
    }
  },
  created() {
    this.pageQueryTalkList()
  },
  methods: {
    handleShowAddTalk() {
      this.addTalkVisible = true;
    },
    refreshTalkList() {
      this.page.pageNo = 1;
      this.page.totalPage = 0;
      this.talkList = [];
      this.pageQueryTalkList();
    },
    pageQueryTalkList() {
      this.$http({
        url: `/talk/pageQueryTalkList?pageNo=${this.page.pageNo}&pageSize=${this.page.pageSize}`,
        method: "get"
      }).then((data) => {
        this.talkList.push(...data.data)
        this.page.totalPage = (data.total -1) / this.page.pageSize + 1;
      })
    },
    handlePreviewImg(imgs) {
      this.images = {
        urls: imgs,
        time: new Date().getTime()
      }
    },
    previewImg(img, imgUrls) {
      this.$imagePreview({
        images: imgUrls,
        index: imgUrls.indexOf(img)
      });
    },
    handlePage(pageNo) {
      this.page.pageNo = pageNo;
      this.pageQueryTalkList();
    },
    handleShowOperate(index) {
      if (this.lastIndex != null && this.lastIndex != index) {
        this.$refs.operate[this.lastIndex].style.display = "none"
      }
      if (this.lastIndex == index) {
        if (this.$refs.operate[index].style.display == "block") {
          this.$refs.operate[index].style.display = "none"
        } else {
          this.$refs.operate[index].style.display = "block"
        }
      } else {
        this.$refs.operate[index].style.display = "block"
      }
      this.lastIndex = index
    },
    sayLike(talk) {
      talk.isLike = true
    },
    cancelLike(talk) {
      talk.isLike = false
    },
    handleShowCommentBox(comment, sayId, index) {

    },
    delTalk(talk, index) {
      this.$http({
        url: "/talk/delete",
        method: 'delete',
        data: {id: talk.id}
      }).then((data) => {
        this.$message.success("删除成功");
        //this.talkList.splice(index, 1);
        this.refreshTalkList();
        this.lastIndex = null;
      }).finally(() => {

      })
    },
    editTalk(talk) {
      this.curTalk = talk;
      this.addTalkVisible = true;
    },
    closeAddTalkDialog() {
      this.addTalkVisible = false;
      this.curTalk = {};
    },
    delBtn(e){
      e.stopPropagation();
    },
  }
}
</script>

<style lang="scss">
.col-xl,
.col-xl-auto,
.col-xl-12,
.col-xl-11,
.col-xl-10,
.col-xl-9,
.col-xl-8,
.col-xl-7,
.col-xl-6,
.col-xl-5,
.col-xl-4,
.col-xl-3,
.col-xl-2,
.col-xl-1,
.col-lg,
.col-lg-auto,
.col-lg-12,
.col-lg-11,
.col-lg-10,
.col-lg-9,
.col-lg-8,
.col-lg-7,
.col-lg-6,
.col-lg-5,
.col-lg-4,
.col-lg-3,
.col-lg-2,
.col-lg-1,
.col-md,
.col-md-auto,
.col-md-12,
.col-md-11,
.col-md-10,
.col-md-9,
.col-md-8,
.col-md-7,
.col-md-6,
.col-md-5,
.col-md-4,
.col-md-3,
.col-md-2,
.col-md-1,
.col-sm,
.col-sm-auto,
.col-sm-12,
.col-sm-11,
.col-sm-10,
.col-sm-9,
.col-sm-8,
.col-sm-7,
.col-sm-6,
.col-sm-5,
.col-sm-4,
.col-sm-3,
.col-sm-2,
.col-sm-1,
.col,
.col-auto,
.col-12,
.col-11,
.col-10,
.col-9,
.col-8,
.col-7,
.col-6,
.col-5,
.col-4,
.col-3,
.col-2,
.col-1 {
  width: 100%;
  padding: 4px !important;
}

.say-main {

  .say-container {

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
        background-color: #6CC6CB;
        padding: 0 5px;
        cursor: pointer;
      }
    }

    .contentBox {
      width: 72%;
      margin: 20px auto;
      color: #5fb878;

      .sayItem {
        padding: 10px;
        display: flex;
        margin-bottom: 10px;
        border-radius: 5px;
        position: relative;
        background-color: #ffffff;
        overflow: hidden;

        .avatar {
          img {
            vertical-align: top;
          }
        }

        .rightBox {
          padding-left: 10px;
          display: flex;
          flex-direction: column;
          width: 100%;

          .nickname {
            text-align: left;
            color: #f56c6c;

            /*svg {
              width: 18px;
              height: 18px;
              vertical-align: -3px;
            }*/
          }

          .content {
            text-align: left;
            margin-top: 10px;
          }

          .talk-images {
            margin-top: 4px;
            margin-right: 2px;

            .images-items {
              cursor: pointer;
              border-radius: 4px;
            }
          }

          .bottomBox {
            margin-top: 20px;
            position: relative;
            display: flex;
            flex-direction: column;

            .address {
              display: flex;
              margin-right: auto;
              color: #5597bd;
              margin-bottom: 10px;
              font-size: 14px;
            }

            .time {
              display: flex;
              margin-right: auto;
              font-size: 14px;
            }

            .operateBtn {
              position: absolute;
              right: 20px;
              bottom: 0px;
              display: inline-block;
              background-color: #6CC6CB;
              padding: 0 5px;
              cursor: pointer;
            }

            .operate {
              position: absolute;
              right: 55px;
              bottom: -8px;
              background-color: #4b5153;
              padding: 8px;
              border-radius: 5px;
              display: none;

              &::after {
                content: '';
                position: absolute;
                right: -15px;
                bottom: 10px;
                width: 0;
                height: 0;
                border-width: 8px;
                border-style: solid;
                border-color: transparent transparent transparent #4b5153;
              }

              span {
                padding: 0 10px;
                color: #fff;
                position: relative;
                cursor: pointer;

                &:hover {
                  color: #f56c6c;
                }
              }

              .like::after {
                content: "";
                width: 2px;
                height: 100%;
                background-color: #373d40;
                position: absolute;
                right: 0;
              }
            }
          }
        }
      }
    }
  }
}
</style>