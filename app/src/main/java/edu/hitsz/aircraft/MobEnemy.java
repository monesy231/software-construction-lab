package edu.hitsz.aircraft;

import edu.hitsz.strategy.NoShootStrategy;

/**
 * 普通敌机：不可射击、不掉落道具。
 */
public class MobEnemy extends AbstractEnemy {

    public MobEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
        setShootStrategy(new NoShootStrategy());
    }

    @Override
    public int getScore() {
        return 15;
    }

    @Override
    public void onFreeze() {
        freezePermanently();
        System.out.println("普通敌机被冰冻：永久静止");
    }
}
