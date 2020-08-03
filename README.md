# flutter_rtmp  

> flutter flutter_rtmp 百度语音识别 webSocket
公司项目要做直播demo。同时需要客户端发送做实时识别。所以选用了[flutter_rtmp](https://github.com/MEnigma/flutter_rtmp)。在这里非常感谢作者。。

这个版本，主要是依赖了[这个库](https://github.com/MEnigma/flutter_rtmp/tree/v0.1.6)。

对yasea其源码进行了魔改。
主要为了支持百度语音识别。

放这个库的目的是如果以后谁有这个需求也正好使用。


改动思路：
yasea 出现bug:
> android.media.MediaCodec$CodecException: Error 0xfffffc0e

>这里我们打开android/srs/main/java/net/ossrs/yasea/SrsEncoder.java
```
 MediaFormat videoFormat = MediaFormat.createVideoFormat(VCODEC, vOutWidth, vOutHeight);
       videoFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, mVideoColorFormat);
        videoFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 0);
        videoFormat.setInteger(MediaFormat.KEY_BIT_RATE, vBitrate);
        videoFormat.setInteger(MediaFormat.KEY_FRAME_RATE, VFPS);
        videoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, VGOP / VFPS);
        vencoder.configure(videoFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        // add the video tracker to muxer.
        videoFlvTrack = flvMuxer.addTrack(videoFormat);
        videoMp4Track = mp4Muxer.addTrack(videoFormat);
```
替换成：
```
 try {
    int formatWidth = vOutWidth;
    int formatHeight = vOutHeight;
    if ((formatWidth & 1) == 1) {
        formatWidth--;
    }
    if ((formatHeight & 1) == 1) {
        formatHeight--;
    }
// final MediaFormat format = MediaFormat.createVideoFormat(MIME_TYPE, formatWidth, formatHeight);
    MediaFormat videoFormat = MediaFormat.createVideoFormat(VCODEC, formatWidth, formatHeight);
    videoFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, mVideoColorFormat);
    videoFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 0);
    videoFormat.setInteger(MediaFormat.KEY_BIT_RATE, vBitrate);
    videoFormat.setInteger(MediaFormat.KEY_FRAME_RATE, VFPS);
    videoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, VGOP / VFPS);
    vencoder.configure(videoFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
    videoFlvTrack = flvMuxer.addTrack(videoFormat);
    videoMp4Track = mp4Muxer.addTrack(videoFormat);
} catch (Exception e) {
    System.out.println(e);
}
```

[参考地址](https://blog.csdn.net/zhang___yong/article/details/82760756)。

第二个改动：
是录制音频的时候需要改动参数。这个是我踩过最大的坑。后端一直说返回的数据不对，这边一直在排查，最后把所有可能都排查了就是没有看百度文档。。。


因为百度实时语音对于数据格式和参数有要求，最后查看[百度语音识别技术文档](https://ai.baidu.com/ai-doc/SPEECH/2k5dllqxj)才发现问题，其实改动很简单。

具体查看
> android/srs/main/java/net/ossrs/yasea/SrsEncoder.java
```
new AudioRecord 方法
就是对两个参数 AudioFormat.CHANNEL_IN_MONO SrsEncoder.ASAMPLERATE 写死。就可以了
因为这两个参数控制了音频的质量和播放频率。
```
第三个改动加入webSocket：
```
mic.read 方法是实时拿到音频的二进制数组的。所以我们在这个方法中实时发送socket。
```
因为是demo所以没有优化，如果有谁需要的话可以自己优化。


### 最后
由于自己是H5前端不能给大家更多的，所以如果还遇见其他问题，我其实也无法解决。。。