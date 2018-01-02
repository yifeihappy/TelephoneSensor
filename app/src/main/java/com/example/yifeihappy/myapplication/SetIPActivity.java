package com.example.yifeihappy.myapplication;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by yifeihappy on 2017/8/31.
 */

public class SetIPActivity extends Activity {
    private Button ipBtn;
    private EditText ipEdt;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    String IP = null;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ip_layout);
        ipBtn = (Button)findViewById(R.id.ip_btn);
        ipEdt = (EditText)findViewById(R.id.ip_edt);
        preferences  = getSharedPreferences("IP", Context.MODE_PRIVATE);
        editor = preferences.edit();
        IP = preferences.getString("IP","192.168.0.10");
        ipEdt.setText(IP);
        ipBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ipEdt.getText()!=null) {
                    IP = ipEdt.getText().toString();
                    editor.putString("IP",IP);
                    editor.commit();
                    SocketThread.setIP(IP);
                    finish();
                }
            }
        });

    }
}
