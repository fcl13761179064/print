package com.programe.print;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;
import androidx.core.app.ActivityCompat;
import com.alibaba.fastjson.JSONObject;
import com.blankj.utilcode.util.ToastUtils;
import com.programe.print.bt.BluetoothActivity;
import com.programe.print.bt.BtUtil;
import com.programe.print.print.PrintQueue;
import com.programe.print.print.PrintUtil;
import com.programe.print.util.ToastUtil;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import io.dcloud.feature.uniapp.bridge.UniJSCallback;
import java.lang.reflect.Method;

/**
 * 蓝牙搜索界面
 * Created by liuguirong on 2017/8/3.
 */

public class SearchBluetoothActivity extends BluetoothActivity implements AdapterView.OnItemClickListener, View.OnClickListener {

    private BluetoothAdapter bluetoothAdapter;
    private ListView lv_searchblt;
    private TextView tv_title;
    private TextView tv_summary;
    private SearchBleAdapter searchBleAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("SearchBluetooth", "========= SearchBluetoothActivity onCreate 开始 =========");

        try {
            String printData = getIntent().getStringExtra("PRINT_DATA");
            Log.d("SearchBluetooth", "接收到的打印数据: " + (printData != null ? printData.substring(0, Math.min(100, printData.length())) + "..." : "null"));

            Log.d("SearchBluetooth", "准备 setContentView...");
            setContentView(R.layout.activity_searchbooth);
            Log.d("SearchBluetooth", "setContentView 完成");

            lv_searchblt = (ListView) findViewById(R.id.lv_searchblt);
            tv_title = (TextView) findViewById(R.id.tv_title);
            tv_summary = (TextView) findViewById(R.id.tv_summary);
            Log.d("SearchBluetooth", "findViewById 完成");

            //初始化蓝牙适配器
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            searchBleAdapter = new SearchBleAdapter(SearchBluetoothActivity.this, null);
            lv_searchblt.setAdapter(searchBleAdapter);
            Log.d("SearchBluetooth", "蓝牙适配器初始化完成");

            init();
            Log.d("SearchBluetooth", "init() 完成");

            lv_searchblt.setOnItemClickListener(this);
            tv_title.setOnClickListener(this);
            tv_summary.setOnClickListener(this);

            // 注册 EventBus - 使用反射避免类加载失败
            Log.d("SearchBluetooth", "准备注册 EventBus...");
            registerEventBusSafely();
            Log.d("SearchBluetooth", "EventBus 注册完成");

        } catch (Throwable e) {
            Log.e("SearchBluetooth", "onCreate 异常: " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace();
        }

        Log.d("SearchBluetooth", "========= SearchBluetoothActivity onCreate 结束 =========");
    }

    /**
     * 安全地注册 EventBus，避免类加载失败导致崩溃
     */
    private void registerEventBusSafely() {
        com.programe.print.util.EventBusHelper.registerSafely(this);
    }


