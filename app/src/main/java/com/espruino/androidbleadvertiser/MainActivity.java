package com.espruino.androidbleadvertiser;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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

        PeriodicWorkRequest workRequest =
       new PeriodicWorkRequest.Builder(DownloadWorker.class, 1, TimeUnit.MINUTES)
           .setConstraints(constraints)
           .setInputData(inputData)
           .build();

        WorkManager
                .getInstance(this)
                .enqueue(workRequest);

    }
 }