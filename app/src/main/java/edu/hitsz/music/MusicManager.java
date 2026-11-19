package edu.hitsz.music;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.hitsz.R;

public class MusicManager {

    public static final String BGM = "bgm";
    public static final String BOSS_BGM = "boss_bgm";

    public static final String BGM_PATH = "bgm";
    public static final String BOSS_BGM_PATH = "bgm_boss";
    public static final String BOMB_EXPLOSION_PATH = "bomb_explosion";
    public static final String BULLET_HIT_PATH = "bullet_hit";
    public static final String GAME_OVER_PATH = "game_over";
    public static final String GET_SUPPLY_PATH = "get_supply";

    private static final Map<String, Integer> MUSIC_RESOURCES = Map.of(
            BGM_PATH, R.raw.bgm,
            BOSS_BGM_PATH, R.raw.bgm_boss
    );

    private static final Map<String, Integer> EFFECT_RESOURCES = Map.of(
            BOMB_EXPLOSION_PATH, R.raw.bomb_explosion,
            BULLET_HIT_PATH, R.raw.bullet_hit,
            GAME_OVER_PATH, R.raw.game_over,
            GET_SUPPLY_PATH, R.raw.get_supply
    );

    private final Context context;
    private final Map<String, MediaPlayer> loopPlayers = new HashMap<>();
    private final Map<String, Integer> effectIds = new HashMap<>();
    private final Set<Integer> loadedEffects = new HashSet<>();
    private SoundPool soundPool;
    private boolean paused;

    public MusicManager(Context context, boolean enabled) {
        this.context = context.getApplicationContext();
        if (enabled) {
            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            soundPool = new SoundPool.Builder().setAudioAttributes(attributes).setMaxStreams(4).build();
            soundPool.setOnLoadCompleteListener(this::onEffectLoaded);
            for (Map.Entry<String, Integer> effect : EFFECT_RESOURCES.entrySet()) {
                effectIds.put(effect.getKey(), soundPool.load(this.context, effect.getValue(), 1));
            }
        }
    }

    private synchronized void onEffectLoaded(SoundPool pool, int sampleId, int status) {
        if (pool == soundPool && status == 0) {
            loadedEffects.add(sampleId);
        }
    }

    public synchronized void loop(String key, String path) {
        if (soundPool == null) {
            return;
        }
        if (!loopPlayers.containsKey(key)) {
            MediaPlayer player = MediaPlayer.create(context, MUSIC_RESOURCES.get(path));
            if (player == null) {
                return;
            }
            player.setLooping(true);
            loopPlayers.put(key, player);
        }
        updateBackgroundMusic();
    }

    private void updateBackgroundMusic() {
        MediaPlayer boss = loopPlayers.get(BOSS_BGM);
        setPlaying(loopPlayers.get(BGM), !paused && boss == null);
        setPlaying(boss, !paused);
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

    public synchronized void playOnce(String path) {
        Integer sampleId = effectIds.get(path);
        if (soundPool != null && !paused && loadedEffects.contains(sampleId)) {
            soundPool.play(sampleId, 1f, 1f, 1, 0, 1f);
        }
    }

    public synchronized void stop(String key) {
        MediaPlayer player = loopPlayers.remove(key);
        if (player != null) {
            player.release();
        }
        updateBackgroundMusic();
    }

    public synchronized void stopAllLoopMusic() {
        loopPlayers.values().forEach(MediaPlayer::release);
        loopPlayers.clear();
    }

    public synchronized void pause() {
        paused = true;
        updateBackgroundMusic();
        if (soundPool != null) {
            soundPool.autoPause();
        }
    }

    public synchronized void resume() {
        paused = false;
        updateBackgroundMusic();
        if (soundPool != null) {
            soundPool.autoResume();
        }
    }

    public synchronized void release() {
        stopAllLoopMusic();
        if (soundPool != null) {
            soundPool.setOnLoadCompleteListener(null);
            soundPool.release();
            soundPool = null;
        }
        loadedEffects.clear();
        effectIds.clear();
    }
}
