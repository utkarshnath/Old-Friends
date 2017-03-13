package org.myapp.android.fbtry;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by utkarshnath on 10/12/15.
 */
public class FriendListCursorAdapter extends CursorAdapter {
    Location myLocation;
    public FriendListCursorAdapter(Context context, Cursor c, int flags,Location myLocation) {
        super(context, c, flags);
        this.myLocation = myLocation;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.friendlistitem, viewGroup, false);
        myViewHolder mViewHolder = new myViewHolder(view);
        view.setTag(mViewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        myViewHolder mViewHolder = (myViewHolder) view.getTag();

        String friendName = cursor.getString(FriendList.COL_FRIEND_NAME);
        mViewHolder.friendname.setText(friendName);
        String Path = cursor.getString(FriendList.COL_FRIEND_DP_PATH);
        Drawable dp;
        if(Path == null){
            dp = context.getResources().getDrawable(R.drawable.user);
            Log.v("!@#path", "pathempty");
        }else{
            dp = getScaledImage(context,128,128,Path);
            Log.v("!@#path1", Path);
        }
        mViewHolder.frienddp.setImageDrawable(dp);
//
        String friendLatitude = cursor.getString(FriendList.COL_FRIEND_Longitude);
        String friendLongitude = cursor.getString(FriendList.COL_FRIEND_Longitude);
        float friendDistance = cursor.getFloat(FriendList.COL_FRIEND_DISTANCE);
        if(friendDistance == 0){
            mViewHolder.friendlocation.setText("Calculating...");
        }
        else if(friendDistance==-1){
            mViewHolder.friendlocation.setText("Location Not Found ");
        }
        else if(friendDistance<1000){
            int distance = (int) (friendDistance);
            mViewHolder.friendlocation.setText(distance + " m away ");
        }
        else {
            int distance = (int) (friendDistance/1000);
            mViewHolder.friendlocation.setText(distance + " Km away ");
        }


        mViewHolder.friendnumber.setText("Last seen At "+ cursor.getString(FriendList.COL_FRIEND_LAST_UPDATED));


    }

    public class myViewHolder{

        TextView friendname;
        TextView friendnumber;
        TextView friendlocation;
        ImageView frienddp;
        public myViewHolder(View itemView) {
            friendname = (TextView) itemView.findViewById(R.id.friend_name);
            friendnumber = (TextView) itemView.findViewById(R.id.last_seen);
            friendlocation = (TextView) itemView.findViewById(R.id.distance_away);
            frienddp = (ImageView) itemView.findViewById(R.id.friend_dp);
            Typeface typeface = Typeface.createFromAsset(mContext.getAssets(),"MavenPro-Regular.ttf");
            friendname.setTypeface(typeface);

        }


    }



    private Drawable getScaledImage(Context context, int reqWidth, int reqHeight,String path) {

            // Decode the input stream into a bitmap.
            Bitmap bitmap = getResizedBitmap(path, reqWidth, reqHeight);
            return new BitmapDrawable(context.getResources(), bitmap);
            // If was successfully created.

    }

    public static Bitmap getResizedBitmap(String filepath, int reqWidth, int reqHeight) {
        Bitmap bitmap;

        // Decode bitmap to get current dimensions.
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filepath, options);

        // Calculate sample size of bitmap.
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap again, setting the new dimensions.
        options.inJustDecodeBounds = false;
        bitmap = BitmapFactory.decodeFile(filepath, options);

        // Return resized bitmap.
        return bitmap;
    }


    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}
