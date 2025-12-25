package com.programe.print.util;

import android.util.Log;

/**
 * EventBus 安全包装器
 * 用于避免 EventBus 类加载失败时导致整个应用崩溃
 */
public class EventBusHelper {
    
    private static final String TAG = "EventBusHelper";
    private static Boolean eventBusAvailable = null;
    
    /**
     * 检查 EventBus 是否可用
     */
    public static boolean isEventBusAvailable() {
        if (eventBusAvailable == null) {
            try {
                Class.forName("org.greenrobot.eventbus.EventBus");
                eventBusAvailable = true;
                Log.d(TAG, "EventBus 类可用");
            } catch (Throwable e) {
                eventBusAvailable = false;
                Log.e(TAG, "EventBus 类不可用: " + e.getClass().getName() + " - " + e.getMessage());
            }
        }
        return eventBusAvailable;
    }
    
    /**
     * 安全地发送事件
     */
    public static void postSafely(Object event) {
        if (!isEventBusAvailable()) {
            Log.w(TAG, "EventBus 不可用，跳过事件: " + event);
            return;
        }
        try {
            org.greenrobot.eventbus.EventBus.getDefault().post(event);
        } catch (Throwable e) {
            Log.e(TAG, "发送事件失败: " + e.getMessage());
        }
    }
    
    /**
     * 安全地注册订阅者
     */
    public static void registerSafely(Object subscriber) {
        if (!isEventBusAvailable()) {
            Log.w(TAG, "EventBus 不可用，跳过注册: " + subscriber.getClass().getName());
            return;
        }
        try {
            if (!org.greenrobot.eventbus.EventBus.getDefault().isRegistered(subscriber)) {
                org.greenrobot.eventbus.EventBus.getDefault().register(subscriber);
            }
        } catch (Throwable e) {
            Log.e(TAG, "注册失败: " + e.getMessage());
        }
    }
    
    /**
     * 安全地注销订阅者
     */
    public static void unregisterSafely(Object subscriber) {
        if (!isEventBusAvailable()) {
            Log.w(TAG, "EventBus 不可用，跳过注销: " + subscriber.getClass().getName());
            return;
        }
        try {
            if (org.greenrobot.eventbus.EventBus.getDefault().isRegistered(subscriber)) {
                org.greenrobot.eventbus.EventBus.getDefault().unregister(subscriber);
            }
        } catch (Throwable e) {
            Log.e(TAG, "注销失败: " + e.getMessage());
        }
    }
}

