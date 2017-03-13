package org.myapp.android.fbtry;


import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.melnykov.fab.FloatingActionButton;

/**
 * Created by Sparsha on 12/17/2015.
 */

public class NearByFriendsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int LOADER_ID = 100;
    private static ListView listView;
    private static SimpleCursorAdapter listViewAdapter;
    FrameLayout fl;



    public static final String[] projection = new String[]{
            DatabaseContract.FriendsEntry.TABLE_NAME + "." +
            DatabaseContract.FriendsEntry._ID,
            DatabaseContract.FriendsEntry.COLUMN_FRIEND_NAME,
            myHelper.FriendNumber,
            myHelper.FriendDpPath
    };

    public static final int COL_ID = 0;
    public static final int COL_FRIEND_NAME = 1;
    public static final int COL_FRIEND_NUMBER = 2;
    public static final int COL_FRIEND_DP_PATH = 3;

    @Override
    public void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    nearByFriendsCursorAdapter cursorAdapter ;
    @Override
    public View onCreateView(LayoutInflater inflater,  ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // Initialize the Loader

        // Inflate the View and return
        View view = inflater.inflate(R.layout.fragment_near_by_friends, container, false);

        // Initialize the instance variables
        listView = (ListView) view.findViewById(R.id.listView);



        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.attachToListView(listView);
        fl = (FrameLayout) view.findViewById(R.id.fl);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((SwipeActivity) getActivity()).setRadiusChangeDialogBox();
            }
        });

        cursorAdapter = new nearByFriendsCursorAdapter(getActivity(),null,0);
        listView.setAdapter(cursorAdapter);
        getLoaderManager().initLoader(LOADER_ID, null, this);
        return view;
    }




    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(),
                DatabaseContract.FriendsEntry.buildFriendsWithLocationUri(),
                projection,
                null,
                null,
                DatabaseContract.FriendsEntry.COLUMN_FRIEND_NAME + " ASC"
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        cursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        cursorAdapter.swapCursor(null);
    }


}