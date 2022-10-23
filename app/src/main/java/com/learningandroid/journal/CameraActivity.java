package com.learningandroid.journal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;
import com.learningandroid.journal.databinding.ActivityCameraBinding;


import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraActivity extends AppCompatActivity {

    private static final String TAG = "check";
    ActivityCameraBinding binding;
    private static final int REQUEST_CODE_PERMISSIONS = 101;
    private static final String ALL_PERMISSION[] = new String[]{Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static final String FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS";
    private ExecutorService cameraExecutor;
    private ImageCapture imageCapture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCameraBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if(allPermissionsGranted()) {
            startCamera();
        }
        else {
            ActivityCompat.requestPermissions(CameraActivity.this,
                    ALL_PERMISSION,
                    REQUEST_CODE_PERMISSIONS);
        }

        binding.captureImageButton.setOnClickListener(view ->  { takePhoto(); } );

        cameraExecutor = Executors.newSingleThreadExecutor();
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderListenableFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderListenableFuture.addListener(() -> {
            //bind the lifecucle of cameras to the lifecycle owner

            ProcessCameraProvider cameraProvider = null;
            try {
                cameraProvider = cameraProviderListenableFuture.get();
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }


            //Preview
            Preview preview = new Preview.Builder()
                    .build();
            preview.setSurfaceProvider(binding.viewFinder.getSurfaceProvider());

            //select back camera as a default
            imageCapture = new ImageCapture.Builder()
                    .setTargetRotation(Surface.ROTATION_0)
                    .build();

            CameraSelector cameraSelector = new CameraSelector
                    .Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                    .build();

            ImageAnalysis imageAnalysis =
                    new ImageAnalysis.Builder()
                            .setTargetResolution(new Size(1280, 720))
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build();

            imageAnalysis.setAnalyzer(cameraExecutor, new ImageAnalysis.Analyzer() {
                @Override
                public void analyze(@NonNull ImageProxy imageProxy) {
                    int rotationDegrees = imageProxy.getImageInfo().getRotationDegrees();
                    imageProxy.close();
                }
            });


            try{
                //Unbind use cases before rebinding
                cameraProvider.unbindAll();
                //bind use cases to camera
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
            }
            catch (Exception e) {
                Log.e(TAG, "Use case binding failed", e);
            }
        },ContextCompat.getMainExecutor(this));

    }

    private void takePhoto() {
        if(imageCapture == null) return;

        //Create time staped name and MediaStore entry
        String dateFormat = new SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis());
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME,dateFormat);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image");
        }

        //Create output options object which contains file + metadata
        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions
                .Builder(getContentResolver(),
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues)
                .build();

        //Set up image capture listener, which is triggered after photo has been taken
        imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        String msg = "Photo capture succeeded: ${outputFileResults.savedUri}";
                        Toast.makeText(CameraActivity.this, msg, Toast.LENGTH_SHORT).show();
                        Log.d(TAG, msg);
                        Intent editIntent = new Intent(CameraActivity.this, EditActivity.class);
                        Uri savedUri = outputFileResults.getSavedUri();
                        editIntent.putExtra("uri", savedUri);
                        startActivity(editIntent);
                        finish();

                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Log.e(TAG, "Photo capture failed: ${exception.message}", exception);
                    }
                }
        );

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == REQUEST_CODE_PERMISSIONS) {
            if(allPermissionsGranted()) {
                startCamera();
            }
            else {
                Toast.makeText(this,
                        "Permissions not granted by the user. ",
                        Toast.LENGTH_SHORT)
                        .show();
                finish();
            }
        }
    }

    private boolean allPermissionsGranted() {
        for(String permission : ALL_PERMISSION) {
            if (ContextCompat.checkSelfPermission(CameraActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }


}