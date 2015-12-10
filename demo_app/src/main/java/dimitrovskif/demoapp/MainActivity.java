package dimitrovskif.demoapp;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import java.util.List;

import dimitrovskif.smartcache.SmartCall;
import dimitrovskif.smartcache.SmartCallFactory;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;
import retrofit.http.GET;


/***
 * A demo activity that consumes a placeholder REST API
 */
public class MainActivity extends AppCompatActivity {

    interface PlaceholderAPI{
        @GET("users")
        SmartCall<List<User>> getUsers();
    }

    static class User{
        public long id;
        public String name;
        public String email;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://jsonplaceholder.typicode.com")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(new SmartCallFactory())
                .build();

        PlaceholderAPI api = retrofit.create(PlaceholderAPI.class);
        api.getUsers().enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Response<List<User>> response, Retrofit retrofit) {
                if(response.isSuccess()){
                    new AlertDialog.Builder(MainActivity.this)
                            .setMessage("Got " + response.body().size() + " users.")
                            .show();
                }
            }

            @Override
            public void onFailure(Throwable t) {

            }
        });
    }

}
