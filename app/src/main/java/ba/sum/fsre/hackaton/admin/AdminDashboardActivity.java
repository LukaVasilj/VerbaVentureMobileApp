package ba.sum.fsre.hackaton.admin;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Locale;

import ba.sum.fsre.hackaton.R;

public class AdminDashboardActivity extends AppCompatActivity {

    private static final int SPEECH_REQUEST_CODE = 100;
    private TextView instructionTextView, resultTextView;
    private Button startSpeechButton;
    private String targetSentence = "Welcome to the Admin Page";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        instructionTextView = findViewById(R.id.instructionTextView);
        resultTextView = findViewById(R.id.resultTextView);
        startSpeechButton = findViewById(R.id.startSpeechButton);

        // Postavi tekst koji korisnik treba izgovoriti
        instructionTextView.setText(targetSentence);

        startSpeechButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSpeechRecognition();
            }
        });
    }

    private void startSpeechRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.ENGLISH);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Please say: " + targetSentence);

        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            startActivityForResult(intent, SPEECH_REQUEST_CODE);
        } else {
            Toast.makeText(this, "Speech recognition is not available on this device.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results != null && !results.isEmpty()) {
                String recognizedText = results.get(0);
                resultTextView.setText("You said: " + recognizedText);

                if (recognizedText.equalsIgnoreCase(targetSentence)) {
                    Toast.makeText(this, "Correct! You said the sentence correctly.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Incorrect. Try again.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}