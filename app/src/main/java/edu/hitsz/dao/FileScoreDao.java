package edu.hitsz.dao;

import android.content.Context;
import android.util.AtomicFile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import edu.hitsz.application.GameDifficulty;
import edu.hitsz.score.ScoreRecord;

/** File-backed implementation for the file storage stage of Lab 4. */
public final class FileScoreDao implements ScoreDao {
    private static final Object FILE_LOCK = new Object();
    private static final String NEXT_ID = "nextId";
    private static final String RECORDS = "records";
    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String SCORE = "score";
    private static final String TIME = "time";
    private static final String DIFFICULTY = "difficulty";
    private static final String ROUND_ID = "roundId";
    private static final Comparator<ScoreRecord> RANK_ORDER = Comparator
            .comparingInt(ScoreRecord::score)
            .thenComparingLong(ScoreRecord::playedAt)
            .thenComparingLong(ScoreRecord::id)
            .reversed();

    private final AtomicFile file;

    public FileScoreDao(Context context) {
        file = new AtomicFile(new File(context.getApplicationContext().getFilesDir(), "rank.json"));
    }

    @Override
    public void add(GameDifficulty difficulty, String playerName, int score, long playedAt,
                    String roundId) {
        synchronized (FILE_LOCK) {
            try {
                JSONObject data = read();
                JSONArray rows = data.getJSONArray(RECORDS);
                if (roundId != null && containsRound(rows, roundId)) {
                    return;
                }
                long id = data.getLong(NEXT_ID);
                rows.put(new JSONObject()
                        .put(ID, id)
                        .put(NAME, Objects.requireNonNull(playerName))
                        .put(SCORE, score)
                        .put(TIME, playedAt)
                        .put(DIFFICULTY, difficulty.name())
                        .put(ROUND_ID, roundId));
                data.put(NEXT_ID, Math.incrementExact(id));
                write(data);
            } catch (IOException | JSONException e) {
                throw new IllegalStateException("Could not add leaderboard record", e);
            }
        }
    }

    private boolean containsRound(JSONArray rows, String roundId) throws JSONException {
        for (int i = 0; i < rows.length(); i++) {
            if (roundId.equals(rows.getJSONObject(i).optString(ROUND_ID, null))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<ScoreRecord> findByDifficulty(GameDifficulty difficulty) {
        synchronized (FILE_LOCK) {
            try {
                List<ScoreRecord> scores = new ArrayList<>();
                JSONArray rows = read().getJSONArray(RECORDS);
                for (int i = 0; i < rows.length(); i++) {
                    JSONObject row = rows.getJSONObject(i);
                    if (difficulty.name().equals(row.getString(DIFFICULTY))) {
                        scores.add(new ScoreRecord(row.getLong(ID), row.getString(NAME),
                                row.getInt(SCORE), row.getLong(TIME)));
                    }
                }
                scores.sort(RANK_ORDER);
                return scores;
            } catch (IOException | JSONException e) {
                throw new IllegalStateException("Could not read leaderboard records", e);
            }
        }
    }

    @Override
    public boolean delete(GameDifficulty difficulty, long id) {
        synchronized (FILE_LOCK) {
            try {
                JSONObject data = read();
                JSONArray rows = data.getJSONArray(RECORDS);
                for (int i = 0; i < rows.length(); i++) {
                    JSONObject row = rows.getJSONObject(i);
                    if (row.getLong(ID) == id && difficulty.name().equals(row.getString(DIFFICULTY))) {
                        rows.remove(i);
                        write(data);
                        return true;
                    }
                }
                return false;
            } catch (IOException | JSONException e) {
                throw new IllegalStateException("Could not delete leaderboard record", e);
            }
        }
    }

    private JSONObject read() throws IOException, JSONException {
        try {
            return new JSONObject(new String(file.readFully(), StandardCharsets.UTF_8));
        } catch (FileNotFoundException e) {
            if (file.getBaseFile().exists()) {
                throw e;
            }
            return new JSONObject().put(NEXT_ID, 1L).put(RECORDS, new JSONArray());
        }
    }

    private void write(JSONObject data) throws IOException {
        FileOutputStream output = file.startWrite();
        try {
            output.write(data.toString().getBytes(StandardCharsets.UTF_8));
            file.finishWrite(output);
        } catch (IOException | RuntimeException e) {
            file.failWrite(output);
            throw e;
        }
    }
}
