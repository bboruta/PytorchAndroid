package com.example.pytorchandroid;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    int cameraRequestCode = 001;
    int pickImageCode = 100;

    Classifier classifier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        classifier = new Classifier(Utils.assetFilePath(this,"mobilenet-v2.pt"));

        Button capture = findViewById(R.id.capture);
        capture.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent,cameraRequestCode);
            }
        });

        Button load = findViewById(R.id.load);
        load.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(gallery, pickImageCode);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == cameraRequestCode && resultCode == RESULT_OK) {

            Intent resultView = new Intent(this, ClassificationResult.class);
            Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            resultView.putExtra("imagedata", byteArray);
            long startTime = StopWatch.now();
            String pred = classifier.predict(imageBitmap);
            long stopTime = StopWatch.now();
            long elapsedTime = StopWatch.getElapsedTime(startTime, stopTime);
            resultView.putExtra("pred", pred);
            resultView.putExtra("elapsed", String.valueOf(elapsedTime));

            startActivity(resultView);
        }
        else if (requestCode == pickImageCode  && resultCode == RESULT_OK) {
            Bitmap imageBitmap = null;
            byte[] byteArray = null;
            try {
                Uri imageUri = data.getData();
                InputStream imageStream = getContentResolver().openInputStream(imageUri);
                byte[] bt = getBytes(imageStream);
                imageBitmap = BitmapFactory.decodeByteArray(bt, 0, bt.length);
                int newWidth = imageBitmap.getWidth() / 8;
                int newHeight = imageBitmap.getHeight() / 8;
                imageBitmap = Bitmap.createScaledBitmap(
                        imageBitmap,
                        newWidth,
                        newHeight,
                        false);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byteArray = stream.toByteArray();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Intent resultView = new Intent(this, ClassificationResult.class);
            resultView.putExtra("imagedata", byteArray);
            long startTime = StopWatch.now();
            String pred = classifier.predict(imageBitmap);
            long stopTime = StopWatch.now();
            long elapsedTime = StopWatch.getElapsedTime(startTime, stopTime);
            resultView.putExtra("pred", pred);
            resultView.putExtra("elapsed", String.valueOf(elapsedTime));
            startActivity(resultView);
        }
    }

    private byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }
}