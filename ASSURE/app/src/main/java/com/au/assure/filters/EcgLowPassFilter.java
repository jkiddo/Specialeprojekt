package com.au.assure.filters;

//package com.cortrium.opkit.filters;

import java.util.Arrays;

/**
 * Copyright (C) 2002 - 2016 Docobo Ltd.
 * <br><br>
 * Class EcgLowPassFilter
 * <br><br>
 * TODO - Class summary
 */
public class EcgLowPassFilter extends LowPassFilter
{
    private static final short[] FILTER_TAPS_ECG_250_SPS = {
            12,
            131,
            21,
            -16,
            -47,
            -28,
            25,
            58,
            33,
            -32,
            -71,
            -38,
            41,
            86,
            44,
            -51,
            -103,
            -50,
            62,
            121,
            57,
            -76,
            -143,
            -64,
            92,
            166,
            71,
            -111,
            -193,
            -78,
            132,
            223,
            85,
            -157,
            -257,
            -93,
            186,
            294,
            100,
            -220,
            -337,
            -107,
            259,
            386,
            114,
            -306,
            -442,
            -121,
            361,
            506,
            127,
            -426,
            -583,
            -133,
            507,
            675,
            139,
            -607,
            -788,
            -144,
            735,
            933,
            149,
            -905,
            -1127,
            -153,
            1145,
            1403,
            156,
            -1510,
            -1836,
            -159,
            2139,
            2626,
            160,
            -3508,
            -4577,
            -162,
            8942,
            18142,
            22007,
            18142,
            8942,
            -162,
            -4577,
            -3508,
            160,
            2626,
            2139,
            -159,
            -1836,
            -1510,
            156,
            1403,
            1145,
            -153,
            -1127,
            -905,
            149,
            933,
            735,
            -144,
            -788,
            -607,
            139,
            675,
            507,
            -133,
            -583,
            -426,
            127,
            506,
            361,
            -121,
            -442,
            -306,
            114,
            386,
            259,
            -107,
            -337,
            -220,
            100,
            294,
            186,
            -93,
            -257,
            -157,
            85,
            223,
            132,
            -78,
            -193,
            -111,
            71,
            166,
            92,
            -64,
            -143,
            -76,
            57,
            121,
            62,
            -50,
            -103,
            -51,
            44,
            86,
            41,
            -38,
            -71,
            -32,
            33,
            58,
            25,
            -28,
            -47,
            -16,
            21,
            131,
            12
    };

    private final double NRCOEFF             = 0.987488;
    private final int    FILTER_ORDER        = 161;
    private final int    SAMPLE_FILTER_ERROR = -32766;

    private int     bufferStart;
    private int     bufferCurrent;
    private short[] workingBuffer = new short[2 * FILTER_ORDER];
    private short[] coefficient   = Arrays.copyOf(FILTER_TAPS_ECG_250_SPS, FILTER_TAPS_ECG_250_SPS.length);

    public EcgLowPassFilter()
    {
//		bufferStart = 0;
//		bufferCurrent = FILTER_ORDER - 1;
    }

    @Override
    public short filterInput(short input)
    {
        short sample = input;

        // Implementation of FIR
        // Store the DC-Removed value in working buffer in millivolts range
        workingBuffer[bufferCurrent] = sample;
        short result = filterProcess(workingBuffer, bufferCurrent, coefficient);

        // Store the DC removed value in workingBuf buffer in millivolts range
        workingBuffer[bufferStart] = sample;

        bufferStart++;
        bufferCurrent++;

        if (bufferStart == FILTER_ORDER - 1)
        {
            bufferStart = 0;
            bufferCurrent = FILTER_ORDER - 1;
        }

        return result;
    }

    // TODO - verify whether this is correct
    private short filterProcess(short[] workingBuffer, int offset, short[] coefficientBuffer)
    {
        int result = 0;
        for (int index = 0; index < FILTER_ORDER; )
        {
            if (offset < 0) offset = FILTER_ORDER - 1;
            result += workingBuffer[offset--] * coefficientBuffer[index++];
        }

        return (short) (result / 65536);
    }
}

