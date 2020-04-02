package com.example.preston.budget;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Paint;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Vector;
import java.util.jar.Attributes;

import static com.google.android.gms.common.internal.safeparcel.SafeParcelable.NULL;

/**
 * Created by Preston on 3/4/2020.
 */

public class ListActivity extends MainActivity {
    LinearLayout main_layout = null;
    final Integer[] static_views = new Integer[]{R.id.list_view_total, R.id.list_add_item, R.id.list_title};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list);
        main_layout = findViewById(R.id.list_main);
        needs = getIntent().getExtras().getInt("needs");

        TextView list_title = findViewById(R.id.list_title);
        list_title.setPaintFlags(list_title.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        if (needs == 1){list_title.setText("Needs");}
        else{list_title.setText("Wants");}

        draw_list_screen();

        Button b = findViewById(R.id.list_add_item);
        b.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        b.setTextColor(getResources().getColor(R.color.colorWhite));
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
            purchase_item p = new purchase_item(price, description, date, needs);
            insert_purchase(p);
            main_layout.addView(create_purchase_view(p));
            Toast.makeText(this, "Purchase Saved", Toast.LENGTH_SHORT).show();
        }
        else if (resultCode == EDIT_ITEM_RET_OK){
            int id = last_purchase_clicked.date;
            remove_purchase_by_id(id);
            double price = data.getExtras().getDouble("price");
            String description = data.getExtras().getString("description");
            purchase_item p = new purchase_item(price, description, id, needs);
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

    @Override
    public void onBackPressed(){
        Intent intent = new Intent();
        double total = Double.parseDouble(((TextView) findViewById(R.id.list_view_total)).getText().toString().substring(1));
        intent.putExtra("total", total);
        int resultCode = (needs == 1) ? NEEDS_RET_OK : WANTS_RET_OK;
        setResult(resultCode, intent);
        finish();
        super.onBackPressed();
    }

    TextView create_purchase_view(purchase_item p){
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

    void draw_list_screen(){
        Vector<TextView> tvs = get_purchase_textviews();
        while(!tvs.isEmpty()){
            ((ViewGroup) tvs.get(0).getParent()).removeView(tvs.get(0));
            tvs.remove(0);
        }

        double total = 0;
        int filter = (needs == 1) ? NEEDS_ONLY : WANTS_ONLY;
        Vector<purchase_item> list_items = get_all_purchases(filter);
        while(list_items != null && !list_items.isEmpty()){
            TextView t = create_purchase_view(list_items.get(0));
            main_layout.addView(t);
            total += list_items.get(0).price;
            list_items.removeElementAt(0);
        }
        ((TextView) findViewById(R.id.list_view_total)).setText(get_total_text(total));
        update_background();
    }

    void update_background(){
        Vector<TextView> tvs = get_purchase_textviews();
        int i = 0;
        while(!tvs.isEmpty()){
            if (i % 2 == 0) {
                tvs.get(0).setBackgroundColor(getResources().getColor(R.color.colorLightBlue));
            }
            else{
                tvs.get(0).setBackgroundColor(getResources().getColor(R.color.colorWhite));
            }
            i++;
            tvs.remove(0);
        }
    }

    void update_total(){
        Vector<TextView> tvs = get_purchase_textviews();
        int total = 0;
        while(!tvs.isEmpty()) {
            total += get_purchase_item_from_view(tvs.get(0)).price;
            tvs.remove(0);
        }
        ((TextView) findViewById(R.id.list_view_total)).setText(get_total_text(total));
    }

    Vector<TextView> get_purchase_textviews(){
        int children = main_layout.getChildCount();
        Vector<TextView> tvs = new Vector<TextView>();
        for (int i = 0; i < children; i++){
            View v = main_layout.getChildAt(i);
            if (!(v instanceof TextView)){continue;}
            TextView tv = (TextView) v;
            if (!Arrays.asList(static_views).contains(tv.getId())){
                tvs.add(tv);
            }
        }
        return tvs;
    }

    String get_purchase_text(purchase_item p){
        return String.format(Locale.US, "$%.2f\n%s", p.price, p.description);
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
