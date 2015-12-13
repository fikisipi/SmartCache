package dimitrovskif.smartcache;

import com.google.common.reflect.TypeToken;
import com.squareup.okhttp.mockwebserver.MockWebServer;

import org.junit.Rule;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.concurrent.Executor;

import retrofit.Call;
import retrofit.CallAdapter;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public class CallbackTest {
    /***
     * Builds a Retrofit SmartCache factory without Android executor
     */
    private CallAdapter.Factory buildSmartCacheFactory(){
        SmartCallFactory factory = new SmartCallFactory(new Executor() {
            @Override
            public void execute(Runnable command) {
                command.run();
            }
        });

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

    interface DemoService{

    }
}
