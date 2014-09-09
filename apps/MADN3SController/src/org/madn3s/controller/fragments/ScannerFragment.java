package org.madn3s.controller.fragments;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONObject;
import org.madn3s.controller.MADN3SController;
import org.madn3s.controller.R;
import org.madn3s.controller.MADN3SController.Device;
import org.madn3s.controller.MADN3SController.Mode;
import org.madn3s.controller.MADN3SController.State;
import org.madn3s.controller.io.BraveHeartMidgetService;
import org.madn3s.controller.io.HiddenMidgetReader;
import org.madn3s.controller.io.UniversalComms;

import android.R.integer;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class ScannerFragment extends BaseFragment {

	public static final String tag = "ScannerFragment";
	public static UniversalComms bridge;
	
	private ScannerFragment mFragment;
	private Button generateModelButton;
	
	public ScannerFragment() {
		mFragment = this;
		BraveHeartMidgetService.scannerBridge = new UniversalComms() {
			@Override
			public void callback(Object msg) {
				Bundle bundle = (Bundle)msg;
				final Device device = Device.setDevice(bundle.getInt("device"));
				final State state = State.setState(bundle.getInt("state"));
				int iter = bundle.containsKey("iter")?bundle.getInt("iter"):-1;
				Log.d(tag, device + " " + state + " " + iter);
//				mFragment.getView().post(
//					new Runnable() { 
//						public void run() { 
//							//update UI
//						} 
//					}
//				); 
			}
		};
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_scanner, container, false);
	}

	@Override
	public void onViewCreated (View view, Bundle savedInstanceState){
		super.onViewCreated(view, savedInstanceState);
		generateModelButton = (Button) view.findViewById(R.id.model_generation_button);
		generateModelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				scan();
			}
		});
	}

	@Override
	public void onDestroy(){
		super.onDestroy();
	}
	
	public void scan(){
		try{
			String timeStamp = new SimpleDateFormat("yyyyMMdd_HH").format(new Date());
			String projectName = "HereIAm-" + timeStamp;
			MADN3SController.sharedPrefsPutString("project_name", projectName);
			MADN3SController.sharedPrefsPutInt("points", 6);
			JSONObject json = new JSONObject();
	        json.put("action", "photo");
	        json.put("project_name", projectName);
	        bridge.callback(json.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
