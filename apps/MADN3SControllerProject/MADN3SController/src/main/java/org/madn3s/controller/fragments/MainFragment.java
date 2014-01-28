package org.madn3s.controller.fragments;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import org.madn3s.controller.R;
import org.madn3s.controller.io.BTConnection;
import org.madn3s.controller.models.DevicesAdapter;

import java.util.ArrayList;

/**
 * Created by inaki on 12/7/13.
 */
public class MainFragment extends BaseFragment{
    public static String EXTRA_DEVICE_ADDRESS = "device_address";

    ListView nxtNewDevicesListView, nxtPairedDevicesListView;
    ListView cameraNewDevicesListView, cameraPairedDevicesListView;
    ProgressBar nxtDiscoveryProgress, cameraDiscoveryProgress;
    DevicesAdapter nxtNewDevicesAdapter, nxtPairedDevicesAdapter;
    DevicesAdapter cameraNewDevicesAdapter, cameraPairedDevicesAdapter;
    ArrayList<BluetoothDevice> devices;
    private boolean nxtDevice;
    private int cams;
    public MainFragment() {
        TAG = "DEBUG "+this.getClass().getSimpleName();
        devices = new ArrayList<BluetoothDevice>();
        nxtDevice =  false;
        cams = 0;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated (View view, Bundle savedInstanceState){

        assert getActivity() != null;
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        getActivity().registerReceiver(mReceiver, filter);

        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        getActivity().registerReceiver(mReceiver, filter);

        //Obtener vistas de adapters
        nxtPairedDevicesListView = (ListView) getActivity().findViewById(R.id.nxt_paired_devices_listView);
        nxtNewDevicesListView = (ListView) getActivity().findViewById(R.id.nxt_new_devices_listView);
        nxtDiscoveryProgress = (ProgressBar) getActivity().findViewById(R.id.nxt_discovery_progressBar);

        cameraPairedDevicesListView = (ListView) getActivity().findViewById(R.id.camera_paired_devices_listView);
        cameraNewDevicesListView = (ListView) getActivity().findViewById(R.id.cameras_new_devices_listView);
        cameraDiscoveryProgress = (ProgressBar) getActivity().findViewById(R.id.cameras_discovery_progressBar);

        Button goButton = (Button) view.findViewById(R.id.scan_button);
        goButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BTConnection btc = BTConnection.getInstance();
//                btc.startMindstormConnection();
                btc.doDiscovery();
                nxtDiscoveryProgress.setVisibility(View.VISIBLE);
                cameraDiscoveryProgress.setVisibility(View.VISIBLE);

                byte b = "hallo!".getBytes()[0];
                try {
                    ArrayList<BluetoothDevice> pairedDevices = new ArrayList<BluetoothDevice>();
                    for(BluetoothDevice device:  BTConnection.pairedDevices){
                        if (device != null && device.getBluetoothClass() != null && device.getBluetoothClass().getDeviceClass() == BluetoothClass.Device.TOY_ROBOT){
                            pairedDevices.add(device);
                            Log.d(TAG, "for toy filter: "+device.getName());
                        }
                    }
                    nxtPairedDevicesAdapter = new DevicesAdapter(pairedDevices, getActivity().getBaseContext());
                    nxtPairedDevicesListView.setAdapter(nxtPairedDevicesAdapter);
                    nxtPairedDevicesListView.setOnItemClickListener(bla);
                    nxtPairedDevicesAdapter.notifyDataSetChanged();

                    nxtNewDevicesAdapter = new DevicesAdapter(getActivity().getBaseContext());
                    nxtNewDevicesListView.setAdapter(nxtNewDevicesAdapter);
                    nxtNewDevicesListView.setOnItemClickListener(bla);

                    pairedDevices = new ArrayList<BluetoothDevice>();
                    for(BluetoothDevice device:  BTConnection.pairedDevices){
//                        if (device != null && device.getBluetoothClass() != null && device.getBluetoothClass().getDeviceClass() == BluetoothClass.Device.PHONE_SMART){
                            pairedDevices.add(device);
                            Log.d(TAG, "for camera filter: "+device.getName());
//                        }
                    }
                    cameraPairedDevicesAdapter = new DevicesAdapter(pairedDevices, getActivity().getBaseContext());
                    cameraPairedDevicesListView.setAdapter(cameraPairedDevicesAdapter);
                    cameraPairedDevicesListView.setOnItemClickListener(bla);
                    cameraPairedDevicesAdapter.notifyDataSetChanged();

                    cameraNewDevicesAdapter = new DevicesAdapter(getActivity().getBaseContext());
                    cameraNewDevicesListView.setAdapter(cameraNewDevicesAdapter);
                    cameraNewDevicesListView.setOnItemClickListener(bla);

//                    long start = System.currentTimeMillis();
//                    while(!btc.isConnected() && System.currentTimeMillis()-start < 10000){}
//                    btc.writeMessage(b);

                } catch (/*Interrupted*/Exception e) {
                    e.printStackTrace();
                }
            }
        });

        Button conButton = (Button) view.findViewById(R.id.connect_button);
        conButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               try {
                    if(nxtDevice && cams == 2 && devices.size() == 3){
                        Log.d(TAG, "Go ahead ");
                        listener.onObjectSelected(devices);
                    } else {
                        Log.d(TAG, "Hit it, Gandalf " + devices.size());
                    }
               } catch (/*Interrupted*/Exception e) {
                    e.printStackTrace();
               }
            }
        });
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(getActivity()!=null)
            getActivity().unregisterReceiver(mReceiver);
    }

    private AdapterView.OnItemClickListener bla = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            BTConnection btc = BTConnection.getInstance();
            btc.cancelDiscovery();

            BluetoothDevice deviceTemp = (BluetoothDevice) parent.getAdapter().getItem(position);
            Log.d(TAG, "ItemClick Device: "+deviceTemp.getName());
