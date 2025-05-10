package ba.sum.fsre.hackaton.user.home;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import ba.sum.fsre.hackaton.R;

public class QuizResultActivity extends AppCompatActivity {

    private static final String TAG = "QuizResultActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_result);

        TextView resultTextView = findViewById(R.id.resultTextView);
        Button goToStartButton = findViewById(R.id.goToStartButton);

        int score = getIntent().getIntExtra("score", 0);
        int totalQuestions = getIntent().getIntExtra("totalQuestions", 0);
        String lessonTitle = getIntent().getStringExtra("lessonTitle");
        String learningLanguage = getIntent().getStringExtra("learningLanguage");

        if (lessonTitle == null || lessonTitle.isEmpty() || learningLanguage == null || learningLanguage.isEmpty()) {
            Log.e(TAG, "Lesson title or learning language is null or empty!");
            return;
        }

        // Map the lesson title to the learning language
        String learningLanguageTitle = getLessonTitleInLearningLanguage(lessonTitle, learningLanguage);

        // Display the result
        resultTextView.setText(getString(R.string.quiz_result_text, score, totalQuestions));

        // Save progress to SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("LessonProgress", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String progressKey = "progress_" + learningLanguage + "_" + learningLanguageTitle.toLowerCase();

        // Retrieve the previously saved score
        int previousScore = sharedPreferences.getInt(progressKey, 0);

        // Update the score only if the current score is higher
        if (score > previousScore) {
            editor.putInt(progressKey, score);
            editor.apply();
            Log.d(TAG, "Updated progress for " + learningLanguageTitle + " in " + learningLanguage + ": " + score + "/" + totalQuestions);
        } else {
            Log.d(TAG, "Progress for " + learningLanguageTitle + " in " + learningLanguage + " remains: " + previousScore + "/" + totalQuestions);
        }

        // Set up the button to navigate back to LessonActivity
        goToStartButton.setOnClickListener(v -> {
            Intent intent = new Intent(QuizResultActivity.this, LessonActivity.class);
            intent.putExtra("appLanguage", getIntent().getStringExtra("appLanguage"));
            intent.putExtra("learningLanguage", learningLanguage);
            startActivity(intent);
            finish();
        });
    }

    private String getLessonTitleInLearningLanguage(String localizedTitle, String learningLanguage) {
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
}