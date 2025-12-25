package com.programe.print;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.programe.print.base.AppInfo;
import com.programe.print.bt.BluetoothActivity;
import com.programe.print.print.PrintMsgEvent;
import com.programe.print.print.PrintUtil;
import com.programe.print.print.PrinterMsgType;
import com.programe.print.util.ToastUtil;

import org.greenrobot.eventbus.EventBus;


/***
 *  Created by liugruirong on 2017/8/3.
 */
public class MainActivity extends BluetoothActivity {

     TextView tv_bluename;
     TextView tv_blueadress;
      boolean mBtEnable = true;
    int PERMISSION_REQUEST_COARSE_LOCATION=2;
    /**
     * bluetooth adapter
     */
    BluetoothAdapter mAdapter;
    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv_bluename =findViewById(R.id.tv_bluename);
        tv_blueadress =findViewById(R.id.tv_blueadress);
        findViewById(R.id.button4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, SearchBluetoothActivity.class));

            }
        });
        findViewById(R.id.button5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(AppInfo.btAddress)){
                    ToastUtil.showToast(MainActivity.this,"请连接蓝牙...");
                    startActivity(new Intent(MainActivity.this,SearchBluetoothActivity.class));
                }else {
                    if ( mAdapter.getState()==BluetoothAdapter.STATE_OFF ){//蓝牙被关闭时强制打开
                        mAdapter.enable();
                        ToastUtil.showToast(MainActivity.this,"蓝牙被关闭请打开...");
                    }else {
                        ToastUtil.showToast(MainActivity.this,"打印测试...");
                        Intent intent = new Intent(getApplicationContext(), BtService.class);
                        intent.setAction(PrintUtil.ACTION_PRINT_TEST);
                        startService(intent);
                    }

                }
            }
        });
        findViewById(R.id.button6).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(AppInfo.btAddress)){
                    ToastUtil.showToast(MainActivity.this,"请连接蓝牙...");
                    startActivity(new Intent(MainActivity.this,SearchBluetoothActivity.class));
                }else {
                    ToastUtil.showToast(MainActivity.this,"打印测试...");
                    Intent intent2 = new Intent(getApplicationContext(), BtService.class);
                    intent2.setAction(PrintUtil.ACTION_PRINT_TEST_TWO);
                    startService(intent2);

                }
            }
        });
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(AppInfo.btAddress)){
                    ToastUtil.showToast(MainActivity.this,"请连接蓝牙...");
                    startActivity(new Intent(MainActivity.this,SearchBluetoothActivity.class));
                }else {
                    ToastUtil.showToast(MainActivity.this,"打印图片...");
                    Intent intent2 = new Intent(getApplicationContext(), BtService.class);
                    intent2.setAction(PrintUtil.ACTION_PRINT_BITMAP);
                    startService(intent2);

                }
            }
        });
        //6.0以上的手机要地理位置权限
        requestPermissions(new String[]{ Manifest.permission.BLUETOOTH_CONNECT,Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.ACCESS_FINE_LOCATION }, PERMISSION_REQUEST_COARSE_LOCATION);
       // EventBus.getDefault().register(MainActivity.this);

    }

    @Override
    protected void onStart() {
        super.onStart();
        BluetoothController.init(this);
    }




    @Override
    public void btStatusChanged(Intent intent) {
        super.btStatusChanged(intent);
        BluetoothController.init(this);
    }


    /**
     * handle printer message
     *
     * @param event print msg event
     */

    // 添加这个注解方法 ↓
    public void onEventMainThread(PrintMsgEvent event) {
        if (event.type == PrinterMsgType.MESSAGE_TOAST) {
            ToastUtil.showToast(MainActivity.this,event.msg);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().register(MainActivity.this);
    }
}
