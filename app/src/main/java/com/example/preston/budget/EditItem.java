package com.example.preston.budget;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.Date;
import java.util.Locale;

/**
 * Created by Preston on 3/15/2020.
 */

public class EditItem extends MainActivity {
    EditText item_price_edit_text;
    EditText item_description_edit_text;
    TextView item_date_text;
    ToggleButton toggle_button;
    int old_date;
    int new_date;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_item);
        context = this;
        set_text_size_for_child_views((LinearLayout) findViewById(R.id.edit_item));
        Bundle extra_data = getIntent().getExtras();

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
        item_date_text.setText(get_string_from_date(new Date(get_ms_from_seconds(old_date))));

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
