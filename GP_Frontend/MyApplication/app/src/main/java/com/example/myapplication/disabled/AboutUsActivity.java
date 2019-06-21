package com.example.myapplication.disabled;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.example.myapplication.R;

public class AboutUsActivity extends AppCompatActivity {
    private Button  home;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about__us_);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        home = (Button) findViewById(R.id.btn);

        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent in = new Intent(AboutUsActivity.this, DisabledHomeActivity.class);
                startActivity(in);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        finish();
        startActivity(new Intent(this, DisabledHomeActivity.class));
        return true;
    }
}
