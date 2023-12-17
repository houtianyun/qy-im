<template>
  <div class="avatar" @click="showUserInfo($event)">
    <el-avatar :src="url ? url : require('../../assets/anonymousAvatar.jpeg')" fit="fill" alt=""></el-avatar>
  </div>
</template>

<script>
export default {
  name: "Avatar",
  data() {
    return {}
  },
  props: {
    userId:{
      type: Number
    },
    url: {
      type: String,
    }
  },
  methods:{
    showUserInfo(e){
      if(this.userId && this.userId>0){
        this.$http({
          url: `/user/find/${this.userId}`,
          method: 'get'
        }).then((user) => {
          this.$store.commit("setUserInfoBoxPos",e);
          this.$store.commit("showUserInfoBox",user);
        })
      }
    }
  }
}
</script>

<style scoped lang="scss">
.avatar {
  cursor: pointer;
}
</style>