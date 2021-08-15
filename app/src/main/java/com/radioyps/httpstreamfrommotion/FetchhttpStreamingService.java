package com.radioyps.httpstreamfrommotion;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
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

        private void getMotionImage(){
            try {

                Uri builtUri = Uri.parse(CommonConstants.MotionCameraUrlStr).buildUpon().build();

                URL url = new URL(builtUri.toString());


                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");

                urlConnection.setConnectTimeout(20000);
                urlConnection.setReadTimeout(20000);
                String userpass = "peter" + ":" + "123";
                //String basicAuth = "Basic " + new String(new Base64().encode(userpass.getBytes()));
                String basicAuth = "Basic " + new String(android.util.Base64.encode(userpass.getBytes(), android.util.Base64.NO_WRAP));
                urlConnection.setRequestProperty ("Authorization", basicAuth);
                urlConnection.connect();
                BufferedInputStream is =
                        (new BufferedInputStream(urlConnection.getInputStream()));
                byte[] buffer = new byte[50 * 1024];
                byte[] imageBuffer = null;
                int currentImageSize = 0;
                int currentImageBufferOffet = 0;
                int count = 0;
                while (((bytesRead = is.read(buffer, 0, buffer.length)) > 0)
                        && (!isNeedStopReadingThread)) {
                    Log.i(LOG_TAG, "Read inputstream:: length: " + bytesRead);

                    try {
                        int[] info = checkBoundary(buffer);
                        if (info[0] != 0) {
                            currentImageSize = info[0] + 2;
                            int offset = info[1];
                            imageBuffer = new byte[currentImageSize];
                            int firstPacketImagedataLength = bytesRead - offset;
                         /* find a strang error, this value is <0 */
                            if (firstPacketImagedataLength > 0) {
                                Log.i(LOG_TAG, "First image data start from: " + firstPacketImagedataLength);
                                System.arraycopy(buffer, offset, imageBuffer, 0, firstPacketImagedataLength);
                                currentImageBufferOffet = firstPacketImagedataLength;
                                MainActivity.sendStringMesg("new image arriving..");
                            }

                        } else if (currentImageSize != 0) {
                            Log.i(LOG_TAG, "This time read Image data length: " + bytesRead);
                            MainActivity.sendStringMesg("Reciving data..");
                            System.arraycopy(buffer, 0, imageBuffer, currentImageBufferOffet, bytesRead);
                            currentImageBufferOffet += bytesRead;
                            if (currentImageBufferOffet == currentImageSize) {
                                Log.i(LOG_TAG, "A new image ready. total length: " + imageBuffer.length);
                                Bitmap imageBitmap = BitmapFactory.decodeByteArray(imageBuffer, 0, imageBuffer.length);
//                                MainActivity.copyImages(imageBuffer);
                                MainActivity.sendStringMesg("refrash image..");
                                MainActivity.sendImageBytes(imageBitmap);
                                currentImageSize = 0;
                                count ++;
                                if(count >= 2) {
                                    Log.i(LOG_TAG, "Motion image showing is end !");
                                    break;
                                }
                            }
                        }

                    } catch (Exception e) {
                        Log.i(LOG_TAG, "One error happend when receiving data, ignore it");
                        currentImageSize = 0;
                    }


                }

                is.close();
                Log.i(LOG_TAG, "FetchData()>> close connection is done");
            } catch (Exception e) {
                Log.e(LOG_TAG, "Error creating ServerSocket: ", e);
                MainActivity.sendStringMesg("Error on connect to the camera..");
                e.printStackTrace();
            }
        }



        private void getKidsScreenshot(String urlKids){
            try {

                Uri builtUri = Uri.parse(urlKids).buildUpon().build();

                URL url = new URL(builtUri.toString());


                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");

                urlConnection.setConnectTimeout(20000);
                urlConnection.setReadTimeout(20000);
                String userpass = "peter" + ":" + "123";
                //String basicAuth = "Basic " + new String(new Base64().encode(userpass.getBytes()));
                String basicAuth = "Basic " + new String(android.util.Base64.encode(userpass.getBytes(), android.util.Base64.NO_WRAP));
                urlConnection.setRequestProperty ("Authorization", basicAuth);
                urlConnection.connect();
                Bitmap bitmap =
                        BitmapFactory.decodeStream((urlConnection.getInputStream()));
                Log.i(LOG_TAG, "getKidsScreenshot()>> bitmap from Zihan is done!");
                if(urlKids == CommonConstants.ZihanUrlStr) {
                    MainActivity.sendStringMesg("Zihan screenshot..");
                }else if(urlKids == CommonConstants.ZiyiUrlStr){
                    MainActivity.sendStringMesg("Ziyi screenshot..");
                }else{
                    MainActivity.sendStringMesg("Error wrong URL..");
                }
                MainActivity.sendImageBytes(bitmap);

            } catch (Exception e) {
                Log.e(LOG_TAG, "Error on connecting Zihan Computer", e);
                MainActivity.sendStringMesg("Error on connect to Zihan Computer..");
                e.printStackTrace();
            }
        }
        @Override
        public void run() {
            while (!isNeedStopReadingThread) {
//                try{
//                    Thread.sleep(4000);
//
//                }catch (Exception e){
//                    e.printStackTrace();
//                }
//                MainActivity.sendStringMesg("Retrying Door camera..");
//                getMotionImage();

                try{
                    Thread.sleep(4000);


                }catch (Exception e){
                    e.printStackTrace();
                }
                MainActivity.sendStringMesg("Retrying Zihan Screenshot..");
                getKidsScreenshot(CommonConstants.ZihanUrlStr);
                try{
                    Thread.sleep(4000);
                   }catch (Exception e){
                    e.printStackTrace();
                }

                MainActivity.sendStringMesg("Retrying Ziyi Screenshot..");
                getKidsScreenshot(CommonConstants.ZiyiUrlStr);

            }
        }
    }
}




