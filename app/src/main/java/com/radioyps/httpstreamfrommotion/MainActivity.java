package com.radioyps.httpstreamfrommotion;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = MainActivity.class.getName();

    private Intent mServiceIntent;
    private  static IncomingHandler mHandler =null ;
    private TextView mStatusTextView ;
    private ImageView mImageView ;
//    private static byte[] imageBufferMain = new byte[CommonConstants.MAX_IMAGE_SIZE];
//    private static int imageBufferMainSize = 0;
    private PowerManager.WakeLock wl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mStatusTextView = (TextView)findViewById(R.id.connect_status);
        mImageView = (ImageView)findViewById(R.id.Image);
        mServiceIntent = new Intent(getApplicationContext(),FetchhttpStreamingService.class);
        mServiceIntent.setAction(CommonConstants.ACTION_START_LOADING);
        startService(mServiceIntent);
        mHandler = new IncomingHandler();

        /**/

        registerReceiver(new PhoneUnlockedReceiver(), new IntentFilter("android.intent.action.USER_PRESENT"));

        Log.i(TAG, "onCreate()>>  PhoneUnlockedReceiver added");
        PhoneUnlockedReceiver receiver = new PhoneUnlockedReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_USER_PRESENT);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(receiver, filter);

    }

    /**
     * Handler of incoming messages from service.
     */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CommonConstants.REFLASH_IMAGE:
                    Bitmap imageData = (Bitmap)msg.obj;
                    //Bitmap imageBitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
                    DisplayMetrics dm = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getMetrics(dm);

                    mImageView.setMinimumHeight(dm.heightPixels);
                    mImageView.setMinimumWidth(dm.widthPixels);
                    mImageView.setImageBitmap(imageData);
                    Log.i(TAG, "IncomingHandler()>> refresh new image ");

                    break;
                case CommonConstants.UPDATE_CONNECTION_STATUS:
                    String mesg = (String)msg.obj;
                    mStatusTextView.setText(mesg);
                    Log.i(TAG, "IncomingHandler()>> Message: " + mesg);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
    public static void sendStringMesg(String mesg){
        Message.obtain(mHandler, CommonConstants.UPDATE_CONNECTION_STATUS,mesg ).sendToTarget();
    }

//    public static void sendImageBytes(String mesg){
//        Message.obtain(mHandler, CommonConstants.REFLASH_IMAGE, mesg ).sendToTarget();
//    }

    public static void sendImageBytes(Bitmap imageData){
        Message.obtain(mHandler, CommonConstants.REFLASH_IMAGE, imageData ).sendToTarget();
    }
   /* public static void copyImages(Bitmap imageBuffer){
        System.arraycopy(imageBuffer,0, imageBufferMain,0, imageBuffer.length );
        imageBufferMainSize = imageBuffer.length;
    }*/

}
