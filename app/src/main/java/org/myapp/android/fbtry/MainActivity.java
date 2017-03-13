package org.myapp.android.fbtry;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.parse.ParseObject;
import com.sinch.verification.CodeInterceptionException;
import com.sinch.verification.Config;
import com.sinch.verification.InvalidInputException;
import com.sinch.verification.ServiceErrorException;
import com.sinch.verification.SinchVerification;
import com.sinch.verification.Verification;
import com.sinch.verification.VerificationListener;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {


   EditText myNumber;
   Button signIn;
    SharedPreferences sp,sharedPreferences;
    ProgressBar progressBar;
    Verification verification;
    float radius;
    private final String APPLICATION_KEY = "8299e7bc-edcc-48c5-b342-dacfea257173";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(this);
        sp = getSharedPreferences("USER", Context.MODE_PRIVATE);
        String checkFirst = sp.getString("UserNumber", null);
        if(checkFirst!=null){
            Intent intent = new Intent();
            intent.setClass(this,SwipeActivity.class);
            startActivity(intent);
        }else{
            setContentView(R.layout.activity_main);
            sharedPreferences = getSharedPreferences("UserRadius",Context.MODE_PRIVATE);
            radius = Constants.GEOFENCE_RADIUS_IN_METERS;
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putFloat("Radius", radius);
            editor.commit();
            myNumber = (EditText) findViewById(R.id.userNumber);
            signIn = (Button) findViewById(R.id.button_signIn);
            progressBar = (ProgressBar) findViewById(R.id.progressBar);
            signIn.setOnClickListener(this);
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public void onClick(View v) {
        if (myNumber.getText().length() == 10) {
            showProgressDialog();
            startVerification("+91"+myNumber.getText().toString());
//            openNextPg();
        } else {
            Toast.makeText(this, "Enter a valid Number", Toast.LENGTH_SHORT).show();
        }
    }

    void openNextPg(){
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("UserNumber", myNumber.getText().toString());
        editor.commit();
        Intent intent = new Intent();
        intent.setClass(this,SwipeActivity.class);
        startActivity(intent);
    }

    private void startVerification(String phoneNumber) {
        Config config = SinchVerification.config().applicationKey(APPLICATION_KEY).context(getApplicationContext()).build();
        VerificationListener listener = new MyVerificationListener();
        verification = SinchVerification.createFlashCallVerification(config, phoneNumber, listener);
        verification.initiate();
    }

    private class MyVerificationListener implements VerificationListener {
        @Override
        public void onInitiated() {}

        @Override
        public void onInitiationFailed(Exception e) {
            hideProgressDialog();
            if (e instanceof InvalidInputException) {
                Toast.makeText(MainActivity.this,"Incorrect number provided",Toast.LENGTH_LONG).show();
            } else if (e instanceof ServiceErrorException) {
                Toast.makeText(MainActivity.this,e+"",Toast.LENGTH_LONG).show();
                Log.d("!@#exception",e+"");
            } else {
                Toast.makeText(MainActivity.this,"check your network state", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onVerified() {
            hideProgressDialog();
            ParseObject userList = new ParseObject("UserList");
            userList.put("Number", myNumber.getText().toString());
            userList.saveInBackground();
            new AlertDialog.Builder(MainActivity.this)
                    .setMessage("Verification Successful!")
                    .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            dialog.cancel();
                            openNextPg();
                        }
                    })
                    .show();
        }

        @Override
        public void onVerificationFailed(Exception e) {
            hideProgressDialog();
            if (e instanceof CodeInterceptionException) {
                Toast.makeText(MainActivity.this,"Intercepting the verification call automatically failed",Toast.LENGTH_LONG).show();
            } else if (e instanceof ServiceErrorException) {
                Toast.makeText(MainActivity.this, "Sinch service error",Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(MainActivity.this,"Other system error, check your network state", Toast.LENGTH_LONG).show();
            }
        }


    }
    private void showProgressDialog() {
        progressBar.setVisibility(ProgressBar.VISIBLE);
    }

    private void hideProgressDialog() {
        progressBar.setVisibility(ProgressBar.INVISIBLE);
    }
}


