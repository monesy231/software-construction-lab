package edu.hitsz.thread;

import edu.hitsz.aircraft.HeroAircraft;

public class FirePowerRunnable implements Runnable {

    public enum FirePowerMode {
        FIRE,
        FIRE_PLUS
    }

    private final HeroAircraft heroAircraft;
    private final FirePowerMode mode;
    private final long durationMillis;

    public FirePowerRunnable(HeroAircraft heroAircraft, FirePowerMode mode, long durationMillis) {
        this.heroAircraft = heroAircraft;
        this.mode = mode;
        this.durationMillis = durationMillis;
    }

    @Override
    public void run() {
        int version;
        if (mode == FirePowerMode.FIRE) {
            version = heroAircraft.activateScatterShoot();
            System.out.println("火力道具生效：散射弹道，持续 " + durationMillis / 1000 + " 秒");
        } else {
            version = heroAircraft.activateCircleShoot();
            System.out.println("超级火力道具生效：环形弹道，持续 " + durationMillis / 1000 + " 秒");
        }

        try {
            Thread.sleep(durationMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        heroAircraft.recoverStraightShoot(version);
    }
}
