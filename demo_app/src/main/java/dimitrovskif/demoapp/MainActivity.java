package dimitrovskif.demoapp;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

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

    TextView textContent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textContent = (TextView) findViewById(R.id.textContent);

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
                    StringBuilder sb = new StringBuilder();
                    for(User user : response.body()){
                        sb.append(user.name).append(" ").append(user.email).append("\n");
                    }
                    textContent.setText(sb);
                }
            }

            @Override
            public void onFailure(Throwable t) {

            }
        });
    }

}
