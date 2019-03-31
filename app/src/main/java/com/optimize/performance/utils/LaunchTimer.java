package com.optimize.performance.utils;

/**
 * 启动时间测量
 * 辅助打点
 */
public class LaunchTimer {

    // 开始时间
    private static long sTime;

    public static void startRecord() {
        sTime = System.currentTimeMillis();
    }

    public static void endRecord() {
        endRecord("");
    }

    public static void endRecord(String msg) {
        long cost = System.currentTimeMillis() - sTime;
        LogUtils.i(msg + "cost " + cost);
    }

}
