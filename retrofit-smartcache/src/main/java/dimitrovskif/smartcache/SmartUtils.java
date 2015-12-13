package dimitrovskif.smartcache;

import android.util.Log;

import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.ResponseBody;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okio.Buffer;
import retrofit.Converter;
import retrofit.Response;
import retrofit.Retrofit;

public final class SmartUtils {
    /*
     * TODO: Do an inverse iteration instead so that the latest Factory that supports <T>
     * does the job?
     */
    @SuppressWarnings("unchecked")
    public static <T> byte[] responseToBytes(Retrofit retrofit, T data, Type dataType,
                                         Annotation[] annotations){
        for(Converter.Factory factory : retrofit.converterFactories()){
            if(factory == null) continue;
            Converter<T, RequestBody> converter =
                    (Converter<T, RequestBody>) factory.toRequestBody(dataType, annotations);

            if(converter != null){
                Buffer buff = new Buffer();
                try {
                    converter.convert(data).writeTo(buff);
                }catch(IOException ioException){
                    continue;
                }

                return buff.readByteArray();
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static <T> T bytesToResponse(Retrofit retrofit, Type dataType, Annotation[] annotations,
                                        byte[] data){
        for(Converter.Factory factory : retrofit.converterFactories()){
            if(factory == null) continue;
            Converter<ResponseBody, T> converter =
                    (Converter<ResponseBody, T>) factory.fromResponseBody(dataType, annotations);

            if(converter != null){
                try {
                    return converter.convert(ResponseBody.create(null, data));
                }catch(IOException | NullPointerException exc){
                    Log.e("SmartCall", "", exc);
                }
            }
        }

        return null;
    }
}
