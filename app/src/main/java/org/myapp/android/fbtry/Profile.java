package org.myapp.android.fbtry;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.squareup.picasso.Picasso;

import java.io.IOException;


public class Profile extends ActionBarActivity {


    SharedPreferences sp;
    ImageView imageView;
    ImageView editDp;
    Bitmap selectedBitmap;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        textView = (TextView) findViewById(R.id.textView_profile_pic);
        imageView = (ImageView) findViewById(R.id.profile_pic);
        sp = getSharedPreferences("USER", Context.MODE_PRIVATE);
        String myNumber = sp.getString("UserNumber", null);
        getImageUrl(myNumber);
        editDp = (ImageView) findViewById(R.id.editDp);
        editDp.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, 1);
            }
        });

    }

    void etImageinProfile(){
        sp = getSharedPreferences("USER", MODE_PRIVATE);
        String dpUri = sp.getString("userProfilePic", null);
        if(dpUri!=null){
            try {
                selectedBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(dpUri));
            } catch (IOException e) {
            }
            imageView.setImageBitmap(selectedBitmap);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            Intent postAdd = new Intent();
            postAdd.setClass(this, CropActivity.class);
            postAdd.putExtra("fullPhotoUri", selectedImage);
            postAdd.putExtra("ActualPath", picturePath);
            startActivityForResult(postAdd, 2);
            // String picturePath contains the path of selected Image

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement


        return super.onOptionsItemSelected(item);
    }


    public void getImageUrl(final String number) {
        if(number==null){
            return;
        }
        ParseQuery<ParseObject> query = ParseQuery.getQuery("UserList");
        Log.v("!@#check", number + " checking on parse");
        query.whereEqualTo("Number", number);
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            public void done(final ParseObject object, ParseException e) {
                if (object != null) {
                    ParseFile fileObject = object.getParseFile("Image");
                    if (fileObject!=null){
                        final String imageUrl = fileObject.getUrl();
                        Picasso.with(getApplicationContext()).load(imageUrl).fit().into(imageView);
                    }

                }
            }
        });
    }
}
