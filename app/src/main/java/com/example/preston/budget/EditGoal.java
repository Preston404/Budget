package com.example.preston.budget;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

/**
 * Created by Preston on 4/2/2020.
 */

public class EditGoal extends MainActivity{
    EditText goal_amount_edit_text;
    EditText goal_day_edit_text;
    Button edit_goal_button;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_goal);
        context = this;


        TextView goal_title = findViewById(R.id.edit_goal_title);
        goal_title.setPaintFlags(goal_title.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        goal_amount_edit_text = findViewById(R.id.goal_amount_edit_text);
        goal_day_edit_text = findViewById(R.id.goal_day_edit_text);
        edit_goal_button = findViewById(R.id.edit_goal_button);

        goal_config c = read_goal_config_from_db();

        String text_amount = String.format(Locale.US,"%.2f",c.monthly_goal_amount);
        goal_amount_edit_text.setText(text_amount);

        String text_day = String.format(Locale.US,"%d",c.start_day);
        goal_day_edit_text.setText(text_day);


        edit_goal_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){

                double monthly_amount;
                int start_day;
                try {
                    monthly_amount = Double.parseDouble(((EditText) goal_amount_edit_text).getText().toString());
                    start_day = Integer.parseInt(((EditText) goal_day_edit_text).getText().toString());
                }
                catch (Exception e){
                    Toast.makeText(context, "Invalid Entry", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (start_day > 28){
                    Toast.makeText(context, "start day must be 28 or less", Toast.LENGTH_SHORT).show();
                    return;
                }
                goal_config c = new goal_config(monthly_amount, start_day);
                insert_config_into_db(c);
                Intent intent = new Intent();
                setResult(EDIT_GOAL_RET_OK, intent);
                finish();

            }
        });

    }
}
