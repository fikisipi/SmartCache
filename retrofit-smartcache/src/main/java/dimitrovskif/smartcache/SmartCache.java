package dimitrovskif.smartcache;

import retrofit2.Response;

public final class SmartCache {
    public static boolean isResponseFromNetwork(Response<?> r) {
        return !r.headers().names().contains("Is-Retrofit-SmartCached");
    }
}
