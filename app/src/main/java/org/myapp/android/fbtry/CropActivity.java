package org.myapp.android.fbtry;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.soundcloud.android.crop.Crop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;


public class CropActivity extends Activity implements BitmapHelperClass.myHelper {

    private ImageView resultView;
    Uri outputUri;
    Uri inputUri;
    Uri finalInputUri;
    String actualPath;
    Bitmap selectedBitmap;
    SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);
        resultView = (ImageView) findViewById(R.id.result_image);
        Intent i = getIntent();
        inputUri = (Uri) i.getExtras().get("fullPhotoUri");
        actualPath = i.getExtras().getString("ActualPath");
        BitmapHelperClass task = new BitmapHelperClass(inputUri, this.getContentResolver(), this,actualPath);
        task.listener = this;
        task.execute();
        outputUri = Uri.fromFile(new File(getCacheDir(), "cropped"));
        sharedPreferences = getSharedPreferences("USER", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor =sharedPreferences.edit();
        editor.putString("userProfilePic",outputUri+"");
        editor.commit();
        Crop.of(finalInputUri, outputUri).asSquare().start(this);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_crop, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent result) {
        if (requestCode == Crop.REQUEST_CROP && resultCode == RESULT_OK) {

            try {
                selectedBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), outputUri);
            } catch (IOException e) {
            }
           sendPostToServer();
            finish();
        }
    }

    private void beginCrop(Uri source) {
        Uri destination = Uri.fromFile(new File(getCacheDir(), "cropped"));
        Crop.of(source, destination).asSquare().start(this);
    }

    private void handleCrop(int resultCode, Intent result) {
        if (resultCode == RESULT_OK) {
            resultView.setImageURI(Crop.getOutput(result));
        } else if (resultCode == Crop.RESULT_ERROR) {
        }
    }



    private byte[] LoadByteArrayFromFile(Bitmap bitmap) {

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        // Compress image to lower quality scale 1 - 100
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] image = stream.toByteArray();
        return image;
    }

    private void sendPostToServer() {

        final byte[] image = LoadByteArrayFromFile(selectedBitmap);
        ParseQuery<ParseObject> query = ParseQuery.getQuery("UserList");
        final String number = sharedPreferences.getString("UserNumber", null);
        query.whereEqualTo("Number",number);
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            public void done(ParseObject object, ParseException e) {
                if (object != null) {
                    ParseFile file = new ParseFile("resume.jpg", image);
                    file.saveInBackground();
                    object.put("Image",file);
                    object.saveInBackground();
                }
            }
        });
    }
    @Override
    public void passUri(Uri uri) {
        finalInputUri = uri;
        Crop.of(finalInputUri, outputUri).asSquare().start(this);
    }
}