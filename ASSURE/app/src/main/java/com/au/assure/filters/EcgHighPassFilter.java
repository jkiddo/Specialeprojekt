package com.au.assure.filters;

//package com.cortrium.opkit.filters;

/**
 * Copyright (C) 2002 - 2016 Docobo Ltd.
 * <br><br>
 * Class EcgHighPassFilter
 * <br><br>
 * TODO - Class summary
 */
public class EcgHighPassFilter extends HighPassFilter
{
    private final int MAX_INT_24   = 8388607;
    private final int MIN_INT_24   = -8388608;
    private final int FILTER_ERROR = -32766;

    private int iir_X_Prev;
    private int iir_Y_Prev;

    public EcgHighPassFilter()
    {
        resetFilter();
    }

    public void resetFilter()
    {
        iir_X_Prev = 0;
        iir_Y_Prev = 0;
    }

    @Override
    public short filterInput(int input)
    {
        int sample = input / 4;

        int temp = iir_Y_Prev / 128;
        temp = iir_Y_Prev - temp;
        iir_Y_Prev = (sample - iir_X_Prev) + temp;
        iir_X_Prev = sample;

        if (iir_Y_Prev > MAX_INT_24)
        {
            iir_Y_Prev = MAX_INT_24;
            sample = FILTER_ERROR;
        }
        else if (iir_Y_Prev < MIN_INT_24)
        {
            iir_Y_Prev = MIN_INT_24;
            sample = FILTER_ERROR;
        }
        else
        {
            //sample = iir_Y_Prev / 256;
            sample = iir_Y_Prev / 1;
        }

        return (short) sample;
    }
}

