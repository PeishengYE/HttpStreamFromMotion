package com.radioyps.httpstreamfrommotion;

/**
 * Created by developer on 03/11/16.
 */
public class CommonConstants {

//    public static final String REFLASH_IMAGE = "com.radioyps.httpstreamfrommotion.REFLASH_IMAGE";
//    public static final String UPDATE_CONNECTION_STATUS = "com.radioyps.httpstreamfrommotion.UPDATE_CONNECTION_STATUS";

    public static final int REFLASH_IMAGE = 0x12;
    public static final int UPDATE_CONNECTION_STATUS = 0x13;

    public static final String ACTION_START_LOADING = "com.radioyps.httpstreamfrommotion.ACTION_START_LOADING";
    public static final String ACTION_STOP_LOADING = "com.radioyps.httpstreamfrommotion.ACTION_STOP_LOADING";


//    public static final String serverUrlStr = "http://192.168.12.109:2022";
    public static final String MotionCameraUrlStr = "http://192.168.106.219:2022/";

    public static final String ZiyiUrlStr = "http://192.168.106.221:8080/image/";
    public static final String ZihanUrlStr = "http://192.168.106.123:8080/image";
    public static final String bundaryString = "--BoundaryString";
    public static final String contentLength = "Content-Length:";
    public static final String HttpHeadEndByte = "\r\n\r\n";
    public static final int MAX_IMAGE_SIZE = 100*1024;
}
