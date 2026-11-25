package edu.hitsz;

import android.content.Context;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

import edu.hitsz.dao.SQLiteScoreDao;
import edu.hitsz.dao.ScoreDao;

final class ScoreStorage {
    private static final ExecutorService WORKER = Executors.newSingleThreadExecutor(
            task -> new Thread(task, "ScoreStorage"));
    // Accessed only on WORKER. The helper and its connection are shared by both screens.
    private static ScoreDao dao;

    private ScoreStorage() {
    }

    static <T> CompletableFuture<T> run(Context context, Function<ScoreDao, T> action) {
        Context appContext = context.getApplicationContext();
        return CompletableFuture.supplyAsync(() -> {
            if (dao == null) {
                dao = new SQLiteScoreDao(appContext);
            }
            return action.apply(dao);
        }, WORKER);
    }
}
