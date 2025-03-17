package ba.sum.fsre.hackaton;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ProgressBar;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import ba.sum.fsre.hackaton.user.MainPageActivity;

public class SplashActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private ProgressBar loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mAuth = FirebaseAuth.getInstance();
        loadingBar = findViewById(R.id.loadingBar);

        // Simulate loading for 3 seconds
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                FirebaseUser currentUser = mAuth.getCurrentUser();
                Intent intent;
                if (currentUser != null) {
                    // User is signed in, redirect to MainPageActivity
                    intent = new Intent(SplashActivity.this, MainPageActivity.class);
                } else {
                    // User is not signed in, redirect to MainActivity
                    intent = new Intent(SplashActivity.this, MainActivity.class);
                }
                startActivity(intent);
                finish();
            }
        }, 3000); // 3000 milliseconds = 3 seconds

        // Animate the loading bar
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int progress = 0; progress <= 100; progress++) {
                    final int currentProgress = progress;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loadingBar.setProgress(currentProgress);
                        }
                    });
                    try {
                        Thread.sleep(30); // Sleep for 30 milliseconds to simulate loading
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
}