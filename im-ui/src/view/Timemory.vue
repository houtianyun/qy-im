<template>
  <div class="body">
    <div id="clock">
      <p class="date">{{ clock.date }}</p>
      <p class="time">{{ clock.time }}</p>
      <p class="text">{{ printerInfo }}</p>
    </div>
  </div>
</template>
<script>
export default {
  name:'',
  props:[''],
  data () {
    return {
      clock: {
        time: '',
        date: ''
      },
      week: ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'],
      printerInfo: "你看对面的青山多漂亮",
      guShi: {
        "content": "",
        "origin": "",
        "author": "",
        "category": ""
      },
    };
  },
  mounted() {
    setInterval(this.updateTime, 1);
  },
  created() {
    this.getGuShi();
  },
  methods: {
    updateTime() {
      let cd = new Date();
      this.clock.time = this.zeroPadding(cd.getHours(), 2)
          + ':' + this.zeroPadding(cd.getMinutes(), 2)
          + ':' + this.zeroPadding(cd.getSeconds(), 2)
          + '.' + this.zeroPadding(cd.getMilliseconds(), 3);
      this.clock.date = this.zeroPadding(cd.getFullYear(), 4)
          + '-' + this.zeroPadding(cd.getMonth()+1, 2)
          + '-' + this.zeroPadding(cd.getDate(), 2)
          + ' ' + this.week[cd.getDay()];
    },
    zeroPadding(num, digit) {
      let zero = '';
      for(let i = 0; i < digit; i++) {
        zero += '0';
      }
      return (zero + num).slice(-digit);
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
  },
  watch: {}
}
</script>
<style lang='scss' scoped>
.body {
  height: 100%;
  background: #0f3854;
  background: radial-gradient(ellipse at center, #0a2e38 0%, #000000 70%);
  background-size: 100%;
}

p {
  margin: 0;
  padding: 0;
}

#clock {
  font-family: "Share Tech Mono", monospace;
  color: #ffffff;
  text-align: center;
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  color: #daf6ff;
  text-shadow: 0 0 20px #0aafe6, 0 0 20px rgba(10, 175, 230, 0);
}
#clock .time {
  letter-spacing: 0.05em;
  font-size: 80px;
  padding: 5px 0;
}
#clock .date {
  letter-spacing: 0.1em;
  font-size: 24px;
}
#clock .text {
  letter-spacing: 0.1em;
  font-size: 12px;
  padding: 20px 0 0;
}
</style>