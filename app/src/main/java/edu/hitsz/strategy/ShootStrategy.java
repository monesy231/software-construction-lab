package edu.hitsz.strategy;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.EnemyBullet;
import edu.hitsz.bullet.HeroBullet;

import java.util.List;

public interface ShootStrategy {


    List<BaseBullet> shoot(AbstractAircraft aircraft);
    default BaseBullet createBullet(AbstractAircraft aircraft, int x, int y, int speedX, int speedY) {
        if (aircraft instanceof HeroAircraft) {
            return new HeroBullet(x, y, speedX, speedY, aircraft.getPower());
        }
        return new EnemyBullet(x, y, speedX, speedY, aircraft.getPower());
    }
}
