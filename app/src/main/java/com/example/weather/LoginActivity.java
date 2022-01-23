package com.example.weather;

import static androidx.constraintlayout.motion.utils.Oscillator.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import io.paperdb.Paper;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText emailEt, passwordEt;
    private Button createUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getSupportActionBar().setTitle("Log In");

        Paper.init(this);

        mAuth = FirebaseAuth.getInstance();

        emailEt = findViewById(R.id.loginEmailEt);
        passwordEt = findViewById(R.id.loginPassEt);
        createUser = findViewById(R.id.createUserBtn);

        createUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });
    }

    private void loginUser() {

        String email = emailEt.getText().toString();
        String password = passwordEt.getText().toString();

        if(TextUtils.isEmpty(email)){
            emailEt.setError("Enter Email!");
            return;
        }

        if(TextUtils.isEmpty(password)){
            passwordEt.setError("Enter Password!");
            return;
        }



        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){


                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                            if (user.isEmailVerified()) {
                                String userId = task.getResult().getUser().getUid();
                                FirebaseDatabase.getInstance().getReference("Users")
                                        .child(userId)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if (snapshot.exists()) {
                                                    User user = snapshot.getValue(User.class);
                                                    Utils.currentUser = user;
                                                    Paper.book().write("user_info", user);

                                                    Intent intent = new Intent(getApplicationContext(), UserHomeActivity.class);
                                                    startActivity(intent);

                                                    finish();
                                                } else {
                                                    Toast.makeText(LoginActivity.this, "Invalid Parameters...", Toast.LENGTH_SHORT).show();
                                                }


                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                Toast.makeText(LoginActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
                                                Log.d(TAG, "onCancelled: "+error.getDetails());
                                            }
                                        });
                            }else
                            {
                                // email is not verified, so just prompt the message to the user and restart this activity.
                                // NOTE: don't forget to log out the user.
                                Toast.makeText(LoginActivity.this, "Email Not Verified!!!", Toast.LENGTH_LONG).show();
                                FirebaseAuth.getInstance().signOut();

                                overridePendingTransition(0, 0);
                                finish();
                                overridePendingTransition(0, 0);
                                startActivity(getIntent());

                            }
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void gotoSignUp(View view) {
        Intent intent = new Intent(getApplicationContext(),SignUpActivity.class);
        startActivity(intent);
    }
    }
