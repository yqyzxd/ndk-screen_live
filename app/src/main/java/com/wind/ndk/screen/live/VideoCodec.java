package com.wind.ndk.screen.live;

import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.projection.MediaProjection;
import android.os.Bundle;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created By wind
 * on 2020/5/5
 */
public class VideoCodec extends Thread{
    private MediaCodec mediaCodec;
    private ScreenLive mScreenLive;
    private long timestamp;
    private long startTime;
    private VirtualDisplay mVirtualDisplay;
    private MediaProjection mediaProjection;
    public VideoCodec(ScreenLive screenLive){
        this.mScreenLive=screenLive;
    }
    public void start(MediaProjection mediaProjection){
        try {
            this.mediaProjection=mediaProjection;
            //1. 创建编码器
            mediaCodec=MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);

            //2. 配置视频编码参数
            MediaFormat mediaFormat=MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC,360,640);
            //帧率 码率 关键帧间隔  数据格式
            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE,400_000);
            mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE,15);
            mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL,2);
            mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
            mediaCodec.configure(mediaFormat,null,null,MediaCodec.CONFIGURE_FLAG_ENCODE);

            Surface inputSurface=mediaCodec.createInputSurface();
            //
            mVirtualDisplay=mediaProjection.createVirtualDisplay("w",360,640,1,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,inputSurface,
                    null,null);

        } catch (IOException e) {
            e.printStackTrace();
        }

        start();
    }

    @Override
    public void run() {

        mediaCodec.start();
        MediaCodec.BufferInfo bufferInfo=new MediaCodec.BufferInfo();
        while (mScreenLive.isLiving()){

            //每隔2s手动触发一个关键帧输出
            if (timestamp!=0){
                if (System.currentTimeMillis()-timestamp>=2000){
                    Bundle params=new Bundle();
                    params.putInt(MediaCodec.PARAMETER_KEY_REQUEST_SYNC_FRAME,0);
                    mediaCodec.setParameters(params);
                    timestamp=System.currentTimeMillis();
                }
            }else {
                timestamp=System.currentTimeMillis();
            }

            //获取编码后的数据
            int index=mediaCodec.dequeueOutputBuffer(bufferInfo,10);
            if (index>=0){
                ByteBuffer byteBuffer=mediaCodec.getOutputBuffer(index);
                byte[]outData=new byte[bufferInfo.size];
                byteBuffer.get(outData);

                if (startTime==0){
                    startTime=bufferInfo.presentationTimeUs/1000;
                }
                RTMPPackage rtmpPackage=new RTMPPackage();
                rtmpPackage.setBuffer(outData);
                rtmpPackage.setType(RTMPPackage.RTMP_PACKET_TYPE_VIDEO);
                long tms=bufferInfo.presentationTimeUs/1000 -startTime;
                rtmpPackage.setTimestamp(tms);
                mScreenLive.addPackage(rtmpPackage);

                mediaCodec.releaseOutputBuffer(index,false);

            }

        }

        startTime=0;
        mediaCodec.stop();
        mediaCodec.release();
        mediaCodec=null;

        mVirtualDisplay.release();
        mVirtualDisplay=null;
        mediaProjection.stop();
        mediaProjection=null;
    }

    public void stopLive(){
        try {
            join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
