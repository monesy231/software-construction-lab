package edu.hitsz.application;

import android.view.MotionEvent;
import android.view.View;

import edu.hitsz.aircraft.HeroAircraft;

/**
 * 英雄机控制类
 * 监听触屏，控制英雄机的移动
 */
public class HeroController {
    private Game game;
    private HeroAircraft heroAircraft;

    public HeroController(Game game, HeroAircraft heroAircraft) {
        this.game = game;
        this.heroAircraft = heroAircraft;

        game.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                int action = motionEvent.getAction();
                if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
                    int x = (int) motionEvent.getX();
                    int y = (int) motionEvent.getY();
                    int halfW = heroAircraft.getWidth() / 2;
                    int halfH = heroAircraft.getHeight() / 2;
                    int clampedX = Math.max(halfW, Math.min(Math.max(halfW, Main.WINDOW_WIDTH - halfW), x));
                    int clampedY = Math.max(halfH, Math.min(Math.max(halfH, Main.WINDOW_HEIGHT - halfH), y));
                    heroAircraft.setLocation(clampedX, clampedY);
                    return true;
                }
                if (action == MotionEvent.ACTION_UP) {
                    view.performClick();
                    return true;
                }
                return false;
            }
        });
    }
}
