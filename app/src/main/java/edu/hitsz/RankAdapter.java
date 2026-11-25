package edu.hitsz;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.text.NumberFormat;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

import edu.hitsz.score.ScoreRecord;

final class RankAdapter extends BaseAdapter {
    private final LayoutInflater inflater;
    private final Consumer<ScoreRecord> deleteAction;
    private final java.text.DateFormat dateFormat;
    private final java.text.DateFormat timeFormat;
    private final NumberFormat numberFormat;
    private List<ScoreRecord> records = List.of();

    RankAdapter(Context context, Consumer<ScoreRecord> deleteAction) {
        inflater = LayoutInflater.from(context);
        this.deleteAction = deleteAction;
        dateFormat = DateFormat.getDateFormat(context);
        timeFormat = DateFormat.getTimeFormat(context);
        numberFormat = NumberFormat.getIntegerInstance();
        numberFormat.setGroupingUsed(false);
    }

    void replace(List<ScoreRecord> records) {
        this.records = records;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return records.size();
    }

    @Override
    public ScoreRecord getItem(int position) {
        return records.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).id();
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_rank, parent, false);
            convertView.setTag(new ViewHolder(convertView));
        }
        ViewHolder row = (ViewHolder) convertView.getTag();
        Context context = parent.getContext();
        ScoreRecord record = getItem(position);
        int rank = position + 1;
        Date playedAt = new Date(record.playedAt());
        row.place.setText(numberFormat.format(rank));
        row.name.setText(record.playerName());
        row.points.setText(numberFormat.format(record.score()));
        row.time.setText(context.getString(R.string.ranking_date_time,
                dateFormat.format(playedAt), timeFormat.format(playedAt)));
        convertView.setBackgroundColor(context.getColor(
                position == 0 ? R.color.rank_top_background : R.color.rank_background));
        row.place.setTextColor(context.getColor(
                position == 0 ? R.color.rank_top_text : R.color.rank_text));
        row.delete.setContentDescription(context.getString(
                R.string.ranking_delete_label, rank, record.playerName()));
        row.delete.setOnClickListener(view -> deleteAction.accept(record));
        return convertView;
    }

    private static final class ViewHolder {
        final TextView place;
        final TextView name;
        final TextView points;
        final TextView time;
        final Button delete;

        ViewHolder(View view) {
            place = view.findViewById(R.id.rank_place);
            name = view.findViewById(R.id.rank_name);
            points = view.findViewById(R.id.rank_points);
            time = view.findViewById(R.id.rank_time);
            delete = view.findViewById(R.id.rank_delete);
        }
    }
}
