package org.madn3s.controller.fragments;

import static org.madn3s.controller.MADN3SController.isCameraDevice;
import static org.madn3s.controller.MADN3SController.isToyDevice;
import static org.madn3s.controller.MADN3SController.leftCamera;
import static org.madn3s.controller.MADN3SController.nxt;
import static org.madn3s.controller.MADN3SController.rightCamera;

import java.util.ArrayList;

import org.madn3s.controller.MADN3SController;
import org.madn3s.controller.MADN3SController.Mode;
import org.madn3s.controller.R;
import org.madn3s.controller.components.CameraSelectionDialogFragment;
import org.madn3s.controller.io.BTConnection;
import org.madn3s.controller.models.DevicesAdapter;
import org.madn3s.controller.models.NewDevicesAdapter;
import org.madn3s.controller.models.PairedDevicesAdapter;

import android.bluetooth.BluetoothAdapter;
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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by inaki on 12/7/13.
 */
public class DiscoveryFragment extends BaseFragment {
	public static final String tag = "MainFragment";
	public static final String EXTRA_DEVICE_ADDRESS = "device_address";

	private BluetoothAdapter btAdapter;
	
	private ListView nxtNewDevicesListView, nxtPairedDevicesListView;
	private ListView cameraNewDevicesListView, cameraPairedDevicesListView;
	private LinearLayout nxtDevicesLayout, cameraDevicesLayout;
	private TextView cameraConnectionTextView, nxtConnectionTextView;
	private ProgressBar discoveryProgress;
	private Button connectButton;
	private Button scanButton;
	private NewDevicesAdapter nxtNewDevicesAdapter, cameraNewDevicesAdapter ;
	private PairedDevicesAdapter nxtPairedDevicesAdapter, cameraPairedDevicesAdapter;
	private boolean isNxtSelected;
	private int cams;
	private DiscoveryFragment mFragment;
	private CameraSelectionDialogFragment cameraSelectionDialogFragment;
	
