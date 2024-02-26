package com.example.preston.budget;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


public class Utils extends AppCompatActivity
{

    String db_name = "purchases";
    SQLiteDatabase sql_db;

    final int ms_in_a_day = 60*60*24*1000;
    final int seconds_in_a_day = 60*60*24;

    final int FILTER_ALL = 2;
    final int FILTER_NEEDS_ONLY = 0;
    final int FILTER_WANTS_ONLY = 1;

    final int PERIODS_THIS = 0;

    final int NEEDS_LIST_VIEW = FILTER_NEEDS_ONLY;
    final int WANTS_LIST_VIEW = FILTER_WANTS_ONLY;
    final int ALL_LIST_VIEW = FILTER_ALL;

    final String[] SORT_OPTIONS = {"None", "Min to Max", "Max to Min"};

    final int GET_DATE_RET_OK = 67;
    final int GET_FILTER_RET_OK = 68;
    final int ADD_ITEM_RET_OK = 69;
    final int EDIT_ITEM_RET_OK = 70;
    final int EDIT_ITEM_RET_DELETE = 71;
    final int NEEDS_RET_OK = 72;
    final int WANTS_RET_OK = 73;
    final int EDIT_SETTINGS_RET_OK = 74;
    final int ALL_RET_OK = 75;
    final int GRAPH_RET_OK = 76;
    final int EDIT_CATEGORIES_RET_OK = 77;

    purchase_item last_purchase_clicked;
    // The needs field in the database should really be stored
    // as a boolean, but these variables are used for now...
    int IS_A_NEED = 1;
    int IS_NOT_A_NEED = 0;

    int list_view_type = FILTER_NEEDS_ONLY;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    public void try_init_databases()
    {
        sql_db = openOrCreateDatabase(db_name, MODE_PRIVATE, null);
        //sql_db.execSQL("DROP TABLE IF EXISTS t0;");
        sql_db.execSQL("CREATE TABLE IF NOT EXISTS t0(price DOUBLE, description VARCHAR, date INTEGER, needs INTEGER);");
        //sql_db.execSQL("DROP TABLE IF EXISTS c0;");
        sql_db.execSQL("CREATE TABLE IF NOT EXISTS c0(monthly_goal_amount DOUBLE, start_day INTEGER, text_size INTEGER, show_remaining INTEGER);");

        sql_db.execSQL("CREATE TABLE IF NOT EXISTS f0(max_price DOUBLE, min_price DOUBLE, text VARCHAR, start_day INTEGER, end_day INTEGER, type INTEGER, sort INTEGER);");
        sql_db.close();
    }

    public void insert_config_into_db(settings_config c)
    {
        // Always overwrite the previous table
        sql_db = openOrCreateDatabase(db_name, MODE_PRIVATE, null);
        sql_db.execSQL("DROP TABLE IF EXISTS c0;");
        sql_db.execSQL("CREATE TABLE IF NOT EXISTS c0(monthly_goal_amount DOUBLE, start_day INTEGER, text_size INTEGER, show_remaining INTEGER);");
        String insert_cmd = String.format(
                Locale.US,
                "INSERT INTO c0 VALUES(%.2f, %d, %d, %d);",
                c.monthly_goal_amount,
                c.start_day,
                c.text_size,
                c.show_remaining
        );
        sql_db.execSQL(insert_cmd);
        sql_db.close();
    }

    public settings_config read_settings_config_from_db()
    {
        try_init_databases();
        sql_db = openOrCreateDatabase(db_name, MODE_PRIVATE, null);
        Cursor resultSet = sql_db.rawQuery("Select * from c0", null);
        if(!resultSet.moveToFirst())
        {
            // Config not found, return default config
            settings_config c = new settings_config(
                800,
                25,
                25,
                1
            );
            sql_db.close();
            return c;
        }

        double monthly_goal_amount  = Double.parseDouble(resultSet.getString(0));
        int start_day = Integer.parseInt(resultSet.getString(1));
        int text_size = Integer.parseInt(resultSet.getString(2));
        int show_remaining = Integer.parseInt(resultSet.getString(3));
        settings_config c = new settings_config(
            monthly_goal_amount,
            start_day,
            text_size,
            show_remaining
        );
        sql_db.close();
        return c;
    }

