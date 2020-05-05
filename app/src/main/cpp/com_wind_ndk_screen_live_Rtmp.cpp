//
// Created by 史浩 on 2020/5/5.
//
#include <rtmp.h>
#include "com_wind_ndk_screen_live_Rtmp.h"
#include "packet.h"
#include <android/log.h>
#ifdef __cplusplus
extern "C" {
#endif

#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,"RTMP",__VA_ARGS__)

Live* live;
int sendPacket(RTMPPacket *packet) {
    int r = RTMP_SendPacket(live->rtmp, packet, 1);
    RTMPPacket_Free(packet);
    free(packet);
    return r;
}
void sendVideo(int8_t *buf, int len, long timestamp){
    int ret;
    do{
        //获取视频数据包类型 对于视频有sps,pps/关键帧/非关键帧
        if(buf[4]==0x67){//sps pps
            LOGI("get sps pps");
            if (live && (!live->sps || !live->pps)){
                LOGI("prepraeVideo");
                prepraeVideo(buf,len,live);
            }
        } else{
            if (buf[4]==0x65){ //关键帧
                //在关键帧之前需要先发送一次sps，pps数据
                LOGI("createVideoPackage sps");
                RTMPPacket *packet = createVideoPackage(live);
                if (!(sendPacket(packet))) {
                    break;
                }
            }
            //将编码之后的数据 按照 flv、rtmp的格式 拼好之后
            LOGI("createVideoPackage frame");
            RTMPPacket *packet = createVideoPackage(buf, len, timestamp, live);
            sendPacket(packet);
        }

    }while (0);
}
void sendAudio(int8_t *data, jint len, jlong timestamp){

}
extern "C"
jboolean JNICALL Java_com_wind_ndk_screen_live_Rtmp_connect
        (JNIEnv *env, jclass, jstring url_) {
    const char *url = (env->GetStringUTFChars(url_, 0));
    LOGI("start connect");
    int ret = 0;
    do {
        live=(Live*)malloc(sizeof(Live));
        live->sps=0;
        live->pps=0;
        LOGI("RTMP_Alloc");
        live->rtmp = RTMP_Alloc();
        RTMP_Init(live->rtmp);
        ret = RTMP_SetupURL(live->rtmp, (char *)(url));
        if (!ret) {
            break;
        }
        LOGI("RTMP_EnableWrite");
        RTMP_EnableWrite(live->rtmp);
        LOGI("RTMP_Connect");
        ret = RTMP_Connect(live->rtmp, 0);
        if (!ret) {
            break;
        }
        LOGI("RTMP_ConnectStream");
        ret = RTMP_ConnectStream(live->rtmp, 0);
    } while (0);
    if (!ret && live) {
        LOGI("RTMP_free");
      free(live);
      live= nullptr;
    }
    LOGI("finish");
    env->ReleaseStringUTFChars(url_,url);
    return ret;
}
extern "C"
void JNICALL Java_com_wind_ndk_screen_live_Rtmp_sendData
        (JNIEnv *env, jclass, jbyteArray data_, jint dataLen_, jint type_, jlong timestamp) {
    jbyte* data=env->GetByteArrayElements(data_,0);
    int ret;
    switch (type_){
        case 1://video
            sendVideo(data,dataLen_,timestamp);
            break;
        case 2://audio
            sendAudio(data,dataLen_,timestamp);
            break;
    }
    env->ReleaseByteArrayElements(data_,data,0);
}


extern "C"
void JNICALL Java_com_wind_ndk_screen_live_Rtmp_disconnect
        (JNIEnv *, jclass) {
    if (live) {
        if (live->sps) {
            free(live->sps);
        }
        if (live->pps) {
            free(live->pps);
        }
        if (live->rtmp) {
            RTMP_Close(live->rtmp);
            RTMP_Free(live->rtmp);
        }
        free(live);
        live = nullptr;
    }
}
#ifdef __cplusplus
}
#endif

