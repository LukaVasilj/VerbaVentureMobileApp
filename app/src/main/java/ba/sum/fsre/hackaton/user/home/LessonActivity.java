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

        // Retrieve appLanguage and learningLanguage from Intent
        appLanguage = getIntent().getStringExtra("appLanguage");
        learningLanguage = getIntent().getStringExtra("learningLanguage");

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("LessonProgress", MODE_PRIVATE);

        RecyclerView recyclerView = findViewById(R.id.lessonRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Create a list of LessonCard objects with dynamic progress
        List<LessonCard> lessonCards = new ArrayList<>();
        lessonCards.add(new LessonCard(
                R.drawable.ic_animals,
                "Životinje",
                "Nauči nazive životinja.",
                getProgress("Životinje", 5),
                "Započni"
        ));
        Log.d("LessonActivity", "Progress for Životinje: " + getProgress("Životinje", 5)); // Add this here

        lessonCards.add(new LessonCard(
                R.drawable.ic_colors,
                "Boje",
                "Nauči boje.",
                getProgress("progress_colors", 5),
                "Započni"
        ));
        lessonCards.add(new LessonCard(
                R.drawable.ic_family,
                "Obitelj i prijatelji",
                "Nauči nazive članove obitelji.",
                getProgress("progress_family", 5),
                "Započni"
        ));

        LessonCardAdapter adapter = new LessonCardAdapter(this, lessonCards, (lessonTitle) -> {
            // Navigate to FlashcardActivity with the selected lesson
            Intent intent = new Intent(LessonActivity.this, FlashcardActivity.class);
            intent.putExtra("appLanguage", appLanguage);
            intent.putExtra("learningLanguage", learningLanguage);
            intent.putExtra("lessonTitle", lessonTitle); // Pass the selected lesson
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);
    }

    // Method to get progress from SharedPreferences
    private String getProgress(String lessonTitle, int totalWords) {
        String progressKey = "progress_" + lessonTitle; // Match the key format
        int learnedWords = sharedPreferences.getInt(progressKey, 0); // Default is 0
        return learnedWords + "/" + totalWords + " riječi naučeno";
    }
}