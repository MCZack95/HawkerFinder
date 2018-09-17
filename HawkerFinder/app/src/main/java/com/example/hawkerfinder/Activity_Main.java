package com.example.hawkerfinder;

import android.app.Dialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class Activity_Main extends AppCompatActivity {

    DatabaseHelper myDb;
    Button mapsButton,browseButton;
    TextView aboutUsTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myDb = new DatabaseHelper(this);
        myDb.preLoadData();

        mapsButton = findViewById(R.id.search_button);
        browseButton = findViewById(R.id.stall_button);
        aboutUsTextView = findViewById(R.id.about);

        mapsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goIntent = new Intent(v.getContext(),Activity_Maps.class);
                startActivity(goIntent);
            }
        });

        browseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goIntent = new Intent(v.getContext(),Activity_Stall.class);
                startActivity(goIntent);
            }
        });

        aboutUsTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog myDialog = new Dialog(Activity_Main.this);
                myDialog.setContentView(R.layout.dialog_about_us);
                myDialog.setCanceledOnTouchOutside(true);
                myDialog.show();
            }
        });
    }
}
