package edu.hitsz.supply;

import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.thread.FirePowerRunnable;

/**
 * 火力道具：切换为散射弹道，持续指定时长后自动恢复直射。
 */
public class FireSupply extends AbstractSupply {

    private static final long EFFECT_DURATION_MILLIS = 8_000;

    public FireSupply(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
    }

    @Override
    public void activate(HeroAircraft heroAircraft) {
        new Thread(
                new FirePowerRunnable(heroAircraft, FirePowerRunnable.FirePowerMode.FIRE, EFFECT_DURATION_MILLIS),
                "FireSupplyThread"
        ).start();
        System.out.println("FireSupply active!");
    }
}
