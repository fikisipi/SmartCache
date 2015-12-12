package dimitrovskif.smartcache;

import android.util.Log;

import com.google.common.reflect.TypeToken;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.ResponseBody;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.Executor;

import okio.Buffer;
import okio.BufferedSink;
import okio.BufferedSource;
import retrofit.Call;
import retrofit.CallAdapter;
import retrofit.Callback;
import retrofit.Converter;
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
            final String cachedResponse = "[{\"email\":\"Sincere@april.biz\",\"name\":\"Leanne Graham\",\"id\":1}]";

            callbackExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    for(Converter.Factory factory : retrofit.converterFactories()){
                        if(factory == null) continue;
                        Converter<ResponseBody, T> converter =
                                (Converter<ResponseBody, T>)
                                factory.fromResponseBody(responseType, annotations);
                        if(converter != null){
                            try {
                                T data = converter.convert(ResponseBody.create(null, cachedResponse));
                                callback.onResponse(Response.success(data), retrofit);
                            }catch(IOException | NullPointerException exc){
                                Log.e("SmartCall", "", exc);
                            }
                        }
                    }
                }
            });

            baseCall.enqueue(new Callback<T>() {
                @Override
                public void onResponse(final Response<T> response, final Retrofit retrofit) {
                    Runnable responseRunnable = new Runnable() {
                        @Override
                        public void run() {
                            if(response.isSuccess()) {
                                byte[] bytes = SmartUtils.responseToBytes(retrofit, response, responseType(),
                                        annotations);
                                if(bytes != null)
                                Log.d("SmartCall", bytes.toString());
                                else
                                    Log.d("SmartCall", "null bytes!");
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
    }
}