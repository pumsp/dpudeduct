package com.deductmark.pumsper.dpudeduct;


import android.app.Activity;
import android.app.Dialog;
import android.content.Context;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.provider.Settings;

import android.os.Bundle;


import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;

import com.pnikosis.materialishprogress.ProgressWheel;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;

import me.drakeet.materialdialog.MaterialDialog;


public class MainActivity extends Activity {

    private Button mLogin;
    private EditText mUsername;
    private EditText mPassword;
    private Context mContext;

    private final String KEY_PREFS = "prefs_user";
    private final String KEY_USERNAME = "username";
    private SharedPreferences mPrefs;
    private SharedPreferences.Editor mEditor;
    private int netCount;

    //dialog
    private ProgressWheel wheel;
    private  NetworkInfo netInfo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mContext = this;
        mLogin = (Button) findViewById(R.id.button_login);
        mUsername = (EditText) findViewById(R.id.username);
        mPassword = (EditText) findViewById(R.id.password);
        mPrefs = mContext.getSharedPreferences(KEY_PREFS, Context.MODE_PRIVATE);
        mEditor = mPrefs.edit();
        netCount = 0;
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        netInfo = cm.getActiveNetworkInfo();


        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkLogin();
                mLogin.setBackgroundColor(Color.parseColor("#7d4df5"));

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        // Actions to do after 10 seconds
                        mLogin.setBackgroundColor(Color.parseColor("#6d03e2"));
                    }
                }, 180);


            }
        });

        /***
         * เซ็ตให้ keyboard หาย เมื่อกดที่วิว
         * ต้องไปเพิ่ม android:focusableInTouchMode="true" ที่ layout ที่เราใช้ด้วย
         */

        View v = findViewById(R.id.login);
        v.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                mUsername.clearFocus();
                mPassword.clearFocus();
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                return true;
            }
        });

        wheel = (ProgressWheel) findViewById(R.id.progress_center);

    }

    private void showDialog(){

        wheel.setVisibility(View.VISIBLE);
        try{
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        catch(Exception e){
            e.printStackTrace();
        }

        mUsername.clearFocus();
        mPassword.clearFocus();
        mUsername.setEnabled(false);
        mPassword.setEnabled(false);
        mLogin.setClickable(false);

    }
    private void hideDialog(){
        wheel.setVisibility(View.INVISIBLE);
        mUsername.setEnabled(true);
        mPassword.setEnabled(true);
        mLogin.setClickable(true);

    }

    private int count = 0;
    @Override
    protected void onResume() {
        super.onResume();

        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        netInfo = cm.getActiveNetworkInfo();
//        Toast.makeText(MainActivity.this, ""+netInfo, Toast.LENGTH_SHORT).show();

        if(netInfo != null && netInfo.isConnectedOrConnecting()){
            netCount++;
//            Toast.makeText(mContext, "if"+netCount, Toast.LENGTH_SHORT).show();

        }
        else{
            if(netCount == 0 || netInfo == null){

                final MaterialDialog mMaterialDialog = new MaterialDialog(this);
                mMaterialDialog
                        .setMessage("ตั้งค่าการเชื่อมต่ออินเตอร์เน็ต")
                        .setPositiveButton("ตกลง", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(Settings.ACTION_SETTINGS);
                                startActivity(intent);
                                netCount = 0;
                                mMaterialDialog.dismiss();
                            }
                        });

                mMaterialDialog.show();
            }


        }
        String user = mPrefs.getString(KEY_USERNAME,"Not fund");
        if(!user.equals("Not fund") && count == 0){
            count++;
            Intent intent = new Intent(mContext, home_activity.class);
            startActivity(intent);
            finish();
        }

//        View view = this.getCurrentFocus();
//        if (view != null) {
//            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
//            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
//        }


    }
    private String message = "";
    private void checkLogin() {
        showDialog();
        String username = mUsername.getText().toString().trim();
        String password = mPassword.getText().toString().trim();

        if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password)) {
            OkHttpClient okHttpClient = new OkHttpClient();

            Request.Builder builder = new Request.Builder();
            RequestBody formBody = new FormEncodingBuilder()
                    .add("em", username)
                    .add("ps", password)
                    .build();
            String ip = "http://ceproject2.dpu.ac.th";
//            String ip = "http://192.168.1.36";
            Request request = builder.url(ip+"/sao/nfc_service/login.php")
                    .post(formBody)
                    .build();


            okHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    updateView("Error - " + e.getMessage());
                }

                @Override
                public void onResponse(Response response) {
                    if (response.isSuccessful()) {
                        try {
                            updateView(response.body().string());
                        } catch (IOException e) {
                            e.printStackTrace();
                            updateView("Error " + e.getMessage());
                        }
                    } else {
                        updateView("Error " + response.code());
                    }
                }

                public void updateView(final String strResult) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            hideDialog();
//                            texter.setText(strResult);
                            Log.w("result", strResult);

                            if(strResult.equals("nodata")){
                                message = getString(R.string.login_error_message);
                                Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();

                            }
                            else{
                                if(!strResult.split(" ")[0].equals("Error")){
                                    mEditor.putString(KEY_USERNAME, strResult);
                                    mEditor.commit();

                                    Intent intent = new Intent(mContext, home_activity.class);
                                    startActivity(intent);
                                    finish();
                                }
                                else{
                                    Toast.makeText(mContext, strResult , Toast.LENGTH_SHORT).show();
                                }


//                                message = mPrefs.getString(KEY_USERNAME,"Not fund");
//                                Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
                            }


                        }
                    });
                }
            });
        }
        else{
//            Toast.makeText(MainActivity.this, "else", Toast.LENGTH_SHORT).show();

            hideDialog();
            if(TextUtils.isEmpty(username)){
                Toast.makeText(MainActivity.this, "กรุณากรอกชื่อผู้ใช้งาน", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(MainActivity.this, "กรุณากรอกรหัสผ่าน", Toast.LENGTH_SHORT).show();
            }
        }


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
