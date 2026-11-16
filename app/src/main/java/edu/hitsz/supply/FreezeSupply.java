package edu.hitsz.supply;

import edu.hitsz.aircraft.HeroAircraft;

/**
 * 冰冻道具：具体通知逻辑在 Game 中通过观察者模式触发。
 */
public class FreezeSupply extends AbstractSupply {

    public FreezeSupply(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
    }

    @Override
    public void activate(HeroAircraft heroAircraft) {
        System.out.println("FreezeSupply active!");
    }
}
