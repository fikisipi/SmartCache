package dimitrovskif.smartcache;

import com.squareup.okhttp.Request;

import java.util.HashMap;

import retrofit.Response;

public class MockCachingSystem implements CachingSystem {
    HashMap<String, byte[]> cachedResponses = new HashMap<>();

    @Override
    public <T> void addInCache(Response<T> response, byte[] rawResponse) {
        cachedResponses.put(response.raw().request().urlString(), rawResponse);
    }

    @Override
    public <T> byte[] getFromCache(Request request) {
        return cachedResponses.get(request.urlString());
    }
}
