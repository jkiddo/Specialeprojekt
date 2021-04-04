package com.au.assure;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.SensorEventListener;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.au.assure.datapackages.EcgData;
import com.au.assure.datatypes.SensorMode;

import java.util.ArrayList;

import static androidx.core.content.PermissionChecker.PERMISSION_DENIED;

public class MainActivity extends AppCompatActivity
        implements ConnectionManager.OnConnectionManagerListener, ConnectionManager.EcgDataListener, AdapterView.OnItemClickListener, DialogFragmentEditValues.NoticeDialogListener
{
    public ArrayList<BluetoothDevice> btDevices = new ArrayList<>();
    public DeviceListAdapter deviceListAdapter;

    private static final String TAG = "MainActivity";
    int REQUEST_ENABLE_BT;
    ListView lvNewDevices;
    TextView tvStatus;
    TextView tvModCSI;
    TextView tvCSI;
    ConnectionManager m_ConnectionManager = null;
    ArrayList<CortriumC3> m_al_C3Devices;
    ArrayList<String> m_al_C3Names;
    BluetoothAdapter bluetoothAdapter;
    String m_strReconnect_DeviceName = null;
    int m_iC3DiscoveredCount = 0;
    boolean bResult = false;
    double ModCSIThresh;
    double CSIThresh;
    View listItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkBTPermissions();

        btDevices = new ArrayList<>();
        tvStatus = findViewById(R.id.lbStatus);
        lvNewDevices = (ListView) findViewById(R.id.lvNewDevices);
        lvNewDevices.setOnItemClickListener(MainActivity.this);

        m_al_C3Devices = new ArrayList<>();
        m_al_C3Names = new ArrayList<>();

        ModCSIThresh = Double.parseDouble(getString(R.string.defaultModCSI));
        CSIThresh = Double.parseDouble(getString(R.string.defaultCSI));

        tvModCSI = findViewById(R.id.tvModCSI);
        tvCSI = findViewById(R.id.tvCSI);

        // Ask user to enable bluetooth
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // Bluetooth not supported on device
            Toast toast = Toast.makeText(getApplicationContext(),R.string.btNotSupported,Toast.LENGTH_LONG);
            toast.show();
        }
        if (!bluetoothAdapter.isEnabled()) {
            // Enable bluetooth
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        // Register for broadcasts when a device is discovered.
//        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
//        registerReceiver(broadcastReceiver1, filter);
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: called.");
        super.onDestroy();

        // Close potential Bluetooth connections
//        unregisterReceiver(broadcastReceiver1);
        if( broadcastReceiver4!=null)
            unregisterReceiver(broadcastReceiver4);
        if( m_ConnectionManager != null ) {
            if (m_ConnectionManager.getConnectionState() == ConnectionManager.ConnectionStates.Scanning)
                m_ConnectionManager.stopScanning();
            m_ConnectionManager.disconnect();
        }
    }

    // Bluetooth
    // Create a BroadcastReceiver for ACTION_FOUND.
//    private final BroadcastReceiver broadcastReceiver1 = new BroadcastReceiver() {
//        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
//            if (action.equals(BluetoothDevice.ACTION_FOUND)){
//                BluetoothDevice device = intent.getParcelableExtra (BluetoothDevice.EXTRA_DEVICE);
//                btDevices.add(device);
//                deviceListAdapter = new DeviceListAdapter(context, R.layout.device_list_adapter, btDevices);
//                lvNewDevices.setAdapter(deviceListAdapter);
//            }
//        }
//    };

    public void btnDiscover(View view) {
        StartConnectionManager();
//        if(bluetoothAdapter.isDiscovering()){
//            bluetoothAdapter.cancelDiscovery();
//
//            //check BT permissions in manifest
//            checkBTPermissions();
//
//            bluetoothAdapter.startDiscovery();
//            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
//            registerReceiver(receiver, discoverDevicesIntent);
//        }
//        if(!bluetoothAdapter.isDiscovering()){
//
//            //check BT permissions in manifest
//            checkBTPermissions();
//
//            bluetoothAdapter.startDiscovery();
//            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
//            registerReceiver(receiver, discoverDevicesIntent);
//        }
    }

