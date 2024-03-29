<template>
	<div class="chat-group-side">
		<div class="group-side-search">
			<el-input placeholder="搜索群成员" v-model="searchText">
				<el-button slot="append" icon="el-icon-search"></el-button>
			</el-input>
		</div>
		<el-scrollbar class="group-side-scrollbar">
			<div class="group-side-member-list">
				<div class="group-side-invite">
					<div class="invite-member-btn" title="邀请好友进群聊" @click="showAddGroupMember=true">
						<i class="el-icon-plus"></i>
					</div>
					<div class="invite-member-text">邀请</div>
					<add-group-member
              :visible="showAddGroupMember"
              :groupId="group.id"
              :members="groupMembers"
              :isTemplate="group.isTemplate"
              :templateGroupId="group.templateGroupId"
              :selectableCharacters="selectableCharacters"
              @reload="$emit('reload')"
              @close="showAddGroupMember=false"></add-group-member>
				</div>
        <div v-if="group.isTemplate === 1" class="member-info">
          <div class="view-member-info-btn" title="群聊成员信息" @click="openGroupMemberInfoDialog">
            <i class="el-icon-search"></i>
          </div>
          <div class="view-member-text">查看</div>
          <el-dialog
              width="25%"
              title="模板群聊成员信息"
              :visible.sync="groupMemberVisible"
              :before-close="closeGroupMemberInfoDialog">
            <el-scrollbar style="height:400px;">
              <div v-for="(groupMember, index) in groupMembers" :key="index" v-show="!groupMember.quit">
                <template-group-member class="r-group-member" :member="groupMember"></template-group-member>
              </div>
            </el-scrollbar>
          </el-dialog>
        </div>
				<div v-for="(member) in groupMembers" :key="member.id">
					<group-member class="group-side-member" v-show="!member.quit && member.aliasName.startsWith(searchText)" :member="member"
					 :showDel="false"></group-member>
				</div>
			</div>
			<el-divider content-position="center"></el-divider>
			<el-form labelPosition="top" class="group-side-form" :model="group">
				<el-form-item label="群聊名称">
					<el-input v-model="group.name" disabled maxlength="20"></el-input>
				</el-form-item>
				<el-form-item label="群主">
					<el-input :value="ownerName" disabled></el-input>
				</el-form-item>
				<el-form-item label="群公告">
					<el-input v-model="group.notice" disabled type="textarea" maxlength="1024" placeholder="群主未设置"></el-input>
				</el-form-item>
				<el-form-item label="备注">
					<el-input v-model="group.remark" :disabled="!editing" placeholder="群聊的备注仅自己可见" maxlength="20"></el-input>
				</el-form-item>
				<el-form-item label="我在本群的昵称">
					<el-input v-model="group.aliasName" :disabled="!editing || group.isTemplate === 1" placeholder="xx" maxlength="20"></el-input>
				</el-form-item>
        <el-form-item label="群成员名称显示" v-if="group.isTemplate === 1">
          <el-switch
              style="display: block"
              v-model="myGroupMemberInfo.showNickName"
              active-color="#13ce66"
              inactive-color="#ff4949"
              active-text="显示"
              inactive-text="关闭"
              @change="showNickNameChange"
              :disabled="!editing">
          </el-switch>
        </el-form-item>

				<div class="btn-group">
					<el-button v-show="editing" type="success" @click="onSaveGroup()">提交</el-button>
					<el-button v-show="!editing" type="primary" @click="editing=!editing">编辑</el-button>
					<el-button type="danger" v-show="!isOwner" @click="onQuit()">退出群聊</el-button>
				</div>
			</el-form>
		</el-scrollbar>

	</div>
</template>

