package com.au.assure;

//package com.cortrium.opkit;

import android.bluetooth.BluetoothDevice;

/**
 * Copyright (C) 2002 - 2016 Docobo Ltd.
 * <br><br>
 * Class CortriumC3
 * <br><br>
 * TODO - Class summary
 */
public class CortriumC3
{
    public static enum DeviceModes
    {
        DeviceModeIdle,
        DeviceModeActive,
        DeviceModeStandAlone,
        DeviceModeFile,
        DeviceModeFileActive,
        DeviceModeDisconnect;
    }

    public static enum EcgChannel
    {
        EcgChannel1,
        EcgChannel2,
        EcgChannel3,
    }

    /*
     * The accelerometer mapping divisor.
     * The full scale (FS) for the accelerometer is 2G for 15-bits
     */
    public static final float ACCELEROMETER_MAPPING_DIVISOR = ((float) (1 << 15) / 2 );

    public static final double SAMPLE_RATE_RESP = 41.6666;
    public static final int SAMPLE_RATE_ECG = 250;

    public static final int SAMPLE_MAX_VALUE = 32767;
    public static final int SAMPLE_MIN_VALUE = -32765;
    public static final int SAMPLE_FILTER_ERROR = -32766;
    public static final int SAMPLE_LEAD_OFF = -32767;
    public static final int SAMPLE_COMM_ERR = -32768;

    private String firmwareRevision;
    private String softwareRevision;
    private String hardwareRevision;
    private String deviceSerial;
    private DeviceModes deviceMode;

    private byte configuration;
    private String filename;

    private ServiceManager mServiceManager;

    private final BluetoothDevice bluetoothDevice;

    public CortriumC3(BluetoothDevice bluetoothDevice)
    {
        if (bluetoothDevice == null)
            throw new NullPointerException("BluetoothDevice cannot be null");

        this.bluetoothDevice = bluetoothDevice;
    }

    protected void registerServiceManager(ServiceManager serviceManager)
    {
        mServiceManager = serviceManager;
    }

    public BluetoothDevice getBluetoothDevice()
    {
        return bluetoothDevice;
    }

    protected void setFilename(String filename) { this.filename = filename; }
    protected String getFilename() { return this.filename; }

    protected void setConfiguration(byte configuration)
    {
        this.configuration = configuration;
    }

    protected byte getConfiguration()
    {
        return this.configuration;
    }

    public String getFirmwareRevision()
    {
        return firmwareRevision;
    }

    public void setFirmwareRevision(String firmwareRevision)
    {
        this.firmwareRevision = firmwareRevision;
    }

    public String getSoftwareRevision()
    {
        return softwareRevision;
    }

    public void setSoftwareRevision(String softwareRevision)
    {
        this.softwareRevision = softwareRevision;
    }

    public String getHardwareRevision()
    {
        return hardwareRevision;
    }

    public void setHardwareRevision(String hardwareRevision)
    {
        this.hardwareRevision = hardwareRevision;
    }

    public String getDeviceSerial()
    {
        return deviceSerial;
    }

    public void setDeviceSerial(String deviceSerial)
    {
        this.deviceSerial = deviceSerial;
    }

    public DeviceModes getDeviceMode()
    {
        return deviceMode;
    }

    public void setDeviceMode(DeviceModes deviceMode)
    {
        this.deviceMode = deviceMode;
    }

    public boolean isDeviceInformationComplete()
    {
 //       Log.i("JKN","isDeviceInformationComplete()");
        if (this.deviceSerial == null)
            return false;
        if (this.softwareRevision == null)
            return false;
        if (this.hardwareRevision == null)
            return false;
        if (this.firmwareRevision == null)
            return false;

//        Log.i("JKN","isDeviceInformationComplete() TRUE");

        return true;
    }

    public String getName()
    {
        return this.bluetoothDevice.getName();
    }

    public String getAddress()
    {
        return this.bluetoothDevice.getAddress();
    }

    public void changeMode(DeviceModes mode)
    {
        mServiceManager.changeSensorMode(mode);
    }

    @Override
    public String toString()
    {
        return String.format("%s[%s]", this.bluetoothDevice.getName(), this.bluetoothDevice.getAddress());
    }
}

