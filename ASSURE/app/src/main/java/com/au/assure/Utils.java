package com.au.assure;

//package com.cortrium.opkit;

import java.util.Date;

/**
 * Copyright (C) 2002 - 2016 Docobo Ltd.
 * <br><br>
 * Class Utils
 * <br><br>
 * TODO - Class summary
 */
public class Utils
{
    public static String toHexString(byte[] byteData)
    {
        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < byteData.length; index++)
        {
            if (index > 0)
                builder.append(", ");
            builder.append(String.format("0x%02X", byteData[index]));
        }

        return builder.toString();
    }

    /**
     * Convert temperature value from Kelvin to Celcius.
     *
     * @param temperatureInKelvin
     * @return
     */
    public static float convertKelvinToCelcius(int temperatureInKelvin)
    {
        return (temperatureInKelvin * 0.02f) - 273.15f;
    }

    /**
     * Converts the 3 byte sample value to 32 bit integer
     *
     * @param byteArray
     * @return
     */
    public static int convertSampleValue(byte[] byteArray)
    {
        int value = 0;

        value |= (byteArray[0] & 0xff) << 8;
        value |= (byteArray[1] & 0xff) << 16;
        value |= (byteArray[2] & 0xff) << 24;

        return  value;
    }

    public static boolean filenameIsDateString(String filename)
    {
        int filenameCount = 0;

        for (int i = 0; i < filename.length(); i++)
        {
            // Break loop if \0 is found - It's a C style string
            if (filename.charAt(i) == '\u0000') break;
            filenameCount++;
        }

        return filenameCount >= 12;
    }

    public static String filenameFromSerial(long serial)
    {
        double timeIntervalSinceRecordingStart = serial / 25;
        double timeIntervalSince1970 = System.currentTimeMillis() / 1000l;
        int start = new Double(timeIntervalSince1970 - timeIntervalSinceRecordingStart).intValue();

        String filename = String.format("%s.BLE", Integer.toHexString(start).toUpperCase());

        return filename;
    }
}

