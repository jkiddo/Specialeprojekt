package com.au.assure;

import androidx.appcompat.app.AppCompatActivity;
import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.content.ContextCompat;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
        implements AdapterView.OnItemClickListener, DialogFragmentEditValues.NoticeDialogListener
        , DialogFragmentLogSettings.NoticeDialogListener, DialogFragmentConfirmDisconnect.DisconnectDialogListener {
    public ArrayList<BluetoothDevice> btDevices = new ArrayList<>();
    public DeviceListAdapter deviceListAdapter;

    private static final String TAG = "MainActivity";

    Button discoverBtn;

    int REQUEST_ENABLE_BT;
    ListView lvNewDevices;
    TextView tvStatus;
    TextView tvModCSI;
    TextView tvCSI;
    TextView tvLatestModCSI;
    TextView tvLatestCSI;
    TextView tvTimestamp;
    BluetoothAdapter bluetoothAdapter;
    double ModCSIThresh;
    double CSIThresh;
    double defaultModCSI;
    double defaultCSI;
    View listItem;
    boolean logBattery = false;
    boolean logRawECG = false;
    boolean logRRintervals = false;
    boolean logSeizureVals = false;
    boolean logSeizure = true;
    boolean logThresholdChanges = false;
    TextView tvMaxTimestamp;
    TextView tvMaxModCSI;
    TextView tvMaxCSI;
    TextView tvBatteryLevel;
    double maxModCSI;
    double maxCSI;

    Intent serviceIntent;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize variables
        btDevices = new ArrayList<>();
        tvStatus = findViewById(R.id.lbStatus);
        lvNewDevices = findViewById(R.id.lvNewDevices);
        lvNewDevices.setOnItemClickListener(MainActivity.this);
        tvModCSI = findViewById(R.id.tvModCSI);
        tvCSI = findViewById(R.id.tvCSI);
        tvLatestModCSI = findViewById(R.id.latestModCSI);
        tvLatestCSI = findViewById(R.id.latestCSI);
        tvTimestamp = findViewById(R.id.latestUpdateTime);
        tvMaxTimestamp = findViewById(R.id.maxUpdateTime);
        tvMaxModCSI = findViewById(R.id.maxModCSI);
        tvMaxCSI = findViewById(R.id.maxCSI);
        tvBatteryLevel = findViewById(R.id.tvBatteryLevel);
        maxModCSI = 0;
        maxCSI = 0;
        discoverBtn = findViewById(R.id.btnDiscover);

        // Read stored thresholds
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        defaultModCSI = Double.parseDouble(getString(R.string.defaultModCSI));
        defaultCSI = Double.parseDouble(getString(R.string.defaultCSI));
        ModCSIThresh = sharedPref.getFloat("savedModCSI", (float) defaultModCSI);
        CSIThresh = sharedPref.getFloat("savedCSI", (float) defaultCSI);
        logBattery = sharedPref.getBoolean("logBattery", logBattery);
        logRawECG = sharedPref.getBoolean("logRawECG", logRawECG);
        logRRintervals = sharedPref.getBoolean("logRRintervals", logRRintervals);
        logSeizureVals = sharedPref.getBoolean("logSeizureVals", logSeizureVals);
        logSeizure = sharedPref.getBoolean("logSeizure", logSeizure);
        logThresholdChanges = sharedPref.getBoolean("logThresh", logThresholdChanges);

        // Display stored thresholds
        tvModCSI.setText(Double.toString(ModCSIThresh));
        tvCSI.setText(Double.toString(CSIThresh));

        deviceListAdapter = new DeviceListAdapter(this, R.layout.device_list_adapter, btDevices);
        lvNewDevices.setAdapter(deviceListAdapter);

        checkBTPermissions();

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            // Bluetooth not supported on device
            Toast toast = Toast.makeText(getApplicationContext(), R.string.btNotSupported, Toast.LENGTH_LONG);
            toast.show();
        }

        // Ask user to enable bluetooth
        if (!bluetoothAdapter.isEnabled()) {
            // Enable bluetooth
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        serviceIntent = new Intent(this, ForegroundService.class);
        serviceIntent.putExtra("MODCSITHRESH", ModCSIThresh);
        serviceIntent.putExtra("CSITHRESH", CSIThresh);
        serviceIntent.putExtra("LOGBATTERY", logBattery);
        serviceIntent.putExtra("LOGRAWECG", logRawECG);
        serviceIntent.putExtra("LOGRRINTERVALS", logRRintervals);
        serviceIntent.putExtra("LOGSEIZUREVALS", logSeizureVals);
        serviceIntent.putExtra("LOGSEIZURE", logSeizure);
        serviceIntent.putExtra("LOGTHRESH", logThresholdChanges);
    }

    ForegroundService mService;
    boolean mBound = false;

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to ForegroundService
        Intent intent = new Intent(this, ForegroundService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(connection);
        mBound = false;
        if (state == State.DISCOVERING) {
            mService.cancelDiscovery();
            SetState(State.NOTCONNECTED);
        }
    }

    public enum State {
        NOTCONNECTED,
        CONNECTED,
        DISCOVERING
    }

    public State state = State.NOTCONNECTED;

    public void SetState(State newState) {
        switch (newState) {
            case NOTCONNECTED:
                state = State.NOTCONNECTED;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        discoverBtn.setText("Discover devices");
                        tvStatus.setText(R.string.status);
                        if (listItem != null) {
                            listItem.setBackgroundColor(Color.WHITE);
                        }
                        btDevices.clear();
                        deviceListAdapter.notifyDataSetChanged();
                    }
                });
                break;
            case DISCOVERING:
                state = State.DISCOVERING;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        discoverBtn.setText("Cancel discovery");
                        tvStatus.setText(R.string.statusScanning);
                    }
                });
                break;
            case CONNECTED:
                state = State.CONNECTED;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        discoverBtn.setText("Disconnect");
                        tvStatus.setText(R.string.statusConnected);
                    }
                });
                break;
        }
    }

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            ForegroundService.LocalBinder binder = (ForegroundService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;

            if (mService.CONNECTED) {
                SetState(State.CONNECTED);
            } else if (!mService.CONNECTED) {
                SetState(State.NOTCONNECTED);
            }

            binder.setCallback(new ForegroundService.uiCallback() {
                @Override
                public void onBufferUpdate(String msg) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvLatestModCSI.setText(msg);
                            tvLatestCSI.setText(msg);

                            // Update timestamp
                            Calendar cal = Calendar.getInstance();
                            Date currentLocalTime = cal.getTime();
                            DateFormat date = new SimpleDateFormat("HH:mm - dd.MM.yy");
                            String currentTime = date.format(currentLocalTime);
                            tvTimestamp.setText(currentTime);
                        }
                    });
                }

                @Override
                public void onSeizureValsUpdate(double modCSI, double CSI) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvLatestModCSI.setText(String.format("%.2f",modCSI));
                            tvLatestCSI.setText(String.format("%.2f",CSI));

                            // Update timestamp
                            Calendar cal = Calendar.getInstance();
                            Date currentLocalTime = cal.getTime();
                            DateFormat date = new SimpleDateFormat("HH:mm - dd.MM.yy");
                            String currentTime = date.format(currentLocalTime);
                            tvTimestamp.setText(currentTime);
                        }
                    });
                }

                @Override
                public void onMaxSeizureValsUpdate(double maxModCSI, double maxCSI) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvMaxModCSI.setText(String.format("%.2f",maxModCSI));
                            tvMaxCSI.setText(String.format("%.2f",maxCSI));

                            // Update timestamp
                            Calendar cal = Calendar.getInstance();
                            Date currentLocalTime = cal.getTime();
                            DateFormat date = new SimpleDateFormat("HH:mm - dd.MM.yy");
                            String currentTime = date.format(currentLocalTime);
                            tvMaxTimestamp.setText(currentTime);
                        }
                    });
                }

                @Override
                public void onBatteryUpdate(double percent) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvBatteryLevel.setText(String.format("%.2f",percent));
                        }
                    });
                }

                @Override
                public void onDeviceFound(CortriumC3 device) {
                    // Add new device and update the listview
                    btDevices.add(device.getBluetoothDevice());
                    deviceListAdapter.notifyDataSetChanged();
                }

                @Override
                public void onConnected() {
                    StartForeground();
                    SetState(State.CONNECTED);
                }

                @Override
                public void onDisconnect() {
                    SetState(State.NOTCONNECTED);
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    private void StartForeground() {
        // Promote service to foreground, when a device is connected.
        ContextCompat.startForegroundService(this, serviceIntent);
    }

    public void btnDiscover(View view) {
        if (state == State.CONNECTED) {
            // We display a dialog instead of just disconnecting right away:
            DialogFragmentConfirmDisconnect dialog = new DialogFragmentConfirmDisconnect();
            dialog.show(getSupportFragmentManager(), "DialogFragmentConfirmDisconnect");
        } else if (state == State.NOTCONNECTED) {
            mService.StartConnectionManager();
            SetState(State.DISCOVERING);
        } else if (state == State.DISCOVERING) {
            mService.cancelDiscovery();
            SetState(State.NOTCONNECTED);
        }
    }

    private void checkBTPermissions() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.WRITE_EXTERNAL_STORAGE");
            permissionCheck += this.checkSelfPermission("Manifest.permission.READ_EXTERNAL_STORAGE");
            if (permissionCheck != 0) {
                // You can only request permissions once, so request ALL in one go.
                this.requestPermissions(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                }, 1001); //Any number
            }
        } else {
            Log.d(TAG, "checkBTPermissions: No need to check permissions. SDK version <= LOLLIPOP.");
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        // Selecting bluetooth device from list
        if (mBound) {
            mService.connectToDevice(i);
            view.setBackgroundColor(getColor(R.color.themeGreen));
        }
        listItem = view;
    }

    public void btnEditValues(View view) {
        // Create an instance of the dialog fragment and show it
        DialogFragmentEditValues editValues = new DialogFragmentEditValues(ModCSIThresh, CSIThresh);
        editValues.show(getSupportFragmentManager(), "editValues");
    }

    // Dialog for changing thresholds
    @Override
    public void onDialogPositiveClick(double modCSI, double CSI) { // User touched the dialog's positive button

        if (mBound)
            mService.updateThresholds(modCSI, CSI);

        // Set the new values
        ModCSIThresh = modCSI;
        CSIThresh = CSI;

        // Display the new values
        tvModCSI.setText(Double.toString(ModCSIThresh));
        tvCSI.setText(Double.toString(CSIThresh));

        // Store the new values for next time the app is opened
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putFloat("savedModCSI", (float) ModCSIThresh);
        editor.putFloat("savedCSI", (float) CSIThresh);
        editor.apply();
    }

    // Dialog for disconnecting
    @Override
    public void onDialogPositiveClick() {
        mService.disconnect();
        mService.stopSelf();
        mService.stopForeground(true);
        SetState(State.NOTCONNECTED);
    }

    @Override
    public void onDialogNegativeClick() { // User touched the dialog's negative button
        // Cancelled - nothing happens
    }

    public void btnLogSettings(View view) {
        // Create an instance of the dialog fragment and show it
        DialogFragmentLogSettings logSettings = new DialogFragmentLogSettings(logBattery, logRawECG, logRRintervals,
                logSeizureVals, logSeizure, logThresholdChanges);
        logSettings.show(getSupportFragmentManager(), "logSettings");
    }

    // Dialog for changing log settings
    @Override
    public void onDialogPositiveClick(boolean logBattery, boolean logRawECG, boolean logRRintervals,
                                      boolean logSeizureVals, boolean logSeizure, boolean logThresholdChanges) {
        this.logBattery = logBattery;
        this.logRawECG = logRawECG;
        this.logRRintervals = logRRintervals;
        this.logSeizureVals = logSeizureVals;
        this.logSeizure = logSeizure;
        this.logThresholdChanges = logThresholdChanges;

        if (mBound)
            mService.updateLogSettings(logBattery,logRawECG,logRRintervals,logSeizureVals,logSeizure,logThresholdChanges);

        // Store the new values for next time the app is opened
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("logBattery", logBattery);
        editor.putBoolean("logRawECG", logRawECG);
        editor.putBoolean("logRRintervals", logRRintervals);
        editor.putBoolean("logSeizureVals", logSeizureVals);
        editor.putBoolean("logSeizure", logSeizure);
        editor.putBoolean("logThresh", logThresholdChanges);
        editor.apply();
    }

    public void btnResetValues(View view) {
        if (mBound)
            mService.resetMaxValues();
    }

    boolean doubleBackToExitPressedOnce = false;

    // Make sure to warn the user before using the back button to exit the app
    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, R.string.pressBackAgainToExit, Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }
}