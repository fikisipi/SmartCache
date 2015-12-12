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
    public static byte[] responseToBytes(Retrofit retrofit, Response response, Type responseType,
                                         Annotation[] annotations){
        for(Converter.Factory factory : retrofit.converterFactories()){
            if(factory == null) continue;
            Converter<Response, RequestBody> converter =
                    (Converter<Response, RequestBody>)
                            factory.toRequestBody(responseType, annotations);

            if(converter != null){
                Buffer buff = new Buffer();
                try {
                    converter.convert(response).writeTo(buff);
                }catch(IOException ioException){
                    continue;
                }

                return buff.readByteArray();
            }
        }
        return null;
    }
}