    public void insert_category_into_db(String category)
    {
        sql_db = openOrCreateDatabase(db_name, MODE_PRIVATE, null);
        sql_db.execSQL("CREATE TABLE IF NOT EXISTS categories(this_category String);");

        List<String> current_categories = read_categories_from_db();
        if(current_categories != null)
        {
            for (int i = 0; i < current_categories.size(); i++) {
                if (current_categories.get(i).equals(category)) {
                    sql_db.close();
                    return; // ALready in the db
                }
            }
        }

        String insert_cmd = String.format(
                Locale.US,
                "INSERT INTO categories VALUES('%s');",
                category
        );
        sql_db.execSQL(insert_cmd);
        sql_db.close();
    }

    public void remove_category_from_db(String category)
    {
        sql_db = openOrCreateDatabase(db_name, MODE_PRIVATE, null);
        sql_db.execSQL("CREATE TABLE IF NOT EXISTS categories(this_category String);");

        String delete_cmd = String.format(
                Locale.US,
                "DELETE FROM categories WHERE this_category='%s';",
                category
        );
        sql_db.execSQL(delete_cmd);
        sql_db.close();
    }

    public List<String> read_categories_from_db()
    {
        sql_db = openOrCreateDatabase(db_name, MODE_PRIVATE, null);
        sql_db.execSQL("CREATE TABLE IF NOT EXISTS categories(this_category String);");
        Cursor resultSet = sql_db.rawQuery("Select * from categories", null);
        if(!resultSet.moveToFirst())
        {
            resultSet.close();
            sql_db.close();
            return null;
        }
        List<String> the_categories = new ArrayList<String>();
        do
        {
            String a_category = resultSet.getString(0);
            the_categories.add(a_category);
            resultSet.moveToNext();
        }
        while(!resultSet.isAfterLast());
        resultSet.close();
        sql_db.close();
        return the_categories;
    }

    public Vector<purchase_item> read_purchases_from_local_db(boolean this_period)
    {
        try_init_databases();
        String sql_query = "Select * from t0";
        if(this_period)
        {
            int seconds_since_start = get_seconds_since_period_start();
            int lowest_num_seconds = get_seconds_from_ms(new Date().getTime()) - seconds_since_start;

            sql_query = String.format(
                Locale.US,
                "Select * from t0 Where cast(date as INTEGER) > %d",
                lowest_num_seconds
            );

        }

        sql_db = openOrCreateDatabase(db_name, MODE_PRIVATE, null);
        Cursor resultSet = sql_db.rawQuery(sql_query, null);
        if(!resultSet.moveToFirst())
        {
            resultSet.close();
            sql_db.close();
            return null;
        }
        Vector<purchase_item> the_purchases = new Vector<purchase_item>();
        do
        {
            double price  = Double.parseDouble(resultSet.getString(0));
            String description = resultSet.getString(1);
            int date = Integer.parseInt(resultSet.getString(2));
            int need = Integer.parseInt(resultSet.getString(3));
            String category = "";
            purchase_item p = new purchase_item(
                    price,
                    description,
                    date, // also the purchase ID
                    need,
                    category
            );
            the_purchases.add(p);
            resultSet.moveToNext();
        }
        while(!resultSet.isAfterLast());
        resultSet.close();
        sql_db.close();
        return the_purchases;
    }

