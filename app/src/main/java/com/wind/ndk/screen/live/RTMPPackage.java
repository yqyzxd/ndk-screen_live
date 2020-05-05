package com.wind.ndk.screen.live;

/**
 * Created By wind
 * on 2020/5/5
 */
public class RTMPPackage {
    public static final int RTMP_PACKET_TYPE_VIDEO=1;
    public static final int RTMP_PACKET_TYPE_AUDIO=2;

    private byte[] buffer;
    private int type;
    private long timestamp;

    public byte[] getBuffer() {
        return buffer;
    }

    public void setBuffer(byte[] buffer) {
        this.buffer = buffer;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
