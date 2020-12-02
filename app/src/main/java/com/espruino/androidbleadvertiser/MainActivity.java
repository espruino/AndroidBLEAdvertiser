package com.espruino.androidbleadvertiser;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    static String WORK_NAME = "updateBLEAdvertising";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        updateStatus();
    }

    /** Called when the user taps the Send button */
    public void fetchPage(View view) {
        EditText editURL = (EditText) findViewById(R.id.editURL);
        EditText editRegEx = (EditText) findViewById(R.id.editRegEx);
        String url = editURL.getText().toString();
        String regEx = editRegEx.getText().toString();

        Data inputData = new Data.Builder()
            .putString("URL", url)
            .putString("REGEX", regEx)
            .build();
        Constraints constraints = new Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            //.setRequiresCharging(true)
            .build();
        /*WorkRequest workRequest =
                new OneTimeWorkRequest.Builder(DownloadWorker.class)
                        .setConstraints(constraints)
                        .setInputData(inputData)
                        .build();*/

        // probably won't work - MIN_PERIODIC_INTERVAL_MILLIS=900000 -> 15 minutes
        PeriodicWorkRequest workRequest =
       new PeriodicWorkRequest.Builder(DownloadWorker.class, 1, TimeUnit.MINUTES)
           .setConstraints(constraints)
           .setInputData(inputData)
           .build();

        WorkManager workManager = WorkManager.getInstance(this);
        workManager.enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.REPLACE, workRequest);

        /*workManager.getWorkInfosForUniqueWorkLiveData(WORK_NAME)
                .observe(this, workInfo -> {
                    Log.i("MainActivity", "WorkInfo status changed");
                    updateStatus();
                });*/

        updateStatus();
    }

    public void cancelFetchPage(View view) {
        WorkManager
                .getInstance(this)
                .cancelUniqueWork(WORK_NAME);
        updateStatus();
    }

    public WorkInfo getCurrentWork() {
        try {
            List<WorkInfo> work = WorkManager
                    .getInstance(this)
                    .getWorkInfosForUniqueWork(WORK_NAME).get();
            //Log.i("WorkInfo", work.size()+ " work items");
            for (int i=0;i<work.size();i++) {
                //Log.i("WorkInfo", i + ":" + work.get(i).getState());
                if (!work.get(i).getState().isFinished())
                    return work.get(i);
            }
            return null;
        } catch (ExecutionException e) {
            return null;
        } catch (InterruptedException e) {
            return null;
        }
    }

    public boolean isWorkRunning() {
        return getCurrentWork() != null;
    }

    /*
    // We cannot get the work result here because it's for a periodic task. Instead
    // I guess we must either start a new single-shot task, or save the result somewhere
    public String getWorkResult() {
        WorkInfo work = getCurrentWork();
        if (work==null) return null;
        return work.getOutputData().getString("result");
    }*/

    public void updateStatus() {
        TextView textStatus = (TextView)findViewById(R.id.textStatus);
        if (isWorkRunning()) {
            textStatus.setText("Task is running." );
        } else {
            textStatus.setText("No task running");
        }

    }
 }