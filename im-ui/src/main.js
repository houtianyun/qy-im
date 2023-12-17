import Vue from 'vue'
import App from './App'
import router from './router'
import ElementUI from 'element-ui';
import vuetify from './plugins/vuetify'
import 'element-ui/lib/theme-chalk/index.css';
import './assets/iconfont/iconfont.css';
import httpRequest from './api/httpRequest';
import * as socketApi from './api/wssocket';
import emotion from './api/emotion.js';
import element from './api/element.js';
import store from './store';
import config from "./assets/js/config";
import * as  enums from './api/enums.js';
import './utils/directive/dialogDrag';
import VueImageSwipe from "vue-image-swipe";
import "vue-image-swipe/dist/vue-image-swipe.css";

Vue.use(ElementUI);
Vue.use(VueImageSwipe);

// 挂载全局
Vue.prototype.$wsApi = socketApi;
Vue.prototype.$http = httpRequest // http请求方法
Vue.prototype.$emo = emotion; // emo表情
Vue.prototype.$elm = element; // 元素操作
Vue.prototype.$enums = enums; // 枚举
Vue.config.productionTip = false;
Vue.prototype.config = config;
Vue.config.productionTip = false;

import hljs from 'highlight.js';

import 'highlight.js/styles/atom-one-dark-reasonable.css' //样式
//创建v-highlight全局指令
Vue.directive('highlight', function (el) {
  let blocks = el.querySelectorAll('pre code');
  blocks.forEach((block) => {
    hljs.highlightBlock(block)
  })
})

import VueViewer from 'v-viewer'
import 'viewerjs/dist/viewer.css'
Vue.use(VueViewer);

new Vue({
  el: '#app',
  // 配置路由
  router,
  store,
  vuetify,
  render: h=>h(App)
})