//            Intent intent = new Intent();
//            intent.putExtra(EXTRA_DEVICE_ADDRESS, ((BluetoothDevice)parent.getAdapter().getItem(position)).getAddress());
            if(deviceTemp.getBluetoothClass().getDeviceClass() == BluetoothClass.Device.TOY_ROBOT && !nxtDevice){
                devices.add(deviceTemp);
                nxtDevice = true;
            } else if(cams < 2 && !devices.contains(deviceTemp)){
                devices.add(deviceTemp);
                cams++;
            } else {
                Log.d(TAG, "Ya existe un NXT en la lista o ya se seleccionaron las 2 camaras");
            }

            Log.d(TAG, "size = "+devices.size() + " cams = " + cams + " nxtDevice " + nxtDevice);

//            listener.onObjectSelected(deviceTemp);

//            setResult(Activity.RESULT_OK, intent);
//            finish();
        }
    };

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if ((device.getBondState() != BluetoothDevice.BOND_BONDED) && (device.getBluetoothClass().getDeviceClass() == BluetoothClass.Device.TOY_ROBOT)) {
                    Log.d(TAG, "Device: {Name:"+device.getName()+", Address: "+device.getAddress()+", Class: "+device.getClass()+"}");
                    nxtNewDevicesAdapter.add(device);
                    nxtNewDevicesAdapter.notifyDataSetChanged();
                }

                if ((device.getBondState() != BluetoothDevice.BOND_BONDED) && (device.getBluetoothClass().getDeviceClass() == BluetoothClass.Device.PHONE_SMART)) {
                    Log.d(TAG, "Device: {Name:"+device.getName()+", Address: "+device.getAddress()+", Class: "+device.getBluetoothClass().getDeviceClass()+"}");
                    cameraNewDevicesAdapter.add(device);
                    cameraNewDevicesAdapter.notifyDataSetChanged();
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Toast.makeText(context, "Busqueda Terminada", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Busqueda Terminada");
                nxtDiscoveryProgress.setVisibility(View.GONE);
                cameraDiscoveryProgress.setVisibility(View.GONE);
//                setProgressBarIndeterminateVisibility(false);
//                setTitle("Select device");
//                findViewById(R.id.button_scan).setVisibility(View.VISIBLE);
            }
        }
    };

}
