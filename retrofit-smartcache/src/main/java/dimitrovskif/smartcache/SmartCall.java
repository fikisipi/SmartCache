package dimitrovskif.smartcache;

import com.squareup.okhttp.Request;

import java.io.IOException;
import java.lang.reflect.Type;

import retrofit.Callback;
import retrofit.Response;

public interface SmartCall<T>{
    /**
     * Asynchronously send the request and notify {@code callback} of its response or if an error
     * occurred talking to the server, creating the request, or processing the response.
     */
    void enqueue(Callback<T> callback);

    /**
     * Returns a runtime {@link Type} that corresponds to the response type specified in your
     * service.
     */
    Type responseType();

    /**
     * Builds a new {@link Request} that is identical to the one that will be dispatched
     * when the {@link SmartCall} is executed/enqueued.
     */
    Request buildRequest();

    /**
     * Create a new, identical call to this one which can be enqueued or executed even if this call
     * has already been.
     */
    SmartCall<T> clone();
}