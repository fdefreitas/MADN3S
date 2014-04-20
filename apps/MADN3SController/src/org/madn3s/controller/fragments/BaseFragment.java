package org.madn3s.controller.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.bluetooth.BluetoothDevice;

/**
 * Created by inaki on 1/11/14.
 */
public class BaseFragment extends Fragment {

    public OnItemSelectedListener listener;
    private static final String TAG = "BaseFragment";

    public interface OnItemSelectedListener {
        public void onObjectSelected(Object selected);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof OnItemSelectedListener) {
            listener = (OnItemSelectedListener) activity;
        } else {
            throw new ClassCastException(activity.toString()
                    + " must implemenet MyListFragment.OnItemSelectedListener");
        }
    }
}