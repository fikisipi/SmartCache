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
            DiskLruCache.Editor editor = cache.edit(urlToKey(response.raw().request().url()));
            editor.set(0, new String(rawResponse, Charset.defaultCharset()));
            editor.commit();
        }catch(IOException exc){
            Log.e("SmartCall", "", exc);
        }
    }

    @Override
    public <T> byte[] getFromCache(Request request) {
        if(cache == null){
            Log.e("SmartCall", "Cannot retrieve cache, DiskLru is null.");
            return null;
        }

        byte[] response;

        try {
            String cacheKey = urlToKey(request.url());
            DiskLruCache.Snapshot cacheSnapshot = cache.get(cacheKey);
            if(cacheSnapshot != null){
                response = cacheSnapshot.getString(0).getBytes();
            }else{
                response = null;
            }
        }catch(IOException exc){
            Log.e("CachingSystem", "Cannot get this URL from journal.", exc);
            response = null;
        }

        return response;
    }

    private String urlToKey(URL url){
        return Hashing.sha1().hashString(url.toString(), Charset.defaultCharset()).toString();
    }
}
