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
import java.util.Vector;

import com.example.preston.budget.NeedsActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class MainActivity extends AppCompatActivity {
    String db_name = "purchases";
    SQLiteDatabase sql_db;
    final int ADD_ITEM_RET_OK = 69;
    final int EDIT_ITEM_RET_OK = 70;
    final int EDIT_ITEM_RET_DELETE = 71;
    final int NEEDS_RET_OK = 72;
    purchase_item last_purchase_clicked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sql_db = openOrCreateDatabase(db_name, MODE_PRIVATE,null);
        //sql_db.execSQL("DROP TABLE IF EXISTS t0;");
        sql_db.execSQL("CREATE TABLE IF NOT EXISTS t0(price DOUBLE, description VARCHAR, date INTEGER);");

        TextView main_title = findViewById(R.id.main_title);
        main_title.setPaintFlags(main_title.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        // Stuff for "needs"
        final TextView needs = findViewById(R.id.needs);
        needs.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent launchNeeds = new Intent(v.getContext(), NeedsActivity.class);
                startActivityForResult(launchNeeds, NEEDS_RET_OK);
            }
        }
        );

        // Stuff for "wants"
        final TextView wants = findViewById(R.id.wants);
        wants.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                 return;
            }
        }
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == NEEDS_RET_OK) {
            double total = data.getExtras().getDouble("total");
            String text = String.format(Locale.US, "$%.2f", total);
            ((TextView) findViewById(R.id.main_needs_amount)).setText(text);
        }
    }

    public void insert_purchase(purchase_item p){
        String insert_cmd = String.format(Locale.US, "INSERT INTO t0 VALUES(%f, '%s', %d);", p.price, p.description, p.date);
        sql_db.execSQL(insert_cmd);
    }

    public Vector<purchase_item> get_all_purchases(){
        Cursor resultSet = sql_db.rawQuery("Select * from t0", null);
        if(!resultSet.moveToFirst()){
            return null;
        }
        Vector<purchase_item> all_purchases = new Vector<>();

        while(true){

            double price = Double.parseDouble(resultSet.getString(0));
            String description = resultSet.getString(1);
            int date = Integer.parseInt(resultSet.getString(2));
            purchase_item p = new purchase_item(price, description, date);
            all_purchases.add(p);
            if (resultSet.isLast()){
                break;
            }
            resultSet.moveToNext();
        }
        return all_purchases;
    }

    public boolean remove_purchase_by_id(int id){
        Cursor resultSet = sql_db.rawQuery("Select * from t0", null);
        if(!resultSet.moveToFirst()){
            return false;
        }
        while(true){
            String date_string = resultSet.getString(2);
            int date = Integer.parseInt(date_string);
            if (date == id){
                String cmd = String.format(Locale.US, "DELETE FROM t0 WHERE date = %d", date);
                sql_db.execSQL(cmd);
                Toast.makeText(this, "Purchase Removed", Toast.LENGTH_SHORT).show();
                return true;
            }
            if (resultSet.isLast()){
                break;
            }
            resultSet.moveToNext();
        }
        return false;
    }

    int get_seconds_from_ms(long ms){
        long seconds = (ms / (long) 1000.0);
        return (int) seconds;
    }

    purchase_item get_purchase_item_from_view(TextView v){
        String description = "";
        double price = 0;
        try {
            description = ((String) v.getText()).split("\\r?\\n")[1];
            String first_line = ((String) v.getText()).split("\\r?\\n")[0];
            String without_dollar_sign = first_line.substring(1);
            price = Double.parseDouble(without_dollar_sign);
        }
        catch (Exception e){
            String msg = String.format(Locale.US, "Parse failed on string '%s'", v.getText());
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        };
        int id = v.getId();
        purchase_item p = new purchase_item(price, description, id);
        return p;
    }

    public class purchase_item{
        double price = 0.69;
        String description = "Not Set";
        int date = 0;

        purchase_item(double price, String description, int date){
            this.price = price;
            this.description = description;
            this.date = date;
        }
    }
}