    public boolean local_db_contains_purchase_id(int id)
    {
        try_init_databases();
        boolean contains_id = false;

        sql_db = openOrCreateDatabase(db_name, MODE_PRIVATE, null);
        String sql_query = String.format(
            Locale.US,
            "Select * from t0 where cast(date as INTEGER) = %d",
            id
        );
        Cursor resultSet = sql_db.rawQuery(sql_query, null);
        if (resultSet.moveToFirst())
        {
            // An entry with this date/id is already present in the local db
            contains_id = true;
        }
        else
        {
            Toast.makeText(this, "no purchase ID found.", Toast.LENGTH_LONG).show();
        }
        resultSet.close();
        sql_db.close();
        return contains_id;
    }

    public purchase_item read_purchase_item_from_local_db(int id)
    {
        try_init_databases();
        purchase_item p = null;

        sql_db = openOrCreateDatabase(db_name, MODE_PRIVATE, null);
        String sql_query = String.format(
            Locale.US,
            "Select * from t0 Where cast(date as INTEGER) = %d",
            id
        );
        Cursor resultSet = sql_db.rawQuery(sql_query, null);
        if (resultSet.moveToFirst())
        {
            double price  = Double.parseDouble(resultSet.getString(0));
            String description = resultSet.getString(1);
            int date = Integer.parseInt(resultSet.getString(2));
            int need = Integer.parseInt(resultSet.getString(3));
            String category = "";
            p = new purchase_item(
                    price,
                    description,
                    date, // also the purchase ID
                    need,
                    category
            );
        }
        resultSet.close();
        sql_db.close();

        return p;
    }

    public int insert_purchase_into_local_db(purchase_item p, boolean do_overwrite)
    {
        try_init_databases();

        while(local_db_contains_purchase_id(p.date))
        {
            if(do_overwrite)
            {
                remove_purchase_from_local_db(p.date);
                break;
            }
            else
            {
                // Push the date/ID field forward to make it unique.
                p.date = p.date + 1;
                Toast.makeText(this, "Purchase ID Adjusted", Toast.LENGTH_SHORT).show();
            }
        }

        sql_db = openOrCreateDatabase(db_name, MODE_PRIVATE, null);
        sql_db.execSQL(
            "INSERT INTO t0 (price, description, date, needs) VALUES(?, ?, ?, ?);",
            new Object[]
                {
                    p.price,
                    p.description,
                    p.date,
                    p.need
                }
        );
        sql_db.close();
        return p.date;
    }

    public void remove_purchase_from_local_db(int id)
    {
        try_init_databases();
        sql_db = openOrCreateDatabase(db_name, MODE_PRIVATE, null);

        String delete_cmd = String.format(
            Locale.US,
            "DELETE FROM t0 WHERE date=%d",
            id
        );
        sql_db.execSQL(delete_cmd);
        sql_db.close();
    }

    // The date doubles as the purchase ID, so we'll push the
    // date forward a second if it is not unique. Returns the date
    // used as the ID
    /*
    public int insert_purchase(purchase_item p)
    {
        boolean adjusted_date = false;
        while (db_contains_purchase_with_id(p.date))
        {
            adjusted_date = true;
            p.date = p.date + 1;
        }
        if(adjusted_date)
        {
            Toast.makeText(this, "Purchase ID Adjusted", Toast.LENGTH_SHORT).show();
        }
        write_firebase(p, this, true);
        return p.date;
    }

    boolean db_contains_purchase_with_id(int id)
    {
        //Vector<purchase_item> purchases = read_firebase(this, null, FILTER_ALL);
        Vector<purchase_item> purchases = read_purchases_from_local_db(false);
        if(purchases.isEmpty())
        {
            return false;
        }
        else
        {
            for(purchase_item p : purchases)
            {
                // The date field doubles as an ID
                if(p.date == id)
                {
                    return true;
                }
            }
        }
        return false;
    }
    */

