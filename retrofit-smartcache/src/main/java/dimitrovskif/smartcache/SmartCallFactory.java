package dimitrovskif.smartcache;

import android.util.Log;

import com.google.common.reflect.TypeToken;
import com.squareup.okhttp.Request;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.Executor;

import retrofit.Call;
import retrofit.CallAdapter;
import retrofit.Callback;
import retrofit.Retrofit;

public class SmartCallFactory implements CallAdapter.Factory {
    @Override
    public CallAdapter<SmartCall<?>> get(Type returnType, Annotation[] annotations, Retrofit retrofit) {
        TypeToken<?> token = TypeToken.of(returnType);
        if (token.getRawType() != SmartCall.class) {
            return null;
        }

        if (!(returnType instanceof ParameterizedType)) {
            throw new IllegalStateException(
                    "SmartCall must have generic type (e.g., SmartCall<ResponseBody>)");
        }

        final Type responseType = ((ParameterizedType) returnType).getActualTypeArguments()[0];
        final Executor callbackExecutor = new MainThreadExecutor();
        return new CallAdapter<SmartCall<?>>() {
            @Override public Type responseType() {
                return responseType;
            }

            @Override public <R> SmartCall<R> adapt(Call<R> call) {
                return new SmartCallImpl<>(callbackExecutor, call, responseType());
            }
        };
    }

    static class SmartCallImpl<T> implements SmartCall<T>{
        private final Executor callbackExecutor;
        private final Call<T> baseCall;
        private final Type responseType;
        private final Request request;

        public SmartCallImpl(Executor callbackExecutor, Call<T> baseCall, Type responseType){
            Request req;
            try {
                Field argsField = baseCall.getClass().getDeclaredField("args");
                argsField.setAccessible(true);
                Object[] args = (Object[]) argsField.get(baseCall);

                Field requestFactoryField = baseCall.getClass().getDeclaredField("requestFactory");
                requestFactoryField.setAccessible(true);
                Object requestFactory = requestFactoryField.get(baseCall);
                Method createMethod = requestFactory.getClass().getDeclaredMethod("create", Object[].class);
                createMethod.setAccessible(true);
                req = (Request) createMethod.invoke(requestFactory, new Object[]{args});
            }catch(Exception exc){
                req = null;
                Log.e("DroidCallFactory", "Failed to create a Response", exc);
            }

            if(callbackExecutor == null){
                throw new RuntimeException("Callback executor == null!");
            }

            this.callbackExecutor = callbackExecutor;
            this.baseCall = baseCall;
            this.responseType = responseType;
            this.request = req;
        }

        @Override
        public void enqueue(final Callback<T> callback) {
            callbackExecutor.execute(new Runnable() {
                @Override
                public void run() {

                }
            });
            baseCall.enqueue(new ExecutorCallback<T>(callbackExecutor, callback));
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
            return new SmartCallImpl<>(callbackExecutor, baseCall.clone(), responseType());
        }
    }
}