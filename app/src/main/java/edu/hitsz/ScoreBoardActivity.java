package edu.hitsz;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Insets;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowInsets;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import edu.hitsz.application.GameDifficulty;
import edu.hitsz.score.ScoreRecord;

public class ScoreBoardActivity extends Activity {
    private static final String TAG = "ScoreBoardActivity";
    private GameDifficulty difficulty;
    private RankAdapter adapter;
    private TextView emptyState;
    private AlertDialog activeDialog;
    private int loadRequest;

    static Intent newIntent(Context context, GameDifficulty difficulty) {
        return new Intent(context, ScoreBoardActivity.class)
                .putExtra(GameActivity.EXTRA_DIFFICULTY, difficulty.name());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score_board);

        View page = findViewById(R.id.ranking_page);
        Insets contentPadding = Insets.of(page.getPaddingLeft(), page.getPaddingTop(),
                page.getPaddingRight(), page.getPaddingBottom());
        page.setOnApplyWindowInsetsListener((view, windowInsets) -> {
            Insets insets = windowInsets.getInsets(
                    WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
            view.setPadding(contentPadding.left + insets.left, contentPadding.top + insets.top,
                    contentPadding.right + insets.right, contentPadding.bottom + insets.bottom);
            return windowInsets;
        });

        String selected = savedInstanceState == null
                ? getIntent().getStringExtra(GameActivity.EXTRA_DIFFICULTY)
                : savedInstanceState.getString(GameActivity.EXTRA_DIFFICULTY);
        difficulty = GameDifficulty.fromName(selected);
        adapter = new RankAdapter(this, this::confirmDelete);
        emptyState = findViewById(R.id.ranking_empty_state);
        ListView list = findViewById(R.id.lv_rank);
        list.setEmptyView(emptyState);
        list.setAdapter(adapter);

        RadioGroup difficultyPicker = findViewById(R.id.ranking_difficulty);
        difficultyPicker.check(switch (difficulty) {
            case EASY -> R.id.ranking_easy;
            case NORMAL -> R.id.ranking_normal;
            case HARD -> R.id.ranking_hard;
        });
        difficultyPicker.setOnCheckedChangeListener((group, checkedId) -> {
            GameDifficulty next = GameDifficulty.EASY;
            if (checkedId == R.id.ranking_normal) {
                next = GameDifficulty.NORMAL;
            } else if (checkedId == R.id.ranking_hard) {
                next = GameDifficulty.HARD;
            }
            if (next != difficulty) {
                difficulty = next;
                list.setSelection(0);
                reload(true);
            }
        });
        findViewById(R.id.ranking_add).setOnClickListener(view -> showAddDialog());
    }

    @Override
    protected void onResume() {
        super.onResume();
        reload(false);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(GameActivity.EXTRA_DIFFICULTY, difficulty.name());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        if (activeDialog != null) {
            activeDialog.dismiss();
        }
        super.onDestroy();
    }

    private void reload(boolean clearRows) {
        int request = ++loadRequest;
        GameDifficulty selected = difficulty;
        emptyState.setText(R.string.ranking_loading);
        if (clearRows) {
            adapter.replace(List.of());
        }
        ScoreStorage.run(this, dao -> dao.findByDifficulty(selected))
                .whenCompleteAsync((records, failure) -> {
                    if (isFinishing() || isDestroyed() || request != loadRequest) {
                        return;
                    }
                    if (failure != null) {
                        Log.e(TAG, "Could not load ranking", failure);
                        emptyState.setText(R.string.ranking_load_failed);
                        adapter.replace(List.of());
                        return;
                    }
                    emptyState.setText(R.string.ranking_empty);
                    adapter.replace(records);
                }, getMainExecutor());
    }

    private void showAddDialog() {
        GameDifficulty selected = difficulty;
        activeDialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.ranking_add_title, selected.getDisplayName()))
                .setView(R.layout.dialog_score_entry)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.ranking_add_save, null)
                .show();
        AlertDialog dialog = activeDialog;
        ScoreEntry entry = new ScoreEntry(dialog, true);
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
            String name = entry.readName();
            if (name == null) {
                return;
            }
            Integer score = entry.readScore();
            if (score == null) {
                return;
            }
            long addedAt = System.currentTimeMillis();
            completeMutation(dialog, ScoreStorage.run(this, dao -> {
                dao.add(selected, name, score, addedAt);
                return null;
            }), R.string.ranking_save_failed);
        });
    }

    private void confirmDelete(ScoreRecord record) {
        GameDifficulty selected = difficulty;
        activeDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.ranking_delete_title)
                .setMessage(getString(R.string.ranking_delete_message,
                        record.playerName(), record.score()))
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.ranking_delete_title, null)
                .show();
        AlertDialog dialog = activeDialog;
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view ->
                completeMutation(dialog, ScoreStorage.run(this,
                        dao -> dao.delete(selected, record.id())), R.string.ranking_delete_failed));
    }

    private void completeMutation(AlertDialog dialog, CompletableFuture<?> operation,
                                  int failureMessage) {
        setMutationPending(dialog, true);
        operation.whenCompleteAsync((result, failure) -> {
            if (isFinishing() || isDestroyed()) {
                return;
            }
            if (failure != null) {
                Log.e(TAG, "Could not update ranking", failure);
                setMutationPending(dialog, false);
                Toast.makeText(this, failureMessage, Toast.LENGTH_LONG).show();
                return;
            }
            dialog.dismiss();
            reload(false);
        }, getMainExecutor());
    }

    private void setMutationPending(AlertDialog dialog, boolean pending) {
        dialog.setCancelable(!pending);
        dialog.setCanceledOnTouchOutside(!pending);
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(!pending);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(!pending);
    }
}
