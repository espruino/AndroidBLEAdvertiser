package com.espruino.androidbleadvertiser;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DownloadWorker extends Worker {

    Context mContext;
    AdvertiseCallback mAdvertiseCallback;

    public DownloadWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        mContext = context;
    }

    private class SampleAdvertiseCallback extends AdvertiseCallback {
        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);
            Log.d("DownloadWorker", "Advertising failed");
        }
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
            Log.d("DownloadWorker", "Advertising successfully started");
        }
    }

    @NonNull
    @Override
    public Result doWork() {
        String url = getInputData().getString("URL");
        String regEx = getInputData().getString("REGEX");
        Log.i("DownloadWorker","Downloading URL " + url);

        String contents = "";
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader((new URL(url)).openStream()));
            String str;
            while ((str = in.readLine()) != null) {
                contents += str;
            }
            in.close();
        } catch (MalformedURLException e) {
        } catch (IOException e) {
        }
        Log.i("DownloadWorker","Download complete, scanning with " + regEx);
        Log.i("DownloadWorker",contents);
        Pattern pattern = Pattern.compile(regEx);
        Matcher matcher = pattern.matcher(contents);
        String foundData = "";
        if (matcher.find()) {
            Log.i("DownloadWorker","Start index: " + matcher.start());
            Log.i("DownloadWorker"," End index: " + matcher.end() + " ");
            Log.i("DownloadWorker", matcher.group());
            Log.i("DownloadWorker", matcher.group(1));
            foundData = matcher.group(1);
        }

        BluetoothManager mBluetoothManager = (BluetoothManager)mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter mBluetoothAdapter = mBluetoothManager.getAdapter();
        BluetoothLeAdvertiser mBluetoothLeAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();

        AdvertiseSettings.Builder settingsBuilder = new AdvertiseSettings.Builder();
        settingsBuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED); // was ADVERTISE_MODE_LOW_POWER
        settingsBuilder.setTimeout(0); // always!
        //setTxPowerLevel(ADVERTISE_TX_POWER_HIGH.)
        AdvertiseSettings settings = settingsBuilder.build();

        AdvertiseData.Builder dataBuilder = new AdvertiseData.Builder();
        dataBuilder.addManufacturerData(0x0590, foundData.getBytes());
        //dataBuilder.setIncludeDeviceName(true);
        AdvertiseData data = dataBuilder.build();

        mAdvertiseCallback = new SampleAdvertiseCallback();

        mBluetoothLeAdvertiser.stopAdvertising(mAdvertiseCallback);
        mBluetoothLeAdvertiser.startAdvertising(settings, data,
                mAdvertiseCallback);

        // Indicate whether the work finished successfully with the Result
        Data output = new Data.Builder()
                .putString("result", foundData)
                .build();
        return Result.success(output);
    }

    /*

setInterval(function() {
  NRF.requestDevice({ filters: [{ manufacturerData:{0x0590:{}} }] }).then(function(dev) {
    print(E.toString(dev.manufacturerData));
  });
},10000)


     */
}
