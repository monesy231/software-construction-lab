package edu.hitsz.application;

import android.content.Context;

/**
 * 模板模式具体类：简单模式。
 * 不生成 Boss，难度不随时间变化。
 */
public class EasyGame extends Game {

    public EasyGame(Context context) {
        super(context, GameDifficulty.EASY);
    }

    @Override
    protected void initDifficultyConfig() {
        enemyMaxNumber = 5;
        enemySpawnCycle = 32;
        heroShootCycle = 20;
        enemyShootCycle = 26;
        bossScoreThreshold = Integer.MAX_VALUE;
        nextBossScoreThreshold = bossScoreThreshold;

        mobProbability = 0.50;
        eliteProbability = 0.78;
        elitePlusProbability = 0.93;

        System.out.println("简单模式初始化：不生成 Boss，难度不随时间递增。");
    }

    @Override
    protected void increaseDifficultyIfNeeded() {
        // 简单模式不递增难度
    }

    @Override
    protected boolean canCreateBoss() {
        return false;
    }

    @Override
    protected void afterBossCreated() {
        // 简单模式不会进入此逻辑
    }
}
