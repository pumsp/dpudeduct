package com.deductmark.pumsper.dpudeduct;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import com.pnikosis.materialishprogress.ProgressWheel;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;


import java.io.IOException;

import me.drakeet.materialdialog.MaterialDialog;

public class home_activity extends AppCompatActivity {

    private final String KEY_PREFS = "prefs_user";
    private final String KEY_USERNAME = "username";
    private SharedPreferences mPrefs;
    private SharedPreferences.Editor mEditor;

    private Context mContext;

    // NFC
    private NfcAdapter mAdapter;
    private PendingIntent mPendingIntent;


    //progress dialog
    private ProgressWheel wheel;

    private TextView texter;
    private NetworkInfo netInfo;
    private int count;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mContext = this;
        count = 0;

        mPrefs = mContext.getSharedPreferences(KEY_PREFS, Context.MODE_PRIVATE);
        mEditor = mPrefs.edit();
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        netInfo = cm.getActiveNetworkInfo();

        //progreass dialog
        wheel = (ProgressWheel) findViewById(R.id.progress_center);
//        TextView tv = (TextView)findViewById(R.id.texter);
//        String mess = mPrefs.getString(KEY_USERNAME,"Not fund");
//        tv.setText(""+mess);

        Toolbar toolbar = (Toolbar)findViewById(R.id.tool_bar);
        toolbar.setTitle("ระบบตัดคะแนนนักศึกษา");
//
////
        setSupportActionBar(toolbar);


        //test show / edit
        Button btn_show = (Button) findViewById(R.id.btn_show);
        Button btn_edit = (Button) findViewById(R.id.btn_edit);

        btn_show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getData("1E48046D");
            }
        });

        btn_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getData("1E48046B");
            }
        });

        btn_edit.setVisibility(View.INVISIBLE);
        btn_show.setVisibility(View.INVISIBLE);

        //NFC
        texter = (TextView) findViewById(R.id.texter);

        resolveIntent(getIntent());
        mAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mAdapter == null) {
            texter.setText("เครื่องของคุณไม่มี NFC");
//            finish();
            return;
        }
        mPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
//        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_WITH_TEXT);



    }

    private void showDialog(){
        try{
            if (mAdapter != null) {
                mAdapter.disableForegroundDispatch(this);
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        texter.setTextColor(Color.parseColor("#95a5a6"));
        wheel.setVisibility(View.VISIBLE);

    }
    private void hideDialog(){
        wheel.setVisibility(View.INVISIBLE);
        texter.setTextColor(Color.parseColor("#7f8c8d"));

    }
    private void showSettingsDialog(String show) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(show);
        builder.setPositiveButton("เปิด", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(Settings.ACTION_SETTINGS);
                builder.create().dismiss();
                startActivity(intent);
                count = 0;
            }
        });

        builder.create().show();

    }

    private String hexData;
    private void getData(String hexcode){
        Log.w("result","start");
        showDialog();
        Log.w("result", "start2");
        hexData = (hexcode.trim()).toUpperCase();
//        Toast.makeText(MainActivity.this, "getData "+hexcode, Toast.LENGTH_SHORT).show();
        Log.w("result","hexData = "+hexData);
        OkHttpClient okHttpClient = new OkHttpClient();
//
        Request.Builder builder = new Request.Builder();
        Request request = builder.url("http://ceproject2.dpu.ac.th/sao/nfc_service/checkHex.php?uc="+hexcode).build();

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
                            updateView("Error - " + e.getMessage());
                        }
                    } else {
                        updateView("Not Success - code : " + response.code());
                    }
                }

                public void updateView(final String strResult) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try{
//                                Toast.makeText(home_activity.this, ""+strResult, Toast.LENGTH_SHORT).show();
                                hideDialog();

                                String str = strResult.trim();
                                Log.w("result", str);
                                if(!str.equals("no")){
                                    // มีข้อมูล
                                    Intent intent = new Intent(mContext, showdata_activity.class);
                                    intent.putExtra("jsonData", str);
                                    intent.putExtra("hexData", hexData);
                                    startActivity(intent);
                                    finish();
//
                                }
                                else{
                                    // ไม่มีข้อมูล
                                    Toast.makeText(mContext, "ไม่พบข้อมูลนักศึกษา", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(mContext, edit_activity.class);
                                    intent.putExtra("jsonData", str);
                                    intent.putExtra("hexData", hexData);
                                    intent.putExtra("from", "home");
                                    startActivity(intent);
                                    finish();

                                    Log.w("result", str);
                                }

                            }catch(Exception e){
                                e.printStackTrace();
                            }
//                            texter.setText(strResult);
//                            Log.w("result", strResult);

                        }
                    });
                }
        });

    }

    private void resolveIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage[] msgs;
            if (rawMsgs != null) {
                msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }
                Log.w("result", "if");
            } else {
                Log.w("result", "else unknow tag");
                // Unknown tag type
                byte[] empty = new byte[0];
                byte[] id = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
                Parcelable tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                byte[] payload = dumpTagData(tag).getBytes();
                NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN, empty, id, payload);
                NdefMessage msg = new NdefMessage(new NdefRecord[] { record });
                msgs = new NdefMessage[] { msg };

                Log.w("result",""+getStudentHex(msgs));
            }
            // Setup the views