    public Vector<purchase_item> get_all_purchases(int filter, boolean filter_this_period)
    {
        //Vector<purchase_item> all_purchases = read_firebase(this, null, filter_need);
        Vector<purchase_item> all_purchases = read_purchases_from_local_db(filter_this_period);
        if (all_purchases.isEmpty())
        {
            all_purchases = null;
        }

        Vector<purchase_item> filtered_purchases = new Vector<purchase_item>();
        for(int i=0; i<all_purchases.size();i++) {
            purchase_item this_purchase = all_purchases.elementAt(i);
            if (filter == FILTER_NEEDS_ONLY && this_purchase.need == IS_A_NEED)
            {
                filtered_purchases.add(this_purchase);
            }
            else if (filter == FILTER_WANTS_ONLY && this_purchase.need == IS_NOT_A_NEED)
            {
                filtered_purchases.add(this_purchase);
            }
            else if (filter == FILTER_ALL)
            {
                filtered_purchases.add(this_purchase);
            }
        }
        return filtered_purchases;
    }


    /*
    public boolean remove_purchase_by_id(int id)
    {
        Task task = db.collection("purchases").document(Integer.toString(id)).delete();
        while(!task.isComplete());
        if(!task.isSuccessful())
        {
            Toast.makeText(this, "Failed to Delete from DB.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
    */

    int get_need_type_from_id(int id, Map<Integer,Integer> id_to_needs_map)
    {
        int need = -1;
        if(id_to_needs_map != null && id_to_needs_map.containsKey(id))
        {
            need = id_to_needs_map.get(id);
        }
        else
        {
            purchase_item p = read_purchase_item_from_local_db(id);
            if (p != null) {
                need = p.need;
            }
        }
        return need;
    }

    purchase_item get_purchase_item_from_view(TextView v, Map<Integer, Integer> id_to_needs_map)
    {
        String description = "";
        double price = 0;
        String all_text = "N/A";
        String category = "";
        try
        {
            // First char should be '$' so skip it
            all_text = ((String) v.getText()).substring(1);

            String lines[] = all_text.split("\\r?\\n");
            price = Double.parseDouble(lines[0]);
            description = lines[1];

            if (lines.length > 2)
            {
                category = lines[2];
            }
        }
        catch (Exception e)
        {
            if (v != null)
            {
                String msg = String.format(
                        Locale.US,
                        "Parse failed on string '%s'",
                        all_text
                );
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(this, "Error, Cannot convert null view", Toast.LENGTH_SHORT).show();
            }
            return null;
        }

        int id = v.getId();
        int need = get_need_type_from_id(id, id_to_needs_map);
        if(need != IS_A_NEED && need != IS_NOT_A_NEED)
        {
            Toast.makeText(this, "Could not find need type for view.", Toast.LENGTH_SHORT).show();
            return null;
        }
        purchase_item p = new purchase_item(
                price,
                description,
                id,
                need,
                category
        );
        return p;
    }


    double get_total_spent(int filter, int period)
    {
        Vector<purchase_item> purchases = get_all_purchases(filter, period == PERIODS_THIS);

        double total = 0;
        while(purchases != null && !purchases.isEmpty())
        {
            total += purchases.get(0).price;
            purchases.remove(0);
        }
        return total;
    }


    public class purchase_item
    {
        double price = 0.69;
        String description = "Not Set";
        int date = 0;
        int need = 1;
        String category = "";

        purchase_item(double price, String description, int date, int need, String category)
        {
            this.price = price;
            this.description = description;
            this.date = date;
            this.need = need;
            this.category = category;
        }
    }


    public class settings_config
    {
        double monthly_goal_amount = 0.0;
        int start_day = 25;
        int text_size = 25;
        int show_remaining = 1;

        settings_config(
            double monthly_goal_amount,
            int start_day,
            int text_size,
            int show_remaining
        )
        {
            this.monthly_goal_amount = monthly_goal_amount;
            this.start_day = start_day;
            this.text_size = text_size;
            this.show_remaining = show_remaining;
        }
    }


