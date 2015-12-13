package dimitrovskif.smartcache;

import android.os.Environment;
import android.util.Log;
import android.util.LruCache;

import com.google.common.reflect.TypeToken;
import com.jakewharton.disklrucache.DiskLruCache;
import com.squareup.okhttp.Request;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.concurrent.Executor;

import retrofit.Call;
import retrofit.CallAdapter;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public class SmartCallFactory implements CallAdapter.Factory {
    private final Executor executor;

    public SmartCallFactory(){
        this(null);
    }

    public SmartCallFactory(Executor executor){
        if(executor != null) {
            this.executor = executor;
        }else{
            this.executor = new MainThreadExecutor();
        }
    }

    @Override
    public CallAdapter<SmartCall<?>> get(final Type returnType, final Annotation[] annotations,
                                         final Retrofit retrofit) {

        TypeToken<?> token = TypeToken.of(returnType);
        if (token.getRawType() != SmartCall.class) {
            return null;
        }

        if (!(returnType instanceof ParameterizedType)) {
            throw new IllegalStateException(
                    "SmartCall must have generic type (e.g., SmartCall<ResponseBody>)");
        }

        final Type responseType = ((ParameterizedType) returnType).getActualTypeArguments()[0];
        final Executor callbackExecutor = executor;

        return new CallAdapter<SmartCall<?>>() {
            @Override public Type responseType() {
                return responseType;
            }

            @Override public <R> SmartCall<R> adapt(Call<R> call) {
                return new SmartCallImpl<>(callbackExecutor, call, responseType(), annotations,
                        retrofit);
            }
        };
    }

    static class SmartCallImpl<T> implements SmartCall<T>{
        private final Executor callbackExecutor;
        private final Call<T> baseCall;
        private final Type responseType;
        private final Annotation[] annotations;
        private final Retrofit retrofit;
        private final Request request;

        public SmartCallImpl(Executor callbackExecutor, Call<T> baseCall, Type responseType,
                             Annotation[] annotations, Retrofit retrofit){
            this.callbackExecutor = callbackExecutor;
            this.baseCall = baseCall;
            this.responseType = responseType;
            this.annotations = annotations;
            this.retrofit = retrofit;

            // This one is a hack but should create a valid Response (which can later be cloned)
            this.request = buildRequestFromCall();
        }

        /***
         * Inspects an OkHttp-powered Call<T> and builds a Request
         * * @return A valid Request (that contains query parameters, right method and endpoint)
         */
        private Request buildRequestFromCall(){
            try {
                Field argsField = baseCall.getClass().getDeclaredField("args");
                argsField.setAccessible(true);
                Object[] args = (Object[]) argsField.get(baseCall);

                Field requestFactoryField = baseCall.getClass().getDeclaredField("requestFactory");
                requestFactoryField.setAccessible(true);
                Object requestFactory = requestFactoryField.get(baseCall);
                Method createMethod = requestFactory.getClass().getDeclaredMethod("create", Object[].class);
                createMethod.setAccessible(true);
                return (Request) createMethod.invoke(requestFactory, new Object[]{args});
            }catch(Exception exc){
                return null;
            }
        }

        @Override
        public void enqueue(final Callback<T> callback) {
            callbackExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    callback.onResponse(getFromCache(callback), retrofit);
                }
            });

            // Enqueue an OkHttp call
            baseCall.enqueue(new Callback<T>() {
                @Override
                public void onResponse(final Response<T> response, final Retrofit retrofit) {
                    // Make a main thread runnable
                    Runnable responseRunnable = new Runnable() {
                        @Override
                        public void run() {
                            if(response.isSuccess()) {
                                addInCache(response);
                            }
                            callback.onResponse(response, retrofit);
                        }
                    };

                    // Run it on the proper thread
                    callbackExecutor.execute(responseRunnable);
                }

                @Override
                public void onFailure(final Throwable t) {
                    Runnable failureRunnable = new Runnable() {
                        @Override
                        public void run() {
                            callback.onFailure(t);
                        }
                    };

                    callbackExecutor.execute(failureRunnable);
                }
            });
        }

        @Override
        public Type responseType() {
            return responseType;
        }

        @Override
        public Request buildRequest() {
            return request.newBuilder().build();
        }

        @Override
        public SmartCall<T> clone() {
            return new SmartCallImpl<>(callbackExecutor, baseCall.clone(), responseType(),
                    annotations, retrofit);
        }

        private <T> Response<T> getFromCache(Callback<T> cb){
            final String cachedResponse = "[{\"email\":\"Sincere@april.biz\",\"name\":\"Leanne Graham\",\"id\":1}]";
            T data = SmartUtils.bytesToResponse(retrofit, responseType(), annotations,
                    cachedResponse.getBytes());
            return Response.success(data);
        }

        private <T> void addInCache(Response<T> response){
            byte[] bytes = SmartUtils.responseToBytes(retrofit, response.body(), responseType(),
                    annotations);
            if(bytes != null) {
                // we can cache this thing.
                DiskLruCache cache;
                try{
                    cache = DiskLruCache.open(
                            new File(Environment.getExternalStorageDirectory(), "smartcache.bin"),
                            1,
                            1,
                            1024 * 1024 * 10
                    );
                }catch(IOException exc){
                    cache = null;
                }

                if(cache != null){
                    try {
                        cache.edit("x038k1").set(0, new String(bytes, Charset.defaultCharset()));
                    }catch(IOException exc){

                    }
                }
            }else {
                // fuck, we can't cache this.
                Log.d("SmartCall", "null bytes!");
            }
        }
    }
}