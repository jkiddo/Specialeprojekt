package com.au.assure;

//package com.cortrium.opkit;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import com.au.assure.datapackages.EcgData;
import com.au.assure.datatypes.SensorMode;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by hsk on 11/10/16.
 */

public final class ConnectionManager {
    private final static String TAG = "ContriumC3Comms";

    public final static String ACTION_GATT_CONNECTED             = "com.docobo.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED          = "com.docobo.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_DEVICE_MODE_UPDATED        = "com.docobo.ACTION_DEVICE_MODE_UPDATED";
    public final static String EXTRA_VALUE = "extra_value";

    private String mBluetoothDeviceAddress;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private boolean bluetoothIsSupported = true;
    private boolean bluetoothIsEnabled = true;
    private ConnectionStates connectionState = ConnectionStates.Unknown;
    private CortriumC3 mCortriumC3Device;
    private Context mContext;

    private ArrayList discoveredDevices = new ArrayList<>();

    private ScanCallback mLeScanCallback21 = null;
    private BluetoothAdapter.LeScanCallback mLeScanCallback19 = null;

    public enum ConnectionStates
    {
        Unknown,
        Disconnected,
        Scanning,
        Connecting,
        Connected
    }

    protected ConnectionManager(Context context) {
        mContext = context;

        if(isSDK21()){
            mLeScanCallback21 = new ScanCallbackAPI21();
        }else{
            // Device scan callback.
            mLeScanCallback19 = new BluetoothAdapter.LeScanCallback()
            {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord)
                {
                    if (!discoveredDevices.contains(device.getAddress()))
                    {
                        discoveredDevices.add(device.getAddress());

                        if (listener != null)
                        {
                            listener.discoveredDevice(new CortriumC3(device));
                        }
                    }
                }
            };
        }

        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            bluetoothIsSupported = false;
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager = (BluetoothManager)context.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            bluetoothIsSupported = false;
        }
        else
        {
            bluetoothIsEnabled = mBluetoothAdapter.isEnabled();
        }
    }

    private static ConnectionManager instance = null;
    public static ConnectionManager getInstance(Context context) {
        if(instance == null) {
            instance = new ConnectionManager(context);
        }
        return instance;
    }
    public void ReleaseInstance()
    {
        stopScanning();

        instance = null;
    }

    private final ServiceManager mGattCallback = new ServiceManager(this)
    {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState)
        {
            if (newState == BluetoothProfile.STATE_CONNECTED)
            {
                mGattCallback.updateBluetoothInfo(mBluetoothAdapter, mBluetoothGatt);
                mGattCallback.setContext(mContext);

                mCortriumC3Device.registerServiceManager(mGattCallback);

                connectionState = ConnectionStates.Connected;
                Log.i(TAG, "Connected to GATT server.");

                if (listener != null)
                {
                    listener.connectedToDevice(mCortriumC3Device);
                }

                final Intent intent = new Intent(ACTION_GATT_CONNECTED);
                mContext.sendBroadcast(intent);

                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery");

                mGattCallback.discoverServicesForDevice(mCortriumC3Device);
            }
            else if (newState == BluetoothProfile.STATE_DISCONNECTED)
            {
                close();
                connectionState = ConnectionStates.Disconnected;

                mGattCallback.deviceDisconnected();

                if (listener != null)
                {
                    listener.disconnectedFromDevice(mCortriumC3Device);
                }

                final Intent intent = new Intent(ACTION_GATT_DISCONNECTED);
                mContext.sendBroadcast(intent);

                Log.i(TAG, "Disconnected from GATT server.");
            }
        }
    };

    public ConnectionStates getConnectionState()
    {
        switch( connectionState ) {
            case Unknown: Log.i("JKN","Unknown");break;
            case Disconnected: Log.i("JKN","Disconnected");break;
            case Scanning: Log.i("JKN","Scanning");break;
            case Connecting: Log.i("JKN","Connecting");break;
            case Connected:
                Log.i("JKN","Connected");
                break;
        }
        return connectionState;
    }

    public CortriumC3 getConnectedDevice()
    {
        return mCortriumC3Device;
    }

    public boolean isCortriumDeviceSupported()
    {
        return bluetoothIsSupported;
    }

    public boolean getBluetoothIsEnabled()
    {
        return bluetoothIsEnabled;
    }

    public void startScanning(){
        if(isSDK21()){
            startScanningSDK21();
        }else{
            startScanningSDK19();
        }
    }

    @TargetApi(19)
    public void startScanningSDK19()
    {
        mBluetoothAdapter.startLeScan(new UUID[] { GattAttributes.CONTRIUM_C3_DATA_SERVICE }, mLeScanCallback19);

        connectionState = ConnectionStates.Scanning;
        if (this.listener != null)
        {
            this.listener.startedScanning(this);
        }
    }

    @TargetApi(21)
    public void startScanningSDK21()
    {
        //final BluetoothLeScanner bluetoothLeScanner = ((BluetoothManager)mContext.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter().getBluetoothLeScanner();
        final BluetoothLeScanner bluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        bluetoothLeScanner.startScan(mLeScanCallback21);

        connectionState = ConnectionStates.Scanning;
        if (this.listener != null)
        {
            this.listener.startedScanning(this);
        }
    }

    public void stopScanning(){
        if(isSDK21()){
            stopScanningSDK21();
        }else{
            stopScanningSDK19();
        }
    }

    @TargetApi(19)
    public void stopScanningSDK19()
    {
        mBluetoothAdapter.stopLeScan(mLeScanCallback19);

        if (connectionState != ConnectionStates.Connected)
        {
            connectionState = ConnectionStates.Disconnected;
        }

        if (this.listener != null)
        {
            this.listener.stoppedScanning(this);
        }
    }

    @TargetApi(21)
    public void stopScanningSDK21()
    {
        final BluetoothLeScanner bluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        bluetoothLeScanner.stopScan(mLeScanCallback21);

        if (connectionState != ConnectionStates.Connected)
        {
            connectionState = ConnectionStates.Disconnected;
        }

        if (this.listener != null)
        {
            this.listener.stoppedScanning(this);
        }
    }

    public void connectDevice(CortriumC3 device)
    {
        if (mBluetoothAdapter == null || device.getAddress() == null)
        {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
        }

        connect(device);
    }

    public void disconnectDevice()
    {
        disconnect();
    }

    private OnConnectionManagerListener listener = null;
    public void setConnectionManagerListener(OnConnectionManagerListener listener)
    {
        this.listener = listener;
    }

    public interface EcgDataListener
    {
        void ecgDataUpdated(EcgData ecgData);
        void modeRead(SensorMode sensorMode);
        void deviceInformationRead(CortriumC3 device);
    }

    public void setEcgDataListener(EcgDataListener ecgDataListener)
    {
        if (mGattCallback != null)
        {
            mGattCallback.setEcgDataListener(ecgDataListener);
        }
    }

    public interface OnConnectionManagerListener
    {
        void startedScanning(ConnectionManager manager);
        void stoppedScanning(ConnectionManager manager);
        void discoveredDevice(CortriumC3 device);
        void connectedToDevice(CortriumC3 device);
        void disconnectedFromDevice(CortriumC3 device);
    }

    /**
     * Clear the list of discovered devices. Fix bug "repeated entities"
     */
    public void clear() {
        discoveredDevices.clear();
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close()
    {
        if (mBluetoothGatt == null)
        {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect()
    {
        if (mBluetoothAdapter == null || mBluetoothGatt == null)
        {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }

        mBluetoothGatt.disconnect();
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param device The device.
     * @return Return true if the connection is initiated successfully. The connection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public boolean connect(CortriumC3 device)
    {
        if (mBluetoothAdapter == null || device == null)
        {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && device.getAddress().equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null)
        {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect())
            {
                connectionState = ConnectionStates.Connecting;
                return true;
            }
            else
            {
                return false;
            }
        }

        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.getBluetoothDevice().connectGatt(mContext, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = device.getAddress();
        mCortriumC3Device = device;
        connectionState = ConnectionStates.Connecting;

        return true;
    }

    static boolean isSDK21(){
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public class ScanCallbackAPI21 extends ScanCallback {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            if (!discoveredDevices.contains(result.getDevice().getAddress()))
            {
                discoveredDevices.add(result.getDevice().getAddress());

                // JKN 19/8-2019
                if( listener!=null)
                {
                    String str = result.getDevice().getName();
                    if( str != null) {
                        str += ":";
                        Log.d("Device name : ",str);

                        if( str.startsWith("C3T") )
                            Log.d("Device name OK : ",str);
                    }
                }

                // JKN 19/8-2019
                if( listener!=null)// B17_393_4
                {
                    String str = result.getDevice().getName();
                    if( str != null) {
                        str += ":";
                        Log.d("Device name : ",str);

                        if( str.startsWith("B1") )
                            Log.d("Device name OK : ",str);
                    }
                }
                //if (listener != null && result.getDevice().getName() != null && result.getDevice().getName().contains("C3-"))
                if (listener != null && result.getDevice().getName() != null &&
                        ( result.getDevice().getName().startsWith("C3") || result.getDevice().getName().startsWith("B17") ) )
                {
                    CortriumC3 newDevice = new CortriumC3(result.getDevice());
                    listener.discoveredDevice(newDevice);
                }
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    }
}


