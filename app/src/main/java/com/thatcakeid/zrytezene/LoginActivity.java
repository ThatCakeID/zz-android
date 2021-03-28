package com.thatcakeid.zrytezene;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
    private TextInputEditText tie1, tie2;
    private TextInputLayout til1, til2;
    private MaterialButton button_continue;
    private FirebaseAuth auth;
    private TextView textView4, textView6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        tie1 = findViewById(R.id.login_email_tie);
        tie2 = findViewById(R.id.login_passw_tie);
        til1 = findViewById(R.id.login_email_til);
        til2 = findViewById(R.id.login_passw_til);
        
        button_continue = findViewById(R.id.button_continue);

        textView4 = findViewById(R.id.textView4);
        textView6 = findViewById(R.id.textView6);

        FirebaseApp.initializeApp(getApplicationContext());
        auth = FirebaseAuth.getInstance();

        tie1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Empty
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (tie1.getText().toString().trim().matches("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+")) {
                    til1.setError(null);
                } else {
                    til1.setError("Invalid email!");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Empty
            }
        });

        button_continue.setOnClickListener(v -> {
            if (tie1.getText().toString().trim().matches("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+")) {
                if (tie2.getText().toString().length() == 0)
                    //Snackbar.make(view, "Password can't be empty!", Snackbar.LENGTH_LONG);
                    Toast.makeText(LoginActivity.this, "Password can't be empty!", Toast.LENGTH_LONG).show();
                else {
                    auth.signInWithEmailAndPassword(tie1.getText().toString().trim(),
                            tie2.getText().toString()).addOnSuccessListener(authResult -> {
                                if (auth.getCurrentUser().isEmailVerified()) {
                                    Toast.makeText(LoginActivity.this, "Logged in", Toast.LENGTH_LONG).show();
                                    startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                                    finish();
                                } else {
                                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(LoginActivity.this);
                                    alertDialog.setTitle("Unverified Email");
                                    alertDialog.setMessage("Your email isn't verified. Do you want to re-send a new verification link to your email?");
                                    alertDialog.setPositiveButton("Yes", (dialog, which) -> auth.getCurrentUser().sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            //Snackbar.make(view, "A verification link has been sent to your email. Please check your inbox or spam box.", Snackbar.LENGTH_LONG);
                                            Toast.makeText(LoginActivity.this, "A verification link has been sent to your email. Please check your inbox or spam box.", Toast.LENGTH_LONG).show();
                                        }
                                    }).addOnFailureListener(e -> {
                                        //Snackbar.make(view, e.getMessage(), Snackbar.LENGTH_LONG);
                                        Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                    }));
                                    alertDialog.setNegativeButton("No", (dialog, which) -> auth.signOut());
                                    alertDialog.setCancelable(false);
                                    alertDialog.create().show();
                                }
                            }).addOnFailureListener(e -> {
                                //Snackbar.make(view, e.getMessage(), Snackbar.LENGTH_LONG);
                                Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                            });
                }
            } else {
                //Snackbar.make(view, "Invalid email!", Snackbar.LENGTH_LONG);
                Toast.makeText(LoginActivity.this, "Invalid email!", Toast.LENGTH_LONG).show();
            }
        });

        textView6.setOnClickListener(v -> {
            if (tie1.getText().toString().trim().matches("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+")) {
                auth.sendPasswordResetEmail(tie1.getText().toString());
                //Snackbar.make(view, "A verification link has been sent to your email. Please check your inbox or spam box.", Snackbar.LENGTH_LONG);
                Toast.makeText(LoginActivity.this, "A verification link has been sent to your email. Please check your inbox or spam box.", Toast.LENGTH_LONG).show();

            } else {
                //Snackbar.make(view, "Invalid email!", Snackbar.LENGTH_LONG);
                Toast.makeText(LoginActivity.this, "Please enter a valid email!", Toast.LENGTH_LONG).show();
            }
        });

        textView4.setOnClickListener(v -> {
            Intent i = new Intent(getApplicationContext(), RegisterActivity.class);
            i.putExtra("email", tie1.getText().toString());
            startActivity(i);
        });
    }
}