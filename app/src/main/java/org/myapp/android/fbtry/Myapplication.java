package org.myapp.android.fbtry;

import android.app.Application;

import com.parse.Parse;

/**
 * Created by utkarshnath on 30/07/15.
 */
public class Myapplication extends Application {

    @Override

    public void onCreate() {
        super.onCreate();
        Parse.initialize(this, "0XurzCl47TBaALAIzcO2Ru3qxTHeZWcCQOFVUW7o", "rx3drVi0x9nebXETXctQhmMRNH5Lb5BWGKQ5e8el");

    }
}

