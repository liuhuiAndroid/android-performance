package com.optimize.performance.tasks.delayinittask;

import com.optimize.performance.launchstarter.task.MainTask;
import com.optimize.performance.utils.LogUtils;

public class DelayInitTaskB extends MainTask {

    @Override
    public void run() {
        // 模拟一些操作

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LogUtils.i("DelayInitTaskB finished");
    }
}
