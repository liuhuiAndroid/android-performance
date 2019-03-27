package com.optimize.performance.tasks;

import android.os.Handler;
import android.os.Looper;

import com.facebook.stetho.Stetho;
import com.optimize.performance.launchstarter.task.Task;

/**
 * 异步的Task
 */
public class InitStethoTask extends Task {

    @Override
    public void run() {

        Handler handler = new Handler(Looper.getMainLooper());
        Stetho.initializeWithDefaults(mContext);
    }
}