    @SuppressLint("SetTextI18n")
    private void init() {
        Log.d("SearchBluetooth", "========= init() 方法被调用 =========");

        if (!BtUtil.isOpen(bluetoothAdapter)) {
            Log.d("SearchBluetooth", "蓝牙未开启");
            tv_title.setText("未连接蓝牙打印机");
            tv_title.setTextColor(getResources().getColor(R.color.red));
            tv_summary.setText("系统蓝牙已关闭,点击开启");
            searchDeviceOrOpenBluetooth();

        } else {
            Log.d("SearchBluetooth", "蓝牙已开启");
            if (!PrintUtil.isBondPrinter(this, bluetoothAdapter)) {
                //未绑定蓝牙打印机器
                Log.d("SearchBluetooth", "未绑定蓝牙打印机");
                tv_title.setText("未连接蓝牙打印机");
                tv_title.setTextColor(getResources().getColor(R.color.red));
                tv_summary.setText("点击后搜索蓝牙打印机");
                searchDeviceOrOpenBluetooth();

            } else {
                //已绑定蓝牙设备
                Log.d("SearchBluetooth", "已绑定蓝牙打印机");
                tv_title.setText(getPrinterName() + "已连接");
                String blueAddress = PrintUtil.getDefaultBluethoothDeviceAddress(this);
                Log.d("SearchBluetooth", "蓝牙地址: " + blueAddress);
                if (TextUtils.isEmpty(blueAddress)) {
                    blueAddress = "点击后搜索蓝牙打印机";
                }
                tv_summary.setText(blueAddress);

                // 显示 Toast - 可选，失败不影响主流程
                try {
                    showToast("开始打印");
                    android.util.Log.d("PrintUtils", "Toast 显示完成");
                } catch (Throwable e) {
                    android.util.Log.w("PrintUtils", "Toast 显示失败（非致命）:" + e.getMessage());
                }
                String printData = getIntent().getStringExtra("PRINT_DATA");
                Log.d("SearchBluetooth", "准备启动 BtService 打印...");
                Log.d("SearchBluetooth", "打印数据: " + (printData != null ? printData.substring(0, Math.min(100, printData.length())) + "..." : "null"));
                Intent intent = new Intent(getApplicationContext(), BtService.class);
                intent.setAction(PrintUtil.ACTION_PRINT_TEST);
                intent.putExtra("PRINT_DATA", printData);
                startService(intent);
                Log.d("SearchBluetooth", "BtService 已启动");
            }
        }
    }

    private void showToast(String message) {
        ToastUtils.getDefaultMaker().setTextColor(Color.WHITE);
        ToastUtils.getDefaultMaker().setBgResource(R.drawable.fm_toast_bg);
        ToastUtils.getDefaultMaker().setGravity(Gravity.CENTER, 0, 5);
        ToastUtils.getDefaultMaker().setNotUseSystemToast().setDurationIsLong(false);
        ToastUtils.showShort(message, 50);
    }

    @Override
    public void btStatusChanged(Intent intent) {

        if (bluetoothAdapter.getState() == BluetoothAdapter.STATE_OFF) {//蓝牙被关闭时强制打开
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            bluetoothAdapter.enable();
        }
        if (bluetoothAdapter.getState() == BluetoothAdapter.STATE_ON) {//蓝牙打开时搜索蓝牙
            searchDeviceOrOpenBluetooth();
        }
    }

    private String getPrinterName() {
        String dName = PrintUtil.getDefaultBluetoothDeviceName(this);
        if (TextUtils.isEmpty(dName)) {
            dName = "未知设备";
        }
        return dName;
    }

    private String getPrinterName(String dName) {
        if (TextUtils.isEmpty(dName)) {
            dName = "未知设备";
        }
        return dName;
    }

    /**
     * 开始搜索
     * search device
     */
    private void searchDeviceOrOpenBluetooth() {
        if (BtUtil.isOpen(bluetoothAdapter)) {
            BtUtil.searchDevices(bluetoothAdapter);
        }
    }

    /**
     * 关闭搜索
     * cancel search
     */
    @Override
    protected void onStop() {
        super.onStop();
        BtUtil.cancelDiscovery(bluetoothAdapter);
    }

    @Override
    public void btStartDiscovery(Intent intent) {
        tv_title.setText("正在搜索蓝牙设备…");
        tv_summary.setText("");
    }

    @Override
    public void btFinishDiscovery(Intent intent) {
        tv_title.setText("搜索完成");
        tv_summary.setText("点击重新搜索");
    }

    @Override
    public void btFoundDevice(Intent intent) {
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        Log.d("1", "!");
        if (null != bluetoothAdapter && device != null) {
            searchBleAdapter.addDevices(device);
            String dName = device.getName() == null ? "未知设备" : device.getName();
            Log.d("未知设备", dName);
            Log.d("1", "!");
        }
    }

