package com.example.yifeihappy.myapplication;

import android.hardware.Sensor;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by yifeihappy on 2017/8/31.
 */

public class SocketThread implements Runnable {

    public static String IP = "192.168.10.101";
    public static int PORT = 30000;
    public Socket s = null;
    public OutputStream os = null;
    public Handler sendHandler = null;//send sensor data to server
    private Handler handler = null;//update UI handler
    SocketThread(Handler h) {
        handler = h;
    }
    public static void setIP(String ip) {
        IP = ip;
    }

    @Override
    public void run() {
        try {
            Log.d("Debug", "try to connect to "+IP+":"+PORT);
           // s = new Socket(IP, PORT);
            s = new Socket();
            s.connect(new InetSocketAddress(IP, PORT),60000);//timeout == 6s
            os = s.getOutputStream();
            Looper.prepare();
            sendHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    if(msg.what == Sensor.TYPE_ACCELEROMETER) {
                        try {
                            os.write(msg.obj.toString().getBytes("utf-8"));
                            Log.e("Sensor:", msg.obj.toString() );
                        } catch (IOException e) {
                            //e.printStackTrace();
                            Message msge = Message.obtain();
                            msge.what = -101;
                            handler.sendMessage(msge);

                        }
                    }
                }
            };
            Looper.loop();

        } catch (IOException e) {
            e.printStackTrace();
            Message msge = Message.obtain();
            msge.what = -100;
            handler.sendMessage(msge);
        }
    }
}
