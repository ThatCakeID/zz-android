package com.thatcakeid.zrytezene;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

public class ProfileActivity extends AppCompatActivity {

    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");
    }
}