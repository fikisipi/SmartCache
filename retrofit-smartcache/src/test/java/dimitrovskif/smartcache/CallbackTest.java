package dimitrovskif.smartcache;

import com.squareup.okhttp.mockwebserver.MockWebServer;

import org.junit.Rule;
import org.junit.Test;

import java.util.concurrent.Executor;

import retrofit.CallAdapter;
import retrofit.Retrofit;

public class CallbackTest {
    /***
     * Builds a Retrofit SmartCache factory without Android executor
     */
    private CallAdapter.Factory buildSmartCacheFactory(){
        SmartCallFactory factory = new SmartCallFactory(BasicCaching.create(null, 0),
                new MainThreadExecutor());

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
        r.create(DemoService.class);

    }

    static class MainThreadExecutor implements Executor{
        @Override
        public void execute(Runnable command) {
            command.run();
        }
    }

    interface DemoService{

    }
}
