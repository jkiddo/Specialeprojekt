package com.au.assure.datatypes;

//package com.cortrium.opkit.datatypes;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.au.assure.CortriumC3;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Copyright (C) 2002 - 2016 Docobo Ltd.
 * <br><br>
 * Class MiscInfo
 * <br><br>
 * TODO - Class summary
 */
public class MiscInfo extends CortriumMessage
{
    /**
     * The number of bytes expected in a MiscInfo message.
     *  Length = 4 serial bytes + 3 * 2 byte accelerometer values + 2 byte temp + 2 byte serialADS + 6 bytes
     */
    public static final int LENGTH = 20;

    /*
     * Message Serial number, incremented for every batch, i.e., 25 Hz
     *  - 32 bit unsigned integer
     */
    private long serial;

    /*
     * Accelerometer values for each axis
     *  - 16 bit signed integers
     */
    private int accelerometerRawX;
    private int accelerometerRawY;
    private int accelerometerRawZ;

    private Accelerometer.DeviceOrientations deviceOrientation;


    /*
     * Temperature value (Ambient or Object)
     *  o When (serial % 2 == 0) Ambient temperature
     *  o When (serial % 2 == 1) Object temperature
     *
     * - 16 bit unsigned integer
     */
    private int temperature;
    /*
     * Serial ADS - to report ADS packets not processed
     */
    private int serial_ADS;

    /*
     * Battery status bits containing
     *  - battery level - batteryStatus & 0x7F
     *  - chargerStat   - batteryStatus & 0x80
     */
    private byte batteryStatus;

    /*
     * Respiration Bytes
     *  - 3 unsigned bytes for respiration
     */
    private byte[] respirationBytes = new byte[3];
    /*
     * Lead off bits
     *  - 1 byte (unsigned 8 bit value) containing lead off indication
     */
    private byte leadOffBits;
    /*
     * Configuration Bitmask
     *  - 1 byte (unsigned 8 bit value) containing configuration bitmask
     */
    private byte configurationBitmask;

    public MiscInfo(byte[] byteData)
    {
        super(MessageType.MiscInfo, byteData);

        if (byteData == null)
        {
            throw new IllegalArgumentException("Ble data cannot be null");
        }
        else if (byteData.length != LENGTH)
        {
            throw new IllegalArgumentException(String.format("Ble data length is invalid (Expected: %d, Actual: %d)", LENGTH, byteData.length));
        }
        else
        {
            ByteBuffer buffer = ByteBuffer.wrap(byteData).order(ByteOrder.LITTLE_ENDIAN);
            serial = ((long) buffer.getInt() & 0xffffffff);

            accelerometerRawX = buffer.getShort();
            accelerometerRawY = buffer.getShort();
            accelerometerRawZ = buffer.getShort();

            deviceOrientation = Accelerometer.getDeviceOrientation(getAccelerometerX(), getAccelerometerY(), getAccelerometerZ());

            temperature = ((int) buffer.getShort() & 0xffff);
            serial_ADS = ((int) buffer.getShort() & 0xffff);

            batteryStatus = buffer.get();

            respirationBytes[0] = buffer.get();
            respirationBytes[1] = buffer.get();
            respirationBytes[2] = buffer.get();

            leadOffBits = buffer.get();
            configurationBitmask = buffer.get();
        }
    }

