package edu.hitsz.basic;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import edu.hitsz.TestBitmapHelper;
import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.aircraft.BossEnemy;
import edu.hitsz.aircraft.EliteEnemy;
import edu.hitsz.aircraft.ElitePlusEnemy;
import edu.hitsz.aircraft.EliteProEnemy;
import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.aircraft.MobEnemy;
import edu.hitsz.application.Main;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.EnemyBullet;
import edu.hitsz.bullet.HeroBullet;
import edu.hitsz.observer.BombPropSubject;
import edu.hitsz.observer.FreezePropSubject;
import edu.hitsz.observer.PropSubject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * 覆盖碰撞判定、速度冻结/减速恢复、观察者道具与射击策略的核心逻辑测试。
 */
public class GameCoreLogicTest {

    @Before
    public void setUp() {
        TestBitmapHelper.initStubImages();
        Main.WINDOW_WIDTH = 512;
        Main.WINDOW_HEIGHT = 768;
    }

    @Test
    public void crashShouldUseHalfHeightForAircraftAndFullHeightForBullet() {
        AbstractAircraft plane = new MobEnemy(200, 200, 0, 0, 30) {
            @Override
            public int getWidth() {
                return 80;
            }

            @Override
            public int getHeight() {
                return 80;
            }
        };
        BaseBullet grazingWingtip = new HeroBullet(200, 165, 0, -6, 10) {
            @Override
            public int getWidth() {
                return 10;
            }

            @Override
            public int getHeight() {
                return 10;
            }
        };
        BaseBullet directCenterHit = new HeroBullet(200, 180, 0, -6, 10) {
            @Override
            public int getWidth() {
                return 10;
            }

            @Override
            public int getHeight() {
                return 10;
            }
        };

        // 飞机纵向判定区间为 height / 4 = 20，子弹为 5，合计数 25；|200 - 165| = 35 -> 未命中
        assertFalse(plane.crash(grazingWingtip));
        // |200 - 180| = 20 < 25 -> 命中
        assertTrue(plane.crash(directCenterHit));
    }

    @Test
    public void slowEffectAndDifficultySpeedUpShouldRecoverCorrectly() throws InterruptedException {
        EliteProEnemy enemy = new EliteProEnemy(200, 100, 3, 5, 85);
        enemy.slowForSeconds(0.5, 1);

        assertEquals(5, enemy.getBaseSpeedY());
        assertEquals(3, enemy.getSpeedY());

        enemy.setSpeedY(enemy.getBaseSpeedY() + 1);
        assertEquals(6, enemy.getBaseSpeedY());
        assertEquals(3, enemy.getSpeedY());

        Thread.sleep(1150L);
        assertEquals(6, enemy.getSpeedY());
    }

    @Test
    public void freezeAtBoundaryAndConsecutiveFreezeShouldPreserveOriginalDirection() throws InterruptedException {
        EliteEnemy enemy = new EliteEnemy(3, 100, -3, 4, 45);
        enemy.forward();
        // 到达左边界 0 后反向为 +3
        assertEquals(0, enemy.getLocationX());
        assertEquals(3, enemy.getSpeedX());

        enemy.freezeForSeconds(1);
        assertEquals(0, enemy.getSpeedX());
        assertEquals(0, enemy.getSpeedY());

        // 冻结期间在边界原地刷新多帧，不应翻转 baseSpeedX
        for (int i = 0; i < 5; i++) {
            enemy.forward();
        }
        assertEquals(0, enemy.getLocationX());

        // 连续再次冻结，刷新版本号，旧定时器到期不得提前恢复
        Thread.sleep(300L);
        enemy.freezeForSeconds(1);

        Thread.sleep(800L);
        assertEquals(0, enemy.getSpeedX());

        Thread.sleep(400L);
        assertEquals(3, enemy.getSpeedX());
        assertEquals(4, enemy.getSpeedY());

        enemy.forward();
        assertEquals(3, enemy.getLocationX());
    }

    @Test
    public void bombAndFreezeObserversShouldApplyPerEnemyRules() {
        MobEnemy mob = new MobEnemy(100, 100, 0, 5, 25);
        EliteEnemy elite = new EliteEnemy(150, 100, 0, 5, 45);
        ElitePlusEnemy elitePlus = new ElitePlusEnemy(200, 100, 3, 5, 65);
        EliteProEnemy survivingPro = new EliteProEnemy(250, 100, 3, 5, 85);
        EliteProEnemy fatalPro = new EliteProEnemy(300, 100, 3, 5, 30);
        BossEnemy boss = new BossEnemy(256, 80, 3, 0, 300);
        EnemyBullet bullet = new EnemyBullet(120, 200, 0, 6, 10);

        PropSubject bombSubject = new BombPropSubject();
        bombSubject.addObserver(mob);
        bombSubject.addObserver(elite);
        bombSubject.addObserver(elitePlus);
        bombSubject.addObserver(survivingPro);
        bombSubject.addObserver(fatalPro);
        bombSubject.addObserver(boss);
        bombSubject.addObserver(bullet);

        int gained = bombSubject.notifyObservers();

        assertEquals(15 + 15 + 35 + 45, gained);
        assertTrue(mob.notValid());
        assertTrue(elite.notValid());
        assertTrue(elitePlus.notValid());
        assertFalse(survivingPro.notValid());
        assertEquals(45, survivingPro.getHp());
        assertTrue(fatalPro.notValid());
        assertFalse(boss.notValid());
        assertEquals(300, boss.getHp());
        assertTrue(bullet.notValid());

        MobEnemy frozenMob = new MobEnemy(100, 100, 0, 5, 25);
        BossEnemy immuneBoss = new BossEnemy(256, 80, 3, 0, 300);
        EnemyBullet frozenBullet = new EnemyBullet(120, 200, 0, 6, 10);
        PropSubject freezeSubject = new FreezePropSubject();
        freezeSubject.addObserver(frozenMob);
        freezeSubject.addObserver(immuneBoss);
        freezeSubject.addObserver(frozenBullet);

        assertEquals(0, freezeSubject.notifyObservers());
        assertEquals(0, frozenMob.getSpeedY());
        assertEquals(3, immuneBoss.getSpeedX());
        assertEquals(0, frozenBullet.getSpeedY());
    }

    @Test
    public void shootStrategiesShouldProduceExpectedBullets() {
        HeroAircraft hero = HeroAircraft.getInstance();
        hero.resetState();

        List<BaseBullet> straight = hero.shoot();
        assertEquals(1, straight.size());
        assertTrue(straight.get(0).getSpeedY() < 0);
        assertEquals(50, straight.get(0).getPower());

        hero.activateScatterShoot();
        List<BaseBullet> scatter = hero.shoot();
        assertEquals(4, scatter.size());

        hero.activateCircleShoot();
        List<BaseBullet> circle = hero.shoot();
        assertEquals(15, circle.size());

        MobEnemy mob = new MobEnemy(100, 100, 0, 5, 25);
        assertTrue(mob.shoot().isEmpty());

        EliteEnemy elite = new EliteEnemy(150, 100, 0, 5, 45);
        List<BaseBullet> eliteBullets = elite.shoot();
        assertEquals(2, eliteBullets.size());
        assertEquals(10, eliteBullets.get(0).getPower());

        BossEnemy boss = new BossEnemy(256, 80, 3, 0, 300);
        assertEquals(25, boss.shoot().size());
    }
}
