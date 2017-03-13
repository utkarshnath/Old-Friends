package org.myapp.android.fbtry;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

public class Utility {

    private static final String TAG = Utility.class.getSimpleName();

    public static boolean checkFriendInDb(Context context, String friendName) {
        Cursor cursor = context.getContentResolver().query(
                DatabaseContract.FriendsEntry.CONTENT_URI,
                null,
                DatabaseContract.FriendsEntry.COLUMN_FRIEND_NAME + " = '" + friendName + "'",
                null,
                null
        );

        if (cursor == null) {
            Log.d(TAG, "checkFriendInDb: Cursor is null");
            return false;
        } else if (cursor.getCount() > 0) {
            Log.d(TAG, "checkFriendInDb: Cursor Count : " + cursor.getCount());
            return true;
        } else {
            Log.d(TAG, "checkFriendInDb: Cursor Count Zero : " + cursor.getCount() );
            return false;
        }

    }
}
