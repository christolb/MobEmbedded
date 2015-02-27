package edu.ucsb.mobemb.mars;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.regex.Pattern;


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
