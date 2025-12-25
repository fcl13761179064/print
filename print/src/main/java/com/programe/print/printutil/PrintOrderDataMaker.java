package com.programe.print.printutil;

import android.content.Context;

import com.blankj.utilcode.util.GsonUtils;
import com.programe.print.PrintBeans;
import com.programe.print.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


/**
 * 测试数据生成器
 * Created by liuguirong on 8/1/17.
 */

public class PrintOrderDataMaker implements PrintDataMaker {


    private String qr;
    private int width;
    private int height;
    Context btService;
    private String remark = "微点筷客推出了餐厅管理系统，可用手机快速接单（来自客户的预订订单），进行订单管理、后厨管理等管理餐厅。";


    public PrintOrderDataMaker( Context btService, String qr, int width, int height) {
        this.qr = qr;
        this.width = width;
        this.height = height;
        this.btService = btService;
    }



    @Override
    public List<byte[]> getPrintData(int type) {
        ArrayList<byte[]> data = new ArrayList<>();

        try {
            PrintBeans printBeans = GsonUtils.fromJson(qr, PrintBeans.class);
            PrinterWriter printer;
            printer = type == PrinterWriter58mm.TYPE_58 ? new PrinterWriter58mm(height, width) : new PrinterWriter80mm(height, width);
            printer.setAlignCenter();
            data.add(printer.getDataAndReset());
           /*
            ArrayList<byte[]> image1 = printer.getImageByte(btService.getResources(), R.drawable.company);
            data.addAll(image1);
            */
            printer.setAlignCenter();
            printer.setEmphasizedOn();
            printer.setFontSize(2);
            printer.print(printBeans.getTitle());
            printer.printLineFeed();
            printer.setEmphasizedOff();
            printer.printLineFeed();

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
            printer.print("身份证："+ printBeans.getParty_id_card());
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
            printer.print("缴纳方式：" + printBeans.getPenalty_type());
            printer.printLineFeed();
            if (!printBeans.getPay_address().isBlank()){
                printer.print("缴纳地点：" + printBeans.getPay_address());
                printer.printLineFeed(); // 现在才换行
            }
            printer.print("备注（案由）：" + printBeans.getRemark());
            printer.printLineFeed();
            printer.print("单位盖章 ：");
            printer.printLineFeed();
            data.add(printer.getDataAndClose());
            return data;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }


}
