package com.example.preston.budget;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.Date;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


/**
 * Created by Preston on 8/30/2020.
 */

public class GetFilter extends Utils{
    TextView start_day_text_view;
    TextView end_day_text_view;
    EditText string_edit_text;
    EditText price_max_edit_text;
    EditText price_min_edit_text;
    Button get_filter_button;

    Context context;
    View last_view_clicked;

    filter_config current_filter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filter_purchases);
        context = this;
        set_text_size_for_child_views((LinearLayout) findViewById(R.id.filter_purchases_linear_layout));

        TextView goal_title = findViewById(R.id.filter_purchases_title);
        goal_title.setPaintFlags(goal_title.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        end_day_text_view = findViewById(R.id.end_day_text_view);
        start_day_text_view = findViewById(R.id.start_day_text_view);
        string_edit_text = findViewById(R.id.string_edit_text);
        price_max_edit_text = findViewById(R.id.price_max_edit_text);
        price_min_edit_text = findViewById(R.id.price_min_edit_text);
        get_filter_button = findViewById(R.id.filter_purchases_button);

        init_views();
        try_init_databases();

        // Parse inputs when we click submit
        get_filter_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){

                String temp_filter_string = "";
                current_filter.text = ((EditText) string_edit_text).getText().toString().toLowerCase();
                if(!current_filter.text.equals("- n/a -"))
                {
                    temp_filter_string = current_filter.text;
                }

                String price_max_text = ((EditText) price_max_edit_text).getText().toString().toLowerCase();
                String price_min_text = ((EditText) price_min_edit_text).getText().toString().toLowerCase();
                try {
                    if(!price_max_text.equals("") && !price_max_text.equals("none"))
                    {
                        current_filter.max_price = Double.parseDouble(price_max_text);
                    }
                    if(!price_min_text.equals("") && !price_min_text.equals("none"))
                    {
                        current_filter.min_price = Double.parseDouble(price_min_text);
                    }
                }
                catch (Exception e){
                    Toast.makeText(context, "Invalid Entry", Toast.LENGTH_SHORT).show();
                    return;
                }

                insert_filter_into_db(current_filter);

                Intent intent = new Intent();
                intent.putExtra("string", temp_filter_string);
                intent.putExtra("day_start", current_filter.start_day);
                intent.putExtra("day_end", current_filter.end_day);
                intent.putExtra("needs", current_filter.type);
                intent.putExtra("sort_price", current_filter.sort);
                intent.putExtra("price_max", current_filter.max_price);
                intent.putExtra("price_min", current_filter.min_price);
                setResult(GET_FILTER_RET_OK, intent);
                finish();
            }
        });

    }

    void init_views()
    {
        findViewById(R.id.start_day_button).setOnClickListener(date_field_click_listener);
        start_day_text_view.setOnClickListener(date_field_click_listener);

        findViewById(R.id.end_day_button).setOnClickListener(date_field_click_listener);
        end_day_text_view.setOnClickListener(date_field_click_listener);

        findViewById(R.id.max_price_button).setOnClickListener(clear_textview);
        findViewById(R.id.min_price_button).setOnClickListener(clear_textview);
        findViewById(R.id.contains_text_button).setOnClickListener(clear_textview);

        findViewById(R.id.purchase_type_textview).setOnClickListener(toggle_purchase_type);
        findViewById(R.id.purchase_type_button).setOnClickListener(toggle_purchase_type);

        findViewById(R.id.sort_price_textview).setOnClickListener(toggle_sort_price);
        findViewById(R.id.sort_price_button).setOnClickListener(toggle_sort_price);

        findViewById(R.id.filter_clear).setOnClickListener(clear_filter);

        init_text();
    }


    void init_text()
    {
        current_filter = read_filter_from_db();

        if(current_filter.max_price != 0)
        {
            ((TextView) findViewById(R.id.price_max_edit_text)).setText(Double.toString(current_filter.max_price));
        }
        else
        {
            ((TextView) findViewById(R.id.price_max_edit_text)).setText("None");
        }
        if(current_filter.min_price != 0)
        {
            ((TextView) findViewById(R.id.price_min_edit_text)).setText(Double.toString(current_filter.min_price));
        }
        else
        {
            ((TextView) findViewById(R.id.price_min_edit_text)).setText("None");
        }

        ((TextView) findViewById(R.id.string_edit_text)).setText(current_filter.text);

        if(current_filter.start_day != 0)
        {
            ((TextView) findViewById(R.id.start_day_text_view)).setText(
                    get_string_from_date(new Date(current_filter.start_day))
            );
        }
        else
        {
            ((TextView) findViewById(R.id.start_day_text_view)).setText("None");
        }
        if(current_filter.end_day != 0)
        {
            ((TextView) findViewById(R.id.end_day_text_view)).setText(
                    get_string_from_date(new Date(current_filter.end_day))
            );
        }
        else
        {
            ((TextView) findViewById(R.id.end_day_text_view)).setText("None");
        }

        ((TextView) findViewById(R.id.purchase_type_textview)).setText(
                get_string_from_filter_type(current_filter.type)
        );

        ((TextView) findViewById(R.id.sort_price_textview)).setText(
                SORT_OPTIONS[current_filter.sort]
        );
    }

    public View.OnClickListener date_field_click_listener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            Intent launchGetDate = new Intent(v.getContext(), GetDate.class);
            String date_title = "Start Date";
            if (v == findViewById(R.id.end_day_button) || v == findViewById(R.id.end_day_text_view))
            {
                date_title = "End Date";
            }
            launchGetDate.putExtra("title", date_title);
            launchGetDate.putExtra("date", get_seconds_from_ms((new Date()).getTime()));

            int requested_code = GET_DATE_RET_OK;
            last_view_clicked = v;
            startActivityForResult(launchGetDate, requested_code);
        }
    };


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == GET_DATE_RET_OK)
        {
            long date = data.getExtras().getLong("date");

            if(last_view_clicked == findViewById(R.id.start_day_text_view) ||
               last_view_clicked == findViewById(R.id.start_day_button))
            {
                current_filter.start_day = date;
                start_day_text_view.setText(get_string_from_date(new Date(date)));
            }
            else if(last_view_clicked == findViewById(R.id.end_day_text_view) ||
                    last_view_clicked == findViewById(R.id.end_day_button))
            {
                current_filter.end_day = date;
                end_day_text_view.setText(get_string_from_date(new Date(date)));
            }
        }
    }

    public View.OnClickListener clear_textview = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (findViewById(R.id.max_price_button) == v)
            {
                ((EditText) findViewById(R.id.price_max_edit_text)).setText("");
            }
            else if (findViewById(R.id.min_price_button) == v)
            {
                ((EditText) findViewById(R.id.price_min_edit_text)).setText("");
            }
            else if (findViewById(R.id.contains_text_button) == v)
            {
                ((EditText) findViewById(R.id.string_edit_text)).setText("");
            }
        }
    };

    public View.OnClickListener toggle_purchase_type = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String filter_needs_string = ((TextView) findViewById(R.id.purchase_type_textview)).getText().toString();
            if(filter_needs_string.equals(get_string_from_filter_type(FILTER_ALL)))
            {
                current_filter.type = FILTER_NEEDS_ONLY;
            }
            else if(filter_needs_string.equals(get_string_from_filter_type((FILTER_NEEDS_ONLY))))
            {
                current_filter.type = FILTER_WANTS_ONLY;
            }
            else if(filter_needs_string.equals(get_string_from_filter_type(FILTER_WANTS_ONLY)))
            {
                current_filter.type = FILTER_ALL;
            }
            ((TextView) findViewById(R.id.purchase_type_textview)).setText(
                    get_string_from_filter_type(current_filter.type)
            );
        }
    };

    public View.OnClickListener toggle_sort_price = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            current_filter.sort += 1;
            current_filter.sort %= SORT_OPTIONS.length;
            ((TextView) findViewById(R.id.sort_price_textview)).setText(SORT_OPTIONS[current_filter.sort]);
        }
    };

    public View.OnClickListener clear_filter = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            current_filter = new filter_config();
            insert_filter_into_db(current_filter);
            init_text();
        }
    };

    public class filter_config
    {
        double max_price = 0.0;
        double min_price = 0.0;
        String text = "- n/a -";
        long start_day = 0;
        long end_day = 0;
        int type = FILTER_ALL;
        int sort = 0;

        filter_config(){};
        filter_config(
                double max_price,
                double min_price,
                String text,
                long start_day,
                long end_day,
                int type,
                int sort
        )
        {
            this.max_price = max_price;
            this.min_price = min_price;
            this.text = text;
            this.start_day = start_day;
            this.end_day = end_day;
            this.type = type;
            this.sort = sort;
        }
    }


    filter_config read_filter_from_db()
    {
        Cursor resultSet = sql_db.rawQuery("Select * from f0", null);
        filter_config f = new filter_config();
        if(!resultSet.moveToFirst())
        {
            // Config not found, return default config
            return new filter_config();
        }
        try {
            f.max_price = Double.parseDouble(resultSet.getString(0));
            f.min_price = Double.parseDouble(resultSet.getString(1));
            f.text = resultSet.getString(2);
            f.start_day = Long.parseLong(resultSet.getString(3));
            f.end_day = Long.parseLong(resultSet.getString(4));
            f.type = Integer.parseInt(resultSet.getString(5));
            f.sort = Integer.parseInt(resultSet.getString(6));
        }
        catch (Exception e)
        {
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
        }
        return f;
    }

    void insert_filter_into_db(filter_config f)
    {
        // Always overwrite the previous table
        sql_db.execSQL("DROP TABLE IF EXISTS f0;");
        sql_db.execSQL("CREATE TABLE IF NOT EXISTS f0(max_price DOUBLE, min_price DOUBLE, text VARCHAR, start_day INTEGER, end_day INTEGER, type INTEGER, sort INTEGER);");
        String insert_cmd = String.format(
                Locale.US,
                "INSERT INTO f0 VALUES(%.2f, %.2f, '%s', %d, %d, %d, %d);",
                f.max_price,
                f.min_price,
                f.text,
                f.start_day,
                f.end_day,
                f.type,
                f.sort
        );
        sql_db.execSQL(insert_cmd);
    }


    String get_string_from_filter_type(int filter_type)
    {
        if(filter_type == FILTER_ALL)
        {
            return "ALL";
        }
        else if(filter_type == FILTER_NEEDS_ONLY)
        {
            return "Needs";
        }
        else if(filter_type == FILTER_WANTS_ONLY)
        {
            return "Wants";
        }
        else
        {
            Toast.makeText(context, "No str for filter type.", Toast.LENGTH_LONG).show();
            return "ALL";
        }
    }
}