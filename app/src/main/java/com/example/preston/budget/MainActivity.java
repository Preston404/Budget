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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import com.example.preston.budget.NeedsActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class MainActivity extends AppCompatActivity {
    FirebaseDatabase fb_database = FirebaseDatabase.getInstance();
    String db_name = "purchases";
    SQLiteDatabase sql_db;
    int ADD_ITEM_RET_OK = 69;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sql_db = openOrCreateDatabase(db_name, MODE_PRIVATE,null);
        sql_db.execSQL("DROP TABLE IF EXISTS t0;");
        insert_purchase(3.50, "nessy");
        insert_purchase(3.75, "loch");
        insert_purchase(2.36, "monster");
        get_all_purchases();


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

    public void insert_purchase(double price, String description){

        sql_db.execSQL("CREATE TABLE IF NOT EXISTS t0(price DOUBLE, description VARCHAR, date VARCHAR);");

        long date_long = (new Date()).getTime();
        String insert_cmd = String.format(Locale.US, "INSERT INTO t0 VALUES(%f, '%s', '%s');", price, description, Long.toString(date_long));
        sql_db.execSQL(insert_cmd);

        String date_string = (new Date(date_long)).toString();
        String msg = String.format("Inserted purchase at: %s", date_string);
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    public void get_all_purchases(){
        Cursor resultSet = sql_db.rawQuery("Select * from t0", null);
        resultSet.moveToFirst();
        while(true){
            String price = resultSet.getString(0);
            String description = resultSet.getString(1);
            String date = (new Date(Long.parseLong(resultSet.getString(2)))).toString();
            String msg = String.format("Price: %s, Description: '%s', Date: '%s'", price, description, date);
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            if (resultSet.isLast()){
                break;
            }
            resultSet.moveToNext();
        }
    }

    public class purchase_item{
        double price = 0.69;
        String description = "Not Set";
    }

}