package edu.hitsz.aircraft;

import edu.hitsz.basic.AbstractFlyingObject;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.strategy.NoShootStrategy;
import edu.hitsz.strategy.ShootStrategy;

import java.util.List;

/**
 * 所有种类飞机的抽象父类
 */
public abstract class AbstractAircraft extends AbstractFlyingObject {

    protected int maxHp;
    protected int hp;
    protected int shootNum = 1;
    protected int power = 0;
    protected int direction = 1;
    protected ShootStrategy shootStrategy = new NoShootStrategy();

    public AbstractAircraft(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY);
        this.hp = hp;
        this.maxHp = hp;
    }

    public void decreaseHp(int decrease) {
        hp = hp - decrease;
        if (hp <= 0) {
            vanish();
            hp = 0;
        }
    }

    public void increaseHp(int amount) {
        if (amount <= 0) {
            return;
        }
        hp = hp + amount;
        if (hp > maxHp) {
            hp = maxHp;
        }
    }

    public int getHp() {
        return hp;
    }

    public int getShootNum() {
        return shootNum;
    }

    public void setShootNum(int shootNum) {
        if (shootNum > 0) {
            this.shootNum = shootNum;
        }
    }

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        if (power >= 0) {
            this.power = power;
        }
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public int getSpeedX() {
        return speedX;
    }

    public ShootStrategy getShootStrategy() {
        return shootStrategy;
    }

    public void setShootStrategy(ShootStrategy shootStrategy) {
        if (shootStrategy != null) {
            this.shootStrategy = shootStrategy;
        }
    }

    public List<BaseBullet> shoot() {
        return shootStrategy.shoot(this);
    }
}