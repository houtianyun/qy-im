<template>
	<div class="template-group-member">
    <div class="avatar">
      <head-image :url="member.userAvatar"></head-image>
    </div>
    <div class="text">
      <div>{{member.nickName}}</div>
    </div>
    <div class="template-character-name">
      <div>{{member.aliasName}}</div>
    </div>
    <div class="avatar">
      <head-image :url="member.headImage"></head-image>
    </div>
	</div>
</template>

<script>
	import HeadImage from "../common/HeadImage.vue";
	export default{
		name: "templateGroupMember",
		components:{HeadImage},
		data(){
			return {};
		},
		props:{
			member:{
				type: Object,
				required: true
			},
		},
		methods:{

		}
	}
</script>

<style lang="scss">
.template-group-member {
  height: 65px;
  display: flex;
  margin-bottom: 1px;
  position: relative;
  padding-left: 15px;
  align-items: center;
  padding-right: 5px;
  background-color: #fafafa;
  white-space: nowrap;

  .avatar {
    display: flex;
    justify-content: center;
    align-items: center;
    width: 45px;
    height: 45px;
  }

  .text {
    margin-left: 15px;
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: space-around;
    height: 100%;
    flex-shrink: 0;
    overflow: hidden;
    text-align: left;
  }

  .template-character-name {
    margin-right: 15px;
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: space-around;
    height: 100%;
    flex-shrink: 0;
    overflow: hidden;
    text-align: right;
  }
}
</style>
