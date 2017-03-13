package org.myapp.android.fbtry;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.ContactsContract;
import android.util.Log;

import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

/**
 * Created by utkarshnath on 16/01/16.
 */
public class Task extends AsyncTask<Void,Void,Void> {
     Context context;
     myHelper Helper;

    public Task(Context context) {
        this.context = context;
    }

    @Override
    protected Void doInBackground(Void... Void) {
        Helper = new myHelper(context);
        createFriendsList();
        return null;
    }


    public void createFriendsList() {
        ContentResolver cr = context.getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);
        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                if (Integer.parseInt(cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    Cursor pCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id}, null);
                    while (pCur.moveToNext()) {
                        String phoneNo = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        String number = getNumber(phoneNo);
                        checkNumberPresence(number, name);
                        Log.v("!@#name", name+" "+number);
                    }
                    pCur.close();
                }
            }
        }
    }

    public String getNumber(String number) {
        String s = new String();
        String s1 = new String();
        int length = number.length();
        for(int i=0;i<length;i++){
            if(number.charAt(i)==' '){
                continue;
            }else
               s+=number.charAt(i);
        }
        for(int i=s.length()-1,k=0;k<10 && i>-1;k++,i--){
            s1+=s.charAt(i);
        }
        String finalNumber = new StringBuffer(s1).reverse().toString();

        return finalNumber;
    }

    public void checkNumberPresence(final String number, final String name) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("UserList");
        Log.v("!@#check", number+" checking on parse");
        query.whereEqualTo("Number", number);
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            public void done(final ParseObject object, ParseException e) {
                if (object != null) {
                    Log.v("!@#done", name+" "+number+" present on parse");
                    final ContentValues contentValues = new ContentValues();
                    contentValues.put(Helper.FriendName, name);
                    contentValues.put(Helper.FriendNumber, number);
                    contentValues.put(Helper.FriendLocation, object.getString("CurrentLocation"));
                    contentValues.put(Helper.FriendLatitude, object.getDouble("Latitude"));
                    contentValues.put(Helper.FriendLongitude, object.getDouble("Longitude"));
                    Date date = object.getUpdatedAt();

                    contentValues.put(Helper.LastupdatedAt,date.getHours()+":"+date.getMinutes()+"  "+date.getDate()+" "+int_to_month(date.getMonth()));

                    contentValues.put(myHelper.FriendDistance, "Calculation...");

                    ParseFile fileObject = object.getParseFile("Image");
                    if (fileObject != null) {
                        fileObject.getDataInBackground(new GetDataCallback() {
                            public void done(byte[] data, ParseException e) {
                                if (e == null && data != null) {

                                    // Decode the Byte[] into
                                    // Bitmap
                                    downloadImage(data, object.getObjectId(), contentValues);
                                    Log.d("!@#test", "We've got data in data.");
                                } else {
                                    Log.d("test",
                                            "There was a problem downloading the data.");
                                    //getContentResolver().insert(DatabaseContract.CONTENT_URI, contentValues);
                                }

                            }
                        });
                    }else context.getContentResolver().insert(DatabaseContract.CONTENT_URI, contentValues);
//                }else{
//                        getContentResolver().insert(DatabaseContract.CONTENT_URI, contentValues);
//                    }


//                    Date date = object.getUpdatedAt();
//                    Calendar rightNow = Calendar.getInstance();
//                    boolean n = rightNow.getTime().getDate() == date.getDate();
//                    Log.v("!@#yesactual", date + "  " + n);
                }
            }
        });
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
        context.getContentResolver().insert(DatabaseContract.CONTENT_URI, contentValues);
    }


}
