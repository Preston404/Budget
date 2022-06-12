package com.example.preston.budget;

import android.content.Context;
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
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;


/**
 * Created by Preston on 3/4/2020.
 */

public class ListActivity extends Utils
{
    LinearLayout main_layout = null;
    final Integer[] static_views = new Integer[]{R.id.list_view_total, R.id.list_add_item, R.id.list_title};
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list);

        main_layout = findViewById(R.id.list_main);
        list_view_type = getIntent().getExtras().getInt("list_view_type");
        context = this;

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

        if(list_view_type == ALL_LIST_VIEW)
        {
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
            Button graph_button = findViewById(R.id.list_graph);
            graph_button.setOnClickListener(
                    new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            Intent launchGraph = new Intent(v.getContext(), Graph.class);
                            String graph_values = "";
                            Vector<TextView> purchases_in_view = get_purchase_textviews();
                            for(int i=0; i<purchases_in_view.size(); i++)
                            {
                                purchase_item p = get_purchase_item_from_view(purchases_in_view.elementAt(i),null);
                                if(p != null)
                                {
                                    graph_values = graph_values.concat(((Double) p.price).toString());
                                    graph_values = graph_values.concat(" ");
                                }
                            }
                            launchGraph.putExtra("values", graph_values);
                            int requested_code = GRAPH_RET_OK;
                            startActivityForResult(launchGraph, requested_code);
                        }
                    }
            );

            // Can't add purchases from ALL view
            main_layout.removeView(findViewById(R.id.list_add_item));
        }
        else
        {
            Button add_button = findViewById(R.id.list_add_item);
            add_button.setOnClickListener(
                new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Intent launchAddItem = new Intent(v.getContext(), AddItem.class);
                        int requested_code = ADD_ITEM_RET_OK;
                        startActivityForResult(launchAddItem, requested_code);
                    }
                }
            );
            // Only allow filtering and graphing from ALL view
            main_layout.removeView(findViewById(R.id.list_filter));
            main_layout.removeView(findViewById(R.id.list_graph));
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
            String category = data.getExtras().getString("category");
            int purchase_type = IS_A_NEED;
            if(list_view_type != NEEDS_LIST_VIEW)
            {
                purchase_type = IS_NOT_A_NEED;
            }
            purchase_item p = new purchase_item(
                price,
                description,
                date,
                purchase_type,
                category
            );
            int new_date = insert_purchase(p);
            // insert_purchase may adjust the date to make it unique
            p.date = new_date;
            int top_purchase_index = 3;
            main_layout.addView(create_purchase_view(p), top_purchase_index);
            Toast.makeText(this, "Purchase Saved", Toast.LENGTH_SHORT).show();
        }
        else if (resultCode == EDIT_ITEM_RET_OK)
        {
            int id = last_purchase_clicked.date;
            double price = data.getExtras().getDouble("price");
            String description = data.getExtras().getString("description");
            String category = data.getExtras().getString("category");
            // Convert the list view type returned to the need type... sigh
            Integer view_type = data.getExtras().getInt("view_type");
            Integer purchase_type = IS_A_NEED;
            if(view_type != NEEDS_LIST_VIEW)
            {
                purchase_type = IS_NOT_A_NEED;
            }
            Integer date = data.getExtras().getInt("date");

            purchase_item p = new purchase_item(
                price,
                description,
                date,
                purchase_type,
                category
            );

            // Update the database
            if(remove_purchase_by_id(id))
            {
                // insert_purchase may adjust the date to make it unique
                p.date = insert_purchase(p);
                TextView t = findViewById(id);
                if (view_type == list_view_type || ALL_LIST_VIEW == list_view_type)
                {
                    if(p.date != t.getId())
                    {
                        int old_id = t.getId();
                        t = create_purchase_view(p);
                        replace_child_with_id(old_id, t);
                    }
                    t.setText(get_purchase_text(p));
                }
                else
                {
                    // Needs/Wants changed, don't show this purchase here anymore
                    ((LinearLayout) findViewById(R.id.list_main)).removeView(t);
                }
                Toast.makeText(this, "Purchase Edited", Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(this, "Failed to Edit Purchase", Toast.LENGTH_SHORT).show();
            }
        }
        else if (resultCode == EDIT_ITEM_RET_DELETE)
        {
            TextView t = findViewById(last_purchase_clicked.date);
            if(remove_purchase_by_id(t.getId()))
            {
                ((ViewGroup) t.getParent()).removeView(t);
                Toast.makeText(this, "Purchase Removed", Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(this, "Failed to Remove Purchase", Toast.LENGTH_SHORT).show();
            }
        }
        else if (resultCode == GET_FILTER_RET_OK)
        {
            apply_filter(
                    data.getExtras().getLong("day_start"),
                    data.getExtras().getLong("day_end"),
                    data.getExtras().getInt("needs"),
                    data.getExtras().getString("string"),
                    data.getExtras().getDouble("price_max"),
                    data.getExtras().getDouble("price_min"),
                    data.getExtras().getInt("sort_price")
            );
        }
        update_background();
        update_total();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        double total = Double.parseDouble(((TextView) findViewById(R.id.list_view_total)).getText().toString().substring(1));
        intent.putExtra("total", total);
        int result_code = -1;
        if (list_view_type == NEEDS_LIST_VIEW) {
            result_code = NEEDS_RET_OK;
        }
        if (list_view_type == WANTS_LIST_VIEW) {
            result_code = WANTS_RET_OK;
        }
        if (list_view_type == ALL_LIST_VIEW) {
            result_code = ALL_RET_OK;
        }
        setResult(result_code, intent);
        finish();
        super.onBackPressed();
    }

    void apply_filter(
            long day_start,
            long day_end,
            int needs_filter,
            String filter_string,
            double filter_price_max,
            double filter_price_min,
            int sort_price
    )
    {
        Vector<purchase_item> purchases = get_all_purchases(needs_filter);
        purchases = sort_purchases_newest_first(purchases);
        // Iterate backward so we don't mess up the indexes when we remove
        // an element.
        for (int i = purchases.size()-1; i>=0; i--)
        {
            boolean remove_it = false;
            purchase_item p = purchases.elementAt(i);
            if (p == null)
            {
                continue;
            }

            long purchase_time = get_ms_from_seconds(p.date);
            if (!filter_string.equals("") && !p.description.toLowerCase().contains(filter_string.toLowerCase()))
            {
                remove_it = true;
            }
            else if (day_start != 0 && purchase_time < day_start)
            {
                remove_it = true;
            }
            else if (day_end != 0 && purchase_time > day_end)
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
                purchases.removeElementAt(i);
            }
        }
        // Sort the purchases by price. Index zero will be the first
        // purchase displayed, at top of list.
        if (!SORT_OPTIONS[sort_price].contains("None"))
        {
            // Nested for loops go brrrrrr
            for (int i = purchases.size()-1; i > 0; i--)
            {
                boolean done_sorting = true;
                // We look one ahead, so we can't iterate over the
                // final purchase view.
                for (int j = purchases.size()-1; j > 0; j--)
                {
                    purchase_item lower = purchases.elementAt(j);
                    purchase_item upper = purchases.elementAt(j-1);
                    if (lower == null || upper == null)
                    {
                        continue;
                    }
                    if (SORT_OPTIONS[sort_price].contains("Max to Min") && upper.price < lower.price)
                    {
                        purchase_item temp = purchases.elementAt(j);
                        purchases.removeElementAt(j);
                        purchases.insertElementAt(temp,j-1);
                        done_sorting = false;
                    }
                    else if (SORT_OPTIONS[sort_price].contains("Min to Max") && upper.price > lower.price)
                    {
                        purchase_item temp = purchases.elementAt(j);
                        purchases.removeElementAt(j);
                        purchases.insertElementAt(temp,j-1);
                        done_sorting = false;
                    }
                }
                if (done_sorting)
                {
                    break;
                }
            }
        }

        // Remove purchase views, skip over "Title", "Total", and "Button" views
        int num_views_to_skip = 3;
        // Also skip GRAPH button if needed
        if(list_view_type == ALL_LIST_VIEW)
        {
            num_views_to_skip++;
        }
        for (int i = main_layout.getChildCount(); i > (num_views_to_skip-1); i--)
        {
            if (main_layout.getChildAt(i) != null)
            {
                main_layout.removeViewAt(i);
            }
        }
        // Insert the filtered purchases
        for (int i = 0; i < purchases.size(); i++)
        {
            if (purchases.elementAt(i) != null)
            {
                purchase_item p = purchases.elementAt(i);
                main_layout.addView(create_purchase_view(p));
            }
        }
    }

    TextView create_purchase_view(purchase_item p)
    {
        TextView t = new TextView(this);
        String text = get_purchase_text(p);
        t.setText(text);
        t.setGravity(Gravity.CENTER);
        t.setTextSize(get_default_purchase_view_text_size());
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
        // Set text size for titles and buttons
        set_text_size_for_child_views((LinearLayout) findViewById(R.id.list_main));
        int purchase_view_text_size = get_default_purchase_view_text_size();

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
        purchase_items = filter_purchases_this_period(purchase_items);
        purchase_items = sort_purchases_newest_first(purchase_items);
        while(purchase_items != null && !purchase_items.isEmpty())
        {
            int first_in_list = 0;
            TextView t = create_purchase_view(purchase_items.get(first_in_list));
            t.setTextSize(purchase_view_text_size);
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
        Map<Integer, Integer> id_to_needs_map = get_id_to_needs_map();
        double total = 0;
        while(!purchase_textviews.isEmpty())
        {
            int first_in_list = 0;
            purchase_item p = get_purchase_item_from_view(purchase_textviews.get(first_in_list), id_to_needs_map);
            if(p != null)
            {
                total += p.price;
            }
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

    void replace_child_with_id(int id, TextView new_text_view)
    {
        for (int i = 0; i < main_layout.getChildCount(); i++)
        {
            if (main_layout.getChildAt(i).getId() == id)
            {
                main_layout.addView(new_text_view, i);
                main_layout.removeViewAt(i+1);
                break;
            }
        }
    }

    String get_purchase_text(purchase_item p)
    {
        String s = String.format(
            Locale.US,
            "$%.2f\n%s",
            p.price,
            p.description
        );
        if(!p.category.equals(""))
        {
            s = s.concat("\n");
            s = s.concat(p.category);
        }
        return s;
    }

    View.OnClickListener purchase_clicked = new View.OnClickListener()
    {
        public void onClick(View v)
        {
            if (!(v instanceof TextView))
            {
                return;
            }
            purchase_item p = get_purchase_item_from_view((TextView) v, null);
            if(p == null)
            {
                return;
            }
            Intent launchAddItem = new Intent(v.getContext(), EditItem.class);
            launchAddItem.putExtra("price", p.price);
            launchAddItem.putExtra("description", p.description);
            launchAddItem.putExtra("need", p.need);
            launchAddItem.putExtra("date", p.date);
            launchAddItem.putExtra("category", p.category);
            int requested_code = EDIT_ITEM_RET_OK;
            startActivityForResult(launchAddItem, requested_code);
            last_purchase_clicked = p;
        }
    };

    Map<Integer, Integer> get_id_to_needs_map()
    {
        Map<Integer, Integer> id_to_needs_map = new HashMap<Integer,Integer>();
        Vector<purchase_item> purchases = read_firebase(this,null,FILTER_ALL);
        for(purchase_item item : purchases)
        {
            id_to_needs_map.put(item.date, item.need);
        }
        return id_to_needs_map;
    }
}
