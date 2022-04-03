package com.example.preston.budget;

import android.content.Intent;
import android.graphics.Paint;
import androidx.biometric.BiometricPrompt;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import java.util.Locale;
import java.util.concurrent.Executor;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;


public class MainActivity extends Utils {

    private boolean authenticated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        update_textviews();

        // Stuff for Summary
        final TextView summary = findViewById(R.id.main_summary_title);
        summary.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Toggle between showing summary for amount spent vs remaining
                    settings_config c = read_settings_config_from_db();
                    c.show_remaining = (c.show_remaining == 0) ? 1 : 0;
                    insert_config_into_db(c);
                    set_main_summary();
                }
            }
        );

        // Stuff for "needs"
        final TextView needs = findViewById(R.id.needs);
        needs.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent launchNeeds = new Intent(v.getContext(), ListActivity.class);
                        launchNeeds.putExtra("list_view_type", NEEDS_LIST_VIEW);
                        startActivityForResult(launchNeeds, NEEDS_RET_OK);
                    }
                }
        );

        // Stuff for "wants"
        final TextView wants = findViewById(R.id.wants);
        wants.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent launchWants = new Intent(v.getContext(), ListActivity.class);
                        launchWants.putExtra("list_view_type", WANTS_LIST_VIEW);
                        startActivityForResult(launchWants, WANTS_RET_OK);
                    }
                }
        );

        // Stuff for "all"
        final TextView all = findViewById(R.id.all);
        all.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent launchAll = new Intent(v.getContext(), ListActivity.class);
                        launchAll.putExtra("list_view_type", ALL_LIST_VIEW);
                        startActivityForResult(launchAll, ALL_RET_OK);
                    }
                }
        );

        final TextView settings = findViewById(R.id.main_settings_title);
        settings.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent launchSettings = new Intent(v.getContext(), EditSettings.class);
                        startActivityForResult(launchSettings, EDIT_SETTINGS_RET_OK);
                    }
                }
        );

        final TextView categories = findViewById(R.id.main_categories_title);
        categories.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent launchSettings = new Intent(v.getContext(), Categories.class);
                        startActivityForResult(launchSettings, EDIT_CATEGORIES_RET_OK);
                    }
                }
        );

        if (!this.authenticated) {
            do_authentication();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == NEEDS_RET_OK) {
            double total = data.getExtras().getDouble("total");
            String text = String.format(Locale.US, "$%.2f", total);
            ((TextView) findViewById(R.id.main_needs_amount)).setText(text);
            update_textviews();
        } else if (resultCode == WANTS_RET_OK) {
            double total = data.getExtras().getDouble("total");
            String text = String.format(Locale.US, "$%.2f", total);
            ((TextView) findViewById(R.id.main_wants_amount)).setText(text);
            update_textviews();
        } else if (requestCode == EDIT_SETTINGS_RET_OK || requestCode == ALL_RET_OK) {
            update_textviews();
        }
    }


    public void update_textviews() {
        TextView main_title = findViewById(R.id.main_title);
        main_title.setPaintFlags(main_title.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        set_text_size_for_child_views((LinearLayout) findViewById(R.id.main_linear_layout));

        settings_config c = read_settings_config_from_db();

        TextView needs_total = findViewById(R.id.main_needs_amount);
        needs_total.setText(get_list_total_string(get_total_spent(FILTER_NEEDS_ONLY, PERIODS_THIS)));

        TextView wants_total = findViewById(R.id.main_wants_amount);
        wants_total.setText(get_list_total_string(get_total_spent(FILTER_WANTS_ONLY, PERIODS_THIS)));

        set_main_summary();

    }


    void set_main_summary() {

        settings_config c = read_settings_config_from_db();
        double spent = get_total_spent(FILTER_ALL, PERIODS_THIS);
        TextView t = findViewById(R.id.main_summary);

        if(c.show_remaining != 0) // Show remaining amount
        {
            int days_remaining = get_days_until_day_of_month(c.start_day);
            double dollars_remaining = c.monthly_goal_amount - spent;
            String text = String.format(
                    Locale.US,
                    "Remaining:\n$%.2f and %d days",
                    dollars_remaining,
                    days_remaining
            );
            t.setText(text);
        }
        else // Show current spending
        {
            int days_passed = get_days_since_day_of_month(c.start_day);
            String text = String.format(
                    Locale.US,
                    "Spent:\n$%.2f over %d days",
                    spent,
                    days_passed
            );
            t.setText(text);
        }
    }


    public void do_authentication() {

        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt biometricPrompt = new BiometricPrompt(MainActivity.this,
                executor, new BiometricPrompt.AuthenticationCallback() {

            @Override
            public void onAuthenticationError(int errorCode,
                                              @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(getApplicationContext(),
                        "Authentication error: " + errString, Toast.LENGTH_SHORT)
                        .show();
                System.exit(0);
            }

            @Override
            public void onAuthenticationSucceeded(
                    @NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Toast.makeText(getApplicationContext(),
                        "Authentication succeeded!", Toast.LENGTH_SHORT).show();
                authenticated = true;
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(getApplicationContext(), "Authentication failed",
                        Toast.LENGTH_SHORT)
                        .show();
                System.exit(0);
            }
        });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Authentication")
                .setSubtitle("")
                .setNegativeButtonText("Cancel")
                .build();

        biometricPrompt.authenticate(promptInfo);
    }
}
