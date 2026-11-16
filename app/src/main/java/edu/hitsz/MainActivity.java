package edu.hitsz;

import android.app.Activity;
import android.os.Bundle;

import edu.hitsz.application.EasyGame;
import edu.hitsz.application.Game;

public class MainActivity extends Activity {
    private Game game;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        game = new EasyGame(this);
        // 默认使用简单模式
        setContentView(game);
    }

    @Override
    protected void onResume() {
        super.onResume();
        game.resumeGame();
    }

    @Override
    protected void onPause() {
        game.pauseGame();
        super.onPause();
    }
}
