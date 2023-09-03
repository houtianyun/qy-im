<template>
  <el-dialog title="邀请好友" :visible.sync="visible"  width="50%" :before-close="handleClose">
    <div class="agm-container">
      <div class="agm-l-box">
        <el-input width="200px" placeholder="搜索好友" class="input-with-select" v-model="searchText">
          <el-button slot="append" icon="el-icon-search"></el-button>
        </el-input>
        <el-scrollbar style="height:400px;">
          <div v-for="(friend,index) in friends" :key="friend.id" v-show="friend.nickName.startsWith(searchText)">
            <friend-item :showDelete="false" @click.native="handleSwitchCheck(friend)"
             :friend="friend" :index="index" :active="index === activeIndex">
              <el-checkbox :disabled="friend.disabled" @click.native.stop="" class="agm-friend-checkbox" v-model="friend.isCheck"
               size="medium"></el-checkbox>
            </friend-item>
          </div>
        </el-scrollbar>
      </div>
      <div class="agm-r-box">
        <div class="agm-select-tip"> 已勾选{{checkCount}}位好友</div>
        <el-scrollbar style="height:400px;">
          <div v-for="(friend,index) in friends" :key="friend.id">
            <friend-item v-if="friend.isCheck && !friend.disabled" :friend="friend" :index="index" :active="false"
                         :isTemplate="isTemplate" @del="handleRemoveFriend(friend)"
                         @select="selectCharacter(friend, index)">
            </friend-item>
          </div>
        </el-scrollbar>
      </div>
    </div>
    <span slot="footer" class="dialog-footer">
      <el-button @click="handleClose()">取 消</el-button>
      <el-button type="primary" @click="handleOk()">确 定</el-button>
    </span>
    <el-dialog
        width="30%"
        title="请选择模板人物"
        :visible.sync="selectTemplateCharacterVisible"
        :before-close="closeSelectCharacter"
        append-to-body>
      <div>
        <el-input width="200px" placeholder="搜索模板人物" class="input-with-select" v-model="characterSearchText">
          <el-button slot="append" icon="el-icon-search"></el-button>
        </el-input>
      </div>
      <el-scrollbar style="height:400px;">
        <div v-for="(templateCharacter, index) in selectableCharacters" :key="index"
             v-show="templateCharacter.name.startsWith(characterSearchText)">
          <template-character-item class="character-item-left" :templateCharacter="templateCharacter"></template-character-item>
          <div class="character-item-right">
            <el-button :disabled="!templateCharacter.selectable ||templateCharacter.choosed "
                       :type="characterActiveIndex === index ? 'success' : ''"
                       icon="el-icon-check"
                       circle
                       @click="chooseTemplateCharacter(templateCharacter, index)"></el-button>
          </div>
          <p style="clear:both;"></p>
        </div>
      </el-scrollbar>
      <span slot="footer" class="dialog-footer">
      <el-button @click="closeSelectCharacter">取 消</el-button>
      <el-button type="primary" @click="chooseTemplateCharacterOk">确 定</el-button>
    </span>
    </el-dialog>
  </el-dialog>
</template>

