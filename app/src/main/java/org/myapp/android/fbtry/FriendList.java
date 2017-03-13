package org.myapp.android.fbtry;


import android.app.Fragment;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendList extends android.support.v4.app.Fragment implements LoaderManager.LoaderCallbacks<Cursor> {


    ArrayList<Friend> list;
    ListView listView;
    FriendListCursorAdapter friendListCursorAdapter;
    private static final String LOG_TAG = FriendList.class.getSimpleName();

    //Uniquely Identify a Loader
    private static final int LOADER_ID = 0;

    final String[] projections = new String[]{
            myHelper.UID,
            myHelper.FriendNumber,
            myHelper.FriendName,
            myHelper.FriendLatitude,
            myHelper.FriendLongitude,
            myHelper.FriendLocation,
            myHelper.FriendDistance,
            myHelper.FriendDpPath,
            myHelper.LastupdatedAt
    };

    public static final int COL_ID = 0;
    public static final int COL_FRIEND_NUMBER = 1;
    public static final int COL_FRIEND_NAME = 2;
    public static final int COL_FRIEND_Latitude = 3;
    public static final int COL_FRIEND_Longitude = 4;
    public static final int COL_FRIEND_LOCATION = 5;
    public static final int COL_FRIEND_DISTANCE = 6;
    public static final int COL_FRIEND_DP_PATH = 7;
    public static final int COL_FRIEND_LAST_UPDATED = 8;

    public FriendList() {
        // Required empty public constructor
    }

    myHelper Helper;
    SQLiteDatabase db;
    SharedPreferences sharedPreferences;
    SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_friend_list, container, false);
        listView = (ListView) v.findViewById(R.id.listView);
        sharedPreferences = getActivity().getSharedPreferences("USER", Context.MODE_PRIVATE);
        mSwipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.Feed_swipe_refresh_layout);
        Location mylocation =new Location("locationA");
        mylocation.setLatitude(sharedPreferences.getFloat("UserLatitude", 0));
        mylocation.setLongitude(sharedPreferences.getFloat("UserLongitude", 0));
        friendListCursorAdapter = new FriendListCursorAdapter(getActivity() , null , 0,mylocation);
        listView.setAdapter(friendListCursorAdapter);
        //fetchFriendFromDatabase();
        //FriendlistAdapter adapter = new FriendlistAdapter(getActivity(), list);
        getLoaderManager().restartLoader(LOADER_ID , null , this);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchFromServer();
                mSwipeRefreshLayout.setRefreshing(false);
            }

        });
        return v;
    }


    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(getActivity(), DatabaseContract.CONTENT_URI, projections, null, null, myHelper.FriendName + " ASC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        Log.d(LOG_TAG , "OnLoadFinished");
        friendListCursorAdapter.swapCursor(cursor);
    }


    @Override
    public void onLoaderReset(Loader loader) {
        friendListCursorAdapter.swapCursor(null);
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    void fetchFromServer(){
        Cursor data = getContext().getContentResolver().query(DatabaseContract.CONTENT_URI,projections,null,null,null);
        while (data.moveToNext()) {
            final String friendNumber = data.getString(COL_FRIEND_NUMBER);
            final String friendName = data.getString(COL_FRIEND_NAME);
            final String frienddpPath = data.getString(COL_FRIEND_DP_PATH);

            ParseQuery<ParseObject> query = ParseQuery.getQuery("UserList");
            query.whereEqualTo("Number", friendNumber);
            query.getFirstInBackground(new GetCallback<ParseObject>() {
                public void done(final ParseObject object, ParseException e) {
                    if (object != null) {
                        final ContentValues contentValues = new ContentValues();
                        contentValues.put(Helper.FriendName, friendName);
                        contentValues.put(Helper.FriendNumber, friendNumber);
                        contentValues.put(Helper.FriendLocation, object.getString("CurrentLocation"));

                        float friendlatitude = (float) object.getDouble("Latitude");
                        contentValues.put(Helper.FriendLatitude, object.getDouble("Latitude"));

                        float friendlongitude = (float) object.getDouble("Longitude");
                        contentValues.put(Helper.FriendLongitude, object.getDouble("Longitude"));
                        Date date = object.getUpdatedAt();

                        contentValues.put(Helper.FriendDpPath,frienddpPath);

                        contentValues.put(Helper.LastupdatedAt,date.getHours()+":"+date.getMinutes()+"  "+date.getDate()+" "+int_to_month(date.getMonth()));

                        Location friendLocation = new Location("locationA");
                        friendLocation.setLatitude(friendlatitude);
                        friendLocation.setLongitude(friendlongitude);

                        Location mylocation = new Location("locationB");
                        mylocation.setLatitude(sharedPreferences.getFloat("UserLatitude", 0));
                        mylocation.setLongitude(sharedPreferences.getFloat("UserLongitude", 0));

                        float distance = mylocation.distanceTo(friendLocation);

                        contentValues.put(myHelper.FriendDistance,distance);

                        getContext().getContentResolver().insert(DatabaseContract.CONTENT_URI, contentValues);
                    }
                }
            });


        }
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
}
