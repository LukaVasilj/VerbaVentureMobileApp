package ba.sum.fsre.hackaton.user.home;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import ba.sum.fsre.hackaton.R;

public class SelectLanguageActivity extends AppCompatActivity {

    private String appLanguage; // Jezik aplikacije (npr. "hr")

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_language); // Use the correct layout file

        // Fetch the app language from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("Settings", MODE_PRIVATE);
        appLanguage = prefs.getString("My_Lang", "en"); // Default is "en" if no value is set

        // Set up click listeners for language options
        findViewById(R.id.learnEnglishOption).setOnClickListener(v -> navigateToLessonActivity("en"));
        findViewById(R.id.learnCroatianOption).setOnClickListener(v -> navigateToLessonActivity("hr"));
        findViewById(R.id.learnSpanishOption).setOnClickListener(v -> navigateToLessonActivity("es"));
    }



    private void navigateToLessonActivity(String learningLanguage) {
        // Start LessonActivity and pass the app language and learning language
        Intent intent = new Intent(SelectLanguageActivity.this, LessonActivity.class);
        intent.putExtra("appLanguage", appLanguage);
        intent.putExtra("learningLanguage", learningLanguage);
        startActivity(intent);
    }

}