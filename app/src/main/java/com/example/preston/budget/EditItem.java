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

import java.util.Locale;

/**
 * Created by Preston on 3/15/2020.
 */

public class EditItem extends MainActivity {
    EditText item_price_edit_text;
    EditText item_description_edit_text;
    ToggleButton toggle_button;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_item);
        context = this;
        set_text_size_for_child_views((LinearLayout) findViewById(R.id.edit_item));

        TextView item_title = findViewById(R.id.edit_item_title);
        item_title.setPaintFlags(item_title.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        item_price_edit_text = findViewById(R.id.edit_item_price);
        item_description_edit_text = findViewById(R.id.edit_item_description);
        toggle_button = findViewById(R.id.toggle_button);

        item_price_edit_text.setText(String.format(Locale.US,"%.2f", getIntent().getExtras().getDouble("price")));
        item_description_edit_text.setText(getIntent().getExtras().getString("description"));
        if(getIntent().getExtras().getInt("need") == IS_A_NEED)
        {
            toggle_button.setChecked(true);
        }
        else
        {
            toggle_button.setChecked((false));
        }

        Button b = findViewById(R.id.edit_submit_item);
        Button delete = findViewById(R.id.edit_delete_item);


        b.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                double price;
                String description;
                int view_type;
                try {
                    price = Float.parseFloat(((EditText) item_price_edit_text).getText().toString());
                    description = ((EditText) item_description_edit_text).getText().toString();
                    // This view type will be compared against the list_view_type that is editing
                    // this purchase to check if the purchase type (need/want) was changed
                    if(toggle_button.isChecked())
                    {
                        view_type = NEEDS_LIST_VIEW;
                    }
                    else
                    {
                        view_type = WANTS_LIST_VIEW;
                    }
                }
                catch (Exception e){
                    Toast.makeText(context, "Invalid Entry", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent();
                intent.putExtra("price",price);
                intent.putExtra("description",description);
                intent.putExtra("view_type",view_type);
                setResult(EDIT_ITEM_RET_OK, intent);
                finish();
            }
        });

        delete.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                setResult(EDIT_ITEM_RET_DELETE);
                finish();
            }
        });
    }
}
