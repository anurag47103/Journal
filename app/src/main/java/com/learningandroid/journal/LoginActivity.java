package com.learningandroid.journal;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.learningandroid.journal.databinding.ActivityLoginBinding;
import com.learningandroid.journal.util.JournalApi;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("Users");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();

        binding.createAccountLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, CreateAccountActivity.class));
            }
        });


        binding.emailSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginemailPasswordUser(binding.email.getText().toString().trim(),
                            binding.password.getText().toString().trim());
            }
        });

    }

    private void loginemailPasswordUser(String email, String pwd) {
        if(!TextUtils.isEmpty(email) &&
            !TextUtils.isEmpty(pwd)) {
            
                firebaseAuth.signInWithEmailAndPassword(email, pwd)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                
                                if(task.isSuccessful()) {
                                    binding.loginProgress.setVisibility(View.VISIBLE);
                                    Toast.makeText(LoginActivity.this, "Mubarakho!", Toast.LENGTH_SHORT).show();

                                    FirebaseUser user = firebaseAuth.getCurrentUser();
                                    assert user != null;
                                    String currentUserId = user.getUid();

                                    collectionReference.whereEqualTo("userId" , currentUserId)
                                            .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                                @Override
                                                public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                                    if(error != null) {

                                                    }

                                                    binding.loginProgress.setVisibility(View.INVISIBLE);
                                                    assert value != null;
                                                    if(!value.isEmpty()) {
                                                        for(QueryDocumentSnapshot snapshot : value) {
                                                            JournalApi journalApi = JournalApi.getInstance();
                                                            journalApi.setUserName(snapshot.getString("username"));
                                                            journalApi.setUserId(currentUserId);

                                                            //go to listactivity
                                                            startActivity(new Intent(LoginActivity.this,
                                                                    JournalListActivity.class));
                                                        }
                                                    }
                                                }
                                            });
                                }
                                else {
                                    binding.loginProgress.setVisibility(View.INVISIBLE);
                                    Toast.makeText(LoginActivity.this, "Credentials sahi nai hai", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                binding.loginProgress.setVisibility(View.INVISIBLE);
                            }
                        });
        }
        else {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
        }
    }


}