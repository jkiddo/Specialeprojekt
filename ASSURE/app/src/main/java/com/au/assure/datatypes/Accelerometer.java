package com.au.assure.datatypes;

//package com.cortrium.opkit.datatypes;

/**
 * Created by hsk on 26/09/16.
 */


public final class Accelerometer {
    public enum DeviceOrientations
    {
        Upright,
        Back,
        Front,
        Head,
        Left,
        Right,
        Unknown
    }

    public static String deviceOrientationToString(DeviceOrientations deviceOrientation)
    {
        switch (deviceOrientation)
        {
            case Upright:
                return "Upright";
            case Back:
                return "Back";
            case Front:
                return "Front";
            case Head:
                return "Head";
            case Left:
                return "Left";
            case Right:
                return "Right";
            default:
                return "Unknown";
        }
    }

    public static DeviceOrientations getDeviceOrientation(float accX, float accY, float accZ)
    {
        float magnitude =  (float) Math.sqrt(accX * accX + accY * accY + accZ * accZ);

        if (magnitude > 0.90f && magnitude < 1.10f)
        {
            // Angle when rotating around X
            float tanX = accZ / accY;
            float angleX = (float) Math.atan(tanX) * (float)(180.0f / Math.PI);
            angleX = Math.abs(Math.round(angleX / 5.0f) * 5.0f);

            if (accY <= 0.0f)
            {
                if (accZ > 0.0f) angleX = -angleX;
            }
            else
            {
                if (accZ < 0.0f)
                    angleX = 180.0f - angleX;
                else
                    angleX = angleX - 180.0f;
            }

            // Angle when rotating around Z
            float tanZ = accX / accY;
            float angleZ = (float) Math.atan(tanZ) * (float)(180.0f / Math.PI);
            angleZ = Math.abs(Math.round(angleZ / 5.0f) * 5.0f);

            if (accY <= 0.0f)
            {
                if (accX > 0.0f)
                    angleZ = -angleZ;
            }
            else
            {
                if (accX < 0.0f)
                    angleZ = 180.0f - angleZ;
                else
                    angleZ = angleZ - 180.0f;
            }

            if (angleX >= -20.0f && angleX <= 20.0f && angleZ >= -20.0f && angleZ <= 20.0f)
                return DeviceOrientations.Upright;
            else if (angleX >= 70.0f && angleX <= 110.0f)
                return DeviceOrientations.Back;
            else if (angleX <= -70.0f && angleX >= -110.0f)
                return DeviceOrientations.Front;
            else if ((angleX <= -160.0f || angleX >= 160.0f) && (angleZ <= -160.0f || angleZ >= 160.0f))
                return DeviceOrientations.Head;
            else if (angleZ >= 70.0f && angleZ <= 110.0f)
                return DeviceOrientations.Left;
            else if (angleZ <= -70.0f && angleZ >= -110.0f)
                return DeviceOrientations.Right;
            else
                return DeviceOrientations.Unknown;
        }

        return DeviceOrientations.Unknown;
    }
}

