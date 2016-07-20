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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.augmentedcoders.realityguide.R;

public class ActivityMenu extends AppCompatActivity {
    final Context currentContext = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        (findViewById(R.id.ltBack)).setOnTouchListener(itemClick);
        (findViewById(R.id.ltPost)).setOnTouchListener(itemClick);
        (findViewById(R.id.ltAccount)).setOnTouchListener(itemClick);
        (findViewById(R.id.ltSettings)).setOnTouchListener(itemClick);
        (findViewById(R.id.ltLogout)).setOnTouchListener(itemClick);
        if (Settings.currentUser == null) {
            ((LinearLayout) findViewById(R.id.linItemList)).removeView((findViewById(R.id.ltPost)));
            ((LinearLayout) findViewById(R.id.linItemList)).removeView((findViewById(R.id.ltLogout)));
            ((LinearLayout) findViewById(R.id.linItemList)).removeView((findViewById(R.id.separator0)));
            ((LinearLayout) findViewById(R.id.linItemList)).removeView((findViewById(R.id.separator3)));
            ((TextView)(((RelativeLayout) findViewById(R.id.ltAccount)).getChildAt(1))).setText("Login");
        }
    }

    private OnTouchListener itemClick = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                v.setBackgroundColor(0xFF99BBFF);
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                v.setBackgroundColor(0xFFFFFFFF);
                String command = ((TextView) ((RelativeLayout) v).getChildAt(1)).getText().toString();
                Intent toSwitch = null;
                switch (command) {
                    case "Advanced Settings":
                        toSwitch = new Intent(currentContext, ActivitySettings.class);
                        break;
                    case "Post Something":
                        toSwitch = new Intent(currentContext, ActivityNewPost.class);
                        break;
                    case "Account":
                        toSwitch = new Intent(currentContext, ActivityAccount.class);
                        break;
                    case "Logout":
                        Settings.currentUser = null;
                        toSwitch = new Intent(currentContext, ActivityDiscovery.class);
                        break;
                    case "Login":
                        toSwitch = new Intent(currentContext, ActivityUserLogin.class);
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this, ActivityDiscovery.class));
    }
}
