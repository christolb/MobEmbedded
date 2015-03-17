package edu.ucsb.mobemb.mars;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;

import java.util.regex.Pattern;

//List of activities to launch from buttons
public class ActivityLauncher extends Activity
{

    private String TAG = "MARS";

    @Override
    public void onCreate(Bundle savedInstanceState)
        {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activities_list);

        //GP To get gmail ID of user and use it as the primary key for AWS table
        Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
        Account[] accounts = AccountManager.get(this).getAccounts();
        for (Account account : accounts) {
            if (emailPattern.matcher(account.name).matches()) {
                Global.userID = account.name;
                break;
            }
        }
        Log.d(TAG,"UserID = "+ Global.userID);

        //Initializing Vuforia Target ID with stored value if it exists
            //DOne in ImagePicker !!!
//        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
//        Global.targetID = sharedPref.getString(Global.targetIDKey, "null");
//        Log.d("GP","Available SharedPref TargetID = "+Global.targetID);

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
