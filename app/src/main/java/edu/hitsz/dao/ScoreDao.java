package edu.hitsz.dao;

import java.util.List;

import edu.hitsz.application.GameDifficulty;
import edu.hitsz.score.ScoreRecord;

/** Stores local leaderboard records independently of how they are displayed. */
public interface ScoreDao {
    /** Adds a record with the time expressed as milliseconds since the Unix epoch. */
    default void add(GameDifficulty difficulty, String playerName, int score, long playedAt) {
        add(difficulty, playerName, score, playedAt, null);
    }

    /** Adds a game result only once for the same round ID. A null ID always adds a new record. */
    void add(GameDifficulty difficulty, String playerName, int score, long playedAt, String roundId);

    /** Lists records by descending score, then descending time and record ID. */
    List<ScoreRecord> findByDifficulty(GameDifficulty difficulty);

    /** Deletes the record with the given ID if it belongs to this difficulty. */
    boolean delete(GameDifficulty difficulty, long id);
}
