package com.au.assure;

/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.HashMap;
import java.util.UUID;

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */
public class GattAttributes
{
    private static HashMap<String, String> attributes = new HashMap<String, String>();

    public static final UUID GENERIC_INFORMATION_SERVICE     = UUID.fromString("00001800-0000-1000-8000-00805f9b34fb");
    public static final UUID DEVICE_NAME                     = UUID.fromString("00002a00-0000-1000-8000-00805f9b34fb");
    public static final UUID DEVICE_APPEARANCE               = UUID.fromString("00002a01-0000-1000-8000-00805f9b34fb");
    public static final UUID PERIPHERAL_PRIVACY_FLAG         = UUID.fromString("00002a02-0000-1000-8000-00805f9b34fb");
    public static final UUID RECONNECT_ADDRESS               = UUID.fromString("00002a03-0000-1000-8000-00805f9b34fb");
    public static final UUID PREFERRED_CONNECTION_PARAMETERS = UUID.fromString("00002a04-0000-1000-8000-00805f9b34fb");

    public static final UUID DEVICE_INFORMATION_SERVICE    = UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb");
    public static final UUID MANUFACTURE_NAME              = UUID.fromString("00002a29-0000-1000-8000-00805f9b34fb");
    public static final UUID MANUFACTURE_MODEL             = UUID.fromString("00002a24-0000-1000-8000-00805f9b34fb");
    public static final UUID MANUFACTURE_SERIAL_NUMBER     = UUID.fromString("00002a25-0000-1000-8000-00805f9b34fb");
    public static final UUID MANUFACTURE_FIRMWARE_REVISION = UUID.fromString("00002a26-0000-1000-8000-00805f9b34fb");
    public static final UUID MANUFACTURE_HARDWARE_REVISION = UUID.fromString("00002a27-0000-1000-8000-00805f9b34fb");
    public static final UUID MANUFACTURE_SOFTWRE_REVISION  = UUID.fromString("00002a28-0000-1000-8000-00805f9b34fb");
    public static final UUID MANUFACTURE_SYSTEM_ID         = UUID.fromString("00002a23-0000-1000-8000-00805f9b34fb");
    public static final UUID IEEE_CERITIFICATION_DATA      = UUID.fromString("00002a2a-0000-1000-8000-00805f9b34fb");
    public static final UUID PLUG_AND_PLAY_ID              = UUID.fromString("00002a50-0000-1000-8000-00805f9b34fb");

    public static final UUID GENERIC_ATTRIBUTES_SERVICE = UUID.fromString("00001801-0000-1000-8000-00805f9b34fb");
    public static final UUID SERVICE_CHANGED            = UUID.fromString("00002a05-0000-1000-8000-00805f9b34fb");

    public static final UUID USER_CHARACTERISTIC_CONFIG   = UUID.fromString("00002901-0000-1000-8000-00805f9b34fb");
    public static final UUID CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    public static final UUID CONTRIUM_C3_DATA_SERVICE = UUID.fromString("0000ffc0-0000-1000-8000-00805f9b34fb");
    public static final UUID CONTRIUM_C3_MISC_DATA    = UUID.fromString("0000ffc1-0000-1000-8000-00805f9b34fb");
    public static final UUID CONTRIUM_C3_ECG1_DATA    = UUID.fromString("0000ffc3-0000-1000-8000-00805f9b34fb");
    public static final UUID CONTRIUM_C3_ECG2_DATA    = UUID.fromString("0000ffc4-0000-1000-8000-00805f9b34fb");
    public static final UUID CONTRIUM_C3_ECG3_DATA    = UUID.fromString("0000ffc5-0000-1000-8000-00805f9b34fb");

    public static final UUID CONTRIUM_C3_MISC_FILE = UUID.fromString("0000ffd1-0000-1000-8000-00805f9b34fb");
    public static final UUID CONTRIUM_C3_ECG1_FILE = UUID.fromString("0000ffd3-0000-1000-8000-00805f9b34fb");
    public static final UUID CONTRIUM_C3_ECG2_FILE = UUID.fromString("0000ffd4-0000-1000-8000-00805f9b34fb");
    public static final UUID CONTRIUM_C3_ECG3_FILE = UUID.fromString("0000ffd5-0000-1000-8000-00805f9b34fb");

    public static final UUID CONTRIUM_C3_MODE = UUID.fromString("0000ffcc-0000-1000-8000-00805f9b34fb");

    public static final UUID CONTRIUM_C3_FILE_COMMAND = UUID.fromString("0000ffcd-0000-1000-8000-00805f9b34fb");
    public static final UUID CONTRIUM_C3_FILE_INFO    = UUID.fromString("0000ffce-0000-1000-8000-00805f9b34fb");
    public static final UUID CONTRIUM_C3_FILE_STATUS  = UUID.fromString("0000ffca-0000-1000-8000-00805f9b34fb");

