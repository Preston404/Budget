package com.example.preston.budget;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Locale;

/**
 * Created by Preston on 3/15/2020.
 */

public class EditItem extends AppCompatActivity {
    EditText item_price_edit_text;
    EditText item_description_edit_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_item);

        item_price_edit_text = findViewById(R.id.edit_item_price);
        item_description_edit_text = findViewById(R.id.edit_item_description);
        item_price_edit_text.setText(String.format(Locale.US,"%.2f", getIntent().getExtras().getDouble("price")));
        item_description_edit_text.setText(getIntent().getExtras().getString("description"));

        Button b = findViewById(R.id.edit_submit_item);
        Button delete = findViewById(R.id.edit_delete_item);


        b.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                double price = Float.parseFloat(((EditText) item_price_edit_text).getText().toString());
                String description = ((EditText) item_description_edit_text).getText().toString();

                Intent intent = new Intent();
                intent.putExtra("price",price);
                intent.putExtra("description",description);
                setResult(70, intent);
                finish();
            }
        });

        delete.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                setResult(71);
                finish();
            }
        });
    }
}
