package com.example.pytorchandroid;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.widget.ImageView;
import android.widget.TextView;

public class ClassificationResult extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        byte[] byteArray = getIntent().getByteArrayExtra("imagedata");
        Bitmap imageBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        String pred = getIntent().getStringExtra("pred");
        String elapsedTime = getIntent().getStringExtra("elapsed");
        ImageView imageView = findViewById(R.id.image);
        imageView.setImageBitmap(imageBitmap);
        TextView textView = findViewById(R.id.label);
        String elapsedTimeText = pred +  " Time: " + elapsedTime + " [ms]";
        textView.setText(elapsedTimeText);
    }
}