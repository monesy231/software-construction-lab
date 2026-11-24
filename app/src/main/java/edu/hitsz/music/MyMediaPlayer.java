package edu.hitsz.music;

import android.content.Context;
import android.media.MediaPlayer;

import edu.hitsz.R;

/**
 * 封装循环背景音乐播放（普通 BGM 与 Boss BGM），由 MusicManager 在主线程调用。
 */
public class MyMediaPlayer {
    private final Context appContext;
    private MediaPlayer bgmPlayer;
    private MediaPlayer bossBgmPlayer;
    private boolean paused;
    private boolean released;

    public MyMediaPlayer(Context context) {
        this.appContext = context.getApplicationContext();
    }

    public void playBgm() {
        if (released) {
            return;
        }
        if (bgmPlayer == null) {
            bgmPlayer = createLoopingPlayer(R.raw.bgm);
        }
        syncPlaybackState();
    }

    public void playBossBgm() {
        if (released) {
            return;
        }
        if (bossBgmPlayer == null) {
            bossBgmPlayer = createLoopingPlayer(R.raw.bgm_boss);
        }
        syncPlaybackState();
    }

    public void stopBgm() {
        if (bgmPlayer != null) {
            bgmPlayer.release();
            bgmPlayer = null;
        }
        syncPlaybackState();
    }

    public void stopBossBgm() {
        if (bossBgmPlayer != null) {
            bossBgmPlayer.release();
            bossBgmPlayer = null;
        }
        syncPlaybackState();
    }

    public void stopAll() {
        if (bgmPlayer != null) {
            bgmPlayer.release();
            bgmPlayer = null;
        }
        if (bossBgmPlayer != null) {
            bossBgmPlayer.release();
            bossBgmPlayer = null;
        }
    }

    public void pause() {
        paused = true;
        syncPlaybackState();
    }

    public void resume() {
        paused = false;
        syncPlaybackState();
    }

    public void release() {
        released = true;
        stopAll();
    }

    private MediaPlayer createLoopingPlayer(int resourceId) {
        MediaPlayer player = MediaPlayer.create(appContext, resourceId);
        if (player != null) {
            player.setLooping(true);
        }
        return player;
    }

    private void syncPlaybackState() {
        if (released) {
            return;
        }
        // Boss 出场时暂停普通背景音乐，Boss 退场后从暂停处继续播放
        boolean playBoss = !paused && bossBgmPlayer != null;
        boolean playNormal = !paused && bossBgmPlayer == null && bgmPlayer != null;
        setPlaying(bgmPlayer, playNormal);
        setPlaying(bossBgmPlayer, playBoss);
    }

    private void setPlaying(MediaPlayer player, boolean play) {
        if (player == null) {
            return;
        }
        if (play && !player.isPlaying()) {
            player.start();
        } else if (!play && player.isPlaying()) {
            player.pause();
        }
    }
}
