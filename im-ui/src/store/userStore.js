import {USER_STATE} from "../api/enums.js"
import http from '../api/httpRequest.js'

export default {
	
	state: {
		userInfo: {
			
		},
		state: USER_STATE.FREE 
	},

	mutations: {
		setUserInfo(state, userInfo) {
			// 切换用户后，清理缓存
			// if(userInfo.id != state.userInfo.id){
			// 	console.log("用户切换")
			// 	this.commit("resetChatStore");
			// }
			state.userInfo = userInfo
		},
		setUserState(state, userState) {
			state.state = userState;
		},
		clear(state){
			state.userInfo = {};
			state.state = USER_STATE.FREE;
		}
	},
	actions:{
		loadUser(context){
			return new Promise((resolve, reject) => {
				http({
					url: '/user/self',
					method: 'GET'
				}).then((userInfo) => {
					context.commit("setUserInfo",userInfo);
					resolve();
				}).catch((res)=>{
					reject(res);
				});
			})
		}
	}
}