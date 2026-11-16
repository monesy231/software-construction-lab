package edu.hitsz.supply;

import edu.hitsz.aircraft.HeroAircraft;

/**
 * 加血道具
 */
public class BloodSupply extends AbstractSupply {

    private static final int RECOVER_HP = 30;

    public BloodSupply(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
    }

    @Override
    public void activate(HeroAircraft heroAircraft) {
        heroAircraft.increaseHp(RECOVER_HP);
    }
}
