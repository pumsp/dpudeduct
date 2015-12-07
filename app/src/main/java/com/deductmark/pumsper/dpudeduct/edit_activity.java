package com.deductmark.pumsper.dpudeduct;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.json.JSONObject;
import android.widget.*;

import com.pnikosis.materialishprogress.ProgressWheel;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;

public class edit_activity extends AppCompatActivity {

    private String jsonData;
    private String hexData;
    private String from;

    private String newJsonData;

    //edit text
    private EditText ed_id;
    private EditText ed_fname;
    private EditText ed_lname;
    private EditText ed_fac;
    private EditText ed_dep;

    //button
    private Button btn_edt_cancle;
    private Button btn_edt_save;

    //Context
    private Context mContext;

    //progress dialog
    private ProgressWheel wheel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_scroll);

        //initial
        wheel = (ProgressWheel) findViewById(R.id.progress_center);
        mContext = this;
        ed_id = (EditText) findViewById(R.id.edt_id);
        ed_fname = (EditText) findViewById(R.id.edt_fname);
        ed_lname = (EditText) findViewById(R.id.edt_lname);
        ed_fac = (EditText) findViewById(R.id.edt_fac);
        ed_dep = (EditText) findViewById(R.id.edt_dep);
        btn_edt_cancle = (Button) findViewById(R.id.btn_edt_cancle);
        btn_edt_save = (Button) findViewById(R.id.btn_edt_save);

        jsonData = getIntent().getStringExtra("jsonData");
        hexData = getIntent().getStringExtra("hexData");
        from = getIntent().getStringExtra("from");


        Toolbar toolbar = (Toolbar)findViewById(R.id.tool_bar);

//        Toast.makeText(edit_activity.this, jsonData, Toast.LENGTH_SHORT).show();
        Log.w("result",hexData);
        try{
            if(!jsonData.equals("no")){

                toolbar.setTitle("แก้ไขรายละเอียดนักศึกษา");

                JSONObject json = new JSONObject(jsonData);
                String id = json.getString("id");
                String fname = json.getString("fname");
                String lname = json.getString("lname");
                String faculty = json.getString("fac");
                String department = json.getString("dep");
//                ed_id.requestFocus();
                ed_id.setText(id);
                ed_fname.setText(fname);
                ed_lname.setText(lname);
                ed_fac.setText(faculty);
                ed_dep.setText(department);

//                Log.w("result", id + ", " + fname + ", " + lname + ", " + faculty + ", " + department);
            }
            else{
                toolbar.setTitle("เพิ่มข้อมูลนักศึกษา");

                TextInputLayout tx = (TextInputLayout)findViewById(R.id.til1);
//                tx.getEditText().settext
//                tx.requestFocus();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        setSupportActionBar(toolbar);


        btn_edt_cancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (from.equals("edit")) {
                    Intent intent = new Intent(mContext, showdata_activity.class);
                    intent.putExtra("jsonData", jsonData);
                    intent.putExtra("hexData", hexData);
                    startActivity(intent);
                    finish();
                } else if (from.equals("home")) {
                    Intent intent = new Intent(mContext, home_activity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });

        btn_edt_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String get_id = ed_id.getText().toString().trim();
                String get_fname = ed_fname.getText().toString().trim();
                String get_lname = ed_lname.getText().toString().trim();
                String get_fac = ed_fac.getText().toString().trim();
                String get_dep = ed_dep.getText().toString().trim();

                if(!TextUtils.isEmpty(get_id) && !TextUtils.isEmpty(get_fname)  &&!TextUtils.isEmpty(get_lname)
                        &&!TextUtils.isEmpty(get_fac) &&!TextUtils.isEmpty(get_dep) ) {
                    try{
                        JSONObject jo = new JSONObject();
                        jo.put("id",get_id);
                        jo.put("fname",get_fname);
                        jo.put("lname",get_lname);
                        jo.put("fac",get_fac);
                        jo.put("dep",get_dep);

                        newJsonData = jo.toString();


                    }catch(Exception e){
                        e.printStackTrace();

                    }
                    if(from.equals("home")) sendToDB(get_id, get_fname, get_lname, get_fac, get_dep,"insertData.php");
                    else sendToDB(get_id, get_fname, get_lname, get_fac, get_dep,"updateData.php");

                }
                else{
                    if(TextUtils.isEmpty(get_id) ){
                        Toast.makeText(edit_activity.this, "กรุณากรอกรหัสนักศึกษา", Toast.LENGTH_SHORT).show();
                    }
                    else if(TextUtils.isEmpty(get_fname) ){
                        Toast.makeText(edit_activity.this, "กรุณากรอกชื่อนักศึกษา", Toast.LENGTH_SHORT).show();
                    }
                    else if(TextUtils.isEmpty(get_lname) ){
                        Toast.makeText(edit_activity.this, "กรุณากรอกนามสกุลนักศึกษา", Toast.LENGTH_SHORT).show();
                    }
                    else if(TextUtils.isEmpty(get_fac) ){
                        Toast.makeText(edit_activity.this, "กรุณากรอกคณะ", Toast.LENGTH_SHORT).show();
                    }else if(TextUtils.isEmpty(get_dep) ){
                        Toast.makeText(edit_activity.this, "กรุณากรอกสาขา", Toast.LENGTH_SHORT).show();
                    }
                }


            }
        });




    }



    private void showDialog(){
        ed_id.clearFocus();
        ed_fname.clearFocus();
        ed_lname.clearFocus();
        ed_fac.clearFocus();
        ed_dep.clearFocus();

        ed_id.setEnabled(false);
        ed_fname.setEnabled(false);
        ed_lname.setEnabled(false);
        ed_fac.setEnabled(false);
        ed_dep.setEnabled(false);

        btn_edt_cancle.setClickable(false);
        btn_edt_save.setClickable(false);

        wheel.setVisibility(View.VISIBLE);

    }

    private void sendToDB(String id,String fname,String lname,String fac,String dep,String toPage){
        String hex = hexData;
        showDialog();

        OkHttpClient okHttpClient = new OkHttpClient();

        Request.Builder builder = new Request.Builder();
        RequestBody formBody = new FormEncodingBuilder()
                .add("hex", hex)
                .add("id", id)
                .add("fname", fname)
                .add("lname", lname)
                .add("fac", fac)
                .add("dep", dep)
                .build();

        String ip = "http://ceproject2.dpu.ac.th";
//        String ip = "http://192.168.1.38";
        Request request = builder.url(ip+"/sao/nfc_service/"+toPage)
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
//                        Toast.makeText(mContext, "insert "+strResult, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(mContext, showdata_activity.class);
                        intent.putExtra("jsonData", newJsonData);
                        intent.putExtra("hexData", hexData);
                        startActivity(intent);
                        finish();

                    }
                });
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit, menu);
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
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (from.equals("edit")) {
            Intent intent = new Intent(mContext, showdata_activity.class);
            intent.putExtra("jsonData", jsonData);
            intent.putExtra("hexData", hexData);
            startActivity(intent);
            finish();
        } else if (from.equals("home")) {
            Intent intent = new Intent(mContext, home_activity.class);
            startActivity(intent);
            finish();
        }
    }
}
