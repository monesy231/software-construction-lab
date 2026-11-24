package edu.hitsz.application;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import edu.hitsz.R;
import edu.hitsz.aircraft.BossEnemy;
import edu.hitsz.aircraft.EliteEnemy;
import edu.hitsz.aircraft.ElitePlusEnemy;
import edu.hitsz.aircraft.EliteProEnemy;
import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.aircraft.MobEnemy;
import edu.hitsz.bullet.EnemyBullet;
import edu.hitsz.bullet.HeroBullet;
import edu.hitsz.supply.BloodSupply;
import edu.hitsz.supply.BombSupply;
import edu.hitsz.supply.FirePlusSupply;
import edu.hitsz.supply.FireSupply;
import edu.hitsz.supply.FreezeSupply;

import java.util.HashMap;
import java.util.Map;

/**
 * 统一管理图片加载和访问
 */
public class ImageManager {

    private static final Map<String, Bitmap> CLASSNAME_IMAGE_MAP = new HashMap<>();
    private static boolean initialized = false;
    private static GameDifficulty loadedDifficulty;
    private static Bitmap rawBackgroundImage;

    public static Bitmap BACKGROUND_IMAGE;
    public static Bitmap HERO_IMAGE;
    public static Bitmap HERO_BULLET_IMAGE;
    public static Bitmap ENEMY_BULLET_IMAGE;
    public static Bitmap MOB_ENEMY_IMAGE;
    public static Bitmap ELITE_ENEMY_IMAGE;
    public static Bitmap ELITE_PLUS_ENEMY_IMAGE;
    public static Bitmap ELITE_PRO_ENEMY_IMAGE;
    public static Bitmap BOSS_ENEMY_IMAGE;
    public static Bitmap BLOOD_SUPPLY_IMAGE;
    public static Bitmap FIRE_SUPPLY_IMAGE;
    public static Bitmap FIRE_PLUS_SUPPLY_IMAGE;
    public static Bitmap BOMB_SUPPLY_IMAGE;
    public static Bitmap FREEZE_SUPPLY_IMAGE;

    public static synchronized void initImage(Context context, GameDifficulty difficulty) {
        Context appContext = context.getApplicationContext();
        if (!initialized) {
            HERO_IMAGE = BitmapFactory.decodeResource(appContext.getResources(), R.drawable.hero);
            MOB_ENEMY_IMAGE = BitmapFactory.decodeResource(appContext.getResources(), R.drawable.mob);
            ELITE_ENEMY_IMAGE = BitmapFactory.decodeResource(appContext.getResources(), R.drawable.elite);
            ELITE_PLUS_ENEMY_IMAGE = BitmapFactory.decodeResource(appContext.getResources(), R.drawable.elite_plus);
            ELITE_PRO_ENEMY_IMAGE = BitmapFactory.decodeResource(appContext.getResources(), R.drawable.elite_pro);
            BOSS_ENEMY_IMAGE = BitmapFactory.decodeResource(appContext.getResources(), R.drawable.boss);

            HERO_BULLET_IMAGE = BitmapFactory.decodeResource(appContext.getResources(), R.drawable.bullet_hero);
            ENEMY_BULLET_IMAGE = BitmapFactory.decodeResource(appContext.getResources(), R.drawable.bullet_enemy);

            BLOOD_SUPPLY_IMAGE = BitmapFactory.decodeResource(appContext.getResources(), R.drawable.prop_blood);
            FIRE_SUPPLY_IMAGE = BitmapFactory.decodeResource(appContext.getResources(), R.drawable.prop_bullet);
            FIRE_PLUS_SUPPLY_IMAGE = BitmapFactory.decodeResource(appContext.getResources(), R.drawable.prop_bullet_plus);
            BOMB_SUPPLY_IMAGE = BitmapFactory.decodeResource(appContext.getResources(), R.drawable.prop_bomb);
            FREEZE_SUPPLY_IMAGE = BitmapFactory.decodeResource(appContext.getResources(), R.drawable.prop_freeze);

            CLASSNAME_IMAGE_MAP.put(HeroAircraft.class.getName(), HERO_IMAGE);
            CLASSNAME_IMAGE_MAP.put(MobEnemy.class.getName(), MOB_ENEMY_IMAGE);
            CLASSNAME_IMAGE_MAP.put(EliteEnemy.class.getName(), ELITE_ENEMY_IMAGE);
            CLASSNAME_IMAGE_MAP.put(ElitePlusEnemy.class.getName(), ELITE_PLUS_ENEMY_IMAGE);
            CLASSNAME_IMAGE_MAP.put(EliteProEnemy.class.getName(), ELITE_PRO_ENEMY_IMAGE);
            CLASSNAME_IMAGE_MAP.put(BossEnemy.class.getName(), BOSS_ENEMY_IMAGE);

            CLASSNAME_IMAGE_MAP.put(HeroBullet.class.getName(), HERO_BULLET_IMAGE);
            CLASSNAME_IMAGE_MAP.put(EnemyBullet.class.getName(), ENEMY_BULLET_IMAGE);

            CLASSNAME_IMAGE_MAP.put(BloodSupply.class.getName(), BLOOD_SUPPLY_IMAGE);
            CLASSNAME_IMAGE_MAP.put(FireSupply.class.getName(), FIRE_SUPPLY_IMAGE);
            CLASSNAME_IMAGE_MAP.put(FirePlusSupply.class.getName(), FIRE_PLUS_SUPPLY_IMAGE);
            CLASSNAME_IMAGE_MAP.put(BombSupply.class.getName(), BOMB_SUPPLY_IMAGE);
            CLASSNAME_IMAGE_MAP.put(FreezeSupply.class.getName(), FREEZE_SUPPLY_IMAGE);
            initialized = true;
        }
        setBackgroundImage(appContext, difficulty);
    }

    public static synchronized void setBackgroundImage(Context context, GameDifficulty difficulty) {
        if (rawBackgroundImage != null && loadedDifficulty == difficulty) {
            if (BACKGROUND_IMAGE == null) {
                BACKGROUND_IMAGE = rawBackgroundImage;
            }
            return;
        }
        Context appContext = context.getApplicationContext();
        int bgResId = R.drawable.bg;
        if (difficulty == GameDifficulty.NORMAL) {
            bgResId = R.drawable.bg2;
        } else if (difficulty == GameDifficulty.HARD) {
            bgResId = R.drawable.bg5;
        }
        rawBackgroundImage = BitmapFactory.decodeResource(appContext.getResources(), bgResId);
        loadedDifficulty = difficulty;
        BACKGROUND_IMAGE = rawBackgroundImage;
    }

    public static synchronized void scaleBackground(int width, int height) {
        if (rawBackgroundImage == null || width <= 0 || height <= 0) {
            return;
        }
        if (BACKGROUND_IMAGE != null
                && BACKGROUND_IMAGE.getWidth() == width
                && BACKGROUND_IMAGE.getHeight() == height) {
            return;
        }
        BACKGROUND_IMAGE = Bitmap.createScaledBitmap(rawBackgroundImage, width, height, true);
    }

    public static Bitmap get(String className) {
        return CLASSNAME_IMAGE_MAP.get(className);
    }

    public static Bitmap get(Object obj) {
        if (obj == null) {
            return null;
        }
        return get(obj.getClass().getName());
    }
}
