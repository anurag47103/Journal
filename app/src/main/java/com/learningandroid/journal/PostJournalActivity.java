package com.learningandroid.journal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.learningandroid.journal.databinding.ActivityPostJournalBinding;
import com.learningandroid.journal.model.Journal;
import com.learningandroid.journal.util.JournalApi;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.Objects;

public class PostJournalActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int GALLERY_CODE = 1;
    private static final int CAMERA_CODE = 2;
    private static final String TAG = "check";
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

    private int postActionId;
    Journal journal_from_list;

    FusedLocationProviderClient client;
    private LatLng currentLatLnd;
    private GeoPoint geoPoint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPostJournalBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        postActionId = getIntent().getIntExtra("toUpdate", 0);
        journal_from_list = getIntent().getParcelableExtra("journal");

        //if we are updating the Journal
        if (JournalApi.getInstance() != null) {
            currentUserId = JournalApi.getInstance().getUserId();
            currentUserName = JournalApi.getInstance().getUserName();
        }

        Log.d(TAG, "cehck - " + postActionId);

        if (postActionId != 0) {
            String imgUrl;
            String title;
            String thought;
            if(journal_from_list != null) {
                imgUrl = journal_from_list.getImageUri();
                title = journal_from_list.getTitle();
                thought = journal_from_list.getThought();
                geoPoint = journal_from_list.getGeoPoint();

                JournalApi.getInstance().setImageUri(imgUrl);
                JournalApi.getInstance().setTitle(title);
                JournalApi.getInstance().setThought(thought);
                JournalApi.getInstance().setGeoPoint(geoPoint);
            }
            else {
                imgUrl = JournalApi.getInstance().getImageUri();
                title = JournalApi.getInstance().getTitle();
                thought = JournalApi.getInstance().getThought();
                geoPoint = JournalApi.getInstance().getGeoPoint();
            }

            if(postActionId == 2) {
                imgUrl = getIntent().getStringExtra("RESULTuri");
//                Toast.makeText(this, postActionId, Toast.LENGTH_SHORT).show();
                Log.d(TAG, "imgURl - " + imgUrl);

            }



            binding.posttitleEditText.setText(title);
            binding.postThoughtEditText.setText(thought);

            Picasso.get().load(imgUrl)
                    .placeholder(R.drawable.bk_photo_1)
                    .fit()
                    .into(binding.backgroundImageView);

            binding.postSaveButton.setText("Update");
        }

        Objects.requireNonNull(getSupportActionBar()).setElevation(0);
        firebaseAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        binding.addPhotoButton.setOnClickListener(this);
        binding.postSaveButton.setOnClickListener(this);



        authStateListener = firebaseAuth -> {
            user = firebaseAuth.getCurrentUser();
            if (user != null) {

            } else {

            }

        };

        binding.locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(PostJournalActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    setCurrentLocation();
                } else {
                    ActivityCompat.requestPermissions(PostJournalActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
                }
            }

        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 44) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setCurrentLocation();
            }
        }
    }

    private void setCurrentLocation() {

        client = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Task<Location> task = client.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location != null) {
                    geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                    currentLatLnd = new LatLng(location.getLatitude(), location.getLongitude());
                    Toast.makeText(PostJournalActivity.this, "loc" + currentLatLnd.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });
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
                                                journal.setJournalId(collectionReference.document().getId());
                                                if(geoPoint != null) journal.setGeoPoint(geoPoint);


                                                collectionReference.document(journal.getJournalId()).set(journal)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void unused) {
                                                                binding.postProgressBar.setVisibility(View.INVISIBLE);
                                                                startActivity(new Intent(PostJournalActivity.this, JournalListActivity.class));
                                                                finish();
                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {

                                                            }
                                                        });
//                                                collectionReference.add(journal)
//                                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
//                                                            @Override
//                                                            public void onSuccess(DocumentReference documentReference) {
//                                                                binding.postProgressBar.setVisibility(View.INVISIBLE);
//                                                                startActivity(new Intent(PostJournalActivity.this, JournalListActivity.class));
////                                                                documentReference.getId();
//                                                                Log.d("check", "doc Iddd " + documentReference.getId());
//
//                                                                finish();
//                                                            }
//                                                        })
//                                                        .addOnFailureListener(new OnFailureListener() {
//                                                            @Override
//                                                            public void onFailure(@NonNull Exception e) {
//                                                                Log.d(TAG, "onFailure: " + e.getMessage());
//                                                            }
//                                                        });
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
        
        if(requestCode == CAMERA_CODE && resultCode == RESULT_OK) {
            if(data != null) {
                Toast.makeText(this, "working", Toast.LENGTH_SHORT).show();
                
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.addPhotoButton:
                Intent galleryIntent  = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_CODE);

//                Intent cameraIntent = new Intent(PostJournalActivity.this, CameraActivity.class);
//                startActivityForResult(cameraIntent,CAMERA_CODE);
                    
                break;
            case R.id.postSaveButton:
                if(postActionId==0) saveJournal();
                else if(postActionId==1) updateJournal();
                break;
                

        }
    }

    private void updateJournal() {

        final String title = binding.posttitleEditText.getText().toString().trim();
        final String thoughts = binding.postThoughtEditText.getText().toString().trim();
        final String imageUrl= journal_from_list.getImageUri();
        //final String[] this_journal_id = new String[1];

        binding.postProgressBar.setVisibility(View.VISIBLE);
        binding.postSaveButton.setEnabled(false);

        if(imageUri != null){
            if (!TextUtils.isEmpty(title) &&
                    !TextUtils.isEmpty(thoughts)) {

                final StorageReference filepath = storageReference
                        .child("journal_images")
                        .child("image_" + Timestamp.now().getSeconds());

                filepath.putFile(imageUri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {


                                filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {

                                        String imageUrl = uri.toString();
                                        updateEntry(title,thoughts,imageUrl);
                                    }
                                });
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                binding.postProgressBar.setVisibility(View.INVISIBLE);
                                binding.postSaveButton.setEnabled(true);
//                                Log.e("Error: ","Image upload failure");
                            }
                        });


            } else emptyFields();
        }
        //if user doesn't change to a new image
        else{
            if (!TextUtils.isEmpty(title) &&
                    !TextUtils.isEmpty(thoughts)) {

                updateEntry(title,thoughts,imageUrl);

            } else emptyFields();
        }

    }

    void updateEntry(String title,String thoughts,String imageUrl){
        final Journal j = new Journal();
        j.setTitle(title);
        j.setThought(thoughts);
        j.setImageUri(imageUrl);
        j.setTimeAdded(new Timestamp(new Date()));
        j.setUserName(currentUserName);
        j.setUserId(currentUserId);
        j.setJournalId(journal_from_list.getJournalId());
        if(geoPoint != null) j.setGeoPoint(geoPoint);


        collectionReference.document(journal_from_list.getJournalId())
                .set(j).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(PostJournalActivity.this, "Updated!",
                        Toast.LENGTH_LONG)
                        .show();
                binding.postProgressBar.setVisibility(View.INVISIBLE);
                startActivity(new Intent(PostJournalActivity.this,
                        JournalListActivity.class));
                binding.postSaveButton.setEnabled(true);
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    void emptyFields(){
        Toast.makeText(this, "Empty Fields", Toast.LENGTH_LONG).show();
        binding.postProgressBar.setVisibility(View.INVISIBLE);
        binding.postSaveButton.setEnabled(true);
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