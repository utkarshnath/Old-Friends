package org.myapp.android.fbtry;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnCancelListener;
import com.orhanobut.dialogplus.OnClickListener;
import com.orhanobut.dialogplus.OnDismissListener;
import com.orhanobut.dialogplus.ViewHolder;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class SwipeActivity extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, ResultCallback<Status>,
        ActionBar.TabListener, LoaderManager.LoaderCallbacks<Cursor>{

    private static final String LOG_TAG = SwipeActivity.class.getSimpleName();

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */

    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */

    ViewPager mViewPager;
    myHelper Helper;
    SQLiteDatabase db;
    SharedPreferences sp, sharedPreferences,radiusSharedPerferences;
    GoogleApiClient googleApiClient;
    LocationRequest locationRequest;
    float radius;
    String invitaion;
    SharedPreferences.Editor editor;
    Geocoder geocoder;
    protected ArrayList<Geofence> mGeofenceList;
    private PendingIntent mGeofencePendingIntent;
    protected static final String TAG = "MainActivity";
    public static final HashMap<String, LatLng> geofenceMap = new HashMap<String, LatLng>();
    private static final int LOADER_ID = 100;
    private static final int DISTANCE_LOADER_ID = 101;
    LoaderManager.LoaderCallbacks<Cursor> callback;
    boolean isGPSEnabled = false;

    private static final String[] projection = new String[]{
            myHelper.FriendName,
            myHelper.FriendLatitude,
            myHelper.FriendLongitude,
            myHelper.FriendNumber,
            myHelper.FriendLocation,
            myHelper.FriendDistance,
            myHelper.FriendDpPath,
            myHelper.LastupdatedAt
    };

    private static final int COL_FRIEND_NAME = 0;
    private static final int COL_FRIEND_LATITUDE = 1;
    private static final int COL_FRIEND_LONGITUDE = 2;
    private static final int COL_FRIEND_NUMBER = 3;
    private static final int COL_FRIEND_LOCATION = 4;
    private static final int COL_FRIEND_DISTANCE = 5;
    private static final int COL_FRIEND_DP_PATH = 6;
    private static final int COL_FRIEND_LAST_UPDATED = 7;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_swipe);

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        callback = this;
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        radiusSharedPerferences = getSharedPreferences("UserRadius", Context.MODE_PRIVATE);
        radius = radiusSharedPerferences.getFloat("Radius", 1000 );

        Helper = new myHelper(this);
        db = Helper.getWritableDatabase();

        mGeofenceList = new ArrayList<Geofence>();
        mGeofencePendingIntent = null;
        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
        sharedPreferences = getSharedPreferences("USER", Context.MODE_PRIVATE);

//        mGeofencesAdded = sp.getBoolean(Constants.GEOFENCES_ADDED_KEY, false);
//        setButtonsEnabledState();

//        Toast.makeText(getApplicationContext(), "fr", Toast.LENGTH_LONG).show();
        Log.v("oncreate", "before createlist");
        editor = sharedPreferences.edit();
        Task task = new Task(this);
        task.execute();
        //createFriendsList();
        geocoder = new Geocoder(this);
        checkGPS();
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        invitaion = new String();
        getInvitaion();
    }


    void getInvitaion(){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("SendInvitation");
        query.getInBackground("fRnduOzAlc", new GetCallback<ParseObject>() {
            public void done(ParseObject object, ParseException e) {
                if (e == null) {
                    invitaion =  object.getString("InvitationText");
                    Log.d("!@#invit",invitaion);
                } else {
                    // something went wrong
                }
            }
        });
    }

    public void addGeofences() {
        if (!googleApiClient.isConnected()) {
            //getLoaderManager().restartLoader(LOADER_ID , null , this);
            return;
        }
        try {
            Log.d("SwipeActivity","Google Client Connected");
            LocationServices.GeofencingApi.addGeofences(
                    googleApiClient,
                    // The GeofenceRequest object.
                    getGeofencingRequest(),
                    // A pending intent that that is reused when calling removeGeofences(). This
                    // pending intent is used to generate an intent when a matched geofence
                    // transition is observed.
                    getGeofencePendingIntent()
            ).setResultCallback(this); // Result processed in onResult().
        } catch (SecurityException securityException) {
            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
            Log.v("!@#exception", String.valueOf(securityException));
        }
    }

    public void removeGeofences() {
        if (!googleApiClient.isConnected()) {
            return;
        }
        try {
            // Remove geofences.
            LocationServices.GeofencingApi.removeGeofences(
                    googleApiClient,
                    // This is the same pending intent that was used in addGeofences().
                    getGeofencePendingIntent()
            ).setResultCallback(this); // Result processed in onResult().
        } catch (SecurityException securityException) {
            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
            logSecurityException(securityException);
        }
    }


    private void logSecurityException(SecurityException securityException) {
        Log.e(TAG, "Invalid location permission. " +
                "You need to use ACCESS_FINE_LOCATION with geofences", securityException);
    }

