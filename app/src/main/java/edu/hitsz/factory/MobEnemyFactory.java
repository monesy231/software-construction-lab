package edu.hitsz.factory;
import edu.hitsz.aircraft.AbstractEnemy;
import edu.hitsz.aircraft.MobEnemy;
import edu.hitsz.application.ImageManager;

public class MobEnemyFactory extends EnemyFactory {

    private static final int MOB_ENEMY_HP = 25;
    private static final int MOB_ENEMY_SPEED_Y = 7;

    @Override
    public AbstractEnemy createEnemy() {
        return new MobEnemy(
                randomLocationX(ImageManager.MOB_ENEMY_IMAGE.getWidth()),
                randomLocationY(ImageManager.MOB_ENEMY_IMAGE.getHeight()),
                0,
                MOB_ENEMY_SPEED_Y,
                MOB_ENEMY_HP
        );
    }
}
