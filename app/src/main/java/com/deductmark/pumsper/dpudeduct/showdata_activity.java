package com.deductmark.pumsper.dpudeduct;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;
import android.widget.*;

import com.pnikosis.materialishprogress.ProgressWheel;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONObject;

import java.io.IOException;

import me.drakeet.materialdialog.MaterialDialog;

public class showdata_activity extends AppCompatActivity {
    private String jsonData;
    private String hexData;

    private EditText deduct_mark;
    private EditText deduct_couse;
    private Button deduct_btn;

    private Context mContext;
    private final String KEY_PREFS = "prefs_user";
    private final String KEY_USERNAME = "username";
    private SharedPreferences mPrefs;

    private String id,fname,lname,faculty,department;

    //Success Dialog
    private TextView suc_id,suc_name,suc_fac,suc_dep,suc_mark,suc_cause;
    private AlertDialog successDialog;
    private  MaterialDialog mMaterialDialog;

    //progress dialog
    private ProgressWheel wheel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showdata);

        mContext = this;
        mPrefs = mContext.getSharedPreferences(KEY_PREFS, Context.MODE_PRIVATE);



        TextView txt_id = (TextView) findViewById(R.id.txt_id);
        TextView txt_name = (TextView) findViewById(R.id.txt_name);
        TextView txt_fac = (TextView) findViewById(R.id.txt_fac);
        TextView txt_dep = (TextView) findViewById(R.id.txt_dep);

        Button btn_cancle = (Button)findViewById(R.id.btn_cancle);
//        btn_close = (Button)findViewById(R.id.deduct_close);
        Button btn_deduct = (Button)findViewById(R.id.btn_deduct);


//set up dialog ,text and button in dialog_deduct
        mMaterialDialog = new MaterialDialog(mContext);
        LayoutInflater inflater = getLayoutInflater();
        View dialoglayout = inflater.inflate(R.layout.dialog_deduct, null);

        mMaterialDialog
                .setView(dialoglayout)
                .setCanceledOnTouchOutside(true);

        ImageButton deduct_close_imb = (ImageButton) dialoglayout.findViewById(R.id.deduct_close_imb);
        deduct_mark = (EditText)dialoglayout.findViewById(R.id.deduct_mark);
        deduct_couse = (EditText)dialoglayout.findViewById(R.id.deduct_couse);
        deduct_btn = (Button)dialoglayout.findViewById(R.id.deduct_btn);
        //progreass dialog
        wheel = (ProgressWheel) dialoglayout.findViewById(R.id.progress_center);

//set up dialog ,text and button in dialog_deduct

        LayoutInflater suc_inflater = getLayoutInflater();
        View suc_dialoglayout = suc_inflater.inflate(R.layout.dialog_success, null);

        successDialog = new AlertDialog.Builder(mContext)
//                .setTitle("รายละเอียดการตัดคะแนน")
                .setView(suc_dialoglayout)
                .create();



        ImageButton suc_close = (ImageButton) suc_dialoglayout.findViewById(R.id.suc_close);
        Button suc_btn_close = (Button)suc_dialoglayout.findViewById(R.id.suc_btn_close);
        suc_id = (TextView)suc_dialoglayout.findViewById(R.id.suc_id);
        suc_name = (TextView)suc_dialoglayout.findViewById(R.id.suc_name);
        suc_fac = (TextView)suc_dialoglayout.findViewById(R.id.suc_fac);
        suc_dep = (TextView)suc_dialoglayout.findViewById(R.id.suc_dep);
        suc_mark = (TextView)suc_dialoglayout.findViewById(R.id.suc_mark);
        suc_cause = (TextView)suc_dialoglayout.findViewById(R.id.suc_cause);



//get data from intent
        jsonData = getIntent().getStringExtra("jsonData");
        hexData = getIntent().getStringExtra("hexData");

