package edu.hitsz.music;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.hitsz.R;

/**
 * 封装短音效的预加载与并发播放，由 MusicManager 在主线程调用。
 */
public class MySoundPool {
    private static final int MAX_STREAMS = 10;
    private static final Map<String, Integer> EFFECT_RESOURCES = Map.of(
            MusicManager.BOMB_EXPLOSION_PATH, R.raw.bomb_explosion,
            MusicManager.BULLET_HIT_PATH, R.raw.bullet_hit,
            MusicManager.GAME_OVER_PATH, R.raw.game_over,
            MusicManager.GET_SUPPLY_PATH, R.raw.get_supply
    );

    private final SoundPool soundPool;
    private final Map<String, Integer> soundPoolMap = new HashMap<>();
    private final Set<Integer> loadedSamples = new HashSet<>();
    private final Set<Integer> pendingSamples = new HashSet<>();
    private boolean paused;
    private boolean released;

    public MySoundPool(Context context) {
        Context appContext = context.getApplicationContext();
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundPool = new SoundPool.Builder()
                .setAudioAttributes(audioAttributes)
                .setMaxStreams(MAX_STREAMS)
                .build();
        soundPool.setOnLoadCompleteListener((pool, sampleId, status) -> {
            if (status != 0) {
                return;
            }
            boolean playPending;
            synchronized (MySoundPool.this) {
                if (released) {
                    return;
                }
                loadedSamples.add(sampleId);
                playPending = !paused && pendingSamples.remove(sampleId);
            }
            if (playPending) {
                playLoadedSample(sampleId, priorityForSample(sampleId));
            }
        });
        for (Map.Entry<String, Integer> effect : EFFECT_RESOURCES.entrySet()) {
            soundPoolMap.put(effect.getKey(), soundPool.load(appContext, effect.getValue(), 1));
        }
    }

    public void play(String effect) {
        Integer sampleId = soundPoolMap.get(effect);
        if (sampleId == null) {
            return;
        }
        synchronized (this) {
            if (released || paused) {
                return;
            }
            if (!loadedSamples.contains(sampleId)) {
                if (isCriticalEffect(effect)) {
                    pendingSamples.add(sampleId);
                }
                return;
            }
        }
        playLoadedSample(sampleId, priorityForEffect(effect));
    }

    public void pause() {
        synchronized (this) {
            if (released) {
                return;
            }
            paused = true;
            pendingSamples.clear();
        }
        soundPool.autoPause();
    }

    public void resume() {
        synchronized (this) {
            if (released) {
                return;
            }
            paused = false;
        }
        soundPool.autoResume();
    }

    public void release() {
        synchronized (this) {
            if (released) {
                return;
            }
            released = true;
            pendingSamples.clear();
            loadedSamples.clear();
        }
        soundPool.setOnLoadCompleteListener(null);
        soundPool.release();
    }

    private void playLoadedSample(int sampleId, int priority) {
        soundPool.play(sampleId, 1.0f, 1.0f, priority, 0, 1.0f);
    }

    private boolean isCriticalEffect(String effect) {
        return MusicManager.GAME_OVER_PATH.equals(effect)
                || MusicManager.BOMB_EXPLOSION_PATH.equals(effect);
    }

    private int priorityForEffect(String effect) {
        return isCriticalEffect(effect) ? 2 : 1;
    }

    private int priorityForSample(int sampleId) {
        Integer gameOverId = soundPoolMap.get(MusicManager.GAME_OVER_PATH);
        Integer bombId = soundPoolMap.get(MusicManager.BOMB_EXPLOSION_PATH);
        if ((gameOverId != null && gameOverId == sampleId)
                || (bombId != null && bombId == sampleId)) {
            return 2;
        }
        return 1;
    }
}
