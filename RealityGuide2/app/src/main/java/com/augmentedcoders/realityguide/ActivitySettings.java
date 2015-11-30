package com.augmentedcoders.realityguide;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.augmentedcoders.realityguide.R;

public class ActivitySettings extends AppCompatActivity {
    final Context currentContext = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        findViewById(R.id.btnOk).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(currentContext, ActivityMenu.class));
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
            changeCamQuality((SeekBar) findViewById(R.id.sbrCameraQuality),
                    ((SeekBar) findViewById(R.id.sbrCameraQuality)).getProgress());
        }
        ((SeekBar) findViewById(R.id.sbrCameraQuality)).setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                        changeCamQuality(seekBar, i);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });
        RadioGroup locations = (RadioGroup) findViewById(R.id.rgrpLocation);
        for (int i = 0; i < locations.getChildCount(); i++) {
            RadioButton location = (RadioButton) locations.getChildAt(i);
            location.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (b) {
                        String label = compoundButton.getText().toString();
                        char lastChar = label.charAt(label.length() - 1);
                        if (lastChar >= '0' && lastChar <= '9') {
                            Settings.mockLocation = lastChar - '0' - 1;
                        } else {
                            Settings.mockLocation = -1;
                        }

                        if (Settings.mockLocation == 0) {
                            Settings.currentLat = Settings.MOCK_1.latitude;
                            Settings.currentLon = Settings.MOCK_1.longitude;
                        } else if (Settings.mockLocation == 1) {
                            Settings.currentLat = Settings.MOCK_2.latitude;
                            Settings.currentLon = Settings.MOCK_2.longitude;
                        } else if (Settings.mockLocation == 2) {
                            Settings.currentLat = Settings.MOCK_3.latitude;
                            Settings.currentLon = Settings.MOCK_3.longitude;
                        }

                        System.out.println(Settings.mockLocation);
                        saveSettings();
                    }
                }
            });
        }
        if (Settings.mockLocation >= 0) {
            ((RadioButton) locations.getChildAt(Settings.mockLocation)).setChecked(true);
        }
    }

    private void changeCamQuality(SeekBar seekBar, int i) {
        Settings.cameraQuality = Settings.numberOfCameras - 1 - i;
        String toShow = "Camera Quality: " + (i + 1);
        if (i == seekBar.getMax()) toShow += " Slowest";
        if (i == 0) toShow += " Fastest";
        ((TextView) findViewById(R.id.txtCamQuality)).setText(toShow);
        saveSettings();
    }
    private void saveSettings() {
        SharedPreferences.Editor edit =
                getSharedPreferences(Settings.PREFERENCES_NAME, MODE_PRIVATE).edit();
        edit.putBoolean("gyroMode", Settings.gyroMode);
        edit.putInt("cameraQuality", Settings.cameraQuality);
        edit.putInt("mockLocation", Settings.mockLocation);
        edit.putString("settingSaved", "saved");
        System.out.println("saved!!!!!");
        edit.commit();
    }

    public void onDestroy() {
        saveSettings();
        super.onDestroy();
    }
}
