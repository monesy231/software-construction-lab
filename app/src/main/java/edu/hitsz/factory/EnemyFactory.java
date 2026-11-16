package edu.hitsz.factory;
import edu.hitsz.aircraft.AbstractEnemy;
import edu.hitsz.application.Main;

import java.util.concurrent.ThreadLocalRandom;

public abstract class EnemyFactory {

    protected final ThreadLocalRandom random = ThreadLocalRandom.current();

    protected int randomLocationX(int objectWidth) {
        return random.nextInt(objectWidth / 2, Main.WINDOW_WIDTH - objectWidth / 2);
    }

    protected int randomLocationY(int objectHeight) {
        return random.nextInt(objectHeight / 2, Math.max(objectHeight / 2 + 1, (int) (Main.WINDOW_HEIGHT * 0.06)));
    }

    protected int randomSideSpeed() {
        return random.nextBoolean() ? 3 : -3;
    }

    public abstract AbstractEnemy createEnemy();
}