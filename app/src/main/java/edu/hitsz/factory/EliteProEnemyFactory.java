package edu.hitsz.factory;
import edu.hitsz.aircraft.AbstractEnemy;
import edu.hitsz.aircraft.EliteProEnemy;
import edu.hitsz.application.ImageManager;

public class EliteProEnemyFactory extends EnemyFactory {

    private static final int ELITE_PRO_ENEMY_HP = 85;
    private static final int ELITE_PRO_ENEMY_SPEED_Y = 5;

    @Override
    public AbstractEnemy createEnemy() {
        return new EliteProEnemy(
                randomLocationX(ImageManager.ELITE_PRO_ENEMY_IMAGE.getWidth()),
                randomLocationY(ImageManager.ELITE_PRO_ENEMY_IMAGE.getHeight()),
                randomSideSpeed(),
                ELITE_PRO_ENEMY_SPEED_Y,
                ELITE_PRO_ENEMY_HP
        );
    }
}
