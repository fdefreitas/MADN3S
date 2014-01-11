package org.madn3s.controller.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.method.CharacterPickerDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.madn3s.controller.R;
import org.madn3s.controller.io.BTConnection;
import org.madn3s.controller.models.DevicesAdapter;

/**
 * Created by inaki on 12/7/13.
 */
public class MainFragment extends BaseFragment{
    public static String EXTRA_DEVICE_ADDRESS = "device_address";

    ListView newDevicesListView, pairedDevicesListView;
    ProgressBar discoveryProgress;
    DevicesAdapter newDevicesAdapter, pairedDevicesAdapter;

    public MainFragment() {
        TAG = "DEBUG "+this.getClass().getName();
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
        assert getActivity() != null;
        pairedDevicesListView = (ListView) getActivity().findViewById(R.id.paired_devices_listView);
        newDevicesListView = (ListView) getActivity().findViewById(R.id.new_devices_listView);

        discoveryProgress = (ProgressBar) getActivity().findViewById(R.id.discovery_progressBar);

        Button goButton = (Button) view.findViewById(R.id.button);
        goButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BTConnection btc = BTConnection.getInstance();
//                btc.startMindstormConnection();
                btc.doDiscovery();
                discoveryProgress.setVisibility(View.VISIBLE);

                byte b = "hallo!".getBytes()[0];
                try {
                    pairedDevicesAdapter = new DevicesAdapter(BTConnection.pairedDevices, getActivity().getBaseContext());
                    pairedDevicesListView.setAdapter(pairedDevicesAdapter);
                    pairedDevicesListView.setOnItemClickListener(bla);

                    newDevicesAdapter = new DevicesAdapter(getActivity().getBaseContext());
                    newDevicesListView.setAdapter(newDevicesAdapter);
                    newDevicesListView.setOnItemClickListener(bla);

//                    long start = System.currentTimeMillis();
//                    while(!btc.isConnected() && System.currentTimeMillis()-start < 10000){}
//                    btc.writeMessage(b);

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
            listener.onObjectSelected(deviceTemp);

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

                if ((device.getBondState() != BluetoothDevice.BOND_BONDED) /*&& (device.getBluetoothClass().getDeviceClass() == BluetoothClass.Device.TOY_ROBOT)*/) {
                    Log.d(TAG, "Device: {Name:"+device.getName()+", Address: "+device.getAddress()+", Class: "+device.getClass()+"}");
                    newDevicesAdapter.add(device);
                    newDevicesAdapter.notifyDataSetChanged();

//                    getActivity().findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);
//                    getActivity().findViewById(R.id.no_devices).setVisibility(View.GONE);
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.d(TAG, "Busqueda Terminada");
                discoveryProgress.setVisibility(View.GONE);
//                setProgressBarIndeterminateVisibility(false);
//                setTitle("Select device");
//                findViewById(R.id.button_scan).setVisibility(View.VISIBLE);
            }
        }
    };

}
