package com.wind.ndk.screen.live;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;

import androidx.annotation.Nullable;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created By wind
 * on 2020/5/5
 */
public class ScreenLive implements Runnable{


    private static final int REQUEST_CODE_CAPTURE_SCREEN=1000;
    private LinkedBlockingDeque<RTMPPackage> queue=new LinkedBlockingDeque<>();
    private MediaProjectionManager mediaProjectionManager;
    private Activity mActivity;
    private String url;
    private ExecutorService mExecutorService;
    private MediaProjection mediaProjection;
    private boolean mLiving;
    public void startLive(String url, Activity activity){
        this.mActivity=activity;
        this.url=url;
        requestRecordScreen();
    }

    private void requestRecordScreen() {
        mediaProjectionManager = (MediaProjectionManager)mActivity.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        Intent captureScreenIntent=mediaProjectionManager.createScreenCaptureIntent();
        mActivity.startActivityForResult(captureScreenIntent,REQUEST_CODE_CAPTURE_SCREEN);
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode== Activity.RESULT_OK && requestCode==REQUEST_CODE_CAPTURE_SCREEN){
            mediaProjection=mediaProjectionManager.getMediaProjection(resultCode,data);
            //子线程中 开启录屏
            if (mExecutorService==null){
                mExecutorService= Executors.newSingleThreadExecutor();
            }
            mExecutorService.execute(this);

        }
    }

    @Override
    public void run() {

        //1. 连接服务器
        boolean connected=Rtmp.connect(url);
        if (!connected){
            return;
        }
        mLiving=true;

        VideoCodec videoCodec=new VideoCodec(this);
        videoCodec.start(mediaProjection);
        while (mLiving){
            try {
                RTMPPackage rtmpPackage=queue.take();
                if (rtmpPackage!=null
                    && rtmpPackage.getBuffer()!=null
                        && rtmpPackage.getBuffer().length>0){
                    Rtmp.sendData(rtmpPackage.getBuffer(),
                            rtmpPackage.getBuffer().length,
                            rtmpPackage.getType(),
                            rtmpPackage.getTimestamp());
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        videoCodec.stopLive();
        queue.clear();
        Rtmp.disconnect();
    }



    public boolean isLiving() {
        return mLiving;
    }



    public void addPackage(RTMPPackage rtmpPackage) {
        if (mLiving){
            queue.add(rtmpPackage);
        }
    }




}
