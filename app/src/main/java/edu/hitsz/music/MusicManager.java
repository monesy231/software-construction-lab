package edu.hitsz.music;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

/**
 * 统一调度游戏背景音乐（MyMediaPlayer）与战斗音效（MySoundPool）。
 */
public class MusicManager {

    public static final String BGM = "bgm";
    public static final String BOSS_BGM = "boss_bgm";

    public static final String BGM_PATH = "bgm";
    public static final String BOSS_BGM_PATH = "bgm_boss";
    public static final String BOMB_EXPLOSION_PATH = "bomb_explosion";
    public static final String BULLET_HIT_PATH = "bullet_hit";
    public static final String GAME_OVER_PATH = "game_over";
    public static final String GET_SUPPLY_PATH = "get_supply";

    private final boolean enabled;
    private final Handler audioHandler = new Handler(Looper.getMainLooper());
    private MyMediaPlayer mediaPlayer;
    private MySoundPool soundPool;
    private boolean paused;
    private boolean released;

    public MusicManager(Context context, boolean enabled) {
        this.enabled = enabled;
        if (enabled) {
            Context appContext = context.getApplicationContext();
            this.mediaPlayer = new MyMediaPlayer(appContext);
            this.soundPool = new MySoundPool(appContext);
        }
    }

    public void loop(String key, String path) {
        audioHandler.post(() -> {
            if (!enabled || released || mediaPlayer == null) {
                return;
            }
            if (BOSS_BGM.equals(key) || BOSS_BGM_PATH.equals(path)) {
                mediaPlayer.playBossBgm();
            } else if (BGM.equals(key) || BGM_PATH.equals(path)) {
                mediaPlayer.playBgm();
            }
        });
    }

    public void playOnce(String path) {
        audioHandler.post(() -> {
            if (!enabled || released || paused || soundPool == null) {
                return;
            }
            soundPool.play(path);
        });
    }

    public void stop(String key) {
        audioHandler.post(() -> {
            if (!enabled || released || mediaPlayer == null) {
                return;
            }
            if (BOSS_BGM.equals(key)) {
                mediaPlayer.stopBossBgm();
            } else if (BGM.equals(key)) {
                mediaPlayer.stopBgm();
            }
        });
    }

    public void stopAllLoopMusic() {
        audioHandler.post(() -> {
            if (!enabled || released || mediaPlayer == null) {
                return;
            }
            mediaPlayer.stopAll();
        });
    }

    public void pause() {
        audioHandler.post(() -> {
            if (!enabled || released) {
                return;
            }
            paused = true;
            if (mediaPlayer != null) {
                mediaPlayer.pause();
            }
            if (soundPool != null) {
                soundPool.pause();
            }
        });
    }

    public void resume() {
        audioHandler.post(() -> {
            if (!enabled || released) {
                return;
            }
            paused = false;
            if (mediaPlayer != null) {
                mediaPlayer.resume();
            }
            if (soundPool != null) {
                soundPool.resume();
            }
        });
    }

    /** 由游戏 Activity 在主线程销毁时调用。 */
    public void release() {
        released = true;
        audioHandler.removeCallbacksAndMessages(null);
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }
}
