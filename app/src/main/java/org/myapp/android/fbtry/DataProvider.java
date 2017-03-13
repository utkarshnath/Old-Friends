package org.myapp.android.fbtry;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

/**
 * Created by utkarshnath on 10/12/15.
 */
public class DataProvider extends ContentProvider {
    private myHelper mHelper;
    private static final String LOG_TAG = DataProvider.class.getSimpleName();
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private static final int MYTABLE = 100;
    private static final int FRIENDS_TABLE = 101;
    private static final int FRIENDS_WITH_LOCATION = 102;

    private static final SQLiteQueryBuilder sFriendsWithLocationQueryBuilder;

    static {
        sFriendsWithLocationQueryBuilder = new SQLiteQueryBuilder();
        sFriendsWithLocationQueryBuilder.setTables(DatabaseContract.FriendsEntry.TABLE_NAME +
                " INNER JOIN " + DatabaseContract.TABLE_NAME +
                " ON "  + DatabaseContract.FriendsEntry.TABLE_NAME + "." +
                DatabaseContract.FriendsEntry.COLUMN_FRIEND_NAME + " = " +
                DatabaseContract.TABLE_NAME + "." + myHelper.FriendName );
    }

    @Override
    public boolean onCreate() {
        mHelper = new myHelper(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor = null;
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case MYTABLE:
                Log.d(LOG_TAG,"MYTABLE Queried");
                retCursor = mHelper.getReadableDatabase().query(
                        DatabaseContract.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                Log.d(LOG_TAG , "retCursor Size : " + retCursor.getCount());
                break;
            case FRIENDS_TABLE:
                Log.d(LOG_TAG , "FRIENDS_TABLE Queried");
                String queryParam = DatabaseContract.FriendsEntry.getQueryParam(uri);
                if (queryParam == null) {
                    retCursor = mHelper.getReadableDatabase().query(
                            DatabaseContract.FriendsEntry.TABLE_NAME,
                            projection,
                            selection,
                            selectionArgs,
                            null,
                            null,
                            sortOrder
                    );
                    Log.d(LOG_TAG , "Friends Table Cursor Size : " + retCursor.getCount());
                }
                else {
                    Log.d(LOG_TAG , "FRIENDS_WITH_LOCATION Queried");
                    retCursor = sFriendsWithLocationQueryBuilder.query(
                            mHelper.getReadableDatabase(),
                            projection,
                            selection,
                            selectionArgs,
                            null,
                            null,
                            sortOrder);
                    Log.d(LOG_TAG , "retCursor Size : "  + retCursor.getCount());
                }
                break;
            default:
                Log.d(LOG_TAG, "No Uri Match Found!!");
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case MYTABLE:
                return DatabaseContract.CONTENT_TYPE;
            case FRIENDS_TABLE:
                return DatabaseContract.FriendsEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Uri Mismatch");
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final int match = sUriMatcher.match(uri);
        Uri returnUri = null;
        switch (match) {
            case MYTABLE:
                long idx = mHelper.getWritableDatabase().insert(DatabaseContract.TABLE_NAME, null, values);
                if (idx > 0) {
                    Log.d(LOG_TAG, "Inserted in database");
                    returnUri = uri;
                }

                break;
            case FRIENDS_TABLE:
                idx = mHelper.getWritableDatabase().insert(DatabaseContract.FriendsEntry.TABLE_NAME, null, values);
                if (idx > 0) {
                    returnUri = uri;
                }
                break;
            default:
                throw new UnsupportedOperationException("Uri Mismatch");
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        int rowsDeleted = 0;
        switch (match) {
            case MYTABLE:
                rowsDeleted = mHelper.getWritableDatabase().delete(DatabaseContract.TABLE_NAME, selection, selectionArgs);
                break;
            case FRIENDS_TABLE:
                rowsDeleted = mHelper.getWritableDatabase().delete(DatabaseContract.FriendsEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Uri Mismatch");
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        int rowsUpdated = 0;
        switch (match) {
            case MYTABLE:
                rowsUpdated = mHelper.getWritableDatabase().update(DatabaseContract.TABLE_NAME, values, selection, selectionArgs);
                break;
            case FRIENDS_TABLE:
                rowsUpdated = mHelper
                        .getWritableDatabase()
                        .update(
                                DatabaseContract.FriendsEntry.TABLE_NAME,
                                values,
                                selection,
                                selectionArgs
                        );
                break;
            default:
                throw new UnsupportedOperationException("Uri Mismatch");
        }
        if (rowsUpdated > 0)
            getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final int match = sUriMatcher.match(uri);
        final SQLiteDatabase db = mHelper.getWritableDatabase();

        switch (match) {
            case MYTABLE:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues contentValues : values) {
                        long _idx = db.insert(DatabaseContract.TABLE_NAME, null, contentValues);
                        if (_idx != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    // Helper method to build the URI MATCHER
    private static UriMatcher buildUriMatcher() {
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String CONTENT_AUTHORITY = DatabaseContract.CONTENT_AUTHORITY;

        // Add the Content Uri to the matcher
        matcher.addURI(CONTENT_AUTHORITY, DatabaseContract.PATH_MYTABLE, MYTABLE);
        matcher.addURI(CONTENT_AUTHORITY, DatabaseContract.PATH_FRIENDS_TABLE, FRIENDS_TABLE);
        return matcher;
    }
}
