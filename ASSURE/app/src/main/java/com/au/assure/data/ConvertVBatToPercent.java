package com.au.assure.data;

import android.util.Log;

public final class ConvertVBatToPercent {

    private final static float [] fVoltage = { 4.121f , 3.969f , 3.856f , 3.753f , 3.668f , 3.609f , 3.572f , 3.537f , 3.497f , 3.438f , 3.305f };
    private final static float [] fHoursLeft = { 229.63f , 206.52f , 183.41f , 159.83f , 136.72f , 113.61f , 90.50f , 67.39f , 44.28f , 20.70f , 0.00f };
    private final static float ERROR_CODE_CONVERT_V_BAT_CALCULATION_FAILED = -1f;

    public static float Convert_to_Percent(float fVBat )
    {
        float fResult = 100f * Convert_to_Hours( fVBat ) / fHoursLeft[0];
        Log.i("JKN","VBat Converted Percent Left = " + fResult + "%" );
        return fResult;
    }

    public static float Convert_to_Hours(float fVBat )
    {
        int iCount = fVoltage.length;

        ///float fVCorrect = fVBat;// - fVoltage[iCount-1];

        for(int i=1;i<iCount;i++)
        {
            if( fVBat >= fVoltage[i] )
            {
                float fDiff = fVBat-fVoltage[i];
                float fDiff_X = fVoltage[i-1]-fVoltage[i];
                float fDiff_Y = fHoursLeft[i-1]- fHoursLeft[i];

                float fResult = fHoursLeft[i] + fDiff_Y * ( fDiff / fDiff_X );
                if( fResult > fHoursLeft[0] )
                    fResult = fHoursLeft[0];
                if( fResult < 0f )
                    fResult = 0f;
                Log.i("JKN","VBat Converted Hours Left = " + fResult + " hours" );
                return fResult;
            }
        }

        return ERROR_CODE_CONVERT_V_BAT_CALCULATION_FAILED;// -1f
    }
}
