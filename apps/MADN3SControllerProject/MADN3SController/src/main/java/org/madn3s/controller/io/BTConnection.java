package org.madn3s.controller.io;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Set;
import java.util.UUID;

/**
 * Created by inaki on 12/7/13.
 */
public class BTConnection {

//    public final static String NXT_MAC_ADDRESS = "00:16:53:02:0E:EC";
//    public final static String NXT_MAC_ADDRESS = "5C:B5:24:C9:A2:33";
    public final static String NXT_MAC_ADDRESS = "30:39:26:63:6F:36";

    static BTConnection instance;
    BluetoothAdapter localAdapter;
//    BluetoothServerSocket nxtSocket;
    public static Set<BluetoothDevice> pairedDevices;
    public static Set<BluetoothDevice> newDevices;
    BluetoothSocket nxtSocket;
    boolean success=false;

    private BTConnection(){
        localAdapter=BluetoothAdapter.getDefaultAdapter();
        setPairedDevices(localAdapter.getBondedDevices());
        setNewDevices(localAdapter.getBondedDevices());
    }

    public static BTConnection getInstance(){
        if(instance == null) instance = new BTConnection();
        return instance;
    }

    public static Set<BluetoothDevice> getNewDevices() {
        return newDevices;
    }

    public static void setNewDevices(Set<BluetoothDevice> newDevices) {
        BTConnection.newDevices = newDevices;
    }

    //Enables Bluetooth if not enabled
    private void enableBT(){
        if(!localAdapter.isEnabled()){
            localAdapter.enable();
        }
    }

    public void doDiscovery() {
        enableBT();
        if (localAdapter.isDiscovering()) localAdapter.cancelDiscovery();
        localAdapter.startDiscovery();
    }

    public void cancelDiscovery(){
        localAdapter.cancelDiscovery();
    }

    public boolean startMindstormConnection(){
        enableBT();
        Log.d("DEBUG","Conectando a "+NXT_MAC_ADDRESS);
        //get the BluetoothDevice of the NXT
        BluetoothDevice nxtBTDevice = localAdapter.getRemoteDevice(NXT_MAC_ADDRESS);

        //try to connect to the nxt
        try {
//            nxtSocket = localAdapter.listenUsingRfcommWithServiceRecord("MADN3S",UUID
//                    .fromString("00001101-0000-1000-8000-00805F9B34FB"));
//            nxtSocket.accept();

            nxtSocket = nxtBTDevice.createRfcommSocketToServiceRecord(UUID
                    .fromString("00001101-0000-1000-8000-00805F9B34FB"));

            if(nxtSocket != null) success = true;
        } catch (IOException e) {
            Log.d("Bluetooth","Err: Device not found or cannot connect");
            success=false;
        }finally{
            Log.d("Bluetooth", "isConnected() : " + String.valueOf(nxtSocket.isConnected()));
            Log.d("DEBUG", "Direccion desde el socket: "+localAdapter.getRemoteDevice(NXT_MAC_ADDRESS).getAddress());
            Log.d("DEBUG", "Clase desde el socket: "+localAdapter.getRemoteDevice(NXT_MAC_ADDRESS).getBluetoothClass());
        }

        return success;
    }


    public void writeMessage(byte msg) throws InterruptedException{
        if(nxtSocket!=null){
            try {
                OutputStreamWriter out = new OutputStreamWriter(nxtSocket.getOutputStream());
                out.write(msg);
                out.flush();


            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            //Error
        }
    }

    public int readMessage(String nxt){
        int n;
        if(nxtSocket!=null){
            try {
                InputStreamReader in=new InputStreamReader(nxtSocket.getInputStream());
                n=in.read();
                return n;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return -1;
            }
        }else{
            //Error
            return -1;
        }
    }

    public boolean isConnected(){
        return nxtSocket.isConnected();
    }

    public Set<BluetoothDevice> getPairedDevices() {
        return pairedDevices;
    }

    public void setPairedDevices(Set<BluetoothDevice> pairedDevices) {
        this.pairedDevices = pairedDevices;
    }


}