<script>
	import FriendItem from '../friend/FriendItem.vue';
  import TemplateCharacterItem from "@/components/group/TemplateCharacterItem";
  import HeadImage from '../common/HeadImage.vue';

	export default {
		name: "addGroupMember",
		components: {
			FriendItem,
      TemplateCharacterItem,
      HeadImage
		},
		data() {
			return {
				searchText: "",
        characterSearchText: "",
				activeIndex: -1,
				friends: [],
        selectTemplateCharacterVisible: false,
        characterActiveIndex: -1,
        activeTemplateCharacter: {},  // 当前选中的模板人物
        curSelectedFriend: {},
        selectedFriendIndex: -1,
			}
		},
    props: {
      visible: {
        type: Boolean
      },
      groupId: {
        type: Number
      },
      members: {
        type: Array
      },
      isTemplate: {
        type: Number
      },
      templateGroupId: {
        type: Number
      },
      selectableCharacters: {
        type: Array
      }
    },
		methods: {
			handleClose() {
        for (let i= 0; i < this.selectableCharacters.length; i++) {
          if (this.selectableCharacters[i].selectable) {
            this.selectableCharacters[i].choosed = false;
          }
        }
				this.$emit("close");
			},
			handleOk() {
			  let returnFlag = false;
        if (this.isTemplate === 1) {
          this.friends.forEach((f) => {
            if (f.isCheck && !f.disabled) {
              if (f.templateCharacterId === undefined || f.templateCharacterId === null) {
                returnFlag = true;
              }
            }
          })
        }
        if (returnFlag) {
          this.$message.warning("请为用户分配模板人物角色");
          return false
        }

				let inviteVO = {
					groupId: this.groupId,
					friendIds: [],
          characterInviteVOList: [],
          isTemplate: this.isTemplate
				}
				this.friends.forEach((f) => {
					if (f.isCheck && !f.disabled) {
						inviteVO.friendIds.push(f.id);
						if (this.isTemplate === 1) {
              let obj = {
                friendId: f.id,
                templateCharacterId: f.templateCharacterId,
                templateCharacterAvatar: f.templateCharacterAvatar,
                templateCharacterName: f.templateCharacterName
              }
              inviteVO.characterInviteVOList.push(obj);
            }
					}
				})
        console.log("inviteVO", inviteVO);
				if (inviteVO.friendIds.length > 0) {
					this.$http({
						url: "/group/invite",
						method: 'post',
						data: inviteVO
					}).then(() => {
						this.$message.success("邀请成功");
					}).finally(() => {
            this.$emit("reload");
            this.$emit("close");
          })
				}
			},
			handleRemoveFriend(friend) {
				friend.isCheck = false;
				// 将已选择的模板人物的不可选标识去掉
				if (friend.choosedCharacterIndex !== undefined &&  friend.choosedCharacterIndex !== null) {
          this.selectableCharacters[friend.choosedCharacterIndex].choosed = false;
        }
        friend.choosedCharacterIndex = null;
        friend.templateCharacterId = null;
        friend.templateCharacterAvatar = null;
        friend.templateCharacterName = null;
			},
			handleSwitchCheck(friend) {
        // 取消好友选择，需要将好友已选的模板人物变成可选状态
        if (!friend.disabled) {
          if (friend.isCheck) {
            if (friend.choosedCharacterIndex !== undefined &&  friend.choosedCharacterIndex !== null) {
              this.selectableCharacters[friend.choosedCharacterIndex].choosed = false;
            }
            friend.choosedCharacterIndex = null;
            friend.templateCharacterId = null;
            friend.templateCharacterAvatar = null;
            friend.templateCharacterName = null;
          }
					friend.isCheck = !friend.isCheck
				}
			},
      selectCharacter(friend, index) {
        this.selectTemplateCharacterVisible = true
        this.selectedFriendIndex = index;
        this.curSelectedFriend = friend;
      },
      closeSelectCharacter() {
        this.characterActiveIndex = -1;
        this.activeTemplateCharacter = {};
        this.selectTemplateCharacterVisible = false
      },
      chooseTemplateCharacter(templateCharacter, index) {
			  // 记录选择的模板人物数组下标
        this.characterActiveIndex = index;
        // 记录选择的模板人物
        this.activeTemplateCharacter = templateCharacter;
      },
      chooseTemplateCharacterOk() {
			  if (this.characterActiveIndex === -1) {
          this.$message.warning("请选择一位模板人物");
			    return false
        }
        // 标识当前模板人物已被选择
        this.selectableCharacters[this.characterActiveIndex].choosed = true;
        let friendIndex = this.selectedFriendIndex;
        // 当前好友之前已选择过模板人物，需要把之前选择的模板人物的不可选标识去掉
        if (this.friends[friendIndex].choosedCharacterIndex !== undefined
            && this.friends[friendIndex].choosedCharacterIndex !== null) {
          this.selectableCharacters[this.friends[friendIndex].choosedCharacterIndex].choosed = false;
        }
        // 记录被选择的模板人物的位置下标
        this.friends[friendIndex].choosedCharacterIndex = this.characterActiveIndex;
        this.friends[friendIndex].templateCharacterId = this.activeTemplateCharacter.id;
        this.friends[friendIndex].templateCharacterAvatar = this.activeTemplateCharacter.avatar;
        this.friends[friendIndex].templateCharacterName = this.activeTemplateCharacter.name;

        //this.curSelectedFriend.choosedCharacterIndex = this.characterActiveIndex;
        //this.curSelectedFriend.templateCharacterId = this.activeTemplateCharacter.id;
        //this.curSelectedFriend.templateCharacterAvatar = this.activeTemplateCharacter.avatar;
        //this.curSelectedFriend.templateCharacterName = this.activeTemplateCharacter.name;
        //this.$set(this.friends, this.selectedFriendIndex, this.curSelectedFriend);

        //let newFriends = Object.assign([], this.friends);
        //console.log("friends", this.friends);
        //console.log("newFriends", newFriends);

        this.characterActiveIndex = -1;
        this.activeTemplateCharacter = {};
        this.selectTemplateCharacterVisible = false;
        //this.$forceUpdate();
      },
		},
		computed: {
			checkCount() {
				return this.friends.filter((f) => f.isCheck && !f.disabled).length;
			}
		},
		watch: {
			visible: function(newData, oldData) {
				if (newData) {
					this.friends = [];
					this.$store.state.friendStore.friends.forEach((f) => {
						let friend = JSON.parse(JSON.stringify(f))
						let m = this.members.filter((m) => !m.quit)
							.find((m) => m.userId == f.id);
						console.log(m);
						if (m) {
							// 好友已经在群里
							friend.disabled = true;
							friend.isCheck = true
						} else {
							friend.disabled = false;
							friend.isCheck = false;
						}
						friend.templateCharacterAvatar = '';
						this.friends.push(friend);
					})
				}
			}
		}

	}
</script>

<style lang="scss">
	.agm-container {
		display: flex;

		.agm-l-box {
			flex: 1;
			border: #dddddd solid 1px;

			.el-checkbox {
				display: flex;
				align-items: center;

				//修改选中框的大小
				.el-checkbox__inner {
					width: 20px;
					height: 20px;

					//修改选中框中的对勾的大小和位置
					&::after {
						height: 12px;
						left: 7px;
					}
				}

				//修改点击文字颜色不变
				.el-checkbox__input.is-checked+.el-checkbox__label {
					color: #333333;
				}

				.el-checkbox__label {
					line-height: 20px;
					padding-left: 8px;
				}
			}

			.agm-friend-checkbox {
				margin-right: 20px;
			}
		}

		.agm-r-box {
			flex: 1;
			border: #dddddd solid 1px;

			.agm-select-tip {
				text-align: left;
				height: 40px;
				line-height: 40px;
				text-indent: 5px;
			}
		}
	}

  .character-item-left {
    float: left;
  }

  .character-item-right {
    float: right;
    margin-right: 10px;
    height: 65px;
    line-height: 65px;
  }
</style>
