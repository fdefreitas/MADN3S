package org.madn3s.controller.fragments;

import static org.madn3s.controller.MADN3SController.camera1;
import static org.madn3s.controller.MADN3SController.camera1WeakReference;
import static org.madn3s.controller.MADN3SController.camera2;
import static org.madn3s.controller.MADN3SController.camera2WeakReference;

import java.util.ArrayList;

import org.madn3s.controller.MADN3SController;
import org.madn3s.controller.MADN3SController.Device;
import org.madn3s.controller.MADN3SController.State;
import org.madn3s.controller.R;
import org.madn3s.controller.components.BraveHeartMidgetService;
import org.madn3s.controller.components.NXTTalker;
import org.madn3s.controller.io.HiddenMidgetAttackAsyncTask;
import org.madn3s.controller.io.HiddenMidgetConnector;
import org.madn3s.controller.io.HiddenMidgetReader;
import org.madn3s.controller.io.UniversalComms;
import org.madn3s.controller.models.DevicesAdapter;

import android.R.integer;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by inaki on 26/01/14.
 */
public class ConnectionFragment extends BaseFragment {
	

    public static final int MESSAGE_STATE_CHANGE = 2;
    public static final int MESSAGE_TOAST = 1;
    public static final String TOAST = "toast";
    
    private static final String TAG = "ConnectionFragment";
    
    private ArrayList<BluetoothDevice> devices;
    private NXTTalker talker;
    private int mState;
    private BroadcastReceiver mReceiver;
    private Handler mHandler;

    private ListView devicesListView;
    private DevicesAdapter devicesAdapter;
    private TextView nxtNameTextView;
    private TextView nxtAddressTextView;
    private ProgressBar nxtConnectingProgressBar;
    private ImageView nxtConnectedImageView;
    private ImageView nxtNotConnectedImageView;

    private TextView camera1NameTextView;
    private TextView camera1AddressTextView;
    private ProgressBar camera1ConnectingProgressBar;
    private ImageView camera1ConnectedImageView;
    private ImageView camera1NotConnectedImageView;


    private TextView camera2NameTextView;
    private TextView camera2AddressTextView;
    private ProgressBar camera2ConnectingProgressBar;
    private ImageView camera2ConnectedImageView;
    private ImageView camera2NotConnectedImageView;
    private ConnectionFragment mConnectionFragment;

    
    public ConnectionFragment(){
        
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mConnectionFragment = this;
        
        HiddenMidgetReader.connectionFragmentBridge = new UniversalComms() {
			@Override
			public void callback(Object msg) {
				Bundle bundle = (Bundle)msg;
				final Device device = Device.setDevice(bundle.getInt("device"));
				final State state = State.setState(bundle.getInt("state"));
				mConnectionFragment.getView().post(
					new Runnable() { 
						public void run() { 
							setMarkers(state, device);
						} 
					}
				); 
			}
		};
    }

    

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_connection, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        nxtNameTextView = (TextView) view.findViewById(R.id.nxt_name_connection_textView);
//        nxtNameTextView.setText(MADN3SController.nxt.getName());
//        nxtAddressTextView = (TextView) view.findViewById(R.id.nxt_address_connection_textView);
//        nxtAddressTextView.setText(MADN3SController.nxt.getAddress());
        nxtConnectingProgressBar = (ProgressBar) view.findViewById(R.id.nxt_connecting_progressBar);
        nxtConnectedImageView = (ImageView) view.findViewById(R.id.nxt_connected_imageView);
        nxtNotConnectedImageView = (ImageView) view.findViewById(R.id.nxt_not_connected_imageView);

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MESSAGE_TOAST:
                        Toast.makeText(getActivity().getApplicationContext(), msg.getData().getString(TOAST), Toast.LENGTH_SHORT).show();
                        break;
                    case MESSAGE_STATE_CHANGE:
                        mState = msg.arg1;
                        switch(mState){
                            case NXTTalker.STATE_CONNECTED:
                                nxtConnectingProgressBar.setVisibility(View.GONE);
                                nxtConnectedImageView.setVisibility(View.VISIBLE);
                                break;
                            case NXTTalker.STATE_NONE:
                                nxtConnectingProgressBar.setVisibility(View.GONE);
                                nxtNotConnectedImageView.setVisibility(View.VISIBLE);
                                nxtConnectedImageView.setVisibility(View.GONE);
                                break;
                            case NXTTalker.STATE_CONNECTING:
                                nxtConnectingProgressBar.setVisibility(View.VISIBLE);
                                nxtNotConnectedImageView.setVisibility(View.GONE);
                                nxtConnectedImageView.setVisibility(View.GONE);
                                break;
                        }
                        break;
                }
            }
        };

     //   talker = new NXTTalker(mHandler);
     //   talker.connect(nxt);
//        Log.d(TAG, "Iniciando Conexion con NXT: " + nxt.getName());
        
        
        
        HiddenMidgetConnector connectCamera1 = new HiddenMidgetConnector(camera1, camera1WeakReference, MADN3SController.readCamera1);
        connectCamera1.execute();
        Log.d(TAG, "Iniciando conexion con Camara1: " + camera1.getName());
        
        HiddenMidgetConnector connectCamera2 = new HiddenMidgetConnector(camera2, camera2WeakReference, MADN3SController.readCamera2);
        connectCamera2.execute();
        Log.d(TAG, "Iniciando conexion con Camara2: " + camera2.getName()); 
        
