package vn.edu.auction.client.api;

import java.util.concurrent.Callable;
import java.util.function.Consumer;
import javafx.concurrent.Task;

/** Chạy việc gọi mạng ở thread nền; kết quả / lỗi luôn trả về trên JavaFX Application Thread. */
public final class FxAsync {
    private FxAsync() {}

    public static <T> void run(Callable<T> work, Consumer<T> onOk, Consumer<Throwable> onFail) {
        Task<T> task = new Task<>() {
            @Override protected T call() throws Exception { return work.call(); }
        };
        task.setOnSucceeded(e -> onOk.accept(task.getValue()));
        task.setOnFailed(e -> onFail.accept(task.getException()));
        Thread t = new Thread(task, "api-call");
        t.setDaemon(true);
        t.start();
    }
}
