package dimitrovskif.smartcache;

import com.squareup.okhttp.mockwebserver.MockWebServer;

import org.junit.Rule;
import org.junit.Test;

import java.util.concurrent.Executor;

import retrofit.CallAdapter;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import retrofit.http.GET;

import static org.junit.Assert.*;

public class CallbackTest {
    /***
     * Builds a Retrofit SmartCache factory without Android executor
     */
    private CallAdapter.Factory buildSmartCacheFactory(){
        SmartCallFactory factory = new SmartCallFactory(new MockCachingSystem());
        factory.setCustomExecutor(new MainThreadExecutor());
        return factory;
    }

    @Rule public final MockWebServer server = new MockWebServer();
    @Test
    public void dispatch_isGood() throws Exception{
        Retrofit r = new Retrofit.Builder()
                .baseUrl(server.url("/"))
                .addConverterFactory(new ToStringConverterFactory())
                .addCallAdapterFactory(buildSmartCacheFactory())
                .build();
        DemoService demoService = r.create(DemoService.class);
        demoService.getHome().enqueue(new Callback<String>() {
            @Override
            public void onResponse(Response<String> response, Retrofit retrofit) {
                assertEquals(response.body(), "woo-hoo!");
            }

            @Override
            public void onFailure(Throwable t) {
                fail("Failure executing the request.");
            }
        });
    }

    static class MainThreadExecutor implements Executor{
        @Override
        public void execute(Runnable command) {
            command.run();
        }
    }

    interface DemoService{
        @GET("/")
        SmartCall<String> getHome();
    }
}