	public DiscoveryFragment() {
		mFragment = this;
		isNxtSelected =  false;
		cams = 0;
		btAdapter = BluetoothAdapter.getDefaultAdapter();
		btAdapter.startDiscovery();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_discovery, container, false);
	}

	@Override
	public void onViewCreated (View view, Bundle savedInstanceState){
		super.onViewCreated(view, savedInstanceState);
		
		Log.d(tag, "entrando a onViewCreated");
		
		discoveryProgress = (ProgressBar) getView().findViewById(R.id.discovery_progressBar);
		discoveryProgress.setVisibility(View.GONE);
		
		nxtConnectionTextView = (TextView) getView().findViewById(R.id.nxt_connection_textView);
		nxtConnectionTextView.setVisibility(View.GONE);
		
		nxtDevicesLayout = (LinearLayout) getView().findViewById(R.id.nxt_devices_layout);
		nxtDevicesLayout.setVisibility(View.GONE);
		nxtPairedDevicesListView = (ListView) getView().findViewById(R.id.nxt_paired_devices_listView);
		nxtNewDevicesListView = (ListView) getView().findViewById(R.id.nxt_new_devices_listView);

		cameraConnectionTextView = (TextView) getView().findViewById(R.id.cameras_connection_textView);
		cameraConnectionTextView.setVisibility(View.GONE);
		cameraDevicesLayout = (LinearLayout) getView().findViewById(R.id.camera_devices_layout);
		cameraDevicesLayout.setVisibility(View.GONE);
		cameraPairedDevicesListView = (ListView) getView().findViewById(R.id.camera_paired_devices_listView);
		cameraNewDevicesListView = (ListView) getView().findViewById(R.id.cameras_new_devices_listView);

		scanButton = (Button) getView().findViewById(R.id.scan_button);
		scanButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				
				discoveryProgress.setVisibility(View.VISIBLE);
				nxtConnectionTextView.setVisibility(View.GONE);
				nxtDevicesLayout.setVisibility(View.GONE);
				cameraConnectionTextView.setVisibility(View.GONE);
				cameraDevicesLayout.setVisibility(View.GONE);
				
				Log.d(tag, "Starting Discovery");
				doDiscovery();
				// TODO sacar cosas utiles de BTConn para eliminarlo
//				BTConnection btc = BTConnection.getInstance();
//				btc.doDiscovery();

				try {
					ArrayList<BluetoothDevice> temporaryPairedDevices = new ArrayList<BluetoothDevice>();
					for(BluetoothDevice device:  btAdapter.getBondedDevices()){
						if (isToyDevice(device)){
							temporaryPairedDevices.add(device);
							Log.d(tag, "Toy Filter Device: "+device.getName());
						}
					}
					
					nxtPairedDevicesAdapter = new PairedDevicesAdapter(temporaryPairedDevices, getActivity().getBaseContext());
					nxtPairedDevicesListView.setAdapter(nxtPairedDevicesAdapter);
					nxtPairedDevicesListView.setOnItemClickListener(onDeviceAdapterClickListener);

					nxtNewDevicesAdapter = new NewDevicesAdapter(getActivity().getBaseContext());
					nxtNewDevicesListView.setAdapter(nxtNewDevicesAdapter);
					nxtNewDevicesListView.setOnItemClickListener(onDeviceAdapterClickListener);

					temporaryPairedDevices = new ArrayList<BluetoothDevice>();
					for(BluetoothDevice device:  btAdapter.getBondedDevices()){
						if (isCameraDevice(device)){
							temporaryPairedDevices.add(device);
							Log.d(tag, "Camera Filter Device: "+device.getName());
						}
					}
					cameraPairedDevicesAdapter = new PairedDevicesAdapter(temporaryPairedDevices, getActivity().getBaseContext());
					cameraPairedDevicesListView.setAdapter(cameraPairedDevicesAdapter);
					cameraPairedDevicesListView.setOnItemClickListener(onDeviceAdapterClickListener);

					cameraNewDevicesAdapter = new NewDevicesAdapter(getActivity().getBaseContext());
					cameraNewDevicesListView.setAdapter(cameraNewDevicesAdapter);
					cameraNewDevicesListView.setOnItemClickListener(onDeviceAdapterClickListener);

					//                    long start = System.currentTimeMillis();
					//                    while(!btc.isConnected() && System.currentTimeMillis()-start < 10000){}
					//                    btc.writeMessage(b);

					IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
					getActivity().registerReceiver(mReceiver, intentFilter);

					intentFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
					getActivity().registerReceiver(mReceiver, intentFilter);
					
				} catch (/*Interrupted*/Exception e) {
					e.printStackTrace();
				}
			}
		});

		connectButton = (Button) getView().findViewById(R.id.connect_button);
		connectButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					if(isNxtSelected && cams == 2){
						showCamerasDialog();
					} else {
						Toast.makeText(getActivity(), "Debe seleccionar un dispositivo NXT y 2 Cámaras", Toast.LENGTH_LONG).show();
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
		if(getActivity()!=null){
			try {
				getActivity().unregisterReceiver(mReceiver);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
	}
	
	public void showCamerasDialog() {
        cameraSelectionDialogFragment = new CameraSelectionDialogFragment();
        cameraSelectionDialogFragment.show(getFragmentManager(), "CameraSelectionDialogFragment");
    }
	
	public void onDevicesSelectionCompleted(){
		if(isNxtSelected && cams == 2){
			cameraSelectionDialogFragment.dismiss();
			Log.d(tag, "Mode: SCANNER");
			listener.onObjectSelected(Mode.SCANNER, mFragment);
		}else if(isNxtSelected && cams == 2 && MADN3SController.rightCamera == MADN3SController.leftCamera) {
			Toast.makeText(getActivity(), "Debe seleccionar Cámaras diferentes para cada posición", Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(getActivity(), "Debe seleccionar un dispositivo NXT y 2 Cámaras", Toast.LENGTH_LONG).show();
		}
	}

	public void onDevicesSelectionCancelled(){
		Log.d(tag, "Device Selection Cancelled");
	}
	
	private AdapterView.OnItemClickListener onDeviceAdapterClickListener = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			BTConnection btc = BTConnection.getInstance();
			btc.cancelDiscovery();

			BluetoothDevice deviceTemp = (BluetoothDevice) parent.getAdapter().getItem(position);
			Log.d(tag, "ItemClick Device: " + deviceTemp.getName());
			
			if(isToyDevice(deviceTemp) && !isNxtSelected){
				nxt = deviceTemp;
				isNxtSelected = true;
			} else if(isToyDevice(deviceTemp) && isNxtSelected){
				Toast.makeText(getActivity(), "Ya fue seleccionado un dispositivo NXT", Toast.LENGTH_LONG).show();
			} else if(cams == 0){
				rightCamera = deviceTemp;
				cams++;
			} else if(cams == 1){
				leftCamera = deviceTemp;
				cams++;
			} else {
				Toast.makeText(getActivity(), "Ya fueron seleccionadas 2 camaras", Toast.LENGTH_LONG).show();
			}

			Log.d(tag, "Cameras Selected: " + cams + ", isNxtSelected: " + isNxtSelected);
		}
	};

	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

				if(device.getBondState() != BluetoothDevice.BOND_BONDED){
					if (isToyDevice(device)) {
						Log.d(tag, "Device: {Name:" + device.getName() + ", Address: " + device.getAddress() + ", Class: " + device.getClass() + "}");
						nxtNewDevicesAdapter.add(device);
						nxtNewDevicesAdapter.notifyDataSetChanged();
						
					} else if (isCameraDevice(device)) {
						Log.d(tag, "Device: {Name:"+device.getName()+", Address: "+device.getAddress()+", Class: "+device.getBluetoothClass().getDeviceClass()+"}");
						cameraNewDevicesAdapter.add(device);
						cameraNewDevicesAdapter.notifyDataSetChanged();
					}
				}
				
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
				Log.d(tag, "Busqueda Terminada");
				
				discoveryProgress.setVisibility(View.GONE);
				nxtConnectionTextView.setVisibility(View.VISIBLE);
				nxtDevicesLayout.setVisibility(View.VISIBLE);
				cameraConnectionTextView.setVisibility(View.VISIBLE);
				cameraDevicesLayout.setVisibility(View.VISIBLE);
				connectButton.setEnabled(true);
			}
		}
	};
	
	private void enableBT(){
        if(!btAdapter.isEnabled()){
        	btAdapter.enable();
        }
    }

    private void doDiscovery() {
        enableBT();
        if (btAdapter.isDiscovering()) btAdapter.cancelDiscovery();
        btAdapter.startDiscovery();
    }

	private void cancelDiscovery(){
    	btAdapter.cancelDiscovery();
    }
}
