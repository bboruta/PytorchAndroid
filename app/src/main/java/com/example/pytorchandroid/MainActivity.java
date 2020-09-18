package com.example.pytorchandroid;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.File;

import javax.xml.transform.Result;

public class MainActivity extends AppCompatActivity {

    int cameraRequestCode = 001;

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

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == cameraRequestCode && resultCode == RESULT_OK) {

            Intent resultView = new Intent(this, ClassificationResult.class);

            resultView.putExtra("imagedata", data.getExtras());

            Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");

            long startTime = StopWatch.now();
            String pred = classifier.predict(imageBitmap);
            long stopTime = StopWatch.now();
            long elapsedTime = StopWatch.getElapsedTime(startTime, stopTime);
            resultView.putExtra("pred", pred);
            resultView.putExtra("elapsed", String.valueOf(elapsedTime));

            startActivity(resultView);

        }

    }

}