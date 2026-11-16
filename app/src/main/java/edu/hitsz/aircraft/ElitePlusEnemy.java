package edu.hitsz.aircraft;

import edu.hitsz.strategy.DoubleStraightShootStrategy;

/**
 * 精锐敌机。
 */
public class ElitePlusEnemy extends AbstractEnemy {

    public ElitePlusEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
        setShootNum(2);
        setPower(13);
        setDirection(1);
        setShootStrategy(new DoubleStraightShootStrategy());
    }

    @Override
    public int getScore() {
        return 35;
    }

    @Override
    public void onFreeze() {
        freezeForSeconds(3);
        System.out.println("精锐敌机被冰冻：静止 3 秒后恢复");
    }
}
