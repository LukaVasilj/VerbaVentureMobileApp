package ba.sum.fsre.hackaton.user.home;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ba.sum.fsre.hackaton.R;

public class LessonActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private String appLanguage;
    private String learningLanguage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson);

        appLanguage = getIntent().getStringExtra("appLanguage");
        learningLanguage = getIntent().getStringExtra("learningLanguage");

        sharedPreferences = getSharedPreferences("LessonProgress", MODE_PRIVATE);

        RecyclerView recyclerView = findViewById(R.id.lessonRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<LessonCard> lessonCards = new ArrayList<>();
        lessonCards.add(new LessonCard(
                R.drawable.ic_animals,
                getString(R.string.lesson_animals_title),
                getString(R.string.lesson_animals_description),
                getProgress(getString(R.string.lesson_animals_title), 5),
                getString(R.string.start_button_text)
        ));
        lessonCards.add(new LessonCard(
                R.drawable.ic_colors,
                getString(R.string.lesson_colors_title),
                getString(R.string.lesson_colors_description),
                getProgress(getString(R.string.lesson_colors_title), 5),
                getString(R.string.start_button_text)
        ));
        lessonCards.add(new LessonCard(
                R.drawable.ic_family,
                getString(R.string.lesson_family_title),
                getString(R.string.lesson_family_description),
                getProgress(getString(R.string.lesson_family_title), 5),
                getString(R.string.start_button_text)
        ));

        LessonCardAdapter adapter = new LessonCardAdapter(this, lessonCards, (lessonTitle) -> {
            Intent intent = new Intent(LessonActivity.this, QuizActivity.class);
            intent.putExtra("appLanguage", appLanguage);
            intent.putExtra("learningLanguage", learningLanguage);
            intent.putExtra("lessonTitle", lessonTitle);
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        RecyclerView recyclerView = findViewById(R.id.lessonRecyclerView);
        List<LessonCard> lessonCards = new ArrayList<>();
        lessonCards.add(new LessonCard(
                R.drawable.ic_animals,
                getString(R.string.lesson_animals_title),
                getString(R.string.lesson_animals_description),
                getProgress(getString(R.string.lesson_animals_title), 5),
                getString(R.string.start_button_text)
        ));
        lessonCards.add(new LessonCard(
                R.drawable.ic_colors,
                getString(R.string.lesson_colors_title),
                getString(R.string.lesson_colors_description),
                getProgress(getString(R.string.lesson_colors_title), 5),
                getString(R.string.start_button_text)
        ));
        lessonCards.add(new LessonCard(
                R.drawable.ic_family,
                getString(R.string.lesson_family_title),
                getString(R.string.lesson_family_description),
                getProgress(getString(R.string.lesson_family_title), 5),
                getString(R.string.start_button_text)
        ));

        LessonCardAdapter adapter = new LessonCardAdapter(this, lessonCards, (lessonTitle) -> {
            Intent intent = new Intent(LessonActivity.this, FlashcardActivity.class);
            intent.putExtra("appLanguage", appLanguage);
            intent.putExtra("learningLanguage", learningLanguage);
            intent.putExtra("lessonTitle", lessonTitle);
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);
    }

    private String getLessonTitleInLearningLanguage(String localizedTitle) {
        if (learningLanguage.equals("en")) {
            if (localizedTitle.equals(getString(R.string.lesson_animals_title))) return "Animals";
            if (localizedTitle.equals(getString(R.string.lesson_colors_title))) return "Colors";
            if (localizedTitle.equals(getString(R.string.lesson_family_title))) return "Family";
        } else if (learningLanguage.equals("hr")) {
            if (localizedTitle.equals(getString(R.string.lesson_animals_title))) return "Životinje";
            if (localizedTitle.equals(getString(R.string.lesson_colors_title))) return "Boje";
            if (localizedTitle.equals(getString(R.string.lesson_family_title))) return "Obitelj";
        } else if (learningLanguage.equals("es")) {
            if (localizedTitle.equals(getString(R.string.lesson_animals_title))) return "Animales";
            if (localizedTitle.equals(getString(R.string.lesson_colors_title))) return "Colores";
            if (localizedTitle.equals(getString(R.string.lesson_family_title))) return "Familia";
        }
        return localizedTitle; // Default to the localized title if no mapping is found
    }

    private String getProgress(String lessonTitle, int totalWords) {
        // Map the localized title to the learning language title
        String learningLanguageTitle = getLessonTitleInLearningLanguage(lessonTitle);
        String progressKey = "progress_" + learningLanguage + "_" + learningLanguageTitle.toLowerCase();
        int learnedWords = sharedPreferences.getInt(progressKey, 0);
        Log.d("LessonActivity", "Retrieved progress for " + learningLanguageTitle + " in " + learningLanguage + ": " + learnedWords + "/" + totalWords);
        return getString(R.string.progress_format, learnedWords, totalWords);
    }

    private void updateProgress(String lessonTitle, int learnedWords) {
        // Map the localized title to the learning language title
        String learningLanguageTitle = getLessonTitleInLearningLanguage(lessonTitle);
        String progressKey = "progress_" + learningLanguage + "_" + learningLanguageTitle.toLowerCase();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(progressKey, learnedWords);
        editor.apply(); // Save changes
        Log.d("LessonActivity", "Updated progress for " + learningLanguageTitle + " in " + learningLanguage + ": " + learnedWords);
    }
}