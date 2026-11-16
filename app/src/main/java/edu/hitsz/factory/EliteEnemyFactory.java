package edu.hitsz.factory;
import edu.hitsz.aircraft.AbstractEnemy;
import edu.hitsz.aircraft.EliteEnemy;
import edu.hitsz.application.ImageManager;

public class EliteEnemyFactory extends EnemyFactory {

    private static final int ELITE_ENEMY_HP = 45;
    private static final int ELITE_ENEMY_SPEED_Y = 7;

    @Override
    public AbstractEnemy createEnemy() {
        return new EliteEnemy(
                randomLocationX(ImageManager.ELITE_ENEMY_IMAGE.getWidth()),
                randomLocationY(ImageManager.ELITE_ENEMY_IMAGE.getHeight()),
                0,
                ELITE_ENEMY_SPEED_Y,
                ELITE_ENEMY_HP
        );
    }
}
