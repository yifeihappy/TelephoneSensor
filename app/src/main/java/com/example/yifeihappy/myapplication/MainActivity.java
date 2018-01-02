package com.example.yifeihappy.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button sensorBtn;
    private Button IPBtn;
    public static boolean FIRSTRUN_B = false;
    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorBtn = (Button)findViewById(R.id.sensor_btn);
        IPBtn = (Button)findViewById(R.id.IP_btn);
        sensorBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sensor_intent = new Intent(MainActivity.this, SenSorActivity.class);
                sensor_intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(sensor_intent);
            }
        });
        IPBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent setIpIntent = new Intent(MainActivity.this, SetIPActivity.class);
                setIpIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(setIpIntent);
            }
        });

        preferences = getSharedPreferences("IP", Context.MODE_PRIVATE);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(preferences.getString("IP",null)==null)
        {
            sensorBtn.setEnabled(false);
        }else
        {
            sensorBtn.setEnabled(true);
        }
    }
}
