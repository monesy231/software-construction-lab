package edu.hitsz;

import android.graphics.Bitmap;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Map;

import edu.hitsz.aircraft.BossEnemy;
import edu.hitsz.aircraft.EliteEnemy;
import edu.hitsz.aircraft.ElitePlusEnemy;
import edu.hitsz.aircraft.EliteProEnemy;
import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.aircraft.MobEnemy;
import edu.hitsz.application.ImageManager;
import edu.hitsz.bullet.EnemyBullet;
import edu.hitsz.bullet.HeroBullet;
import edu.hitsz.supply.BloodSupply;
import edu.hitsz.supply.BombSupply;
import edu.hitsz.supply.FirePlusSupply;
import edu.hitsz.supply.FireSupply;
import edu.hitsz.supply.FreezeSupply;

/**
 * 为纯 JVM JUnit 单元测试注入占位 Bitmap，避免测试直接依赖 Android 资源解码。
 */
public final class TestBitmapHelper {

    private static boolean initialized;

    private TestBitmapHelper() {
    }

    @SuppressWarnings("unchecked")
    public static synchronized void initStubImages() {
        if (initialized) {
            return;
        }
        try {
            Constructor<Bitmap> ctor = Bitmap.class.getDeclaredConstructor();
            ctor.setAccessible(true);
            Bitmap stub = ctor.newInstance();

            for (Field field : ImageManager.class.getFields()) {
                if (field.getType() == Bitmap.class && field.get(null) == null) {
                    field.set(null, stub);
                }
            }

            Field mapField = ImageManager.class.getDeclaredField("CLASSNAME_IMAGE_MAP");
            mapField.setAccessible(true);
            Map<String, Bitmap> classMap = (Map<String, Bitmap>) mapField.get(null);
            Class<?>[] spriteClasses = {
                    HeroAircraft.class,
                    MobEnemy.class,
                    EliteEnemy.class,
                    ElitePlusEnemy.class,
                    EliteProEnemy.class,
                    BossEnemy.class,
                    HeroBullet.class,
                    EnemyBullet.class,
                    BloodSupply.class,
                    FireSupply.class,
                    FirePlusSupply.class,
                    BombSupply.class,
                    FreezeSupply.class
            };
            for (Class<?> spriteClass : spriteClasses) {
                classMap.putIfAbsent(spriteClass.getName(), stub);
            }
            initialized = true;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to initialize stub bitmaps for unit tests", e);
        }
    }
}
