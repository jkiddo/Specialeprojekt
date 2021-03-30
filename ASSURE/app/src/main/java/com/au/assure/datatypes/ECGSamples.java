package com.au.assure.datatypes;

//package com.cortrium.opkit.datatypes;

import android.os.Parcel;
import android.os.Parcelable;

import com.au.assure.Utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * Copyright (C) 2017 Cortrium Aps
 * <br><br>
 * Class ECGSamples
 * <br><br>
 */
public class ECGSamples extends CortriumMessage implements Parcelable
{
    public static final short NUMBER_OF_SAMPLES = 6;
    public static final short BYTES_PER_SAMPLE = 3;

    /**
     * The number of bytes expected in a ECGSamples message.
     *  Length = unsigned 16 bit value + (NUMBER_OF_SAMPLES * BYTES_PER_SAMPLE * unsigned 8 bit values)
     */
    public static final int LENGTH = 2 + (NUMBER_OF_SAMPLES * BYTES_PER_SAMPLE * 1);

    private int serialNumber;
    private byte[] ecgDataBytes = new byte[NUMBER_OF_SAMPLES * BYTES_PER_SAMPLE];

    public ECGSamples(byte[] byteData)
    {
        super(MessageType.EcgSamples, byteData);

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
            serialNumber = buffer.getShort() & 0xffff;
            int i = 2;
            for (int index = 0; index < ecgDataBytes.length; index++)
            {
                ecgDataBytes[index] = byteData[i++];
            }
        }
    }

    protected ECGSamples(Parcel in)
    {
        super(in);

        serialNumber = in.readInt();
        ecgDataBytes = in.createByteArray();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        super.writeToParcel(dest, flags);

        dest.writeInt(serialNumber);
        dest.writeByteArray(ecgDataBytes);
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    public static final Creator<ECGSamples> CREATOR = new Creator<ECGSamples>()
    {
        @Override
        public ECGSamples createFromParcel(Parcel in)
        {
            return new ECGSamples(in);
        }

        @Override
        public ECGSamples[] newArray(int size)
        {
            return new ECGSamples[size];
        }
    };

    public int getSerialNumber()
    {
        return serialNumber;
    }

    public byte[] getEcgDataBytes()
    {
        return ecgDataBytes;
    }

    public int[] getRawEcgValues()
    {
        int[] rawSamples = new int[NUMBER_OF_SAMPLES];
        byte[] sampleBuffer;

        int offset = 0;
        for (int index = 0; index < NUMBER_OF_SAMPLES; index++)
        {
            sampleBuffer = Arrays.copyOfRange(ecgDataBytes, offset, offset + 3);
            rawSamples[index] = Utils.convertSampleValue(sampleBuffer);
            offset += 3;
        }

        return  rawSamples;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("ECG[");
        builder.append("Serial:").append(this.serialNumber);
        builder.append("|ECG:");
        for (int index = 0; index < ecgDataBytes.length; index++)
        {
            if (index > 0) builder.append(", ");
            builder.append(String.format("0x%02X", ecgDataBytes[index]));
        }
        builder.append("]");
        return builder.toString();
    }
}

