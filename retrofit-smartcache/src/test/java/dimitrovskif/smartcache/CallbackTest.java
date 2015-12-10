package dimitrovskif.smartcache;

import com.google.common.reflect.TypeToken;

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
    @Test
    public void dispatch_isGood() throws Exception{
        SmartCallFactory f = new SmartCallFactory(new Executor() {
            @Override
            public void execute(Runnable command) {
                command.run();
            }
        });

        Retrofit r = new Retrofit.Builder()
                .baseUrl("http://localhost")
                .addCallAdapterFactory(f)
                .build();
        Type t = new TypeToken<SmartCall<String>>(){}.getType();
        CallAdapter<SmartCall<?>> cc = f.get(t, null, r);
        SmartCall<?> call = cc.adapt(new Call<String>() {
            @Override
            public Response<String> execute() throws IOException {
                return null;
            }

            @Override
            public void enqueue(Callback<String> callback) {
            }

            @Override
            public void cancel() {

            }

            @Override
            public Call<String> clone() {
                return null;
            }
        });

        assertEquals(call.responseType(), String.class);
        call.enqueue(null);

    }

    interface DemoService{

    }
}
