package dimitrovskif.smartcache;

import android.os.Environment;
import android.util.Log;

import com.google.common.hash.Hashing;
import com.jakewharton.disklrucache.DiskLruCache;
import com.squareup.okhttp.Request;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;

import retrofit.Response;

public class DefaultCachingSystem implements CachingSystem {
    private DiskLruCache cache;

    public DefaultCachingSystem(){
        try{
            cache = DiskLruCache.open(new File(Environment.getExternalStorageDirectory(), "monty"),
                1, 1, 1024 * 1024 * 10);
        }catch(IOException exc){
            Log.e("SmartCall", "", exc);
            cache = null;
        }
    }

    @Override
    public <T> void addInCache(Response<T> response, byte[] rawResponse) {
        if(cache == null) return;

        try {
            cache.edit(urlToKey(response.raw().request().url())).set(0, new String(rawResponse, Charset.defaultCharset()));
        }catch(IOException exc){
            Log.e("SmartCall", "", exc);
        }
    }

    @Override
    public <T> byte[] getFromCache(Request request) {
        if(cache == null) return null;
        byte[] response;

        try {
            response = cache.get(urlToKey(request.url())).getString(0).getBytes();
        }catch(IOException | NullPointerException exc){
            Log.e("CachingSystem", "", exc);
            response = null;
        }

        return response;
    }

    private String urlToKey(URL url){
        return Hashing.sha1().hashString(url.toString(), Charset.defaultCharset()).toString();
    }
}