//    /**
//     * This method is required for all devices running API23+
//     * Android must programmatically check the permissions for bluetooth. Putting the proper permissions
//     * in the manifest is not enough.
//     *
//     * NOTE: This will only execute on versions > LOLLIPOP because it is not needed otherwise.
//     */
//    private void checkBTPermissions() {
//        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
//            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
//            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
//            if (permissionCheck != 0) {
//
//                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
//            }
//        }else{
//            // No need to check permissions
//        }
//    }

    private void checkBTPermissions() {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.WRITE_EXTERNAL_STORAGE");
            permissionCheck += this.checkSelfPermission("Manifest.permission.READ_EXTERNAL_STORAGE");
            if (permissionCheck != 0)
            {
                // You can only request permissions once, so request ALL in one go.
                this.requestPermissions(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                }, 1001); //Any number
            }
        }else{
            Log.d(TAG, "checkBTPermissions: No need to check permissions. SDK version <= LOLLIPOP.");
        }
    }

    /**
     * Broadcast Receiver that detects bond state changes (Pairing status changes)
     */
    private final BroadcastReceiver broadcastReceiver4 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)){
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //3 cases:
                //case1: bonded already
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED){
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

    protected void CancleConnectToDevice() {
        m_ConnectionManager.stopScanning();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        ConnectToDevice(i);
        if (bResult)
            view.setBackgroundColor(getColor(R.color.softGreen));
        listItem = view;
    }

    protected void ConnectToDevice(int i)
    {
        m_ConnectionManager.stopScanning();

        //first cancel discovery because its very memory intensive.
        boolean b = bluetoothAdapter.cancelDiscovery();

        // JKN new 4/8-2019
        Log.d(TAG, "onItemClick: You Clicked on a device.");
        String deviceName = m_al_C3Devices.get(i).getName();
        String deviceAddress = m_al_C3Devices.get(i).getAddress();

        Log.d(TAG, "onItemClick: deviceName = " + deviceName);
        Log.d(TAG, "onItemClick: deviceAddress = " + deviceAddress);

        bResult = m_ConnectionManager.connect(m_al_C3Devices.get(i));
        Toast.makeText(this, "bResult = " + ( bResult?"true":"false"), Toast.LENGTH_SHORT).show();

        tvStatus.setText(getString(R.string.statusConnected));
    }

    public void StartConnectionManager()
    {
        // Try to solve problem with C3-device not being discoverable.
        if(bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }

        m_ConnectionManager = ConnectionManager.getInstance(this);
        m_ConnectionManager.setConnectionManagerListener(this);
        m_ConnectionManager.startScanning();

        if( m_strReconnect_DeviceName == null ) {
            tvStatus.setText(getString(R.string.statusScanning));
            lvNewDevices.setBackgroundColor(Color.WHITE);
        } // Startup. Not reconnect. No device selected by user.
        else {
            ConnectionManager.ConnectionStates state = m_ConnectionManager.getConnectionState();
        }
    }

    @Override
    public void startedScanning(ConnectionManager manager) {
        Log.d(TAG, "public void startedScanning(ConnectionManager manager)");
    }

    @Override
    public void discoveredDevice(CortriumC3 device) {
        Log.d(TAG, "public void discoveredDevice(CortriumC3 device)");
        m_iC3DiscoveredCount++;

        String strDeviceName = device.getName();

        if( strDeviceName.startsWith( "C3" ) || strDeviceName.startsWith( "B17" ) ) {
            m_al_C3Devices.add(device);
            m_al_C3Names.add( device.getName());

            Log.d(TAG, "onReceive: " + device.getName() + ": " + device.getAddress());

            if( m_strReconnect_DeviceName == null ) { // Startup. No device selected by user.
                // Fill listview with devices.
                btDevices.add(device.getBluetoothDevice());
                deviceListAdapter = new DeviceListAdapter(getApplicationContext(), R.layout.device_list_adapter, btDevices);
                lvNewDevices.setAdapter(deviceListAdapter);
            }
            else// Device disconnected, and app tries to reconnect.
            {
                if( m_strReconnect_DeviceName.equals(strDeviceName) )
                {
                    // Device found
                    m_strReconnect_DeviceName = null;// No more reconnect.
                    int iDevideIndex = m_al_C3Devices.size() - 1;
                    ConnectToDevice(iDevideIndex);
                }
            }
        }
    }

    @Override
    public void ecgDataUpdated(EcgData ecgData) {

    }

    @Override
    public void modeRead(SensorMode sensorMode) {

    }

    @Override
    public void deviceInformationRead(CortriumC3 device) {

    }

    @Override
    public void stoppedScanning(ConnectionManager manager) {
        Log.d(TAG, "public void stoppedScanning(ConnectionManager manager)");

        if( m_strReconnect_DeviceName != null )// If reconnect not in progress
            StartConnectionManager();
    }

    @Override
    public void connectedToDevice(CortriumC3 device) {
        m_ConnectionManager.setEcgDataListener(this);

        if( true ) {
            CortriumC3 c3Device = m_ConnectionManager.getConnectedDevice();
            String strName = c3Device.getName();
            Log.i("JKN","DeviceName = " + strName );
        }
    }

    @Override
    public void disconnectedFromDevice(CortriumC3 device) {
        tvStatus.setText(getString(R.string.status));
        if (listItem != null)
            listItem.setBackgroundColor(Color.WHITE);
    }

    public void btnEditValues(View view) {
        // Create an instance of the dialog fragment and show it
        DialogFragmentEditValues editValues = new DialogFragmentEditValues(ModCSIThresh, CSIThresh);
        editValues.show(getSupportFragmentManager(), "editValues");
    }

    // The dialog fragment receives a reference to this Activity through the
    // Fragment.onAttach() callback, which it uses to call the following methods
    // defined by the NoticeDialogFragment.NoticeDialogListener interface
    @Override
    public void onDialogPositiveClick(double modCSI, double CSI) {
        // User touched the dialog's positive button
        ModCSIThresh = modCSI;
        CSIThresh = CSI;

        tvModCSI.setText(Double.toString(ModCSIThresh));
        tvCSI.setText(Double.toString(CSIThresh));
    }

    @Override
    public void onDialogNegativeClick() {
        // User touched the dialog's negative button
    }
}