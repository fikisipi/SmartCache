package dimitrovskif.smartcache;

import java.util.HashMap;

import okhttp3.Request;
import retrofit2.Response;


public class MockCachingSystem implements CachingSystem {
    HashMap<String, byte[]> cachedResponses = new HashMap<>();

    @Override
    public <T> void addInCache(Response<T> response, byte[] rawResponse) {
        cachedResponses.put(response.raw().request().url().toString(), rawResponse);
    }

    @Override
    public <T> byte[] getFromCache(Request request) {
        return cachedResponses.get(request.url().toString());
    }
}
