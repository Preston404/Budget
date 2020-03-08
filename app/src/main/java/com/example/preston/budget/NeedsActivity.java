package com.example.preston.budget;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
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

public class NeedsActivity extends AppCompatActivity {
    String[] needs_items = {"this", "that", "other"};
    FragmentManager manager = getFragmentManager();
    FragmentTransaction transaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.needs);

        LinearLayout main = findViewById(R.id.needs_main);
        TextView t = create_view(needs_items[0], 55);
        main.addView(t);

        Button b = findViewById(R.id.needs_add_item);
        b.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                myFragment frag = new myFragment();
                transaction = manager.beginTransaction();
                transaction.replace(R.id.fragment_layout, frag);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
    }

    TextView create_view(String title, int id){
        TextView t = new TextView(this);
        t.setText(title);
        t.setGravity(Gravity.CENTER);
        t.setTextSize(20);
        t.setId(id);
        t.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));
        return t;
    }

    public static class myFragment extends Fragment implements View.OnClickListener {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
            return inflater.inflate(R.layout.add_item, container, false);
        }

        @Override
        public void onClick(View v){
            Toast.makeText(v.getContext(), "Hello world", Toast.LENGTH_LONG).show();
        }
    }
}
