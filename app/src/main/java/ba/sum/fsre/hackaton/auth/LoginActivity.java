package ba.sum.fsre.hackaton.auth;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.AuthResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

import ba.sum.fsre.hackaton.user.MainPageActivity;
import ba.sum.fsre.hackaton.R;
import ba.sum.fsre.hackaton.user.UserRole;
import ba.sum.fsre.hackaton.admin.AdminDashboardActivity;

public class LoginActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 9001;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private ProgressDialog progressDialog;
    private EditText emailField;
    private EditText passwordField;
    private Button loginButton;
    private TextInputLayout emailLayout;
    private TextInputLayout passwordLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        emailLayout = findViewById(R.id.emailLayout);
        passwordLayout = findViewById(R.id.passwordLayout);

        emailField = findViewById(R.id.email);
        passwordField = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);
        TextView forgotPassword = findViewById(R.id.forgotPassword);
        TextView signupLink = findViewById(R.id.signupLink);
        TextView errorMessage = findViewById(R.id.errorMessage);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailField.getText().toString().trim();
                String password = passwordField.getText().toString().trim();
                if (TextUtils.isEmpty(email)) {
                    emailLayout.setError(getString(R.string.please_enter_email));
                    return;
                } else {
                    emailLayout.setError(null);
                }

                if (TextUtils.isEmpty(password)) {
                    passwordLayout.setError(getString(R.string.please_enter_password));
                    return;
                } else {
                    passwordLayout.setError(null);
                }

                loginUser(email, password);
            }
        });

        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, PasswordRecoveryActivity.class);
                startActivity(intent);
            }
        });

        signupLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        Button googleSignInButton = findViewById(R.id.googleSignInButton);
        googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
    }

    private void loginUser(String email, String password) {
        showLoadingScreen();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        hideLoadingScreen();
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null && user.isEmailVerified()) {
                                fetchUserRole(user);
                            } else {
                                Toast.makeText(LoginActivity.this, getString(R.string.verify_email), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.w("LoginActivity", "Authentication Failed", task.getException());
                            Toast.makeText(LoginActivity.this, getString(R.string.authentication_failed), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void fetchUserRole(FirebaseUser user) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(user.getUid()).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                String role = document.getString("role");
                                if (UserRole.ADMIN.name().equals(role)) {
                                    Intent intent = new Intent(LoginActivity.this, AdminDashboardActivity.class);
                                    startActivity(intent);
                                } else {
                                    Intent intent = new Intent(LoginActivity.this, MainPageActivity.class);
                                    startActivity(intent);
                                }
                                finish();
                            } else {
                                Map<String, Object> userData = new HashMap<>();
                                userData.put("email", user.getEmail());
                                userData.put("role", UserRole.USER.name());
                                db.collection("users").document(user.getUid()).set(userData)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Intent intent = new Intent(LoginActivity.this, MainPageActivity.class);
                                                    startActivity(intent);
                                                    finish();
                                                } else {
                                                    Log.w("LoginActivity", "Failed to create user document", task.getException());
                                                    Toast.makeText(LoginActivity.this, getString(R.string.failed_to_fetch_user_role), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }
                        } else {
                            Log.w("LoginActivity", "Fetch failed", task.getException());
                            Toast.makeText(LoginActivity.this, getString(R.string.failed_to_fetch_user_role), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void signIn() {
        mGoogleSignInClient.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            showLoadingScreen();
            firebaseAuthWithGoogle(account);
        } catch (ApiException e) {
            Log.w("LoginActivity", "Google sign-in failed", e);
            Toast.makeText(this, getString(R.string.google_sign_in_failed, e.getStatusCode()), Toast.LENGTH_SHORT).show();
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        hideLoadingScreen();
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                String email = user.getEmail();
                                String username = email != null ? email.split("@")[0] : "User";
                                saveUserToFirestore(user, username);
                            }
                        } else {
                            Log.w("LoginActivity", "Authentication Failed", task.getException());
                            Toast.makeText(LoginActivity.this, getString(R.string.authentication_failed), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void saveUserToFirestore(FirebaseUser user, String username) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(user.getUid()).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (!document.exists()) {
                                Map<String, Object> userData = new HashMap<>();
                                userData.put("email", user.getEmail());
                                userData.put("username", username);
                                userData.put("role", UserRole.USER.name());
                                db.collection("users").document(user.getUid()).set(userData)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    fetchUserRole(user);
                                                } else {
                                                    Log.w("LoginActivity", "Failed to create user document", task.getException());
                                                    Toast.makeText(LoginActivity.this, getString(R.string.failed_to_save_user_data), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            } else {
                                fetchUserRole(user);
                            }
                        } else {
                            Log.w("LoginActivity", "Fetch failed", task.getException());
                            Toast.makeText(LoginActivity.this, getString(R.string.failed_to_fetch_user_role), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void showLoadingScreen() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.signing_in));
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void hideLoadingScreen() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}