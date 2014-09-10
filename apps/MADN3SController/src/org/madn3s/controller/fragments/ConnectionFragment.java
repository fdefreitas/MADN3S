package org.madn3s.controller.fragments;

import static org.madn3s.controller.MADN3SController.camera1;
import static org.madn3s.controller.MADN3SController.camera1WeakReference;
import static org.madn3s.controller.MADN3SController.camera2;
import static org.madn3s.controller.MADN3SController.camera2WeakReference;

import java.util.ArrayList;

import org.madn3s.controller.MADN3SController;
import org.madn3s.controller.MADN3SController.Device;
import org.madn3s.controller.MADN3SController.Mode;
import org.madn3s.controller.MADN3SController.State;
import org.madn3s.controller.R;
import org.madn3s.controller.components.NXTTalker;
import org.madn3s.controller.io.HiddenMidgetConnector;
import org.madn3s.controller.io.HiddenMidgetReader;
import org.madn3s.controller.io.UniversalComms;
import org.madn3s.controller.models.DevicesAdapter;
import org.madn3s.controller.models.StatusViewHolder;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
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
    private ConnectionFragment mFragment;

    private TextView nxtNameTextView;
    private TextView nxtAddressTextView;
    private StatusViewHolder nxtStatusViewHolder;

    private TextView camera1NameTextView;
    private TextView camera1AddressTextView;
    private StatusViewHolder camera1StatusViewHolder;

    private TextView camera2NameTextView;
    private TextView camera2AddressTextView;
    private StatusViewHolder camera2StatusViewHolder;

    private Button scannerButton;
    private Button remoteControlButton;
    private Button modelGalleryButton;
    
    public ConnectionFragment(){
        
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFragment = this;
        
        HiddenMidgetReader.connectionFragmentBridge = new UniversalComms() {
			@Override
			public void callback(Object msg) {
				Bundle bundle = (Bundle)msg;
				final Device device = Device.setDevice(bundle.getInt("device"));
				final State state = State.setState(bundle.getInt("state"));
				mFragment.getView().post(
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

    @SuppressLint("HandlerLeak")
	@Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        nxtNameTextView = (TextView) view.findViewById(R.id.nxt_name_connection_textView);
        nxtAddressTextView = (TextView) view.findViewById(R.id.nxt_address_connection_textView);
        
        if(MADN3SController.nxt != null){
	        nxtNameTextView.setText(MADN3SController.nxt.getName());
	        nxtAddressTextView.setText(MADN3SController.nxt.getAddress());
        }
        
        nxtStatusViewHolder = new StatusViewHolder(
        		view.findViewById(R.id.nxt_not_connected_imageView), 
        		view.findViewById(R.id.nxt_connected_imageView), 
        		view.findViewById(R.id.nxt_connecting_progressBar)
    		);

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
                                nxtStatusViewHolder.success();
                                break;
                            case NXTTalker.STATE_NONE:
                                nxtStatusViewHolder.failure();
                                break;
                            case NXTTalker.STATE_CONNECTING:
                                nxtStatusViewHolder.working();
                                break;
                        }
                        break;
                }
            }
        };

     //   talker = new NXTTalker(mHandler);
     //   talker.connect(nxt);
//        Log.d(TAG, "Iniciando Conexion con NXT: " + nxt.getName());
        
        
        
        HiddenMidgetConnector connectCamera1 = new HiddenMidgetConnector(camera1, camera1WeakReference, MADN3SController.readCamera1, "right");
        connectCamera1.execute();
        Log.d(TAG, "Iniciando conexion con Camara1: " + camera1.getName());
        
        HiddenMidgetConnector connectCamera2 = new HiddenMidgetConnector(camera2, camera2WeakReference, MADN3SController.readCamera2, "left");
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
        camera1AddressTextView = (TextView) view.findViewById(R.id.camera1_address_connection_textView);
        
        if(camera1 != null){
        	camera1NameTextView.setText(camera1.getName());
        	camera1AddressTextView.setText(camera1.getAddress());
        }
        
        camera1StatusViewHolder = new StatusViewHolder(
        		view.findViewById(R.id.camera1_not_connected_imageView), 
        		view.findViewById(R.id.camera1_connected_imageView), 
        		view.findViewById(R.id.camera1_connecting_progressBar)
    		);

        camera2NameTextView = (TextView) view.findViewById(R.id.camera2_name_connection_textView);
        camera2AddressTextView = (TextView) view.findViewById(R.id.camera2_address_connection_textView);
        
        if(camera2 != null){
	        camera2NameTextView.setText(camera2.getName());
	        camera2AddressTextView.setText(camera2.getAddress());
        }
        
        camera2StatusViewHolder = new StatusViewHolder(
        		view.findViewById(R.id.camera2_not_connected_imageView), 
        		view.findViewById(R.id.camera2_connected_imageView), 
        		view.findViewById(R.id.camera2_connecting_progressBar)
    		);

        scannerButton = (Button) view.findViewById(R.id.scanner_button);
        scannerButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				
//				try{
//		        	String timeStamp = new SimpleDateFormat("yyyyMMdd_HH").format(new Date());
//			        JSONObject json = new JSONObject();
//			        json.put("action", "photo");
//			        json.put("project_name", "HereIAm-" + timeStamp);
//					
//			        if(camera1WeakReference != null){
//						json.put("side", "left");
//						json.put("camera_name", camera1.getName());
//						HiddenMidgetWriter sendCamera1 = new HiddenMidgetWriter(camera1WeakReference, json.toString());
//						sendCamera1.execute();
//				        Log.d(TAG, "Enviando a Camara1: " + camera1.getName());
//				        MADN3SController.readCamera1.set(true);
//					} else {
//						Log.d(TAG, "camera1WeakReference null");
//					}
//					
//					if(camera2WeakReference != null){
//						json.put("side", "right");
//						json.put("camera_name", camera2.getName());
//						HiddenMidgetWriter sendCamera2 = new HiddenMidgetWriter(camera2WeakReference, json.toString());
//						sendCamera2.execute();
//				        Log.d(TAG, "Enviando a Camara2: " + camera2.getName());
//				        MADN3SController.readCamera2.set(true);
//					} else {
//						Log.d(TAG, "camera2WeakReference null");
//					}
//					
//					
//		        } catch (JSONException e){
//		            Log.d("Awesome AsyncTask", "Error armando el JSON");
//		        } catch (Exception e){
//		            Log.d("Awesome AsyncTask", "Error generico enviando");
//		        }
				listener.onObjectSelected(Mode.SCAN, mFragment);
			}
		});
        remoteControlButton = (Button) view.findViewById(R.id.remote_control_button);
        remoteControlButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				listener.onObjectSelected(Mode.CONTROLLER, mFragment);
			}
		});
        modelGalleryButton = (Button) view.findViewById(R.id.model_gallery_button);
        modelGalleryButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Mostrar Galeria
			}
		});
    }
    
    protected void setMarkers(State state, Device device) {
    	StatusViewHolder statusHolder;
    	switch (device){
	        case CAMERA1:
	        	statusHolder = camera1StatusViewHolder;
	        	break;
	        case CAMERA2:
	        	statusHolder = camera2StatusViewHolder;
	            break;
	        default:
	        case NXT:
	        	statusHolder = nxtStatusViewHolder;
	            break;
	    }
    	
    	switch (state){
	        case CONNECTED:
        		statusHolder.success();
	            break;
	        case CONNECTING:
	        	statusHolder.working();
	            break;
	        default:
	        case FAILED:
	        	statusHolder.failure();
	            break;
	    }
		
	}

}
