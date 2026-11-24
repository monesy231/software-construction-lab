package edu.hitsz;

import android.app.Activity;
import android.app.AlertDialog;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.lang.ref.WeakReference;
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
    private static final int MSG_GAME_OVER = 1;

    private final Handler mainHandler = new GameHandler(this);
    private GameDifficulty difficulty;
    private Game game;
    private MusicManager musicManager;
    private AlertDialog resultDialog;
    private boolean resumed;
    private Integer pendingResultScore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        difficulty = parseDifficulty(getIntent().getStringExtra(EXTRA_DIFFICULTY));
        boolean audioEnabled = getIntent().getBooleanExtra(EXTRA_AUDIO_ENABLED, true);
        musicManager = new MusicManager(this, audioEnabled);
        IntConsumer onGameOver = score -> {
            Message msg = Message.obtain(mainHandler, MSG_GAME_OVER, score, 0);
            mainHandler.sendMessage(msg);
        };

        game = switch (difficulty) {
            case EASY -> new EasyGame(this, musicManager, onGameOver);
            case NORMAL -> new NormalGame(this, musicManager, onGameOver);
            case HARD -> new HardGame(this, musicManager, onGameOver);
        };
        setContentView(game);
    }

    private GameDifficulty parseDifficulty(String difficultyName) {
        if (difficultyName == null) {
            return GameDifficulty.EASY;
        }
        try {
            return GameDifficulty.valueOf(difficultyName);
        } catch (IllegalArgumentException ignored) {
            return GameDifficulty.EASY;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        game.resumeGame();
        resumed = true;
        showPendingResult();
    }

    @Override
    protected void onPause() {
        resumed = false;
        game.pauseGame();
        super.onPause();
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
        if (!resumed || pendingResultScore == null || isFinishing() || isDestroyed()) {
            return;
        }
        if (resultDialog != null && resultDialog.isShowing()) {
            return;
        }
        int score = pendingResultScore;
        pendingResultScore = null;

        resultDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.result_title)
                .setMessage(getString(R.string.result_message, difficulty.getDisplayName(), score))
                .setNegativeButton(R.string.return_to_menu, (dialog, which) -> finish())
                .setPositiveButton(R.string.play_again, (dialog, which) -> recreate())
                .setOnCancelListener(dialog -> finish())
                .show();
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
                    activity.pendingResultScore = msg.arg1;
                    activity.showPendingResult();
                }
                default -> { }
            }
        }
    }
}
