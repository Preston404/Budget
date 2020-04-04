package com.example.preston.budget;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Preston on 3/15/2020.
 */

public class AddItem extends MainActivity {
    EditText item_price_edit_text = null;
    EditText item_description_edit_text = null;
    double price = 0.0;
    String description = "No description";
    Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_item);

        TextView item_title = findViewById(R.id.add_item_title);
        item_title.setPaintFlags(item_title.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        item_price_edit_text = findViewById(R.id.item_price);
        item_description_edit_text = findViewById(R.id.item_description);

        Button b = findViewById(R.id.submit_item);

        b.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                try {
                    price = Float.parseFloat(((EditText) item_price_edit_text).getText().toString());
                    description = ((EditText) item_description_edit_text).getText().toString();
                }catch(Exception e){
                    Toast.makeText(context, "Invalid Entry", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent();
                intent.putExtra("price",price);
                intent.putExtra("description",description);
                setResult(ADD_ITEM_RET_OK, intent);
                finish();
            }
        });
    }
}