package com.example.yifeihappy.myapplication;

import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by yifeihappy on 2017/8/31.
 */

public class SocketThread implements Runnable {

    public static String IP = null;
    public static int PORT = 30000;
    public Socket s = null;
    public OutputStream os = null;
    public Handler sendHandler = null;//send sensor data to server
    private Handler uiHander = null;//update UI handler
    private String sensorsTypeStr = null;
    public BufferedReader br = null;
    private SensorManager sensorManage;
    SensorEventListener sensorEventListener = null;
    private int SAMPLINGPERIODUS = 3;//default NORMAL

    public SocketThread(Handler h, SensorManager sensorManager, SensorEventListener sensorEventListener, String IP) {
        uiHander = h;
        this.sensorManage = sensorManager;
        this.sensorEventListener = sensorEventListener;
        this.IP = IP;

        //获取传感器的集合
        List<Sensor> list = sensorManager.getSensorList(Sensor.TYPE_ALL);
        StringBuffer strBuffer = new StringBuffer();
        strBuffer.append("SENSORSTYPE");//字符串以SENSORSTYPE开头
        for (Sensor sensor : list) {
            strBuffer.append("," + sensor.getType() + ":" + sensor.getName());
            Log.d("SENSORSTYPE", sensor.getType() + ":" + sensor.getName());
        }
        strBuffer.append("\n");
        sensorsTypeStr = strBuffer.toString();

    }

    public static void setIP(String ip) {
        IP = ip;
    }

    @Override
    public void run() {
        try {
            // Log.d("Debug", "try to connect to "+IP+":"+PORT);
            // s = new Socket(IP, PORT);
            s = new Socket();
            s.connect(new InetSocketAddress(IP, PORT), 60000);//timeout == 6s
            os = s.getOutputStream();

            br = new BufferedReader(new InputStreamReader(s.getInputStream()));

            //发送机器支持的传感器类型
            try {
                os.write(sensorsTypeStr.getBytes("utf-8"));
            } catch (IOException e) {
                Message msgUI = Message.obtain();
                msgUI.what = -101;
                uiHander.sendMessage(msgUI);
            }
            new Thread() {

                @Override
                public void run() {
                    String content = null;
                    try {
                        Log.d("DEB_SOCK:", "br.readLine() start!");
                        while ((content = br.readLine()) != null) {
                            String[] contentArr = content.split(",");
                            if (contentArr[0].equals("SAMPLINGPERIODUS")) {
                                sensorManage.unregisterListener(sensorEventListener);
                                Log.d("DEB_SOCK", "SAMPLINGPERIODUS:" + contentArr[1]);
                                SAMPLINGPERIODUS = Integer.parseInt(contentArr[1]);
                            } else if (contentArr[0].equals("SENSORSTYPE")) {
                                //register
                                for (int i = 1; i < contentArr.length; i++) {
                                    Log.d("DEB_SOCK", "SensorType:" + contentArr[i]);
                                    sensorManage.registerListener(sensorEventListener, sensorManage.getDefaultSensor(Integer.parseInt(contentArr[i])), SAMPLINGPERIODUS);
                                }
                            }

                            Log.d("DEB_SOCK:", content);
                        }
                    } catch (IOException e) {
                        Log.d("DEB_SOCK:", e.toString());
                    }
                }
            }.start();
            Looper.prepare();
            sendHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    //if(msg.what == Sensor.TYPE_ACCELEROMETER ||msg.what == Sensor.TYPE_GRAVITY) {
                    try {
                        os.write(msg.obj.toString().getBytes("utf-8"));
                        os.flush();
                        Log.d("Sensor:", msg.obj.toString());
                    } catch (IOException e) {
                        //e.printStackTrace();
                        Message msgUI = Message.obtain();
                        msgUI.what = -101;
                        uiHander.sendMessage(msgUI);
                    }
                    //}
                }
            };
            Looper.loop();

        } catch (IOException e) {
            e.printStackTrace();
            Message msgUI = Message.obtain();
            msgUI.what = -100;
            uiHander.sendMessage(msgUI);
        }
    }
}
