package net.ossrs.yasea;

import android.hardware.Camera;
import android.media.AudioRecord;
import android.media.audiofx.AcousticEchoCanceler;
import android.media.audiofx.AutomaticGainControl;
import android.os.Environment;
import android.util.Log;

import com.github.faucamp.simplertmp.RtmpHandler;
import com.seu.magicfilter.utils.MagicFilterType;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Arrays;
import java.util.List;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import android.media.AudioFormat;


import java.io.File;
import java.net.URI;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Created by Leo Ma on 2016/7/25.
 */
public class SrsPublisher {

    private static AudioRecord mic;
    private static AcousticEchoCanceler aec;
    private static AutomaticGainControl agc;
    private  File file;
    FileOutputStream outputStream;
    private byte[] mPcmBuffer = new byte[1024];
    private byte[] socketAudio = new byte[4096];
    private Thread aworker;

    private SrsCameraView mCameraView;

    private boolean sendVideoOnly = false;
    private boolean sendAudioOnly = false;
    private int videoFrameCount;
    private long lastTimeMillis;
    private double mSamplingFps;

    private SrsFlvMuxer mFlvMuxer;
    private SrsMp4Muxer mMp4Muxer;
    private SrsEncoder mEncoder;
    private static MySocketClient socketClient;

    public SrsPublisher(SrsCameraView view) {
        mCameraView = view;
        mCameraView.setPreviewCallback(new SrsCameraView.PreviewCallback() {
            @Override
            public void onGetRgbaFrame(byte[] data, int width, int height) {
                calcSamplingFps();
                if (!sendAudioOnly) {
                    mEncoder.onGetRgbaFrame(data, width, height);
                }
            }
        });
        
        // file = createFile();
        // try {
        //     outputStream =new FileOutputStream(file);
        // } catch (FileNotFoundException e) {
        //     e.printStackTrace();
        // }
    }

    private void calcSamplingFps() {
        // Calculate sampling FPS
        if (videoFrameCount == 0) {
            lastTimeMillis = System.nanoTime() / 1000000;
            videoFrameCount++;
        } else {
            if (++videoFrameCount >= SrsEncoder.VGOP) {
                long diffTimeMillis = System.nanoTime() / 1000000 - lastTimeMillis;
                mSamplingFps = (double) videoFrameCount * 1000 / diffTimeMillis;
                videoFrameCount = 0;
            }
        }
    }

    public void startCamera() {
        mCameraView.startCamera();
    }

    public void stopCamera() {
        mCameraView.stopCamera();
    }


