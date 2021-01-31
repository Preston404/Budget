package com.example.preston.budget;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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
        else if (requestCode == EDIT_SETTINGS_RET_OK)
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
        while (db_contains_purhcase_with_id(p.date))
        {
            adjusted_date = true;
            p.date = p.date + 1;
        }
        if(adjusted_date)
        {
            Toast.makeText(this, "Purchase ID Adjusted", Toast.LENGTH_SHORT).show();
        }
        sql_db.execSQL
        (
            "INSERT INTO t0 (price, description, date, needs) VALUES (?, ?, ?, ?); ",
            new Object[]
            {
                p.price,
                p.description,
                p.date,
                p.need
            }
        );
        return p.date;
    }

    boolean db_contains_purhcase_with_id(int id)
    {
        boolean contains_id = false;
        // The date field doubles as an ID
        String cmd = String.format(Locale.US, "SELECT * FROM t0 WHERE date = %d", id);
        Cursor resultSet = sql_db.rawQuery(cmd, null);
        if(resultSet.moveToFirst())
        {
            contains_id = true;
        }
        return contains_id;
    }

    public Vector<purchase_item> get_all_purchases(int filter_need)
    {
        Cursor resultSet = sql_db.rawQuery("Select * from t0", null);
        if(!resultSet.moveToFirst())
        {
            return null;
        }
        Vector<purchase_item> all_purchases = new Vector<>();
        while(true)
        {
            double price = Double.parseDouble(resultSet.getString(0));
            String description = resultSet.getString(1);
            int date = Integer.parseInt(resultSet.getString(2));
            int need = Integer.parseInt(resultSet.getString(3));
            purchase_item p = new purchase_item(price, description, date, need);
            if ((filter_need == FILTER_NEEDS_ONLY && need == IS_A_NEED) ||
                (filter_need == FILTER_WANTS_ONLY && need == IS_NOT_A_NEED) ||
                (filter_need == FILTER_ALL))
            {
                all_purchases.add(p);
            }
            if (resultSet.isLast())
            {
                break;
            }
            resultSet.moveToNext();
        }
        return all_purchases;
    }


    public boolean remove_purchase_by_id(int id)
    {
        Cursor resultSet = sql_db.rawQuery("Select * from t0", null);
        if(!resultSet.moveToFirst())
        {
            return false;
        }
        while(true)
        {
            String date_string = resultSet.getString(2);
            int date = Integer.parseInt(date_string);
            if (date == id)
            {
                String cmd = String.format(Locale.US, "DELETE FROM t0 WHERE date = %d", date);
                sql_db.execSQL(cmd);
                return true;
            }
            if (resultSet.isLast())
            {
                break;
            }
            resultSet.moveToNext();
        }
        return false;
    }

    int get_need_type_from_id(int id)
    {
        // The date field doubles as an ID
        String cmd = String.format(Locale.US, "SELECT * FROM t0 WHERE date = %d", id);
        Cursor resultSet = sql_db.rawQuery(cmd, null);
        // Assume failure
        int need = -1;
        if(resultSet.moveToFirst())
        {
            if(resultSet.isLast())
            {
                need = Integer.parseInt(resultSet.getString(3));
            }
            else
            {
                Toast.makeText(this, "Could not find unique db entry for id:", Toast.LENGTH_SHORT).show();
                Toast.makeText(this, String.format("%d", id), Toast.LENGTH_SHORT).show();
            }
        }
        else
        {
            Toast.makeText(this, "Could not find db entry for id:", Toast.LENGTH_SHORT).show();
            Toast.makeText(this, String.format("%d", id), Toast.LENGTH_SHORT).show();
        }
        if (need != IS_A_NEED && need != IS_NOT_A_NEED && need != -1)
        {
            need = -1;
            Toast.makeText(this, "Invalid need type.", Toast.LENGTH_SHORT).show();
        }
        return need;
    }

    purchase_item get_purchase_item_from_view(TextView v)
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
            //description = matcher.group(1);
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
        int need = get_need_type_from_id(id);
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
        String suffix = "th";
        String[] suffix_exceptions = {"", "st", "nd", "rd"};
        if(day >= 1 && day <= 3)
        {
            suffix = suffix_exceptions[day];
        }
        return String.format(Locale.US,"Start Day: %d%s", day, suffix);
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
}