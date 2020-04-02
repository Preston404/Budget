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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Vector;

import com.example.preston.budget.ListActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class MainActivity extends AppCompatActivity {
    String db_name = "purchases";
    SQLiteDatabase sql_db;
    final int ALL_PURCHASES = 0;
    final int NEEDS_ONLY = 1;
    final int WANTS_ONLY = 2;
    final int ADD_ITEM_RET_OK = 69;
    final int EDIT_ITEM_RET_OK = 70;
    final int EDIT_ITEM_RET_DELETE = 71;
    final int NEEDS_RET_OK = 72;
    final int WANTS_RET_OK = 73;
    purchase_item last_purchase_clicked;
    int needs = 1;
    double monthly_budget_amount = 800;
    int monthly_budget_start_day = 25;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sql_db = openOrCreateDatabase(db_name, MODE_PRIVATE,null);
        //sql_db.execSQL("DROP TABLE IF EXISTS t0;");
        sql_db.execSQL("CREATE TABLE IF NOT EXISTS t0(price DOUBLE, description VARCHAR, date INTEGER, needs INTEGER);");

        init_textviews();

        // Stuff for "needs"
        final TextView needs = findViewById(R.id.needs);
        needs.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent launchNeeds = new Intent(v.getContext(), ListActivity.class);
                launchNeeds.putExtra("needs", 1);
                startActivityForResult(launchNeeds, NEEDS_RET_OK);
            }
        }
        );

        // Stuff for "wants"
        final TextView wants = findViewById(R.id.wants);
        wants.setOnClickListener(new View.OnClickListener(){
                                     @Override
                                     public void onClick(View v){
                Intent launchWants = new Intent(v.getContext(), ListActivity.class);
                launchWants.putExtra("needs", 0);
                startActivityForResult(launchWants, NEEDS_RET_OK);}
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
        if (resultCode == WANTS_RET_OK) {
            double total = data.getExtras().getDouble("total");
            String text = String.format(Locale.US, "$%.2f", total);
            ((TextView) findViewById(R.id.main_wants_amount)).setText(text);
        }
    }

    public void init_textviews(){

        TextView main_title = findViewById(R.id.main_title);
        main_title.setPaintFlags(main_title.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        TextView needs_total = findViewById(R.id.main_needs_amount);
        needs_total.setText(get_total_text(get_total_spent(NEEDS_ONLY)));

        TextView wants_total = findViewById(R.id.main_wants_amount);
        wants_total.setText(get_total_text(get_total_spent(WANTS_ONLY)));

        double goal_amount = get_main_monthly_goal_amount();
        double spent = get_total_spent(ALL_PURCHASES);
        set_main_remaining_amount(goal_amount - spent);

        int days_remaining = get_days_until_day_of_month(get_main_monthly_start_day());
        set_main_remaining_days(days_remaining);


    }

    public void insert_purchase(purchase_item p){
        String insert_cmd = String.format(Locale.US, "INSERT INTO t0 VALUES(%f, '%s', %d, %d);", p.price, p.description, p.date, p.need);
        sql_db.execSQL(insert_cmd);
    }

    public Vector<purchase_item> get_all_purchases(int filter){
        Cursor resultSet = sql_db.rawQuery("Select * from t0", null);
        if(!resultSet.moveToFirst()){
            return null;
        }
        Vector<purchase_item> all_purchases = new Vector<>();
        while(true){

            double price = Double.parseDouble(resultSet.getString(0));
            String description = resultSet.getString(1);
            int date = Integer.parseInt(resultSet.getString(2));
            int need = Integer.parseInt(resultSet.getString(3));
            purchase_item p = new purchase_item(price, description, date, need);
            if ((filter == NEEDS_ONLY && need == 1) || (filter == WANTS_ONLY && need == 0) || filter == ALL_PURCHASES){
                all_purchases.add(p);
            }
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
        purchase_item p = new purchase_item(price, description, id, needs);
        return p;
    }

    double get_total_spent(int filter){
        Vector<purchase_item> purchases = get_all_purchases(filter);
        double total = 0;
        while(purchases != null && !purchases.isEmpty()){
            total += purchases.get(0).price;
            purchases.remove(0);
        }
        return total;
    }

    public class purchase_item{
        double price = 0.69;
        String description = "Not Set";
        int date = 0;
        int need = 1;

        purchase_item(double price, String description, int date, int need){
            this.price = price;
            this.description = description;
            this.date = date;
            this.need = need;
        }
    }

    double get_main_monthly_goal_amount(){
        TextView t = findViewById(R.id.main_monthly_budget_amount);
        return Double.parseDouble(t.getText().toString().split(" ")[1].substring(1));
    }

    int get_main_monthly_start_day(){
        TextView t = findViewById(R.id.main_monthly_budget_start_day);
        String s = t.getText().toString().split(" ")[1];
        return Integer.parseInt(s.substring(0, s.length()-2));
    }

    void set_main_remaining_amount(double amount){
        TextView t = findViewById(R.id.main_remaining_amount);
        String text = String.format(Locale.US, "Amount: $%.2f", amount);
        t.setText(text);
    }

    void set_main_remaining_days(int days){
        TextView t = findViewById(R.id.main_remaining_days);
        String text = String.format(Locale.US, "Days: %d", days);
        t.setText(text);
    }

    int get_days_until_day_of_month(int day_of_month){
        long new_date = (new Date()).getTime();
        while ((new Date(new_date)).getDate() != day_of_month){
            new_date += 60*60*24*1000;
        };
        long ms_difference = new_date - (new Date()).getTime();
        int day_difference = (int) (ms_difference / (60*60*24*1000));
        return day_difference;
    }

    int get_seconds_from_ms(long ms){
        long seconds = (ms / (long) 1000.0);
        return (int) seconds;
    }

    String get_total_text(double d){
        return String.format(Locale.US,"$%.2f", d);
    }
}