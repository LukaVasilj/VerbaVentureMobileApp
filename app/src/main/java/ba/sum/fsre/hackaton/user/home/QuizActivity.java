package ba.sum.fsre.hackaton.user.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ba.sum.fsre.hackaton.R;

public class QuizActivity extends AppCompatActivity {

    private ImageView questionImageView;
    private RadioGroup optionsRadioGroup;
    private Button submitButton;
    private TextView questionCounterTextView;

    private List<Flashcard> flashcards;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private String lessonTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        questionImageView = findViewById(R.id.questionImageView);
        optionsRadioGroup = findViewById(R.id.optionsRadioGroup);
        submitButton = findViewById(R.id.submitButton);
        questionCounterTextView = findViewById(R.id.questionCounterTextView);

        ProgressBar progressBar = findViewById(R.id.progressBar);
        TextView progressTextView = findViewById(R.id.progressTextView);

        flashcards = getIntent().getParcelableArrayListExtra("flashcards");
        lessonTitle = getIntent().getStringExtra("lessonTitle");



        if (flashcards == null) {
            Log.e("QuizActivity", "Flashcards list is null!");
        } else {
            Log.d("QuizActivity", "Flashcards received. Size: " + flashcards.size());
        }
        Log.d("QuizActivity", "Lesson title received: " + lessonTitle);

        // Initialize progress bar
        int initialProgress = 6; // Start from 6/10
        progressBar.setMax(10);
        updateProgress(progressBar, progressTextView, initialProgress);

        Collections.shuffle(flashcards);

        // Initially disable the submit button
        submitButton.setEnabled(false);

        // Enable the submit button when an option is selected
        optionsRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            submitButton.setEnabled(checkedId != -1);
        });

        displayQuestion();

        submitButton.setOnClickListener(v -> {
            if (submitButton.getText().toString().equals("Submit")) {
                handleSubmitAction();
            } else {
                handleNextAction(progressBar, progressTextView, initialProgress);
            }
        });
    }

    private void handleSubmitAction() {
        int selectedOptionId = optionsRadioGroup.getCheckedRadioButtonId();
        if (selectedOptionId == -1) {
            Toast.makeText(this, "Please select an answer!", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton selectedOption = findViewById(selectedOptionId);
        String selectedAnswer = selectedOption.getText().toString();
        String correctAnswer = flashcards.get(currentQuestionIndex).getTranslatedWord();

        // Highlight the correct answer
        for (int i = 0; i < optionsRadioGroup.getChildCount(); i++) {
            RadioButton option = (RadioButton) optionsRadioGroup.getChildAt(i);
            if (option.getText().toString().equals(correctAnswer)) {
                option.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light)); // Correct answer
            } else if (option.getText().toString().equals(selectedAnswer)) {
                option.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light)); // Incorrect answer
            }
        }

        // Increment score if the selected answer is correct
        if (selectedAnswer.equals(correctAnswer)) {
            score++;
        }

        // Disable further selection
        for (int i = 0; i < optionsRadioGroup.getChildCount(); i++) {
            optionsRadioGroup.getChildAt(i).setEnabled(false);
        }

        // Change the button text to "Next"
        submitButton.setText("Next");
    }

    private void handleNextAction(ProgressBar progressBar, TextView progressTextView, int initialProgress) {
        currentQuestionIndex++;
        if (currentQuestionIndex < flashcards.size()) {
            displayQuestion();
            updateProgress(progressBar, progressTextView, initialProgress + currentQuestionIndex);
        } else {
            showQuizResult();
        }
    }

    private void updateProgress(ProgressBar progressBar, TextView progressTextView, int currentProgress) {
        progressBar.setProgress(currentProgress);
        progressTextView.setText(currentProgress + "/" + progressBar.getMax());
    }

    private void displayQuestion() {
        Flashcard currentFlashcard = flashcards.get(currentQuestionIndex);

        questionCounterTextView.setText("Question " + (currentQuestionIndex + 1) + " of " + flashcards.size());
        questionImageView.setImageResource(currentFlashcard.getImageResId());

        List<String> options = new ArrayList<>();
        options.add(currentFlashcard.getTranslatedWord());
        for (Flashcard flashcard : flashcards) {
            if (!flashcard.getTranslatedWord().equals(currentFlashcard.getTranslatedWord()) && options.size() < 4) {
                options.add(flashcard.getTranslatedWord());
            }
        }
        Collections.shuffle(options);

        optionsRadioGroup.removeAllViews();
        for (String option : options) {
            RadioButton radioButton = new RadioButton(this);
            radioButton.setText(option);
            radioButton.setTextSize(18); // Increase text size
            radioButton.setTextColor(getResources().getColor(R.color.black));
            radioButton.setPadding(32, 32, 32, 32); // Add padding
            radioButton.setBackgroundResource(R.drawable.radio_button_background);
            radioButton.setButtonDrawable(android.R.color.transparent); // Hide default circle

            // Set layout parameters for larger size and spacing
            RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(
                    RadioGroup.LayoutParams.MATCH_PARENT, // Full width
                    RadioGroup.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(16, 16, 16, 16); // Add margins
            radioButton.setLayoutParams(params);

            optionsRadioGroup.addView(radioButton);
        }

        // Reset the submit button text and disable it
        submitButton.setText("Submit");
        submitButton.setEnabled(false);
    }

    private void showQuizResult() {
        Intent intent = new Intent(this, QuizResultActivity.class);
        intent.putExtra("score", score);
        intent.putExtra("totalQuestions", flashcards.size());
        intent.putExtra("lessonTitle", lessonTitle); // Pass lessonTitle
        intent.putExtra("learningLanguage", getIntent().getStringExtra("learningLanguage")); // Pass learningLanguage
        startActivity(intent);
        finish();
    }
}