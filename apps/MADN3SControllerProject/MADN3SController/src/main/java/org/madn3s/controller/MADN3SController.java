package org.madn3s.controller;

import android.app.Application;
import android.bluetooth.BluetoothDevice;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by inaki on 1/11/14.
 */
public class MADN3SController extends Application {
    public static BluetoothDevice nxt;
    public static ArrayList<BluetoothDevice> cameras = new ArrayList<BluetoothDevice>();
    public static final String SERVICE_NAME ="MADN3S";
    public static final UUID APP_UUID = UUID.fromString("65da7fe0-8b80-11e3-baa8-0800200c9a66");
}
