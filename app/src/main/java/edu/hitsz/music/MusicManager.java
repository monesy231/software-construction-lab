package edu.hitsz.music;

public class MusicManager {

    public static final String BGM = "bgm";
    public static final String BOSS_BGM = "boss_bgm";

    public static final String BGM_PATH = "bgm";
    public static final String BOSS_BGM_PATH = "bgm_boss";
    public static final String BOMB_EXPLOSION_PATH = "bomb_explosion";
    public static final String BULLET_HIT_PATH = "bullet_hit";
    public static final String GAME_OVER_PATH = "game_over";
    public static final String GET_SUPPLY_PATH = "get_supply";

    public void loop(String key, String path) {}

    public void playOnce(String path) {}

    public void stop(String key) {}

    public void stopAllLoopMusic() {}
}
