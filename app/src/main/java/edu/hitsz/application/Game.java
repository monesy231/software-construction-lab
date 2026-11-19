package edu.hitsz.application;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import edu.hitsz.aircraft.AbstractEnemy;
import edu.hitsz.aircraft.BossEnemy;
import edu.hitsz.aircraft.EliteEnemy;
import edu.hitsz.aircraft.ElitePlusEnemy;
import edu.hitsz.aircraft.EliteProEnemy;
import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.aircraft.MobEnemy;
import edu.hitsz.basic.AbstractFlyingObject;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.EnemyBullet;
import edu.hitsz.music.MusicManager;
import edu.hitsz.observer.BombPropSubject;
import edu.hitsz.observer.FreezePropSubject;
import edu.hitsz.observer.PropSubject;
import edu.hitsz.supply.AbstractSupply;
import edu.hitsz.supply.BloodSupply;
import edu.hitsz.supply.BombSupply;
import edu.hitsz.supply.FirePlusSupply;
import edu.hitsz.supply.FireSupply;
import edu.hitsz.supply.FreezeSupply;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.IntConsumer;

/**
 * 模板模式中的抽象类角色：定义游戏主流程 action() 的算法骨架。
 * EasyGame、NormalGame、HardGame 只重写难度差异步骤。
 */
