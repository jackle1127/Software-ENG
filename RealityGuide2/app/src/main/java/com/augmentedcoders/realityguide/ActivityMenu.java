package com.augmentedcoders.realityguide;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.view.ViewGroup.LayoutParams;

import com.augmentedcoders.realityguide.R;

public class ActivityMenu extends AppCompatActivity {
    final Context currentContext = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Display thisDisplay = getWindowManager().getDefaultDisplay();
        float textSize = (float) Math.sqrt(thisDisplay.getWidth() * thisDisplay.getWidth() +
                thisDisplay.getHeight() * thisDisplay.getHeight()) / 50;
        String[] listItems = {"<<", "Post Something", "Account", "Advanced Settings",
                "Logout"};
        Button[] btnItem = new Button[listItems.length];
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        for (int i = 0; i < listItems.length; i++) {
            String item = listItems[i];
            btnItem[i] = new Button(this);
            btnItem[i].setText(item);
            btnItem[i].setTextSize(textSize);
            btnItem[i].setTextColor(0xFF444444);
            btnItem[i].setBackgroundColor(0xFFFFFFFF);
            btnItem[i].setLayoutParams(new LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            btnItem[i].setOnTouchListener(itemClick);
            LinearLayout linearLayout = (LinearLayout) findViewById(R.id.linItemList);
            linearLayout.addView(btnItem[i]);
            if (i < listItems.length - 1) {
                View separator = new View(this);
                separator.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 1));
                separator.setBackgroundColor(0xFF333333);
                linearLayout.addView(separator);
            }
        }
    }

    private OnTouchListener itemClick = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                v.setBackgroundColor(0xFF9999FF);
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                v.setBackgroundColor(0xFFFFFFFF);
                String command = ((Button) v).getText().toString();
                Intent toSwitch = null;
                switch (command) {
                    case "Advanced Settings":
                        toSwitch = new Intent(currentContext, ActivitySettings.class);
                        break;
                    case "<<":
                        toSwitch = new Intent(currentContext, ActivityDiscovery.class);
                        break;
                }
                if (toSwitch != null) {
                    startActivity(toSwitch);
                }
            }
            return true;
        }
    };
}
