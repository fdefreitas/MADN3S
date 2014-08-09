package org.madn3s.controller.fragments;

import static org.madn3s.controller.MADN3SController.camera1;
import static org.madn3s.controller.MADN3SController.camera2;
import static org.madn3s.controller.MADN3SController.isCameraDevice;
import static org.madn3s.controller.MADN3SController.isToyDevice;
import static org.madn3s.controller.MADN3SController.nxt;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import org.madn3s.controller.MADN3SController;
import org.madn3s.controller.MADN3SController.Mode;
import org.madn3s.controller.R;
import org.madn3s.controller.io.BTConnection;
import org.madn3s.controller.models.NewDevicesAdapter;
import org.madn3s.controller.models.PairedDevicesAdapter;

import java.util.ArrayList;

/**
 * Created by inaki on 12/7/13.
 */
public class DiscoveryFragment extends BaseFragment{
	public static final String tag = "MainFragment";
	public static final String EXTRA_DEVICE_ADDRESS = "device_address";

	private ListView nxtNewDevicesListView, nxtPairedDevicesListView;
	private ListView cameraNewDevicesListView, cameraPairedDevicesListView;
	private LinearLayout nxtDevicesLayout, cameraDevicesLayout;
	private ProgressBar discoveryProgress;
	private Button connectButton;
	private Button scanButton;
	private NewDevicesAdapter nxtNewDevicesAdapter, cameraNewDevicesAdapter ;
	private PairedDevicesAdapter nxtPairedDevicesAdapter, cameraPairedDevicesAdapter;
	private boolean isNxtSelected;
	private int cams;
	
	public DiscoveryFragment() {
		isNxtSelected =  false;
		cams = 0;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_discovery, container, false);
	}

	@Override
	public void onViewCreated (View view, Bundle savedInstanceState){
		super.onViewCreated(view, savedInstanceState);
		
		nxtDevicesLayout = (LinearLayout) getView().findViewById(R.id.nxt_devices_layout);
		cameraDevicesLayout = (LinearLayout) getView().findViewById(R.id.camera_devices_layout);
		
		nxtPairedDevicesListView = (ListView) getView().findViewById(R.id.nxt_paired_devices_listView);
		nxtNewDevicesListView = (ListView) getView().findViewById(R.id.nxt_new_devices_listView);
		discoveryProgress = (ProgressBar) getView().findViewById(R.id.discovery_progressBar);

		cameraPairedDevicesListView = (ListView) getView().findViewById(R.id.camera_paired_devices_listView);
		cameraNewDevicesListView = (ListView) getView().findViewById(R.id.cameras_new_devices_listView);

		scanButton = (Button) getView().findViewById(R.id.scan_button);
		scanButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				
				discoveryProgress.setVisibility(View.VISIBLE);
				nxtDevicesLayout.setVisibility(View.GONE);
				cameraDevicesLayout.setVisibility(View.GONE);
				
				Log.d(tag, "Starting Discovery");
				BTConnection btc = BTConnection.getInstance();
				btc.doDiscovery();

//				byte b = "hallo!".getBytes()[0];
				try {
					ArrayList<BluetoothDevice> temporaryPairedDevices = new ArrayList<BluetoothDevice>();
					for(BluetoothDevice device:  BTConnection.pairedDevices){
						if (isToyDevice(device)){
							temporaryPairedDevices.add(device);
							Log.d(tag, "For de Toy filter: "+device.getName());
						}
					}
					
					nxtPairedDevicesAdapter = new PairedDevicesAdapter(temporaryPairedDevices, getActivity().getBaseContext());
					nxtPairedDevicesListView.setAdapter(nxtPairedDevicesAdapter);
					nxtPairedDevicesListView.setOnItemClickListener(onDeviceAdapterClickListener);

					nxtNewDevicesAdapter = new NewDevicesAdapter(getActivity().getBaseContext());
					nxtNewDevicesListView.setAdapter(nxtNewDevicesAdapter);
					nxtNewDevicesListView.setOnItemClickListener(onDeviceAdapterClickListener);

					temporaryPairedDevices = new ArrayList<BluetoothDevice>();
					for(BluetoothDevice device:  BTConnection.pairedDevices){
						temporaryPairedDevices.add(device);
						Log.d(tag, "for camera filter: "+device.getName());
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
					
					if(true /*isNxtSelected && cams == 2*/){
						Log.d(tag, "Mode: SCANNER");
						listener.onObjectSelected(Mode.SCANNER);
						
					} else if (isNxtSelected){
						
						Log.d(tag, "Mode: CONTROLLER");
						AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
							alertDialogBuilder.setTitle("Iniciar Modo Control Remoto");
							alertDialogBuilder
								.setMessage("No ha seleccionado Cámaras. Está seguro que desea iniciar el modo 'Control Remoto' del NXT?")
								.setCancelable(true)
								.setPositiveButton("Yes",new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,int id) {
										listener.onObjectSelected(Mode.CONTROLLER);
									}
								  })
								.setNegativeButton("No",new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,int id) {
										dialog.cancel();
									}
								});				 
						alertDialogBuilder.create().show();
						
					} else {
						Toast.makeText(getActivity(), "Debe seleccionar al menos un dispositivo NXT", Toast.LENGTH_LONG).show();
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
			getActivity().unregisterReceiver(mReceiver);
		}
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
				camera1 = deviceTemp;
				cams++;
			} else if(cams == 1){
				camera2 = deviceTemp;
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
				nxtDevicesLayout.setVisibility(View.VISIBLE);
				cameraDevicesLayout.setVisibility(View.VISIBLE);
				connectButton.setEnabled(true);
			}
		}
	};
}
