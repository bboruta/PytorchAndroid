package com.example.pytorchandroid;

import android.content.ClipData;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    int cameraRequestCode = 001;
    int pickImageCode = 100;
    ArrayList<Integer> elapsedTimeForPredictions = new ArrayList<>();

    Classifier classifier;

    TextView averageTimeTextView;
    TextView logTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        classifier = new Classifier(Utils.assetFilePath(this,"mobilenet-v2.pt"));

        averageTimeTextView = findViewById(R.id.averageTime);
        logTextView = findViewById(R.id.logTextView);
        logTextView.setMovementMethod(new ScrollingMovementMethod());

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
                gallery.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                startActivityForResult(gallery, pickImageCode);
            }
        });

        Button clearLog = findViewById(R.id.clearButton);
        clearLog.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                averageTimeTextView.setText("");
                logTextView.setText("");
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Intent resultView = new Intent(this, ClassificationResult.class);
        Bitmap imageBitmap = null;

        if (requestCode == cameraRequestCode && resultCode == RESULT_OK) {
            imageBitmap = (Bitmap) data.getExtras().get("data");
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
            startPrediction(imageBitmap, resultView);
        }
        else if (requestCode == pickImageCode  && resultCode == RESULT_OK) {
            byte[] byteArray = null;
            try {
                Uri imageUri = data.getData();
                List<Uri> uris = new ArrayList<>();
                ClipData clipData = data.getClipData();
                if (clipData != null) {
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        ClipData.Item item = clipData.getItemAt(i);
                        Uri uri = item.getUri();
                        uris.add(uri);
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
                        startPrediction(imageBitmap, resultView);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            // showing only the last image from the set
            resultView.putExtra("imagedata", byteArray);
        }
        startActivity(resultView);
    }

    private void startPrediction(Bitmap imageBitmap, Intent resultView) {
        long startTime = StopWatch.now();
        String pred = classifier.predict(imageBitmap);
        long stopTime = StopWatch.now();
        int elapsedTime = (int) StopWatch.getElapsedTime(startTime, stopTime);
        resultView.putExtra("pred", pred);
        resultView.putExtra("elapsed", String.valueOf(elapsedTime));
        elapsedTimeForPredictions.add(elapsedTime);
        logTextView.append("Time for predition: " + elapsedTime + "[ms]\n");
        int mean = mean(elapsedTimeForPredictions);
        averageTimeTextView.setText("Average time: " + mean + "[ms] for " + elapsedTimeForPredictions.size() + " predictions.");
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

    private int sum(ArrayList<Integer> list) {
        int sum = 0;
        for (int i = 0; i < list.size(); i++)
        {
            sum += list.get(i);
        }
        return sum;
    }

    private int mean(ArrayList<Integer> list) {
        return sum(list) / list.size();
    }
}