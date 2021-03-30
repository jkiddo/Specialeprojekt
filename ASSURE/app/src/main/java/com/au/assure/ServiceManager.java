package com.au.assure;

//package com.cortrium.opkit;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.au.assure.data.ConvertVBatToPercent;
import com.au.assure.datapackages.EcgData;
import com.au.assure.datatypes.ECGSamples;
import com.au.assure.datatypes.HeartRate;
import com.au.assure.datatypes.MiscInfo;
import com.au.assure.datatypes.SensorMode;
import com.au.assure.filters.EcgHighPassFilter;
import com.au.assure.filters.EcgLowPassFilter;

//import com.au.assure.filters.RespHighPassFilter;
//import com.au.assure.filters.RespLowPassFilter;
//import com.au.assure.views.C3S_Reader;
//import com.au.assure.views.enumCortriunDeviceType;
//import com.au.assure.views.singleton_MyScanningRecorder;
//import com.au.assure.views.singleton_MyScanningRecorder2;
//import com.au.assure.views.singleton_Send_Data;
//import com.au.assure.views.singleton_Shared_Data;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import static com.au.assure.GattAttributes.BLE_CHARACTERISTIC_UUID_Rx;
import static com.au.assure.GattAttributes.BLE_CHARACTERISTIC_UUID_Tx;
import static com.au.assure.GattAttributes.BLE_SERVICE_UUID_C3TESTER;

/**
 * Created by hsk on 11/10/16.
 */

class ServiceManager extends BluetoothGattCallback {
    private final static String TAG = "ContriumC3Comms";

    private final long MAX_UNSIGNED_32BIT_INT_VALUE = 0xffffffffL;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private boolean mServicesDiscovered;

    // Contrium Device Communication Protocol variables
    private boolean mDeviceInformationBroadcastSent = false;
    private boolean mFirstBatchAfterConnection = true;
    private long mExpectedSerial = 0;
    private MiscInfo mMiscInfoData = null;
    private ECGSamples mEcg1Data = null;
    private ECGSamples mEcg2Data = null;
    private ECGSamples mEcg3Data = null;
    private ByteBuffer mPayloadBytesBuffer = ByteBuffer.allocate(com.au.assure.datatypes.MiscInfo.LENGTH + (3 * ECGSamples.LENGTH));
    private long mLostBatchCount = 0;

    private EcgHighPassFilter[] mHighPassFilters = null;
    private EcgLowPassFilter[] mLowPassFilters = null;

//    private RespHighPassFilter mRespHighPassFilter = null;
//    private RespLowPassFilter mRespLowPassFilter = null;

//    private PeakDetector mPeakDetector;
    private HeartRate mHeartRateDetector;

    private BluetoothGattCharacteristic mModeCharacteristic;

    private int mDescriptorIndex = 0;

    private ConnectionManager mConnectionManager;
    private CortriumC3 mDevice;
    private Context mContext;

    enumCortriunDeviceType m_eDevice = enumCortriunDeviceType.DEFAULT;

    protected ServiceManager(ConnectionManager connectionManager) {
        mConnectionManager = connectionManager;

//        m_sDebugSettings = singleton_DebugSettings.getInstance();
    }

    protected void updateBluetoothInfo(BluetoothAdapter adapter, BluetoothGatt gatt) {
        mBluetoothAdapter = adapter;
        mBluetoothGatt = gatt;
    }

    protected void setContext(Context context) {
        mContext = context;
    }

    protected void discoverServicesForDevice(CortriumC3 device) {
        mDevice = device;
        mBluetoothGatt.discoverServices();
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            mServicesDiscovered = true;
            //  broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            // A connection has been established and all services have been discovered.
            // Now start the communication protocols

            if (true)// jkn 20/8-2019
            {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    List<BluetoothGattService> gattServices = gatt.getServices();
                    for (BluetoothGattService gattService : gattServices) {
                        {
                            String str = gattService.getUuid().toString();
                            Log.i("JKN", "Service UUID Found: " + str);
                        }

//  00001800-0000-1000-8000-00805f9b34fb    	GENERIC_INFORMATION_SERVICE
//  00001801-0000-1000-8000-00805f9b34fb		GENERIC_ATTRIBUTES_SERVICE
//  0000180a-0000-1000-8000-00805f9b34fb		EVICE_INFORMATION_SERVICE
//  0000ffc0-0000-1000-8000-00805f9b34fb		CONTRIUM_C3_DATA_SERVICE

                        // C3TESTER
                        /// Service UUID Found: 00001800-0000-1000-8000-00805f9b34fb    	GENERIC_INFORMATION_SERVICE
                        /// Service UUID Found: 00001801-0000-1000-8000-00805f9b34fb		GENERIC_ATTRIBUTES_SERVICE
                        /// Service UUID Found: 6e400001-b5a3-f393-e0a9-e50e24dcca9e     BLE_SERVICE_UUID_C3TESTER

                    }
                }
            }

