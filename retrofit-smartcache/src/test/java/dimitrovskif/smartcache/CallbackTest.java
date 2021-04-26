package dimitrovskif.smartcache;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Semaphore;
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
import retrofit2.http.POST;

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

    private DemoService demoService = null;
    private Logger log = null;

    @Before
    public void setUp() {
        Retrofit r = new Retrofit.Builder()
                .baseUrl(server.url("/"))
                .addConverterFactory(new ToStringConverterFactory())
                .addCallAdapterFactory(buildSmartCacheFactory())
                .build();
        this.demoService = r.create(DemoService.class);
        log = Logger.getLogger("test_logger");
    }

    @After
    public void tearDown() {
        this.demoService = null;
    }

    @Rule public final MockWebServer server = new MockWebServer();

    @Test
    public void simpleNetworkTest() throws Exception {
        /* Set up the mock webserver */
        MockResponse resp = new MockResponse();
        resp.setBody("VERY_BASIC_BODY");
        server.enqueue(resp);
        server.enqueue(resp.clone());


        final Logger log = Logger.getLogger("test_logger");
        final CountDownLatch latch = new CountDownLatch(2);
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
        latch.await(1, TimeUnit.SECONDS);
        assertEquals(latch.getCount(), 1);
        assertEquals(responseRef.get().body(), "VERY_BASIC_BODY");
        assertTrue("No network flag on response.", SmartCache.isResponseFromNetwork(responseRef.get()));
    }

    @Test
    public void twoTestCheck() throws Exception{
        /* Set up the mock webserver */
        MockResponse resp = new MockResponse();
        resp.setBody("VERY_BASIC_BODY");
        server.enqueue(resp);
        server.enqueue(resp.clone());

        final CountDownLatch latch = new CountDownLatch(2);
        final AtomicReference<Response<String>> responseRef = new AtomicReference<>();
        demoService.getHome().enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                log.info("Got a response in pass #1 in twoTestCheck() call.");
                responseRef.set(response);
                latch.countDown();
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                fail("Failure executing the request.");
            }
        });
        latch.await(1, TimeUnit.SECONDS);
        assertEquals(latch.getCount(), 1);
        assertEquals(responseRef.get().body(), "VERY_BASIC_BODY");
        assertTrue("Response isn't from network.", SmartCache.isResponseFromNetwork(responseRef.get()));

        final CountDownLatch doubleLatch = new CountDownLatch(2);
        final AtomicReference<Response<String>> secondTestResponse = new AtomicReference<>();
        final AtomicReference<Response<String>> secondTestResponse2 = new AtomicReference<>();

        demoService.getHome().enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                doubleLatch.countDown();
                if(doubleLatch.getCount() == 1){
                    secondTestResponse.set(response);
                    log.info("Got a response in pass #2 in twoTestCheck() call. [latch = 1]");
                } else { // the network one.
                    secondTestResponse2.set(response);
                    log.info("Got a response in pass #2 in twoTestCheck() call. [latch = 2]");
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                fail("Failure executing the request.");
            }
        });

        assertTrue(doubleLatch.await(1, TimeUnit.SECONDS));
        Response<String> resp1 = secondTestResponse.get();
        Response<String> resp2 = secondTestResponse2.get();

        assertNotNull(resp1);
        assertNotNull(resp2);

        assertFalse(SmartCache.isResponseFromNetwork(secondTestResponse.get()));
        assertTrue(SmartCache.isResponseFromNetwork(secondTestResponse2.get()));
    }

    @Test
    public void testDisabledCache() throws Exception {
        MockResponse resp = new MockResponse();
        resp.setBody("VERY_BASIC_BODY");
        server.enqueue(resp);
        server.enqueue(resp.clone());

        for(int i = 0; i < 2; i++) {
            log.info("Doing disabled test cache twice. i=" + String.valueOf(i));
            CountDownLatch latch = new CountDownLatch(2);
            demoService.postHome().enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    latch.countDown();
                    log.info("Got a response in testDisabledCache()");
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    log.warning("Got a failure in testDisabledCache()");
                }
            });
            latch.await(1, TimeUnit.SECONDS);
            assertEquals(latch.getCount(), 1);
        }
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
        @POST("/")
        SmartCall<String> postHome();
    }
}
