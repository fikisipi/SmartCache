package dimitrovskif.smartcache;

import org.junit.Rule;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.GET;

import static org.junit.Assert.*;

public class CallbackTest {
    /***
     * Builds a Retrofit SmartCache factory without Android executor
     */
    private CallAdapter.Factory buildSmartCacheFactory(){
        SmartCallFactory factory = new SmartCallFactory(new MockCachingSystem(),
                new MainThreadExecutor());
        return factory;
    }

    @Rule public final MockWebServer server = new MockWebServer();
    @Test
    public void dispatch_isGood() throws Exception{
        /* Set up the mock webserver */
        MockResponse resp = new MockResponse();
        resp.setBody("VERY_BASIC_BODY");
        server.enqueue(resp);
        server.enqueue(resp.clone());

        Retrofit r = new Retrofit.Builder()
                .baseUrl(server.url("/"))
                .addConverterFactory(new ToStringConverterFactory())
                .addCallAdapterFactory(buildSmartCacheFactory())
                .build();
        DemoService demoService = r.create(DemoService.class);


        final Logger log = Logger.getLogger("test_logger");
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<Response<String>> responseRef = new AtomicReference<>();
        demoService.getHome().enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                log.info("Got a response. Should be a network hit (no cache the first time).");
                responseRef.set(response);
                latch.countDown();
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                fail("Failure executing the request.");
            }
        });
        assertTrue(latch.await(1, TimeUnit.SECONDS));
        assertEquals(responseRef.get().body(), "VERY_BASIC_BODY");
        final CountDownLatch latch2 = new CountDownLatch(2);
        final AtomicReference<Response<String>> response2Ref = new AtomicReference<>();
        demoService.getHome().enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                latch2.countDown();
                if(latch2.getCount() == 1){ // the cache hit one.
                    log.info("Got a response. Should be a cache hit.");
                    response2Ref.set(response);
                }else{ // the network one.
                    log.info("Got a response. Should be a real network hit.");
                    assertEquals(response.body(), response2Ref.get().body());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                fail("Failure executing the request.");
            }
        });
        assertTrue(latch2.await(1, TimeUnit.SECONDS));
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
