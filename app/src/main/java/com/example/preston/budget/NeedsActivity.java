package com.example.preston.budget;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.text.AttributedCharacterIterator;
import java.util.jar.Attributes;

import static com.google.android.gms.common.internal.safeparcel.SafeParcelable.NULL;

/**
 * Created by Preston on 3/4/2020.
 */

public class NeedsActivity extends MainActivity {
    LinearLayout main_layout = null;
    int next_id = 55;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.needs);
        main_layout = findViewById(R.id.needs_main);

        Button b = findViewById(R.id.needs_add_item);
        b.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
            Intent launchAddItem = new Intent(v.getContext(), AddItem.class);
            int requested_code = 0;
            startActivityForResult(launchAddItem, requested_code);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != ADD_ITEM_RET_OK){
            return;
        }
        double price = data.getExtras().getDouble("price");
        String description = data.getExtras().getString("description");
        TextView t = create_view(price, description, next_id);
        main_layout.addView(t);
        purchase_item item_to_add = new purchase_item();
        next_id += 1;

    }

    TextView create_view(double price, String description, int id){
        TextView t = new TextView(this);
        String text = String.format("$%.2f\n %s", price, description);
        if (id % 2 == 0){
            t.setBackgroundColor(Color.parseColor("#c0c0c0"));
            Toast.makeText(this, "Setting background", Toast.LENGTH_LONG).show();
        }
        t.setText(text);
        t.setGravity(Gravity.CENTER);
        t.setTextSize(20);
        t.setId(id);
        t.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));
        return t;
    }

}
