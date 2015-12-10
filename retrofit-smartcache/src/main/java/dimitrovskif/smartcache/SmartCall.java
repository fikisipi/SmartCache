package dimitrovskif.smartcache;

import com.squareup.okhttp.Request;

import java.lang.reflect.Type;

import retrofit.Callback;

public interface SmartCall<T>{
    void enqueue(Callback<T> callback);
    Type responseType();
    Request buildRequest();
    SmartCall<T> clone();
}