    int get_days_until_day_of_month(int day_of_month)
    {
        long new_date = (new Date()).getTime() + ms_in_a_day; // Tomorrow
        while ((new Date(new_date)).getDate() != day_of_month)
        {
            new_date += ms_in_a_day;
        }
        long ms_difference = new_date - (new Date()).getTime();
        int day_difference = (int) (ms_difference / (ms_in_a_day));
        return day_difference;
    }


    int get_days_since_day_of_month(int day_of_month)
    {
        long old_date = (new Date()).getTime();

        while ((new Date(old_date)).getDate() != day_of_month)
        {
            old_date -= ms_in_a_day;
        };
        long ms_difference = (new Date()).getTime() - old_date;
        int day_difference = (int) (ms_difference / (ms_in_a_day));
        return day_difference;
    }


    int get_seconds_from_ms(long ms)
    {
        long seconds = (ms / (long) 1000);
        return (int) seconds;
    }


    long get_ms_from_seconds(int seconds)
    {
        long ms = ((long) seconds * 1000);
        return ms;
    }


    Vector<purchase_item> sort_purchases_newest_first(Vector<purchase_item> purchases)
    {
        if (purchases == null)
        {
            return null;
        }
        boolean sorted = false;
        while(!sorted)
        {
            sorted = true;
            for (int i = 0; i < purchases.size() - 1; i++)
            {
                if (purchases.elementAt(i).date < purchases.elementAt(i + 1).date)
                {
                    Collections.swap(purchases, i, i + 1);
                    sorted = false;
                }
            }
        }
        return purchases;
    }

    int get_seconds_since_period_start()
    {
        Date d = new Date();
        settings_config c = read_settings_config_from_db();
        int seconds_today = ((new Date()).getHours()*3600) + ((new Date()).getMinutes()*60);
        int days_since_start = get_days_since_day_of_month(c.start_day);
        int seconds_since_start = days_since_start*seconds_in_a_day + seconds_today;
        return seconds_since_start;
    }

    Vector<purchase_item> filter_purchases_this_period(Vector<purchase_item> purchases)
    {
        if (purchases == null)
        {
            return null;
        }
        Vector<purchase_item> purchases_this_month = new Vector<>();
        Date d = new Date();
        int seconds_now = get_seconds_from_ms(d.getTime());
        int seconds_since_start = get_seconds_since_period_start();
        for (int i = 0; i < purchases.size(); i++)
        {
            if((seconds_now - purchases.elementAt(i).date) < seconds_since_start)
            {
                purchases_this_month.add(purchases.elementAt(i));
            }
        }
        if (purchases_this_month.size() == 0)
        {
            return null;
        }
        return purchases_this_month;
    }


    String get_list_total_string(double d)
    {
        return String.format(Locale.US,"$%.2f", d);
    }


    settings_config set_text_size_for_child_views(LinearLayout parent_view)
    {
        settings_config c = read_settings_config_from_db();
        int size = c.text_size;
        int num_children = parent_view.getChildCount();
        for(int i = 0; i<num_children; i++)
        {
            View child = parent_view.getChildAt(i);
            if (child instanceof TextView)
            {
                ((TextView) child).setTextSize(size);
            }
            else if(child instanceof Button)
            {
                ((Button) child).setTextSize(size);
            }
            else if(child instanceof ToggleButton)
            {
                ((ToggleButton) child).setTextSize(size);
            }
        }
        return c;
    }


    int get_default_purchase_view_text_size()
    {
        int text_size = read_settings_config_from_db().text_size;
        int purchase_text_size = text_size / 2;
        if (purchase_text_size == 0)
        {
            purchase_text_size = text_size;
        }
        return purchase_text_size;
    }

    // e.g. "Jan 10"
    String get_month_day_string(Date date)
    {
        String[] d_array = date.toString().split("\\s+");
        String month_day =  d_array[1] + " " + d_array[2];
        return month_day;
    }