            // Step 1: Get the sensor mode after 2 seconds
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    if (m_eDevice == enumCortriunDeviceType.C3_ORIGINAL)
                        requestSensorMode();
                    //                   else
                    //                     StartScanning_intersog();
                }
            }, 2000);

            // Step 2: Read Device Information
            requestDeviceInfo();


        } else {
            Log.w(TAG, "onServicesDiscovered received: " + status);
        }
    }

    /*
        boolean m_Scanning = false;
        ScanCallback m_scanCallback = null;

        void Make_ScanCallback() {
            m_scanCallback = new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);

                    ScanRecord scanRecord = result.getScanRecord();
                    byte[] scanBytes = scanRecord.getBytes();
                    int iLength = (int) scanBytes[0];
                    String str = "iLength = " + iLength + " ";
    //                Log.i("JKN",str);

                    for (int i = 0; i < iLength; i++) {
                        //str += "," + scanBytes[i + 1];
                        str += getHexString( ((int)scanBytes[i+1])>=0 ? ((int)scanBytes[i+1]):((int)scanBytes[i+1]+1+0xFF) );
                    }

                    if (scanRecord.getAdvertiseFlags() == -1) {
                        Log.i("JKN", str);

                    } else
                        Log.i("JKN", "Other : ( " + scanRecord.getAdvertiseFlags() + " ) " + str);

                }
            };

        }
    */
    // JKN 6/9-2019
    // https://intersog.com/blog/tech-tips/how-to-work-properly-with-bt-le-on-android/
    private void ConnectToService(BluetoothGatt gatt) {
        if (m_eDevice == enumCortriunDeviceType.C3_PLUS) {
            BluetoothGattService commService = gatt.getService(BLE_SERVICE_UUID_C3TESTER);
            BluetoothGattCharacteristic inputChar = commService.getCharacteristic(BLE_CHARACTERISTIC_UUID_Rx);
            subscribeNotifications(gatt, inputChar);
            /*BluetoothGattCharacteristic*/
            outputChar = commService.getCharacteristic(BLE_CHARACTERISTIC_UUID_Tx);

            Log.i("JKN", "ConnectToService C3_PLUS");
        }
    }

    private void subscribeNotifications(BluetoothGatt gatt,
                                        BluetoothGattCharacteristic c) {
        boolean b = gatt.setCharacteristicNotification(c, true);
        Log.i("JKN", "subscribeNotifications:" + (b ? "True" : "False"));


        BluetoothGattDescriptor descriptor = c.getDescriptors().get(0);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        gatt.writeDescriptor(descriptor);
/*
//This is usually needed as well
        //BluetoothGattDescriptor desc = c.getDescriptor(BLE_CHARACTERISTIC_UUID_Rx);//	INPUT_DESC_ID);
        BluetoothGattDescriptor desc = c.getDescriptor(BLE_SERVICE_UUID_C3TESTER);
//Could also be ENABLE_NOTIICATION_VALUE
        desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        gatt.writeDescriptor(desc);
*/
    }

    // JKN 6/9-2019
