package edu.hitsz.aircraft;

import edu.hitsz.strategy.DoubleStraightShootStrategy;

/**
 * 精英敌机：可向下直射双排子弹。
 */
public class EliteEnemy extends AbstractEnemy {

    public EliteEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
        setShootNum(2);
        setPower(10);
        setDirection(1);
        setShootStrategy(new DoubleStraightShootStrategy());
    }

    @Override
    public int getScore() {
        return 15;
    }

    @Override
    public void onFreeze() {
        freezeForSeconds(4);
        System.out.println("精英敌机被冰冻：静止 4 秒后恢复");
    }
}