    String get_suffix_for_day(int day)
    {
        String suffix = "th";
        String[] suffix_exceptions = {"", "st", "nd", "rd"};
        int day_mod = day % 10;
        if(day_mod >= 1 && day_mod <= 3 && (day < 11 || day > 13))
        {
            suffix = suffix_exceptions[day_mod];
        }
        return suffix;
    }

    String get_string_from_date(Date date)
    {
        // input = Sun Jan 31 13:17:48 PST 2021
        // output = Sun Jan 31st, 2021
        String[] s = date.toString().split(" ");
        int day_of_month = Integer.parseInt(s[2]);
        String suffix = get_suffix_for_day(day_of_month);
        String new_str = String.format(
                "%s %s %s%s, %s",
                s[0],
                s[1],
                s[2],
                suffix,
                s[5]
        );
        return new_str;
    }

    Vector<purchase_item> read_firebase(Context the_context, purchase_item item_to_find, int filter)
    {
        Vector<purchase_item> purchases_from_firebase = new Vector<>();
        Task task = db.collection("purchases").get();
        while(!task.isComplete());
        if (task.isSuccessful())
        {
            //Toast.makeText(the_context, "read Success", Toast.LENGTH_SHORT).show();
            QuerySnapshot snapshot = (QuerySnapshot) task.getResult();
            if(snapshot != null)
            {
                for (QueryDocumentSnapshot document : snapshot)
                {
                    if (document.getData().containsKey("date"))
                    {
                        purchase_item this_purchase = get_purchase_item_from_doc(document);
                        if (item_to_find != null)
                        {
                            if (item_to_find.date == this_purchase.date)
                            {
                                purchases_from_firebase.addElement(this_purchase);
                                break;
                            }
                        }
                        else if(filter == FILTER_NEEDS_ONLY && this_purchase.need == IS_A_NEED)
                        {
                            purchases_from_firebase.addElement(get_purchase_item_from_doc(document));
                        }
                        else if(filter == FILTER_WANTS_ONLY && this_purchase.need == IS_NOT_A_NEED)
                        {
                            purchases_from_firebase.addElement(get_purchase_item_from_doc(document));
                        }
                        else if(filter == FILTER_ALL)
                        {
                            purchases_from_firebase.addElement(get_purchase_item_from_doc(document));
                        }
                    }
                }
            }
            else
            {
                Toast.makeText(the_context, "Null Returned From DB", Toast.LENGTH_SHORT).show();
            }
        }
        else
        {
            Toast.makeText(the_context, "Firestore Read Failure", Toast.LENGTH_SHORT).show();
        }
        //Toast.makeText(the_context, "Read Complete", Toast.LENGTH_SHORT).show();
        return purchases_from_firebase;
    }

    void write_firebase(purchase_item p, final Context the_context, boolean wait)
    {
        // Create a new purchase
        Map<String, Object> purchase = new HashMap<>();
        purchase.put("description", p.description);
        purchase.put("price", p.price);
        purchase.put("date", p.date);
        purchase.put("need", p.need);
        purchase.put("category", p.category);

        // Write/Overwrite a new document
        db.collection("purchases").document(Integer.toString(p.date))
            .set(purchase)
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void nothing)
                {
                    //Toast.makeText(the_context, "write Success", Toast.LENGTH_SHORT).show();
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e)
                {
                    Toast.makeText(the_context, "Firestore Write Failure", Toast.LENGTH_SHORT).show();
                }
            }
        );
        if (wait)
        {
            db.waitForPendingWrites();
        }
    }


    purchase_item get_purchase_item_from_doc(QueryDocumentSnapshot document)
    {
        String category = "";
        try
        {
            category = document.get("category").toString();
        }
        catch (Exception e) {};

        purchase_item ret = new purchase_item(
                Double.parseDouble(document.get("price").toString()),
                document.get("description").toString(),
                Integer.parseInt(document.get("date").toString()),
                Integer.parseInt(document.get("need").toString()),
                category
        );
        return ret;
    }
}
