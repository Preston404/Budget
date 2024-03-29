package com.example.preston.budget;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Preston on 3/15/2020.
 */

public class EditItem extends Utils {
    EditText item_price_edit_text;
    EditText item_description_edit_text;
    TextView item_date_text;
    ToggleButton toggle_button;
    Spinner category_dropdown;
    int old_date;
    int new_date;
    String old_category;
    String new_category;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_item);
        context = this;
        settings_config c = set_text_size_for_child_views((LinearLayout) findViewById(R.id.edit_item));
        Bundle extra_data = getIntent().getExtras();
        int select_spinner_index = 0;

        TextView item_title = findViewById(R.id.edit_item_title);
        item_title.setPaintFlags(item_title.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        item_price_edit_text = findViewById(R.id.edit_item_price);
        item_description_edit_text = findViewById(R.id.edit_item_description);
        item_date_text = findViewById(R.id.edit_item_date_text);
        toggle_button = findViewById(R.id.toggle_button);

        item_price_edit_text.setText(String.format(Locale.US, "%.2f", extra_data.getDouble("price")));
        item_description_edit_text.setText(extra_data.getString("description"));
        if (extra_data.getInt("need") == IS_A_NEED)
        {
            toggle_button.setChecked(true);
        }
        else
        {
            toggle_button.setChecked((false));
        }
        old_date = extra_data.getInt("date");
        new_date = old_date;
        old_category = extra_data.getString("category");
        new_category = old_category;

        item_date_text.setText(get_string_from_date(new Date(get_ms_from_seconds(old_date))));

        //get the spinner from the xml.
        category_dropdown = findViewById(R.id.category_spinner_edit);
        //create a list of items for the spinner.
        List<String> categories_spinner = read_categories_from_db();
        if(categories_spinner == null)
        {
            categories_spinner = new ArrayList<String>();
        }
        // Put "N/A" at the front of the list, this will be the default
        categories_spinner.add(0,"N/A");
        for(int i=0; i<categories_spinner.size();i++)
        {
            if(categories_spinner.get(i).equals(old_category))
            {
                select_spinner_index = i;
                break;
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                R.layout.custom_spinner_view,
                R.id.spinner_textview,
                categories_spinner
        );
        category_dropdown.setAdapter(adapter);
        category_dropdown.setSelection(select_spinner_index);

        Button submit = findViewById(R.id.edit_submit_item);
        Button delete = findViewById(R.id.edit_delete_item);
        Button date_button = findViewById(R.id.edit_item_date_button);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double new_price;
                String new_description;
                int new_view_type;
                try {
                    new_price = Float.parseFloat(((EditText) item_price_edit_text).getText().toString());
                    new_description = ((EditText) item_description_edit_text).getText().toString();
                    new_category = category_dropdown.getSelectedItem().toString();
                    if (new_category.equals("N/A"))
                    {
                        new_category = "";
                    }
                } catch (Exception e) {
                    Toast.makeText(context, "Invalid Entry", Toast.LENGTH_SHORT).show();
                    return;
                }
                // This view type will be compared against the list_view_type that is editing
                // this purchase to check if the purchase type (need/want) was changed
                if (toggle_button.isChecked())
                {
                    new_view_type = NEEDS_LIST_VIEW;
                }
                else
                {
                    new_view_type = WANTS_LIST_VIEW;
                }
                Intent intent = new Intent();
                intent.putExtra("price", new_price);
                intent.putExtra("description", new_description);
                intent.putExtra("category", new_category);
                intent.putExtra("view_type", new_view_type);
                intent.putExtra("date", new_date);
                setResult(EDIT_ITEM_RET_OK, intent);
                finish();
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(EDIT_ITEM_RET_DELETE);
                finish();
            }
        });

        date_button.setOnClickListener(date_field_click_listener);
        item_date_text.setOnClickListener(date_field_click_listener);
    }

    public View.OnClickListener date_field_click_listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent launchGetDate = new Intent(v.getContext(), GetDate.class);
            launchGetDate.putExtra("title", "Edit Item Date");
            launchGetDate.putExtra("date", old_date);
            int requested_code = GET_DATE_RET_OK;
            startActivityForResult(launchGetDate, requested_code);
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == GET_DATE_RET_OK)
        {
            long date = data.getExtras().getLong("date");
            new_date = get_seconds_from_ms(date);
            item_date_text.setText(get_string_from_date(new Date(date)));

            if (new_date != old_date)
            {
                String old_description = item_description_edit_text.getText().toString();
                String new_description = adjust_date_in_description(new_date, old_description);
                item_description_edit_text.setText(new_description);
            }
        }
    }

    String adjust_date_in_description(int date, String description)
    {
        // Make sure the date in the description matches the date in the date field
        String month_day = get_month_day_string(new Date(get_ms_from_seconds(date))) + ":";
        description = description.replaceFirst("[a-zA-Z]{3} [0-9]*:", month_day);
        return description;
    }
};
