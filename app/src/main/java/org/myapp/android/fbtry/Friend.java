package org.myapp.android.fbtry;

import android.graphics.Bitmap;

/**
 * Created by utkarshnath on 24/10/15.
 */
public class Friend {

    String friendName;
    String friendNumber;
    Bitmap friendDp;
    String lastlocation;
    String Latitude;
    String Longitude;
    String friendStatus;
    String friendDpPath;

    Friend(String friendName,String friendNumber){
        this.friendName = friendName;
        this.friendNumber = friendNumber;
    }
    void setFriendDp(Bitmap friendDp){
        this.friendDp = friendDp;
    }
    void setLastlocation(String lastlocation){
        this.lastlocation = lastlocation;
    }
    void setFriendStatus(String friendStatus){
        this.friendStatus = friendStatus;
    }
    void setLatitude(String Latitude){
        this.Latitude = Latitude;
    }
    void setLongitude(String Longitude){
        this.Longitude = Longitude;
    }

}
