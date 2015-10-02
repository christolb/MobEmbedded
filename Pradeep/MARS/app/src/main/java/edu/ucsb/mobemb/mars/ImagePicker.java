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
import android.text.Editable;
import android.text.TextWatcher;
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
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import edu.ucsb.mobemb.mars.CloudCommunication.PostNewTarget;
import edu.ucsb.mobemb.mars.CloudCommunication.UpdateTarget;


public class ImagePicker extends Activity {

    private final int SELECT_PHOTO = 1;
    private final int CROP_RESULT = 2;
    private final int REQUEST_IMAGE_CAPTURE = 3;
    private ImageView imageView;
    String name;
    Activity context;
    final String imageURIKey = "selctedImageURI";
    public EditText statusTextView;
    Button btnUpdate;
    String mCurrentPhotoPath;

    private Uri mMakePhotoUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_picker);
        context = this;

        imageView = (ImageView) findViewById(R.id.imageView);
        statusTextView = (EditText) findViewById(R.id.statusText);

        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);


        sharedPref.edit().remove(imageURIKey).commit();
        sharedPref.edit().remove(Global.targetIDKey).commit();

        String uriString = sharedPref.getString(imageURIKey, "temp");

        Uri imageUri = Uri.parse(uriString);

        if (uriString.equalsIgnoreCase("temp")) {
            imageView.setImageResource(R.mipmap.mars_launcher);
        } else {
            Log.e("GP", "onCreate imgUri loaded from SharedPref = " + uriString);

            //GP render thumbnail of image instead of full on Imageview
//            imageStream = getContentResolver().openInputStream(imageUri);
//            final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
//            imageView.setImageBitmap(selectedImage);

            Bitmap bitmap = MediaStore.Images.Thumbnails.getThumbnail(
                    getContentResolver(), Integer.parseInt(imageUri.getLastPathSegment()),
                    MediaStore.Images.Thumbnails.MINI_KIND,
                    (BitmapFactory.Options) null);

            //GP Using thumbnail to display in ImageView
            imageView.setImageBitmap(bitmap);
        }

        Global.profilePicUri = imageUri;

        String targetID = sharedPref.getString(Global.targetIDKey, "null");
        Log.e("GP", "temp targetID in ImagePicker Activity= " + targetID);
        Global.targetID = targetID;

        String statusMsg = sharedPref.getString(Global.statusMessageKey, "Hello MARS");
        Log.e("GP", "statusMessage in ImagePicker Activity= " + statusMsg);
        Global.statusMessage = statusMsg;

        final InputStream imageStream;

        statusTextView.setText(statusMsg);


        Button pickImage = (Button) findViewById(R.id.btn_pick);
        pickImage.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, SELECT_PHOTO);

            }
        });

        btnUpdate = (Button) findViewById(R.id.btn_update);

        statusTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d("GP", "Status Updation started. Enabling Button");
                if (!Global.profilePicUri.toString().equalsIgnoreCase("temp"))
                    btnUpdate.setEnabled(true);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


    }

    public void onCamButtonClicked(View v) {
        /*Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        //takePictureIntent.putExtra("return-data", true);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }  */

        //GP doing as per developer site
        dispatchTakePictureIntent();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch (requestCode) {
            case SELECT_PHOTO:
                if (resultCode == RESULT_OK) {
                    Global.profilePicUri = imageReturnedIntent.getData();
                    Log.d("GP", "From Gallery: Image picker returned Uri = " + Global.profilePicUri);
                    // Update image to image view and global variables
                    //GP For displaying the  selected image on ImageView
//                    InputStream imageStream = null;
//                    try {
//                        imageStream = getContentResolver().openInputStream(Global.profilePicUri);
//                    } catch (FileNotFoundException e) {
//                        e.printStackTrace();
//                    }
                    //    Bitmap bmp = BitmapFactory.decodeStream(imageStream);
                    //   imageView.setImageBitmap(bmp);

                    //GP
                    if (!Global.profilePicUri.toString().equalsIgnoreCase("temp"))
                        btnUpdate.setEnabled(true);

                    Bitmap bitmap = MediaStore.Images.Thumbnails.getThumbnail(
                            getContentResolver(), Integer.parseInt(imageReturnedIntent.getData().getLastPathSegment()),
                            MediaStore.Images.Thumbnails.MINI_KIND,
                            (BitmapFactory.Options) null);

                    //GP Using thumbnail to display in ImageView
                    imageView.setImageBitmap(bitmap);

                    //GP - code for calling bitmap
//                    Intent intent = new Intent("com.android.camera.action.CROP");
//                    intent.setType("image/*");
//                    List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, 0);
//                    int size = list.size();
//                    if (size == 0) {
//                        Toast.makeText(this, "Can not find image crop app", Toast.LENGTH_SHORT).show();
//                        Log.e("GP", "Can not find image crop app");
//                        return;
//                    } else {
//                        intent.setData(Global.profilePicUri);
//                        intent.putExtra("outputX", 4000);
//                        intent.putExtra("outputY", 4000);
////                        intent.putExtra("aspectX", 1);
////                        intent.putExtra("aspectY", 1);
//                        intent.putExtra("crop", true);
//                        intent.putExtra("return-data", true);
//                        startActivityForResult(intent, CROP_RESULT);
//                    }
                }
                break;
            case REQUEST_IMAGE_CAPTURE:
                if (resultCode == RESULT_OK) {
                    galleryAddPic();
                    //  Global.profilePicUri = imageReturnedIntent.getData();
                    Log.d("GP", "From Camera: Image picker returned Uri = " + Global.profilePicUri);
                    // Update image to image view and global variables
                    //GP For displaying the  selected image on ImageView
//                    InputStream imageStream = null;
//                    try {
//                        imageStream = getContentResolver().openInputStream(Global.profilePicUri);
//                    } catch (FileNotFoundException e) {
//                        e.printStackTrace();
//                    }
                    //    Bitmap bmp = BitmapFactory.decodeStream(imageStream);
                    //   imageView.setImageBitmap(bmp);


                    if (!Global.profilePicUri.toString().equalsIgnoreCase("temp"))
                        btnUpdate.setEnabled(true);
                    //GP - working but not for chris
//
//                    Bundle extras = imageReturnedIntent.getExtras();
//                    Bitmap imageBitmap = (Bitmap) extras.get("data");
//                    if (imageBitmap != null) {
//                        Log.e("GP", "Bitmap from camera 1 : thumbnail : size :  " + imageBitmap.getHeight() + " " + imageBitmap.getWidth());
//                        imageView.setImageBitmap(imageBitmap);
//                    } else {
//                        Log.e("GP", "Bitmap returned by camera is null . So taking URI and finding thumbnail ");
//                        Bitmap bitmap = MediaStore.Images.Thumbnails.getThumbnail(
//                                getContentResolver(), Integer.parseInt(Global.profilePicUri.getLastPathSegment()),
//                                MediaStore.Images.Thumbnails.MINI_KIND,
//                                (BitmapFactory.Options) null);
//                        Log.e("GP", "Bitmap from camera 2 : thumbnail : size :  " + imageBitmap.getHeight() + " " + imageBitmap.getWidth());
//                        //GP Using thumbnail to display in ImageView
//                        imageView.setImageBitmap(bitmap);
//                    }
                    //GP not working for chris end

                    setPic();  //GP as per developer site

                    //GP - code for calling bitmap
//                    Intent intent = new Intent("com.android.camera.action.CROP");
//                    intent.setType("image/*");
//                    List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, 0);
//                    int size = list.size();
//                    if (size == 0) {
//                        Toast.makeText(this, "Can not find image crop app", Toast.LENGTH_SHORT).show();
//                        Log.e("GP", "Can not find image crop app");
//                        return;
//                    } else {
//                        intent.setData(Global.profilePicUri);
//                        intent.putExtra("outputX", 4000);
//                        intent.putExtra("outputY", 4000);
////                        intent.putExtra("aspectX", 1);
////                        intent.putExtra("aspectY", 1);
//                        intent.putExtra("crop", true);
//                        intent.putExtra("return-data", true);
//                        startActivityForResult(intent, CROP_RESULT);
//                    }
                }
                break;

//            case CROP_RESULT: {
//                //GP - Not gonna do CROP!!!
//                if (resultCode == RESULT_OK) {
//                    Log.e("GP", "CROP_RESULT");
//                    btnUpdate.setEnabled(true);
//
//                    Bundle extras = imageReturnedIntent.getExtras();
//                    if (extras != null) {
//                        Bitmap bmp = extras.getParcelable("data");
//
//
//                        imageView.setImageBitmap(bmp);
//
//                        //Saving image file
//                        String filename = "marsPic.png";
//                        File sd = Environment.getExternalStorageDirectory();
//                        File dest = new File(sd, filename);
//
//                        //Bitmap bitmap = (Bitmap)data.getExtras().get("data");
//                        try {
//                            Log.d("GP","Cropped bitmap size = "+bmp.getWidth()+ " x "+bmp.getHeight());
//                            FileOutputStream out = new FileOutputStream(dest);
//                            bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
//                            out.flush();
//                            out.close();
//                        } catch (Exception e) {
//                            Log.e("GP", "Exception in storing profile pic as png");
//                            e.printStackTrace();
//                        }
//
//                        try {
//                           String path =  MediaStore.Images.Media.insertImage(getContentResolver(), dest.getAbsolutePath(), dest.getName(), dest.getName());
//                            Global.profilePicUri = Uri.parse(path);
//                        } catch (FileNotFoundException e) {
//                            e.printStackTrace();
//                        }
//                       // Global.profilePicUri = Uri.fromFile(dest);
//                        Log.e("GP", "URI from cropped pic = " + Global.profilePicUri);
//
//                        break;
//                    }
//                }
//            }
        }
    }

    public void updateTargetImageID(String targetID, Uri imageUri) {
        //GP saving the selected image id for furture use
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(imageURIKey, imageUri.toString());  //TODO Image is not getting
        editor.putString(Global.targetIDKey, targetID);
        editor.putString(Global.statusMessageKey, statusTextView.getText().toString());
        editor.commit();
        Log.d("GP", "Saving to sharedPref targetID" + Global.targetID + " imageUri=" + imageUri + " Status Msg=" + statusTextView.getText().toString());
    }

    // By using this method get the Uri of Internal/External Storage for Media
    private Uri getUri() {
        String state = Environment.getExternalStorageState();
        if (!state.equalsIgnoreCase(Environment.MEDIA_MOUNTED))
            return MediaStore.Images.Media.INTERNAL_CONTENT_URI;

        return MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    }


    //on click of Update Button
    public void updateStatus(View v) {
        {
            //GP Obtaining absolute path of picked image
            //GP TODO might not work on version lower than kitkat
            try {

                Uri originalUri = Global.profilePicUri;
                Log.e("GP", "UpdateStatus originalUri = " + originalUri);
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
                            updateTargetImageID(Global.targetID, Global.profilePicUri);  //GP added for updation image not changed issue
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





    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                ex.printStackTrace();
            }

            // Continue only if the File was successfully created
            if (photoFile != null) {
                Log.e("GP","Camera - dispatchTakePictureIntent - saving file = "+photoFile.getAbsolutePath());
//                mMakePhotoUri = Uri.fromFile(photoFile);
//                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
//                        mMakePhotoUri);

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private void setPic() {
        // Get the dimensions of the View
        int targetW = 300; //imageView.getWidth();
        int targetH = 300; //imageView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        imageView.setImageBitmap(bitmap);
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        //GP
//        Uri contentUri=  mMakePhotoUri;
//        Log.e("GP","Photo taken - galleryAddPic :contentUri = mMakePhotoUri ="+mMakePhotoUri);

        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mCurrentPhotoPath != null) {
            //outState.putString("curPhotoPath", mImageUri.toString());
            outState.putString("curPhotoPath", mCurrentPhotoPath);
            Log.e("GP","Camera mCurrentPhotoPath on save = "+mCurrentPhotoPath);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.containsKey("curPhotoPath")) {
            //mImageUri = Uri.parse(savedInstanceState.getString("cameraImageUri"));
            mCurrentPhotoPath = savedInstanceState.getString("curPhotoPath");
            Log.e("GP","Camera mCurrentPhotoPath on restore = "+mCurrentPhotoPath);
        }
    }
}