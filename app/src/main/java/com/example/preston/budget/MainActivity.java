package com.example.preston.budget;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class MainActivity extends AppCompatActivity {
    FirebaseDatabase fb_database = FirebaseDatabase.getInstance();
    SQLiteDatabase sql_db;
    String db_name = "purchases";
    int ADD_ITEM_RET_OK = 69;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sql_db = openOrCreateDatabase(db_name, MODE_PRIVATE,null);
        sql_db.execSQL("CREATE TABLE IF NOT EXISTS P1(Price DOUBLE, Description VARCHAR);");
        sql_db.execSQL("INSERT INTO P1 VALUES(2.50,'None');");

        Cursor resultSet = sql_db.rawQuery("Select * from P1", null);
        resultSet.moveToFirst();
        Toast.makeText(this, resultSet.getString(0), Toast.LENGTH_LONG).show();
        Toast.makeText(this, resultSet.getString(1), Toast.LENGTH_LONG).show();

        // Stuff for "needs"
        final TextView needs = findViewById(R.id.needs);
        needs.setPaintFlags(needs.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        needs.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
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
                 return;
            }
        }
        );
    }

    public class purchase_item{
        double price;
        String description;
        int year;
        int month;
        int day_of_month;
    }

}