    protected MiscInfo(Parcel in)
    {
        super(in);

        serial = in.readLong();
        accelerometerRawX = in.readInt();
        accelerometerRawY = in.readInt();
        accelerometerRawZ = in.readInt();
        deviceOrientation =  Accelerometer.DeviceOrientations.values()[in.readInt()];
        temperature = in.readInt();
        serial_ADS = in.readInt();
        batteryStatus = in.readByte();
        respirationBytes = in.createByteArray();
        leadOffBits = in.readByte();
        configurationBitmask = in.readByte();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        super.writeToParcel(dest, flags);

        dest.writeLong(serial);
        dest.writeInt(accelerometerRawX);
        dest.writeInt(accelerometerRawY);
        dest.writeInt(accelerometerRawZ);
        dest.writeInt(deviceOrientation.ordinal());
        dest.writeInt(temperature);
        dest.writeInt(serial_ADS);
        dest.writeByte(batteryStatus);
        dest.writeByteArray(respirationBytes);
        dest.writeByte(leadOffBits);
        dest.writeByte(configurationBitmask);
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    public static final Parcelable.Creator<MiscInfo> CREATOR = new Parcelable.Creator<MiscInfo>()
    {
        @Override
        public com.au.assure.datatypes.MiscInfo createFromParcel(Parcel in)
        {
            return new com.au.assure.datatypes.MiscInfo(in);
        }

        @Override
        public com.au.assure.datatypes.MiscInfo[] newArray(int size)
        {
            return new com.au.assure.datatypes.MiscInfo[size];
        }
    };

    public long getSerial()
    {
        return serial;
    }

    public void SetAccelerometerRawX(int iNewAccelerometerRawX){accelerometerRawX = iNewAccelerometerRawX;}
    public void SetAccelerometerRawY(int iNewAccelerometerRawY){accelerometerRawY = iNewAccelerometerRawY;}
    public void SetAccelerometerRawZ(int iNewAccelerometerRawZ){accelerometerRawZ = iNewAccelerometerRawZ;}
    public void FindAccelerometerOrientation()
    {
        float accX = getAccelerometerX();
        float accY = getAccelerometerY();
        float accZ = getAccelerometerZ();
        deviceOrientation =  Accelerometer.getDeviceOrientation( accX,  accY, accZ );
        String strOr = Accelerometer.deviceOrientationToString(deviceOrientation);
        Log.i("JKN",strOr);
    }

    public int getAccelerometerRawX()
    {
        return accelerometerRawX;
    }

    public int getAccelerometerRawY()
    {
        return accelerometerRawY;
    }

    public int getAccelerometerRawZ()
    {
        return accelerometerRawZ;
    }

    public int getTemperature()
    {
        return temperature;
    }

    public int getSerial_ADS()
    {
        return serial_ADS;
    }

    public byte getBatteryStatus()
    {
        return batteryStatus;
    }

    public byte[] getRespirationBytes()
    {
        return respirationBytes;
    }

    public byte getLeadOffBits()
    {
        return leadOffBits;
    }

    public byte getConfigurationBitmask()
    {
        return configurationBitmask;
    }

    /*
     ************************************************************************
     * 																		*
     * 				Helper functions used to process the data.				*
     *																		*
     ************************************************************************
     */
    public boolean isAmbientTemperature()
    {
        return this.serial % 2 == 0;
    }

    public int getBatteryLevel()
    {
        return this.batteryStatus & 0x7F;
    }

    public float getAccelerometerX()
    {
        return this.accelerometerRawY / CortriumC3.ACCELEROMETER_MAPPING_DIVISOR;
    }

    public float getAccelerometerY()
    {
        return -this.accelerometerRawX / CortriumC3.ACCELEROMETER_MAPPING_DIVISOR;
    }

    public float getAccelerometerZ()
    {
        return this.accelerometerRawZ / CortriumC3.ACCELEROMETER_MAPPING_DIVISOR;
    }

    public Accelerometer.DeviceOrientations getDeviceOrientation()
    {
        return this.deviceOrientation;
    }
    public String getDeviceOrientationAsString() { return Accelerometer.deviceOrientationToString(this.deviceOrientation); }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("MiscInfo[");
        builder.append(String.format("Serial:%s|", serial));
        builder.append(String.format("Acc:X=%s,Y=%s,Z=%s, DeviceOrientation=%s|", accelerometerRawX, accelerometerRawY, accelerometerRawZ, deviceOrientation));
        builder.append(String.format("Temperature:Type=%s,Value:%d|", isAmbientTemperature() ? "Ambient" : "Object", this.temperature));
        builder.append(String.format("Respiration:[0x%02X, 0x%02X, 0x%02X]|", this.respirationBytes[0], this.respirationBytes[1], this.respirationBytes[2]));
        builder.append(String.format("leadOff:0x%02X|", this.leadOffBits));
        builder.append(String.format("Conf:0x%02X", this.configurationBitmask));
        builder.append("]");
        return builder.toString();
    }
}

