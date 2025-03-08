package ba.sum.fsre.hackaton.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import ba.sum.fsre.hackaton.MainActivity;
import ba.sum.fsre.hackaton.R;
import ba.sum.fsre.hackaton.user.adventure.AdventureModeActivity;

public class MainPageActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private TextView welcomeTextView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        mAuth = FirebaseAuth.getInstance();
        welcomeTextView = findViewById(R.id.welcomeTextView);

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users").document(user.getUid()).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    String username = document.getString("username");
                                    if (username != null) {
                                        String welcomeMessage = "Welcome, " + username + "!";
                                        welcomeTextView.setText(welcomeMessage);
                                    }
                                }
                            }
                        }
                    });
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.navigation_home) {
                    // Handle home action
                    return true;
                } else if (id == R.id.navigation_map) {
                    // Handle map action
                    return true;
                } else if (id == R.id.navigation_challenges) {
                    // Handle challenges action
                    return true;
                } else if (id == R.id.navigation_progress) {
                    // Handle progress action
                    return true;
                } else if (id == R.id.navigation_profile) {
                    // Handle profile action
                    return true;
                } else if (id == R.id.navigation_logout) {
                    mAuth.signOut();
                    Intent intent = new Intent(MainPageActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                    return true;
                }
                return false;
            }
        });

        // Dodana logika za gumb Adventure Mode
        Button adventureModeButton = findViewById(R.id.adventureModeButton);
        adventureModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainPageActivity.this, AdventureModeActivity.class);
                startActivity(intent);
            }
        });
    }
}