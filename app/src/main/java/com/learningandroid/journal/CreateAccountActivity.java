package com.learningandroid.journal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.learningandroid.journal.databinding.ActivityCreateAccountBinding;
import com.learningandroid.journal.util.JournalApi;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CreateAccountActivity extends AppCompatActivity {

    private ActivityCreateAccountBinding binding;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference collectionReference = db.collection("Users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateAccountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Objects.requireNonNull(getSupportActionBar()).setElevation(0);
        firebaseAuth = FirebaseAuth.getInstance();

        authStateListener = firebaseAuth -> {
            currentUser = firebaseAuth.getCurrentUser();

            if(currentUser != null) {
                //user is already logged in...
            }
            else {
                //no user yet....
            }
        };

        binding.createAccountButton.setOnClickListener(view ->
        {
            String email = binding.emailAcct.getText().toString();
            String password = binding.passwordAcct.getText().toString();
            String username = binding.usernameAccount.getText().toString();

            if(!TextUtils.isEmpty(email)
                    && !TextUtils.isEmpty(password)
                    && !TextUtils.isEmpty(username)) {
                Toast.makeText(CreateAccountActivity.this, "creating", Toast.LENGTH_SHORT).show();
                createUserEmailAccount(email, password, username);
            }
            else {
                Toast.makeText(CreateAccountActivity.this,
                        "Empty Fields Not allowed",
                            Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void createUserEmailAccount(String email, String password, String username) {

            binding.acctProgress.setVisibility(View.VISIBLE);

            firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()) {

                                //we take user to AddJournalActivity
                                currentUser = firebaseAuth.getCurrentUser();
                                assert currentUser != null;
                                String currentUserId = currentUser.getUid();

                                //create a user map so we can create a user in the user collection
                                Map<String, String> userObj = new HashMap<>();
                                userObj.put("userId", currentUserId);
                                userObj.put("username", username);

                                //save to our firestore database
                                collectionReference.add(userObj)
                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {
                                                documentReference.get()
                                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                if(task.getResult().exists()) {
                                                                    String name = task.getResult().getString("username");

                                                                    JournalApi journalApi = JournalApi.getInstance();
                                                                    journalApi.setUserId(currentUserId);
                                                                    journalApi.setUserName(name);

                                                                    Intent intent = new Intent(CreateAccountActivity.this, PostJournalActivity.class);
                                                                    startActivity(intent);
                                                                }
                                                                binding.acctProgress.setVisibility(View.INVISIBLE);
                                                            }
                                                        });
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.e("firebaseError", "onFailure: User information could not be stored in database", e);
                                            }
                                        });


                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(CreateAccountActivity.this, "Could not create the account", Toast.LENGTH_SHORT).show();
                            Log.e("firebaseError", "onFailure: Could not create the account", e);
                        }
                    });
    }


    @Override
    protected void onStart() {
        super.onStart();
        currentUser = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }
}