//        Toast.makeText(showdata_activity.this, hexData, Toast.LENGTH_SHORT).show();

        Toolbar toolbar = (Toolbar)findViewById(R.id.tool_bar);
        toolbar.setTitle("รายละเอียดนักศึกษา");
        setSupportActionBar(toolbar);



        try{
            JSONObject json = new JSONObject(jsonData);
            id = json.getString("id");
            fname = json.getString("fname");
            lname = json.getString("lname");
            faculty = json.getString("fac");
            department = json.getString("dep");

            txt_id.setText(id);
            txt_name.setText(fname+" "+lname);
            txt_fac.setText(faculty);
            txt_dep.setText(department);

            Log.w("result", id + ", " + fname + ", " + lname + ", " + faculty + ", " + department);
        }catch (Exception e){
            e.printStackTrace();
        }

        btn_cancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, home_activity.class);
                startActivity(intent);
                finish();
            }
        });



        deduct_close_imb.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mMaterialDialog.dismiss();
                    }
                }
        );
        btn_deduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                deduct_mark.requestFocus();
                mMaterialDialog.show();
            }
        });

        deduct_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String mark = deduct_mark.getText().toString().trim();
                String cause = deduct_couse.getText().toString().trim();

                if(!TextUtils.isEmpty(mark) && !TextUtils.isEmpty(cause)){
                    String name = fname+" "+lname;
                    String teacher = mPrefs.getString(KEY_USERNAME, "Not fund");
                    sendToDB(id, name, mark, cause, teacher);



                }
                else{
                    if(TextUtils.isEmpty(mark) ){
                        Toast.makeText(mContext, "กรุณาระบุคะแนนที่จะตัด", Toast.LENGTH_SHORT).show();
                    }
                    else if(TextUtils.isEmpty(cause) ){
                        Toast.makeText(mContext, "กรุณาระบุสาเหตุการตัดคะแนน", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });


//        success Dialog
        suc_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goHome();
            }
        });
        suc_btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goHome();

            }
        });

    }

    private void showDialog(){
        deduct_mark.setEnabled(false);
        deduct_couse.setEnabled(false);
        deduct_btn.setEnabled(false);
        wheel.setVisibility(View.VISIBLE);
    }
    private void hideDialog(){
        wheel.setVisibility(View.INVISIBLE);
    }

    private void goHome(){
        Intent intent = new Intent(mContext, home_activity.class);
        startActivity(intent);
        successDialog.dismiss();
        finish();
    }
    private void sendToDB(final String id,final String name,final String point,final String cause,final String teacher){
        try{
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }catch(Exception e){
            e.printStackTrace();
        }

        showDialog();

        OkHttpClient okHttpClient = new OkHttpClient();

        Request.Builder builder = new Request.Builder();
        RequestBody formBody = new FormEncodingBuilder()
                .add("no", id)
                .add("name", name)
                .add("point", point)
                .add("cause", cause)
                .add("fac",faculty)
                .add("dep",department)
                .add("teacher", teacher)
                .build();

        String ip = "http://ceproject2.dpu.ac.th";
//        String ip = "http://192.168.1.38";
        Request request = builder.url(ip+"/sao/pages/probation/b_addpro.php")
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
//                        Intent intent = new Intent(mContext, home_activity.class);
//                        startActivity(intent);
//                        finish();
                        Log.w("result", id + "," + name + "," + point + "," + cause + "," + faculty + "," + department);
                        suc_id.setText(id);
                        suc_name.setText(name);
                        suc_mark.setText(point);
                        suc_cause.setText("            "+cause);
                        suc_fac.setText(faculty);
                        suc_dep.setText(department);

                        hideDialog();
                        mMaterialDialog.dismiss();
                        successDialog.show();

                    }
                });
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_showdata, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_edit) {
            Intent intent = new Intent(mContext, edit_activity.class);
            intent.putExtra("jsonData", jsonData);
            intent.putExtra("hexData", hexData);
            intent.putExtra("from", "edit");
            startActivity(intent);
            finish();
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(mContext, home_activity.class);
        startActivity(intent);
        finish();
    }
}