/*
    // JKN 5/9-2019
    // https://intersog.com/blog/tech-tips/how-to-work-properly-with-bt-le-on-android/
    private void StartScanning_intersog()
    {
        ScanSettings scanSettings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();

        String deviceName = mDevice.getName();
        String deviceAddress = mDevice.getAddress();

        //If name or address of peripheral is known
        ScanFilter scanFilter = new ScanFilter.Builder()
                .setDeviceName(deviceName)
                .setDeviceAddress(deviceAddress)
                .build();

        if ( m_scanCallback == null )
            Make_ScanCallback();

        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothLeScanner bluetoothLeScanner = adapter.getBluetoothLeScanner();
        bluetoothLeScanner.startScan(Collections.singletonList(scanFilter), scanSettings, m_scanCallback );
    }
    // JKN 5/9-2019
*/
    String[] strLetter = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};

    public String getHexString(int b) {
        String str = new String(",0x");

        str += strLetter[(b & 0xF0) >> 4];

        str += strLetter[(b & 0x0F)];

        return str;
    }

    public String getOctString(byte b) {
        String str = new String(",");

        for (int i = 7; i >= 0; i--) {
            byte mask = (byte) (0x01 << i);
            if ((b & mask) != 0) {
                str += "1";
            } else
                str += "0";

        }

        return str;
    }

    private int C1_Value = 0;
    private int C2_Value = 0;
    private int C3_Value = 0;

    private int[] decoded_iECG1 = new int[100];
    private int[] decoded_iECG2 = new int[100];
    private int[] decoded_iECG3 = new int[100];

    private static int MyIndex_Counter = 0;

    private static int myLineIndex = 0;


    long m_lLastMissingTime = 0;
    /*
        private singleton_Shared_Data m_sData = null;
        private singleton_Shared_Data GetSharedData()
        {
            if( m_sData==null)
                m_sData = singleton_Shared_Data.getInstance();
            return m_sData;
        }
    */
    public C3S_Reader m_C3S_Reader = new C3S_Reader("32min.C3S");


    public void ReadFile() {
        byte[] Line = m_C3S_Reader.GetLine();
        if (Line != null)
            decode_c(Line);
    }

    private void decode_c(byte[] Line) {

/*
        if( true) {
            decode_Static();
            return;
        }
/*
        if( m_sDebugSettings.GetShowStaticData() )
        {
            decode_Static();
            return;
        }*/

        if (mFirstBatchAfterConnection) {
            C1_Value = 0;
            C2_Value = 0;
            C3_Value = 0;
        }

        int Length = Line[0];                            //First byte contains the length of the telegram

        int index = 0;

        //Decodes Part1 of the telegram
        int len = Line[index++];
        int New_ID = Line[index++];
        int Index_Counter = Line[index++];

        if (true) {
            if ((MyIndex_Counter != (Index_Counter - 1)) && ((Index_Counter != -128) && (MyIndex_Counter != 127))) {
                long lCurrentTime = System.currentTimeMillis();
                long lDiffMissingTime = lCurrentTime - m_lLastMissingTime;
                int iMissing = Index_Counter - MyIndex_Counter - 1;
                Log.i("JKN", "( MyIndex_Counter != Index_Counter+1) " + MyIndex_Counter + ":" + Index_Counter + "     " + (iMissing == -1 ? "Duplicate" : iMissing + " missing ") + "   " + lDiffMissingTime + " ms ");
                String strLog = "Index counter : " + MyIndex_Counter;
                m_lLastMissingTime = lCurrentTime;
            }


            //Log.i("JKN", "( Index_Counter : " + Index_Counter  );

            MyIndex_Counter = Index_Counter;

            //String strLog = "Index counter : " + Index_Counter;
            //           Log.i("JKN",strLog);


        }

//        if (true) {
//            if (m_sDebugSettings == null)
//                m_sDebugSettings = singleton_DebugSettings.getInstance();
//            if ((m_sDebugSettings != null) && (m_sDebugSettings.Get_IsRecording())) {
//                singleton_MyScanningRecorder.Save(Line);
//                //singleton_MyScanningRecorder.Save(Line);
//
//            }
//        }

        int Samples = Line[index++];
/*        int Acc_X = (((Line[index] << 4)&0xFF0) | ((Line[index+1]>>4)&0x0F))<<4 ;			//5 6 // 0xFFF0
        int Acc_Y = (((Line[index+1]&0x0F)<<8) | (Line[index+2]&0xFF))<<4;				//6 7 // 0xFFF0
        int Acc_Z = (((Line[index+3] << 4)&0xFF0) | ((Line[index+4]>>4)&0x0F))<<4;		//8 9 // 0xFFF0
*/
        int Acc_X = Convert_16bit_Sign_Value((((Line[index] << 4) & 0xFF0) | ((Line[index + 1] >> 4) & 0x0F)) << 4);            //5 6 // 0xFFF0
        int Acc_Y = Convert_16bit_Sign_Value((((Line[index + 1] & 0x0F) << 8) | (Line[index + 2] & 0xFF)) << 4);                //6 7 // 0xFFF0
        int Acc_Z = Convert_16bit_Sign_Value((((Line[index + 3] << 4) & 0xFF0) | ((Line[index + 4] >> 4) & 0x0F)) << 4);        //8 9 // 0xFFF0


        index += 4;
        int Events = Line[index++] & 0x0F;
        int Vbat = 0;
        int Button = 0;
        int LOD_Active = 0x00;

        Acc_X = Acc_X / 16;
        Acc_Y = Acc_Y / 16;
        Acc_Z = Acc_Z / 16;

//        if (true) {
//            if (m_sDebugSettings == null)
//                m_sDebugSettings = singleton_DebugSettings.getInstance();
//            if (m_sDebugSettings != null) {
//
//                if (m_sDebugSettings.m_GraphX != null)
//                    m_sDebugSettings.m_GraphX.SetValue((float) (Acc_X));
//                if (m_sDebugSettings.m_GraphY != null)
//                    m_sDebugSettings.m_GraphY.SetValue((float) (Acc_Y));
//                if (m_sDebugSettings.m_GraphZ != null)
//                    m_sDebugSettings.m_GraphZ.SetValue((float) (Acc_Z));
//
//            }
//        }

        if (Events != 0x00)        //Read event bytes
        {
            //index = index + 2/4/6/8
            if ((Events & 0x01) == 0x01)        //Vbat event
            {
                int value = (((Line[index] << 8) & 0xFF00) | ((Line[index + 1] & 0x00FF)));        //10 11
                //Log.i("JKN","VBat value = "+value);
                index += 2;
                float fVbat = (float) (value) * 0.6f * 3f * 3f / 1024f;
                Log.i("JKN", "VBat fVbat = " + fVbat);

                float fPercent = ConvertVBatToPercent.Convert_to_Percent(fVbat);
                Log.i("JKN", "Battery : " + fPercent + "%");

                Vbat = (value * 1080) / 1024;
                //Log.i("JKN","VBat Vbat = "+Vbat);
//                if (m_sDebugSettings.m_GraphVBat != null)
//                    m_sDebugSettings.m_GraphVBat.SetValue((float) (fVbat));
//                singleton_Shared_Data sd = singleton_Shared_Data.getInstance();
//                if (sd.m_vBatPer == null)
//                    sd.Initialize_VBat(10, fPercent);
//                if (sd != null) sd.m_vBatPer.SetValue(fPercent);
//                if( m_sDebugSettings.m_GraphX!=null)
                //                  m_sDebugSettings.m_GraphX.SetValue((float) (Vbat));

                //to do : Series7->AddY(Vbat/100.0,' ', clRed);
            }
            if ((Events & 0x02) == 0x02)        //LOD event
            {
                int value = (((Line[index] << 8) & 0xFF00) | ((Line[index + 1] & 0x00FF)));        //10 11
                index += 2;

//                if( m_sDebugSettings.m_GraphY!=null)
                //                  m_sDebugSettings.m_GraphY.SetValue((float) (value));

                LOD_Active = value;

                //int X = Series4->Count();
                //Series12->AddArrow(X, 0, X, 1000,' ', clBlue);
            }
            if ((Events & 0x04) == 0x04)        //Button event
            {
                Button = (((Line[index] << 8) & 0xFF00) | ((Line[index + 1] & 0x00FF)));        //10 11
                index += 2;

//                if( m_sDebugSettings.m_GraphZ!=null)
                //                  m_sDebugSettings.m_GraphZ.SetValue((float) (Button));

                //to do : int X = Series1->Count();
                //to do : Series12->AddArrow(X, 0, X, 1000,' ', clBlue);
            }

        }

        int C1_Compression = 0;
        int C2_Compression = 0;
        int C3_Compression = 0;

        //Number of samples determines how many bytes are used for compression mask
        if (Samples > 12) {
            C1_Compression = ((Line[index] << 24) & 0xFF000000) | ((Line[index + 1] << 16) & 0x00FF0000) | ((Line[index + 2] << 8) & 0x0000FF00) | ((Line[index + 3]) & 0x000000FF);    //10 11 12 13
            C2_Compression = ((Line[index + 4] << 24) & 0xFF000000) | ((Line[index + 5] << 16) & 0x00FF0000) | ((Line[index + 6] << 8) & 0x0000FF00) | ((Line[index + 7]) & 0x000000FF);    //14 15 16 17
            C3_Compression = ((Line[index + 8] << 24) & 0xFF000000) | ((Line[index + 9] << 16) & 0x00FF0000) | ((Line[index + 10] << 8) & 0x0000FF00) | ((Line[index + 11]) & 0x000000FF);    //18 19 20 21
            index += 12;
        } else if (Samples > 8) {
            C1_Compression = ((Line[index] << 16) & 0xFF0000) | ((Line[index + 1] << 8) & 0x00FF00) | ((Line[index + 2]) & 0x0000FF);    //10 11 12
            C2_Compression = ((Line[index + 3] << 16) & 0xFF0000) | ((Line[index + 4] << 8) & 0x00FF00) | ((Line[index + 5]) & 0x0000FF);    //13 14 15
            C3_Compression = ((Line[index + 6] << 16) & 0xFF0000) | ((Line[index + 7] << 8) & 0x00FF00) | ((Line[index + 8]) & 0x0000FF);    //16 17 18
            index += 9;
        } else {
            C1_Compression = ((Line[index] << 8) & 0xFF00) | ((Line[index + 1] & 0x00FF));                //10 11
            C2_Compression = ((Line[index + 2] << 8) & 0xFF00) | ((Line[index + 3] & 0x00FF));                //12 13
            C3_Compression = ((Line[index + 4] << 8) & 0xFF00) | ((Line[index + 5] & 0x00FF));                //14 15
            index += 6;
        }
/*
        int C1_Value = 0;
        int C2_Value = 0;
        int C3_Value = 0;
/*
        C1_Value = 100000;
        C2_Value = 100000;
        C3_Value = 100000;
*/
        int C1_Diff = 0;
        int C2_Diff = 0;
        int C3_Diff = 0;

        int tmp_buf;

        int bitmask;
        String str = "";

        for (int i = Samples - 1; i >= 0; i--) {
            bitmask = (C1_Compression >> (i * 2)) & 0x03;        //Decodes the compression mask (0=1byte value, 1=2byte value, 2=3byte value)

            if (bitmask == 2) {
                tmp_buf = ((Line[index] << 16) & 0xFF0000) | ((Line[index + 1] << 8) & 0x00FF00) | ((Line[index + 2]) & 0x0000FF);
                C1_Diff = Convert_24bit_Sign_Value(tmp_buf);
                index += 3;
            } else if (bitmask == 1) {
                tmp_buf = ((Line[index] << 8) & 0xFF00) | (Line[index + 1] & 0x00FF);
                C1_Diff = Convert_16bit_Sign_Value(tmp_buf);
                //str = " " + Line[index] + " " + Line[index+1] + " " + getHexString( Line[index] )+" "+ getHexString( Line[index+1] )+" "+tmp_buf+" "+ C1_Diff;
                index += 2;
            } else {
                tmp_buf = (Line[index] & 0x00FF);
                C1_Diff = Convert_8bit_Sign_Value(tmp_buf);
                //str = " " + Line[index] + " " + getHexString( Line[index] )+" "+tmp_buf+" "+ C1_Diff;
                index += 1;
            }

            bitmask = (C2_Compression >> (i * 2)) & 0x03;        //Decodes the compression mask (0=1byte value, 1=2byte value, 2=3byte value)

            if (bitmask == 2) {
                tmp_buf = ((Line[index] << 16) & 0xFF0000) | ((Line[index + 1] << 8) & 0x00FF00) | ((Line[index + 2]) & 0x0000FF);
                C2_Diff = Convert_24bit_Sign_Value(tmp_buf);
                index += 3;
            } else if (bitmask == 1) {
                tmp_buf = ((Line[index] << 8) & 0xFF00) | (Line[index + 1] & 0x00FF);
                C2_Diff = Convert_16bit_Sign_Value(tmp_buf);
                //               str = " " + Line[index] + " " + Line[index+1] + " " + getHexString( Line[index] )+" "+ getHexString( Line[index+1] )+" "+tmp_buf+" "+ C2_Diff;
                index += 2;
            } else {
                tmp_buf = (Line[index] & 0x00FF);
                C2_Diff = Convert_8bit_Sign_Value(tmp_buf);
                //               str = " " + Line[index] + " " + getHexString( Line[index] )+" "+tmp_buf+" "+ C2_Diff;
                index += 1;
            }

            bitmask = (C3_Compression >> (i * 2)) & 0x03;        //Decodes the compression mask (0=1byte value, 1=2byte value, 2=3byte value)

            if (bitmask == 2) {
                tmp_buf = ((Line[index] << 16) & 0xFF0000) | ((Line[index + 1] << 8) & 0x00FF00) | ((Line[index + 2]) & 0x0000FF);
                C3_Diff = Convert_24bit_Sign_Value(tmp_buf);
                index += 3;
            } else if (bitmask == 1) {
                tmp_buf = ((Line[index] << 8) & 0xFF00) | (Line[index + 1] & 0x00FF);
                C3_Diff = Convert_16bit_Sign_Value(tmp_buf);
                index += 2;
            } else {
                tmp_buf = (Line[index] & 0x00FF);
                C3_Diff = Convert_8bit_Sign_Value(tmp_buf);
                index += 1;
            }

            if (index >= Length) {
                Log.i("JKN", "decode error : ( index > Length ) : " + index + " >= " + Length);
            }

            //         Log.i("JKN",str);
            str += " ";

            C1_Value += C1_Diff;
            C2_Value += C2_Diff;
            C3_Value += C3_Diff;


            int iECGindex = Samples - 1 - i;
            decoded_iECG1[iECGindex] = C1_Value;
            decoded_iECG2[iECGindex] = C2_Value;
            decoded_iECG3[iECGindex] = C3_Value;
        }

        // Check if this is the first batch after connection
        // If so initialise the expected serial
        if (mFirstBatchAfterConnection) InitializeFilters(Samples);

        // Filter channels
        EcgData ecgData = new EcgData(false);

        ecgData.setEcgData(decoded_iECG1, decoded_iECG2, decoded_iECG3, Samples);

        ecgData.SetSampleIndex(Index_Counter);

        //if(  ecgData.miscInfo.getAccelerometerX())

        //       ecgData.SetAccelerometerRaw( Acc_X , Acc_Y , Acc_Z );

        int[] decodedECG1 = ecgData.getRawEcg1Samples();
        int[] decodedECG2 = ecgData.getRawEcg2Samples();
        int[] decodedECG3 = ecgData.getRawEcg3Samples();

//        singleton_MyScanningRecorder2.SaveRaw(decodedECG1, decodedECG2, decodedECG3);

        // Apply filters
        ecgData.applyFilters_C3_PLUS(mHighPassFilters, mLowPassFilters, mHeartRateDetector);

        int[] filteredECG1 = ecgData.getFilteredEcg1Samples();
        int[] filteredECG2 = ecgData.getFilteredEcg2Samples();
        int[] filteredECG3 = ecgData.getFilteredEcg3Samples();

//        singleton_MyScanningRecorder2.SaveFiltered(filteredECG1, filteredECG2, filteredECG3);

        // JKNJKN
//        if (singleton_Send_Data.m_bSend) {
///*            singleton_Send_Data.m_bSend = false;
//            byte [] data = {0x70,0x00,0x50};
//            data[0] = singleton_Send_Data.m_byteSend;
//            //Toast.makeText(mContext, "Begin messages send" + (int)data[0], Toast.LENGTH_SHORT).show();
//            singleton_Send_Data.m_byteSend++;
//            if( singleton_Send_Data.m_byteSend > 0x72)
//                singleton_Send_Data.m_byteSend = 0x70;
//*/
//            writeDataToChar(singleton_Send_Data.GetBuffer());
//        }

        reportNewEcgData(ecgData);
    }

    private int Convert_24bit_Sign_Value(int tmp_buf) {
        if (tmp_buf > 0x7FFFFF)
            tmp_buf = tmp_buf | 0xFF000000;
        return tmp_buf;
    }

    private int Convert_16bit_Sign_Value(int tmp_buf) {
        if (tmp_buf > 0x7FFF)
            tmp_buf = tmp_buf | 0xFFFF0000;

        return tmp_buf;
    }

    private int Convert_8bit_Sign_Value(int tmp_buf) {
        if (tmp_buf > 0x7F)
            tmp_buf = tmp_buf | 0xFFFFFF00;

        return tmp_buf;
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt,
                                     BluetoothGattCharacteristic characteristic,
                                     int status) {
        // JKN 6/9-2019
        if (status == BluetoothGatt.GATT_SUCCESS && characteristic.getUuid().equals(BLE_CHARACTERISTIC_UUID_Rx)) {
            byte[] data = characteristic.getValue();
            if (data == null) {
                Log.e(TAG, "onCharacteristicRead: data = null");
                return;
            }
            Log.i("JKN", "onCharacteristicRead");
            //bleHandler.obtainMessage(MSG_DATA_READ, data).sendToTarget();
        } else {
            //reconnectOnError("onCharacteristicRead", status);
        }
        // JKN 6/9-2019

        if (status == BluetoothGatt.GATT_SUCCESS) {
            if (GattAttributes.CONTRIUM_C3_DATA_SERVICE.equals(characteristic.getService().getUuid())) {
                onContriumDataServiceCharacteristicUpdated(characteristic);
            } else {
                onOtherCharacteristicUpdated(characteristic);
            }
        }
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt,
                                      BluetoothGattCharacteristic characteristic,
                                      int status) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            if (GattAttributes.CONTRIUM_C3_MODE.equals(characteristic.getUuid())) {
                Log.d(TAG, "Did write MODE");
                if (mDescriptorIndex == 0) {
                    setDataServiceNotificationsState(true, mDescriptorIndex++);
                } else {
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            requestSensorMode();
                        }
                    }, 200);
                }
            }

            // JKN 22/8-2019
            if (BLE_CHARACTERISTIC_UUID_Tx.equals(characteristic.getUuid())) {
                String str = "BLE_CHARACTERISTIC_UUID_Rx : Write succeded";
                Log.i("JKN", str);
                //Toast.makeText(mContext, "Write succeded", Toast.LENGTH_SHORT).show();
            }
   /*         if (BLE_CHARACTERISTIC_UUID_Tx.equals(characteristic.getUuid())){
                String str = "BLE_CHARACTERISTIC_UUID_Tx";
                Log.i("JKN",str);
            }
            // JKN 22/8-2019
            */
        }
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        if (GattAttributes.CONTRIUM_C3_DATA_SERVICE.equals(characteristic.getService().getUuid())) {
            // JKN 2019-2019
            if (mFirstBatchAfterConnection)
                InitializeFilters(ECGSamples.NUMBER_OF_SAMPLES);
            // JKN 2019-2019

            onContriumDataServiceCharacteristicUpdated(characteristic);
        } else {
            onOtherCharacteristicUpdated(characteristic);
        }
    }

    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        if (mDescriptorIndex < 4) {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    setDataServiceNotificationsState(true, mDescriptorIndex++);
                }
            }, 200);

        }
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        if (characteristic == null) {
            Log.w(TAG, "Characteristic not found");
            return;
        }

        boolean b1 = mBluetoothGatt.readCharacteristic(characteristic);
        // JKN 20/8-2019
        Log.i("JKN", "readCharacteristic : Value = " + b1);
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled        If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }

        if (characteristic == null) {
            Log.i("JKN", "characteristic == null");
            return;
        }

        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(GattAttributes.CLIENT_CHARACTERISTIC_CONFIG);
        if (descriptor != null) {
            descriptor.setValue(enabled ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }
    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;

        return mBluetoothGatt.getServices();
    }

    protected void deviceDisconnected() {
        resetServiceStates();
    }

    private void resetServiceStates() {
        mServicesDiscovered = false;

        // Processing Variables
        mFirstBatchAfterConnection = true;
        mDeviceInformationBroadcastSent = false;
        mExpectedSerial = 0;
        mMiscInfoData = null;
        mEcg1Data = null;
        mEcg2Data = null;
        mEcg3Data = null;
        mPayloadBytesBuffer.clear();
        mDescriptorIndex = 0;

        mHighPassFilters = null;
        mLowPassFilters = null;
    }

    public boolean requestSensorMode() {
        boolean requestSent = false;

        if (mBluetoothGatt != null && mServicesDiscovered) {
            BluetoothGattService contriumDataService = null;
            if (m_eDevice == enumCortriunDeviceType.C3_ORIGINAL) {
                // JKN 22/7-2019 : CONTRIUM_C3_DATA_SERVICE not available for C3TESTER
                contriumDataService = mBluetoothGatt.getService(GattAttributes.CONTRIUM_C3_DATA_SERVICE);

                if (contriumDataService != null) {
                    mModeCharacteristic = contriumDataService.getCharacteristic(GattAttributes.CONTRIUM_C3_MODE);
                }
            } else {
                contriumDataService = mBluetoothGatt.getService(GattAttributes.BLE_SERVICE_UUID_C3TESTER);

                if (contriumDataService != null) {
                    mModeCharacteristic = contriumDataService.getCharacteristic(BLE_CHARACTERISTIC_UUID_Rx);
                }
            }

        }

        if (mModeCharacteristic != null) {
            readCharacteristic(mModeCharacteristic);
            requestSent = true;
        }

        return requestSent;
    }

    public void changeSensorMode(CortriumC3.DeviceModes mode) {
        if (mModeCharacteristic == null) return;
        if (mDevice == null) return;
        if (mBluetoothGatt == null) return;

        ByteBuffer buffer = ByteBuffer.allocate(15);

        buffer.put((byte) mode.ordinal());
        buffer.put(mDevice.getConfiguration());
        buffer.put(mDevice.getFilename().getBytes());

        // Must be 15
        byte[] byteArray = buffer.array();

        mModeCharacteristic.setValue(byteArray);
        mBluetoothGatt.writeCharacteristic(mModeCharacteristic);
    }

    public boolean requestDeviceInfo() {
        boolean requestSent = false;

        if (mBluetoothGatt != null && mServicesDiscovered) {
            // JKN 2278-2019 : Not available for C3+ . Not needed for scanning, only information.

            BluetoothGattService deviceInformationService = mBluetoothGatt.getService(GattAttributes.DEVICE_INFORMATION_SERVICE);
            if (deviceInformationService != null) {
                m_eDevice = enumCortriunDeviceType.C3_ORIGINAL;

                readCharacteristic(deviceInformationService.getCharacteristic(GattAttributes.MANUFACTURE_SYSTEM_ID));
                readCharacteristic(deviceInformationService.getCharacteristic(GattAttributes.MANUFACTURE_SOFTWRE_REVISION));
                readCharacteristic(deviceInformationService.getCharacteristic(GattAttributes.MANUFACTURE_FIRMWARE_REVISION));
                readCharacteristic(deviceInformationService.getCharacteristic(GattAttributes.MANUFACTURE_HARDWARE_REVISION));
            } else {
                m_eDevice = enumCortriunDeviceType.C3_PLUS;

                ConnectToService(mBluetoothGatt);//Intersog
            }
        }

        return requestSent;
    }

    public boolean setDataServiceNotificationsState(boolean enable, int index) {
        UUID[] characteristicUUIDs = {GattAttributes.CONTRIUM_C3_MISC_DATA, GattAttributes.CONTRIUM_C3_ECG1_DATA, GattAttributes.CONTRIUM_C3_ECG2_DATA, GattAttributes.CONTRIUM_C3_ECG3_DATA};

        boolean requestSent = false;

        if (mBluetoothGatt != null && mServicesDiscovered) {

            // JKN 2278-2019 : CONTRIUM_C3_DATA_SERVICE not available for C3TESTER
            enumCortriunDeviceType eDevice = enumCortriunDeviceType.DEFAULT;
            // JKN end
            BluetoothGattService btGattService = mBluetoothGatt.getService(GattAttributes.CONTRIUM_C3_DATA_SERVICE);
            if (btGattService != null) {
                Log.d(TAG, String.format("Enabling notifications for %s", characteristicUUIDs[index]));
                setCharacteristicNotification(btGattService.getCharacteristic(characteristicUUIDs[index]), enable);

                // JKN 22/8-2019
                eDevice = enumCortriunDeviceType.C3_ORIGINAL;
                // JKN end
            }

            // JKN 22/8-2019
            if (eDevice == enumCortriunDeviceType.DEFAULT)// not C3original
            {
                BluetoothGattService btGattService_C3Tester = mBluetoothGatt.getService(BLE_SERVICE_UUID_C3TESTER);
                if (btGattService_C3Tester != null) {
                    Log.d(TAG, String.format("Enabling notifications for %s", characteristicUUIDs[index]));
                    setCharacteristicNotification(btGattService_C3Tester.getCharacteristic(characteristicUUIDs[index]), enable);

                    // JKN 22/8-2019
                    eDevice = enumCortriunDeviceType.C3_PLUS;
                    // JKN end

                    outputChar = btGattService_C3Tester.getCharacteristic(BLE_CHARACTERISTIC_UUID_Tx);
                }
            }
            // JKN end
        }

        return requestSent;
    }

    //JKNJKN
    // Write to BLE device ( BLE_CHARACTERISTIC_UUID_Tx )
    // https://intersog.com/blog/tech-tips/how-to-work-properly-with-bt-le-on-android/
    private BluetoothGattCharacteristic outputChar = null;

    //Should always be called on our BLE thread!
    private void writeDataToChar(byte[] data) {
        if (data.length > 20) {
            throw new IllegalArgumentException();
        }

        if (outputChar != null) {
            outputChar.setValue(data);
            mBluetoothGatt.writeCharacteristic(outputChar);
        } else {
            if (data != null) {
                BluetoothGattService btGattService_C3Tester = mBluetoothGatt.getService(BLE_SERVICE_UUID_C3TESTER);
                if (btGattService_C3Tester != null) {
                    outputChar = btGattService_C3Tester.getCharacteristic(BLE_CHARACTERISTIC_UUID_Tx);
                    outputChar.setValue(data);
                    mBluetoothGatt.writeCharacteristic(outputChar);
                }
            }
        }
    }
