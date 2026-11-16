package edu.hitsz.bullet;

import edu.hitsz.observer.PropObserver;

/**
 * 敌机子弹，同时作为炸弹、冰冻道具的观察者。
 */
public class EnemyBullet extends BaseBullet implements PropObserver {

    public EnemyBullet(int locationX, int locationY, int speedX, int speedY, int power) {
        super(locationX, locationY, speedX, speedY, power);
    }

    @Override
    public int onBomb() {
        vanish();
        System.out.println("敌机子弹被炸弹清除");
        return 0;
    }

    @Override
    public void onFreeze() {
        freezeForSeconds(5);
        System.out.println("敌机子弹被冰冻：静止 5 秒后恢复");
    }
}
