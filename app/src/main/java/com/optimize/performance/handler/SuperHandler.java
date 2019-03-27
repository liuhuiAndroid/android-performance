package com.optimize.performance.handler;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.optimize.performance.utils.LogUtils;

import org.json.JSONObject;

public class SuperHandler extends Handler {

    private long mStartTime = System.currentTimeMillis();

    public SuperHandler() {
        super(Looper.myLooper(), null);
    }

    public SuperHandler(Callback callback) {
        super(Looper.myLooper(), callback);
    }

    public SuperHandler(Looper looper, Callback callback) {
        super(looper, callback);
    }

    public SuperHandler(Looper looper) {
        super(looper);
    }

    @Override
    public boolean sendMessageAtTime(Message msg, long uptimeMillis) {
        boolean send = super.sendMessageAtTime(msg, uptimeMillis);
        if (send) {
            GetDetailHandlerHelper.getMsgDetail().put(msg, Log.getStackTraceString(new Throwable()).replace("java.lang.Throwable", ""));
        }
        return send;
    }

    @Override
    public void dispatchMessage(Message msg) {
        mStartTime = System.currentTimeMillis();
        super.dispatchMessage(msg);

        if (GetDetailHandlerHelper.getMsgDetail().containsKey(msg)
                && Looper.myLooper() == Looper.getMainLooper()) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("Msg_Cost", System.currentTimeMillis() - mStartTime);
                jsonObject.put("MsgTrace", msg.getTarget() + " " + GetDetailHandlerHelper.getMsgDetail().get(msg));

                LogUtils.i("MsgDetail " + jsonObject.toString());
                GetDetailHandlerHelper.getMsgDetail().remove(msg);
            } catch (Exception e) {
            }
        }
    }

}
