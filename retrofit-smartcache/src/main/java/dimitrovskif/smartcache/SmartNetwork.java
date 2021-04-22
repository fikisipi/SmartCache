package dimitrovskif.smartcache;

import retrofit2.Response;

public final class SmartNetwork{
    public static boolean isResponseFromNetwork(Response<?> r) {
        return !r.headers().names().contains("Is-Retrofit-SmartCached");
    }
}