//    @Override
//    public void onBackPressed() {
//        Toast.makeText(this,"no back",Toast.LENGTH_SHORT).show();
////        finish();
//    }

    private void checkGPS() {
        final LocationManager locationManager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
        isGPSEnabled = locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER);
        if(!isGPSEnabled)  {
            showGpsAlert();
        }
    }



    public void showGpsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        // Setting DialogHelp Title
        alertDialog.setTitle("Location Service Disabled");
        // Setting DialogHelp Message
        alertDialog
                .setMessage("Please enable location Services");

        alertDialog.setPositiveButton("Enable",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(
                                Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                    }
                });
        alertDialog.show();
    }


    String int_to_month(int i){
        String month = null;
        switch (i){
            case 0:
                month = "Jan";
                break;
            case 1:
                month = "Feb";
                break;
            case 2:
                month = "Mar";
                break;
            case 3:
                month = "April";
                break;
            case 4:
                month = "May";
                break;
            case 5:
                month = "Jun";
                break;
            case 6:
                month = "July";
                break;
            case 7:
                month = "Aug";
                break;
            case 8:
                month = "Sept";
                break;
            case 9:
                month = "Oct";
                break;
            case 10:
                month = "Nov";
                break;
            case 11:
                month = "Dec";
                break;
        }
        return month;
    }

    private void downloadImage(byte[] bytes, String id,ContentValues contentValues) {


        try {
            //First we create a new Folder named Speed Share if it does not exist
            File folder = new File(Environment.getExternalStorageDirectory() + "/Old Friends");
            boolean success = true;
            if (!folder.exists()) {
                success = folder.mkdir();
            }

            //Now prepare to receive data
            if (success) {
                //Toast.makeText(context,post.getFeedId()+"",Toast.LENGTH_SHORT).show();
                //Initialize output Stream and Output file's full path
                FileOutputStream fileOutputStream = new FileOutputStream(Environment.getExternalStorageDirectory() + "/Old Friends" + "/" + id +".jpg");
                BufferedOutputStream bufOutputStream = new BufferedOutputStream(fileOutputStream);

                //Byte Buffer to read from the input stream
                byte[] contents = new byte[1024];

                //No. of bytes in one read() call
                int bytesRead = 0;
                InputStream myInputStream = new ByteArrayInputStream(bytes);

                //Start reading from inputStream and write on the output Stream
                while ((bytesRead = myInputStream.read(contents)) != -1) {
                    bufOutputStream.write(contents, 0, bytesRead);
                }

                //Hopefully we are done receiving so lets do the housekeeping stuff
                bufOutputStream.flush();
                myInputStream.close();


            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        contentValues.put(myHelper.FriendDpPath, Environment.getExternalStorageDirectory() + "/Old Friends" + "/" + id + ".jpg");
        getContentResolver().insert(DatabaseContract.CONTENT_URI, contentValues);
    }


    private byte[] LoadByteArrayFromFile(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        // Compress image to lower quality scale 1 - 100
        bitmap.compress(Bitmap.CompressFormat.PNG, 1, stream);
        byte[] image = stream.toByteArray();
        return image;
    }


    @Override
    protected void onStart() {
        super.onStart();
        checkGPS();
        googleApiClient.connect();
    }


    @Override
    protected void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        googleApiClient.disconnect();
    }


    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();

        // The INITIAL_TRIGGER_ENTER flag indicates that geofencing service should trigger a
        // GEOFENCE_TRANSITION_ENTER notification when the geofence is added and if the device
        // is already inside that geofence.
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);

        // Add the geofences to be monitored by geofencing service.
        builder.addGeofences(mGeofenceList);

        // Return a GeofencingRequest.
        return builder.build();
    }


    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }


    public void populateGeofenceList() {
        //addFencesFromDatabase();
        Log.d("!@#radius", radius + "");
        for (Map.Entry<String, LatLng> entry : geofenceMap.entrySet()) {

            mGeofenceList.add(new Geofence.Builder()
                        // Set the request ID of the geofence. This is a string to identify this
                       // geofence.
                    .setRequestId(entry.getKey())

                            // Set the circular region of this geofence.
                    .setCircularRegion(
                            entry.getValue().latitude,
                            entry.getValue().longitude,
                            radius
                    )

                            // Set the expiration duration of the geofence. This geofence gets automatically
                            // removed after this period of time.
                    .setExpirationDuration(Constants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)

                            // Set the transition types of interest. Alerts are only generated for these
                            // transition. We track entry and exit transitions in this sample.
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                            Geofence.GEOFENCE_TRANSITION_EXIT)

                            // Create the geofence.
                    .build());
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_swipe, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        if (id == R.id.uploadDp) {
            Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(i, 1);
            return true;
        }
        if(id == R.id.Profile){
            Intent i = new Intent();
            i.setClass(this,Profile.class);
            startActivity(i);
        }
        if(id == R.id.sendInvitation){
            if(!invitaion.isEmpty()){
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, invitaion);
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
            }else {
                Toast.makeText(this,"Check Internet Connections",Toast.LENGTH_SHORT).show();
            }
        }
        if(id == R.id.aboutUs){
            Intent i = new Intent();
            i.setClass(this,AboutUs.class);
            startActivity(i);
        }

        return super.onOptionsItemSelected(item);
    }

    public void setRadiusChangeDialogBox(){

        DialogPlus dialog = DialogPlus.newDialog(this)
                .setContentHolder(new ViewHolder(R.layout.setradius_layout))
                .setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(DialogPlus dialog, View view) {
                        if (view.getId() == R.id.donebutton) {
                            EditText editText = (EditText) dialog.findViewById(R.id.edittext_radius);
                            int x = Integer.valueOf(editText.getText().toString());
                            if(x==0){
                                Toast.makeText(getApplicationContext(), " 0 radius not allowed ", Toast.LENGTH_SHORT).show();
                            }
                            if(x>20){
                                Toast.makeText(getApplicationContext(), " Enter radius less than 20 ", Toast.LENGTH_SHORT).show();
                            }else{
                                radius = Integer.parseInt(editText.getText().toString()) * 1000;
                                SharedPreferences.Editor editor = radiusSharedPerferences.edit();
                                editor.putFloat("Radius",radius);
                                editor.commit();
                                dialog.dismiss();
                            }
                        }
                    }
                })
                .setGravity(Gravity.CENTER)
                .setOnDismissListener(new OnDismissListener() {
                    @Override
                    public void onDismiss(DialogPlus dialog) {
                        getLoaderManager().restartLoader(LOADER_ID, null, callback);
                        deleteExtraEntries(radius);
                    }
                })
                .setOnCancelListener(new OnCancelListener() {
                    @Override
                    public void onCancel(DialogPlus dialog) {
                    }
                })
                .setCancelable(true)
                .setExpanded(true)  // This will enable the expand feature, (similar to android L share dialog)
                .create();
        TextView textView = (TextView) dialog.findViewById(R.id.radius_value);
        int radiusInKm = (int) (radius/1000);
        textView.setText(radiusInKm + " Km");
        dialog.show();
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

        if(requestCode == 2){
            SharedPreferences setting = getSharedPreferences("checkFirst",Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = setting.edit();
            editor.putBoolean("firstTime",false);
            editor.commit();
        }
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onConnected(Bundle bundle) {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        locationRequest.setInterval(30000);
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }


    @Override
    public void onLocationChanged(final Location location) {

        try {

            final List<Address> list = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (list != null & list.size() > 0) {
                Address address = list.get(0);
                String result = address.getThoroughfare();
//                Log.v("!@#parselocation", result);
                if (result == null) {
                    result = "not able to get address";
                }
                ParseQuery<ParseObject> query = ParseQuery.getQuery("UserList");
                final String number = sharedPreferences.getString("UserNumber", null);
                query.whereEqualTo("Number", number);
                Log.v("!@#number", sharedPreferences.getString("UserNumber", null));
                final String finalResult = result;
                query.getFirstInBackground(new GetCallback<ParseObject>() {
                    public void done(ParseObject object, ParseException e) {
                        if (object != null) {
                            Log.v("!@#parseiilocation", "parseiilocation");
                            object.put("CurrentLocation", finalResult);
                            editor.putString("UserNumber", number);
                            object.put("Latitude", Math.round(location.getLatitude() * 1000000.0) / 1000000.0);
                            editor.putFloat("UserLatitude", (float) location.getLatitude());
                            object.put("Longitude", Math.round(location.getLongitude() * 1000000.0) / 1000000.0);
                            editor.putFloat("UserLongitude", (float) location.getLongitude());
                            editor.commit();
                            object.saveInBackground();

                            // ..........................................................

                            Cursor data = getContentResolver().query(DatabaseContract.CONTENT_URI,projection,null,null,null);
                            ContentValues contentValues = new ContentValues();
                            while (data.moveToNext()) {
                                String friendNumber = data.getString(COL_FRIEND_NUMBER);
                                String friendName = data.getString(COL_FRIEND_NAME);
                                String friendlocation = data.getString(COL_FRIEND_LOCATION);
                                float friendlatitude = data.getFloat(COL_FRIEND_LATITUDE);
                                float friendlongitude = data.getFloat(COL_FRIEND_LONGITUDE);
                                String frienddppath = data.getString(COL_FRIEND_DP_PATH);
                                String lastSeen = data.getString(COL_FRIEND_LAST_UPDATED);

                                Location friendLocation = new Location("locationA");
                                friendLocation.setLatitude(friendlatitude);
                                friendLocation.setLongitude(friendlongitude);

                                Location mylocation = new Location("locationB");
                                mylocation.setLatitude(sharedPreferences.getFloat("UserLatitude", 0));
                                mylocation.setLongitude(sharedPreferences.getFloat("UserLongitude", 0));

                                float distance = mylocation.distanceTo(friendLocation);
                                Log.d("!@#fdistance", "Name : " + friendName + " Distance : " + distance);
                                contentValues.put(Helper.FriendName, friendName);
                                contentValues.put(Helper.FriendNumber, friendNumber);
                                contentValues.put(Helper.FriendLocation, friendlocation);
                                contentValues.put(Helper.FriendLatitude, friendlatitude);
                                contentValues.put(Helper.FriendLongitude, friendlongitude);
                                if(friendlatitude==0 && friendlongitude==0){
                                    contentValues.put(Helper.FriendDistance, -1);
                                }else contentValues.put(Helper.FriendDistance, distance);
                                contentValues.put(Helper.FriendDpPath,frienddppath);
                                contentValues.put(Helper.LastupdatedAt,lastSeen);
                                getContentResolver().insert(DatabaseContract.CONTENT_URI,contentValues);
                            }
                        }

                    }
                });
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        deleteExtraEntries(radius);

    }



void deleteExtraEntries(float radius){
    Cursor data = getContentResolver().query(DatabaseContract.CONTENT_URI,projection,null,null,null);
    while (data.moveToNext()) {
        String friendName = data.getString(COL_FRIEND_NAME);
        float friendDistance = data.getFloat(COL_FRIEND_DISTANCE);
        if (Utility.checkFriendInDb(getApplicationContext(),friendName)){
          if(friendDistance>radius){
              String selection = DatabaseContract.FriendsEntry.COLUMN_FRIEND_NAME + " = ?";
              String[] selectionArgs = new String[1];
                  selectionArgs[0] = friendName;
                  getContentResolver().delete(DatabaseContract.FriendsEntry.CONTENT_URI,
                          selection,
                          selectionArgs
                  );
          }
        }

    }
}

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onResult(Status status) {
        if (status.isSuccess()) {

        } else {
            // Get the status code for the error and log it using a user-friendly message.
            String errorMessage = GeofenceErrorMessages.getErrorString(this,
                    status.getStatusCode());
            Log.e(TAG, errorMessage);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case LOADER_ID:
                return new CursorLoader(this,
                        DatabaseContract.CONTENT_URI,
                        projection,
                        null,
                        null,
                        null
                );

        }

        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        final int id = loader.getId();

        switch (id) {
            case LOADER_ID:
                sp = getSharedPreferences("GEOFENCES_PREFERENCE", MODE_PRIVATE);
                boolean geofenceAdded = sp.getBoolean(getString(R.string.KEY_GEOFENCE_ADDED), false);

                if (geofenceAdded) {
                    if (data != null) {
                        removeGeofences();
                    }
                }
                geofenceMap.clear();
                while (data.moveToNext()) {
                    String friendName = data.getString(COL_FRIEND_NAME);
                    Float friendLat = data.getFloat(COL_FRIEND_LATITUDE);
                    Float friendLong = data.getFloat(COL_FRIEND_LONGITUDE);
                    geofenceMap.put(friendName, new LatLng(friendLat, friendLong));
                }
                populateGeofenceList();
                addGeofences();
                sp.edit().putBoolean(getString(R.string.KEY_GEOFENCE_ADDED), true).commit();
                break;

            /*case DISTANCE_LOADER_ID:
                ContentValues contentValues = new ContentValues();
                Vector<ContentValues> cvVector = new Vector<>();
                while (data.moveToNext()) {
                    String friendNumber = data.getString(COL_FRIEND_NUMBER);
                    String friendName = data.getString(COL_FRIEND_NAME);
                    String friendlocation = data.getString(COL_FRIEND_LOCATION);
                    float friendlatitude = data.getFloat(COL_FRIEND_LATITUDE);
                    float friendlongitude = data.getFloat(COL_FRIEND_LONGITUDE);

                    Location friendLocation = new Location("locationA");
                    friendLocation.setLatitude(friendlatitude);
                    friendLocation.setLongitude(friendlongitude);

                    Location mylocation = new Location("locationB");
                    mylocation.setLatitude(sharedPreferences.getFloat("UserLatitude", 0));
                    mylocation.setLongitude(sharedPreferences.getFloat("UserLongitude", 0));
                    float distance = mylocation.distanceTo(friendLocation);

                    contentValues.put(Helper.FriendName, friendName);
                    contentValues.put(Helper.FriendNumber, friendNumber);
                    contentValues.put(Helper.FriendLocation, friendlocation);
                    contentValues.put(Helper.FriendLatitude, friendlatitude);
                    contentValues.put(Helper.FriendLongitude, friendlongitude);
                    contentValues.put(myHelper.FriendDistance, distance);
                    cvVector.add(contentValues);
                }
                if (cvVector.size() > 0) {
                    ContentValues[] cvArray = new ContentValues[cvVector.size()];
                    cvVector.toArray(cvArray);
                    int rowsInserted = getContentResolver().
                            bulkInsert(DatabaseContract.CONTENT_URI, cvArray);
                    Log.d(LOG_TAG , "Inserting in database");
                }*/
        }


    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position) {
                case 0:
                    return new FriendList();
                case 1:
                    return new NearByFriendsFragment();
                default:
                    return new FriendList();
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);

            }
            return null;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_swipe, container, false);
            return rootView;
        }
    }


}