//        HiddenMidgetAttackAsyncTask taskCamera1 = new HiddenMidgetAttackAsyncTask(camera1, "right");
//        taskCamera1.execute();
//        Log.d(TAG, "Iniciando conexion con Camara1: " + camera1.getName());
//        
//        HiddenMidgetAttackAsyncTask taskCamera2 = new HiddenMidgetAttackAsyncTask(camera2, "left");
//        taskCamera2.execute();
//        Log.d(TAG, "Iniciando conexion con Camara2: " + camera2.getName());        

        camera1NameTextView = (TextView) view.findViewById(R.id.camera1_name_connection_textView);
        camera1NameTextView.setText(camera1.getName());
        camera1AddressTextView = (TextView) view.findViewById(R.id.camera1_address_connection_textView);
        camera1AddressTextView.setText(camera1.getAddress());
        camera1ConnectingProgressBar = (ProgressBar) view.findViewById(R.id.camera1_connecting_progressBar);
        camera1ConnectedImageView = (ImageView) view.findViewById(R.id.camera1_connected_imageView);
        camera1NotConnectedImageView = (ImageView) view.findViewById(R.id.camera1_not_connected_imageView);

        camera2NameTextView = (TextView) view.findViewById(R.id.camera2_name_connection_textView);
        camera2NameTextView.setText(camera2.getName());
        camera2AddressTextView = (TextView) view.findViewById(R.id.camera2_address_connection_textView);
        camera2AddressTextView.setText(camera2.getAddress());
        camera2ConnectingProgressBar = (ProgressBar) view.findViewById(R.id.camera2_connecting_progressBar);
        camera2ConnectedImageView = (ImageView) view.findViewById(R.id.camera2_connected_imageView);
        camera2NotConnectedImageView = (ImageView) view.findViewById(R.id.camera2_not_connected_imageView);

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
            	
//                BTConnection conn = BTConnection.getInstance();
//                BluetoothSocket mSocket1 = conn.getCam1Socket();
//                BluetoothSocket mSocket2 = conn.getCam2Socket();
                BluetoothSocket mSocket1 = camera1WeakReference.get();
                BluetoothSocket mSocket2 = camera2WeakReference.get();
                String action = intent.getAction();
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.d(TAG, action);
                if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)){
                    String addressCamera1 = camera1.getAddress();
                    String addressCamera2 = camera2.getAddress();

                    //Camera 1
                    if(MADN3SController.isCamera1(device.getAddress()) && mSocket1.getRemoteDevice().getBondState() == BluetoothDevice.BOND_BONDED){
                        //if (device.getBondState() == BluetoothDevice.BOND_BONDED){
                        if (mSocket1.isConnected()){
                            camera1ConnectedImageView.setVisibility(View.VISIBLE);
                            camera1ConnectingProgressBar.setVisibility(View.GONE);
                        //}else if (device.getBondState() == BluetoothDevice.BOND_NONE){
                        }else if (!mSocket1.isConnected()){
                            camera1NotConnectedImageView.setVisibility(View.VISIBLE);
                            camera1ConnectingProgressBar.setVisibility(View.GONE);
                        }
                    }

                    //Camera 2
                    if(MADN3SController.isCamera2(device.getAddress()) && mSocket2.getRemoteDevice().getBondState() == BluetoothDevice.BOND_BONDED){
                    //    if (device.getBondState() == BluetoothDevice.BOND_BONDED){
                        if (mSocket2.isConnected()){
                            camera2ConnectedImageView.setVisibility(View.VISIBLE);
                            camera2ConnectingProgressBar.setVisibility(View.GONE);
                        ///}else if (device.getBondState() == BluetoothDevice.BOND_NONE){
                        }else if (!mSocket2.isConnected()){
                            camera2NotConnectedImageView.setVisibility(View.VISIBLE);
                            camera2ConnectingProgressBar.setVisibility(View.GONE);
                        }
                    }


                }
                if(mSocket1 == null){
                    Log.d(TAG, "mSocket1 es null");
                } else {
                    if (mSocket1.isConnected()){
                        Log.d(TAG, "mSocket1 conectado");
                    } else {
                        Log.d(TAG, "mSocket1 NO conectado");
                    }
                }

                if(mSocket2 == null){
                    Log.d(TAG, "mSocket2 es null");
                } else {
                    if (mSocket2.isConnected()){
                        Log.d(TAG, "mSocket2 conectado");
                    } else {
                        Log.d(TAG, "mSocket2 NO conectado");
                    }
                }
            }
        };
    }
    
    protected void setMarkers(State state, Device device) {
    	ImageView connected, failed;
    	ProgressBar connecting;
    	switch (device){
	        case CAMERA1:
	        	connected = camera1ConnectedImageView;
	        	failed = camera1NotConnectedImageView;
	        	connecting = camera1ConnectingProgressBar;
	        	break;
	        case CAMERA2:
	        	connected = camera2ConnectedImageView;
	        	failed = camera2NotConnectedImageView;
	        	connecting = camera2ConnectingProgressBar;
	            break;
	        default:
	        case NXT:
	        	connected = nxtConnectedImageView;
	        	failed = nxtNotConnectedImageView;
	        	connecting = nxtConnectingProgressBar;
	            break;
	    }
    	
    	switch (state){
	        case CONNECTED:
	        	connected.setVisibility(View.VISIBLE);
	        	failed.setVisibility(View.GONE);
	        	connecting.setVisibility(View.GONE);
	            break;
	        case CONNECTING:
	        	connected.setVisibility(View.GONE);
	        	failed.setVisibility(View.GONE);
	        	connecting.setVisibility(View.VISIBLE);
	            break;
	        default:
	        case FAILED:
	        	connected.setVisibility(View.GONE);
	        	failed.setVisibility(View.VISIBLE);
	        	connecting.setVisibility(View.GONE);
	            break;
	    }
		
	}

}
