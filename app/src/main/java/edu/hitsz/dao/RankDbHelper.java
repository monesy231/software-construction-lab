package edu.hitsz.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

final class RankDbHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "rank.db";
    private static final int DB_VERSION = 1;

    static final String TABLE = "rank";
    static final String ID = "_id";
    static final String NAME = "name";
    static final String SCORE = "score";
    static final String TIME = "time";
    static final String DIFFICULTY = "difficulty";
    static final String ROUND_ID = "round_id";
    static final String ORDER_BY = SCORE + " DESC, " + TIME + " DESC, " + ID + " DESC";

    RankDbHelper(Context context) {
        super(context.getApplicationContext(), DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE + " ("
                + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + NAME + " TEXT NOT NULL, "
                + SCORE + " INTEGER NOT NULL, "
                + TIME + " INTEGER NOT NULL, "
                + DIFFICULTY + " TEXT NOT NULL, "
                + ROUND_ID + " TEXT UNIQUE)");
        db.execSQL("CREATE INDEX rank_by_difficulty ON " + TABLE
                + " (" + DIFFICULTY + ", " + ORDER_BY + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        throw new SQLiteException("No leaderboard migration from " + oldVersion + " to " + newVersion);
    }
}
