package edu.hitsz.aircraft;

import edu.hitsz.application.ImageManager;
import edu.hitsz.application.Main;
import edu.hitsz.strategy.CircleShootStrategy;
import edu.hitsz.strategy.ScatterShootStrategy;
import edu.hitsz.strategy.StraightShootStrategy;

/**
 * 英雄飞机，游戏玩家操控
 */
public class HeroAircraft extends AbstractAircraft {

    private static HeroAircraft instance;

    private int fireVersion = 0;

    private HeroAircraft(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
        setStraightShoot();
    }

    public static synchronized HeroAircraft getInstance() {
        if (instance == null) {
            instance = new HeroAircraft(
                    Main.WINDOW_WIDTH / 2,
                    Main.WINDOW_HEIGHT - ImageManager.HERO_IMAGE.getHeight(),
                    0,
                    0,
                    250
            );
        }
        return instance;
    }

    public synchronized void resetState() {
        fireVersion++;
        setLocation(
                Main.WINDOW_WIDTH / 2,
                Main.WINDOW_HEIGHT - ImageManager.HERO_IMAGE.getHeight()
        );
        setSpeed(0, 0);
        this.hp = this.maxHp;
        resetValid();
        setStraightShoot();
    }

    public synchronized void setStraightShoot() {
        setShootNum(1);
        setPower(50);
        setDirection(-1);
        setShootStrategy(new StraightShootStrategy());
    }

    public synchronized int activateScatterShoot() {
        fireVersion++;
        setShootNum(4);
        setPower(50);
        setDirection(-1);
        setShootStrategy(new ScatterShootStrategy());
        return fireVersion;
    }

    public synchronized int activateCircleShoot() {
        fireVersion++;
        setShootNum(15);
        setPower(50);
        setDirection(-1);
        setShootStrategy(new CircleShootStrategy());
        return fireVersion;
    }

    public synchronized void recoverStraightShoot(int version) {
        if (version == fireVersion) {
            setStraightShoot();
            System.out.println("火力道具结束：英雄机恢复直射状态");
        }
    }

    @Override
    public void forward() {
        // 英雄机由触屏控制，不通过forward函数移动
    }
}
