package com.example.jack.opengltest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;

public class Setting extends AppCompatActivity {

    static boolean useGyro = true;
    static int cameraQuality = 0;
    TextView cameraText;
    SeekBar cameraBar;
    static int numOfCam = 0;
    boolean instantiated = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        cameraText = (TextView) findViewById(R.id.cameraText);
        cameraBar = (SeekBar) findViewById(R.id.cameraQuality);
        cameraBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (instantiated) {
                    cameraQuality = cameraBar.getMax() - cameraBar.getProgress();
                    changeQualityText();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        CheckBox gyroCheckBox = (CheckBox) findViewById(R.id.gyroCheckBox);
        cameraBar.setMax(numOfCam - 1);
        cameraBar.setProgress(cameraBar.getMax() - cameraQuality);
        changeQualityText();
        gyroCheckBox.setChecked(useGyro);
        gyroCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                useGyro = b;
            }
        });
        Button okButton = (Button) findViewById(R.id.okButton);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        instantiated = true;
    }
    private void changeQualityText() {
        cameraText.setText("Camera Quality: " + (cameraBar.getProgress() + 1));
        if (cameraBar.getProgress() == 0) {
            cameraText.setText(cameraText.getText() + " Fastest");
        }
        if (cameraBar.getProgress() == cameraBar.getMax()) {
            cameraText.setText(cameraText.getText() + " Slowest");
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_setting, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
