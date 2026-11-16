package edu.hitsz.aircraft;

import edu.hitsz.application.Main;
import edu.hitsz.observer.PropObserver;

/**
 * 所有敌机的抽象父类，同时作为炸弹、冰冻道具的观察者。
 */
public abstract class AbstractEnemy extends AbstractAircraft implements PropObserver {

    public AbstractEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
    }

    @Override
    public void forward() {
        super.forward();
        if (locationY >= Main.WINDOW_HEIGHT) {
            vanish();
        }
    }

    public int getScore() {
        return 15;
    }

    @Override
    public int onBomb() {
        vanish();
        return getScore();
    }

    @Override
    public void onFreeze() {
        freezeForSeconds(4);
    }
}
