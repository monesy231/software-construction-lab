package edu.hitsz;

import android.app.AlertDialog;
import android.view.View;
import android.widget.EditText;

final class ScoreEntry {
    final EditText name;
    final EditText score;

    ScoreEntry(AlertDialog dialog, boolean showScore) {
        name = dialog.findViewById(R.id.score_entry_name);
        score = dialog.findViewById(R.id.score_entry_score);
        score.setVisibility(showScore ? View.VISIBLE : View.GONE);
    }

    String readName() {
        String playerName = name.getText().toString().trim();
        if (playerName.codePoints().allMatch(
                character -> Character.isWhitespace(character) || Character.isSpaceChar(character))) {
            name.requestFocus();
            name.setError(name.getContext().getString(R.string.ranking_name_required));
            return null;
        }
        return playerName;
    }

    Integer readScore() {
        try {
            int points = Integer.parseInt(score.getText().toString());
            if (points >= 0) {
                return points;
            }
        } catch (NumberFormatException ignored) {
            // Empty, non-numeric and out-of-range scores receive the same field error.
        }
        score.requestFocus();
        score.setError(score.getContext().getString(R.string.ranking_score_invalid));
        return null;
    }
}
