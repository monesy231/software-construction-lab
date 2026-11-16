package edu.hitsz.aircraft;

import edu.hitsz.strategy.ScatterShootStrategy;

/**
 * 王牌敌机。
 */
public class EliteProEnemy extends AbstractEnemy {

    private static final int BOMB_DAMAGE = 40;

    public EliteProEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
        setShootNum(3);
        setPower(20);
        setDirection(1);
        setShootStrategy(new ScatterShootStrategy());
    }

    @Override
    public int getScore() {
        return 45;
    }

    @Override
    public int onBomb() {
        decreaseHp(BOMB_DAMAGE);
        System.out.println("王牌敌机被炸弹击中：掉血 " + BOMB_DAMAGE);
        return notValid() ? getScore() : 0;
    }

    @Override
    public void onFreeze() {
        slowForSeconds(0.5, 5);
        System.out.println("王牌敌机被冰冻：减速 5 秒后恢复");
    }
}
