<template>
  <div id="app">
		<router-view></router-view>
  </div>
</template>

<script>


export default {
  name: 'App',
  components: {
  },
  created() {
    this.$http({
      url: "/website/report",
      method: "post",
    }).then((data) => {
    })
  }
}
</script>

<style>
 @import './assets/style/global.css';
 
#app {
  font-family: 'Avenir', Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #2c3e50;
  position: absolute;
  height: 100%;
  width: 100%;
}

.el-message {
 z-index: 99999999 !important;
}
</style>
