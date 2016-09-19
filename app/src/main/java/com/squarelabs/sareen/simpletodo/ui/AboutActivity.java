package com.squarelabs.sareen.simpletodo.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.squarelabs.sareen.simpletodo.R;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        getSupportActionBar().setElevation(0f);
    }
}
