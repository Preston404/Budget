package com.example.preston.budget;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
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
    Spinner spinner;
    String filter_needs_string = "All";
    Context context;
    View last_view_clicked;

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

        init_days_text_views();
        init_spinner();

        // Parse inputs when we click submit
        get_filter_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                String filter_string = "";
                double filter_price_max = 0.0;
                double filter_price_min = 0.0;
                int needs_filter = FILTER_ALL;

                try {
                    filter_string = ((EditText) string_edit_text).getText().toString();
                    filter_price_max = Double.parseDouble(((EditText) price_max_edit_text).getText().toString());
                    filter_price_min = Double.parseDouble(((EditText) price_min_edit_text).getText().toString());
                }
                catch (Exception e){
                    Toast.makeText(context, "Invalid Entry", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Default filter is ALL, only check if
                // it changed
                if(filter_needs_string.equals("Wants"))
                {
                    needs_filter = FILTER_WANTS_ONLY;
                }
                else if(filter_needs_string.equals("Needs"))
                {
                    needs_filter = FILTER_NEEDS_ONLY;
                }

                Intent intent = new Intent();
                intent.putExtra("string",filter_string);
                intent.putExtra("day_start",filter_start_day);
                intent.putExtra("day_end",filter_end_day);
                intent.putExtra("needs", needs_filter);
                intent.putExtra("price_max", filter_price_max);
                intent.putExtra("price_min", filter_price_min);
                setResult(GET_FILTER_RET_OK, intent);
                finish();
            }
        });

    }

    void init_days_text_views()
    {
        findViewById(R.id.start_day_button).setOnClickListener(date_field_click_listener);
        start_day_text_view.setOnClickListener(date_field_click_listener);

        findViewById(R.id.end_day_button).setOnClickListener(date_field_click_listener);
        end_day_text_view.setOnClickListener(date_field_click_listener);
    }

    public View.OnClickListener date_field_click_listener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            Intent launchGetDate = new Intent(v.getContext(), GetDate.class);
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
                start_day_text_view.setText((new Date(date)).toString());
            }
            else if(last_view_clicked == findViewById(R.id.end_day_text_view) ||
                    last_view_clicked == findViewById(R.id.end_day_button))
            {
                // Add 24 hours so that the filter will include purchases
                // on the final day.
                filter_end_day = date + get_ms_from_seconds(seconds_in_a_day-1);
                end_day_text_view.setText((new Date(filter_end_day)).toString());
            }
        }
    }


    void init_spinner()
    {
        spinner = findViewById(R.id.needs_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.spinner_choices, R.layout.spinner_view);
        adapter.setDropDownViewResource(R.layout.spinner_view);
        spinner.setAdapter(adapter);

        SpinnerActivity spinnerActivity = new SpinnerActivity();
        spinner.setOnItemSelectedListener(spinnerActivity);
    }


    public class SpinnerActivity extends Activity implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view,
                                   int pos, long id) {
            filter_needs_string = parent.getItemAtPosition(pos).toString();
        }

        // Java complains if we don't implement this
        public void onNothingSelected(AdapterView<?> parent) {}
    }

}