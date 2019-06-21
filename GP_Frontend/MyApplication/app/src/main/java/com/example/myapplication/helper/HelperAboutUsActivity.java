package com.example.myapplication.helper;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.example.myapplication.R;

public class HelperAboutUsActivity extends AppCompatActivity {
    private Button  home;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_helper_about__us);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        home = (Button) findViewById(R.id.btn);

        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent in = new Intent(HelperAboutUsActivity.this, HelperHomeActivity.class);
                startActivity(in);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        finish();
        startActivity(new Intent(this, HelperHomeActivity.class));
        return true;
    }
}
