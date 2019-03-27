package com.optimize.performance.tasks.delayinittask;

import com.optimize.performance.launchstarter.task.MainTask;
import com.optimize.performance.utils.LogUtils;

public class DelayInitTaskA extends MainTask {

    @Override
    public void run() {
        // 模拟一些操作
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LogUtils.i("DelayInitTaskA finished");
    }
}
