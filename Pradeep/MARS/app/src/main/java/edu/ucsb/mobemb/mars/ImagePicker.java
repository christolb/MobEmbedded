package edu.ucsb.mobemb.mars;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_picker);

        imageView = (ImageView)findViewById(R.id.imageView);

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

                        //GP - getting absolute file path
                        /*
                        String wholeID = DocumentsContract.getDocumentId(imageUri);
                        String id = wholeID.split(":")[1];
                        String[] column = { MediaStore.Images.Media.DATA };
                        String sel = MediaStore.Images.Media._ID + "=?";
                        Cursor cursor = getContentResolver().
                                query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                        column, sel, new String[]{ id }, null);

                        String filePath = "";

                        int columnIndex = cursor.getColumnIndex(column[0]);

                        if (cursor.moveToFirst()) {
                            filePath = cursor.getString(columnIndex);
                        }

                        cursor.close();
                           Log.d("GP","New Img Location = "+filePath); */

                        //GP - to prevent network on main thread exception
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                PostNewTarget p = new PostNewTarget("AppTestImg","temp");
                                p.postTargetThenPollStatus();
                            }
                        }).start();


                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }
}