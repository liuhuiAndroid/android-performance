package com.optimize.performance;

import android.annotation.TargetApi;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.AsyncLayoutInflater;
import android.support.v4.view.LayoutInflaterCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Choreographer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;

import com.alibaba.fastjson.JSON;
import com.optimize.performance.adapter.NewsAdapter;
import com.optimize.performance.adapter.OnFeedShowCallBack;
import com.optimize.performance.async.ThreadPoolUtils;
import com.optimize.performance.bean.NewsItem;
import com.optimize.performance.launchstarter.DelayInitDispatcher;
import com.optimize.performance.net.JobSchedulerService;
import com.optimize.performance.net.RetrofitNewsUtils;
import com.optimize.performance.tasks.delayinittask.DelayInitTaskA;
import com.optimize.performance.tasks.delayinittask.DelayInitTaskB;
import com.optimize.performance.utils.ExceptionMonitor;
import com.optimize.performance.utils.LaunchTimer;
import com.optimize.performance.utils.LogUtils;
import com.zhangyue.we.x2c.X2C;
import com.zhangyue.we.x2c.ano.Xml;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Xml(layouts = "activity_main")
public class MainActivity extends AppCompatActivity implements OnFeedShowCallBack {

    private AlphaAnimation alphaAnimation;
    private RecyclerView mRecyclerView;
    private NewsAdapter mNewsAdapter;
    private String mStringIds = "20190220005233,20190220005171,20190220005160,20190220005146,20190220001228,20190220001227,20190219006994,20190219006839,20190219005350,20190219005343,20190219004522,20190219004520,20190219000132,20190219000118,20190219000119,20190218009367,20190218009078,20190218009075,20190218008572,20190218008496,20190218006078,20190218006156,20190218006190,20190218006572,20190218006235,20190218006284,20190218006571,20190218006283,20190218006191,20190218005733,20190217004740,20190218001891,20190218001889,20190217004183,20190217004019,20190217004011,20190217003152,20190217002757,20190217002249,20190217000954,20190217000957,20190217000953,20190216004269,20190216003721,20190216003720,20190216003351,20190216003364,20190216002989,20190216002613,20190216000044,20190216000043,20190216000042,20190215007933,20190215008945,20190215007932,20190215007090,20190215005473,20190215005469,20190215005313,20190215004868,20190215004299,20190215001233,20190215001229,20190215001226,20190214008652,20190214008429,20190214009262,20190214008347,20190214008345,20190214007362,20190214006949,20190214006948,20190214006588,20190214006270,20190214006102,20190214005769,20190214005583,20190214005581,20190214005484,20190214005466,20190214005303,20190214004660,20190213009703,20190213009285,20190214002912,20190213007775,20190213007461,20190213007049,20190213007047,20190213006228,20190213006050,20190213005767,20190213005738,20190213005641,20190213005512,20190213004174,20190212007918,20190212007914,20190212007913,20190212007696,20190212007369,20190212007361,20190212006921,20190212006007,20190212005954,20190212005925,20190212005924,20190212005923,20190212005922,20190212005428,20190212005427,20190212005426,20190212005226,20190212004916,20190212004422,20190212004355,20190212004351,20190212000989,20190212000994,20190212000991,20190211005672,20190211004121,20190211004049,20190211003973,20190211003434,20190211003199,20190211005392,20190211003179,20190211000956,20190211000955,20190211003203,20190211003206,20190210004201,20190210003934,20190210004067,20190210003683,20190210003685,20190210003684,20190210003682,20190210003281,20190210002944,20190210002936,20190210003308,20190210002745,20190210002634,20190210002893,20190210002315,20190210001977,20190210002046,20190210001663,20190209004408,20190209003643,20190209003582,20190209003401,20190209003193,20190209002777,20190209002664,20190209002724,20190209002723,20190209002119,20190208001691,20190208004370,20190208000203,20190208004129,20190208003560,20190208002739,20190208002661,20190208000144,20190208000194,20190208002671,20190208003081,20190208002398,20190208000184,20190208001943,20190208000074,20190208000051,20190208000121,20190207003938,20190207003939,20190208002394,20190207003698,20190207001759,20190207003882,20190207003424,20190207002872,20190207003101,20190207002873,20190207002772,20190207002036,20190207001888,20190207000695,20190206004239,20190206004172,20190206002264,20190206002238,20190206002237,20190206004192,20190206004176,20190206003738,20190206003028";

    private long mStartFrameTime = 0;
    private int mFrameCount = 0;
    private static final long MONITOR_INTERVAL = 160L; //单次计算FPS使用160毫秒
    private static final long MONITOR_INTERVAL_NANOS = MONITOR_INTERVAL * 1000L * 1000L;
    private static final long MAX_INTERVAL = 1000L; //设置计算fps的单位时间间隔1000ms,即fps/s;