//            buildTagViews(msgs);
            getData(getStudentHex(msgs));
        }
    }

    private String dumpTagData(Parcelable p) {
        StringBuilder sb = new StringBuilder();
        Tag tag = (Tag) p;
        byte[] id = tag.getId();
        sb.append("Tag ID (hex): ").append(getHex(id)).append("\n");
        return sb.toString();
    }

    private String getHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = bytes.length - 1; i >= 0; --i) {
            int b = bytes[i] & 0xff;
            if (b < 0x10)
                sb.append('0');
            sb.append(Integer.toHexString(b));
            if (i > 0) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }

    private String getStudentHex(NdefMessage[] msgs){
        if (msgs == null || msgs.length == 0) {
            return "";
        }

        NdefRecord[] all_record = msgs[0].getRecords();
        String hex_s = new String(all_record[0].getPayload());
        Log.w("record","h = "+hex_s);
        String hex_cut = hex_s.split(": ")[1];
        String hex_array[] = hex_cut.split(" ");
        String hex = hex_array[0]+hex_array[1]+hex_array[2]+hex_array[3];
        Log.w("record", "hexLine = " + hex);
        String hex_line = hex.substring(0,8);
        Log.w("record", "hexLine = " + hex_line);

        return hex_line;

    }

    @Override
    public void onNewIntent(Intent intent) {
//        setIntent(intent);
        Log.w("result","onNewIntent");
        resolveIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        Toast.makeText(home_activity.this, "resume "+(++cc)+" "+count+" "+netInfo, Toast.LENGTH_SHORT).show();
        if (mAdapter != null) {
            if (!mAdapter.isEnabled()) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("NFC ของคุณปิดอยู่");
                builder.create().setCanceledOnTouchOutside(false);
                builder.setCancelable(false);
                builder.setPositiveButton("เปิด", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                        builder.create().dismiss();
                        startActivity(intent);
                        count = 0;
                    }
                });

                builder.create().show();

            }
            mAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);
//            mAdapter.enableForegroundNdefPush(this, mNdefPushMessage);
        }
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        netInfo = cm.getActiveNetworkInfo();

        if(netInfo != null && netInfo.isConnectedOrConnecting()){
            count++;
        }
        else{
            if(count == 0 || netInfo == null){
                showSettingsDialog("ตั้งค่าการเชื่อมต่ออินเตอร์เน็ต");
            }
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (mAdapter != null) {
            mAdapter.disableForegroundDispatch(this);
//            mAdapter.disableForegroundNdefPush(this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);



        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_signout) {
            String mess = mPrefs.getString(KEY_USERNAME,"Not fund");

            final MaterialDialog mMaterialDialog = new MaterialDialog(this);

            mMaterialDialog
//                    .setMessage("ชื่อผู้ใช้ : "+mess+"\n         *------------------*\n      ยืนยันการออกจากระบบ?")
                    .setMessage("      ยืนยันการออกจากระบบ?")
                    .setPositiveButton("ตกลง", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mEditor.remove(KEY_USERNAME);
                            mEditor.commit();

                            Intent intent = new Intent(mContext, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    })
                    .setNegativeButton("ยกเลิก", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mMaterialDialog.dismiss();
                            onResume();
                        }
                    })
            .setCanceledOnTouchOutside(true);

            mMaterialDialog.show();
        }

        return super.onOptionsItemSelected(item);
    }
}
