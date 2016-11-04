package com.radioyps.httpstreamfrommotion;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

        private int[] checkBoundary(byte[] buffer){
            int[] ret = {0,0};
            int startindex = indexOf(buffer,bound);
            if(startindex != -1){
                Log.i(LOG_TAG, "found bound on " + startindex);
                int endIndex = indexOf(buffer, endByte);
                if(endIndex != -1) {
                    Log.i(LOG_TAG, "found end on " + endIndex);
                }
                byte[] tmp = new byte[endIndex];
                System.arraycopy(buffer, 0, tmp, 0, endIndex);
                int imageSize = getImageSize(tmp);
                ret[0] = imageSize;
                ret[1] = endIndex +4;

            }
            return ret;
        }

        private int getImageSize(byte[] input){
            int imageSize = 0;
            String headString = new String(input);

            String[] tmpString = headString.split(CommonConstants.contentLength);
            imageSize = Integer.parseInt(tmpString[1].trim());
            Log.i(LOG_TAG, "image length: " + imageSize);
            return  imageSize;
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
                byte[] imageBuffer = null;
                int currentImageSize = 0;
                int currentImageBufferOffet = 0;
                while (((bytesRead = is.read(buffer,0,buffer.length))>0)
                        &&(!isNeedStopReadingThread)){
                    Log.i(LOG_TAG, "Read inputstream:: length: " + bytesRead);
                     int[] info = checkBoundary(buffer);
                     if(info[0] != 0 ){
                         currentImageSize = info[0] + 2;
                         int offset = info[1];
                         imageBuffer = new byte[currentImageSize];
                         System.arraycopy(buffer,offset, imageBuffer,0, bytesRead - offset);
                         Log.i(LOG_TAG, "Image data got, length: " + (bytesRead-offset));
                         currentImageBufferOffet = bytesRead-offset;

                     }else if(currentImageSize != 0){
                         Log.i(LOG_TAG, "Image data got, length: " + bytesRead);
                         System.arraycopy(buffer, 0, imageBuffer, currentImageBufferOffet,bytesRead);
                         currentImageBufferOffet += bytesRead;
                         if(currentImageBufferOffet == currentImageSize){
                             Log.i(LOG_TAG, "we have a new image ready. length: " + imageBuffer.length);
                             MainActivity.copyImages(imageBuffer);

                             MainActivity.sendImageBytes("image ready");

                             currentImageSize = 0;
                         }
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




