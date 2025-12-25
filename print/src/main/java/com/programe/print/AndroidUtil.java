package com.programe.print;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;


public class AndroidUtil {


    private static DisplayMetrics DISPLAY_METRICES = null;
    /**
     * 获取屏幕宽度
     *
     * @return
     */
    public static int getScreenWidth(Context context) {
        DisplayMetrics displayMetrics = getDisplayMetrics(context);
        return displayMetrics.widthPixels;
    }

    /**
     * 获取屏幕高度
     *
     * @return
     */
    public static int getScreenHeight(Context context) {
        DisplayMetrics displayMetrics = getDisplayMetrics(context);
        return displayMetrics.heightPixels;
    }

    /**
     * @param @param  context
     * @param @return 设定文件
     * @return DisplayMetrics 返回类型
     * @throws
     * @Title: getDisplayMetrics
     * @Description: TODO
     */
    public static DisplayMetrics getDisplayMetrics(Context pContext) {
        if (DISPLAY_METRICES == null) {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            WindowManager wm = (WindowManager) pContext.getSystemService(Context.WINDOW_SERVICE);
            wm.getDefaultDisplay().getMetrics(displayMetrics);
            DISPLAY_METRICES = displayMetrics;
        }
        return DISPLAY_METRICES;
    }
}
