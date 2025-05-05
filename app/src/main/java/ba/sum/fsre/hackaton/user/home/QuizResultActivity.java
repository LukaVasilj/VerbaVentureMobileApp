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

        resultTextView.setText("You scored " + score + " out of " + totalQuestions + "!");

        // Save progress to SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("LessonProgress", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String progressKey = "progress_" + lessonTitle;

        // Retrieve the previously saved score
        int previousScore = sharedPreferences.getInt(progressKey, 0);

        // Update the score only if the current score is higher
        if (score > previousScore) {
            editor.putInt(progressKey, score);
            editor.apply();
            Log.d(TAG, "Updated progress for " + lessonTitle + ": " + score + "/" + totalQuestions);
        } else {
            Log.d(TAG, "Progress for " + lessonTitle + " remains: " + previousScore + "/" + totalQuestions);
        }

        // Set up the button to navigate back to LessonActivity
        goToStartButton.setOnClickListener(v -> {
            Intent intent = new Intent(QuizResultActivity.this, LessonActivity.class);
            startActivity(intent);
            finish();
        });
    }
}