package edu.hitsz.factory;
import edu.hitsz.aircraft.AbstractEnemy;
import edu.hitsz.aircraft.BossEnemy;
import edu.hitsz.application.ImageManager;
import edu.hitsz.application.Main;

public class BossEnemyFactory extends EnemyFactory {

    private static final int BOSS_ENEMY_HP = 280;

    @Override
    public AbstractEnemy createEnemy() {
        return new BossEnemy(
                Main.WINDOW_WIDTH / 2,
                ImageManager.BOSS_ENEMY_IMAGE.getHeight(),
                randomSideSpeed(),
                0,
                BOSS_ENEMY_HP
        );
    }
}