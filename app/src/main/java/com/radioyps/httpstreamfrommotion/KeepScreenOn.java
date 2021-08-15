package com.radioyps.httpstreamfrommotion;

import android.content.Context;

/**
 * Created by yep on 12/06/21.
 */





public class KeepScreenOn  implements Runnable {

    private boolean isNeedStopScreenOnThread = false;
    private Context context ;

    public KeepScreenOn(Context runningContext){
        context = runningContext;
    }

    public void run() {
        while (!isNeedStopScreenOnThread) {
            try {
                Thread.sleep(4000);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private void screenON(){
       
    }

    private void screenOff(){

    }
}
