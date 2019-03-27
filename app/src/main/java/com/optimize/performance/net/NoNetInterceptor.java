package com.optimize.performance.net;

import com.optimize.performance.PerformanceApp;
import com.optimize.performance.utils.Utils;

import java.io.IOException;

import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class NoNetInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Request.Builder builder = request.newBuilder();
        if(!Utils.isNetworkConnected(PerformanceApp.getApplication())){
            builder.cacheControl(CacheControl.FORCE_CACHE);
        }
        return chain.proceed(builder.build());
    }
}
