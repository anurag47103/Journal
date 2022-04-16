package com.learningandroid.journal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.learningandroid.journal.databinding.ActivityPostJournalBinding;
import com.learningandroid.journal.model.Journal;
import com.learningandroid.journal.util.JournalApi;

import java.util.Date;
import java.util.Objects;

public class PostJournalActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int GALLERY_CODE = 1;
    private static final String TAG = "postjournalactivity";
    ActivityPostJournalBinding binding;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;

    //Connection to Firestore
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference storageReference;
    private CollectionReference collectionReference = db.collection("Journal");
    private String currentUserId;
    private String currentUserName;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPostJournalBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Objects.requireNonNull(getSupportActionBar()).setElevation(0);
        firebaseAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        binding.addPhotoButton.setOnClickListener(this);
        binding.postSaveButton.setOnClickListener(this);

        if(JournalApi.getInstance()!=null) {
            currentUserId = JournalApi.getInstance().getUserId();
            currentUserName = JournalApi.getInstance().getUsername();

            binding.currentUserTextView.setText(currentUserName);
        }

        authStateListener = firebaseAuth -> {
            user = firebaseAuth.getCurrentUser();
            if(user != null) {

            }
            else {

            }

        };

    }

    private void saveJournal() {
        binding.postProgressBar.setVisibility(View.VISIBLE);
        String title = binding.posttitleEditText.getText().toString().trim();
        String thoughts = binding.postThoughtEditText.getText().toString().trim();

        if(!TextUtils.isEmpty(title) &&
                !TextUtils.isEmpty(thoughts) &&
                imageUri != null) {
                final StorageReference filePath = storageReference
                        .child("journal_images")
                        .child("my_image" + Timestamp.now().getSeconds());

                filePath.putFile(imageUri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                binding.postProgressBar.setVisibility(View.INVISIBLE);

                                filePath.getDownloadUrl()
                                        .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {

                                                String imageUri = uri.toString();
                                                Journal journal = new Journal();
                                                journal.setTitle(title);
                                                journal.setThought(thoughts);
                                                journal.setImageUri(imageUri);
                                                journal.setTimeAdded(new Timestamp((new Date())));
                                                journal.setUserName(currentUserName);
                                                journal.setUserId(currentUserId);


                                                collectionReference.add(journal)
                                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                            @Override
                                                            public void onSuccess(DocumentReference documentReference) {
                                                                binding.postProgressBar.setVisibility(View.INVISIBLE);
                                                                startActivity(new Intent(PostJournalActivity.this, JournalListActivity.class));
                                                                finish();
                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Log.d(TAG, "onFailure: " + e.getMessage());
                                                            }
                                                        });
                                            }
                                        });
                                //todo : create a journal object -
                                //todo: invoke our collectionReference
                                //and save a Journal instance.
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                binding.postProgressBar.setVisibility(View.INVISIBLE);
                            }
                        });




        }
        else {

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_CODE && resultCode == RESULT_OK) {
            if(data != null) {
                imageUri = data.getData();
                binding.backgroundImageView.setImageURI(imageUri);
                binding.addPhotoButton.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.addPhotoButton:
                //saveJournal
                Intent galleryIntent  = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_CODE);
                break;
            case R.id.postSaveButton:
                //get image from gallery phone
                saveJournal();
                break;

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        user = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(firebaseAuth != null) {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }
}