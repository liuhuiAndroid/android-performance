package com.optimize.performance.net;

import com.optimize.performance.PerformanceApp;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.fastjson.FastJsonConverterFactory;

public class RetrofitNewsUtils {
    private static final APIService API_SERVICE;

    public static APIService getApiService() {
        return API_SERVICE;
    }

    public static final String HTTP_SPORTSNBA_QQ_COM = "http://sportsnba.qq.com/";

    static {
        OkHttpClient.Builder client = new OkHttpClient.Builder();
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        Cache cache = new Cache(PerformanceApp.getApplication().getCacheDir(),10*1024*1024);
        client.
                eventListenerFactory(OkHttpEventListener.FACTORY).
                dns(OkHttpDNS.getIns(PerformanceApp.getApplication())).
                addInterceptor(new NoNetInterceptor()).
                addInterceptor(logging);

        final Retrofit RETROFIT = new Retrofit.Builder()
                .baseUrl(HTTP_SPORTSNBA_QQ_COM)
                .addConverterFactory(FastJsonConverterFactory.create())
                .client(client.build())
                .build();
        API_SERVICE = RETROFIT.create(APIService.class);
    }


}
