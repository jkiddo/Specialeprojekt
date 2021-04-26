package com.au.assure;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.au.assure.datapackages.EcgData;
import com.au.assure.datatypes.SensorMode;
import com.au.assure.pantompkins.OSEAFactory;
import com.au.assure.pantompkins.detection.QRSDetector2;

import java.util.ArrayList;
import java.util.List;

import static com.au.assure.AppNotifications.CHANNEL1_ID;
import static com.au.assure.AppNotifications.CHANNEL2_ID;
import static com.au.assure.AppNotifications.CHANNEL3_ID;

public class ForegroundService extends Service
        implements ConnectionManager.OnConnectionManagerListener, ConnectionManager.EcgDataListener, ConnectionManager.BatteryDataListener {

    public boolean CONNECTED = false;

    private static final String TAG = "ForegroundService";
    private BluetoothAdapter bluetoothAdapter;
    private ConnectionManager m_ConnectionManager = null;
    private double defaultModCSI;
    private double defaultCSI;
    private double ModCSIThresh;
    private double CSIThresh;
    private boolean logBattery = false;
    private boolean logRawECG = false;
    private boolean logRRintervals = false;
    private boolean logSeizureVals = false;
    private boolean logSeizure = true;
    private boolean logThresholdChanges = false;
    private ArrayList<CortriumC3> m_al_C3Devices;
    private ArrayList<String> m_al_C3Names;
    private String m_strReconnect_DeviceName = null;
    private boolean bResult = false;
    private int sampleCounter;
    private int sampleRate;
    private List<Integer> rPeakBuffer;
    private List<Double> rrIntervals;
    private double observedModCSI;
    private double observedCSI;
    private int rrSinceSeizure;
    private double maxModCSI;
    private double maxCSI;

    private SeizureDetector seizureDetector;
    private QRSDetector2 qrsDetector;
    private Recorder recorder;

    private NotificationManagerCompat notificationManager;

    // Binder given to clients
    private final IBinder binder = new LocalBinder();

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        ForegroundService getService() {
            // Return this instance of LocalService so clients can call public methods
            return ForegroundService.this;
        }

        void setCallback(uiCallback uiCallback) {
            callback = uiCallback;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    private uiCallback callback;


    @Override
    public void onCreate() {
        super.onCreate();

        sampleCounter = 0;
        sampleRate = 256;

        rPeakBuffer = new ArrayList<>();
        m_al_C3Devices = new ArrayList<>();
        m_al_C3Names = new ArrayList<>();

        rrSinceSeizure = 100;

        maxModCSI = 0;
        maxCSI = 0;

        seizureDetector = new SeizureDetector();

        defaultModCSI = Double.parseDouble(getString(R.string.defaultModCSI));
        defaultCSI = Double.parseDouble(getString(R.string.defaultCSI));

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        recorder = new Recorder();

        // Initialize qrsDetector
        qrsDetector = OSEAFactory.createQRSDetector2(sampleRate);

        notificationManager = NotificationManagerCompat.from(this);

        //Broadcasts when bond state changes (ie:pairing)
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(broadcastReceiverBT, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (broadcastReceiverBT != null)
            unregisterReceiver(broadcastReceiverBT);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Load thresholds
        ModCSIThresh = intent.getDoubleExtra("MODCSITHRESH", defaultModCSI);
        CSIThresh = intent.getDoubleExtra("CSITHRESH", defaultCSI);

        // Load log settings
        logBattery = intent.getBooleanExtra("LOGBATTERY", logBattery);
        logRawECG = intent.getBooleanExtra("LOGRAWECG", logRawECG);
        logRRintervals = intent.getBooleanExtra("LOGRRINTERVALS", logRRintervals);
        logSeizureVals = intent.getBooleanExtra("LOGSEIZUREVALS", logSeizureVals);
        logSeizure = intent.getBooleanExtra("LOGSEIZURE", logSeizure);
        logThresholdChanges = intent.getBooleanExtra("LOGTHRESH", logThresholdChanges);

        // Create the notification for the foreground service
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL1_ID)
                .setContentTitle("Detection is active - tap to open app")
                .setContentText("You can rest assured, ASSURE is looking out for you 🤓")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);

        return START_NOT_STICKY;
    }

    private void sendNotificationDisconnect() {
        Notification notification = new NotificationCompat.Builder(this, CHANNEL2_ID)
                .setSmallIcon(R.drawable.ic_exclamation_triangle_solid)
                .setContentTitle("Disconnected")
                .setContentText("Disconnected from device")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_SYSTEM)
                .build();
        notificationManager.notify(2, notification);
    }

    private void sendNotificationSeizure() {
        // If logging is enable by user, log the time of the seizure
        if (logSeizure) {
            recorder.saveSeizures();
        }

        // This intent makes it possible to open the app by pressing the notification
        Intent activityIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this,
                0, activityIntent, 0);

        // This intent is used for when the user presses YES to the seizure notification
        Intent posIntent = new Intent(this, SeizureReceiverPositive.class);
        PendingIntent actionPosIntent = PendingIntent.getBroadcast(this,
                0, posIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // This intent is used for when the user presses NO to the seizure notificaiton
        Intent negIntent = new Intent(this, SeizureReceiverNegative.class);
        PendingIntent actionNegIntent = PendingIntent.getBroadcast(this,
                0, negIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL3_ID)
                .setSmallIcon(R.drawable.ic_exclamation_triangle_solid)
                .setContentTitle(getString(R.string.seizureNotificationTitle))
                .setContentText(getString(R.string.seizureNotificationDesc))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_SYSTEM)
                .setColor(Color.RED)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(contentIntent)
                .addAction(R.mipmap.ic_launcher, "YES", actionPosIntent)
                .addAction(R.mipmap.ic_launcher, "NO", actionNegIntent)
                .build();
        notificationManager.notify(3, notification);
    }

    /**
     * Broadcast Receiver that detects bond state changes (Pairing status changes)
     */
    private final BroadcastReceiver broadcastReceiverBT = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //3 cases:
                //case1: bonded already
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDED.");
                }
                //case2: creating a bone
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDING) {
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDING.");
                }
                //case3: breaking a bond
                if (mDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                    Log.d(TAG, "BroadcastReceiver: BOND_NONE.");
                }
            }
        }
    };

    public void StartConnectionManager() {
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }

        m_ConnectionManager = ConnectionManager.getInstance(this);
        m_ConnectionManager.setConnectionManagerListener(this);
        m_ConnectionManager.startScanning();

    }

    protected void ConnectToDevice(int i) {
        m_ConnectionManager.stopScanning();

        //first cancel discovery because its very memory intensive.
        bluetoothAdapter.cancelDiscovery();

        // JKN new 4/8-2019
        Log.d(TAG, "onItemClick: You Clicked on a device.");
        String deviceName = m_al_C3Devices.get(i).getName();
        String deviceAddress = m_al_C3Devices.get(i).getAddress();

        Log.d(TAG, "onItemClick: deviceName = " + deviceName);
        Log.d(TAG, "onItemClick: deviceAddress = " + deviceAddress);

        bResult = m_ConnectionManager.connect(m_al_C3Devices.get(i));
        Toast.makeText(this, "bResult = " + (bResult ? "true" : "false"), Toast.LENGTH_SHORT).show();

        if (bResult && callback != null) {
            callback.onConnected();
        }

//        tvStatus.setText(getString(R.string.statusConnected));
    }

    @Override
    public void startedScanning(ConnectionManager manager) {
        Log.d(TAG, "public void startedScanning(ConnectionManager manager)");
    }

    @Override
    public void stoppedScanning(ConnectionManager manager) {
        Log.d(TAG, "public void stoppedScanning(ConnectionManager manager)");

        if (m_strReconnect_DeviceName != null)// If reconnect not in progress
            StartConnectionManager();
    }

    @Override
    public void discoveredDevice(CortriumC3 device) {
        Log.d(TAG, "public void discoveredDevice(CortriumC3 device)");

        String strDeviceName = device.getName();

        if (strDeviceName.startsWith("C3") || strDeviceName.startsWith("B17")) {
            m_al_C3Devices.add(device);
            m_al_C3Names.add(device.getName());

            Log.d(TAG, "onReceive: " + device.getName() + ": " + device.getAddress());

            if (m_strReconnect_DeviceName == null) { // Startup. No device selected by user.

                if (callback != null) {
                    callback.onDeviceFound(device);
                }
            } else// Device disconnected, and app tries to reconnect.
            {
                if (m_strReconnect_DeviceName.equals(strDeviceName)) {
                    // Device found
                    m_strReconnect_DeviceName = null;// No more reconnect.
                    int iDevideIndex = m_al_C3Devices.size() - 1;
                    ConnectToDevice(iDevideIndex);
                }
            }
        }
    }

    @Override
    public void connectedToDevice(CortriumC3 device) {
        CONNECTED = true;

        m_ConnectionManager.setEcgDataListener(this);
        m_ConnectionManager.setBatteryDataListener(this);

//        if( true ) {
//            CortriumC3 c3Device = m_ConnectionManager.getConnectedDevice();
//            String strName = c3Device.getName();
//            Log.i("JKN","DeviceName = " + strName );
//        }
    }

    @Override
    public void disconnectedFromDevice(CortriumC3 device) {
        CONNECTED = false;
        sendNotificationDisconnect();
        if (callback != null) {
            callback.onDisconnect();
        }
        m_ConnectionManager.disconnect();
        m_ConnectionManager.ReleaseInstance();// Reset Bluetooth connection
        stopSelf();
        stopForeground(true);
    }

    @Override
    public void ecgDataUpdated(EcgData ecgData) {
        int[] batch = ecgData.getFilteredEcg2Samples();

        // If the raw data should be logged
        if (logRawECG) {
            recorder.saveRawECG(batch);
        }

        sampleCounter = sampleCounter + 12; // Determines the number of samples since last R-peak
        // It is useful for calculating the RR interval.
        boolean newRpeak = false;

        // Send the ECG data to R-peak analysis:
        for (int i = 0; i < batch.length; i++) {
            int result = qrsDetector.QRSDet(batch[i]);
            if (result != 0) { // If R-peak is detected
                rPeakBuffer.add(sampleCounter - 12 + i);
                sampleCounter = 0;
                newRpeak = true;
            }
        }

        // Making sure the buffer size is always 100 R-peaks
        // The seizure detection has a 7 RR interval median filter, so an additional 7 intervals
        // are needed
        if (newRpeak && rPeakBuffer.size() > 107) {
            rPeakBuffer.remove(0); // Remove first R-peak
        }

        // If the buffer is filled and new a new Rpeak is detected. This is where the
        // seizure detection algorithm is used.
        if (newRpeak && rPeakBuffer.size() > 106) {
            rrIntervals = new ArrayList<>();

            // Convert intervals in sample no. to intervals in seconds (RR intervals)
            for (int i = 0; i < rPeakBuffer.size() - 1; i++) {
                rrIntervals.add((double) rPeakBuffer.get(i) / sampleRate);
            }

            // Log the RR intervals if the user has chose to do so
            if (logRRintervals) {
                recorder.saveRRintervals(rrIntervals.get(rrIntervals.size() - 1));
            }

            // Calculate CSI and ModCSI
            double[] returnVals;
            returnVals = seizureDetector.CalcModCSI_and_CSI(rrIntervals);
            observedModCSI = returnVals[0];
            observedCSI = returnVals[1];

            // Check if the observed values are greater than the current thresholds
            if (observedModCSI > ModCSIThresh || observedCSI > CSIThresh) {
                if (rrSinceSeizure > 100) { // Makes sure same seizure is not detected multiple times
                    sendNotificationSeizure(); // Notify about seizure
                    rrSinceSeizure = 0;
                }
            }
            rrSinceSeizure++;

            // Log the values if the user has chosen to do so
            if (logSeizureVals) {
                recorder.saveModCSIandCSI(observedModCSI, observedCSI);
            }

            if (callback != null) {
                callback.onSeizureValsUpdate(observedModCSI, observedCSI);
            }

            // Check if this observation is larger than the currently largest observation
            if (observedModCSI > maxModCSI) {
                maxModCSI = observedModCSI;

                if (callback != null) {
                    callback.onMaxSeizureValsUpdate(maxModCSI, maxCSI);
                }
            }
            if (observedCSI > maxCSI) {
                maxCSI = observedCSI;

                if (callback != null) {
                    callback.onMaxSeizureValsUpdate(maxModCSI, maxCSI);
                }
            }
        }

        // If not enough data RR intervals yet, update the UI with how far along the buffer is
        if (newRpeak && rPeakBuffer.size() < 107) {

            double pct = (((double) rPeakBuffer.size()) / 107) * 100;
            String msg = getString(R.string.waiting) + String.format("%.1f", pct) + "%";

            if (callback != null) {
                callback.onBufferUpdate(msg);
            }
        }
    }

    @Override
    public void modeRead(SensorMode sensorMode) {

    }

    @Override
    public void deviceInformationRead(CortriumC3 device) {

    }

    @Override
    public void batteryPercentUpdated(float percent, float vBat) {
        // If the battery level should be logged
        if (logBattery) {
            recorder.saveBatteryInfo(percent, vBat, getApplicationContext());
        }

        if (callback != null) {
            callback.onBatteryUpdate(percent);
        }
    }

    public interface uiCallback {
        void onBufferUpdate(String msg);

        void onSeizureValsUpdate(double modCSI, double CSI);

        void onMaxSeizureValsUpdate(double maxModCSI, double CSI);

        void onBatteryUpdate(double percent);

        void onDeviceFound(CortriumC3 device);

        void onConnected();

        void onDisconnect();
    }

    public void resetMaxValues() {
        maxModCSI = 0;
        maxCSI = 0;
    }

    public void connectToDevice(int i) {
        ConnectToDevice(i);
    }

    public void updateThresholds(double modCSI, double CSI) {
        // Log the threshold change if it is enabled by the user
        if (logThresholdChanges) {
            // Log only if the values were actually changed
            if (CSIThresh != CSI || ModCSIThresh != modCSI) {
                recorder.saveThresholdChange(CSIThresh, ModCSIThresh, CSI, modCSI);
            }
        }

        ModCSIThresh = modCSI;
        CSIThresh = CSI;
    }

    public void updateLogSettings(boolean logBattery, boolean logRawECG, boolean logRRintervals,
                                  boolean logSeizureVals, boolean logSeizure, boolean logThresholdChanges) {
        this.logBattery = logBattery;
        this.logRawECG = logRawECG;
        this.logRRintervals = logRRintervals;
        this.logSeizureVals = logSeizureVals;
        this.logSeizure = logSeizure;
        this.logThresholdChanges = logThresholdChanges;
    }

    public void cancelDiscovery() {
        if (m_ConnectionManager != null) {
            if (m_ConnectionManager.getConnectionState() == ConnectionManager.ConnectionStates.Scanning)
                m_ConnectionManager.stopScanning();
            m_ConnectionManager.ReleaseInstance();// Reset Bluetooth connection
        }
        bluetoothAdapter.cancelDiscovery();
    }

    public void disconnect() {
        m_ConnectionManager.disconnect();
        m_ConnectionManager.ReleaseInstance();// Reset Bluetooth connection
    }
}
