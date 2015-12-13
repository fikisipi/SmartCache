package dimitrovskif.smartcache;

import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;

import org.junit.Rule;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

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

        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<Response<String>> responseRef = new AtomicReference<>();
        demoService.getHome().enqueue(new Callback<String>() {
            @Override
            public void onResponse(Response<String> response, Retrofit retrofit) {
                responseRef.set(response);
                latch.countDown();
            }

            @Override
            public void onFailure(Throwable t) {
                fail("Failure executing the request.");
            }
        });
        assertTrue(latch.await(1, TimeUnit.SECONDS));
        assertEquals(responseRef.get().body(), "VERY_BASIC_BODY");

        final CountDownLatch latch2 = new CountDownLatch(2);
        final AtomicReference<Response<String>> response2Ref = new AtomicReference<>();
        demoService.getHome().enqueue(new Callback<String>() {
            @Override
            public void onResponse(Response<String> response, Retrofit retrofit) {
                latch2.countDown();
                if(latch2.getCount() == 1){ // the cache hit one.
                    response2Ref.set(response);
                }else{ // the network one.
                    assertEquals(response.body(), response2Ref.get().body());
                }
            }

            @Override
            public void onFailure(Throwable t) {
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