    public void startAudio() {
        mic = mEncoder.chooseAudioRecord();
        if (mic == null) {
            return;
        }

        if (AcousticEchoCanceler.isAvailable()) {
            aec = AcousticEchoCanceler.create(mic.getAudioSessionId());
            if (aec != null) {
                aec.setEnabled(true);
            }
        }

        if (AutomaticGainControl.isAvailable()) {
            agc = AutomaticGainControl.create(mic.getAudioSessionId());
            if (agc != null) {
                agc.setEnabled(true);
            }
        }
        // final int minBufferSize = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        // initSocketOnNewThread("这里是websocket 地址");


        aworker = new Thread(new Runnable() {
            @Override
            public void run() {
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);
                mic.startRecording();
                //  byte[] dataLen = new byte[minBufferSize];
                int pos = 0;
                while (!Thread.interrupted()) {
                    if (sendVideoOnly) {
                        mEncoder.onGetPcmFrame(mPcmBuffer, mPcmBuffer.length);
                        try {
                            // This is trivial...
                            Thread.sleep(20);
                        } catch (InterruptedException e) {
                            break;
                        }
                    } else {
                        // int audioSize = mic.read(socketAudio, pos, socketAudio.length);
                        int size = mic.read(mPcmBuffer, 0, mPcmBuffer.length);
                        if (size > 0){
                            mEncoder.onGetPcmFrame(mPcmBuffer, size);
                           try {
                            // #TODO: 发送buffer给百度进行语音识别
                            sendSocket(mPcmBuffer);
                           } catch (Exception e) {
                               System.out.println(e);
                               //TODO: handle exception
                           }
                        }
                    }
                }
            }
        });
        aworker.start();
    }

    public static  byte[] concat(byte[] first, byte[] second) { 
        byte[] result = Arrays.copyOf(first, first.length + second.length); 
        System.arraycopy(second, 0, result, first.length, second.length); 
        return result; 
    }


    private void copyWaveFile(String inFilename, String outFilename) {
        System.out.println(inFilename);
        System.out.println(outFilename);
        FileInputStream in = null;
        FileOutputStream out = null;
        long totalAudioLen = 0;
        long totalDataLen = totalAudioLen + 36;
        long longSampleRate = 44100;
        int channels = 1;
        long byteRate = 16 * 44100 * channels / 8;

        byte[] data = new byte[1024];

        try {
        in = new FileInputStream(inFilename);
        out = new FileOutputStream(outFilename);
        totalAudioLen = in.getChannel().size();
        totalDataLen = totalAudioLen + 36;

        WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
                longSampleRate, channels, byteRate);

        while (in.read(data) != -1) {
            out.write(data);
        }

        in.close();
        out.close();
        } catch (FileNotFoundException e) {
        e.printStackTrace();
        } catch (IOException e) {
        e.printStackTrace();
        }
    }

    public void stopAudio() {
        if (aworker != null) {
            aworker.interrupt();
            try {
                aworker.join();
            } catch (InterruptedException e) {
                aworker.interrupt();
            }
            aworker = null;
        }

        if (mic != null) {
            mic.setRecordPositionUpdateListener(null);
            mic.stop();
            mic.release();
            mic = null;
        }

        if (aec != null) {
            aec.setEnabled(false);
            aec.release();
            aec = null;
        }

        if (agc != null) {
            agc.setEnabled(false);
            agc.release();
            agc = null;
        }
    }
    private void WriteWaveFileHeader(FileOutputStream out, long totalAudioLen,
                                   long totalDataLen, long longSampleRate, int channels, long byteRate)
          throws IOException {
    byte[] header = new byte[44];

    header[0] = 'R'; // RIFF/WAVE header
    header[1] = 'I';
    header[2] = 'F';
    header[3] = 'F';
    header[4] = (byte) (totalDataLen & 0xff);
    header[5] = (byte) ((totalDataLen >> 8) & 0xff);
    header[6] = (byte) ((totalDataLen >> 16) & 0xff);
    header[7] = (byte) ((totalDataLen >> 24) & 0xff);
    header[8] = 'W';
    header[9] = 'A';
    header[10] = 'V';
    header[11] = 'E';
    header[12] = 'f'; // 'fmt ' chunk
    header[13] = 'm';
    header[14] = 't';
    header[15] = ' ';
    header[16] = 16; // 4 bytes: size of 'fmt ' chunk
    header[17] = 0;
    header[18] = 0;
    header[19] = 0;
    header[20] = 1; // format = 1
    header[21] = 0;
    header[22] = (byte) channels;
    header[23] = 0;
    header[24] = (byte) (longSampleRate & 0xff);
    header[25] = (byte) ((longSampleRate >> 8) & 0xff);
    header[26] = (byte) ((longSampleRate >> 16) & 0xff);
    header[27] = (byte) ((longSampleRate >> 24) & 0xff);
    header[28] = (byte) (byteRate & 0xff);
    header[29] = (byte) ((byteRate >> 8) & 0xff);
    header[30] = (byte) ((byteRate >> 16) & 0xff);
    header[31] = (byte) ((byteRate >> 24) & 0xff);
    header[32] = (byte) (1); // block align
    header[33] = 0;
    header[34] = 16; // bits per sample
    header[35] = 0;
    header[36] = 'd';
    header[37] = 'a';
    header[38] = 't';
    header[39] = 'a';
    header[40] = (byte) (totalAudioLen & 0xff);
    header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
    header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
    header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

    out.write(header, 0, 44);
  }


    public void startEncode() {
        if (!mEncoder.start()) {
            return;
        }

        mCameraView.enableEncoding();

        startAudio();
    }

    public void stopEncode() {
        stopAudio();
        stopCamera();
        closeSocket();
        // copyWaveFile(file.getAbsolutePath(), createFilemp3().getAbsolutePath());
        System.out.println("====stopEncode--------====");
        mEncoder.stop();
    }

    public void pauseEncode() {
        stopAudio();
        closeSocket();
        System.out.println("====pauseEncode--------====");
        mCameraView.disableEncoding();
        mCameraView.stopTorch();
    }

    private void resumeEncode() {
        startAudio();
        mCameraView.enableEncoding();
    }

    public void startPublish(String rtmpUrl) {
        if (mFlvMuxer != null) {
            System.out.println("=====-0990=--------====");
            // initSocketOnNewThread()
            mFlvMuxer.start(rtmpUrl);
            mFlvMuxer.setVideoResolution(mEncoder.getOutputWidth(), mEncoder.getOutputHeight());
            startEncode();
        }
    }

    public void resumePublish() {
        if (mFlvMuxer != null) {
            mEncoder.resume();
            resumeEncode();
        }
    }

    public void stopPublish() {
        if (mFlvMuxer != null) {
            stopEncode();
            mFlvMuxer.stop();
        }
    }

    public void pausePublish() {
        if (mFlvMuxer != null) {
            mEncoder.pause();
            pauseEncode();
        }
    }

    public boolean startRecord(String recPath) {
        return mMp4Muxer != null && mMp4Muxer.record(new File(recPath));
    }

    public void stopRecord() {
        if (mMp4Muxer != null) {
            mMp4Muxer.stop();
        }
    }

    public void pauseRecord() {
        if (mMp4Muxer != null) {
            mMp4Muxer.pause();
        }
    }

    public void resumeRecord() {
        if (mMp4Muxer != null) {
            mMp4Muxer.resume();
        }
    }

    public boolean isAllFramesUploaded() {
        return mFlvMuxer.getVideoFrameCacheNumber().get() == 0;
    }

    public int getVideoFrameCacheCount() {
        if (mFlvMuxer != null) {
            return mFlvMuxer.getVideoFrameCacheNumber().get();
        }
        return 0;
    }

    public void switchToSoftEncoder() {
        mEncoder.switchToSoftEncoder();
    }

    public void switchToHardEncoder() {
        mEncoder.switchToHardEncoder();
    }

    public boolean isSoftEncoder() {
        return mEncoder.isSoftEncoder();
    }

    public int getPreviewWidth() {
        return mEncoder.getPreviewWidth();
    }

    public int getPreviewHeight() {
        return mEncoder.getPreviewHeight();
    }

    public double getmSamplingFps() {
        return mSamplingFps;
    }

    public int getCameraId() {
        return mCameraView.getCameraId();
    }

    public Camera getCamera() {
        return mCameraView.getCamera();
    }

    public void setPreviewResolution(int width, int height) {
        int resolution[] = mCameraView.setPreviewResolution(width, height);
        mEncoder.setPreviewResolution(resolution[0], resolution[1]);
    }

    public void setOutputResolution(int width, int height) {
        if (width <= height) {
            mEncoder.setPortraitResolution(width, height);
        } else {
            mEncoder.setLandscapeResolution(width, height);
        }
    }

    public void setScreenOrientation(int orientation) {
        mCameraView.setPreviewOrientation(orientation);
        mEncoder.setScreenOrientation(orientation);
    }

    public void setVideoHDMode() {
        mEncoder.setVideoHDMode();
    }

    public void setVideoSmoothMode() {
        mEncoder.setVideoSmoothMode();
    }

    public void setSendVideoOnly(boolean flag) {
        if (mic != null) {
            if (flag) {
                mic.stop();
                mPcmBuffer = new byte[1024];
                socketAudio = new byte[4096];
            } else {
                mic.startRecording();
            }
        }
        sendVideoOnly = flag;
    }

    public void setSendAudioOnly(boolean flag) {
        sendAudioOnly = flag;
    }

    public boolean switchCameraFilter(MagicFilterType type) {
        return mCameraView.setFilter(type);
    }

    public void switchCameraFace(int id) {
        mCameraView.stopCamera();
        mCameraView.setCameraId(id);
        if (id == 0) {
            mEncoder.setCameraBackFace();
        } else {
            mEncoder.setCameraFrontFace();
        }
        if (mEncoder != null && mEncoder.isEnabled()) {
            mCameraView.enableEncoding();
        }
        mCameraView.startCamera();
    }

    public void setRtmpHandler(RtmpHandler handler) {
        mFlvMuxer = new SrsFlvMuxer(handler);
        if (mEncoder != null) {
            mEncoder.setFlvMuxer(mFlvMuxer);
        }
    }

    public void setRecordHandler(SrsRecordHandler handler) {
        mMp4Muxer = new SrsMp4Muxer(handler);
        if (mEncoder != null) {
            mEncoder.setMp4Muxer(mMp4Muxer);
        }
    }

    public void setEncodeHandler(SrsEncodeHandler handler) {
        mEncoder = new SrsEncoder(handler);
        if (mFlvMuxer != null) {
            mEncoder.setFlvMuxer(mFlvMuxer);
        }
        if (mMp4Muxer != null) {
            mEncoder.setMp4Muxer(mMp4Muxer);
        }
    }

    /**
     * 关闭Socket连接
     */
    public void closeSocket() {
        if (socketClient != null) {
            if (socketClient.isOpen()) {
                socketClient.close();
            }
            socketClient = null;
        }
    }

    /**
     * 初始化socket
     *
     * @param url 服务器地址
     */
    public void initSocket(String url) {
        closeSocket();
        socketClient = new MySocketClient(URI.create(url)) {
            @Override
            public void onMessage(String message) {
            //    System.out.println(message);
            //    System.out.println();
            //   JSONObject result = new JSONObject(message);
            //  if (message != null && "".equals(message)){
            //         System.out.println("接收到一个空字符串");
            //         return;
            //     }
            // try {
            //     JSONObject jsonObject = new JSONObject(message);
            //     System.out.println(message);
            //     System.out.println(jsonObject);
            //     System.out.println("=========jsonObject-------");
            //     String type = jsonObject.getString("type");
            //     if ("open".equals(type)){
            //         System.out.println("=========open-------");
            //     }else if("close".equals(type)){
            //         System.out.println("=========close-------");
            //     }
            //     //    System.out.println(type);
            //     System.out.println("=====-initSocket=--------====");
            // } catch (Exception e) {
            //     Log.e("ZYY", "报错了--------");
            // }
           
            }
        };
        try {
            System.out.println("=====-initSocket=--------====");
            socketClient.connectBlocking();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Log.e("ZYY", "报错了--------");
        }
    }


    /**
     * 在新线程中初始化socket，因为不知道上面那个方法会不会卡
     *
     * @param url 服务器地址
     */
    public void initSocketOnNewThread(final String url){
        new Thread(){
            @Override
            public void run() {
                super.run();
                initSocket(url);
            }
        }.start();
    }
    /**
     * 发送数据
     * @param data 需要发送的数据
     */
    public void sendSocket(byte[] data) {
       

        if (socketClient.isOpen()) {
          socketClient.send(getJsonData(data));
        }else {
            // 这儿你看这些
        }
    }

    /**
     * 将
     * @param data
     * @return
     */
    public String getJsonData(byte[] data) {
        JSONObject jsonObject = new JSONObject();
        
        // jsonObject.put("text", "bytes2HexString(data)");
        try {
            String TextArray  = bytes2HexString(data);
            jsonObject.put("type", "video");

            // System.out.println(TextArray);
            jsonObject.put("text", TextArray);
        } catch (JSONException e) {
            System.out.println(e);
        }

        return jsonObject.toString();
    }
    public  String bytes2HexString(byte[] b) {
        StringBuilder builder=new StringBuilder();

        for (int i = 0; i < b.length; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = "0" + hex;
            }
            builder.append(hex);
        }
        return builder.toString();
    }

    public static File createFilemp3() {

        Calendar time = new GregorianCalendar();
        SimpleDateFormat simpleDate = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
        String format = simpleDate.format(time.getTime());
        String fileName = format + ".wav";
        String fileStoreDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/ZhangYangyang/mp3/";
        File file = new File(fileStoreDir);
        if (!file.exists()) {
            file.mkdirs();
        }
        String s = fileStoreDir + "/" + fileName;

        File f = new File(s);
        System.out.println("文件路径："+f.getAbsolutePath());
        return f;
    }
     public static File createFile() {

        Calendar time = new GregorianCalendar();
        SimpleDateFormat simpleDate = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
        String format = simpleDate.format(time.getTime());
        String fileName = format + ".tmp";
        String fileStoreDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/ZhangYangyang/mp3/";
        File file = new File(fileStoreDir);
        if (!file.exists()) {
            file.mkdirs();
        }
        String s = fileStoreDir + "/" + fileName;

        File f = new File(s);
        System.out.println("文件路径："+f.getAbsolutePath());
        return f;
    }
}
