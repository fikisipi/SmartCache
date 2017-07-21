package dimitrovskif.smartcache;


import java.io.IOException;
import java.lang.reflect.Type;

import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
    Call<T> clone();

    /* ================================================================ */
    /* Now it's time for the blocking methods - which can't be smart :(
    /* ================================================================ */

    /**
     * Synchronously send the request and return its response. NOTE: No smart caching allowed!
     *
     * @throws IOException if a problem occurred talking to the server.
     * @throws RuntimeException (and subclasses) if an unexpected error occurs creating the request
     * or decoding the response.
     */
    Response<T> execute() throws IOException;

    /**
     * Cancel this call. An attempt will be made to cancel in-flight calls, and if the call has not
     * yet been executed it never will be.
     */
    void cancel();
}