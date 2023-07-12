package com.example.location_requester1;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.CallLog;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.location_requester1.adapter.CallRecordsAdapter;
import com.example.location_requester1.models.CallsModel;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final String PHONE_NUMBER = "phoneNumber";
    private RecyclerView callsRecordRV;
    private AlertDialog alertDialog = null;
    private final int progressBarMax = 100;
    private final int progressBarDurationInSeconds = 15;
    private final int updateIntervalInMilliseconds = 15000 / progressBarMax;
    private ProgressBar progressBar;
    private boolean isSmsSent = false;
    List<CallsModel> dataModel;

    String selectedNumber = "";

    CheckBox handleSendingMessageAutomatically;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//getSupportActionBar().hide();
        setContentView(R.layout.activity_main);

        Toolbar titleBar = findViewById(R.id.titleBar);
        progressBar = findViewById(R.id.progressBar);
        handleSendingMessageAutomatically = findViewById(R.id.handleSendingMessageAutomatically);
        callsRecordRV = findViewById(R.id.callsRecordRV);
        callsRecordRV.setLayoutManager(new LinearLayoutManager(this));
        callsRecordRV.setHasFixedSize(true);

        setSupportActionBar(titleBar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            getCallDetails();
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    private void getCallDetails() {
        List<CallsModel> model = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        Cursor managedCursor = getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null, null);
        int number = managedCursor.getColumnIndex(CallLog.Calls.NUMBER);
        int type = managedCursor.getColumnIndex(CallLog.Calls.TYPE);
        int date = managedCursor.getColumnIndex(CallLog.Calls.DATE);
        int duration = managedCursor.getColumnIndex(CallLog.Calls.DURATION);
        int name = managedCursor.getColumnIndex(CallLog.Calls.CACHED_NAME);
        sb.append("Call Details :");
        int i = 0;
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

        while (managedCursor.moveToNext()) {
            String phNumber = managedCursor.getString(number);
            String callType = managedCursor.getString(type);
            String _name = managedCursor.getString(name);


            String callDate = managedCursor.getString(date);
            Date callDayTime = new Date(Long.parseLong(callDate));
            String formattedDate = sdf.format(callDayTime);

            String typeOfCall = null;
            int dircode = Integer.parseInt(callType);

            switch (dircode) {
                case CallLog.Calls.OUTGOING_TYPE:
                    typeOfCall = "OUTGOING";
                    break;

                case CallLog.Calls.INCOMING_TYPE:
                    typeOfCall = "INCOMING";
                    break;

                case CallLog.Calls.MISSED_TYPE:
                    typeOfCall = "MISSED";
                    break;
            }
            sb.append("name " + _name + "\nPhone Number:--- " + phNumber + " \nCall Type:--- " + typeOfCall + " \nCall Date:--- " + formattedDate);
            sb.append("\n----------------------------------");
            model.add(new CallsModel(_name, phNumber, formattedDate));

        }

        managedCursor.close();
        Log.d("callDetails", "getCallDetails: " + sb.toString());

//        Collections.reverse(model);
        if (model.size() > 0) {
            dataModel = new ArrayList<>();
            try {
                for (int x = 0; x < 5; x++) {
                    dataModel.add(model.get(x));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            CallRecordsAdapter adapter = new CallRecordsAdapter(this, dataModel, position -> {
                if (getPhoneNum() != null) {
                    selectedNumber = model.get(position).getPhoneNo();
                    sendSMS(selectedNumber);
                } else {
                    savePhone();
                }
            }

            );
            callsRecordRV.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        } else {
            Toast.makeText(this, "No Record Found", Toast.LENGTH_SHORT).show();
        }
        Log.d("Yesterday", "getCallDetails: " + model.size());
    }

    public void sendSMS(String phoneNo) {

        String m = "Click on the link below and please send your location: \n" + "https://api.whatsapp.com/send?phone=" + "+972" + getPhoneNum();
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, m, null, null);
            isSmsSent = true;
            new Handler().postDelayed(() -> {
                Toast.makeText(getApplicationContext(), "SMS Sent",
                        Toast.LENGTH_LONG).show();
                if (alertDialog == null) {
                    finishAffinity();
                }
            }, 1500);
        } catch (Exception ex) {
            Log.d("smsError", "sendSMS: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.saveNum) {
            savePhone();
        }
        return super.onOptionsItemSelected(item);
    }

    private void savePhone() {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        ViewGroup viewGroup = findViewById(android.R.id.content);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.customview, viewGroup, false);

        TextInputEditText edit_text = dialogView.findViewById(R.id.edit_text);
        Button save = dialogView.findViewById(R.id.save);

        builder.setView(dialogView);
        alertDialog = builder.create();
        alertDialog.show();

        save.setOnClickListener(v -> {
            setPhoneNum(edit_text.getText().toString());
            alertDialog.cancel();
            alertDialog.dismiss();
            alertDialog = null;
            MainActivity.this.onResume();
        });
    }

    private void setPhoneNum(String phoneNum) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.edit().putString(PHONE_NUMBER, phoneNum).apply();
    }

    private String getPhoneNum() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        return preferences.getString(PHONE_NUMBER, null);
    }

    @Override
    protected void onResume() {
        if (getPhoneNum() != null) {
            CountDownTimer countDownTimer = new CountDownTimer(15000L, updateIntervalInMilliseconds) {
                @Override
                public void onTick(long millisUntilFinished) {
                    int progress = (int) ((millisUntilFinished / 1000) * (progressBarMax / progressBarDurationInSeconds));
                    progressBar.setProgress(progress);
                }

                @Override
                public void onFinish() {
                    progressBar.setProgress(0);
                    if (!isSmsSent) {
                        if (handleSendingMessageAutomatically.isChecked()) {
                            sendSMS(dataModel.get(0).getPhoneNo());
                        }
                    }
                }
            };
            countDownTimer.start();
        } else {
            savePhone();
        }
        super.onResume();
    }
}