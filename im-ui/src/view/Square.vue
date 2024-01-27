<template>
  <el-container class="im-box-square">
    <el-aside width="250px" class="l-square-box">
      <el-scrollbar class="l-square-list">
        <div v-for="(community,index) in communityList" :key="index">
          <community-item :community="community" :active="index === activeIndex"
                          @click.native="handleActiveItem(community,index)"></community-item>
        </div>
      </el-scrollbar>
    </el-aside>
    <el-main class="community-box">
      <router-view></router-view>
    </el-main>
  </el-container>
</template>

<script>
import CommunityItem from "@/components/square/CommunityItem";

export default {
  name: "Square",
  components: {
    CommunityItem
  },
  data() {
    return {
      communityList: [
        {name: "模板群聊", sort: 1, route: '/home/square/templateGroup', headImage: require('../assets/image/QiQiaoBan.jpg')},
        {name: "朋友圈", sort: 2, route: '/home/square/friendActivity', headImage: require('../assets/image/CircleOfFriends.png')}
      ],
      activeIndex: -1,
    }
  },
  methods: {
    handleActiveItem(community, index) {
      //console.log("community", community)
      this.activeIndex = index;
      this.$router.push(community.route);
    }
  }
}
</script>

<style lang="scss">
.im-box-square {
  .l-square-box {
    display: flex;
    flex-direction: column;
    border: #dddddd solid 1px;
    background: white;
  }

  .community-box {
    margin-top: -10px;
  }
}
</style>