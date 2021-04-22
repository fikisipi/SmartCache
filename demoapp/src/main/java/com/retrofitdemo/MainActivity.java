package com.retrofitdemo;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import dimitrovskif.smartcache.SmartCall;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

public class MainActivity extends Activity {
    static class Comment {
        public String email = "empty";
        public String body = "empty";
        public Comment(String _email, String _body) {
            email = _email; body = _body;
        }
    }

    public interface CommentSvc {
        @GET("comments")
        Call<List<Comment>> getComments();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ListView demoList = findViewById(R.id.demoList);
        final CommentAdapter adapter = new CommentAdapter(this);
        demoList.setAdapter(adapter);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://mockbin.org/bin/ec4a0020-37d0-4221-b4fd-7002000b2019/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        CommentSvc service = retrofit.create(CommentSvc.class);
        service.getComments().enqueue(new Callback<List<Comment>>() {
            @Override
            public void onResponse(Call<List<Comment>> call, Response<List<Comment>> response) {
                List<Comment> l = response.body();
                runOnUiThread(() -> {
                    if(l == null) return;
                    Log.w("Filip", String.valueOf(l.size()));
                    adapter.addAll(l);
                });
            }

            @Override
            public void onFailure(Call<List<Comment>> call, Throwable t) {
                Log.w("Filip", t.getMessage());
            }
        });
    }

    class CommentAdapter extends ArrayAdapter<Comment>{
        CommentAdapter(Context ctx) {
            super(ctx, R.layout.comment_row, R.id.comment_text);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = super.getView(position, convertView, parent);
            Comment comm = getItem(position);
            ((TextView)row.findViewById(R.id.comment_text)).setText(comm.body);
            ((TextView)row.findViewById(R.id.email)).setText(comm.email);
            return row;
        }
    }
}