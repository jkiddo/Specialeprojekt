package com.au.assure.datatypes;

//package com.cortrium.opkit.datatypes;

import android.util.Log;

/**
 * Copyright (C) 2002 - 2016 Docobo Ltd.
 * <br><br>
 * Class FileCommand
 * <br><br>
 * TODO - Class summary
 */
public class FileCommand
{
    /**
     * The number of bytes expected in a FileCommand message.
     *  Length = 4 * unsigned bytes + FILE_NAME_LENGTH
     */
    public static final int LENGTH = 17;

    /**
     * The maximum filename length.
     */
    public static final int FILE_NAME_LENGTH = 8 + 1 + 3 + 1;

    public static final short FILE_CMD_DIR        = 1;
    public static final short FILE_CMD_OPEN       = 2;
    public static final short FILE_CMD_READ       = 3;
    public static final short FILE_CMD_CLOSE      = 4;
    public static final short FILE_CMD_DELETE     = 5;
    public static final short FILE_CMD_DELETE_ALL = 6;

    private final short command;
    //private short[] filler = new short[3]; // to ensure serial structure is aligned properly
    /*
     * File name string with max lenght FILE_NAME_LENGTH
     * - This is only used when using FILE_CMD_OPEN or FILE_CMD_DELETE
     */
    private String fileName = null;
    /*
     * The start and end index is only used with FILE_CMD_READ
     */
    private long startIndex;
    private long endIndex;

    /**
     * @param command
     */
    public FileCommand(short command)
    {
        switch (command)
        {
            case FILE_CMD_DIR:
            case FILE_CMD_OPEN:
            case FILE_CMD_READ:
            case FILE_CMD_CLOSE:
            case FILE_CMD_DELETE:
            case FILE_CMD_DELETE_ALL:
            {
                this.command = command;
                break;
            }
            default:
            {
                Log.e("FileCommand", command + " is not a supported command");
                throw new IllegalArgumentException(command + " is not a valid command");
            }
        }
    }

    public short getCommand()
    {
        return command;
    }

    public String getFileName()
    {
        return fileName;
    }

    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }

    public long getStartIndex()
    {
        return startIndex;
    }

    public void setStartIndex(long startIndex)
    {
        this.startIndex = startIndex;
    }

    public long getEndIndex()
    {
        return endIndex;
    }

    public void setEndIndex(long endIndex)
    {
        this.endIndex = endIndex;
    }
}

