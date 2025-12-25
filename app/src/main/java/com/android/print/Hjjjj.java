package com.android.print;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;

import com.programe.print.Printstart;

public class Hjjjj extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_printer);
        
        try {
            // 连接打印机按钮
            Button btnConnect = findViewById(R.id.btnConnect);
            btnConnect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 启动蓝牙搜索和打印流程
                    Printstart.INSTANCE.startPrint("");
                }
            });
            
            // 打印按钮
            Button btnPrint = findViewById(R.id.btnPrint);
            btnPrint.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 启动蓝牙搜索和打印流程
                    Printstart.INSTANCE.startPrint("");
                }
            });
            
        } catch (Exception e) {
            Log.e("Hjjjj", "初始化错误: " + e.getMessage());
        }
    }
}
