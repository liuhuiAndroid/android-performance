package com.optimize.performance.net;

/**
 * 统计对象
 */
public class OkHttpEvent {
    public long dnsStartTime;
    public long dnsEndTime;
    public long responseBodySize;
    public boolean apiSuccess;
    public String errorReason;
}
