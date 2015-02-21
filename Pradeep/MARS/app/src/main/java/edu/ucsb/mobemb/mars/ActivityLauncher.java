package edu.ucsb.mobemb.mars;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;


public class ActivityLauncher extends Activity
{

    private String TAG = "MARS";

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
//                R.layout.activities_list_text_view, mActivities);
//
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activities_list);
//        setListAdapter(adapter);
    }

    public void launchARScreen(View v) {
        Log.d(TAG,"Launch AR Screen");
        Intent intent1 = new Intent(this, CloudAR.class);
        startActivity(intent1);
    }

    public void launchImgPicker(View v) {
        Log.d(TAG,"Launch Image Picker");
        Intent intent2 = new Intent(this, ImagePicker.class);
        startActivity(intent2);
    }

}
