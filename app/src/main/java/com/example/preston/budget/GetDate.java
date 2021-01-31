package com.example.preston.budget;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Date;
import java.util.List;

public class GetDate extends MainActivity {
    Button get_date_button;
    DatePicker date_picker;
    TextView date_title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.get_date);

        set_text_size_for_child_views((LinearLayout) findViewById(R.id.get_date_layout));
        TextView date_title = findViewById(R.id.get_date_title);
        date_title.setPaintFlags(date_title.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        get_date_button = findViewById(R.id.get_date_button);
        date_picker = findViewById(R.id.date_picker);
        date_title = findViewById(R.id.get_date_title);
        date_title.setText(getIntent().getExtras().getString("title"));

        get_date_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Picker() and Date() start at different years
                int year_offset = 1900;

                Date old_day = new Date();
                old_day.setYear(date_picker.getYear() - year_offset);
                old_day.setMonth(date_picker.getMonth());
                old_day.setDate(date_picker.getDayOfMonth());
                old_day.setHours(0);
                old_day.setMinutes(0);
                old_day.setSeconds(1);

                Intent intent = new Intent();
                intent.putExtra("date",old_day.getTime());
                setResult(GET_DATE_RET_OK, intent);
                finish();
            }
        });
    }
}
