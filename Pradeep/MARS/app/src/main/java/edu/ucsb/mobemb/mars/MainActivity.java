package edu.ucsb.mobemb.mars;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

//First Activity - shows a splash screen for 1 sec
public class MainActivity extends Activity {
    private static long SPLASH_MILLIS = 5000;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        LayoutInflater inflater = LayoutInflater.from(this);
        LinearLayout layout = (LinearLayout) inflater.inflate(
                R.layout.splash_screen, null, false);

        addContentView(layout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        WebView wv = (WebView) findViewById(R.id.webView);
        wv.loadUrl("file:///android_asset/marssplash.gif");

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable()
        {

            @Override
            public void run()
            {

                Intent intent = new Intent(MainActivity.this,
                        ActivityLauncher.class);
                startActivity(intent);

            }

        }, SPLASH_MILLIS);
    }
}
