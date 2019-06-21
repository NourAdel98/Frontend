package com.example.myapplication.disabled;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.example.myapplication.LoginActivity;
import com.example.myapplication.R;

public class WaitingAcceptActivity extends AppCompatActivity {
    private Button ok;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting_accept_);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ok=(Button)findViewById(R.id.btn);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                Intent intent = new Intent(WaitingAcceptActivity.this,LoginActivity.class);
                startActivity(intent);
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
