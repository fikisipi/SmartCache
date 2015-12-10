package dimitrovskif.smartcache;

import java.util.concurrent.Executor;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

class ExecutorCallback<T> implements Callback<T> {
    private final Executor callbackExecutor;
    private final Callback<T> callback;

    public ExecutorCallback(Executor callbackExecutor, Callback<T> callback){
        this.callbackExecutor = callbackExecutor;
        this.callback = callback;
    }

    @Override
    public void onResponse(final Response<T> response, final Retrofit retrofit) {
        callbackExecutor.execute(new Runnable() {
            @Override public void run() {
                callback.onResponse(response, retrofit);
            }
        });
    }

    @Override public void onFailure(final Throwable t) {
        callbackExecutor.execute(new Runnable() {
            @Override public void run() {
                callback.onFailure(t);
            }
        });
    }
}