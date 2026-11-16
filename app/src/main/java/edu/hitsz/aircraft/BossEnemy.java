package edu.hitsz.aircraft;

import edu.hitsz.application.Main;
import edu.hitsz.strategy.CircleShootStrategy;

/**
 * Boss 敌机。
 */
public class BossEnemy extends AbstractEnemy {

    public BossEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
        setShootNum(25);
        setPower(25);
        setDirection(1);
        setShootStrategy(new CircleShootStrategy());
    }

    @Override
    public void forward() {
        locationX += speedX;
        int halfW = getWidth() / 2;
        if (locationX <= halfW || locationX >= Main.WINDOW_WIDTH - halfW) {
            speedX = -speedX;
            locationX += speedX;
        }
    }

    @Override
    public int getScore() {
        return 120;
    }

    @Override
    public int onBomb() {
        System.out.println("Boss 敌机不受炸弹影响");
        return 0;
    }

    @Override
    public void onFreeze() {
        System.out.println("Boss 敌机不受冰冻影响");
    }
}
