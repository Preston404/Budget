package com.example.preston.budget;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import java.util.Map;


/**
 * Created by Preston on 8/30/2020.
 */

public class GetFilter extends MainActivity{
    TextView start_day_text_view;
    TextView end_day_text_view;
    EditText string_edit_text;
    EditText price_max_edit_text;
    EditText price_min_edit_text;
    Button get_filter_button;
    String filter_needs_string = "All";
    int sort_price_index = 0;
    Context context;
    View last_view_clicked;
    int needs_filter = FILTER_ALL;

    long filter_start_day = 0;
    long filter_end_day = 0;

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

        // Parse inputs when we click submit
        get_filter_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){

                String filter_string = ((EditText) string_edit_text).getText().toString().toLowerCase();
                if(filter_string.equals("- n/a -"))
                {
                    filter_string = "";
                }

                double filter_price_max = 0.0;
                double filter_price_min = 0.0;
                String price_max_text = ((EditText) price_max_edit_text).getText().toString().toLowerCase();
                String price_min_text = ((EditText) price_min_edit_text).getText().toString().toLowerCase();
                try {
                    if(!price_max_text.equals("") && !price_max_text.equals("none"))
                    {
                        filter_price_max = Double.parseDouble(price_max_text);
                    }
                    if(!price_min_text.equals("") && !price_min_text.equals("none"))
                    {
                        filter_price_min = Double.parseDouble(price_min_text);
                    }
                }
                catch (Exception e){
                    Toast.makeText(context, "Invalid Entry", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = new Intent();
                intent.putExtra("string",filter_string);
                intent.putExtra("day_start",filter_start_day);
                intent.putExtra("day_end",filter_end_day);
                intent.putExtra("needs", needs_filter);
                intent.putExtra("sort_price", sort_price_index);
                intent.putExtra("price_max", filter_price_max);
                intent.putExtra("price_min", filter_price_min);
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
                filter_start_day = date;
                start_day_text_view.setText(get_string_from_date(new Date(date)));
            }
            else if(last_view_clicked == findViewById(R.id.end_day_text_view) ||
                    last_view_clicked == findViewById(R.id.end_day_button))
            {
                filter_end_day = date;
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
            if(filter_needs_string.equals("All"))
            {
                filter_needs_string = "Needs";
                needs_filter = FILTER_NEEDS_ONLY;
            }
            else if(filter_needs_string.equals("Needs"))
            {
                filter_needs_string = "Wants";
                needs_filter = FILTER_WANTS_ONLY;
            }
            else if(filter_needs_string.equals("Wants"))
            {
                filter_needs_string = "All";
                needs_filter = FILTER_ALL;
            }
            ((TextView) findViewById(R.id.purchase_type_textview)).setText(filter_needs_string);
        }
    };

    public View.OnClickListener toggle_sort_price = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            sort_price_index += 1;
            sort_price_index %= SORT_OPTIONS.length;
            ((TextView) findViewById(R.id.sort_price_textview)).setText(SORT_OPTIONS[sort_price_index]);
        }
    };
}