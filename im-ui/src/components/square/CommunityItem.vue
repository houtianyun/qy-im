<template>
  <div class="item" :class="active ? 'active' : ''">
    <div class="avatar">
      <head-image :url="community.headImage"> </head-image>
    </div>
    <div class="text">
      <div>{{community.name}}</div>
    </div>
  </div>
</template>

<script>
import HeadImage from '../common/HeadImage.vue';

export default {
  name: "CommunityItem",
  components: {
    HeadImage
  },
  data() {
    return {}
  },
  props: {
    community: {
      type: Object
    },
    active: {
      type: Boolean
    }
  }
}
</script>

<style lang="scss">
.item {
  height: 65px;
  display: flex;
  margin-bottom: 1px;
  position: relative;
  padding-left: 15px;
  align-items: center;
  padding-right: 5px;
  background-color: #fafafa;
  white-space: nowrap;

  &:hover {
   background-color: #eeeeee;
  }

  &.active {
   background-color: #dddddd;
  }

  .avatar {
    width: 45px;
    height: 45px;
  }

  .text {
    display: flex;
    flex-direction: column;
    justify-content: space-around;
    flex: 1;
    margin-left: 15px;
    height: 100%;
    flex-shrink: 0;
    overflow: hidden;
    text-align: left;
  }
}
</style>