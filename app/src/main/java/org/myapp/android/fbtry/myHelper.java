package org.myapp.android.fbtry;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class myHelper extends SQLiteOpenHelper {
    public static final String DataBase_Name = "mydatabase";
    public static final String Table_Name = "MYTABLE";
    public static final String UID = "_id";
    public static final String FriendName = "FriendName";
    public static final String FriendNumber = "FriendNumber";
    public static final String FriendLocation = "FriendLocation";
    public static final String FriendLatitude = "FriendLatitude";
    public static final String FriendLongitude = "FriendLongitude";
    public static final String FriendDistance = "FriendDistance";
    public static final String FriendDpPath = "FriendDpPath";
    public static final String LastupdatedAt = "LastUpdatedAt";


    public static final int Version = 3;
    public myHelper(Context context) {
        super(context,DataBase_Name,null,Version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE "  + Table_Name +
                " (" + UID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                FriendName + " VARCHAR(255)," +
                FriendLocation + " VARCHAR(255)," +
                FriendLatitude + " FLOAT," +
                FriendLongitude + " FLOAT," +
                FriendDpPath + " VARCHAR(255)," +
                LastupdatedAt + " VARCHAR(255)," +
                FriendNumber + " VARCHAR(255),"+

                FriendDistance + " FLOAT,"+" UNIQUE ("+FriendNumber+") ON CONFLICT REPLACE);");

        final String SQL_CREATE_FREINDS_TABLE_QUERY = "CREATE TABLE " + DatabaseContract.FriendsEntry.TABLE_NAME + "(" +
                DatabaseContract.FriendsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DatabaseContract.FriendsEntry.COLUMN_FRIEND_NAME + " TEXT NOT NULL," +
                DatabaseContract.FriendsEntry.COLUMN_GEOFENCE_TRANSITION + " INTEGER NOT NULL, " +

                // The table will have unique friend names and conflict will be replaced
                "UNIQUE (" + DatabaseContract.FriendsEntry.COLUMN_FRIEND_NAME + ") ON CONFLICT REPLACE);";

        db.execSQL(SQL_CREATE_FREINDS_TABLE_QUERY);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + Table_Name);
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.FriendsEntry.TABLE_NAME);
        onCreate(db);
    }
}