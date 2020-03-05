package com.example.preston.budget;

import android.content.Intent;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import com.example.preston.budget.NeedsActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Stuff for "needs"
        final TextView needs = findViewById(R.id.needs);
        needs.setPaintFlags(needs.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        needs.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                needs.setText("clicked");
                Intent launchNeeds = new Intent(v.getContext(), NeedsActivity.class);
                startActivity(launchNeeds);
            }
        }
        );

        // Stuff for "wants"
        final TextView wants = findViewById(R.id.wants);
        wants.setPaintFlags(wants.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        wants.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                 wants.setText("clicked");
            }
        }
        );
    }

}