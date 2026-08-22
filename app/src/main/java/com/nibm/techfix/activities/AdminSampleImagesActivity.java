package com.nibm.techfix.activities;

import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nibm.techfix.R;
import com.nibm.techfix.adapters.SampleImageAdapter;
import com.nibm.techfix.database.SampleImageDao;

import java.io.File;
import java.io.IOException;

public class AdminSampleImagesActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_REQUEST = 300;

    private SampleImageDao sampleImageDao;
    private RecyclerView rvSampleImages;
    private Uri pendingPhotoUri;

    private final ActivityResultLauncher<Uri> takePictureLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                if (success) {
                    promptForCaption(pendingPhotoUri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_sample_images);

        sampleImageDao = new SampleImageDao(this);
        rvSampleImages = findViewById(R.id.rvSampleImages);
        rvSampleImages.setLayoutManager(new LinearLayoutManager(this));

        Button btnAddImage = findViewById(R.id.btnAddImage);
        btnAddImage.setOnClickListener(v -> checkCameraPermissionAndCapture());

        refreshList();
    }

    private void refreshList() {
        SampleImageAdapter adapter = new SampleImageAdapter(sampleImageDao.getAllSampleImages(), id -> {
            sampleImageDao.deleteSampleImage(Integer.parseInt(id));
            refreshList();
        });
        rvSampleImages.setAdapter(adapter);
    }

    private void checkCameraPermissionAndCapture() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
            return;
        }
        launchCamera();
    }

    private void launchCamera() {
        try {
            File photoFile = File.createTempFile("sample_", ".jpg", getExternalFilesDir("Pictures"));
            pendingPhotoUri = FileProvider.getUriForFile(this, "com.nibm.techfix.fileprovider", photoFile);
            takePictureLauncher.launch(pendingPhotoUri);
        } catch (IOException e) {
            Toast.makeText(this, "Couldn't open camera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void promptForCaption(Uri photoUri) {
        EditText input = new EditText(this);
        input.setHint("Caption (e.g. 'Cracked screen fixed')");

        new AlertDialog.Builder(this)
                .setTitle("Add Caption")
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    String caption = input.getText().toString().trim();
                    sampleImageDao.insertSampleImage(photoUri.toString(), caption.isEmpty() ? "Repair sample" : caption);
                    refreshList();
                })
                .setNegativeButton("Discard", null)
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST
                && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            launchCamera();
        } else {
            Toast.makeText(this, "Camera permission is needed to add a photo", Toast.LENGTH_SHORT).show();
        }
    }
}
