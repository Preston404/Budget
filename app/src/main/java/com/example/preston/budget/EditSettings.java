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

import java.util.Locale;

/**
 * Created by Preston on 4/2/2020.
 */

public class EditSettings extends Utils{
    EditText goal_amount_edit_text;
    EditText goal_day_edit_text;
    EditText text_size_edit_text;
    Button edit_settings_button;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_settings);
        context = this;

        set_text_size_for_child_views((LinearLayout) findViewById(R.id.edit_settings));

        TextView settings_title = findViewById(R.id.edit_settings_title);
        settings_title.setPaintFlags(settings_title.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        goal_amount_edit_text = findViewById(R.id.goal_amount_edit_text);
        goal_day_edit_text = findViewById(R.id.goal_day_edit_text);
        text_size_edit_text = findViewById(R.id.text_size_edit_text);
        edit_settings_button = findViewById(R.id.edit_settings_button);

        settings_config c = read_settings_config_from_db();

        String text_amount = String.format(Locale.US,"%.2f",c.monthly_goal_amount);
        goal_amount_edit_text.setText(text_amount);

        String text_day = String.format(Locale.US,"%d",c.start_day);
        goal_day_edit_text.setText(text_day);

        String text_size = String.format(Locale.US,"%d",c.text_size);
        text_size_edit_text.setText(text_size);


        edit_settings_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){

                double monthly_amount;
                int start_day;
                int text_size;
                try {
                    monthly_amount = Double.parseDouble(((EditText) goal_amount_edit_text).getText().toString());
                    start_day = Integer.parseInt(((EditText) goal_day_edit_text).getText().toString());
                    text_size = Integer.parseInt(((EditText) text_size_edit_text).getText().toString());
                }
                catch (Exception e){
                    Toast.makeText(context, "Invalid Entry", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (start_day > 28){
                    Toast.makeText(context, "start day must be 28 or less", Toast.LENGTH_SHORT).show();
                    return;
                }
                settings_config c = read_settings_config_from_db();
                c.monthly_goal_amount = monthly_amount;
                c.start_day = start_day;
                c.text_size = text_size;
                insert_config_into_db(c);
                Intent intent = new Intent();
                setResult(EDIT_SETTINGS_RET_OK, intent);
                finish();
            }
        });

    }
}
