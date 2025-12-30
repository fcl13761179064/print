package com.programe.print;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.programe.print.print.GPrinterCommand;
import com.programe.print.print.PrintPic;
import com.programe.print.print.PrintQueue;
import com.programe.print.print.PrintUtil;
import com.programe.print.printutil.PrintOrderDataMaker;
import com.programe.print.printutil.PrinterWriter;
import com.programe.print.printutil.PrinterWriter58mm;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 * Created by liuguirong on 8/1/17.
 * <p/>
 * print ticket service
 */
public class BtService extends IntentService {

    public BtService() {
        super("BtService");
    }

    public BtService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("BtService", "========= onHandleIntent 被调用 =========");
        if (intent == null || intent.getAction() == null) {
            Log.e("BtService", "intent 或 action 为空");
            return;
        }
        Log.d("BtService", "Action: " + intent.getAction());

        if (intent.getAction().equals(PrintUtil.ACTION_PRINT_TEST)) {
            String data = intent.getStringExtra("PRINT_DATA");
            printTest(data);
        } else if (intent.getAction().equals(PrintUtil.ACTION_PRINT_TEST_TWO)) {
            printTesttwo(3);
        } else if (intent.getAction().equals(PrintUtil.ACTION_PRINT_BITMAP)) {
            String ss = intent.getStringExtra("PRINT_DATA");
            if (ss != null) {
                printBitmapTest(ss);
            }
        }
    }

    private void printTest(String data) {
        Log.d("BtService", "========= printTest 开始 =========");
        PrintOrderDataMaker printOrderDataMaker = new PrintOrderDataMaker(this, data, PrinterWriter58mm.TYPE_58, PrinterWriter.HEIGHT_PARTING_DEFAULT);
        
        // 1. 尝试从 assets 加载 gaizhang.png
        android.graphics.Bitmap assetsSeal = Base64PrintUtils.INSTANCE.getBitmapFromAssets(this, "gaizhang.png");
        if (assetsSeal != null) {
            printOrderDataMaker.setSealBitmap(assetsSeal);
            Log.d("BtService", "已采用 assets 中的 gaizhang.png 作为印章");
        }
        
        ArrayList<byte[]> printData = (ArrayList<byte[]>) printOrderDataMaker.getPrintData(PrinterWriter58mm.TYPE_58);
        PrintQueue.getQueue(getApplicationContext()).add(printData);
        
        // 记得及时回收 assetsSeal (如果在 Maker 中没回收)
        // 逻辑已调整：Maker 内部调用合成，合成后由于返回了新的 Bitmap，原始 sealBitmap 需要在这里回收
        if (assetsSeal != null) {
            // assetsSeal.recycle(); // 谨慎：如果 getPrintData 是同步的且已经用完，可以回收
        }
        
        Log.d("BtService", "打印数据已添加到队列");
    }

    private void printTesttwo(int num) {
        try {
            ArrayList<byte[]> bytes = new ArrayList<byte[]>();
            for (int i = 0; i < num; i++) {
                String message = "蓝牙打印测试\n蓝牙打印测试\n蓝牙打印测试\n\n";
                bytes.add(GPrinterCommand.reset);
                bytes.add(message.getBytes("gbk"));
                bytes.add(GPrinterCommand.print);
                bytes.add(GPrinterCommand.print);
                bytes.add(GPrinterCommand.print);
            }
            PrintQueue.getQueue(getApplicationContext()).add(bytes);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void printBitmapTest(String singUrl) {
        Base64PrintUtils.INSTANCE.printSealImage(singUrl, bitmap -> {
            try {
                PrintPic printPic = PrintPic.getInstance();
                printPic.init(bitmap);

                if (!bitmap.isRecycled()) {
                    bitmap.recycle();
                }
                byte[] bytes = printPic.printDraw();
                ArrayList<byte[]> printBytes = new ArrayList<>();
                
                // 打印指令集
                printBytes.add(new byte[]{0x1B, 0x61, 0x01}); // 居中
                printBytes.add(bytes);
                printBytes.add(new byte[]{0x0A, 0x0A, 0x0A});
                printBytes.add(GPrinterCommand.print);
                
                byte[] allBytes = combineByteArrays(printBytes);
                PrintQueue.getQueue(getApplicationContext()).addTwo(allBytes);
            } catch (Exception e) {
                Log.e("BtService", "打印异常", e);
            }
            return null;
        });
    }

    private byte[] combineByteArrays(ArrayList<byte[]> arrays) {
        int totalLength = 0;
        for (byte[] array : arrays) {
            totalLength += array.length;
        }
        byte[] result = new byte[totalLength];
        int currentPos = 0;
        for (byte[] array : arrays) {
            System.arraycopy(array, 0, result, currentPos, array.length);
            currentPos += array.length;
        }
        return result;
    }
}