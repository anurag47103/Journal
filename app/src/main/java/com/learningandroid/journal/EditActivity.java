package com.learningandroid.journal;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;


import com.learningandroid.journal.databinding.ActivityEditBinding;
import com.yalantis.ucrop.UCrop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.UUID;

public class EditActivity extends AppCompatActivity {

    private ActivityEditBinding binding;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if(getIntent().getExtras() != null) {
            imageUri = (Uri) getIntent().getExtras().get("uri");
            binding.backgroundImageView.setImageURI(imageUri);
            binding.backgroundImageView.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        }

        String dest_uri = new StringBuilder(UUID.randomUUID().toString()).append(".jpg").toString();

        UCrop.Options options = new UCrop.Options();

        UCrop.of(imageUri, Uri.fromFile(new File(getCacheDir(), dest_uri)))
                .withOptions(options)
                .withAspectRatio(0,0)
                .useSourceImageAspectRatio()
                .withMaxResultSize(2000, 2000)
                .start(EditActivity.this);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode==UCrop.REQUEST_CROP) {
            final Uri resultUri = UCrop.getOutput(data);
            Intent returnIntent = new Intent(EditActivity.this, PostJournalActivity.class);
            Log.d("check", "editScreen - "+ resultUri);
            returnIntent.putExtra("RESULTuri", resultUri);
            returnIntent.putExtra("toUpdate", 2);
            startActivity(returnIntent);
            finish();
        }
        else if(resultCode== UCrop.RESULT_ERROR){
            final Throwable cropError = UCrop.getError(data);
        }

    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public String getRealPathFromURI(Uri uri) {
        String path = "";
        if (getContentResolver() != null) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                path = cursor.getString(idx);
                cursor.close();
            }
        }
        return path;
    }
}