package com.au.assure.datatypes;

//package com.cortrium.opkit.datatypes;

import android.os.Parcel;
import android.os.Parcelable;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Copyright (C) 2002 - 2016 Docobo Ltd.
 * <br><br>
 * Class SensorMode
 * <br><br>
 * TODO - Class summary
 */
public class SensorMode implements Parcelable
{
    /**
     * The number of bytes expected in a SensorMode message.
     * - Length = 2 * 8 bit values + FILE_NAME_LENGTH
     */
    public static final int LENGTH = 2 + FileCommand.FILE_NAME_LENGTH;

    public static final int MODE_IDLE        = 0;
    public static final int MODE_ACTIVE      = 1;
    public static final int MODE_HOLTER      = 2;
    public static final int MODE_FILE        = 3;
    public static final int MODE_FILE_ACTIVE = 4;
    public static final int MODE_DISCONNECT  = 5;

    public static final int CONFIGURATION_NONE        = 0x00;
    public static final int CONFIGURATION_RESPIRATION = 0x01;
    public static final int CONFIGURATION_ECG1        = 0x02;
    public static final int CONFIGURATION_ECG2        = 0x04;
    public static final int CONFIGURATION_ECG3        = 0x08;
    public static final int CONFIGURATION_USDC        = 0x10; // Save data on uSDC
    public static final int CONFIGURATION_SEND_HALF   = 0x20;
    public static final int CONFIGURATION_ALL         = 0x1F;

    private byte  mode;            // MODE_IDLE or MODE_ACTIVE
    private byte  configuration;   // bitmask of CONF_*
    private String fileName;        // only used when MODE_ACTIVE. if empty string then no file is saved to memory

    public SensorMode(byte[] byteData)
    {
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
            ByteBuffer byteBuffer = ByteBuffer.wrap(byteData).order(ByteOrder.LITTLE_ENDIAN);
            mode = byteBuffer.get();
            configuration = byteBuffer.get();
            fileName = new String(byteData, 2, byteData.length - 2).trim();
        }
    }

    protected SensorMode(Parcel in)
    {
        mode = in.readByte();
        configuration = in.readByte();
        fileName = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeInt(mode);
        dest.writeInt(configuration);
        dest.writeString(fileName);
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    public static final Creator<SensorMode> CREATOR = new Creator<SensorMode>()
    {
        @Override
        public com.au.assure.datatypes.SensorMode createFromParcel(Parcel in)
        {
            return new com.au.assure.datatypes.SensorMode(in);
        }

        @Override
        public com.au.assure.datatypes.SensorMode[] newArray(int size)
        {
            return new com.au.assure.datatypes.SensorMode[size];
        }
    };

    public byte getMode()
    {
        return mode;
    }

    public byte getConfiguration()
    {
        return configuration;
    }

    public String getFileName()
    {
        return fileName;
    }

    public boolean isChannelEnabled(int channelNumber)
    {
        boolean result = false;

        switch (channelNumber)
        {
            case 1: result = (configuration & CONFIGURATION_ECG1) == CONFIGURATION_ECG1;
            case 2: result = (configuration & CONFIGURATION_ECG2) == CONFIGURATION_ECG2;
            case 3: result = (configuration & CONFIGURATION_ECG3) == CONFIGURATION_ECG3;
        }

        return result;
    }

    public static String getModeName(int mode)
    {
        switch (mode)
        {
            case MODE_IDLE:
                return "MODE_IDLE";
            case MODE_ACTIVE:
                return "MODE_ACTIVE";
            case MODE_HOLTER:
                return "MODE_HOLTER";
            case MODE_FILE:
                return "MODE_FILE";
            case MODE_FILE_ACTIVE:
            default:
                return "UNKNOWN";
        }
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("SensorMode[");
        builder.append("Mode:" + getModeName(this.mode));
        builder.append("|Conf:" + this.configuration);
        builder.append("|FileName:" + this.fileName);
        builder.append("]");
        return builder.toString();
    }
}
