package edu.hitsz;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Insets;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;
import android.widget.Switch;

import edu.hitsz.application.GameDifficulty;

public class MainActivity extends Activity {
    private static final String SETTINGS = "game_settings";
    private static final String AUDIO_ENABLED = "audio_enabled";

    private Switch audioSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View menu = findViewById(R.id.difficulty_menu);
        menu.setOnApplyWindowInsetsListener((view, windowInsets) -> {
            Insets insets = windowInsets.getInsets(
                    WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom);
            return windowInsets;
        });

        SharedPreferences settings = getSharedPreferences(SETTINGS, MODE_PRIVATE);
        audioSwitch = findViewById(R.id.audio_switch);
        audioSwitch.setChecked(settings.getBoolean(AUDIO_ENABLED, true));
        audioSwitch.setOnCheckedChangeListener((button, checked) ->
                settings.edit().putBoolean(AUDIO_ENABLED, checked).apply());

        findViewById(R.id.start_easy).setOnClickListener(view -> startGame(GameDifficulty.EASY));
        findViewById(R.id.start_normal).setOnClickListener(view -> startGame(GameDifficulty.NORMAL));
        findViewById(R.id.start_hard).setOnClickListener(view -> startGame(GameDifficulty.HARD));
    }

    private void startGame(GameDifficulty difficulty) {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra(GameActivity.EXTRA_DIFFICULTY, difficulty.name());
        intent.putExtra(GameActivity.EXTRA_AUDIO_ENABLED, audioSwitch.isChecked());
        startActivity(intent);
    }
}
