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
import java.util.Collections;
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
    final int PERIODS_THIS = 0;

    final int ADD_ITEM_RET_OK = 69;
    final int EDIT_ITEM_RET_OK = 70;
    final int EDIT_ITEM_RET_DELETE = 71;
    final int NEEDS_RET_OK = 72;
    final int WANTS_RET_OK = 73;
    final int EDIT_GOAL_RET_OK = 74;


    purchase_item last_purchase_clicked;
    int needs = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sql_db = openOrCreateDatabase(db_name, MODE_PRIVATE,null);
        //sql_db.execSQL("DROP TABLE IF EXISTS t0;");
        sql_db.execSQL("CREATE TABLE IF NOT EXISTS t0(price DOUBLE, description VARCHAR, date INTEGER, needs INTEGER);");
        sql_db.execSQL("CREATE TABLE IF NOT EXISTS c0(monthly_goal_amount DOUBLE, start_day INTEGER);");

        update_textviews();

        final TextView goals = findViewById(R.id.main_monthly_budget_title);
        goals.setOnClickListener(new View.OnClickListener(){
                                     @Override
                                     public void onClick(View v){
                                         Intent launchWants = new Intent(v.getContext(), EditGoal.class);
                                         startActivityForResult(launchWants, EDIT_GOAL_RET_OK);}
                                 }
        );

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
                startActivityForResult(launchWants, WANTS_RET_OK);}
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
            update_textviews();
        }
        else if (resultCode == WANTS_RET_OK) {
            double total = data.getExtras().getDouble("total");
            String text = String.format(Locale.US, "$%.2f", total);
            ((TextView) findViewById(R.id.main_wants_amount)).setText(text);
            update_textviews();
        }
        else if (requestCode == EDIT_GOAL_RET_OK){
            update_textviews();
        }
    }

    public void update_textviews(){

        TextView main_title = findViewById(R.id.main_title);
        main_title.setPaintFlags(main_title.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        goal_config c = read_goal_config_from_db();

        set_main_monthly_goal_amount(c.monthly_goal_amount);
        set_main_monthly_start_day(c.start_day);


        TextView needs_total = findViewById(R.id.main_needs_amount);
        needs_total.setText(get_list_total_string(get_total_spent(NEEDS_ONLY, PERIODS_THIS)));

        TextView wants_total = findViewById(R.id.main_wants_amount);
        wants_total.setText(get_list_total_string(get_total_spent(WANTS_ONLY, PERIODS_THIS)));

        double spent = get_total_spent(ALL_PURCHASES, PERIODS_THIS);
        set_main_remaining_amount(c.monthly_goal_amount - spent);

        int days_remaining = get_days_until_day_of_month(c.start_day);
        set_main_remaining_days(days_remaining);

    }

    public void insert_config_into_db(goal_config c){
        // Always overwrite the previous table
        sql_db.execSQL("DROP TABLE IF EXISTS c0;");
        sql_db.execSQL("CREATE TABLE IF NOT EXISTS c0(monthly_goal_amount DOUBLE, start_day INTEGER);");
        String insert_cmd = String.format(Locale.US, "INSERT INTO c0 VALUES(%.2f, %d);", c.monthly_goal_amount, c.start_day);
        sql_db.execSQL(insert_cmd);
    }

    public goal_config read_goal_config_from_db(){
        Cursor resultSet = sql_db.rawQuery("Select * from c0", null);
        if(!resultSet.moveToFirst()){
            goal_config c = new goal_config(800,25);
            return c;
        }
        double monthly_goal_amount  = Double.parseDouble(resultSet.getString(0));
        int start_day = Integer.parseInt(resultSet.getString(1));
        goal_config c = new goal_config(monthly_goal_amount, start_day);
        return c;
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

    double get_total_spent(int filter, int period){
        Vector<purchase_item> purchases = get_all_purchases(filter);
        if (period == PERIODS_THIS){
            purchases = filter_purchases_this_period(purchases);
        }
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

    public class goal_config{
        double monthly_goal_amount = 0.0;
        int start_day = 25;
        goal_config(double monthly_goal_amount, int start_day){
            this.monthly_goal_amount = monthly_goal_amount;
            this.start_day = start_day;
        }
    }

    void set_main_monthly_goal_amount(double d){
        TextView t = findViewById(R.id.main_monthly_budget_amount);
        t.setText(get_monthly_goal_amount_string(d));
    }

    void set_main_monthly_start_day(int day){
        TextView t = findViewById(R.id.main_monthly_budget_start_day);
        t.setText(get_monthly_start_day_string(day));
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

    int get_days_since_day_of_month(int day_of_month){
        long old_date = (new Date()).getTime();
        while ((new Date(old_date)).getDate() != day_of_month){
            old_date -= 60*60*24*1000;
        };
        long ms_difference = (new Date()).getTime() - old_date;
        int day_difference = (int) (ms_difference / (60*60*24*1000));
        return day_difference;
    }

    int get_seconds_from_ms(long ms){
        long seconds = (ms / (long) 1000.0);
        return (int) seconds;
    }

    Vector<purchase_item> sort_purchases_newest_first(Vector<purchase_item> purchases){
        if (purchases == null){return null;}
        boolean sorted = false;
        while(!sorted) {
            sorted = true;
            for (int i = 0; i < purchases.size() - 1; i++) {
                if (purchases.elementAt(i).date < purchases.elementAt(i + 1).date) {
                    Collections.swap(purchases, i, i + 1);
                    sorted = false;
                }
            }
        }
        return purchases;
    }

    Vector<purchase_item> filter_purchases_this_period(Vector<purchase_item> purchases){
        if (purchases == null){return null;}
        Vector<purchase_item> purchases_this_month = new Vector<>();
        goal_config c = read_goal_config_from_db();
        Date d = new Date();
        int days_since_start = get_days_since_day_of_month(c.start_day);
        int seconds_since_start = days_since_start*24*60*60;
        for (int i = 0; i < purchases.size(); i++) {
            if(purchases.elementAt(i).date - get_seconds_from_ms(d.getTime()) < seconds_since_start){
                purchases_this_month.add(purchases.elementAt(i));
            }
        }
        if (purchases_this_month.size() == 0){return null;}
        return purchases;
    }

    String get_list_total_string(double d){return String.format(Locale.US,"$%.2f", d);}
    String get_monthly_start_day_string(int day){return String.format(Locale.US,"Start Day: %dth", day);}
    String get_monthly_goal_amount_string(double amount){return String.format(Locale.US,"Amount: $%.2f", amount);}
}