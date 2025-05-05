package ba.sum.fsre.hackaton.user.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ba.sum.fsre.hackaton.R;

public class LessonCardAdapter extends RecyclerView.Adapter<LessonCardAdapter.LessonCardViewHolder> {

    private final Context context;
    private final List<LessonCard> lessonCards;
    private final OnLessonClickListener listener;

    public interface OnLessonClickListener {
        void onLessonClick(String lessonTitle);
    }

    public LessonCardAdapter(Context context, List<LessonCard> lessonCards, OnLessonClickListener listener) {
        this.context = context;
        this.lessonCards = lessonCards;
        this.listener = listener;
    }

    @NonNull
    @Override
    public LessonCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_lesson_card, parent, false);
        return new LessonCardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LessonCardViewHolder holder, int position) {
        LessonCard card = lessonCards.get(position);
        holder.icon.setImageResource(card.getIconResId());
        holder.title.setText(card.getTitle());
        holder.description.setText(card.getDescription());
        holder.status.setText(card.getStatus());
        holder.button.setText(card.getButtonText());

        // Set button click listener
        holder.button.setOnClickListener(v -> listener.onLessonClick(card.getTitle()));
    }

    @Override
    public int getItemCount() {
        return lessonCards.size();
    }

    public static class LessonCardViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView title, description, status;
        Button button;

        public LessonCardViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.cardIcon);
            title = itemView.findViewById(R.id.cardTitle);
            description = itemView.findViewById(R.id.cardDescription);
            status = itemView.findViewById(R.id.cardStatus);
            button = itemView.findViewById(R.id.cardButton);
        }
    }
}