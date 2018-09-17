package com.example.hawkerfinder;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

public class Activity_Stall extends Activity {

    DatabaseHelper myDb;
    Button backButton;
    ListView mListView;
    Adapter_Stall mAdapterStall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stall);
        myDb = new DatabaseHelper(this);

        mListView = findViewById(R.id.stall_list_view);
        backButton = findViewById(R.id.back_button);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mAdapterStall = new Adapter_Stall(getApplicationContext(),R.layout.row_stall,myDb.getAllStallName());

        if(mListView != null){
            mListView.setAdapter(mAdapterStall);
        }
    }

}