/*
    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status)
    {
        //if (status == BluetoothGatt.GATT_SUCCESS && c.getUUID().equals(outputChar.getUUID()))
        if (status == BluetoothGatt.GATT_SUCCESS )// && c.getUUID().equals(outputChar.getUUID()))
        {
            //Write operation successful - process with next chunk!
        }
    }
*/
    // https://intersog.com/blog/tech-tips/how-to-work-properly-with-bt-le-on-android/
    // Write to BLE device ( BLE_CHARACTERISTIC_UUID_Tx )

    private void broadcastSensorMode(SensorMode sensorMode) {
        if (this.listener != null)
            if (this.listener != null) {
                this.listener.modeRead(sensorMode);
            }

        final Intent intent = new Intent(ConnectionManager.ACTION_DEVICE_MODE_UPDATED);
        intent.putExtra(ConnectionManager.EXTRA_VALUE, sensorMode);
        mContext.sendBroadcast(intent);
    }

    private void InitializeFilters(int iSampleCount) {
        if (mFirstBatchAfterConnection) {
            if (mMiscInfoData != null)
                mExpectedSerial = mMiscInfoData.getSerial();
            mFirstBatchAfterConnection = false;

            mHighPassFilters = new EcgHighPassFilter[3];
            mHighPassFilters[0] = new EcgHighPassFilter();
            mHighPassFilters[1] = new EcgHighPassFilter();
            mHighPassFilters[2] = new EcgHighPassFilter();

            mLowPassFilters = new EcgLowPassFilter[3];
            mLowPassFilters[0] = new EcgLowPassFilter();
            mLowPassFilters[1] = new EcgLowPassFilter();
            mLowPassFilters[2] = new EcgLowPassFilter();

//            mRespHighPassFilter = new RespHighPassFilter();
//            mRespLowPassFilter = new RespLowPassFilter();
//
//            mPeakDetector = new PeakDetector();
            //mHeartRateDetector = new HeartRate(CortriumC3.SAMPLE_RATE_ECG, iSampleCount);

            int iSampleRate = (iSampleCount == 6 ? CortriumC3.SAMPLE_RATE_ECG : 256);
            mHeartRateDetector = new HeartRate(iSampleRate, iSampleCount);
        }
    }

    private void onContriumDataServiceCharacteristicUpdated(BluetoothGattCharacteristic characteristic) {
        final UUID characteristicID = characteristic.getUuid();
        try {
            if (GattAttributes.BLE_CHARACTERISTIC_UUID_Rx.equals(characteristicID)) {
                String str = "BLE_CHARACTERISTIC_UUID_Rx";
                Log.i("JKN", str);
            }
            if (BLE_CHARACTERISTIC_UUID_Tx.equals(characteristicID)) {
                String str = "BLE_CHARACTERISTIC_UUID_Tx";
                Log.i("JKN", str);
            }
            if (GattAttributes.CONTRIUM_C3_MODE.equals(characteristicID)) {
                Log.d(TAG, String.format("Mode <-- %s", Utils.toHexString(characteristic.getValue())));

                SensorMode sensorMode = new SensorMode(characteristic.getValue());
                Log.d(TAG, sensorMode.toString());

                mDevice.setConfiguration(sensorMode.getConfiguration());
                mDevice.setFilename(sensorMode.getFileName());

                if (sensorMode.getMode() == SensorMode.MODE_HOLTER) {
                    if (!Utils.filenameIsDateString(sensorMode.getFileName())) {
                        String newFilename = Utils.filenameFromSerial(mExpectedSerial);
                        Log.d(TAG, String.format("mode: HOLTER - Original filename: %s Sending new filename to device: %s",
                                sensorMode.getFileName(), newFilename));

                        ByteBuffer buffer = ByteBuffer.allocate(15);

                        buffer.put(sensorMode.getMode());
                        buffer.put(sensorMode.getConfiguration());
                        buffer.put(newFilename.getBytes());

                        mDevice.setFilename(newFilename);

                        // Must be 15
                        byte[] byteArray = buffer.array();

                        broadcastSensorMode(new SensorMode(byteArray));

                        characteristic.setValue(byteArray);
                        mBluetoothGatt.writeCharacteristic(characteristic);
                    } else {
                        Log.d(TAG, "mode: HOLTER - Correct filename active");
                        setDataServiceNotificationsState(true, mDescriptorIndex++);
                        broadcastSensorMode(sensorMode);
                    }
                } else if (sensorMode.getMode() == SensorMode.MODE_ACTIVE) {
                    Log.d(TAG, "Active mode - start reading ECG");
                    setDataServiceNotificationsState(true, mDescriptorIndex++);
                    broadcastSensorMode(sensorMode);
                } else if (sensorMode.getMode() == SensorMode.MODE_IDLE) {
                    Log.d(TAG, "Idle mode");
                    broadcastSensorMode(sensorMode);
                } else if (sensorMode.getMode() == sensorMode.MODE_DISCONNECT) {
                    Log.d(TAG, "Mode disconnecct");
                    broadcastSensorMode(sensorMode);
                }
            } else if (GattAttributes.CONTRIUM_C3_MISC_DATA.equals(characteristicID)) {
                mMiscInfoData = new MiscInfo(characteristic.getValue());

                // Check if this is the first batch after connection
                // If so initialise the expected serial
                if (mFirstBatchAfterConnection)
                    InitializeFilters(ECGSamples.NUMBER_OF_SAMPLES);

                if (mExpectedSerial == mMiscInfoData.getSerial()) {
                    final byte conf = mMiscInfoData.getConfigurationBitmask();

                    if (((conf & SensorMode.CONFIGURATION_ECG1) == 0 || mEcg1Data != null) &&
                            ((conf & SensorMode.CONFIGURATION_ECG2) == 0 || mEcg2Data != null) &&
                            ((conf & SensorMode.CONFIGURATION_ECG3) == 0 || mEcg3Data != null)
                    ) {
                        EcgData ecgData = new EcgData();

                        // Clear the byte buffer before writing bytes
                        mPayloadBytesBuffer.clear();
                        // Always write the misc bytes first
                        mPayloadBytesBuffer.put(mMiscInfoData.getRawDataBytes());
                        ecgData.setMiscInfo(mMiscInfoData);

                        // TODO - Add calback
                        if (mEcg1Data != null) {
                            mPayloadBytesBuffer.put(mEcg1Data.getRawDataBytes());
                            ecgData.setEcg1Data(mEcg1Data);
                            // TODO - Callback
                        }

                        if (mEcg2Data != null) {
                            mPayloadBytesBuffer.put(mEcg2Data.getRawDataBytes());
                            ecgData.setEcg2Data(mEcg2Data);
                            // TODO - Callback
                        }

                        if (mEcg3Data != null) {
                            mPayloadBytesBuffer.put(mEcg3Data.getRawDataBytes());
                            ecgData.setEcg3Data(mEcg3Data);
                            // TODO - Callback
                        }

                        // Batch complete so dispatch rawByte
                        // TODO - Add callback

                        // Apply filters
                        ecgData.applyFilters(mHighPassFilters, mLowPassFilters, mHeartRateDetector);
                        //writeEcgDataToFileDump(ecgData);
                        ecgData.setRawBlePayload(mPayloadBytesBuffer);
                        reportNewEcgData(ecgData);
                    } else {
                        sendFillerSamplesForMiscInfo(mMiscInfoData);
                    }
                } else {
                    Log.d(TAG, String.format("Message serial number was invalid (Expected: %d, Actual: %s)", mExpectedSerial, mMiscInfoData.getSerial()));
                    sendFillerSamplesForMiscInfo(mMiscInfoData);
                }

                // Processing for current batch is complete
                // If max unsigned 32bit integer value (0xffffffff) is reached wrap around to 0.
                mExpectedSerial = mMiscInfoData.getSerial() + 1;
                mEcg1Data = null;
                mEcg2Data = null;
                mEcg3Data = null;
            } else if (GattAttributes.CONTRIUM_C3_ECG1_DATA.equals(characteristicID)) {
                mEcg1Data = new ECGSamples(characteristic.getValue());
            } else if (GattAttributes.CONTRIUM_C3_ECG2_DATA.equals(characteristicID)) {
                mEcg2Data = new ECGSamples(characteristic.getValue());
            } else if (GattAttributes.CONTRIUM_C3_ECG3_DATA.equals(characteristicID)) {
                mEcg3Data = new ECGSamples(characteristic.getValue());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error occurred processing data for characteristic " + GattAttributes.lookup(characteristicID.toString(), "Unknown"), e);
        }
    }

    private void onOtherCharacteristicUpdated(BluetoothGattCharacteristic characteristic) {
        final UUID characteristicID = characteristic.getUuid();
        if (GattAttributes.MANUFACTURE_SYSTEM_ID.equals(characteristicID)) {
            StringBuilder sb = new StringBuilder();
            byte[] data = characteristic.getValue();
            for (byte byteChar : data)
                sb.append(String.format("%02X ", byteChar));

            mDevice.setDeviceSerial(sb.toString());

            Log.i("JKN", "MANUFACTURE_SYSTEM_ID : " + sb.toString());
        } else if (GattAttributes.MANUFACTURE_FIRMWARE_REVISION.equals(characteristicID)) {
            mDevice.setFirmwareRevision(characteristic.getStringValue(0));

            Log.i("JKN", "MANUFACTURE_FIRMWARE_REVISION : " + characteristic.getStringValue(0));
        } else if (GattAttributes.MANUFACTURE_SOFTWRE_REVISION.equals(characteristicID)) {
            mDevice.setSoftwareRevision(characteristic.getStringValue(0));

            Log.i("JKN", "MANUFACTURE_FIRMWARE_REVISION : " + characteristic.getStringValue(0));
        } else if (GattAttributes.MANUFACTURE_HARDWARE_REVISION.equals(characteristicID)) {
            mDevice.setHardwareRevision(characteristic.getStringValue(0));

            Log.i("JKN", "MANUFACTURE_SOFTWRE_REVISION : " + characteristic.getStringValue(0));
        } else if (BLE_CHARACTERISTIC_UUID_Tx.equals(characteristicID)) {
            Log.i("JKN", "BLE_CHARACTERISTIC_UUID_Tx : " + characteristic.getStringValue(0));
        } else if (GattAttributes.BLE_CHARACTERISTIC_UUID_Rx.equals(characteristicID)) {
            byte[] buffer = characteristic.getValue();

//            if (m_sDebugSettings.GetShowStaticData())
//                decode_Static();
//            else
                decode_c(buffer);
        }

        if (mDeviceInformationBroadcastSent == false && mDevice.isDeviceInformationComplete()) {
            if (this.listener != null) {
                this.listener.deviceInformationRead(mDevice);
            }
        }
    }

    /*
     * The iOS stack uses a 18 byte array which only contains the 18 ECG sample bytes.
     * but here two more bytes have been added making it 20 to include the 2 serial bytes defined in the
     * ECGSamples class.
     */
    private static final byte[] ECG_FILLER_BYTES = {0x00, 0x00, 0x7F, (byte) 0xFF, (byte) 0xFF, 0x7F, (byte) 0xFF, (byte) 0xFF, 0x7F, (byte) 0xFF, (byte) 0xFF, 0x7F, (byte) 0xFF, (byte) 0xFF, 0x7F, (byte) 0xFF, (byte) 0xFF, 0x7F, (byte) 0xFF, (byte) 0xFF};

    private void sendFillerSamplesForMiscInfo(MiscInfo mMiscInfoData) {
        mLostBatchCount = mMiscInfoData.getSerial() - mExpectedSerial;

        EcgData ecgData = new EcgData(true);
        ecgData.setMiscInfo(mMiscInfoData);

        byte conf = mMiscInfoData.getConfigurationBitmask();
        if ((conf & SensorMode.CONFIGURATION_ECG1) > 0) {
            mEcg1Data = new ECGSamples(ECG_FILLER_BYTES);
            ecgData.setEcg1Data(mEcg1Data);
            // TODO - Add callback
        }
        if ((conf & SensorMode.CONFIGURATION_ECG2) > 0) {
            mEcg2Data = new ECGSamples(ECG_FILLER_BYTES);
            ecgData.setEcg2Data(mEcg2Data);
            // TODO - Add callback
        }
        if ((conf & SensorMode.CONFIGURATION_ECG3) > 0) {
            mEcg3Data = new ECGSamples(ECG_FILLER_BYTES);
            ecgData.setEcg3Data(mEcg3Data);
            // TODO - Add callback
        }

        reportNewEcgData(ecgData);
    }

    private void reportNewEcgData(EcgData ecgData) {
        if (this.listener != null) {
            this.listener.ecgDataUpdated(ecgData);
        }
    }

    private ConnectionManager.EcgDataListener listener = null;

    public void setEcgDataListener(ConnectionManager.EcgDataListener listener) {
        this.listener = listener;
    }

}

