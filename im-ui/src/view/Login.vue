<template>
	<div class="login-view" ref="loginView" :style="backImgStyle">
<!--    <div id="particles"></div>-->
    <el-form :model="loginForm"  status-icon :rules="rules" ref="loginForm"  label-width="60px" class="web-ruleForm" @keyup.enter.native="submitForm('loginForm')">
      <div class="login-brand">輕語</div>
      <el-form-item label="用户名" prop="username">
        <el-input type="username" v-model="loginForm.userName" autocomplete="off"></el-input>
      </el-form-item>
      <el-form-item label="密码" prop="password">
        <el-input type="password" v-model="loginForm.password" autocomplete="off"></el-input>
      </el-form-item>
      <el-form-item prop="code">
        <el-input
            v-model="loginForm.code"
            auto-complete="off"
            placeholder="验证码"
            style="width: 120px"
        >
        </el-input>
        <div class="login-code">
          <img :src="codeUrl" @click="getCode" class="login-code-img"/>
        </div>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="submitForm('loginForm')">登陆</el-button>
        <el-button @click="resetForm('loginForm')">清空</el-button>
      </el-form-item>
      <div class="register-box">
        <div class="register" @click="toRegister()">立即注册</div>
        <div class="forget-pwd">忘记密码</div>
        <p style="clear:both;"></p>
      </div>
      <div class="social-login">
        <div class="social-login-title">社交账号登录</div>
        <div class="social-login-wrapper">
          <el-image
              style="width: 30px; height: 30px"
              :src="require('@/assets/image/qq.png')"
              @click="qqLogin"
              />
        </div>
      </div>
    </el-form>

    <div class="printer" @click="getGuShi()" v-if="showMediaType === 'picture'">
      <printer :printerInfo="printerInfo">
        <template slot="paper" slot-scope="scope">
          <h3>
            {{ scope.content }}<span class="cursor">|</span>
          </h3>
        </template>
      </printer>
    </div>

    <div class="videoContainer" v-if="showMediaType === 'video'">
      <video class="fullscreenVideo"
             ref="videoPlayer"
             id="videoPlayer"
             :src="videoUrl"
             autoplay="autoplay"
             muted
             type="video/mp4">
      </video>
      <div class="player unsound" @click="playVideo()">
      </div>
    </div>

    <div class="audioContainer" v-if="showMediaType === 'audio'" :style="audioBackImgStyle">
      <div ref="waveform_Ref" class="waveformRef"></div>
      <div class="playOrPause play-audio" @click="playMusic">
<!--        <el-button
            v-if="!playing"
            size="small"
            icon="el-icon-video-play"
            @click="playMusic"
            circle
        ></el-button
        >
        <el-button
            v-if="playing"
            size="small"
            icon="el-icon-video-pause"
            @click="playMusic"
            circle
        >
        </el-button>-->
      </div>
      <div class="song-name">{{songName}}</div>
    </div>

<!--    <div class="visitorMap">
    </div>-->
    <div class="media-type" v-if="mediaInfo.showMedia">
      <div class="video-media" @click="getMediaByType('video')" :style="videoStyle">
        <img src="../assets/image/video.png">
      </div>
      <div class="audio-media" @click="getMediaByType('audio')" :style="audioStyle">
        <img src="../assets/image/audio.png">
      </div>
      <div class="picture-media" @click="getMediaByType('picture')" :style="pictureStyle">
        <img src="../assets/image/picture.png">
      </div>
    </div>
  </div>
</template>

