package com.example.preston.budget;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import static java.lang.Thread.sleep;


public class MainActivity extends AppCompatActivity
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

    final int GET_DATE_RET_OK = 67;
    final int GET_FILTER_RET_OK = 68;
    final int ADD_ITEM_RET_OK = 69;
    final int EDIT_ITEM_RET_OK = 70;
    final int EDIT_ITEM_RET_DELETE = 71;
    final int NEEDS_RET_OK = 72;
    final int WANTS_RET_OK = 73;
    final int EDIT_SETTINGS_RET_OK = 74;
    final int ALL_RET_OK = 75;


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
        setContentView(R.layout.activity_main);

        sql_db = openOrCreateDatabase(db_name, MODE_PRIVATE,null);
        //sql_db.execSQL("DROP TABLE IF EXISTS t0;");
        sql_db.execSQL("CREATE TABLE IF NOT EXISTS t0(price DOUBLE, description VARCHAR, date INTEGER, needs INTEGER);");
        //sql_db.execSQL("DROP TABLE IF EXISTS c0;");
        sql_db.execSQL("CREATE TABLE IF NOT EXISTS c0(monthly_goal_amount DOUBLE, start_day INTEGER, text_size INTEGER);");

        update_textviews();

        final TextView settings = findViewById(R.id.main_settings_title);
        settings.setOnClickListener(
            new View.OnClickListener()
            {
                 @Override
                 public void onClick(View v)
                 {
                     Intent launchSettings = new Intent(v.getContext(), EditSettings.class);
                     startActivityForResult(launchSettings, EDIT_SETTINGS_RET_OK);
                 }
            }
        );

        // Stuff for "needs"
        final TextView needs = findViewById(R.id.needs);
        needs.setOnClickListener(
            new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Intent launchNeeds = new Intent(v.getContext(), ListActivity.class);
                    launchNeeds.putExtra("list_view_type", NEEDS_LIST_VIEW);
                    startActivityForResult(launchNeeds, NEEDS_RET_OK);
                }
            }
        );

        // Stuff for "wants"
        final TextView wants = findViewById(R.id.wants);
        wants.setOnClickListener(
            new View.OnClickListener()
            {
                 @Override
                 public void onClick(View v)
                 {
                    Intent launchWants = new Intent(v.getContext(), ListActivity.class);
                    launchWants.putExtra("list_view_type", WANTS_LIST_VIEW);
                    startActivityForResult(launchWants, WANTS_RET_OK);
                 }
            }
        );

        // Stuff for "all"
        final TextView all = findViewById(R.id.all);
        all.setOnClickListener(
            new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Intent launchAll = new Intent(v.getContext(), ListActivity.class);
                    launchAll.putExtra("list_view_type", ALL_LIST_VIEW);
                    startActivityForResult(launchAll, ALL_RET_OK);
                }
            }
        );
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == NEEDS_RET_OK)
        {
            double total = data.getExtras().getDouble("total");
            String text = String.format(Locale.US, "$%.2f", total);
            ((TextView) findViewById(R.id.main_needs_amount)).setText(text);
            update_textviews();
        }
        else if (resultCode == WANTS_RET_OK)
        {
            double total = data.getExtras().getDouble("total");
            String text = String.format(Locale.US, "$%.2f", total);
            ((TextView) findViewById(R.id.main_wants_amount)).setText(text);
            update_textviews();
        }
        else if (requestCode == EDIT_SETTINGS_RET_OK || requestCode == ALL_RET_OK)
        {
            update_textviews();
        }
    }


    public void update_textviews()
    {
        TextView main_title = findViewById(R.id.main_title);
        main_title.setPaintFlags(main_title.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        set_text_size_for_child_views((LinearLayout) findViewById(R.id.main_linear_layout));

        settings_config c = read_settings_config_from_db();
        set_main_monthly_goal_amount(c.monthly_goal_amount);
        set_main_monthly_start_day(c.start_day);

        TextView needs_total = findViewById(R.id.main_needs_amount);
        needs_total.setText(get_list_total_string(get_total_spent(FILTER_NEEDS_ONLY, PERIODS_THIS)));

        TextView wants_total = findViewById(R.id.main_wants_amount);
        wants_total.setText(get_list_total_string(get_total_spent(FILTER_WANTS_ONLY, PERIODS_THIS)));

        double spent = get_total_spent(FILTER_ALL, PERIODS_THIS);
        set_main_remaining_amount(c.monthly_goal_amount - spent);

        int days_remaining = get_days_until_day_of_month(c.start_day);
        set_main_remaining_days(days_remaining);

    }


    public void insert_config_into_db(settings_config c)
    {
        // Always overwrite the previous table
        sql_db.execSQL("DROP TABLE IF EXISTS c0;");
        sql_db.execSQL("CREATE TABLE IF NOT EXISTS c0(monthly_goal_amount DOUBLE, start_day INTEGER, text_size INTEGER);");
        String insert_cmd = String.format(
                Locale.US,
                "INSERT INTO c0 VALUES(%.2f, %d, %d);",
                c.monthly_goal_amount,
                c.start_day,
                c.text_size
        );
        sql_db.execSQL(insert_cmd);
    }


    public settings_config read_settings_config_from_db()
    {
        Cursor resultSet = sql_db.rawQuery("Select * from c0", null);
        if(!resultSet.moveToFirst())
        {
            // Config not found, return default config
            settings_config c = new settings_config(800,25, 25);
            return c;
        }
        double monthly_goal_amount  = Double.parseDouble(resultSet.getString(0));
        int start_day = Integer.parseInt(resultSet.getString(1));
        int text_size = Integer.parseInt(resultSet.getString(2));
        settings_config c = new settings_config(monthly_goal_amount, start_day, text_size);
        return c;
    }

    // The date doubles as the purchase ID, so we'll push the
    // date forward a second if it is not unique. Returns the date
    // used as the ID
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
        boolean contains_id = false;
        Vector<purchase_item> purchases = read_firebase(this, null, FILTER_ALL);
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

    public Vector<purchase_item> get_all_purchases(int filter_need)
    {
        Vector<purchase_item> all_purchases = read_firebase(this, null, filter_need);
        if (all_purchases.isEmpty())
        {
            all_purchases = null;
        }
        return all_purchases;
    }


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

    int get_need_type_from_id(int id, Map<Integer, Integer> id_to_need_map)
    {
        int need = -1;
        if(id_to_need_map != null)
        {
            if (id_to_need_map.containsKey(id))
            {
                need = id_to_need_map.get(id);
            }
            else
            {
                Toast.makeText(this, "Map Does not contain ID", Toast.LENGTH_SHORT).show();
            }
        }
        else
        {
            // The date field doubles as an ID
            Task task = db.collection("purchases").document(Integer.toString(id)).get();
            while (!task.isComplete()){}
            if (task.isSuccessful())
            {
                DocumentSnapshot doc = (DocumentSnapshot) task.getResult();
                if (doc != null)
                {
                    need = Integer.parseInt(doc.get("need").toString());
                }
            }
        }
        if(need == -1 || (need != IS_A_NEED && need != IS_NOT_A_NEED))
        {
            Toast.makeText(this, "Could not find need tyep for id:", Toast.LENGTH_SHORT).show();
            Toast.makeText(this, String.format("%d", id), Toast.LENGTH_SHORT).show();
            Toast.makeText(this, String.format("%d", need), Toast.LENGTH_SHORT).show();
            need = -1;
        }
        return need;
    }

    purchase_item get_purchase_item_from_view(TextView v, Map<Integer,Integer> id_to_need_map)
    {
        String description = "";
        double price = 0;
        // First char should be '$' so skip it
        String all_text = ((String) v.getText()).substring(1);
        try
        {
            String lines[] = all_text.split("\\r?\\n");
            price = Double.parseDouble(lines[0]);
            description = lines[1];
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

        int id = ((TextView) v).getId();
        int need = get_need_type_from_id(id, id_to_need_map);
        if(need != IS_A_NEED && need != IS_NOT_A_NEED)
        {
            return null;
        }
        purchase_item p = new purchase_item(
            price,
            description,
            id,
            need
        );
        return p;
    }


    double get_total_spent(int filter, int period)
    {
        Vector<purchase_item> purchases = get_all_purchases(filter);
        if (period == PERIODS_THIS)
        {
            purchases = filter_purchases_this_period(purchases);
        }
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

        purchase_item(double price, String description, int date, int need)
        {
            this.price = price;
            this.description = description;
            this.date = date;
            this.need = need;
        }
    }


    public class settings_config
    {
        double monthly_goal_amount = 0.0;
        int start_day = 25;
        int text_size = 25;
        settings_config(double monthly_goal_amount, int start_day, int text_size)
        {
            this.monthly_goal_amount = monthly_goal_amount;
            this.start_day = start_day;
            this.text_size = text_size;
        }
    }


    void set_main_monthly_goal_amount(double d)
    {
        TextView t = findViewById(R.id.main_monthly_budget_amount);
        t.setText(get_monthly_goal_amount_string(d));
    }


    void set_main_monthly_start_day(int day)
    {
        TextView t = findViewById(R.id.main_monthly_budget_start_day);
        t.setText(get_monthly_start_day_string(day));
    }


    void set_main_remaining_amount(double amount)
    {
        TextView t = findViewById(R.id.main_remaining_amount);
        String text = String.format(Locale.US, "Amount: $%.2f", amount);
        t.setText(text);
    }


    void set_main_remaining_days(int days)
    {
        TextView t = findViewById(R.id.main_remaining_days);
        String text = String.format(Locale.US, "Days: %d", days);
        t.setText(text);
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


    Vector<purchase_item> filter_purchases_this_period(Vector<purchase_item> purchases)
    {
        if (purchases == null)
        {
            return null;
        }
        Vector<purchase_item> purchases_this_month = new Vector<>();
        settings_config c = read_settings_config_from_db();
        int seconds_today = ((new Date()).getHours()*3600) + ((new Date()).getMinutes()*60);
        Date d = new Date();
        int days_since_start = get_days_since_day_of_month(c.start_day);
        int seconds_since_start = days_since_start*seconds_in_a_day + seconds_today;
        for (int i = 0; i < purchases.size(); i++)
        {
            if((get_seconds_from_ms(d.getTime()) - purchases.elementAt(i).date) < seconds_since_start)
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


    String get_monthly_start_day_string(int day)
    {
        String suffix = get_suffix_for_day(day);
        return String.format(Locale.US,"Start Day: %d%s", day, suffix);
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


    String get_monthly_goal_amount_string(double amount)
    {
        return String.format(Locale.US,"Amount: $%.2f", amount);
    }

    void set_text_size_for_child_views(LinearLayout parent_view)
    {
        int size = read_settings_config_from_db().text_size;
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

    void write_firebase(purchase_item p, final Context the_context, boolean wait)
    {
        // Create a new purchase
        Map<String, Object> purchase = new HashMap<>();
        purchase.put("description", p.description);
        purchase.put("price", p.price);
        purchase.put("date", p.date);
        purchase.put("need", p.need);

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
                });
        if (wait)
        {
            db.waitForPendingWrites();
        }
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


    purchase_item get_purchase_item_from_doc(QueryDocumentSnapshot document)
    {
        purchase_item ret = new purchase_item(
                Double.parseDouble(document.get("price").toString()),
                document.get("description").toString(),
                Integer.parseInt(document.get("date").toString()),
                Integer.parseInt(document.get("need").toString())
        );
        return ret;
    }
}