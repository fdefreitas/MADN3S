package org.madn3s.controller.fragments;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.madn3s.controller.R;

/**
 * Created by inaki on 1/11/14.
 */
public class ControlsFragment extends BaseFragment {
    BluetoothDevice device;

    private ControlsFragment(){
        TAG = this.getClass().getName();
    }

    public ControlsFragment(BluetoothDevice device){
        this();
        this.device = device;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_controls, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
        assert getActivity() !=null;
        Log.d(TAG, "Device: "+device.getName());
        TextView tv = (TextView)getActivity().findViewById(R.id.address_textView);
        tv.setText("Name: "+device.getName()+" Address: "+device.getAddress());
    }


}