<script>
import printer from "@/components/common/printer";
import WaveSurfer from "wavesurfer.js";
//import particlesJson from '../assets/json/particles.json'

	export default {
		name: "login",
    components: {
      printer,
    },
		data() {
      let pattern = new RegExp("[`~!@#$^&*()=|{}':;',\\[\\].<>《》/?~！@#￥……&*（）——|{}【】‘；：”“'。，、？]");
      let reg =/[\u4E00-\u9FA5]|[\uFE30-\uFFA0]/gi;
			let checkUsername = (rule, value, callback) => {
				console.log("checkUsername");
				if (!value) {
					return callback(new Error('请输入用户名'));
				}
        if (pattern.test(value)) {
          callback(new Error('不允许输入特殊字符'));
        }
        if (reg.test(value)) {
          callback(new Error('用户名不能有中文'));
        }
				callback();
			};
			let checkPassword = (rule, value, callback) => {
				console.log("checkPassword");
				if (value === '') {
					callback(new Error('请输入密码'));
				}
				callback();

			};
			return {
        codeUrl: '',
				loginForm: {
          userName: '',
					password: '',
          code: '',
          uuid: ''
				},
				rules: {
          userName: [{
						validator: checkUsername,
						trigger: 'blur'
					}],
					password: [{
						validator: checkPassword,
						trigger: 'blur'
					}],
          code: [
            { required: true, trigger: "change", message: "请输入验证码" },
            { min: 1, max: 4, message: "请输入4位验证码", trigger: "change"}
          ]
				},
        mediaInfo: {},
        showMediaType: null,
        curMediaType: null,
        isMuted: true,
        silence: true,
        videoUrl: '',
        backImgStyle: {
          backgroundImage: "url(" + require("@/assets/image/girl.jpg") + ")",
          backgroundSize: "100% 100%",
          backgroundRepeat: "no-repeat",
          position: "relative",
          display: "flex",
          justifyContent: "space-around",
          width: "100%",
          height: "100%"
        },
        audioBackImgStyle: {
          position: "fixed",
          backgroundSize: "100% 100%",
          width: "100%",
          height: "100%",
          overflow: "hidden",
          zIndex: "55",
          //backgroundPosition: "center center",
          backgroundRepeat: "no-repeat",
          backgroundAttachment: "fixed",
          backgroundImage: "url(" + require("@/assets/image/background-image.jpg") + ")"
        },
        wavesurfer: null,
        playing: false,
        isPlayOver: false,
        songName: '',
        audioBtn: null,
        printerInfo: "你看对面的青山多漂亮",
        guShi: {
          "content": "",
          "origin": "",
          "author": "",
          "category": ""
        },
        videoStyle: {
          opacity: "0.5"
        },
        audioStyle: {
          opacity: "0.5"
        },
        pictureStyle: {
          opacity: "0.5"
        },
			}
		},
    created() {
      this.getCode();
      this.getPlayMediaMaterial();
    },
    methods: {
      playMusic() {
        //this.wavesurfer.play();
        this.audioBtn=document.querySelector(".playOrPause");
        if (!this.playing) {
          this.audioBtn.classList.remove("play-audio");
          this.audioBtn.classList.add("pause-audio");
        } else {
          this.audioBtn.classList.remove("pause-audio");
          this.audioBtn.classList.add("play-audio");
        }
        this.wavesurfer.playPause.bind(this.wavesurfer)();
        this.playing = !this.playing;
        this.isPlayOver = false;
      },
      getPlayMediaMaterial() {
        console.log("获取媒体素材")
        this.mediaInfo.type = this.curMediaType;
        this.$http({
          url: "/website/getPlayMediaMaterial",
          method: "post",
          data: this.mediaInfo
        }).then((data) => {
          this.mediaInfo = data;
          if (data.showMedia) {
            if (data.type === 'video') {
              this.showMediaType = 'video';
              this.videoUrl = data.url;
              this.addEndedEvent();
            } else if (data.type === 'picture') {
              this.videoUrl = '';
              this.showMediaType = 'picture';
              this.backImgStyle.backgroundImage = "url(" + data.url + ")";
              setTimeout(this.getPlayMediaMaterial, data.displayDuration * 1000)

              this.getGuShi();
            } else if (data.type === "audio") {
              this.videoUrl = '';
              this.showMediaType = 'audio';
              this.audioBackImgStyle.backgroundImage = "url(" + data.coverImage + ")";
              this.songName = data.title;
              this.createAudioWaveSurfer(data.url);
            }
          } else {
            this.videoUrl = '';
          }
        })
      },
      getGuShi() {
        let that = this;
        let xhr = new XMLHttpRequest();
        xhr.open('get', 'https://v1.jinrishici.com/all.json');
        xhr.onreadystatechange = function () {
          if (xhr.readyState === 4) {
            that.guShi = JSON.parse(xhr.responseText);
            that.printerInfo = that.guShi.content;
          }
        };
        xhr.send();
      },
      getMediaByType(mediaType) {
        console.log("mediaType", mediaType)
        if (mediaType==='video') {
          this.videoStyle.opacity = this.curMediaType === 'video' ? "0.5" : '1';
          this.audioStyle.opacity = "0.5";
          this.pictureStyle.opacity = "0.5";
          this.mediaInfo.type = this.curMediaType === 'video' ? null : mediaType;
        } else if (mediaType==='audio') {
          this.videoStyle.opacity = "0.5";
          this.audioStyle.opacity = this.curMediaType === 'audio' ? "0.5" : '1';
          this.pictureStyle.opacity = "0.5";
          this.mediaInfo.type = this.curMediaType === 'audio' ? null : mediaType;
        } else if (mediaType==='picture') {
          this.videoStyle.opacity = "0.5";
          this.audioStyle.opacity = "0.5";
          this.pictureStyle.opacity = this.curMediaType === 'picture' ? "0.5" : '1';
          this.mediaInfo.type = this.curMediaType === 'picture' ? null : mediaType;
        }
        this.curMediaType = this.mediaInfo.type;
      },
      addEndedEvent() {
        this.$nextTick(function () {
          let targetEle = document.getElementById("videoPlayer")
          targetEle.addEventListener('ended', this.getPlayMediaMaterial, false)
        });
      },
      getCode() {
        this.$http({
          url: "captchaImage",
          method: "get"
        }).then((result) => {
          this.codeUrl = "data:image/gif;base64," + result['img'];
          this.loginForm.uuid = result['uuid'];
        })
      },
			submitForm(formName) {
				this.$refs[formName].validate((valid) => {
					if (valid) {
						this.$http({
								url: "/login",
								method: 'post',
								data: this.loginForm
							})
							.then((data) => {
                // 保存密码到cookie(不安全)
                this.setCookie('username',this.loginForm.userName);
                this.setCookie('password',this.loginForm.password);
                // 保存token
                sessionStorage.setItem("accessToken",data.accessToken);
                sessionStorage.setItem("refreshToken",data.refreshToken);
                this.$message.success("登陆成功");
                this.$router.push("/home/chat");
							})

					}
				});
			},
			resetForm(formName) {
				this.$refs[formName].resetFields();
			},
			// 获取cookie、
			getCookie(name) {
				let reg = new RegExp("(^| )" + name + "=([^;]*)(;|$)");
				let arr = document.cookie.match(reg)
			    if (arr){
					 return unescape(arr[2]);
				}
			    return '';
			 },
			  // 设置cookie,增加到vue实例方便全局调用
			 setCookie (name, value, expiredays) {
			    let exdate = new Date();
			    exdate.setDate(exdate.getDate() + expiredays);
			    document.cookie = name + "=" + escape(value) + ((expiredays == null) ? "" : ";expires=" + exdate.toGMTString());
			  },
			  // 删除cookie
			  delCookie (name) {
            let exp = new Date();
            exp.setTime(exp.getTime() - 1);
            let cval = this.getCookie(name);
            if (cval != null){
            document.cookie = name + "=" + cval + ";expires=" + exp.toGMTString();
				  }
			  },
      qqLogin() {
        //保留当前路径
        if (
            navigator.userAgent.match(
                /(iPhone|iPod|Android|ios|iOS|iPad|Backerry|WebOS|Symbian|Windows Phone|Phone)/i
            )
        ) {
          // eslint-disable-next-line no-undef
          QC.Login.showPopup({
            appId: this.config.QQ_APP_ID,
            redirectURI: this.config.QQ_REDIRECT_URI
          });
        } else {
          window.open(
              "https://graph.qq.com/oauth2.0/show?which=Login&display=pc&client_id=" +
              +this.config.QQ_APP_ID +
              "&response_type=token&scope=all&redirect_uri=" +
              this.config.QQ_REDIRECT_URI,
              "_self"
          );
        }
      },
      toRegister() {
        this.$router.push("/register");
      },
      playVideo() {
        let video=document.querySelector("video");
        let btn=document.querySelector(".player");
        if(video.muted){
          video.muted=false;/*不沉默*/
          btn.classList.remove("unsound");/*为btn元素移除unmute这个class name，即close图片消失*/
          btn.classList.add("sound");/*为btn元素添加mute这个class name，即open图片出现*/
        } else{
          video.muted=true;/*沉默*/
          btn.classList.remove("sound");/*open图片消失*/
          btn.classList.add("unsound");/*close图片出现*/
        }
      },
      createAudioWaveSurfer(url) {
        if (this.wavesurfer == null) {
          this.$nextTick(() => {
            this.wavesurfer = WaveSurfer.create({
              // 波形图的容器
              container: this.$refs.waveform_Ref,
              // 已播放波形的颜色
              //progressColor: "red",
              // 未播放波形的颜色
              waveColor: "lightgrey",
              // 波形图的高度，单位为px
              height: 800,
              // 是否显示滚动条，默认为false
              scrollParent: false,
              // 波形的振幅（高度），默认为1
              barHeight: 0.8,
              // 波形条的圆角
              barRadius: 2,
              // 波形条的宽度
              barWidth: 1,
              // 波形条间的间距
              barGap: 3,
              // 播放进度光标条的颜色
              cursorColor: "red",
              // 播放进度光标条的宽度，默认为1
              cursorWidth: 4,
              // 播放进度颜色
              progressColor: "blue",
              // 波形容器的背景颜色
              //backgroundColor: "yellow",
              // 音频的播放速度
              audioRate: "1",
              // （与区域插件一起使用）启用所选区域的循环
              // loopSelection:false
            });
            this.wavesurfer.on('finish', this.audioPlayOver);
            this.wavesurfer.load(url);
          })
        } else {
          this.wavesurfer.load(url);
        }
      },
      audioPlayOver() {
        console.log("播放完成")
        if (!this.isPlayOver) {
          this.isPlayOver = true;
          this.playing = !this.playing;
          this.audioBtn.classList.remove("pause-audio");
          this.audioBtn.classList.add("play-audio");
          // 重置波形
          this.wavesurfer.stop();
        }
        this.getPlayMediaMaterial();
      }
		},
		mounted() {
			this.loginForm.userName = this.getCookie("username");
			// cookie存密码并不安全，暂时是为了方便
			this.loginForm.password = this.getCookie("password");

      // 监听视频播放
      /*this.$refs.videoPlayer.addEventListener("ended", () => {
        console.log("播放完成");
      });*/
      /*this.$nextTick(() => {

      });*/

      //require('particles.js')
      // eslint-disable-next-line no-undef
      //particlesJS('particles', particlesJson)
		}
	}
