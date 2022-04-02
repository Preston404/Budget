package com.example.preston.budget;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Preston on 3/15/2020.
 */

public class AddItem extends Utils {
    EditText item_price_edit_text = null;
    EditText item_description_edit_text = null;
    double price = 0.0;
    String description = "No description";
    String category = "";
    Context context = this;
    Spinner category_dropdown;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_item);
        set_text_size_for_child_views((LinearLayout) findViewById(R.id.add_item));

        TextView item_title = findViewById(R.id.add_item_title);
        item_title.setPaintFlags(item_title.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        item_price_edit_text = findViewById(R.id.item_price);
        item_description_edit_text = findViewById(R.id.item_description);


        //get the spinner from the xml.
        category_dropdown = findViewById(R.id.category_spinner);
        //create a list of items for the spinner.
        List<String> categories_spinner = read_categories_from_db();
        if(categories_spinner == null)
        {
            categories_spinner = new ArrayList<String>();
            categories_spinner.add("");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                categories_spinner
        );
        category_dropdown.setAdapter(adapter);

        Button b = findViewById(R.id.submit_item);
        b.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                try
                {
                    price = Float.parseFloat(((EditText) item_price_edit_text).getText().toString());
                    description = ((EditText) item_description_edit_text).getText().toString();
                    description = description.replaceAll("\\n","").replaceAll("\\r","");
                    category = category_dropdown.getSelectedItem().toString();
                }
                catch(Exception e)
                {
                    Toast.makeText(context, "Invalid Entry", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent();
                String month_day = get_month_day_string(new Date());
                description = String.format(Locale.US, "%s: %s", month_day, description);
                intent.putExtra("price",price);
                intent.putExtra("description",description);
                intent.putExtra("category",category);
                setResult(ADD_ITEM_RET_OK, intent);
                finish();
            }
        });
    }
}
