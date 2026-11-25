package edu.hitsz;

import android.app.Activity;
import android.app.AlertDialog;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.UUID;
import java.util.function.IntConsumer;

import edu.hitsz.application.EasyGame;
import edu.hitsz.application.Game;
import edu.hitsz.application.GameDifficulty;
import edu.hitsz.application.HardGame;
import edu.hitsz.application.NormalGame;
import edu.hitsz.music.MusicManager;

public class GameActivity extends Activity {
    static final String EXTRA_DIFFICULTY = "edu.hitsz.extra.DIFFICULTY";
    static final String EXTRA_AUDIO_ENABLED = "edu.hitsz.extra.AUDIO_ENABLED";
    private static final String TAG = "GameActivity";
    private static final String STATE_RESULT_SCORE = "result_score";
    private static final String STATE_RESULT_TIME = "result_time";
    private static final String STATE_RESULT_NAME = "result_name";
    private static final String STATE_RESULT_SAVED = "result_saved";
    private static final String STATE_RESULT_ROUND = "result_round";
    private static final int MSG_GAME_OVER = 1;

    private final Handler mainHandler = new GameHandler(this);
    private GameDifficulty difficulty;
    private Game game;
    private MusicManager musicManager;
    private AlertDialog resultDialog;
    private ScoreEntry resultEntry;
    private boolean resumed;
    private boolean resultSaved;
    private Integer resultScore;
    private long resultTime;
    private String draftName;
    private String resultRound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        difficulty = GameDifficulty.fromName(getIntent().getStringExtra(EXTRA_DIFFICULTY));
        if (savedInstanceState != null && savedInstanceState.containsKey(STATE_RESULT_SCORE)) {
            resultScore = savedInstanceState.getInt(STATE_RESULT_SCORE);
            resultTime = savedInstanceState.getLong(STATE_RESULT_TIME);
            draftName = savedInstanceState.getString(STATE_RESULT_NAME);
            resultSaved = savedInstanceState.getBoolean(STATE_RESULT_SAVED);
            resultRound = savedInstanceState.getString(STATE_RESULT_ROUND);
        }
        boolean audioEnabled = getIntent().getBooleanExtra(EXTRA_AUDIO_ENABLED, true);
        musicManager = new MusicManager(this, audioEnabled);
        IntConsumer onGameOver = score -> {
            Message msg = Message.obtain(mainHandler, MSG_GAME_OVER, score, 0,
                    System.currentTimeMillis());
            mainHandler.sendMessage(msg);
        };

        game = switch (difficulty) {
            case EASY -> new EasyGame(this, musicManager, onGameOver);
            case NORMAL -> new NormalGame(this, musicManager, onGameOver);
            case HARD -> new HardGame(this, musicManager, onGameOver);
        };
        setContentView(game);
    }

    @Override
    protected void onResume() {
        super.onResume();
        resumed = true;
        if (resultScore == null) {
            game.resumeGame();
        }
        showPendingResult();
    }

    @Override
    protected void onPause() {
        resumed = false;
        game.pauseGame();
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (resultScore != null) {
            outState.putInt(STATE_RESULT_SCORE, resultScore);
            outState.putLong(STATE_RESULT_TIME, resultTime);
            outState.putString(STATE_RESULT_NAME, resultEntry == null
                    ? draftName : resultEntry.name.getText().toString());
            outState.putBoolean(STATE_RESULT_SAVED, resultSaved);
            outState.putString(STATE_RESULT_ROUND, resultRound);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        mainHandler.removeCallbacksAndMessages(null);
        if (resultDialog != null) {
            resultDialog.setOnCancelListener(null);
            resultDialog.dismiss();
            resultDialog = null;
        }
        musicManager.release();
        super.onDestroy();
    }

    private void showPendingResult() {
        if (!resumed || resultScore == null || isFinishing() || isDestroyed()) {
            return;
        }
        if (resultSaved) {
            startActivity(ScoreBoardActivity.newIntent(this, difficulty));
            finish();
            return;
        }
        if (resultDialog != null && resultDialog.isShowing()) {
            return;
        }

        resultDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.result_title)
                .setMessage(getString(R.string.result_message, difficulty.getDisplayName(), resultScore))
                .setView(R.layout.dialog_score_entry)
                .setNegativeButton(R.string.return_to_menu, (dialog, which) -> finish())
                .setNeutralButton(R.string.play_again, (dialog, which) -> {
                    resultScore = null;
                    recreate();
                })
                .setPositiveButton(R.string.save_result, null)
                .setOnCancelListener(dialog -> finish())
                .show();
        resultEntry = new ScoreEntry(resultDialog, false);
        resultEntry.name.setText(draftName);
        resultDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> saveResult());
    }

    private void saveResult() {
        String playerName = resultEntry.readName();
        if (playerName == null) {
            return;
        }
        int score = resultScore;
        long playedAt = resultTime;
        String round = resultRound;
        setSavePending(true);
        ScoreStorage.run(this, dao -> {
            dao.add(difficulty, playerName, score, playedAt, round);
            return null;
        }).whenCompleteAsync((unused, failure) -> {
            if (isFinishing() || isDestroyed()) {
                return;
            }
            if (failure != null) {
                Log.e(TAG, "Could not save game result", failure);
                setSavePending(false);
                resultEntry.name.setError(getString(R.string.ranking_save_failed));
                return;
            }
            resultSaved = true;
            showPendingResult();
        }, getMainExecutor());
    }

    private void setSavePending(boolean pending) {
        resultEntry.name.setEnabled(!pending);
        resultDialog.setCancelable(!pending);
        resultDialog.setCanceledOnTouchOutside(!pending);
        resultDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(!pending);
        resultDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(!pending);
        resultDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setEnabled(!pending);
    }

    private static final class GameHandler extends Handler {
        private final WeakReference<GameActivity> activityRef;

        GameHandler(GameActivity activity) {
            super(Looper.getMainLooper());
            this.activityRef = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            GameActivity activity = activityRef.get();
            if (activity == null) {
                return;
            }
            switch (msg.what) {
                case MSG_GAME_OVER -> {
                    activity.resultScore = msg.arg1;
                    activity.resultTime = (Long) msg.obj;
                    activity.resultRound = UUID.randomUUID().toString();
                    activity.showPendingResult();
                }
                default -> { }
            }
        }
    }
}
