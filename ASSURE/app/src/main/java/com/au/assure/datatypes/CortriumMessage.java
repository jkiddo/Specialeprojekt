package com.au.assure.datatypes;

//package com.cortrium.opkit.datatypes;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Copyright (C) 2002 - 2016 Docobo Ltd.
 * <br><br>
 * Class ContirumMessage
 * <br><br>
 * TODO - Class summary
 */
public abstract class CortriumMessage implements Parcelable
{
    public enum MessageType
    {
        unknown,
        EcgSamples,
        FileCommand,
        FileInfo,
        FileStatus,
        MiscInfo,
        SensorMode,
        Units;
    }

    private final MessageType messageType;
    private final byte[] rawDataBytes;

    public CortriumMessage(MessageType messageType, byte[] rawDataBytes)
    {
        this.messageType = messageType;
        this.rawDataBytes = rawDataBytes;
    }

    protected CortriumMessage(Parcel in)
    {
        messageType = MessageType.values()[in.readInt()];
        rawDataBytes = in.createByteArray();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeInt(messageType.ordinal());
        dest.writeByteArray(rawDataBytes);
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    public MessageType getMessageType()
    {
        return messageType;
    }

    public byte[] getRawDataBytes()
    {
        return rawDataBytes;
    }

    public void packDataToBytes()
    {
        throw new UnsupportedOperationException("Not supported for message type: " + messageType);
    }
}

