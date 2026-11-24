package edu.hitsz.aircraft;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import edu.hitsz.TestBitmapHelper;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.strategy.CircleShootStrategy;
import edu.hitsz.strategy.ScatterShootStrategy;
import edu.hitsz.strategy.StraightShootStrategy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * 英雄机类单元测试（从 Windows 端实验四用例迁移至 JUnit 4）。
 */
public class HeroAircraftTest {

    private HeroAircraft heroAircraft;

    @Before
    public void setUp() {
        TestBitmapHelper.initStubImages();
        heroAircraft = HeroAircraft.getInstance();
        heroAircraft.resetState();
    }

    @Test
    public void decreaseHpShouldReduceHp() {
        int oldHp = heroAircraft.getHp();

        heroAircraft.decreaseHp(50);

        assertEquals(oldHp - 50, heroAircraft.getHp());
        assertFalse(heroAircraft.notValid());
    }

    @Test
    public void decreaseHpToZeroShouldMakeHeroInvalid() {
        heroAircraft.decreaseHp(heroAircraft.getHp());

        assertEquals(0, heroAircraft.getHp());
        assertTrue(heroAircraft.notValid());
    }

    @Test
    public void increaseHpShouldNotExceedMaxHp() {
        heroAircraft.decreaseHp(100);

        heroAircraft.increaseHp(30);
        assertEquals(180, heroAircraft.getHp());

        heroAircraft.increaseHp(1000);
        assertEquals(250, heroAircraft.getHp());
    }

    @Test
    public void forwardShouldNotMoveHero() {
        int oldX = heroAircraft.getLocationX();
        int oldY = heroAircraft.getLocationY();

        heroAircraft.forward();

        assertEquals(oldX, heroAircraft.getLocationX());
        assertEquals(oldY, heroAircraft.getLocationY());
    }

    @Test
    public void shootShouldCreateBulletInStraightMode() {
        List<BaseBullet> bullets = heroAircraft.shoot();

        assertNotNull(bullets);
        assertFalse(bullets.isEmpty());
        assertTrue(heroAircraft.getShootStrategy() instanceof StraightShootStrategy);
    }

    @Test
    public void firePowerShouldRecoverByVersion() {
        int scatterVersion = heroAircraft.activateScatterShoot();
        assertTrue(heroAircraft.getShootStrategy() instanceof ScatterShootStrategy);

        int circleVersion = heroAircraft.activateCircleShoot();
        assertTrue(heroAircraft.getShootStrategy() instanceof CircleShootStrategy);

        heroAircraft.recoverStraightShoot(scatterVersion);
        assertTrue(heroAircraft.getShootStrategy() instanceof CircleShootStrategy);

        heroAircraft.recoverStraightShoot(circleVersion);
        assertTrue(heroAircraft.getShootStrategy() instanceof StraightShootStrategy);
    }

    @Test
    public void resetStateShouldRestoreHpValidityAndInvalidateOldFireVersion() {
        int oldVersion = heroAircraft.activateCircleShoot();
        heroAircraft.decreaseHp(250);
        assertTrue(heroAircraft.notValid());

        heroAircraft.resetState();

        assertEquals(250, heroAircraft.getHp());
        assertFalse(heroAircraft.notValid());
        assertTrue(heroAircraft.getShootStrategy() instanceof StraightShootStrategy);

        int newVersion = heroAircraft.activateScatterShoot();
        heroAircraft.recoverStraightShoot(oldVersion);
        assertTrue(heroAircraft.getShootStrategy() instanceof ScatterShootStrategy);
        heroAircraft.recoverStraightShoot(newVersion);
        assertTrue(heroAircraft.getShootStrategy() instanceof StraightShootStrategy);
    }
}
