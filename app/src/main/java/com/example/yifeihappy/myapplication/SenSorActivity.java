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
import java.util.List;

/**
 * Created by yifeihappy on 2017/8/30.
 */

public class SenSorActivity extends Activity implements SensorEventListener{

    public SensorManager sensorManage;
    private Sensor mAccelerometer;
    private Sensor mGravity;
    private Sensor mGyroscope;
    private TextView xTxv,yTxv,zTxv,sysTimeTxv;
    public Handler handlerUI;
    private SocketThread threadSocket;
    private String msgStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sensor_layout);

        xTxv = (TextView)findViewById(R.id.x);
        yTxv = (TextView)findViewById(R.id.y);
        zTxv = (TextView)findViewById(R.id.z);
        sysTimeTxv = (TextView)findViewById(R.id.sys_time);

        //get the sensor manager object.
        sensorManage = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        handlerUI = new UIHandler();

        threadSocket = new SocketThread(handlerUI, sensorManage, this);
        new Thread(threadSocket).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        threadSocket = new SocketThread(handlerUI, sensorManage, this);
//        new Thread(threadSocket).start();

    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onStop() {
        super.onStop();
//        if(threadSocket.os != null) {
//            try {
//                threadSocket.os.close();
//                Log.d("DEB","os.close()");
//            } catch (IOException e) {
//                e.printStackTrace();
//            } finally {
//                try
//                {
//                    if(threadSocket.br != null)
//                    {
//                        threadSocket.br.close();
//                        Log.d("DEB", "br.close()");
//                    }
//                }
//                catch (IOException e)
//                {
//                    e.printStackTrace();
//                }
//                finally {
//                    if(threadSocket.s != null) {
//                        try {
//                            threadSocket.s.close();
//                            Log.d("DEB","s.close");
//                        } catch (IOException e1) {
//                            e1.printStackTrace();
//                        }
//                    }
//                }
//            }
//        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(threadSocket.os != null) {
            try {
                threadSocket.os.close();
                Log.d("DEB","os.close()");
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try
                {
                    if(threadSocket.br != null)
                    {
                        threadSocket.br.close();
                        Log.d("DEB", "br.close()");
                    }
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                finally {
                    if(threadSocket.s != null) {
                        try {
                            threadSocket.s.close();
                            Log.d("DEB","s.close");
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            }
        }
        sensorManage.unregisterListener(this);

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float x, y, z;
        int sensorType = sensorEvent.sensor.getType();
        long curTime = System.currentTimeMillis();
        Message msgUI = Message.obtain();
        Message msgSocket = Message.obtain();
        msgSocket.obj = null;
        Bundle bundle = new Bundle();
        switch (sensorType)
        {
            //需要添加传感器类型在这里添加case分支
            case Sensor.TYPE_ACCELEROMETER:
                bundle.putString("t", ""+curTime);
                bundle.putString("x",""+sensorEvent.values[0]);
                bundle.putString("y",""+sensorEvent.values[1]);
                bundle.putString("z",""+sensorEvent.values[2]);
                msgUI.what = Sensor.TYPE_ACCELEROMETER;
                msgUI.setData(bundle);
                handlerUI.sendMessage(msgUI);//更新UI

                msgStr = new String(Sensor.TYPE_ACCELEROMETER + ","+curTime+","+sensorEvent.values[0]+","+sensorEvent.values[1]+","+sensorEvent.values[2]+"\n");
                msgSocket.what = Sensor.TYPE_ACCELEROMETER;
                msgSocket.obj = msgStr;
                break;
            case Sensor.TYPE_GRAVITY:
                msgStr = new String(Sensor.TYPE_GRAVITY + ","+curTime+","+sensorEvent.values[0]+","+sensorEvent.values[1]+","+sensorEvent.values[2]+"\r\n");
                msgSocket.what = Sensor.TYPE_GRAVITY;
                msgSocket.obj = msgStr;
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                msgStr = new String(Sensor.TYPE_MAGNETIC_FIELD + ","+curTime+","+sensorEvent.values[0]+","+sensorEvent.values[1]+","+sensorEvent.values[2]+"\r\n");
                msgSocket.what = Sensor.TYPE_MAGNETIC_FIELD;
                msgSocket.obj = msgStr;
                break;
            case Sensor.TYPE_GYROSCOPE:
                msgStr = new String(Sensor.TYPE_GYROSCOPE + ","+curTime+","+sensorEvent.values[0]+","+sensorEvent.values[1]+","+sensorEvent.values[2]+"\r\n");
                msgSocket.what = Sensor.TYPE_GYROSCOPE;
                msgSocket.obj = msgStr;
                break;

        }


        if(threadSocket.sendHandler!=null && !threadSocket.s.isClosed() && msgSocket.obj != null) {
            threadSocket.sendHandler.sendMessage(msgSocket);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    class  UIHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle bundle = msg.getData();
            switch (msg.what) {
                case Sensor.TYPE_ACCELEROMETER:
                    sysTimeTxv.setText("T:"+bundle.getString("t"));
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
                case Sensor.TYPE_GRAVITY:
                case Sensor.TYPE_MAGNETIC_FIELD:
                case Sensor.TYPE_GYROSCOPE:
                    sysTimeTxv.setText("T:"+bundle.getString("t"));
                    break;
            }
        }
    }
}
