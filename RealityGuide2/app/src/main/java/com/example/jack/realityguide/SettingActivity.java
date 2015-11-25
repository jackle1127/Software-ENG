package com.example.jack.realityguide;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

public class SettingActivity extends AppCompatActivity {
    final Context currentContext = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        findViewById(R.id.btnOk).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(currentContext, MenuActivity.class));
                saveSettings();
            }
        });
        ((ToggleButton) findViewById(R.id.tglGyroscope)).setChecked(Settings.gyroMode);
        ((ToggleButton) findViewById(R.id.tglGyroscope)).setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Settings.gyroMode = b;
                saveSettings();
            }
        });
        if (Settings.numberOfCameras >= 0) {
            ((SeekBar) findViewById(R.id.sbrCameraQuality)).setMax(Settings.numberOfCameras - 1);
            ((SeekBar) findViewById(R.id.sbrCameraQuality)).setProgress(
                    Settings.numberOfCameras - 1 - Settings.cameraQuality);
        }
        ((SeekBar) findViewById(R.id.sbrCameraQuality)).setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                Settings.cameraQuality = Settings.numberOfCameras - 1 - i;
                ((TextView) findViewById(R.id.txtCamQuality)).setText("Camera Quality: " + (i + 1));
                saveSettings();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        ((TextView) findViewById(R.id.txtCamQuality)).setText("Camera Quality: " +
                (((SeekBar) findViewById(R.id.sbrCameraQuality)).getProgress() + 1));
    }

    private void saveSettings() {
        SharedPreferences.Editor edit =
                getSharedPreferences(Settings.PREFERENCES_NAME, MODE_PRIVATE).edit();
        edit.putBoolean("gyroMode", Settings.gyroMode);
        edit.putInt("cameraQuality", Settings.cameraQuality);
        edit.putString("settingSaved", "saved");
        System.out.println("saved!!!!!");
        edit.commit();
    }

    public void onDestroy() {
        saveSettings();
        super.onDestroy();
    }
}
