package com.sudoku.sudokusolver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private Button takeSudokuPicture;
    private ImageView imageView;
    private TextView resultTextView;

    private Uri imageFile;

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (imageFile != null) {
            outState.putString("imageFilePath", imageFile.getPath());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (
                savedInstanceState != null &&
                        imageFile == null &&
                        savedInstanceState.getString("imageFilePath") != null
        ) {
            imageFile = Uri.parse(savedInstanceState.getString("imageFilePath"));
        }

        setContentView(R.layout.activity_main);

        takeSudokuPicture = findViewById(R.id.takeSudokuPicture);
        imageView = findViewById(R.id.imageView);
        resultTextView = findViewById(R.id.resultTextView);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            takeSudokuPicture.setEnabled(false);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
            takeSudokuPicture.setEnabled(true);
        }

    }

    public void takeSudokuPictureOnClick(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        imageFile = Uri.fromFile(getOutputMediaFile());
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageFile);

        startActivityForResult(intent, 7);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 7) {
            if (resultCode == RESULT_OK) {
                takeSudokuPicture.setEnabled(false);
                takeSudokuPicture.setVisibility(View.INVISIBLE);

                File imgFile = new File(imageFile.getPath());
                Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                Frame imageFrame = new Frame.Builder()
                        .setBitmap(bitmap)
                        .build();

                StringBuilder imageText = new StringBuilder();

                TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();

                SparseArray<TextBlock> textBlocks = textRecognizer.detect(imageFrame);

                for (int i = 0; i < textBlocks.size(); i++) {
                    TextBlock textBlock = textBlocks.get(textBlocks.keyAt(i));
                    imageText.append(textBlock.getValue());
                }
                resultTextView.setText(imageText.toString());

                if (imgFile.exists()) {
                    imageView.setImageURI(Uri.fromFile(imgFile));
                }
            }
        }
    }

    private static File getOutputMediaFile() {
        File mediaStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "SudokuPictures"
        );

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(new Date());
        return new File(
                mediaStorageDir.getPath() +
                        File.separator +
                        "IMG" +
                        timeStamp +
                        ".jpg"
        );
    }

}
