package vn.edu.auction.client.api;

import java.util.concurrent.Callable;
import java.util.function.Consumer;
import javafx.concurrent.Task;

public final class FxAsync {
    private FxAsync() {}

    /** work chạy ở thread nền; onOk/onFail luôn chạy trên JavaFX Application Thread. */
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