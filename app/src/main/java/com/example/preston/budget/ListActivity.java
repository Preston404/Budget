package com.example.preston.budget;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Vector;
import java.util.Calendar;


/**
 * Created by Preston on 3/4/2020.
 */

public class ListActivity extends MainActivity
{
    LinearLayout main_layout = null;
    final Integer[] static_views = new Integer[]{R.id.list_view_total, R.id.list_add_item, R.id.list_title};

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list);
        main_layout = findViewById(R.id.list_main);
        list_view_type = getIntent().getExtras().getInt("list_view_type");

        TextView list_title = findViewById(R.id.list_title);
        list_title.setPaintFlags(list_title.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        if (list_view_type == NEEDS_LIST_VIEW)
        {
            list_title.setText("Needs");
        }
        else if (list_view_type == WANTS_LIST_VIEW)
        {
            list_title.setText("WANTS");
        }
        else if (list_view_type == ALL_LIST_VIEW)
        {
            list_title.setText("ALL");
        }
        else
        {
            throw new RuntimeException("List view must be given a type.");
        }

        draw_list_screen();

        Button b = findViewById(R.id.list_add_item);
        b.setOnClickListener(
            new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if(list_view_type == ALL_LIST_VIEW)
                    {
                        return;
                    }
                    Intent launchAddItem = new Intent(v.getContext(), AddItem.class);
                    int requested_code = ADD_ITEM_RET_OK;
                    startActivityForResult(launchAddItem, requested_code);
                }
            }
        );

        Button filter_button = findViewById(R.id.list_filter);

        filter_button.setOnClickListener(
                new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Intent launchGetFilter = new Intent(v.getContext(), GetFilter.class);
                        int requested_code = GET_FILTER_RET_OK;
                        startActivityForResult(launchGetFilter, requested_code);
                    }
                }
        );

        if(list_view_type == ALL_LIST_VIEW)
        {
            main_layout.removeView(b);
        }
        else
        {
            main_layout.removeView(filter_button);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == ADD_ITEM_RET_OK)
        {
            double price = data.getExtras().getDouble("price");
            String description = data.getExtras().getString("description");
            int date = get_seconds_from_ms((new Date()).getTime());
            int purchase_type = IS_A_NEED;
            if(list_view_type != NEEDS_LIST_VIEW)
            {
                purchase_type = IS_NOT_A_NEED;
            }
            purchase_item p = new purchase_item(
                price,
                description,
                date,
                purchase_type
            );
            insert_purchase(p);
            int top_purchase_index = 3;
            main_layout.addView(create_purchase_view(p), top_purchase_index);
            Toast.makeText(this, "Purchase Saved", Toast.LENGTH_SHORT).show();
        }
        else if (resultCode == EDIT_ITEM_RET_OK)
        {
            int id = last_purchase_clicked.date;
            remove_purchase_by_id(id);
            double price = data.getExtras().getDouble("price");
            String description = data.getExtras().getString("description");
            purchase_item p = new purchase_item(
                price,
                description,
                id,
                list_view_type
            );
            insert_purchase(p);
            TextView t = findViewById(id);
            t.setText(get_purchase_text(p));
            Toast.makeText(this, "Purchase Edited", Toast.LENGTH_SHORT).show();
        }
        else if (resultCode == EDIT_ITEM_RET_DELETE)
        {
            TextView t = findViewById(last_purchase_clicked.date);
            remove_purchase_by_id(t.getId());
            ((ViewGroup) t.getParent()).removeView(t);
            Toast.makeText(this, "Purchase Removed", Toast.LENGTH_SHORT).show();
        }
        else if (resultCode == GET_FILTER_RET_OK)
        {
            Calendar calendar = Calendar.getInstance();
            int seconds_today = calendar.get(Calendar.MINUTE) * 60 + calendar.get(Calendar.HOUR_OF_DAY) * 3600;
            int filter_seconds_ago_max = (data.getExtras().getInt("days_max") * seconds_in_a_day);
            if (filter_seconds_ago_max != 0)
            {
                filter_seconds_ago_max = filter_seconds_ago_max + seconds_today;
            }
            int filter_seconds_ago_min = (data.getExtras().getInt("days_min") * seconds_in_a_day);
            if (filter_seconds_ago_min != 0)
            {
                filter_seconds_ago_min = filter_seconds_ago_min - (seconds_in_a_day - seconds_today);
            }

            String filter_string = data.getExtras().getString("string");
            Integer needs_filter = data.getExtras().getInt("needs");
            Double filter_price_max = data.getExtras().getDouble("price_max");
            Double filter_price_min = data.getExtras().getDouble("price_min");
            hide_purchase_views(
                    filter_seconds_ago_max,
                    filter_seconds_ago_min,
                    needs_filter,
                    filter_string,
                    filter_price_max,
                    filter_price_min
            );
        }
        update_background();
        update_total();
    }

    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent();
        double total = Double.parseDouble(((TextView) findViewById(R.id.list_view_total)).getText().toString().substring(1));
        intent.putExtra("total", total);
        int result_code = -1;
        if(list_view_type == NEEDS_LIST_VIEW)
        {
            result_code = NEEDS_RET_OK;
        }
        if(list_view_type == WANTS_LIST_VIEW)
        {
            result_code = WANTS_RET_OK;
        }
        if(list_view_type == ALL_LIST_VIEW)
        {
            result_code = ALL_RET_OK;
        }
        setResult(result_code, intent);
        finish();
        super.onBackPressed();
    }

    void hide_purchase_views(
            int filter_seconds_ago_max,
            int filter_seconds_ago_min,
            int needs_filter,
            String filter_string,
            double filter_price_max,
            double filter_price_min
    )
    {
        int seconds_now = get_seconds_from_ms((new Date()).getTime());
        // Skip over Title and Total views
        for (int i = main_layout.getChildCount(); i > 2; i--)
        {
            boolean remove_it = false;
            purchase_item p = get_purchase_item_from_view((TextView) main_layout.getChildAt(i));
            if (p == null)
            {
                continue;
            }
            int time_ago = seconds_now - p.date;

            if (needs_filter == FILTER_NEEDS_ONLY && p.need != IS_A_NEED)
            {
                remove_it = true;
            }
            else if (needs_filter == FILTER_WANTS_ONLY && p.need != IS_NOT_A_NEED)
            {
                remove_it = true;
            }
            else if (!filter_string.equals("") && !p.description.toLowerCase().contains(filter_string.toLowerCase()))
            {
                remove_it = true;
            }
            else if (filter_seconds_ago_max != 0 && time_ago > filter_seconds_ago_max)
            {
                remove_it = true;
            }
            else if (filter_seconds_ago_min != 0 && time_ago < filter_seconds_ago_min)
            {
                remove_it = true;
            }
            else if (filter_price_min != 0 && p.price < filter_price_min)
            {
                remove_it = true;
            }
            else if (filter_price_max != 0 && p.price > filter_price_max)
            {
                remove_it = true;
            }

            if (remove_it)
            {
                main_layout.removeView(main_layout.getChildAt(i));
            }
        }
    }

    TextView create_purchase_view(purchase_item p)
    {
        TextView t = new TextView(this);
        String text = get_purchase_text(p);
        t.setText(text);
        t.setGravity(Gravity.CENTER);
        t.setTextSize(20);
        t.setOnClickListener(purchase_clicked);
        t.setId(p.date);
        t.setLayoutParams(
            new ViewGroup.LayoutParams
            (
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        );
        return t;
    }

    void draw_list_screen()
    {
        Vector<TextView> textviews = get_purchase_textviews();
        while(!textviews.isEmpty())
        {
            ((ViewGroup) textviews.get(0).getParent()).removeView(textviews.get(0));
            textviews.remove(0);
        }

        double total = 0;

        int filter = FILTER_NEEDS_ONLY;
        if (list_view_type == WANTS_LIST_VIEW)
        {
            filter = FILTER_WANTS_ONLY;
        }
        else if (list_view_type == ALL_LIST_VIEW)
        {
            filter = FILTER_ALL;
        }
        Vector<purchase_item> purchase_items = get_all_purchases(filter);
        purchase_items = sort_purchases_newest_first(purchase_items);
        if(filter != FILTER_ALL)
        {
            purchase_items = filter_purchases_this_period(purchase_items);
        }
        while(purchase_items != null && !purchase_items.isEmpty())
        {
            int first_in_list = 0;
            TextView t = create_purchase_view(purchase_items.get(first_in_list));
            main_layout.addView(t);
            total += purchase_items.get(first_in_list).price;
            purchase_items.removeElementAt(first_in_list);
        }
        ((TextView) findViewById(R.id.list_view_total)).setText(get_list_total_string(total));
        update_background();
    }

    void update_background()
    {
        Vector<TextView> purchase_textviews = get_purchase_textviews();
        int view_num = 0;
        while(!purchase_textviews.isEmpty())
        {
            int first_in_list = 0;
            if (view_num % 2 != 0)
            {
                purchase_textviews.get(first_in_list).setBackgroundColor(getResources().getColor(R.color.colorLightBlue));
            }
            else{
                purchase_textviews.get(first_in_list).setBackgroundColor(getResources().getColor(R.color.colorWhite));
            }
            view_num++;
            purchase_textviews.remove(first_in_list);
        }
    }

    void update_total()
    {
        Vector<TextView> purchase_textviews = get_purchase_textviews();
        double total = 0;
        while(!purchase_textviews.isEmpty())
        {
            int first_in_list = 0;
            total += get_purchase_item_from_view(purchase_textviews.get(first_in_list)).price;
            purchase_textviews.remove(first_in_list);
        }
        ((TextView) findViewById(R.id.list_view_total)).setText(get_list_total_string(total));
    }

    Vector<TextView> get_purchase_textviews()
    {
        int num_children = main_layout.getChildCount();
        Vector<TextView> purchase_textviews = new Vector<TextView>();
        for (int i = 0; i < num_children; i++)
        {
            View v = main_layout.getChildAt(i);
            if (!(v instanceof TextView) || (v instanceof Button)){continue;}
            TextView tv = (TextView) v;
            if (!Arrays.asList(static_views).contains(tv.getId()))
            {
                purchase_textviews.add(tv);
            }
        }
        return purchase_textviews;
    }

    String get_purchase_text(purchase_item p)
    {
        return String.format(
            Locale.US,
            "$%.2f\n%s",
            p.price,
            p.description
        );
    }

    View.OnClickListener purchase_clicked = new View.OnClickListener()
    {
        public void onClick(View v)
        {
            if (!(v instanceof TextView) || list_view_type == ALL_LIST_VIEW)
            {
                return;
            }
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
