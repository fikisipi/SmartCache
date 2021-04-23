package dimitrovskif.smartcache;

import android.content.Context;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.Executor;

import okhttp3.Headers;
import okhttp3.Request;
import okio.Timeout;
import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class SmartCallFactory extends CallAdapter.Factory {
    private final CachingSystem cachingSystem;
    private final Executor asyncExecutor;

    public SmartCallFactory(CachingSystem cachingSystem){
        this.cachingSystem = cachingSystem;
        this.asyncExecutor = new AndroidExecutor();
    }

    public SmartCallFactory(CachingSystem cachingSystem, Executor executor){
        this.cachingSystem = cachingSystem;
        this.asyncExecutor = executor;
    }

    public static SmartCallFactory createBasic(Context ctx) {
        return new SmartCallFactory(BasicCaching.buildFromContext(ctx));
    }

    @Override
    public CallAdapter<?, ?> get(final Type returnType, final Annotation[] annotations,
                                         final Retrofit retrofit) {
        Class<?> cls = getRawType(returnType);
        if (cls != SmartCall.class) {
            return null;
        }

        if (!(returnType instanceof ParameterizedType)) {
            throw new IllegalStateException(
                    "SmartCall must have generic type (e.g., SmartCall<ResponseBody>)");
        }

        final Type responseType = ((ParameterizedType) returnType).getActualTypeArguments()[0];
        final Executor callbackExecutor = asyncExecutor;

        return new CallAdapter<Object, SmartCall<?>>() {
            @Override
            public Type responseType() {
                return responseType;
            }

            @Override
            public SmartCall<?> adapt(Call<Object> call) {
                return new SmartCallImpl<>(callbackExecutor, call, responseType(), annotations,
                        retrofit, cachingSystem);
            }
        };
    }

    static class SmartCallImpl<T> implements SmartCall<T>, Call<T>{
        private final Executor callbackExecutor;
        private final Call<T> baseCall;
        private final Type responseType;
        private final Annotation[] annotations;
        private final Retrofit retrofit;
        private final CachingSystem cachingSystem;
        private final Request request;

        @Override
        public Timeout timeout() {
            return baseCall.timeout();
        }

        @Override
        public boolean isExecuted() {
            return baseCall.isExecuted();
        }

        @Override
        public boolean isCanceled() {
            return baseCall.isCanceled();
        }

        @Override
        public Request request() {
            return request;
        }

        public SmartCallImpl(Executor callbackExecutor, Call<T> baseCall, Type responseType,
                             Annotation[] annotations, Retrofit retrofit, CachingSystem cachingSystem){
            this.callbackExecutor = callbackExecutor;
            this.baseCall = baseCall;
            this.responseType = responseType;
            this.annotations = annotations;
            this.retrofit = retrofit;
            this.cachingSystem = cachingSystem;

            this.request = baseCall.request();
        }

        @Override
        public void enqueue(final Callback<T> callback) {
            Runnable enqueueRunnable = new Runnable() {
                @Override
                public void run() {
                    /* Read cache */
                    byte[] data = cachingSystem.getFromCache(request);
                    if(data != null) {
                        final T convertedData = SmartUtils.bytesToResponse(retrofit, responseType, annotations,
                                data);
                        Runnable cacheCallbackRunnable = new Runnable() {
                            @Override
                            public void run() {
                                Headers headers = Headers.of("Is-Retrofit-SmartCached", "true");
                                Response<T> resp = Response.success(convertedData, headers);
                                callback.onResponse(baseCall, resp);
                            }
                        };
                        callbackExecutor.execute(cacheCallbackRunnable);
                    }

                    /* Enqueue actual network call */
                    baseCall.enqueue(new Callback<T>() {
                        @Override
                        public void onResponse(final Call<T> call, final Response<T> response) {
                            // Make a main thread runnable
                            Runnable responseRunnable = new Runnable() {
                                @Override
                                public void run() {
                                    if (response.isSuccessful()) {
                                        byte[] rawData = SmartUtils.responseToBytes(retrofit, response.body(),
                                                responseType, annotations);
                                        cachingSystem.addInCache(response, rawData);
                                    }
                                    callback.onResponse(call, response);
                                }
                            };
                            // Run it on the proper thread
                            callbackExecutor.execute(responseRunnable);
                        }

                        @Override
                        public void onFailure(final Call<T> call, final Throwable t) {
                            Runnable failureRunnable = new Runnable() {
                                @Override
                                public void run() {
                                    callback.onFailure(call, t);
                                }
                            };
                            callbackExecutor.execute(failureRunnable);
                        }
                    });

                }
            };
            Thread enqueueThread = new Thread(enqueueRunnable);
            enqueueThread.start();
        }

        @Override
        public Call<T> clone() {
            return new SmartCallImpl<>(callbackExecutor, baseCall.clone(), this.responseType,
                    annotations, retrofit, cachingSystem);
        }

        @Override
        public Response<T> execute() throws IOException {
            return baseCall.execute();
        }

        @Override
        public void cancel() {
            baseCall.cancel();
        }
    }
}