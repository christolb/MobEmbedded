package edu.ucsb.mobemb.mars;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

import edu.ucsb.mobemb.mars.CloudCommunication.PostNewTarget;
import edu.ucsb.mobemb.mars.CloudCommunication.UpdateTarget;


public class ImagePicker extends Activity {

    private final int SELECT_PHOTO = 1;
    private final int CROP_RESULT = 2;
    private ImageView imageView;
    String name;
    Activity context;
    final String imageURIKey = "selctedImageURI";
    public EditText statusTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_picker);
        context = this;

        imageView = (ImageView) findViewById(R.id.imageView);
        statusTextView = (EditText) findViewById(R.id.statusText);

        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        String uriString = sharedPref.getString(imageURIKey, "temp");

        String targetID = sharedPref.getString(Global.targetIDKey, "null");
        Log.e("GP", "temp targetID in ImagePicker Activity= " + targetID);
        Global.targetID = targetID;

        Uri imageUri = Uri.parse(uriString);
        Global.profilePicUri = imageUri;

        final InputStream imageStream;
        try {
            imageStream = getContentResolver().openInputStream(imageUri);
            final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
            imageView.setImageBitmap(selectedImage);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.e("GP", "Image file not found!!!");
        }


        Button pickImage = (Button) findViewById(R.id.btn_pick);
        pickImage.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, SELECT_PHOTO);

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch (requestCode) {
            case SELECT_PHOTO:
                if (resultCode == RESULT_OK) {
                    Global.profilePicUri = imageReturnedIntent.getData();
                    Log.d("GP", "Image picker returned Uri = " + Global.profilePicUri);
                    // Update image to image view and global variables
                    //GP For displaying the  selected image on ImageView

                    Intent intent = new Intent("com.android.camera.action.CROP");
                    intent.setType("image/*");
                    List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, 0);
                    int size = list.size();
                    if (size == 0) {
                        Toast.makeText(this, "Can not find image crop app", Toast.LENGTH_SHORT).show();
                        Log.e("GP", "Can not find image crop app");
                        return;
                    } else {
                        intent.setData(Global.profilePicUri);
                        intent.putExtra("outputX", 300);
                        intent.putExtra("outputY", 300);
//                        intent.putExtra("aspectX", 1);
//                        intent.putExtra("aspectY", 1);
                        intent.putExtra("crop", true);
                        intent.putExtra("return-data", true);
                        startActivityForResult(intent, CROP_RESULT);

//                     //   try {
//                            final Uri imageUri = Global.profilePicUri;
//                            Log.d("GP", "Picked Image URI = " + imageUri.toString());
//                            String imageLocation = imageUri.getEncodedPath();//   FileUtils.getPath(this, uri);
//                            Log.d("GP", "Picked Image Location = " + imageLocation + "Path =" + imageUri.getPath() + "");
//                            //final InputStream imageStream = getContentResolver().openInputStream(imageUri);
//                            //final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
//                            // imageView.setImageBitmap(selectedImage);
//
//                     //   } catch (FileNotFoundException e) {
//                     //       e.printStackTrace();
//                     //   }

                    }
                }
                break;
            case CROP_RESULT: {
                if (resultCode == RESULT_OK) {
                    Log.e("GP", "CROP_RESULT");
                    Bundle extras = imageReturnedIntent.getExtras();
                    if (extras != null) {
                        Bitmap bmp = extras.getParcelable("data");
                        imageView.setImageBitmap(bmp);

                        //Saving image file
                        String filename = "marsPic.png";
                        File sd = Environment.getExternalStorageDirectory();
                        File dest = new File(sd, filename);

                        //Bitmap bitmap = (Bitmap)data.getExtras().get("data");
                        try {
                            Log.d("GP","Cropped bitmap size = "+bmp.getWidth()+ " x "+bmp.getHeight());
                            FileOutputStream out = new FileOutputStream(dest);
                            bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
                            out.flush();
                            out.close();
                        } catch (Exception e) {
                            Log.e("GP", "Exception in storing profile pic as png");
                            e.printStackTrace();
                        }

                        try {
                           String path =  MediaStore.Images.Media.insertImage(getContentResolver(), dest.getAbsolutePath(), dest.getName(), dest.getName());
                            Global.profilePicUri = Uri.parse(path);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                       // Global.profilePicUri = Uri.fromFile(dest);
                        Log.e("GP", "URI from cropped pic = " + Global.profilePicUri);

                        break;
                    }
                }
            }
        }
    }

    public void updateTargetImageID(String targetID, Uri imageUri) {
        //GP saving the selected image id for furture use
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(imageURIKey, imageUri.toString());
        editor.putString(Global.targetIDKey, targetID);
        editor.commit();
        Log.d("GP", "Saving to sharedPref targetID" + Global.targetID + " imageUri=" + imageUri);
    }

    // By using this method get the Uri of Internal/External Storage for Media
    private Uri getUri() {
        String state = Environment.getExternalStorageState();
        if (!state.equalsIgnoreCase(Environment.MEDIA_MOUNTED))
            return MediaStore.Images.Media.INTERNAL_CONTENT_URI;

        return MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    }

    public void updateStatus(View v) {
        {
            //GP Obtaining absolute path of picked image
            //GP TODO might not work on version lower than kitkat
            try {

                Uri originalUri = Global.profilePicUri;
                Log.e("GP","UpdateStatus originalUri = "+originalUri);
                String pathsegment[] = originalUri.getLastPathSegment().split(":");
                String id = pathsegment[0];
                final String[] imageColumns = {MediaStore.Images.Media.DATA};
                final String imageOrderBy = null;

                Uri uri = getUri();
                Cursor imageCursor = this.getContentResolver().query(uri, imageColumns,
                        MediaStore.Images.Media._ID + "=" + id, null, null);

                if (imageCursor.moveToFirst()) {
                    name = imageCursor.getString(imageCursor.getColumnIndex(MediaStore.Images.Media.DATA));
                    Log.d("GP", "Obtained name = " + name);
                } else {
                    Log.e("GP", "Failed to get image name");
                }

            } catch (Exception e) {
                e.printStackTrace();
                Log.e("GP", "Failed to get image due to exception");
            }


            Log.d("GP", "In ImagePicker targetID = " + Global.targetID + " Length=" + Global.targetID.length());
            if (Global.targetID.length() > 5)  //initially it is null
            {
                Log.d("GP", "Target ID available. Update the Target with UserID " + Global.userID + " name=" + name);
                //GP Update the image on Vuforia cloud
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            UpdateTarget u = new UpdateTarget(Global.userID, name);
                            u.updateTarget(statusTextView.getText().toString());
                        } catch (Exception e) {
                            Log.e("GP", "Exception while Updating VWS " + e.getMessage());
                        }
                    }
                }).start();
            } else {
                Log.d("GP", "Target ID not available. Posting new Target to Vuforia");
                //GP Post the image to Vuforia cloud
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        PostNewTarget p = new PostNewTarget(Global.userID, name);
                        Global.targetID = p.postTargetThenPollStatus(statusTextView.getText().toString());
                        Log.e("GP", "Received targetID=" + Global.targetID);
                        updateTargetImageID(Global.targetID, Global.profilePicUri);
                    }
                }).start();
            }
        }
        //GP - show Alert that Image will be ready after 10 mins
        new AlertDialog.Builder(this)
                .setTitle("Profile Update")
                .setMessage("It will take 5~10 mins for your updated proile to be ready for AR")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }
}