package org.madn3s.controller.fragments;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import org.madn3s.controller.R;
import org.madn3s.controller.components.NXTTalker;

/**
 * Created by inaki on 1/11/14.
 */

// de aqui se debe eliminar la parte referente a levantar la conexion y se debe recibir el talker por parametro
public class ControlsFragment extends BaseFragment {
    public static final int MESSAGE_STATE_CHANGE = 2;
    public static final int MESSAGE_TOAST = 1;
    public static final String TOAST = "toast";
    BluetoothDevice device;
    private int mPower = 80;
    private boolean mReverse = false;
    private boolean mReverseLR = false;
    private boolean mRegulateSpeed = false;
    private boolean mSynchronizeMotors = true;
    int mState;

    private NXTTalker talker;

    private ImageView frontImageView, backImageView, rightImageView,leftImageView;

    private ControlsFragment(){
        TAG = this.getClass().getName();
    }

    public ControlsFragment(BluetoothDevice device){
        this();
        this.device = device;

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

        talker = new NXTTalker(mHandler);
        talker.connect(device);
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

        frontImageView = (ImageView) view.findViewById(R.id.front_arrow_imageView);
        backImageView = (ImageView) view.findViewById(R.id.back_arrow_imageView);
        leftImageView = (ImageView) view.findViewById(R.id.left_arrow_imageView);
        rightImageView = (ImageView) view.findViewById(R.id.right_arrow_imageView);
        frontImageView.setOnTouchListener(new DirectionButtonOnTouchListener(1,1));
        backImageView.setOnTouchListener(new DirectionButtonOnTouchListener(-1,-1));
        leftImageView.setOnTouchListener(new DirectionButtonOnTouchListener(-0.6,0.6));
        rightImageView.setOnTouchListener(new DirectionButtonOnTouchListener(0.6,-0.6));
    }

    private class DirectionButtonOnTouchListener implements View.OnTouchListener {

        private double lmod;
        private double rmod;

        public DirectionButtonOnTouchListener(double l, double r) {
            lmod = l;
            rmod = r;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            //Log.i("NXT", "onTouch event: " + Integer.toString(event.getAction()));
            int action = event.getAction();
            //if ((action == MotionEvent.ACTION_DOWN) || (action == MotionEvent.ACTION_MOVE)) {
            if (action == MotionEvent.ACTION_DOWN) {
                byte power = (byte) mPower;
                if (mReverse) {
                    power *= -1;
                }
                byte l = (byte) (power*lmod);
                byte r = (byte) (power*rmod);
                if (!mReverseLR) {
                    talker.motors(l, r, mRegulateSpeed, mSynchronizeMotors);
                } else {
                    talker.motors(r, l, mRegulateSpeed, mSynchronizeMotors);
                }
            } else if ((action == MotionEvent.ACTION_UP) || (action == MotionEvent.ACTION_CANCEL)) {
                talker.motors((byte) 0, (byte) 0, mRegulateSpeed, mSynchronizeMotors);
            }
            return true;
        }
    }


}
