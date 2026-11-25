package edu.hitsz.dao;

import static edu.hitsz.dao.RankDbHelper.DIFFICULTY;
import static edu.hitsz.dao.RankDbHelper.ID;
import static edu.hitsz.dao.RankDbHelper.NAME;
import static edu.hitsz.dao.RankDbHelper.ORDER_BY;
import static edu.hitsz.dao.RankDbHelper.ROUND_ID;
import static edu.hitsz.dao.RankDbHelper.SCORE;
import static edu.hitsz.dao.RankDbHelper.TABLE;
import static edu.hitsz.dao.RankDbHelper.TIME;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import edu.hitsz.application.GameDifficulty;
import edu.hitsz.score.ScoreRecord;

public final class SQLiteScoreDao implements ScoreDao, AutoCloseable {
    private final RankDbHelper helper;

    public SQLiteScoreDao(Context context) {
        helper = new RankDbHelper(context);
    }

    @Override
    public void add(GameDifficulty difficulty, String playerName, int score, long playedAt,
                    String roundId) {
        ContentValues values = new ContentValues();
        values.put(DIFFICULTY, difficulty.name());
        values.put(NAME, Objects.requireNonNull(playerName));
        values.put(SCORE, score);
        values.put(TIME, playedAt);
        SQLiteDatabase db = helper.getWritableDatabase();
        if (roundId == null) {
            db.insertOrThrow(TABLE, null, values);
            return;
        }
        values.put(ROUND_ID, roundId);
        long rowId = db.insertWithOnConflict(TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        if (rowId == -1 && DatabaseUtils.queryNumEntries(db, TABLE, ROUND_ID + " = ?",
                new String[] {roundId}) == 0) {
            throw new SQLiteException("Could not save leaderboard round");
        }
    }

    @Override
    public List<ScoreRecord> findByDifficulty(GameDifficulty difficulty) {
        List<ScoreRecord> scores = new ArrayList<>();
        try (Cursor cursor = helper.getReadableDatabase().query(TABLE,
                new String[] {ID, NAME, SCORE, TIME}, DIFFICULTY + " = ?",
                new String[] {difficulty.name()}, null, null, ORDER_BY)) {
            while (cursor.moveToNext()) {
                scores.add(new ScoreRecord(cursor.getLong(0), cursor.getString(1),
                        cursor.getInt(2), cursor.getLong(3)));
            }
        }
        return scores;
    }

    @Override
    public boolean delete(GameDifficulty difficulty, long id) {
        return helper.getWritableDatabase().delete(TABLE, DIFFICULTY + " = ? AND " + ID + " = ?",
                new String[] {difficulty.name(), Long.toString(id)}) > 0;
    }

    @Override
    public void close() {
        helper.close();
    }
}
