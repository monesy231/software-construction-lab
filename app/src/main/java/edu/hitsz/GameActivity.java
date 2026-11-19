package edu.hitsz;

import android.app.Activity;
import android.app.AlertDialog;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

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

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private GameDifficulty difficulty;
    private Game game;
    private MusicManager musicManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        difficulty = GameDifficulty.valueOf(getIntent().getStringExtra(EXTRA_DIFFICULTY));
        boolean audioEnabled = getIntent().getBooleanExtra(EXTRA_AUDIO_ENABLED, true);
        musicManager = new MusicManager(this, audioEnabled);
        IntConsumer onGameOver = score -> mainHandler.post(() -> showResult(score));

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
        game.resumeGame();
    }

    @Override
    protected void onPause() {
        game.pauseGame();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mainHandler.removeCallbacksAndMessages(null);
        musicManager.release();
        super.onDestroy();
    }

    private void showResult(int score) {
        if (isFinishing()) {
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.result_title)
                .setMessage(getString(R.string.result_message, difficulty.getDisplayName(), score))
                .setNegativeButton(R.string.return_to_menu, (dialog, which) -> finish())
                .setPositiveButton(R.string.play_again, (dialog, which) -> recreate())
                .setOnCancelListener(dialog -> finish())
                .show();
    }
}