    @Override
    public void btBondStatusChange(Intent intent) {
        super.btBondStatusChange(intent);
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        switch (device.getBondState()) {
            case BluetoothDevice.BOND_BONDING://正在配对
                Log.d("BlueToothTestActivity", "正在配对......");
                break;
            case BluetoothDevice.BOND_BONDED://配对结束
                Log.d("BlueToothTestActivity", "完成配对");
                connectBlt(device);
                break;
            case BluetoothDevice.BOND_NONE://取消配对/未配对
                Log.d("BlueToothTestActivity", "取消配对");
            default:
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

        if (null == searchBleAdapter) {
            return;
        }
        final BluetoothDevice bluetoothDevice = searchBleAdapter.getItem(position);
        if (null == bluetoothDevice) {
            return;
        }
        new AlertDialog.Builder(this).setTitle("绑定" + getPrinterName(bluetoothDevice.getName()) + "?").setMessage("点击确认绑定蓝牙设备").setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    BtUtil.cancelDiscovery(bluetoothAdapter);


                    if (bluetoothDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                        connectBlt(bluetoothDevice);
                    } else {
                        Method createBondMethod = BluetoothDevice.class.getMethod("createBond");
                        createBondMethod.invoke(bluetoothDevice);
                    }
                    PrintQueue.getQueue(getApplicationContext()).disconnect();
                    String name = bluetoothDevice.getName();
                } catch (Exception e) {
                    e.printStackTrace();
                    PrintUtil.setDefaultBluetoothDeviceAddress(getApplicationContext(), "");
                    PrintUtil.setDefaultBluetoothDeviceName(getApplicationContext(), "");
                    ToastUtil.showToast(SearchBluetoothActivity.this, "蓝牙绑定失败,请重试");
                }
            }
        }).create().show();


    }

    /***
     * 配对成功连接蓝牙
     * @param bluetoothDevice
     */

    private void connectBlt(BluetoothDevice bluetoothDevice) {
        if (null != searchBleAdapter) {
            searchBleAdapter.setConnectedDeviceAddress(bluetoothDevice.getAddress());
        }
        searchBleAdapter.notifyDataSetChanged();
        PrintUtil.setDefaultBluetoothDeviceAddress(getApplicationContext(), bluetoothDevice.getAddress());
        PrintUtil.setDefaultBluetoothDeviceName(getApplicationContext(), bluetoothDevice.getName());
        init();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.tv_summary) {
            searchDeviceOrOpenBluetooth();
        }
    }

    // 接收服务完成事件
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onServiceComplete(Boolean isPrintSuccess) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // 调用保存的回调，通知前端打印结果
                showToast(isPrintSuccess ? "打印成功" : "打印失败");
                invokeCallback(isPrintSuccess);
            }
        }, 3000);
    }

    /**
     * 调用前端回调
     */
    private void invokeCallback(boolean success) {
        try {
            UniJSCallback callback = PrintUtils.getPrintCallback();
            if (callback != null) {
                JSONObject result = new JSONObject();
                result.put("success", success);
                result.put("message", success ? "打印成功" : "打印失败");
                callback.invoke(result);
                // 清空回调，防止重复调用
                PrintUtils.setPrintCallback(null);
                Log.d("SearchBluetoothActivity", "回调已发送: " + result);
            } else {
                Log.w("SearchBluetoothActivity", "回调为空，无法通知前端");
            }
            finish();
        } catch (Exception e) {
            finish();
            Log.e("SearchBluetoothActivity", "调用回调异常: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        Log.d("SearchBluetooth", "========= onDestroy 开始 =========");

        // 注销 EventBus - 使用安全方式
        unregisterEventBusSafely();

        // 如果 Activity 被销毁但回调还未调用，返回失败
        try {
            UniJSCallback callback = PrintUtils.getPrintCallback();
            if (callback != null) {
                JSONObject result = new JSONObject();
                result.put("success", false);
                result.put("message", "打印被取消");
                callback.invoke(result);
                PrintUtils.setPrintCallback(null);
                Log.d("SearchBluetooth", "已通知前端打印被取消");
            }
        } catch (Throwable e) {
            Log.e("SearchBluetooth", "onDestroy 回调异常: " + e.getMessage());
            e.printStackTrace();
        }

        super.onDestroy();
        Log.d("SearchBluetooth", "========= onDestroy 结束 =========");
    }

    /**
     * 安全地注销 EventBus
     */
    private void unregisterEventBusSafely() {
        com.programe.print.util.EventBusHelper.unregisterSafely(this);
    }
}
