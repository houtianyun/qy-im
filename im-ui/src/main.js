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

Vue.use(ElementUI);
 
// 挂载全局
Vue.prototype.$wsApi = socketApi;
Vue.prototype.$http = httpRequest // http请求方法
Vue.prototype.$emo = emotion; // emo表情
Vue.prototype.$elm = element; // 元素操作
Vue.prototype.$enums = enums; // 枚举
Vue.config.productionTip = false;
Vue.prototype.config = config;
Vue.config.productionTip = false;

new Vue({
  el: '#app',
  // 配置路由
  router,
  store,
  vuetify,
  render: h=>h(App)
})
