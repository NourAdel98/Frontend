package com.example.myapplication.disabled;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Button;

import com.example.myapplication.R;
import com.example.myapplication.disabled.DisabledHomeActivity;
import com.example.myapplication.request.ViewHistoryActivity;

public class GuidesActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guides);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        finish();
        startActivity(new Intent(this, DisabledHomeActivity.class));
        return true;
    }
}
