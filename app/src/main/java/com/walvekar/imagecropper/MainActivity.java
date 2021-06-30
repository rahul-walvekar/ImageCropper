package com.walvekar.imagecropper;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.theartofdev.edmodo.cropper.CropImage;
import com.walvekar.imagecropper.databinding.ActivityMainBinding;

import java.io.IOException;

import static com.walvekar.imagecropper.FileHelper.getFileExtension;
import static com.walvekar.imagecropper.FileHelper.getFileName;
import static com.walvekar.imagecropper.FileHelper.getImageSize;
import static com.walvekar.imagecropper.FileHelper.saveFile;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    private Uri imageUrl;
    String fileSize;
    String fileName;
    String fileExtension;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Pick an Image
        binding.fabAdd.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            intentLauncher.launch(intent);
        });

        // Redirect to CropImage
        binding.fabEdit.setOnClickListener(view -> {
            // start picker to get image for cropping and then use the image in cropping activity
            if (imageUrl!=null){
                CropImage.activity(imageUrl).start(MainActivity.this);
            }
        });

        // Save the image
        binding.fabSave.setOnClickListener(view -> {
            // get the permissions
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        1);

                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        1);
                return;
            }

            // save to the downloads folder
            try {
                saveFile(MainActivity.this, imageUrl, fileName);
                Toast.makeText(this, "Image saved", Toast.LENGTH_SHORT).show();
            } catch (Exception e){
                Toast.makeText(this, "Some error occured", Toast.LENGTH_SHORT).show();
            }

        });
    }

    /*
    * Intent launcher to get Image Uri from storage
    * */
    ActivityResultLauncher<Intent> intentLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == Activity.RESULT_OK) {
                // There are no request codes
                Intent data = result.getData();
                imageUrl = data.getData();

                // get the size of the image
                try {
                    fileSize = getImageSize(MainActivity.this, imageUrl);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // get name and ext of image
                fileName = getFileName(getContentResolver(),imageUrl);
                fileExtension = getFileExtension(fileName);

                // set the data to the view
                binding.imageView.setImageURI(imageUrl);
                binding.fabEdit.setVisibility(View.VISIBLE);
                binding.textView.setText("Image name: " + fileName + "\n" + "Image size: "+ fileSize+ "\n"+ "Image extension: "+ fileExtension);
            }
        }
    });

    /*
    * To handle the activity result of CropImage
    * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                // get Uri
                imageUrl = result.getUri();

                // setting the result image
                binding.imageView.setImageURI(imageUrl);
                binding.fabSave.setVisibility(View.VISIBLE);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}