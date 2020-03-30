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
import java.util.Date;
import java.util.Locale;
import java.util.Vector;
import java.util.jar.Attributes;

import static com.google.android.gms.common.internal.safeparcel.SafeParcelable.NULL;

/**
 * Created by Preston on 3/4/2020.
 */

public class NeedsActivity extends MainActivity {
    LinearLayout main_layout = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.needs);
        main_layout = findViewById(R.id.needs_main);

        draw_needs_screen();

        Button b = findViewById(R.id.needs_add_item);
        b.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
            Intent launchAddItem = new Intent(v.getContext(), AddItem.class);
            int requested_code = ADD_ITEM_RET_OK;
            startActivityForResult(launchAddItem, requested_code);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == ADD_ITEM_RET_OK) {
            double price = data.getExtras().getDouble("price");
            String description = data.getExtras().getString("description");
            int date = get_seconds_from_ms((new Date()).getTime());
            purchase_item p = new purchase_item(price, description, date);
            insert_purchase(p);
            main_layout.addView(create_view(p));
            Toast.makeText(this, "Purchase Saved", Toast.LENGTH_SHORT).show();
        }
        else if (resultCode == EDIT_ITEM_RET_OK){
            int id = last_purchase_clicked.date;
            remove_purchase_by_id(id);
            double price = data.getExtras().getDouble("price");
            String description = data.getExtras().getString("description");
            purchase_item p = new purchase_item(price, description, id);
            insert_purchase(p);
            TextView t = findViewById(id);
            t.setText(get_purchase_text(p));
        }
        else if (resultCode == EDIT_ITEM_RET_DELETE){
            TextView t = findViewById(last_purchase_clicked.date);
            remove_purchase_by_id(t.getId());
            ((ViewGroup) t.getParent()).removeView(t);
        }
        update_background();
        update_total();
    }

    TextView create_view(purchase_item p){
        TextView t = new TextView(this);
        String text = get_purchase_text(p);
        t.setText(text);
        t.setGravity(Gravity.CENTER);
        t.setTextSize(20);
        t.setOnClickListener(purchase_clicked);

        t.setId(p.date);
        t.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));
        return t;
    }

    void draw_needs_screen(){
        TextView[] tvs = get_purchase_textviews();
        for (int i = 0; i < tvs.length; i++){
            if (tvs[i] ==  null){break;}
            ((ViewGroup) tvs[i].getParent()).removeView(tvs[i]);
        }

        double total = 0;
        Vector<purchase_item> needs = get_all_purchases();
        while(needs != null && !needs.isEmpty()){
            TextView t = create_view(needs.firstElement());
            total += needs.firstElement().price;
            main_layout.addView(t);
            needs.removeElementAt(0);
        }
        ((TextView) findViewById(R.id.needs_view_total)).setText(get_total_text(total));
        update_background();
    }

    void update_background(){
        TextView[] tvs = get_purchase_textviews();
        for (int i = 0; i < tvs.length; i++){
            if (tvs[i] == null){break;}
            if (i % 2 == 0) {
                tvs[i].setBackgroundColor(Color.parseColor("#c0c0c0"));
            }
            else{
                tvs[i].setBackgroundColor(Color.parseColor("#FFFFFF"));
            }
        }
    }

    void update_total(){
        TextView[] tvs = get_purchase_textviews();
        int total = 0;
        for (int i = 0; i < tvs.length; i++) {
            if (tvs[i] == null){break;}
            total += get_purchase_item_from_view(tvs[i]).price;
        }
        ((TextView) findViewById(R.id.needs_view_total)).setText(get_total_text(total));
    }

    TextView[] get_purchase_textviews(){
        int children = main_layout.getChildCount();
        TextView[] tvs = new TextView[children];
        int tv_iterator = 0;
        for (int i = 0; i < children; i++){
            View v = main_layout.getChildAt(i);
            if (!(v instanceof TextView)){continue;}
            TextView tv = (TextView) v;
            if (tv.getId() != R.id.needs_view_total && tv.getId() != R.id.needs_add_item){
                tvs[tv_iterator] = tv;
                tv_iterator += 1;
            }
        }
        return tvs;
    }

    String get_purchase_text(purchase_item p){
        return String.format(Locale.US, "$%.2f\n %s", p.price, p.description);
    }

    String get_total_text(double d){
        return String.format(Locale.US,"$%.2f", d);
    }

    View.OnClickListener purchase_clicked = new View.OnClickListener(){
        public void onClick(View v){
            if (!(v instanceof TextView)){return;}
            purchase_item p = get_purchase_item_from_view((TextView) v);
            Intent launchAddItem = new Intent(v.getContext(), EditItem.class);
            launchAddItem.putExtra("price", p.price);
            launchAddItem.putExtra("description", p.description);
            int requested_code = EDIT_ITEM_RET_OK;
            startActivityForResult(launchAddItem, requested_code);
            last_purchase_clicked = p;
        }
    };



}
