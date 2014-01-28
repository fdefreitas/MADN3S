package org.madn3s.controller.fragments;

import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;
import org.madn3s.controller.R;
import org.madn3s.controller.components.NXTTalker;
import org.madn3s.controller.models.DevicesAdapter;

import java.util.ArrayList;

/**
 * Created by inaki on 26/01/14.
 */
public class ConectionFragment extends BaseFragment {

    private ArrayList<BluetoothDevice> device;
    private NXTTalker talker;
    int mState;
    public static final int MESSAGE_STATE_CHANGE = 2;
    public static final int MESSAGE_TOAST = 1;
    public static final String TOAST = "toast";

    ListView devicesListView;
    DevicesAdapter devicesAdapter;

    private ConectionFragment(){
        TAG = this.getClass().getName();
    }

    public ConectionFragment(ArrayList<BluetoothDevice>  device){
        this();
        this.device = (ArrayList<BluetoothDevice>)device.clone();

        final Handler mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MESSAGE_TOAST:
                        Toast.makeText(getActivity().getApplicationContext(), msg.getData().getString(TOAST), Toast.LENGTH_SHORT).show();
                        break;
                    case MESSAGE_STATE_CHANGE:
                        mState = msg.arg1;
//                        displayState();
                        break;
                }
            }
        };

        for(BluetoothDevice b : device){
            if(b.getBluetoothClass().getDeviceClass() == BluetoothClass.Device.TOY_ROBOT){
                talker = new NXTTalker(mHandler);
                talker.connect(b);
            } else {
                //establecer conexion bluetooth con las camaras
            }
            Log.d(TAG, b.getName());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_connection, container, false);
    }

    //aqui me gustaria q pusiesemos la info del device y un indicador de q la conexion se levanto, esta levantandose o fallo
    // y un boton que se active cuando todas las conexiones esten establecidas que te lleve al menu donde iran las opciones de manejar, scanear, etc
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
     //   super.onViewCreated(view, savedInstanceState);

        devicesListView = (ListView) getActivity().findViewById(R.id.devices_to_connect_listView);
        devicesAdapter = new DevicesAdapter(device,getActivity().getBaseContext());
        devicesListView.setAdapter(devicesAdapter);
    }

}
