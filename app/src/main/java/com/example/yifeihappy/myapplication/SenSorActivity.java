package com.example.yifeihappy.myapplication;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

/**
 * Created by yifeihappy on 2017/8/30.
 */

public class SenSorActivity extends Activity implements SensorEventListener{

    private SensorManager mSensorManage;
    private Sensor mAccelerometer;
    private Sensor mGravity;
    private Sensor mGyroscope;
    private TextView xTxv,yTxv,zTxv,sysTimeTxv;
    private Handler handler;
    private SocketThread clientThread;
    private String msgStr;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sensor_layout);

        Log.d("IP", SocketThread.IP);

        xTxv = (TextView)findViewById(R.id.x);
        yTxv = (TextView)findViewById(R.id.y);
        zTxv = (TextView)findViewById(R.id.z);
        sysTimeTxv = (TextView)findViewById(R.id.sys_time);

        //get the sensor manager object.
        mSensorManage = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        //get sensor type.
        mAccelerometer = mSensorManage.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        handler = new mHandler();

        clientThread = new SocketThread(handler);
        new Thread(clientThread).start();

    }

    @Override
    protected void onResume() {
        super.onResume();
        //regist accelerometer
        mSensorManage.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);

    }

    @Override
    protected void onPause() {
        super.onPause();

        mSensorManage.unregisterListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(clientThread.os != null) {
            try {
                clientThread.os.close();
            } catch (IOException e) {
                e.printStackTrace();
                if(clientThread.s != null) {
                    try {
                        clientThread.s.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float x, y, z;
        int sensorType = sensorEvent.sensor.getType();
        long curTime = System.currentTimeMillis();
        Message msg = Message.obtain();
        Message smsg = Message.obtain();
        Bundle bundle = new Bundle();
        switch (sensorType)
        {
            case Sensor.TYPE_ACCELEROMETER:
                bundle.putString("time", ""+curTime);
                bundle.putString("x",""+sensorEvent.values[0]);
                bundle.putString("y",""+sensorEvent.values[1]);
                bundle.putString("z",""+sensorEvent.values[2]);
                msg.what = Sensor.TYPE_ACCELEROMETER;
                msg.setData(bundle);


                msgStr = new String("time:"+curTime+"x:"+sensorEvent.values[0]+"y:"+sensorEvent.values[1]+"z:"+sensorEvent.values[2]+"\r\n");
                smsg.what = Sensor.TYPE_ACCELEROMETER;
                smsg.obj = msgStr;

                break;
        }
        handler.sendMessage(msg);
        if(clientThread.sendHandler!=null) {
            clientThread.sendHandler.sendMessage(smsg);
        }
        //make sure that telephone has connected to the server.
//        if(clientThread.os != null) {
//            Thread t = new SendDataThread(clientThread.os, msgStr, handler);
//            t.start();
//        }


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    class  mHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case Sensor.TYPE_ACCELEROMETER:
                    Bundle bundle = msg.getData();
                    sysTimeTxv.setText("T:"+bundle.getString("time"));
                    xTxv.setText("x:"+bundle.getString("x"));
                    yTxv.setText("y:"+bundle.getString("y"));
                    zTxv.setText("z:"+bundle.getString("z"));
                    break;
                case -100:
                    Toast.makeText(SenSorActivity.this, "connect to server failed!", Toast.LENGTH_LONG).show();
                    Log.e("E","fail to connect to the server");
                    finish();
                    break;
                case -101:
                    Toast.makeText(SenSorActivity.this, "send data to server failed!", Toast.LENGTH_LONG).show();
                    Log.e("E","fail to send data to the server");
                    finish();
                    break;
            }
        }
    }
}