    public List<NewsItem> mItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // 以下代码是为了演示修改任务的名称
        ThreadPoolUtils.getService().execute(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);
                String oldName = Thread.currentThread().getName();
                Thread.currentThread().setName("new Name");
                LogUtils.i("");
                Thread.currentThread().setName(oldName);
            }
        });

        // 以下代码是为了演示Msg导致的主线程卡顿
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                LogUtils.i("Msg 执行");
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
            }
        });

        LayoutInflaterCompat.setFactory2(getLayoutInflater(), new LayoutInflater.Factory2() {
            @Override
            public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {

                if (TextUtils.equals(name, "TextView")) {
                    // 生成自定义TextView
                }
                long time = System.currentTimeMillis();
                View view = getDelegate().createView(parent, name, context, attrs);
                LogUtils.i(name + " cost " + (System.currentTimeMillis() - time));
                return view;
            }

            @Override
            public View onCreateView(String name, Context context, AttributeSet attrs) {
                return null;
            }
        });

        new AsyncLayoutInflater(MainActivity.this).inflate(R.layout.activity_main, null, new AsyncLayoutInflater.OnInflateFinishedListener() {
            @Override
            public void onInflateFinished(@NonNull View view, int i, @Nullable ViewGroup viewGroup) {
                setContentView(view);
                mRecyclerView = findViewById(R.id.recycler_view);
                mRecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                mRecyclerView.setAdapter(mNewsAdapter);
                mNewsAdapter.setOnFeedShowCallBack(MainActivity.this);
            }
        });

        setTheme(R.style.AppTheme);

        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        X2C.setContentView(MainActivity.this, R.layout.activity_main);
        mNewsAdapter = new NewsAdapter(mItems);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        Intent intent = registerReceiver(null, filter);
        LogUtils.i("battery " + intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1));

        getNews();
        getFPS();


        // 以下代码是为了演示业务不正常场景下的监控
        try {
            // 一些业务处理
            Log.i("", "");
        } catch (Exception e) {
            ExceptionMonitor.monitor(Log.getStackTraceString(e));
        }

        boolean flag = true;
        if (flag) {
            // 正常，继续执行流程
        } else {
            ExceptionMonitor.monitor("");
        }
    }

    /**
     * 演示JobScheduler的使用
     */
    private void startJobScheduler() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
            JobInfo.Builder builder = new JobInfo.Builder(1, new ComponentName(getPackageName(), JobSchedulerService.class.getName()));
            builder.setRequiresCharging(true)
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED);
            jobScheduler.schedule(builder.build());
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void getFPS() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            return;
        }
        Choreographer.getInstance().postFrameCallback(new Choreographer.FrameCallback() {
            @Override
            public void doFrame(long frameTimeNanos) {
                if (mStartFrameTime == 0) {
                    mStartFrameTime = frameTimeNanos;
                }
                long interval = frameTimeNanos - mStartFrameTime;
                if (interval > MONITOR_INTERVAL_NANOS) {
                    double fps = (((double) (mFrameCount * 1000L * 1000L)) / interval) * MAX_INTERVAL;
                    mFrameCount = 0;
                    mStartFrameTime = 0;
                } else {
                    ++mFrameCount;
                }

                Choreographer.getInstance().postFrameCallback(this);
            }
        });
    }

    private void getNews() {
        RetrofitNewsUtils.getApiService().getNBANews("banner", mStringIds)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        try {
                            String json = response.body().string();
                            JSONObject jsonObject = new JSONObject(json);
                            JSONObject data = jsonObject.getJSONObject("data");
                            Iterator<String> keys = data.keys();
                            while (keys.hasNext()) {
                                String next = keys.next();
                                JSONObject itemJO = data.getJSONObject(next);
                                NewsItem newsItem = JSON.parseObject(itemJO.toString(), NewsItem.class);
                                mItems.add(newsItem);
                            }
                            mNewsAdapter.setItems(mItems);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 以下代码是为了演示电量优化中对动画的处理
//      alphaAnimation.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 以下代码是为了演示电量优化中对动画的处理
//        alphaAnimation.cancel();
    }

    @Override
    public void onFeedShow() {
        // 以下两行是原有方式
//        new DispatchRunnable(new DelayInitTaskA()).run();
//        new DispatchRunnable(new DelayInitTaskB()).run();

        DelayInitDispatcher delayInitDispatcher = new DelayInitDispatcher();
        delayInitDispatcher.addTask(new DelayInitTaskA())
                .addTask(new DelayInitTaskB())
                .start();

        // 一系列操作 10s
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        LaunchTimer.endRecord("onWindowFocusChanged");
    }
}
