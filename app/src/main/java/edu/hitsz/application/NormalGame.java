package edu.hitsz.application;

import android.content.Context;

import java.util.function.IntConsumer;

import edu.hitsz.music.MusicManager;

/**
 * 模板模式具体类：普通模式。
 * 随时间提升敌机产生频率、敌机速度和敌机血量；Boss 血量保持固定。
 */
public class NormalGame extends Game {

    public NormalGame(Context context, MusicManager musicManager, IntConsumer onGameOver) {
        super(context, GameDifficulty.NORMAL, musicManager, onGameOver);
    }

    @Override
    protected void initDifficultyConfig() {
        enemyMaxNumber = 6;
        enemySpawnCycle = 28;
        heroShootCycle = 20;
        enemyShootCycle = 22;
        bossScoreThreshold = 900;
        nextBossScoreThreshold = bossScoreThreshold;
        bossEnemyHp = 300;

        mobProbability = 0.38;
        eliteProbability = 0.66;
        elitePlusProbability = 0.88;

        System.out.println("普通模式初始化：Boss 固定血量=" + bossEnemyHp + "。");
    }

    @Override
    protected void increaseDifficultyIfNeeded() {
        if (!isDifficultyUpdateTime()) {
            return;
        }

        if (enemySpawnCycle > 12) {
            enemySpawnCycle -= 2;
        }
        mobEnemySpeedY += 1;
        eliteEnemySpeedY += 1;
        elitePlusEnemySpeedY += 1;
        eliteProEnemySpeedY += 1;
        mobEnemyHp += 2;
        eliteEnemyHp += 3;
        elitePlusEnemyHp += 4;
        eliteProEnemyHp += 5;
        speedUpCurrentEnemies(1);

        System.out.println("普通模式难度递增：时间=" + getGameTimeSeconds()
                + "s，敌机产生周期=" + enemySpawnCycle
                + "，敌机速度提升，敌机血量提升。");
    }

    @Override
    protected boolean canCreateBoss() {
        return true;
    }

    @Override
    protected void afterBossCreated() {
        System.out.println("普通模式：本次 Boss 血量保持为 " + bossEnemyHp + "。");
    }
}
