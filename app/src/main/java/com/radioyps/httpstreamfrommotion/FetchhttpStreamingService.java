package com.radioyps.httpstreamfrommotion;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by developer on 03/11/16.
 */
public class FetchhttpStreamingService extends IntentService{

    private static final String LOG_TAG = FetchhttpStreamingService.class.getName();
    private Thread mReadThread;
    private boolean isNeedStopReadingThread = false;
    @Override
    public void onCreate() {
        super.onCreate();
    }


    public FetchhttpStreamingService() {
        super("com.radioyps.httpstreamfrommotion");
    }

    @Override
    protected void onHandleIntent(Intent intent) {


        String action = intent.getAction();
        if(action.equals(CommonConstants.ACTION_START_LOADING)) {
            Log.i(LOG_TAG, "onHandleIntent()>> reading thread started ...");
            isNeedStopReadingThread = false;
            mReadThread = new Thread(new FetchData());
            mReadThread.start();

        }else if(action.equals(CommonConstants.ACTION_STOP_LOADING)){
            isNeedStopReadingThread = true;
        }


    }

    class FetchData implements Runnable {

        HttpURLConnection urlConnection = null;

        byte[] bound = CommonConstants.bundaryString.getBytes();

        byte[] endByte = CommonConstants.HttpHeadEndByte.getBytes();
        byte[] cntLength = CommonConstants.contentLength.getBytes();
        int bytesRead;


        public int indexOf(byte[] outerArray, byte[] smallerArray) {
            for(int i = 0; i < outerArray.length - smallerArray.length+1; ++i) {
                boolean found = true;
                for(int j = 0; j < smallerArray.length; ++j) {
                    if (outerArray[i+j] != smallerArray[j]) {
                        found = false;
                        break;
                    }
                }
                if (found) return i;
            }
            return -1;
        }

        @Override
        public void run() {

            try {

                Uri builtUri = Uri.parse(CommonConstants.serverUrlStr).buildUpon().build();

                URL url = new URL(builtUri.toString());


                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                BufferedInputStream is =
                        (new BufferedInputStream(urlConnection.getInputStream()));
                byte[] buffer = new byte[50*1024];
                while (((bytesRead = is.read(buffer,0,buffer.length))>0)
                        &&(!isNeedStopReadingThread)){

                    Log.i(LOG_TAG, "FetchData()>> new read with length: " + bytesRead);
                    int startindex = indexOf(buffer,bound);
                    if(startindex != -1){
                        Log.i(LOG_TAG, "found bound on " + startindex);
                        int endIndex = indexOf(buffer, endByte);
                        if(endIndex != -1) {
                            Log.i(LOG_TAG, "found end on " + endIndex);
                        }
                        byte[] tmp = new byte[endIndex];
                        System.arraycopy(buffer, 0, tmp, 0, endIndex);
                        String headString = new String(tmp);
                        Log.i(LOG_TAG, "<<" + headString + ">>");
                    }
                }

                is.close();
                Log.i(LOG_TAG, "FetchData()>> close connection is done");
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error creating ServerSocket: ", e);
                e.printStackTrace();
            }
        }
    }
}




