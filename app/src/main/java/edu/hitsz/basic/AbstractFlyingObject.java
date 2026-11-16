package edu.hitsz.basic;

import android.graphics.Bitmap;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.application.ImageManager;
import edu.hitsz.application.Main;

/**
 * 可飞行对象的基类
 * @author hitsz
 */
public abstract class AbstractFlyingObject {

    //locationX,locationY为图片中心位置坐标
    protected int locationX;
    protected int locationY;

    // x,y轴移动速度
    protected int speedX;
    protected int speedY;

    // 临时冻结/减速效果记录原始速度、当前倍率与令牌版本，避免连续触发把0存为原速
    private int baseSpeedX;
    private int baseSpeedY;
    private boolean speedAffected = false;
    private double effectRate = 1.0;
    private int effectVersion = 0;

    //图片, null 表示未设置
    protected Bitmap image = null;

    //x 轴长度，根据图片尺寸获得, -1 表示未设置
    protected int width = -1;

    //y 轴长度，根据图片尺寸获得, -1 表示未设置
    protected int height = -1;

    //有效（生存）标记，标记为 false的对象会在下次刷新时清除
    protected boolean isValid = true;

    public AbstractFlyingObject() {
    }

    public AbstractFlyingObject(int locationX, int locationY, int speedX, int speedY) {
        this.locationX = locationX;
        this.locationY = locationY;
        this.speedX = speedX;
        this.speedY = speedY;
    }

    /**
     * 可飞行对象根据速度移动
     * 若飞行对象触碰到横向边界，横向速度反向
     */
    public synchronized void forward() {
        locationX += speedX;
        locationY += speedY;
        if (speedX != 0 && (locationX <= 0 || locationX >= Main.WINDOW_WIDTH)) {
            // 横向超出边界后反向
            speedX = -speedX;
            if (speedAffected) {
                baseSpeedX = -baseSpeedX;
            }
        }
    }

    /**
     * 碰撞检测，当对方坐标进入我方范围，判定我方击中<br>
     * 对方与我方覆盖区域有交叉即判定撞击。
     *  <br>
     * 非飞机对象区域：
     *  横向，[x - width/2, x + width/2]
     *  纵向，[y - height/2, y + height/2]
     *  <br>
     * 飞机对象区域：
     *  横向，[x - width/2, x + width/2]
     *  纵向，[y - height/4, y + height/4]
     *
     * @param flyingObject 撞击对方
     * @return true: 我方被击中; false 我方未被击中
     */
    public boolean crash(AbstractFlyingObject flyingObject) {
        // 缩放因子，用于控制 y轴方向区域范围
        int factor = this instanceof AbstractAircraft ? 2 : 1; //我方
        int fFactor = flyingObject instanceof AbstractAircraft ? 2 : 1;//对方

        //对方坐标、宽度、高度
        int x = flyingObject.getLocationX();
        int y = flyingObject.getLocationY();
        int fWidth = flyingObject.getWidth();
        int fHeight = flyingObject.getHeight();

        return x + (fWidth+this.getWidth())/2 > locationX
                && x - (fWidth+this.getWidth())/2 < locationX
                && y + ( fHeight/fFactor+this.getHeight()/factor )/2 > locationY
                && y - ( fHeight/fFactor+this.getHeight()/factor )/2 < locationY;
    }

    public int getLocationX() {
        return locationX;
    }

    public int getLocationY() {
        return locationY;
    }

    public void setLocation(double locationX, double locationY){
        this.locationX = (int) locationX;
        this.locationY = (int) locationY;
    }

    public synchronized int getSpeedY() {
        return speedY;
    }

    public synchronized int getBaseSpeedY() {
        return speedAffected ? baseSpeedY : speedY;
    }

    public synchronized void setSpeedX(int speedX) {
        if (speedAffected) {
            this.baseSpeedX = speedX;
            this.speedX = (int) Math.round(baseSpeedX * effectRate);
        } else {
            this.speedX = speedX;
        }
    }

    public synchronized void setSpeedY(int speedY) {
        if (speedAffected) {
            this.baseSpeedY = speedY;
            this.speedY = (int) Math.round(baseSpeedY * effectRate);
        } else {
            this.speedY = speedY;
        }
    }

    public synchronized void setSpeed(int speedX, int speedY) {
        setSpeedX(speedX);
        setSpeedY(speedY);
    }

    public synchronized void freezePermanently() {
        effectVersion++;
        speedAffected = false;
        effectRate = 1.0;
        this.speedX = 0;
        this.speedY = 0;
    }

    public void freezeForSeconds(int seconds) {
        applySpeedEffect(0.0, seconds, "FreezeRecover-" + getClass().getSimpleName());
    }

    public void slowForSeconds(double rate, int seconds) {
        applySpeedEffect(rate, seconds, "SlowRecover-" + getClass().getSimpleName());
    }

    private void applySpeedEffect(double rate, int seconds, String threadName) {
        final int version;
        synchronized (this) {
            if (!speedAffected) {
                baseSpeedX = this.speedX;
                baseSpeedY = this.speedY;
                speedAffected = true;
            }
            effectVersion++;
            version = effectVersion;
            effectRate = rate;
            this.speedX = (int) Math.round(baseSpeedX * rate);
            this.speedY = (int) Math.round(baseSpeedY * rate);
        }
        new Thread(() -> {
            try {
                Thread.sleep(seconds * 1000L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            recoverSpeed(version);
        }, threadName).start();
    }

    private synchronized void recoverSpeed(int version) {
        if (version == effectVersion && speedAffected && !notValid()) {
            this.speedX = baseSpeedX;
            this.speedY = baseSpeedY;
            speedAffected = false;
            effectRate = 1.0;
        }
    }

    public void resetValid() {
        this.isValid = true;
    }

    public Bitmap getImage() {
        if (image == null){
            image = ImageManager.get(this);
        }
        return image;
    }

    public int getWidth() {
        if (width == -1){
            // 若未设置，则查询图片宽度并设置
            width = ImageManager.get(this).getWidth();
        }
        return width;
    }

    public int getHeight() {
        if (height == -1){
            // 若未设置，则查询图片高度并设置
            height = ImageManager.get(this).getHeight();
        }
        return height;
    }


    /**
     * 标记消失
     * isValid = false.
     * notValid() => true.
     */
    public void vanish() {
        isValid = false;
    }

    public boolean notValid() {
        return !this.isValid;
    }

}
