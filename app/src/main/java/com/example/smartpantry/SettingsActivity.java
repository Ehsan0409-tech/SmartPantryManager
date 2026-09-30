package com.example.smartpantry;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.material.appbar.MaterialToolbar;

/** Settings screen with a toggle for expiring-soon alerts (saved in SharedPreferences). */
public class SettingsActivity extends AppCompatActivity {

    public static final String PREFS = "smart_pantry_prefs";
    public static final String KEY_EXPIRY_ALERTS = "expiry_alerts";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Settings");

        SharedPreferences prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        SwitchCompat sw = findViewById(R.id.switchExpiry);
        sw.setChecked(prefs.getBoolean(KEY_EXPIRY_ALERTS, true));
        sw.setOnCheckedChangeListener((button, checked) ->
                prefs.edit().putBoolean(KEY_EXPIRY_ALERTS, checked).apply());
    }

    @Override
    protected void onResume() {
        super.onResume();
        NavHelper.setup(this, R.id.nav_settings);
    }
}
