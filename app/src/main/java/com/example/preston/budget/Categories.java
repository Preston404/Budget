package com.example.preston.budget;

import android.content.Context;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class Categories extends Utils {
    Button add_category_button;
    Button delete_category_button;
    EditText add_category_edit_text;
    Spinner category_dropdown;
    Context the_context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_categories);
        set_text_size_for_child_views((LinearLayout) findViewById(R.id.edit_categories));
        the_context = this;

        TextView item_title = findViewById(R.id.edit_categories_title);
        item_title.setPaintFlags(item_title.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        update_spinner();

        add_category_edit_text = findViewById(R.id.add_category_edit_text);
        add_category_button = findViewById(R.id.edit_categories_add);
        add_category_button.setOnClickListener(new add_category());

        delete_category_button = findViewById(R.id.edit_categories_delete);
        delete_category_button.setOnClickListener(new remove_category());


    }

    public void update_spinner()
    {
        //get the spinner from the xml.
        category_dropdown = findViewById(R.id.category_spinner_delete);
        //create a list of items for the spinner.
        List<String> categories_spinner = read_categories_from_db();
        if(categories_spinner == null)
        {
            categories_spinner = new ArrayList<String>();
        }
        // Set the first spinner value to the empty string
        categories_spinner.add(0, "");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                R.layout.custom_spinner_view,
                R.id.spinner_textview,
                categories_spinner
        );
        category_dropdown.setAdapter(adapter);
    }

    class add_category implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            String category = add_category_edit_text.getText().toString();
            category = category.replace("*","");
            if(!category.equals(""))
            {
                insert_category_into_db(category);
                update_spinner();
                Toast.makeText(the_context, "Category Added", Toast.LENGTH_SHORT).show();
            }
        }
    }

    class remove_category implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            String category = category_dropdown.getSelectedItem().toString();
            if(!category.equals(""))
            {
                remove_category_from_db(category);
                update_spinner();
                Toast.makeText(the_context, "Category Removed", Toast.LENGTH_SHORT).show();
            }
        }
    }
};

