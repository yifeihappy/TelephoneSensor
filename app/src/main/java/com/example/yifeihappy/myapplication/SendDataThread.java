package com.example.yifeihappy.myapplication;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by yifeihappy on 2017/9/2.
 */

public class SendDataThread extends Thread {
    private OutputStream os = null;
    private String msgStr = null;
    private Handler handler = null;
    SendDataThread(OutputStream ost, String str, Handler h)
    {
        os = ost;
        msgStr = str;
        handler = h;
    }

    @Override
    public void run() {
        super.run();
        try {
            os.write(msgStr.getBytes("utf-8"));
            Log.d("Sensor:", msgStr );
        } catch (IOException e) {
            //e.printStackTrace();
            Message msge = Message.obtain();
            msge.what = -101;
            handler.sendMessage(msge);
        }
    }
}
