package edu.hitsz.application;

import android.content.Context;

/**
 * 模板模式具体类：困难模式。
 * 随时间提升敌机产生频率、敌机速度、敌机血量、英雄机射击频率和敌机射击频率；
 * 每次召唤 Boss 后提高下一次 Boss 血量。
 */
public class HardGame extends Game {

    public HardGame(Context context) {
        super(context, GameDifficulty.HARD);
    }

    @Override
    protected void initDifficultyConfig() {
        enemyMaxNumber = 7;
        enemySpawnCycle = 24;
        heroShootCycle = 22;
        enemyShootCycle = 18;
        bossScoreThreshold = 700;
        nextBossScoreThreshold = bossScoreThreshold;
        bossEnemyHp = 360;

        mobProbability = 0.28;
        eliteProbability = 0.56;
        elitePlusProbability = 0.80;

        System.out.println("困难模式初始化：Boss 初始血量=" + bossEnemyHp + "，难度会快速递增。");
    }

    @Override
    protected void increaseDifficultyIfNeeded() {
        if (!isDifficultyUpdateTime()) {
            return;
        }

        if (enemySpawnCycle > 8) {
            enemySpawnCycle -= 2;
        }
        if (heroShootCycle > 10) {
            heroShootCycle -= 1;
        }
        if (enemyShootCycle > 8) {
            enemyShootCycle -= 1;
        }
        mobEnemySpeedY += 1;
        eliteEnemySpeedY += 1;
        elitePlusEnemySpeedY += 1;
        eliteProEnemySpeedY += 1;
        mobEnemyHp += 3;
        eliteEnemyHp += 5;
        elitePlusEnemyHp += 6;
        eliteProEnemyHp += 8;
        speedUpCurrentEnemies(1);

        System.out.println("困难模式难度递增：时间=" + getGameTimeSeconds()
                + "s，敌机产生周期=" + enemySpawnCycle
                + "，英雄机射击周期=" + heroShootCycle
                + "，敌机射击周期=" + enemyShootCycle
                + "，敌机速度提升，敌机血量提升。");
    }

    @Override
    protected boolean canCreateBoss() {
        return true;
    }

    @Override
    protected void afterBossCreated() {
        bossEnemyHp += 100;
        System.out.println("困难模式：下一次 Boss 血量提升为 " + bossEnemyHp + "。");
    }
}
