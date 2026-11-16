package edu.hitsz.strategy;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.strategy.ShootStrategy;

import java.util.LinkedList;
import java.util.List;

public class CircleShootStrategy implements ShootStrategy {

    @Override
    public List<BaseBullet> shoot(AbstractAircraft aircraft) {
        List<BaseBullet> bullets = new LinkedList<>();
        int shootNum = Math.max(1, aircraft.getShootNum());
        int bulletSpeed = aircraft instanceof HeroAircraft ? 8 : 6;

        for (int i = 0; i < shootNum; i++) {
            double angle = 2 * Math.PI * i / shootNum;
            int speedX = (int) Math.round(bulletSpeed * Math.cos(angle));
            int speedY = (int) Math.round(bulletSpeed * Math.sin(angle));
            bullets.add(createBullet(
                    aircraft,
                    aircraft.getLocationX(),
                    aircraft.getLocationY(),
                    speedX,
                    speedY
            ));
        }
        return bullets;
    }
}
