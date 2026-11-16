package edu.hitsz.factory;
import edu.hitsz.aircraft.AbstractEnemy;
import edu.hitsz.aircraft.ElitePlusEnemy;
import edu.hitsz.application.ImageManager;

public class ElitePlusEnemyFactory extends EnemyFactory {

    private static final int ELITE_PLUS_ENEMY_HP = 65;
    private static final int ELITE_PLUS_ENEMY_SPEED_Y = 5;

    @Override
    public AbstractEnemy createEnemy() {
        return new ElitePlusEnemy(
                randomLocationX(ImageManager.ELITE_PLUS_ENEMY_IMAGE.getWidth()),
                randomLocationY(ImageManager.ELITE_PLUS_ENEMY_IMAGE.getHeight()),
                randomSideSpeed(),
                ELITE_PLUS_ENEMY_SPEED_Y,
                ELITE_PLUS_ENEMY_HP
        );
    }
}