public abstract class Game extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    private static final String TAG = "AircraftWar";
    private static final double ELITE_PLUS_SUPPLY_PROBABILITY = 0.75;
    private static final double ELITE_PRO_SUPPLY_PROBABILITY = 0.95;
    private static final int SUPPLY_SPEED_Y = 6;

    protected int mobEnemyHp = 25;
    protected int mobEnemySpeedY = 7;
    protected int eliteEnemyHp = 45;
    protected int eliteEnemySpeedY = 7;
    protected int elitePlusEnemyHp = 65;
    protected int elitePlusEnemySpeedY = 5;
    protected int eliteProEnemyHp = 85;
    protected int eliteProEnemySpeedY = 5;
    protected int bossEnemyHp = 280;

    protected int enemyMaxNumber = 5;
    protected int bossScoreThreshold = 1200;
    protected int nextBossScoreThreshold = bossScoreThreshold;

    protected double enemySpawnCycle = 30;
    protected double heroShootCycle = 22;
    protected double enemyShootCycle = 22;

    protected double mobProbability = 0.38;
    protected double eliteProbability = 0.64;
    protected double elitePlusProbability = 0.85;

    private int backGroundTop = 0;
    protected final int timeInterval = 40;

    protected final GameDifficulty difficulty;
    protected final HeroAircraft heroAircraft;
    protected final List<AbstractEnemy> enemyAircrafts;
    protected final List<BaseBullet> heroBullets;
    protected final List<BaseBullet> enemyBullets;
    protected final List<AbstractSupply> supplies;
    private final MusicManager musicManager;
    private final IntConsumer onGameOver;

    protected int score = 0;
    private int enemySpawnCounter = 0;
    private int heroShootCounter = 0;
    private int enemyShootCounter = 0;
    private int gameTimeTick = 0;
    private boolean gameOverHandled = false;
    private boolean backgroundMusicStarted = false;
    private boolean started = false;
    private boolean heroInitialized = false;
    private int lastWidth = 0;
    private int lastHeight = 0;

    private final SurfaceHolder mSurfaceHolder;
    private final Paint mPaint = new Paint();
    private volatile boolean mbLoop = false;
    private boolean surfaceReady = false;
    private boolean activityResumed = false;
    private Thread gameThread;

    public Game(Context context, GameDifficulty difficulty,
                MusicManager musicManager, IntConsumer onGameOver) {
        super(context);
        this.difficulty = difficulty;
        this.musicManager = musicManager;
        this.onGameOver = onGameOver;
        ImageManager.initImage(context, difficulty);

        heroAircraft = HeroAircraft.getInstance();
        heroAircraft.resetState();

        enemyAircrafts = new LinkedList<>();
        heroBullets = new LinkedList<>();
        enemyBullets = new LinkedList<>();
        supplies = new LinkedList<>();

        new HeroController(this, heroAircraft);
        mSurfaceHolder = this.getHolder();
        mSurfaceHolder.addCallback(this);
        this.setFocusable(true);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        ImageManager.initImage(getContext(), difficulty);
        if (!started) {
            initDifficultyConfig();
            started = true;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        stopLoop();
        int oldWidth = lastWidth;
        int oldHeight = lastHeight;
        Main.WINDOW_WIDTH = width;
        Main.WINDOW_HEIGHT = height;
        lastWidth = width;
        lastHeight = height;

        ImageManager.setBackgroundImage(getContext(), difficulty);
        ImageManager.scaleBackground(width, height);

        int halfW = heroAircraft.getWidth() / 2;
        int halfH = heroAircraft.getHeight() / 2;
        if (!heroInitialized) {
            heroAircraft.resetState();
            int clampedX = Math.max(halfW, Math.min(Math.max(halfW, width - halfW), heroAircraft.getLocationX()));
            int clampedY = Math.max(halfH, Math.min(Math.max(halfH, height - halfH), heroAircraft.getLocationY()));
            heroAircraft.setLocation(clampedX, clampedY);
            heroInitialized = true;
        } else if (oldWidth > 0 && oldHeight > 0 && (oldWidth != width || oldHeight != height)) {
            int scaledX = Math.round(heroAircraft.getLocationX() * (float) width / oldWidth);
            int scaledY = Math.round(heroAircraft.getLocationY() * (float) height / oldHeight);
            int clampedX = Math.max(halfW, Math.min(Math.max(halfW, width - halfW), scaledX));
            int clampedY = Math.max(halfH, Math.min(Math.max(halfH, height - halfH), scaledY));
            heroAircraft.setLocation(clampedX, clampedY);
        }
        Log.i(TAG, "surfaceChanged " + oldWidth + "x" + oldHeight + " -> " + width + "x" + height
                + ", hero=(" + heroAircraft.getLocationX() + "," + heroAircraft.getLocationY() + ")"
                + ", score=" + score + ", hp=" + heroAircraft.getHp());
        backGroundTop = backGroundTop % Math.max(1, Main.WINDOW_HEIGHT);
        surfaceReady = true;
        startLoopIfReady();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        surfaceReady = false;
        stopLoop();
    }

    public void resumeGame() {
        activityResumed = true;
        startLoopIfReady();
    }

    public void pauseGame() {
        activityResumed = false;
        stopLoop();
    }

    private void startLoopIfReady() {
        if (surfaceReady && activityResumed && gameThread == null) {
            if (!backgroundMusicStarted) {
                musicManager.loop(MusicManager.BGM, MusicManager.BGM_PATH);
                backgroundMusicStarted = true;
            }
            musicManager.resume();
            mbLoop = true;
            gameThread = new Thread(this, "GameThread");
            Log.i(TAG, "GameThread started");
            gameThread.start();
        }
    }

    private void stopLoop() {
        mbLoop = false;
        if (gameThread != null) {
            gameThread.interrupt();
            try {
                gameThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            gameThread = null;
            Log.i(TAG, "GameThread stopped");
        }
        musicManager.pause();
    }

    @Override
    public void run() {
        while (mbLoop) {
            long startTime = System.currentTimeMillis();
            if (!gameOverHandled) {
                gameTimeTick++;
                spawnEnemyAction();
                shootAction();
                bulletsMoveAction();
                aircraftsMoveAction();
                suppliesMoveAction();
                crashCheckAction();
                increaseDifficultyIfNeeded();
                postProcessAction();
                checkResultAction();
            }
            draw();
            if (gameOverHandled) {
                mbLoop = false;
                break;
            }
            long costTime = System.currentTimeMillis() - startTime;
            long sleepTime = Math.max(5, timeInterval - costTime);
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    protected abstract void initDifficultyConfig();

    protected abstract void increaseDifficultyIfNeeded();

    protected abstract boolean canCreateBoss();

    protected abstract void afterBossCreated();

    protected void spawnEnemyAction() {
        enemySpawnCounter++;
        if (enemySpawnCounter < enemySpawnCycle) {
            return;
        }
        enemySpawnCounter = 0;

        if (canCreateBoss() && score >= nextBossScoreThreshold && !hasBossEnemy()) {
            BossEnemy bossEnemy = createBossEnemy();
            enemyAircrafts.add(bossEnemy);
            System.out.println(difficulty.getDisplayName() + "模式：Boss 出场，血量=" + bossEnemy.getHp());
            musicManager.loop(MusicManager.BOSS_BGM, MusicManager.BOSS_BGM_PATH);
            nextBossScoreThreshold += bossScoreThreshold;
            afterBossCreated();
            return;
        }

        if (enemyAircrafts.size() >= enemyMaxNumber) {
            return;
        }
        enemyAircrafts.add(createCommonEnemyAircraft());
    }

    protected void shootAction() {
        heroShootCounter++;
        enemyShootCounter++;

        if (heroShootCounter >= heroShootCycle) {
            heroShootCounter = 0;
            heroBullets.addAll(heroAircraft.shoot());
        }

        if (enemyShootCounter >= enemyShootCycle) {
            enemyShootCounter = 0;
            for (AbstractEnemy enemyAircraft : enemyAircrafts) {
                if (!enemyAircraft.notValid()) {
                    enemyBullets.addAll(enemyAircraft.shoot());
                }
            }
        }
    }

    protected void bulletsMoveAction() {
        for (BaseBullet bullet : heroBullets) {
            bullet.forward();
        }
        for (BaseBullet bullet : enemyBullets) {
            bullet.forward();
        }
    }

    protected void aircraftsMoveAction() {
        for (AbstractEnemy enemyAircraft : enemyAircrafts) {
            enemyAircraft.forward();
        }
    }

    protected void suppliesMoveAction() {
        for (AbstractSupply supply : supplies) {
            supply.forward();
        }
    }

    /**
     * 碰撞检测：敌机子弹、英雄子弹、敌机撞击、道具拾取。
     */
    protected void crashCheckAction() {
        for (BaseBullet bullet : enemyBullets) {
            if (bullet.notValid()) {
                continue;
            }
            if (heroAircraft.crash(bullet)) {
                musicManager.playOnce(MusicManager.BULLET_HIT_PATH);
                heroAircraft.decreaseHp(bullet.getPower());
                bullet.vanish();
            }
        }

        for (BaseBullet bullet : heroBullets) {
            if (bullet.notValid()) {
                continue;
            }
            for (AbstractEnemy enemyAircraft : enemyAircrafts) {
                if (enemyAircraft.notValid()) {
                    continue;
                }
                if (enemyAircraft.crash(bullet)) {
                    musicManager.playOnce(MusicManager.BULLET_HIT_PATH);
                    enemyAircraft.decreaseHp(bullet.getPower());
                    bullet.vanish();
                    if (enemyAircraft.notValid()) {
                        score += enemyAircraft.getScore();
                        supplies.addAll(createSupplies(enemyAircraft));
                    }
                    break;
                }
            }
        }

        for (AbstractEnemy enemyAircraft : enemyAircrafts) {
            if (enemyAircraft.notValid()) {
                continue;
            }
            if (enemyAircraft.crash(heroAircraft) || heroAircraft.crash(enemyAircraft)) {
                enemyAircraft.vanish();
                heroAircraft.decreaseHp(Integer.MAX_VALUE);
            }
        }

        for (AbstractSupply supply : supplies) {
            if (supply.notValid()) {
                continue;
            }
            if (heroAircraft.crash(supply)) {
                supply.activate(heroAircraft);
                if (supply instanceof BombSupply) {
                    score += triggerBombProp();
                    musicManager.playOnce(MusicManager.BOMB_EXPLOSION_PATH);
                } else {
                    musicManager.playOnce(MusicManager.GET_SUPPLY_PATH);
                    if (supply instanceof FreezeSupply) {
                        triggerFreezeProp();
                    }
                }
                supply.vanish();
            }
        }
    }

    private int triggerBombProp() {
        PropSubject bombSubject = new BombPropSubject();
        for (AbstractEnemy enemyAircraft : enemyAircrafts) {
            if (!enemyAircraft.notValid()) {
                bombSubject.addObserver(enemyAircraft);
            }
        }
        for (BaseBullet bullet : enemyBullets) {
            if (!bullet.notValid() && bullet instanceof EnemyBullet) {
                bombSubject.addObserver((EnemyBullet) bullet);
            }
        }
        return bombSubject.notifyObservers();
    }

    private void triggerFreezeProp() {
        PropSubject freezeSubject = new FreezePropSubject();
        for (AbstractEnemy enemyAircraft : enemyAircrafts) {
            if (!enemyAircraft.notValid()) {
                freezeSubject.addObserver(enemyAircraft);
            }
        }
        for (BaseBullet bullet : enemyBullets) {
            if (!bullet.notValid() && bullet instanceof EnemyBullet) {
                freezeSubject.addObserver((EnemyBullet) bullet);
            }
        }
        freezeSubject.notifyObservers();
    }

    /**
     * 后处理：删除无效子弹、敌机、道具。
     */
    protected void postProcessAction() {
        boolean bossInvalid = false;
        for (AbstractEnemy enemyAircraft : enemyAircrafts) {
            if (enemyAircraft instanceof BossEnemy && enemyAircraft.notValid()) {
                bossInvalid = true;
                break;
            }
        }
        enemyBullets.removeIf(AbstractFlyingObject::notValid);
        heroBullets.removeIf(AbstractFlyingObject::notValid);
        enemyAircrafts.removeIf(AbstractFlyingObject::notValid);
        supplies.removeIf(AbstractFlyingObject::notValid);
        if (bossInvalid && heroAircraft.getHp() > 0) {
            musicManager.stop(MusicManager.BOSS_BGM);
        }
    }

    protected void checkResultAction() {
        if (heroAircraft.getHp() <= 0 && !gameOverHandled) {
            gameOverHandled = true;
            mbLoop = false;
            musicManager.stopAllLoopMusic();
            musicManager.playOnce(MusicManager.GAME_OVER_PATH);
            Log.i(TAG, "Game Over! Score = " + score);
            onGameOver.accept(score);
        }
    }

    protected void draw() {
        Canvas canvas = mSurfaceHolder.lockCanvas();
        if (canvas == null) {
            return;
        }
        try {
            Bitmap bg = ImageManager.BACKGROUND_IMAGE;
            if (bg != null) {
                canvas.drawBitmap(bg, 0, this.backGroundTop - Main.WINDOW_HEIGHT, null);
                canvas.drawBitmap(bg, 0, this.backGroundTop, null);
                if (!gameOverHandled) {
                    this.backGroundTop += 2;
                    if (this.backGroundTop >= Main.WINDOW_HEIGHT) {
                        this.backGroundTop = 0;
                    }
                }
            }

            paintImageWithPositionRevised(canvas, enemyBullets);
            paintImageWithPositionRevised(canvas, heroBullets);
            paintImageWithPositionRevised(canvas, enemyAircrafts);
            paintImageWithPositionRevised(canvas, supplies);

            if (ImageManager.HERO_IMAGE != null) {
                canvas.drawBitmap(
                        ImageManager.HERO_IMAGE,
                        heroAircraft.getLocationX() - ImageManager.HERO_IMAGE.getWidth() / 2f,
                        heroAircraft.getLocationY() - ImageManager.HERO_IMAGE.getHeight() / 2f,
                        null
                );
            }

            paintScoreAndLife(canvas);

            if (gameOverHandled) {
                paintGameOver(canvas);
            }
        } finally {
            mSurfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    private void paintImageWithPositionRevised(Canvas g, List<? extends AbstractFlyingObject> objects) {
        if (objects.isEmpty()) {
            return;
        }

        for (AbstractFlyingObject object : objects) {
            Bitmap image = object.getImage();
            if (image == null) {
                continue;
            }
            g.drawBitmap(
                    image,
                    object.getLocationX() - image.getWidth() / 2f,
                    object.getLocationY() - image.getHeight() / 2f,
                    null
            );
        }
    }

    private void paintScoreAndLife(Canvas g) {
        float textSize = Math.max(36f, Main.WINDOW_WIDTH * 0.05f);
        float x = 24f;
        float y = Math.max(96f, Main.WINDOW_HEIGHT * 0.06f);
        mPaint.setColor(Color.RED);
        mPaint.setTypeface(Typeface.DEFAULT_BOLD);
        mPaint.setTextSize(textSize);
        mPaint.setTextAlign(Paint.Align.LEFT);
        g.drawText("MODE: " + difficulty.getDisplayName(), x, y, mPaint);
        y += textSize * 1.25f;
        g.drawText("SCORE: " + this.score, x, y, mPaint);
        y += textSize * 1.25f;
        g.drawText("LIFE: " + this.heroAircraft.getHp(), x, y, mPaint);
    }

    private void paintGameOver(Canvas g) {
        mPaint.setColor(Color.argb(180, 0, 0, 0));
        g.drawRect(0, Main.WINDOW_HEIGHT * 0.4f, Main.WINDOW_WIDTH, Main.WINDOW_HEIGHT * 0.6f, mPaint);
        mPaint.setColor(Color.WHITE);
        mPaint.setTypeface(Typeface.DEFAULT_BOLD);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setTextSize(Math.max(48f, Main.WINDOW_WIDTH * 0.08f));
        g.drawText("GAME OVER", Main.WINDOW_WIDTH / 2f, Main.WINDOW_HEIGHT * 0.48f, mPaint);
        mPaint.setTextSize(Math.max(36f, Main.WINDOW_WIDTH * 0.055f));
        g.drawText("SCORE: " + this.score, Main.WINDOW_WIDTH / 2f, Main.WINDOW_HEIGHT * 0.55f, mPaint);
        mPaint.setTextAlign(Paint.Align.LEFT);
    }

    protected AbstractEnemy createCommonEnemyAircraft() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        double probability = random.nextDouble();
        if (probability < mobProbability) {
            return new MobEnemy(
                    randomLocationX(ImageManager.MOB_ENEMY_IMAGE.getWidth()),
                    randomLocationY(ImageManager.MOB_ENEMY_IMAGE.getHeight()),
                    0,
                    mobEnemySpeedY,
                    mobEnemyHp
            );
        }
        if (probability < eliteProbability) {
            return new EliteEnemy(
                    randomLocationX(ImageManager.ELITE_ENEMY_IMAGE.getWidth()),
                    randomLocationY(ImageManager.ELITE_ENEMY_IMAGE.getHeight()),
                    0,
                    eliteEnemySpeedY,
                    eliteEnemyHp
            );
        }
        if (probability < elitePlusProbability) {
            return new ElitePlusEnemy(
                    randomLocationX(ImageManager.ELITE_PLUS_ENEMY_IMAGE.getWidth()),
                    randomLocationY(ImageManager.ELITE_PLUS_ENEMY_IMAGE.getHeight()),
                    randomSideSpeed(),
                    elitePlusEnemySpeedY,
                    elitePlusEnemyHp
            );
        }
        return new EliteProEnemy(
                randomLocationX(ImageManager.ELITE_PRO_ENEMY_IMAGE.getWidth()),
                randomLocationY(ImageManager.ELITE_PRO_ENEMY_IMAGE.getHeight()),
                randomSideSpeed(),
                eliteProEnemySpeedY,
                eliteProEnemyHp
        );
    }

    protected BossEnemy createBossEnemy() {
        return new BossEnemy(
                Main.WINDOW_WIDTH / 2,
                ImageManager.BOSS_ENEMY_IMAGE.getHeight(),
                randomSideSpeed(),
                0,
                bossEnemyHp
        );
    }

    protected boolean hasBossEnemy() {
        for (AbstractEnemy enemyAircraft : enemyAircrafts) {
            if (!enemyAircraft.notValid() && enemyAircraft instanceof BossEnemy) {
                return true;
            }
        }
        return false;
    }

    protected boolean isDifficultyUpdateTime() {
        int updatePeriodTicks = 10_000 / timeInterval;
        return gameTimeTick > 0 && gameTimeTick % updatePeriodTicks == 0;
    }

    protected void speedUpCurrentEnemies(int deltaY) {
        for (AbstractEnemy enemyAircraft : enemyAircrafts) {
            if (!(enemyAircraft instanceof BossEnemy) && !enemyAircraft.notValid()) {
                int speedY = enemyAircraft.getBaseSpeedY();
                if (speedY > 0) {
                    enemyAircraft.setSpeedY(speedY + deltaY);
                }
            }
        }
    }

    protected int getGameTimeSeconds() {
        return gameTimeTick * timeInterval / 1000;
    }

    private List<AbstractSupply> createSupplies(AbstractEnemy enemyAircraft) {
        List<AbstractSupply> droppedSupplies = new ArrayList<>();
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int locationX = enemyAircraft.getLocationX();
        int locationY = enemyAircraft.getLocationY();

        if (enemyAircraft instanceof ElitePlusEnemy) {
            if (random.nextDouble() < ELITE_PLUS_SUPPLY_PROBABILITY) {
                droppedSupplies.add(createRandomElitePlusSupply(locationX, locationY));
            }
            return droppedSupplies;
        }

        if (enemyAircraft instanceof EliteProEnemy) {
            if (random.nextDouble() < ELITE_PRO_SUPPLY_PROBABILITY) {
                droppedSupplies.add(createRandomEliteProSupply(locationX, locationY));
            }
            return droppedSupplies;
        }

        if (enemyAircraft instanceof BossEnemy) {
            for (int i = 0; i < 3; i++) {
                droppedSupplies.add(createRandomEliteProSupply(locationX, locationY));
            }
        }
        return droppedSupplies;
    }

    private int randomLocationX(int objectWidth) {
        int minX = objectWidth / 2;
        int maxX = Math.max(minX + 1, Main.WINDOW_WIDTH - objectWidth / 2);
        return ThreadLocalRandom.current().nextInt(minX, maxX);
    }

    private int randomLocationY(int objectHeight) {
        return ThreadLocalRandom.current().nextInt(
                objectHeight / 2,
                Math.max(objectHeight / 2 + 1, (int) (Main.WINDOW_HEIGHT * 0.06))
        );
    }

    protected int randomSideSpeed() {
        return ThreadLocalRandom.current().nextBoolean() ? 3 : -3;
    }

    private AbstractSupply createRandomElitePlusSupply(int locationX, int locationY) {
        int supplyType = ThreadLocalRandom.current().nextInt(4);
        if (supplyType == 0) {
            return new BloodSupply(locationX, locationY, 0, SUPPLY_SPEED_Y);
        }
        if (supplyType == 1) {
            return new FireSupply(locationX, locationY, 0, SUPPLY_SPEED_Y);
        }
        if (supplyType == 2) {
            return new FirePlusSupply(locationX, locationY, 0, SUPPLY_SPEED_Y);
        }
        return new BombSupply(locationX, locationY, 0, SUPPLY_SPEED_Y);
    }

    private AbstractSupply createRandomEliteProSupply(int locationX, int locationY) {
        int supplyType = ThreadLocalRandom.current().nextInt(5);
        if (supplyType == 0) {
            return new BloodSupply(locationX, locationY, 0, SUPPLY_SPEED_Y);
        }
        if (supplyType == 1) {
            return new FireSupply(locationX, locationY, 0, SUPPLY_SPEED_Y);
        }
        if (supplyType == 2) {
            return new FirePlusSupply(locationX, locationY, 0, SUPPLY_SPEED_Y);
        }
        if (supplyType == 3) {
            return new BombSupply(locationX, locationY, 0, SUPPLY_SPEED_Y);
        }
        return new FreezeSupply(locationX, locationY, 0, SUPPLY_SPEED_Y);
    }
}
