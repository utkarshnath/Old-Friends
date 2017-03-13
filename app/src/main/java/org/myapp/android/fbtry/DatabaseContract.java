package org.myapp.android.fbtry;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by utkarshnath on 10/12/15.
 */
public class DatabaseContract {
    // Content Uri for Content Provider
    public static final String CONTENT_AUTHORITY = "com.example.android.fbtry";

    // Base Content Uri using the Content Authority
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_MYTABLE = "mytable";
    public static final String PATH_FRIENDS_TABLE = "friends_table";
    public static final String PATH_FRIENDS_WITH_LOCATION = "friends_with_location";

    // Content URI to query the MYTABLE
    public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_MYTABLE).build();

    // Content type description
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_MYTABLE;
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_MYTABLE;

    // Table Name
    public static final String TABLE_NAME = "MYTABLE";



    // Table to store the nearby friends
    public static class FriendsEntry implements BaseColumns {

        private static final String QUERY_PARAM_KEY = "KEY_QUERY_PARAM";

        // Content type description
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_FRIENDS_TABLE;
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_FRIENDS_TABLE;
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_FRIENDS_TABLE).build();

        // Table Name String
        public static final String TABLE_NAME = "friends_table";

        // Column to store the name of the friend
        public static final String COLUMN_FRIEND_NAME = "friend_name";

        // Column to store the current Geofence Transition State
        public static final String COLUMN_GEOFENCE_TRANSITION = "geofence_transition";

        public static Uri buildFriendsWithLocationUri() {
            return CONTENT_URI.buildUpon().appendQueryParameter(QUERY_PARAM_KEY , String.valueOf(true)).build();
        }

        public static String getQueryParam(Uri uri) {
            return uri.getQueryParameter(QUERY_PARAM_KEY);
        }
    }
}
