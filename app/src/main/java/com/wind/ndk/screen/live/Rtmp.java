package com.wind.ndk.screen.live;

/**
 * Created By wind
 * on 2020/5/5
 */
public class Rtmp {

    static {
        System.loadLibrary("native-lib");
    }

    public static native boolean connect(String url);
    public static native void sendData(byte[] buffer, int length, int type, long timestamp);

    public static native void disconnect();
}
