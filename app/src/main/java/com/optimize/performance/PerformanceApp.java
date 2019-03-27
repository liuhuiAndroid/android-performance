package com.optimize.performance;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcel;
import android.os.StrictMode;
import android.support.multidex.MultiDex;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.ImageView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.stetho.Stetho;
import com.github.anrwatchdog.ANRWatchDog;
import com.github.moduth.blockcanary.BlockCanary;
import com.optimize.performance.adapter.NewsAdapter;
import com.optimize.performance.bean.NewsItem;
import com.optimize.performance.block.AppBlockCanaryContext;
import com.optimize.performance.launchstarter.TaskDispatcher;
import com.optimize.performance.memory.ImageHook;
import com.optimize.performance.tasks.GetDeviceIdTask;
import com.optimize.performance.tasks.InitAMapTask;
import com.optimize.performance.tasks.InitBuglyTask;
import com.optimize.performance.tasks.InitFrescoTask;
import com.optimize.performance.tasks.InitJPushTask;
import com.optimize.performance.tasks.InitStethoTask;
import com.optimize.performance.tasks.InitUmengTask;
import com.optimize.performance.tasks.InitWeexTask;
import com.optimize.performance.utils.LaunchTimer;
import com.optimize.performance.utils.LogUtils;
import com.taobao.android.dexposed.DexposedBridge;
import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.weex.InitConfig;
import com.taobao.weex.WXSDKEngine;
import com.tencent.bugly.crashreport.CrashReport;
import com.tencent.mmkv.MMKV;
import com.umeng.commonsdk.UMConfigure;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.jpush.android.api.JPushInterface;

public class PerformanceApp extends Application {

    private AMapLocationClient mLocationClient = null;
    private AMapLocationClientOption mLocationOption = null;

    public void setDeviceId(String deviceId) {
        this.mDeviceId = deviceId;
    }

    public String getDeviceId() {
        return mDeviceId;
    }

    private String mDeviceId;
    private static Application mApplication;
    private boolean DEV_MODE = true;

    private CountDownLatch mCountDownLatch = new CountDownLatch(1);

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = Math.max(2, Math.min(CPU_COUNT - 1, 4));

    private AMapLocationListener mLocationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {
            // 一些处理
        }
    };

    private int mCrashTimes;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        if(mCrashTimes > 3){
            // 删除文件，恢复到重新安装的状态
        }

        if(mCrashTimes > 5){
            // 清除热修信息
        }

        LaunchTimer.startRecord();
        MultiDex.install(this);
        DexposedBridge.hookAllConstructors(Thread.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                Thread thread = (Thread) param.thisObject;
                LogUtils.i(thread.getName()+" stack "+Log.getStackTraceString(new Throwable()));
            }
        });
    }

    @Override
    public void onCreate() {
        super.onCreate();

        MMKV.initialize(PerformanceApp.this);
        MMKV.defaultMMKV().encode("times",100);

        int times = MMKV.defaultMMKV().decodeInt("times");


        LaunchTimer.startRecord();
        mApplication = this;

        TaskDispatcher.init(PerformanceApp.this);

        TaskDispatcher dispatcher = TaskDispatcher.createInstance();

        dispatcher.addTask(new InitAMapTask())
                .addTask(new InitStethoTask())
                .addTask(new InitWeexTask())
                .addTask(new InitBuglyTask())
                .addTask(new InitFrescoTask())
                .addTask(new InitJPushTask())
                .addTask(new InitUmengTask())
                .addTask(new GetDeviceIdTask())
                .start();

        dispatcher.await();

        LaunchTimer.endRecord();

        DexposedBridge.hookAllConstructors(ImageView.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                DexposedBridge.findAndHookMethod(ImageView.class, "setImageBitmap", Bitmap.class, new ImageHook());
            }
        });


//        try {
//            DexposedBridge.findAndHookMethod(Class.forName("android.os.BinderProxy"), "transact",
//                    int.class, Parcel.class, Parcel.class, int.class, new XC_MethodHook() {
//                        @Override
//                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                            LogUtils.i( "BinderProxy beforeHookedMethod " + param.thisObject.getClass().getSimpleName()
//                                    + "\n" + Log.getStackTraceString(new Throwable()));
//                            super.beforeHookedMethod(param);
//                        }
//                    });
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        }

//        BlockCanary.install(this, new AppBlockCanaryContext()).start();

        initStrictMode();

//        new ANRWatchDog().start();
    }

    private void initStrictMode() {
        if (DEV_MODE) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectCustomSlowCalls() //API等级11，使用StrictMode.noteSlowCode
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()// or .detectAll() for all detectable problems
                    .penaltyLog() //在Logcat 中打印违规异常信息
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .setClassInstanceLimit(NewsItem.class, 1)
                    .detectLeakedClosableObjects() //API等级11
                    .penaltyLog()
                    .build());
        }
    }


    private void initStetho() {
        Handler handler = new Handler(Looper.getMainLooper());
        Stetho.initializeWithDefaults(this);
    }

    private void initWeex() {
        InitConfig config = new InitConfig.Builder().build();
        WXSDKEngine.initialize(this, config);
    }

    private void initJPush() {
        JPushInterface.init(this);
        JPushInterface.setAlias(this, 0, mDeviceId);
    }

    private void initFresco() {
        Fresco.initialize(this);
    }

    private void initAMap() {
        mLocationClient = new AMapLocationClient(getApplicationContext());
        mLocationClient.setLocationListener(mLocationListener);
        mLocationOption = new AMapLocationClientOption();
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        mLocationOption.setOnceLocation(true);
        mLocationClient.setLocationOption(mLocationOption);
        mLocationClient.startLocation();
    }

    private void initUmeng() {
        UMConfigure.init(this, "58edcfeb310c93091c000be2", "umeng",
                UMConfigure.DEVICE_TYPE_PHONE, "1fe6a20054bcef865eeb0991ee84525b");
    }

    private void initBugly() {
        CrashReport.initCrashReport(getApplicationContext(), "注册时申请的APPID", false);
    }

    public String getStringFromAssets(String fileName) {
        String Result = "";
        InputStreamReader inputReader = null;
        BufferedReader bufReader = null;
        try {
            inputReader = new InputStreamReader(getResources().getAssets().open(fileName));
            bufReader = new BufferedReader(inputReader);
            String line = "";
            while ((line = bufReader.readLine()) != null) {
                Result += line;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inputReader != null) {
                try {
                    inputReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (bufReader != null) {
                try {
                    bufReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return Result;
    }

    public static Application getApplication() {
        return mApplication;
    }

}