<script>
	import AddGroupMember from '../group/AddGroupMember.vue';
	import GroupMember from '../group/GroupMember.vue';
	import TemplateGroupMember from "@/components/group/TemplateGroupMember";

	export default {
		name: "chatGroupSide",
		components: {
			AddGroupMember,
			GroupMember,
      TemplateGroupMember
		},
		data() {
			return {
				searchText: "",
				editing: false,
				showAddGroupMember: false,
        selectableCharacters: [],
        groupMemberVisible: false,
        showNickName: false,
			}
		},
		props: {
			group: {
				type: Object
			},
			groupMembers: {
				type: Array
			},
      myGroupMemberInfo: {
        type: Object
      }
		},
		methods: {
      onClose() {
				this.$emit('close');
			},
			loadGroupMembers() {
				this.$http({
					url: `/group/members/${this.group.id}`,
					method: "get"
				}).then((members) => {
					this.groupMembers = members;
				})
			},
      onSaveGroup() {
				let vo = this.group;
				vo.showNickName = this.myGroupMemberInfo.showNickName;
				this.editing = false;
				this.$http({
					url: "/group/modify",
					method: "put",
					data: vo
				}).then((group) => {
					this.$store.commit("updateGroup", group);
					this.$emit('reload');
					this.$message.success("修改成功");
				})
			},
      onQuit() {
				this.$confirm('退出群聊后将不再接受群里的消息，确认退出吗？', '确认退出?', {
					confirmButtonText: '确定',
					cancelButtonText: '取消',
					type: 'warning'
				}).then(() => {
					this.$http({
						url: `/group/quit/${this.group.id}`,
						method: 'delete'
					}).then(() => {
						this.$store.commit("removeGroup", this.group.id);
						this.$store.commit("activeGroup", -1);
						this.$store.commit("removeGroupChat", this.group.id);
					});
				})
			},
      querySelectableTemplateCharacter(group) {
			  if (group == null || group === {}) {
          return false;
        }
			  if (group.id === undefined || group.id === null) {
			    return false
        }
        if (group.templateGroupId === undefined || group.templateGroupId === null) {
          return false
        }
        let paramVO = {
          groupId: group.id,
          templateGroupId: group.templateGroupId
        }
        this.$http({
          url: "/templateCharacter/findSelectableTemplateCharacter",
          method: 'post',
          data: paramVO
        }).then(result => {
          this.selectableCharacters = result;
        }).finally(() =>{

        })
      },
      closeGroupMemberInfoDialog() {
        this.groupMemberVisible = false;
      },
      openGroupMemberInfoDialog() {
        this.groupMemberVisible = true;
      },
      showNickNameChange() {
        this.$emit("change", this.myGroupMemberInfo.showNickName);
      }
		},
		computed: {
			ownerName() {
				let member = this.groupMembers.find((m) => m.userId == this.group.ownerId);
				return member && member.aliasName;
			},
			isOwner() {
			  this.querySelectableTemplateCharacter(this.group);
				return this.group.ownerId == this.$store.state.userStore.userInfo.id;
			}
			
		},
  }
</script>

<style lang="scss">
	.chat-group-side {
		position: relative;

		.group-side-member-list {
			padding: 10px;
			display: flex;
			align-items: center;
			flex-wrap: wrap;
			font-size: 16px;
			text-align: center;

			.group-side-member {
				margin-left: 15px;
			}

			.group-side-invite {
				display: flex;
				flex-direction: column;
				align-items: center;
				width: 50px;
				margin-left: 15px;

				.invite-member-btn {
					width: 100%;
					height: 50px;
					line-height: 50px;
					border: #cccccc solid 1px;
					font-size: 25px;
					cursor: pointer;
					box-sizing: border-box;

					&:hover {
						border: #aaaaaa solid 1px;
					}
				}

				.invite-member-text {
					font-size: 16px;
					text-align: center;
					width: 100%;
					height: 30px;
					line-height: 30px;
					white-space: nowrap;
					text-overflow: ellipsis;
					overflow: hidden
				}
			}

      .member-info {
        display: flex;
        flex-direction: column;
        align-items: center;
        width: 50px;
        margin-left: 15px;

        .view-member-info-btn {
          width: 100%;
          height: 50px;
          line-height: 50px;
          border: #cccccc solid 1px;
          font-size: 25px;
          cursor: pointer;
          box-sizing: border-box;

          &:hover {
            border: #aaaaaa solid 1px;
          }
        }

        .view-member-text {
          font-size: 16px;
          text-align: center;
          width: 100%;
          height: 30px;
          line-height: 30px;
          white-space: nowrap;
          text-overflow: ellipsis;
          overflow: hidden
        }
      }
		}

		.group-side-form {
			text-align: left;
			padding: 10px;
			height: 30%;

			.el-form-item {
				margin-bottom: 12px;

				.el-form-item__label {
					padding: 0;
					line-height: 30px;
				}

				.el-textarea__inner {
					min-height: 100px !important;
				}
			}

			.btn-group {
				text-align: center;
			}
		}

	}
</style>
