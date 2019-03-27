package com.optimize.performance.memory;

import java.util.ArrayList;

public class CallBackManager {

    public static ArrayList<CallBack> sCallBacks = new ArrayList<>();

    public static void addCallBack(CallBack callBack) {
        sCallBacks.add(callBack);
    }

    public static void removeCallBack(CallBack callBack) {
        sCallBacks.remove(callBack);
    }

}
