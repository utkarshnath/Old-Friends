package org.myapp.android.fbtry;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by utkarshnath on 19/12/15.
 */
public class nearByFriendsCursorAdapter extends CursorAdapter {

        public nearByFriendsCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.frienditem,viewGroup,false);
        myViewHolder mViewHolder = new myViewHolder(view);
        view.setTag(mViewHolder);
        return view;
    }

        @Override
        public void bindView(View view, final Context context, final Cursor cursor) {
        myViewHolder mViewHolder = (myViewHolder) view.getTag();

        String friendName = cursor.getString(cursor.getColumnIndex(NearByFriendsFragment.projection[1]));
        mViewHolder.friendname.setText(friendName);

        final String number = cursor.getString(NearByFriendsFragment.COL_FRIEND_NUMBER);

            String Path = cursor.getString(NearByFriendsFragment.COL_FRIEND_DP_PATH);
            Drawable dp;
            if(Path == null){
                dp = context.getResources().getDrawable(R.drawable.user);
                Log.v("!@#path", "pathempty");
            }else{
                dp = getScaledImage(context,128,128,Path);
                Log.v("!@#path1", Path);
            }
            mViewHolder.frienddp.setImageDrawable(dp);

        mViewHolder.call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", number, null));
                context.startActivity(intent);
            }
        });

            mViewHolder.message.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent send = new Intent(Intent.ACTION_SEND);
                    send.setType("text/plain");
                    send.putExtra(
                            Intent.EXTRA_TEXT,
                            "Hey,using Old Friends app I got to know that you are near by,would you like to meet");
                    context.startActivity(Intent.createChooser(send, "Share with"));
                }
            });


    }

        public class myViewHolder{

            TextView friendname;
            ImageView call;
            ImageView message;
            ImageView frienddp;
            public myViewHolder(View itemView) {
                friendname = (TextView) itemView.findViewById(R.id.friendname);
                call = (ImageView) itemView.findViewById(R.id.friend_call);
                message = (ImageView) itemView.findViewById(R.id.friend_message);
                Typeface typeface = Typeface.createFromAsset(mContext.getAssets(),"MavenPro-Regular.ttf");
                friendname.setTypeface(typeface);
                frienddp = (ImageView) itemView.findViewById(R.id.friend_dp);
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
