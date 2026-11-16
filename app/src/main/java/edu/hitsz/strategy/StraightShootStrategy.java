package edu.hitsz.strategy;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.bullet.BaseBullet;

import java.util.LinkedList;
import java.util.List;

public class StraightShootStrategy implements ShootStrategy {

    @Override
    public List<BaseBullet> shoot(AbstractAircraft aircraft) {
        List<BaseBullet> bullets = new LinkedList<>();
        int shootNum = Math.max(1, aircraft.getShootNum());
        int x = aircraft.getLocationX();
        int y = aircraft.getLocationY() + aircraft.getDirection() * 2;
        int speedY = aircraft.getSpeedY() + aircraft.getDirection() * 6;

        for (int i = 0; i < shootNum; i++) {
            bullets.add(createBullet(
                    aircraft,
                    x + (i * 2 - shootNum + 1) * 11,
                    y,
                    0,
                    speedY
            ));
        }
        return bullets;
    }
}