    public static final UUID CONTRIUM_C3_UNITS = UUID.fromString("0000ffaa-0000-1000-8000-00805f9b34fb");

    // JKN 22/8-2019                                                       6e400001-b5a3-f393-e0a9-e50e24dcca9e
    // https://intersog.com/blog/tech-tips/how-to-work-properly-with-bt-le-on-android/
    public static final UUID BLE_SERVICE_UUID_C3TESTER  = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    public static final UUID BLE_CHARACTERISTIC_UUID_Tx = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
    public static final UUID BLE_CHARACTERISTIC_UUID_Rx = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");

    static
    {
        // Generic Service and Characteristics
        attributes.put(GENERIC_INFORMATION_SERVICE.toString(), "Generic Access Info");
        attributes.put(DEVICE_NAME.toString(), "Device Name");
        attributes.put(DEVICE_APPEARANCE.toString(), "Device Appearance");
        attributes.put(PERIPHERAL_PRIVACY_FLAG.toString(), "Peripheral Privacy Flags");
        attributes.put(RECONNECT_ADDRESS.toString(), "Reconnect Address");
        attributes.put(PREFERRED_CONNECTION_PARAMETERS.toString(), "Preferred Connection Params");

        // Device Information Service and supported characteristics
        attributes.put(DEVICE_INFORMATION_SERVICE.toString(), "Device Information Service");
        attributes.put(MANUFACTURE_NAME.toString(), "Manufacturer Name");
        attributes.put(MANUFACTURE_MODEL.toString(), "Manufacturer Model");
        attributes.put(MANUFACTURE_SERIAL_NUMBER.toString(), "Manufacturer Serial Number");
        attributes.put(MANUFACTURE_FIRMWARE_REVISION.toString(), "Manufacturer Firmware Rev.");
        attributes.put(MANUFACTURE_HARDWARE_REVISION.toString(), "Manufacturer Hardware Rev.");
        attributes.put(MANUFACTURE_SOFTWRE_REVISION.toString(), "Manufacturer Software Rev.");
        attributes.put(MANUFACTURE_SYSTEM_ID.toString(), "Manufacturer System ID");
        attributes.put(IEEE_CERITIFICATION_DATA.toString(), "IEEE Reg. Certification Data");
        attributes.put(PLUG_AND_PLAY_ID.toString(), "PnP ID");

        // Generic Attributes
        attributes.put(GENERIC_ATTRIBUTES_SERVICE.toString(), "Generic Attribute Info");
        attributes.put(SERVICE_CHANGED.toString(), "Service Changed Info");

        // Contrium C3 device service
        attributes.put(CONTRIUM_C3_DATA_SERVICE.toString(), "C3 Data Service");
        attributes.put(CONTRIUM_C3_MISC_DATA.toString(), "Misc");
        attributes.put(CONTRIUM_C3_ECG1_DATA.toString(), "ECG1");
        attributes.put(CONTRIUM_C3_ECG2_DATA.toString(), "ECG2");
        attributes.put(CONTRIUM_C3_ECG3_DATA.toString(), "ECG3");

        attributes.put(CONTRIUM_C3_MISC_FILE.toString(), "Misc File");
        attributes.put(CONTRIUM_C3_ECG1_FILE.toString(), "ECG1 File");
        attributes.put(CONTRIUM_C3_ECG2_FILE.toString(), "ECG2 File");
        attributes.put(CONTRIUM_C3_ECG3_FILE.toString(), "ECG3 File");

        attributes.put(CONTRIUM_C3_MODE.toString(), "Operation Mode");

        attributes.put(CONTRIUM_C3_FILE_COMMAND.toString(), "File Command");
        attributes.put(CONTRIUM_C3_FILE_INFO.toString(), "File Info");
        attributes.put(CONTRIUM_C3_FILE_STATUS.toString(), "File Status");

        attributes.put(CONTRIUM_C3_UNITS.toString(), "Units");
        // End Contrium C3 device service

        // JKN 22/8-2019 for C3+
        attributes.put(BLE_SERVICE_UUID_C3TESTER.toString(), "BLE_SERVICE_UUID_C3TESTER");
        attributes.put(BLE_CHARACTERISTIC_UUID_Tx.toString(), "BLE_CHARACTERISTIC_UUID_Tx Write without response");
        attributes.put(BLE_CHARACTERISTIC_UUID_Rx.toString(), "BLE_CHARACTERISTIC_UUID_Rx Read/Notify");
        // JKN 22/8-2019 END
    }

    public static String lookup(String uuid, String defaultName)
    {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}

