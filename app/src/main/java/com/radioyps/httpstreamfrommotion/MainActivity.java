package com.radioyps.httpstreamfrommotion;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = MainActivity.class.getName();

    private  static IncomingHandler mHandler =null ;
    private TextView mStatusTextView = (TextView)findViewById(R.id.connect_status);
    private ImageView mImageView = (ImageView)findViewById(R.id.Image);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /**
     * Handler of incoming messages from service.
     */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CommonConstants.REFLASH_IMAGE:
                    byte[] imageBytes = (byte[]) msg.obj;

//                    byte[] imageBytes = intent.getByteArrayExtra(ImageViewActivity.EXTRA_IMAGE_BYTE_ARRAY);

                    Bitmap bm = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                    DisplayMetrics dm = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getMetrics(dm);

                    mImageView.setMinimumHeight(dm.heightPixels);
                    mImageView.setMinimumWidth(dm.widthPixels);
                    mImageView.setImageBitmap(bm);
                    Log.i(TAG, "IncomingHandler()>> image length: " + imageBytes.length);

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



}
