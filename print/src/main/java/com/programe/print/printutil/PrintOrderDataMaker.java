package com.programe.print.printutil;

import android.content.Context;
import com.blankj.utilcode.util.GsonUtils;
import com.programe.print.Base64PrintUtils;
import com.programe.print.PrintBeans;
import com.programe.print.R;
import java.util.ArrayList;
import java.util.List;

public class PrintOrderDataMaker implements PrintDataMaker {

    private String qr;
    private int width;
    private int height;
    Context btService;
    private String sealBase64;
    private android.graphics.Bitmap sealBitmap;

    public PrintOrderDataMaker(Context btService, String qr, int width, int height) {
        this.qr = qr;
        this.width = width;
        this.height = height;
        this.btService = btService;
    }

    public void setSealBase64(String sealBase64) {
        this.sealBase64 = sealBase64;
    }

    public void setSealBitmap(android.graphics.Bitmap sealBitmap) {
        this.sealBitmap = sealBitmap;
    }

    @Override
    public List<byte[]> getPrintData(int type) {
        ArrayList<byte[]> data = new ArrayList<>();

        try {
            PrintBeans printBeans = GsonUtils.fromJson(qr, PrintBeans.class);
            PrinterWriter printer = type == PrinterWriter58mm.TYPE_58 ? new PrinterWriter58mm(height, width) : new PrinterWriter80mm(height, width);
            
            printer.setAlignCenter();
            data.add(printer.getDataAndReset());
            
            // Title
            printer.setAlignCenter();
            printer.setEmphasizedOn();
            printer.setFontSize(2);
            printer.print(printBeans.getTitle());
            printer.printLineFeed();
            printer.setEmphasizedOff();
            printer.printLineFeed();

            // Case Info
            printer.setFontSize(0);
            printer.setAlignLeft();
            printer.print("案件编号：" + printBeans.getNumber());
            printer.printLineFeed();
            printer.print("姓名: " + printBeans.getParty_name());
            printer.printLineFeed();
            printer.print("性别: " + printBeans.getParty_gender());
            printer.printLineFeed();
            printer.print("年龄: " + printBeans.getParty_age());
            printer.printLineFeed();
            printer.print("家庭住址:" + printBeans.getParty_address());
            printer.printLineFeed();
            printer.print("联系方式：" + printBeans.getParty_phone());
            printer.printLineFeed();
            printer.print("身份证：" + printBeans.getParty_id_card());
            printer.printLineFeed();

            printer.print("案件来源：" + printBeans.getFrom_type());
            printer.printLineFeed();
            printer.print("案件类型：" + printBeans.getCase_type_id());
            printer.printLineFeed();
            printer.print("发生时间：" + printBeans.getJuridical_person_img_a());
            printer.printLineFeed();
            printer.print("发生地点：" + printBeans.getAddress());
            printer.printLineFeed();
            printer.print("违法主体：" + printBeans.getSubject());
            printer.printLineFeed();
            printer.print("违法行为：" + printBeans.getBehavior());
            printer.printLineFeed();
            printer.print("危害后果：" + printBeans.getHazard());
            printer.printLineFeed();
            printer.print("罚款数额：" + printBeans.getPenalty_amount());
            printer.printLineFeed();
            // Payment info - normally printed now
            printer.print("缴纳方式：" + printBeans.getPenalty_type());
            printer.printLineFeed();
            if (!printBeans.getPay_address().isEmpty()) {
                printer.print("缴纳地点：" + printBeans.getPay_address());
                printer.printLineFeed();
            }
            // Footer block (Remark + Seal)
            // If seal is available, we synthesize the last part to achieve the overlay effect
            if (sealBitmap != null || (sealBase64 != null && !sealBase64.isEmpty())) {
                data.add(printer.getDataAndReset()); // Flush preceding text
                ArrayList<String> footerLines = new ArrayList<>();
                footerLines.add("备注（案由）：" + printBeans.getRemark());
                footerLines.add("单位盖章 ：");
                int printWidth = (type == PrinterWriter58mm.TYPE_58) ? 384 : 576;
                android.graphics.Bitmap compositeBitmap;
                if (sealBitmap != null) {
                    compositeBitmap = Base64PrintUtils.INSTANCE.compositeSealWithMultiLines(footerLines, sealBitmap, printWidth);
                } else {
                    compositeBitmap = Base64PrintUtils.INSTANCE.compositeSealWithText(footerLines.get(0), sealBase64, printWidth); // Fallback to single line if only base64 provided and old method used
                    // Actually I should update my implementation to use multiline even for base64
                }

                // Re-implementation check: compositeSealWithText already calls compositeSealWithMultiLines now
                if (sealBitmap == null && sealBase64 != null) {
                     android.graphics.Bitmap base64SealBitmap = Base64PrintUtils.INSTANCE.getSealBitmap(sealBase64);
                     if (base64SealBitmap != null) {
                         compositeBitmap = Base64PrintUtils.INSTANCE.compositeSealWithMultiLines(footerLines, base64SealBitmap, printWidth);
                     }
                }

                if (compositeBitmap != null) {
                    ArrayList<byte[]> imageBytes = printer.getImageByte(compositeBitmap);
                    if (imageBytes != null) {
                        data.addAll(imageBytes);
                    }
                } else {
                    // Fallback to normal printing if synthesis fails
                    printer.print("备注（案由）：" + printBeans.getRemark());
                    printer.printLineFeed();
                    printer.print("单位盖章 ：");
                    printer.printLineFeed();
                }
            } else {
                printer.print("备注（案由）：" + printBeans.getRemark());
                printer.printLineFeed();
                printer.print("单位盖章 ：");
                printer.printLineFeed();
            }

            data.add(printer.getDataAndClose());
            return data;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
