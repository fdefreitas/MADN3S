package org.madn3s.controller;

import android.app.Application;
import android.bluetooth.BluetoothDevice;

import java.util.ArrayList;

/**
 * Created by inaki on 1/11/14.
 */
public class MADN3SController extends Application {
    public static BluetoothDevice nxt;
    public static ArrayList<BluetoothDevice> cameras = new ArrayList<BluetoothDevice>();
}
