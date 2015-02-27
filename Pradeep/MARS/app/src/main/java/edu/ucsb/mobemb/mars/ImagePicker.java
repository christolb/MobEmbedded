package edu.ucsb.mobemb.mars;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.ImageView;


import java.io.FileNotFoundException;
import java.io.InputStream;

import edu.ucsb.mobemb.mars.CloudCommunication.PostNewTarget;


public class ImagePicker  extends Activity {

    private final int SELECT_PHOTO = 1;
    private ImageView imageView;
    String name;
    final String imageURIKey = "selctedImageURI";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_picker);

        imageView = (ImageView)findViewById(R.id.imageView);

        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        String uriString = sharedPref.getString(imageURIKey, "temp");

        Uri imageUri = Uri.parse(uriString);

        final InputStream imageStream;
        try {
            imageStream = getContentResolver().openInputStream(imageUri);
            final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
            imageView.setImageBitmap(selectedImage);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.e("GP","Image file not found!!!");
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

        switch(requestCode) {
            case SELECT_PHOTO:
                if(resultCode == RESULT_OK){
                    try {
                        final Uri imageUri = imageReturnedIntent.getData();
                        Log.d("GP","Picked Image URI = "+imageUri.toString());
                        String imageLocation = imageUri.getEncodedPath();//   FileUtils.getPath(this, uri);
                        Log.d("GP","Picked Image Location = "+imageLocation+"Path ="+imageUri.getPath()+"");
                        final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                        final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                        imageView.setImageBitmap(selectedImage);

                        //GP Obtaining absolute path of picked image
                        //GP TODO might not work on version lower than kitkat

                        try {
                            Uri originalUri = imageReturnedIntent.getData();
                            String pathsegment[] = originalUri.getLastPathSegment().split(":");
                            String id = pathsegment[0];
                            final String[] imageColumns = { MediaStore.Images.Media.DATA };
                            final String imageOrderBy = null;

                            Uri uri = getUri();
                            Cursor imageCursor = this.getContentResolver().query(uri, imageColumns,
                                    MediaStore.Images.Media._ID + "=" + id, null, null);

                            if (imageCursor.moveToFirst()) {
                                name = imageCursor.getString(imageCursor.getColumnIndex(MediaStore.Images.Media.DATA));
                                Log.d("GP", "Obtained name = " + name);
                            }
                            else
                            {
                                Log.e("GP","Failed to get image name");
                            }

                        } catch (Exception e) {
                            Log.e("GP", "Failed to get image due to exception");
                        }



                        //GP saving the selected image id for furture use
                        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString(imageURIKey, imageUri.toString());
                        editor.commit();

                        //GP Post the image to Vuforia cloud
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                PostNewTarget p = new PostNewTarget(Global.userID,name);
                                p.postTargetThenPollStatus();
                            }
                        }).start();


                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                }
        }
    }
    // By using this method get the Uri of Internal/External Storage for Media
    private Uri getUri() {
        String state = Environment.getExternalStorageState();
        if(!state.equalsIgnoreCase(Environment.MEDIA_MOUNTED))
            return MediaStore.Images.Media.INTERNAL_CONTENT_URI;

        return MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }
}