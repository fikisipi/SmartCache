package dimitrovskif.smartcache;

import com.squareup.okhttp.Request;

import retrofit.Response;

public interface CachingSystem {
    <T> void addInCache(Response<T> response, byte[] rawResponse);
    <T> byte[] getFromCache(Request request);
}