</script>

<style scoped lang="scss">
  /*#particles{
    position: absolute;
    width: 100%;
    height: 100%;
    background-color: rgb(135, 183, 255);
    background-repeat: no-repeat;
    background-size: cover;
    background-position: 50% 50%;
    z-index: 60;
  }*/

	.login-view {
		/*position: relative;
		display: flex;
		justify-content: space-around;
		width: 100%;
		height: 100%;
    background-image: url("../assets/image/background-image.jpg");
		background-size: cover;*/

		.web-ruleForm {
			height: 446px;
			padding: 20px;
			margin-top: 150px ;
			background: rgba(255,255,255,.75);
			box-shadow: 0px 0px  1px #ccc;
			border-radius: 5px;
			overflow: hidden;
	    z-index: 100;
			
			.login-brand {
				line-height: 30px;
				margin: 10px 0 20px 0;
				font-size: 30px;
				font-weight: 600;
				letter-spacing: 2px;
				text-transform: uppercase;
				text-align: center;
        background: linear-gradient(to right, red, orange, yellow, green, cyan, blue,  purple);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
        animation:  hue 3s linear infinite;
			}

      .login-code {
        width: 90px;
        height: 38px;
        float: right;
        margin-right: 25px;
        margin-left: 10px;
        img {
          cursor: pointer;
          vertical-align: middle;
        }

        .login-code-img {
          height: 38px;
        }
      }

      @keyframes hue {
        0% {
          filter: hue-rotate(0deg);
        }
        100% {
          filter: hue-rotate(360deg);
        }
      }
			
			.register-box {
				line-height: 40px;

        .register {
          float: left;
          cursor:pointer;
        }

        .forget-pwd {
          float: right;
          cursor:pointer;
        }
			}

      .social-login {
        .social-login-title {
          margin-top: 5px;
          color: #b5b5b5;
          font-size: 0.75rem;
          text-align: center;
        }
        .social-login-title::before {
          content: "";
          display: inline-block;
          background-color: #d8d8d8;
          width: 60px;
          height: 1px;
          margin: 0 12px;
          vertical-align: middle;
        }
        .social-login-title::after {
          content: "";
          display: inline-block;
          background-color: #d8d8d8;
          width: 60px;
          height: 1px;
          margin: 0 12px;
          vertical-align: middle;
        }

        .social-login-wrapper {
          margin-top: 1rem;
          font-size: 2rem;
          text-align: center;
        }
      }
		}

    .printer {
      position: absolute;
      cursor: pointer;
      color: white;
      background: rgba(0, 0, 0, 0.5);
      border-radius: 10px;
      padding: 10px;
      bottom: 20px;
      left: 50%;
      transform: translateX(-50%); /* 移动元素本身50% */
      z-index: 60;
    }

    .videoContainer{
      position: fixed;
      width: 100%;
      height: 100%;
      overflow: hidden;
      z-index: 50;

      .fullscreenVideo {
        width: 100%;
        height: 100%;
        object-fit: fill;
      }

      .player{
        position: absolute;
        background-position: center;
        background-repeat: no-repeat;
        background-size: cover;
        width: 36px;
        height: 36px;
        cursor: pointer;
        bottom: 20px;
        left: 15px;
        z-index: 60;
        cursor:pointer;
      }

      .unsound{
        background-image:url("../assets/image/close.png");
      }

      .sound{
        background-image:url("../assets/image/open.png");
      }

    }

    .videoContainer:before{
      content: "";
      position: absolute;
      width: 100%;
      height: 100%;
      display: block;
      z-index: 50;
      top: 0;
      left: 0;
      /*background: rgba(25,29,34,.65);*/
    }

    .audioContainer {
      /*position: fixed;
      width: 100%;
      height: 100%;
      overflow: hidden;
      z-index: 55;
      background-image: url("../assets/image/background-image.jpg");*/

      .waveformRef {
        width: 100%;
        height: 100%;
        white-space: nowrap;
      }

      .playOrPause {
        width: 36px;
        height: 36px;
        position: absolute;
        background-position: center;
        background-repeat: no-repeat;
        background-size: cover;
        bottom: 20px;
        left: 15px;
        z-index: 60;
        cursor:pointer;
      }

      .play-audio {
        background-image: url("../assets/image/play.png");
      }

      .pause-audio {
        background-image: url("../assets/image/pause.png");
      }

      .song-name {
        position: absolute;
        bottom: 20px;
        z-index: 60;
        margin: 0 auto;
        font-size: xx-large;
        left: 50%;
        transform: translateX(-50%); /* 移动元素本身50% */
        text-shadow: 0 0 5px #fff,
        0 0 10px #fff,
        0 0 15px #fff,
        0 0 20px #00a67c,
        0 0 35px #00a67c,
        0 0 40px #00a67c,
        0 0 50px #00a67c,
        0 0 75px #00a67c;
      }
    }

    .media-type {
      position: absolute;
      right: 20px;
      bottom: 20px;
      z-index: 105;

      div {
         img {
           width: 36px;
           height: 36px;
         }
        margin-top: 10px;
        cursor:pointer;
      }

      .video-media {

      }

      .audio-media {

      }

      .picture-media {

      }
    }
  }

	
</style>
