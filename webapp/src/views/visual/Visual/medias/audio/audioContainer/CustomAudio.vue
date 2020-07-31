<template>
  <div v-loading="audioStruct.waiting" class="di main-wrap">
    <audio
      ref="myAudio"
      class="shadow"
      :src="url"
      preload="auto"
      @play="play"
      @error="error"
      @pause="pause"
      @timeupdate="timeUpdate"
      @loadedmetadata="loadAudioData"
    />

    <div class="audioOut">
      <el-card class="mainAudio myCard">
        <el-row key="1" type="flex" justify="center" class="row-bg">
          <el-col :span="2">
            <el-button
              type="primary"
              :icon="audioStruct.playing ?'iconfont icon-zanting':'iconfont icon-ziyuan74'"
              size="medium"
              class="playButton"
              @click="playPause"
            />
          </el-col>
          <el-col :span="6" class="hidden-md-and-down">
            <span type="info" class="timeInfo">{{ audioStruct.curTime | formatSecond }}/{{ audioStruct.maxTime | formatSecond }}</span>
          </el-col>
          <el-col :span="13" class="hidden-sm-and-down">
            <el-slider
              v-model="sliderTime"
              :format-tooltip="timeLineToolTip"
              class="audioSlider"
              @change="changeCurTime"
            />
          </el-col>
          <el-col :span="2" :class="['mutedKey', 'hidden-sm-and-down']">
            <el-button
              type="primary"
              icon="iconfont icon-ziyuan73"
              :class="[audioStruct.muted ? 'gray' : 'light']"
              size="medium"
              @click="mute"
            />
          </el-col>
          <el-col :span="2" class="hidden-md-and-down">
            <el-button
              :href="url"
              download="audio.wav"
              icon="el-icon-bottom"
              type="primary"
              class="download"
              target="_blank"
              @click="downAudio(url)"
            />
          </el-col>
        </el-row>
      </el-card>
    </div>
  </div>
</template>

<script>
import 'element-ui/lib/theme-chalk/display.css';

function formatSeconds(second) {
  const secondType = typeof second;
  if (secondType === 'number' || secondType === 'string') {
    second = parseInt(second, 10);
    const mimute = Math.floor(second / 60);
    second -= mimute * 60;
    return `${(`0${mimute}`).slice(-2)}:${(`0${second}`).slice(-2)}`;
  }
  return '00:00';
}

export default {
  filters: {
    formatSecond(second = 0) {
      return formatSeconds(second);
    },
  },
  props: {
    URL: {
      type: String,
      required: true,
    },
  },
  data() {
    return {
      url: this.URL || '',
      audioStruct: {
        curTime: 0,
        maxTime: 0,
        playing: false,
        muted: false,
        waiting: true,
      },
      mutedShow: false,
      sliderTime: 0,
      volume: 100,
    };
  },
  watch: {
    URL() {
      this.url = this.URL;
      this.audioStruct.playing = false;
      this.audioStruct.curTime = 0;
      this.$refs.myAudio.currentTime = 0;
    },
  },
  methods: {
    downAudio(param) {
      const filename = 'audio.wav';
      fetch(param, {
        headers: new Headers({
          Origin: location.origin,
        }),
        mode: 'cors',
      })
        .then(res => res.blob())
        .then(blob => {
          const blobUrl = window.URL.createObjectURL(blob);
          this.download(blobUrl, filename);
          window.URL.revokeObjectURL(blobUrl);
        });
    },
    download(href, filename) {
      const a = document.createElement('a');
      a.download = filename;
      a.href = href;
      document.body.appendChild(a);
      a.click();
      a.remove();
    },
    mute() {
      this.$refs.myAudio.muted = !this.$refs.myAudio.muted;
      this.audioStruct.muted = this.$refs.myAudio.muted;
    },
    timeLineToolTip(index = 0) {
      index = parseInt(this.audioStruct.maxTime / 100 * index, 10);
      return formatSeconds(index);
    },
    changeVolume(index = 0) {
      this.$refs.myAudio.volume = index / 100;
      this.volume = index;
    },
    changeCurTime(index) {
      this.$refs.myAudio.currentTime  = parseInt(index / 100 * this.audioStruct.maxTime, 10);
    },
    playPause() {
      return this.audioStruct.playing ? this.pPlay() : this.sPlay();
    },
    sPlay() {
      this.$refs.myAudio.play(this.$refs.myAudio.currentTime);
    },
    pPlay() {
      this.$refs.myAudio.pause();
    },
    pause() {
      this.audioStruct.playing = false;
    },
    error() {
      this.audioStruct.waiting = true;
    },
    play() {
      this.audioStruct.playing = true;
      this.audioStruct.loading = false;
    },
    timeUpdate(val) {
      this.audioStruct.curTime = val.target.currentTime;
      this.sliderTime = parseInt(this.audioStruct.curTime / this.audioStruct.maxTime * 100, 10);
    },
    loadAudioData(val) {
      this.audioStruct.waiting = false;
      this.audioStruct.maxTime = parseInt(val.target.duration, 10);
    },
  },
};
</script>

<style lang="less" scoped>
  .audioSlider {
    padding-left: 3px;
  }

  .di {
    display: block;
  }

  .download {
    float: left;
    color: #8f8ad7;
  }

  .shadow {
    display: none;
  }

  /deep/ .el-button {
    padding-top: 4px;
    padding-right: 0;
    padding-left: 0;
  }

  .el-button--primary {
    font-size: 20px;
    color: #8f8ad7;
    background-color: white;
    border: #8f8ad7;
  }

  .row-bg {
    align-items: center;
  }

  .mainAudio {
    width: 100%;
  }

  /deep/ .iconfont {
    color: #8f8ad7;
  }

  .light {
    /deep/ .iconfont {
      color: #8f8ad7;
    }
  }

  .gray {
    /deep/ .iconfont {
      color: gray;
    }
  }

  /deep/ .playButton {
    float: right;
  }

  /deep/ .myCard {
    .timeInfo {
      font-size: 9px;
    }

    .el-slider__button {
      border-color: #8f8ad7;
    }

    .el-button--primary:active {
      background-color: white;
    }

    .el-button--primary:hover,
    .el-button--primary:focus {
      background-color: white;
    }

    .el-slider__bar {
      background-color: #8f8ad7;
    }

    .el-card__body {
      padding: 0;
    }

    margin-right: 1%;
    border-radius: 30px;
  }

  /deep/ .el-icon-bottom {
    color: #8f8ad7;
  }

  .audioOut {
    height: 30%;
  }
</style>
