package dimitrovskif.smartcache;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;

class AndroidExecutor implements Executor {
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override public void execute(Runnable r) {
        handler.post(r);
    }
}