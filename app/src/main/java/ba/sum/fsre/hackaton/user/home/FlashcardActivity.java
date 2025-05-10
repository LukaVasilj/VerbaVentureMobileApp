package ba.sum.fsre.hackaton.user.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import ba.sum.fsre.hackaton.R;

public class FlashcardActivity extends AppCompatActivity {

    private ImageView animalImageView;
    private TextView nativeWordTextView, translatedWordTextView;
    private Button showTranslationButton, nextButton;

    private List<Flashcard> flashcards;
    private int currentIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashcard);

        animalImageView = findViewById(R.id.animalImageView);
        nativeWordTextView = findViewById(R.id.nativeWordTextView);
        translatedWordTextView = findViewById(R.id.translatedWordTextView);
        showTranslationButton = findViewById(R.id.showTranslationButton);
        nextButton = findViewById(R.id.nextButton);
        // Initialize views

        ProgressBar progressBar = findViewById(R.id.progressBar);
        TextView progressTextView = findViewById(R.id.progressTextView);
        TextView wordCounterTextView = findViewById(R.id.wordCounterTextView); // Added reference



        // Initialize flashcards
        flashcards = getFlashcards();

        // Set initial progress
        progressBar.setMax(flashcards.size() + 5); // Total progress (flashcards + quiz)
        updateProgress(progressBar, progressTextView, currentIndex);

        // Display the first flashcard and update the word counter
        displayFlashcard(currentIndex);
        updateWordCounter(wordCounterTextView, currentIndex, flashcards.size());

        showTranslationButton.setOnClickListener(v -> {
            // Show the translation and hide the "Prikaži prijevod" button
            translatedWordTextView.setText(flashcards.get(currentIndex).getTranslatedWord());
            translatedWordTextView.setVisibility(View.VISIBLE);
            showTranslationButton.setVisibility(View.GONE);
            nextButton.setVisibility(View.VISIBLE); // Show the "Next" button
        });

        nextButton.setOnClickListener(v -> {
            currentIndex++;
            if (currentIndex < flashcards.size()) {
                displayFlashcard(currentIndex);
                updateWordCounter(wordCounterTextView, currentIndex, flashcards.size());
                nextButton.setVisibility(View.GONE);
                translatedWordTextView.setVisibility(View.GONE);
                showTranslationButton.setVisibility(View.VISIBLE);
                updateProgress(progressBar, progressTextView, currentIndex);
            } else {
                Log.d("FlashcardActivity", "End of flashcards reached. Starting QuizActivity.");
                Intent intent = new Intent(FlashcardActivity.this, QuizActivity.class);
                intent.putParcelableArrayListExtra("flashcards", new ArrayList<>(flashcards));
                intent.putExtra("lessonTitle", getIntent().getStringExtra("lessonTitle")); // Pass lessonTitle
                intent.putExtra("learningLanguage", getIntent().getStringExtra("learningLanguage")); // Pass learningLanguage
                startActivity(intent);
                finish();
            }
        });
    }

    private void updateWordCounter(TextView wordCounterTextView, int currentIndex, int totalWords) {
        // Update the word counter text
        wordCounterTextView.setText("Word " + (currentIndex + 1) + " of " + totalWords);
    }

    private void updateProgress(ProgressBar progressBar, TextView progressTextView, int currentIndex) {
        int progress = currentIndex + 1; // Current flashcard index (1-based)
        progressBar.setProgress(progress);
        progressTextView.setText(progress + "/" + progressBar.getMax());
    }

    private void displayFlashcard(int index) {
        Flashcard flashcard = flashcards.get(index);
        animalImageView.setImageResource(flashcard.getImageResId());
        nativeWordTextView.setText(flashcard.getNativeWord());
    }

    private List<Flashcard> getFlashcards() {
        // Retrieve the app language and learning language from the Intent
        String appLanguage = getIntent().getStringExtra("appLanguage");
        String learningLanguage = getIntent().getStringExtra("learningLanguage");

        List<Flashcard> flashcards = new ArrayList<>();

        // Example logic for Croatian (hr) as native and Spanish (es) as learning
        if ("hr".equals(appLanguage) && "es".equals(learningLanguage)) {
            flashcards.add(new Flashcard(R.drawable.ic_dog, "Pas", "Perro")); // Dog
            flashcards.add(new Flashcard(R.drawable.ic_cat, "Mačka", "Gato")); // Cat
            flashcards.add(new Flashcard(R.drawable.ic_elephant, "Slon", "Elefante")); // Elephant
            flashcards.add(new Flashcard(R.drawable.ic_mouse, "Miš", "Ratón")); // Mouse
            flashcards.add(new Flashcard(R.drawable.ic_turtle, "Kornjača", "Tortuga")); // Turtle

        } else if ("hr".equals(appLanguage) && "en".equals(learningLanguage)) {
            flashcards.add(new Flashcard(R.drawable.ic_dog, "Pas", "Dog"));
            flashcards.add(new Flashcard(R.drawable.ic_cat, "Mačka", "Cat"));
            flashcards.add(new Flashcard(R.drawable.ic_elephant, "Slon", "Elephant"));
            flashcards.add(new Flashcard(R.drawable.ic_mouse, "Miš", "Mouse")); // Mouse
            flashcards.add(new Flashcard(R.drawable.ic_turtle, "Kornjača", "Turtle")); // Turtle

        } else if ("hr".equals(appLanguage) && "hr".equals(learningLanguage)) {
            flashcards.add(new Flashcard(R.drawable.ic_dog, "Pas", "Pas"));
            flashcards.add(new Flashcard(R.drawable.ic_cat, "Mačka", "Mačka"));
            flashcards.add(new Flashcard(R.drawable.ic_elephant, "Slon", "Slon"));
            flashcards.add(new Flashcard(R.drawable.ic_mouse, "Miš", "Miš")); // Mouse
            flashcards.add(new Flashcard(R.drawable.ic_turtle, "Kornjača", "Kornjača")); // Turtle

        } else if ("en".equals(appLanguage) && "en".equals(learningLanguage)) {
            flashcards.add(new Flashcard(R.drawable.ic_dog, "Dog", "Dog"));
            flashcards.add(new Flashcard(R.drawable.ic_cat, "Cat", "Cat"));
            flashcards.add(new Flashcard(R.drawable.ic_elephant, "Elephant", "Elephant"));
            flashcards.add(new Flashcard(R.drawable.ic_mouse, "Mouse", "Mouse"));
            flashcards.add(new Flashcard(R.drawable.ic_turtle, "Turtle", "Turtle"));

        } else if ("en".equals(appLanguage) && "es".equals(learningLanguage)) {
            flashcards.add(new Flashcard(R.drawable.ic_dog, "Dog", "Perro")); // Dog
            flashcards.add(new Flashcard(R.drawable.ic_cat, "Cat", "Gato")); // Cat
            flashcards.add(new Flashcard(R.drawable.ic_elephant, "Elephant", "Elefante")); // Elephant
            flashcards.add(new Flashcard(R.drawable.ic_mouse, "Mouse", "Ratón")); // Mouse
            flashcards.add(new Flashcard(R.drawable.ic_turtle, "Turtle", "Tortuga")); // Turtle

        } else if ("en".equals(appLanguage) && "hr".equals(learningLanguage)) {
            flashcards.add(new Flashcard(R.drawable.ic_dog, "Dog", "Pas"));
            flashcards.add(new Flashcard(R.drawable.ic_cat, "Cat", "Mačka"));
            flashcards.add(new Flashcard(R.drawable.ic_elephant, "Elephant", "Slon"));
            flashcards.add(new Flashcard(R.drawable.ic_mouse, "Mouse", "Miš")); // Mouse
            flashcards.add(new Flashcard(R.drawable.ic_turtle, "Turtle", "Kornjača")); // Turtle

        } else if ("es".equals(appLanguage) && "es".equals(learningLanguage)) {
            flashcards.add(new Flashcard(R.drawable.ic_dog, "Perro", "Perro")); // Dog
            flashcards.add(new Flashcard(R.drawable.ic_cat, "Gato", "Gato")); // Cat
            flashcards.add(new Flashcard(R.drawable.ic_elephant, "Elefante", "Elefante")); // Elephant
            flashcards.add(new Flashcard(R.drawable.ic_mouse, "Ratón", "Ratón")); // Mouse
            flashcards.add(new Flashcard(R.drawable.ic_turtle, "Tortuga", "Tortuga")); // Turtle

        } else if ("es".equals(appLanguage) && "en".equals(learningLanguage)) {
            flashcards.add(new Flashcard(R.drawable.ic_dog, "Perro", "Dog")); // Dog
            flashcards.add(new Flashcard(R.drawable.ic_cat, "Gato", "Cat")); // Cat
            flashcards.add(new Flashcard(R.drawable.ic_elephant, "Elefante", "Elephant")); // Elephant
            flashcards.add(new Flashcard(R.drawable.ic_mouse, "Ratón", "Mouse")); // Mouse
            flashcards.add(new Flashcard(R.drawable.ic_turtle, "Tortuga", "Turtle")); // Turtle

        } else if ("es".equals(appLanguage) && "hr".equals(learningLanguage)) {
            flashcards.add(new Flashcard(R.drawable.ic_dog, "Perro", "Pas")); // Dog
            flashcards.add(new Flashcard(R.drawable.ic_cat, "Gato", "Mačka")); // Cat
            flashcards.add(new Flashcard(R.drawable.ic_elephant, "Elefante", "Slon")); // Elephant
            flashcards.add(new Flashcard(R.drawable.ic_mouse, "Ratón", "Miš")); // Mouse
            flashcards.add(new Flashcard(R.drawable.ic_turtle, "Tortuga", "Kornjača")); // Turtle
        }
        // Add more conditions for other language combinations as needed

        return flashcards;
    }
}