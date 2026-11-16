package edu.hitsz.supply;

import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.thread.FirePowerRunnable;

/**
 * 超级火力道具：切换为环形弹道，持续指定时长后自动恢复直射。
 */
public class FirePlusSupply extends AbstractSupply {

    private static final long EFFECT_DURATION_MILLIS = 8_000;

    public FirePlusSupply(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
    }

    @Override
    public void activate(HeroAircraft heroAircraft) {
        new Thread(
                new FirePowerRunnable(heroAircraft, FirePowerRunnable.FirePowerMode.FIRE_PLUS, EFFECT_DURATION_MILLIS),
                "FirePlusSupplyThread"
        ).start();
        System.out.println("FirePlusSupply active!");
    }
}
