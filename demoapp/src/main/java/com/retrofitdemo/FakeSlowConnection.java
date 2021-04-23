package com.retrofitdemo;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;

public class FakeSlowConnection {
    public static OkHttpClient create() {
        return new OkHttpClient.Builder().addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                try {
                    Thread.sleep(1000);
                } catch(InterruptedException exc){
                  return null;
                }
                return chain.proceed(chain.request());
            }
        }).build();
    }
}
