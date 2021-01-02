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


/**
 * Created by Preston on 8/30/2020.
 */

public class GetFilter extends MainActivity{
    EditText max_number_days_edit_text;
    EditText min_number_days_edit_text;
    EditText string_edit_text;
    EditText price_max_edit_text;
    EditText price_min_edit_text;
    Button get_filter_button;
    Spinner spinner;
    String filter_needs_string = "All";
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filter_purchases);
        context = this;
        set_text_size_for_child_views((LinearLayout) findViewById(R.id.filter_purchases_linear_layout));

        TextView goal_title = findViewById(R.id.filter_purchases_title);
        goal_title.setPaintFlags(goal_title.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        max_number_days_edit_text = findViewById(R.id.max_days_edit_text);
        min_number_days_edit_text = findViewById(R.id.min_days_edit_text);
        string_edit_text = findViewById(R.id.string_edit_text);
        price_max_edit_text = findViewById(R.id.price_max_edit_text);
        price_min_edit_text = findViewById(R.id.price_min_edit_text);
        get_filter_button = findViewById(R.id.filter_purchases_button);

        // Set up the spinner
        spinner = findViewById(R.id.needs_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.spinner_choices, R.layout.spinner_view);
        adapter.setDropDownViewResource(R.layout.spinner_view);
        spinner.setAdapter(adapter);

        SpinnerActivity spinnerActivity = new SpinnerActivity();
        spinner.setOnItemSelectedListener(spinnerActivity);

        get_filter_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                int filter_number_days_max = 0;
                int filter_number_days_min = 0;
                String filter_string = "";
                double filter_price_max = 0.0;
                double filter_price_min = 0.0;
                int needs_filter = FILTER_ALL;

                try {
                    filter_number_days_max = Integer.parseInt(((EditText) max_number_days_edit_text).getText().toString());
                    filter_number_days_min = Integer.parseInt(((EditText) min_number_days_edit_text).getText().toString());
                    filter_string = ((EditText) string_edit_text).getText().toString();
                    filter_price_max = Double.parseDouble(((EditText) price_max_edit_text).getText().toString());
                    filter_price_min = Double.parseDouble(((EditText) price_min_edit_text).getText().toString());
                }
                catch (Exception e){
                    Toast.makeText(context, "Invalid Entry", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (filter_number_days_min < 0 || filter_number_days_max < 0){
                    Toast.makeText(context, "Days must be positive.", Toast.LENGTH_SHORT).show();
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
                intent.putExtra("days_min",filter_number_days_min);
                intent.putExtra("days_max",filter_number_days_max);
                intent.putExtra("needs", needs_filter);
                intent.putExtra("price_max", filter_price_max);
                intent.putExtra("price_min", filter_price_min);
                setResult(GET_FILTER_RET_OK, intent);
                finish();
            }
        });

    }

    public class SpinnerActivity extends Activity implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view,
                                   int pos, long id) {
            // An item was selected. You can retrieve the selected item using
            // parent.getItemAtPosition(pos)
            filter_needs_string = parent.getItemAtPosition(pos).toString();
        }

        public void onNothingSelected(AdapterView<?> parent) {
            // Do nothing
        }
    }

}