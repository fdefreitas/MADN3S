package org.madn3s.controller.fragments;

import static org.madn3s.controller.MADN3SController.camera1;
import static org.madn3s.controller.MADN3SController.camera1WeakReference;
import static org.madn3s.controller.MADN3SController.camera2;
import static org.madn3s.controller.MADN3SController.camera2WeakReference;

import org.json.JSONObject;
import org.madn3s.controller.MADN3SController;
import org.madn3s.controller.R;
import org.madn3s.controller.io.HiddenMidgetWriter;
import org.madn3s.controller.io.UniversalComms;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

public class SettingsFragment extends BaseFragment {
	private static final String tag = "SettingsFragment";
	public static UniversalComms bridge;
	private EditText pointsEditText;
	private EditText speedEditText;
	private EditText radiusEditText;
	
	private CheckBox cleanImagesCheckBox;
	
	private EditText p1xEditText;
	private EditText p1yEditText;
	private EditText p2xEditText;
	private EditText p2yEditText;
	private EditText iterationsEditText;
	
	private EditText maxCornersEditText;
	private EditText qualityLevelEditText;
	private EditText minDistanceEditText;
	
	private EditText upperThresholdEditText;
	private EditText lowerThresholdEditText;
	
	private EditText dDepthEditText;
	private EditText dXEditText;
	private EditText dYEditText;
	
	private RadioGroup algortihmRadioGroup;
	
	private Button saveButton;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		return inflater.inflate(R.layout.fragment_settings, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		pointsEditText = (EditText) getView().findViewById(R.id.points_editText);
		pointsEditText.setText(""+MADN3SController.sharedPrefsGetInt("points"));
		
		speedEditText = (EditText) getView().findViewById(R.id.speed_editText);
		speedEditText.setText(""+MADN3SController.sharedPrefsGetInt("speed"));
		
		radiusEditText = (EditText) getView().findViewById(R.id.radius_editText);
		radiusEditText.setText(""+MADN3SController.sharedPrefsGetFloat("radius"));
		
		cleanImagesCheckBox = (CheckBox) getView().findViewById(R.id.clean_checkBox);
		cleanImagesCheckBox.setChecked(MADN3SController.sharedPrefsGetBoolean("clean"));
		
		p1xEditText = (EditText) getView().findViewById(R.id.p1_x_editText);
		p1yEditText = (EditText) getView().findViewById(R.id.p1_y_editText);
		p2xEditText = (EditText) getView().findViewById(R.id.p2_x_EditText);
		p2yEditText = (EditText) getView().findViewById(R.id.p2_y_editText);
		iterationsEditText = (EditText) getView().findViewById(R.id.iterations_editText);
		p1xEditText.setText(""+MADN3SController.sharedPrefsGetInt("p1x"));
		p1yEditText.setText(""+MADN3SController.sharedPrefsGetInt("p1y"));
		p2xEditText.setText(""+MADN3SController.sharedPrefsGetInt("p2x"));
		p2yEditText.setText(""+MADN3SController.sharedPrefsGetInt("p2y"));
		iterationsEditText.setText(""+MADN3SController.sharedPrefsGetInt("iterations"));
		
		maxCornersEditText = (EditText) getView().findViewById(R.id.max_corners_editText);
		qualityLevelEditText = (EditText) getView().findViewById(R.id.quality_level_editText);
		minDistanceEditText = (EditText) getView().findViewById(R.id.min_distance_editText);
		maxCornersEditText.setText(""+MADN3SController.sharedPrefsGetInt("maxCorners"));
		qualityLevelEditText.setText(""+MADN3SController.sharedPrefsGetFloat("qualityLevel"));
		minDistanceEditText.setText(""+MADN3SController.sharedPrefsGetInt("minDistance"));
		
		upperThresholdEditText = (EditText) getView().findViewById(R.id.upper_threshold_editText);
		lowerThresholdEditText = (EditText) getView().findViewById(R.id.lower_threshold_editText);
		upperThresholdEditText.setText(""+MADN3SController.sharedPrefsGetFloat("upperThreshold"));
		lowerThresholdEditText.setText(""+MADN3SController.sharedPrefsGetFloat("lowerThreshold"));
		
		dDepthEditText = (EditText) getView().findViewById(R.id.d_depth_editText);
		dXEditText = (EditText) getView().findViewById(R.id.d_x_editText);
		dYEditText = (EditText) getView().findViewById(R.id.d_y_editText);
		dDepthEditText.setText(""+MADN3SController.sharedPrefsGetInt("dDepth"));
		dXEditText.setText(""+MADN3SController.sharedPrefsGetInt("dX"));
		dYEditText.setText(""+MADN3SController.sharedPrefsGetInt("dY"));
		
		algortihmRadioGroup = (RadioGroup) getView().findViewById(R.id.algorithm_radioGroup);
		algortihmRadioGroup.check(MADN3SController.sharedPrefsGetInt("algorithmIndex"));
		
		saveButton = (Button) getView().findViewById(R.id.settings_save_button);
		saveButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(!pointsEditText.getText().toString().isEmpty()){
					MADN3SController.sharedPrefsPutInt("points", Integer.parseInt(pointsEditText.getText().toString()));
				}
				
				if(!speedEditText.getText().toString().isEmpty()){
					MADN3SController.sharedPrefsPutFloat("speed", Float.parseFloat(speedEditText.getText().toString()));
				}
				
				if(!radiusEditText.getText().toString().isEmpty()){
					MADN3SController.sharedPrefsPutFloat("radius", Float.parseFloat(radiusEditText.getText().toString()));
				}
				
				if(!p1xEditText.getText().toString().isEmpty()){
					MADN3SController.sharedPrefsPutInt("p1x", Integer.parseInt(p1xEditText.getText().toString()));
				}
				
				if(!p1yEditText.getText().toString().isEmpty()){
					MADN3SController.sharedPrefsPutInt("p1y", Integer.parseInt(p1yEditText.getText().toString()));
				}
				
				if(!p2xEditText.getText().toString().isEmpty()){
					MADN3SController.sharedPrefsPutInt("p2x", Integer.parseInt(p2xEditText.getText().toString()));
				}
				
				if(!p2yEditText.getText().toString().isEmpty()){
					MADN3SController.sharedPrefsPutInt("p2y", Integer.parseInt(p2yEditText.getText().toString()));
				}
				
				if(!iterationsEditText.getText().toString().isEmpty()){
					MADN3SController.sharedPrefsPutInt("iterations", Integer.parseInt(iterationsEditText.getText().toString()));
				}
				
				if(!maxCornersEditText.getText().toString().isEmpty()){
					MADN3SController.sharedPrefsPutInt("maxCorners", Integer.parseInt(maxCornersEditText.getText().toString()));
				}
				
				if(!qualityLevelEditText.getText().toString().isEmpty()){
					MADN3SController.sharedPrefsPutFloat("qualityLevel", Float.parseFloat(qualityLevelEditText.getText().toString()));
				}
				
				if(!minDistanceEditText.getText().toString().isEmpty()){
					MADN3SController.sharedPrefsPutInt("minDistance", Integer.parseInt(minDistanceEditText.getText().toString()));
				}
				
				if(!upperThresholdEditText.getText().toString().isEmpty()){
					MADN3SController.sharedPrefsPutFloat("upperThreshold", Float.parseFloat(upperThresholdEditText.getText().toString()));
				}
				
				if(!lowerThresholdEditText.getText().toString().isEmpty()){
					MADN3SController.sharedPrefsPutFloat("lowerThreshold", Float.parseFloat(lowerThresholdEditText.getText().toString()));
				}
				
				if(!dDepthEditText.getText().toString().isEmpty()){
					MADN3SController.sharedPrefsPutInt("dDepth", Integer.parseInt(dDepthEditText.getText().toString()));
				}
				
				if(!dXEditText.getText().toString().isEmpty()){
					MADN3SController.sharedPrefsPutInt("dX", Integer.parseInt(dXEditText.getText().toString()));
				}
				
				if(!dYEditText.getText().toString().isEmpty()){
					MADN3SController.sharedPrefsPutInt("dY", Integer.parseInt(dYEditText.getText().toString()));
				}
				
				RadioButton selectedAlgorithmRadioButton = (RadioButton) getView().findViewById(algortihmRadioGroup.getCheckedRadioButtonId());
				MADN3SController.sharedPrefsPutString("algorithm", selectedAlgorithmRadioButton.getText().toString());
				MADN3SController.sharedPrefsPutInt("algorithmIndex", algortihmRadioGroup.getCheckedRadioButtonId());
				
				MADN3SController.sharedPrefsPutBoolean("clean", cleanImagesCheckBox.isChecked());
				
				if(MADN3SController.camera1 != null && MADN3SController.camera2 != null && MADN3SController.talker != null){
					try{
						JSONObject json = new JSONObject();
				        json.put("action", "config");
				        json.put("clean", MADN3SController.sharedPrefsGetBoolean("clean"));
				        
				        JSONObject grabCut = new JSONObject();
				        JSONObject rectangle = new JSONObject();
				        JSONObject point1 = new JSONObject();
				        point1.put("x", MADN3SController.sharedPrefsGetInt("p1x"));
				        point1.put("y", MADN3SController.sharedPrefsGetInt("p1y"));
				        rectangle.put("point_1", point1);
				        JSONObject point2 = new JSONObject();
				        point2.put("x", MADN3SController.sharedPrefsGetInt("p2x"));
				        point2.put("y", MADN3SController.sharedPrefsGetInt("p2y"));
				        rectangle.put("point_2", point2);
				        grabCut.put("rectangle", rectangle);
				        grabCut.put("iterations", MADN3SController.sharedPrefsGetInt("iterations"));
				        json.put("grab_cut", grabCut);
				        
				        JSONObject goodFeatures = new JSONObject();
				        goodFeatures.put("max_corners", MADN3SController.sharedPrefsGetInt("maxCorners"));
				        goodFeatures.put("quality_level", MADN3SController.sharedPrefsGetFloat("qualityLevel"));
				        goodFeatures.put("min_distance", MADN3SController.sharedPrefsGetInt("minDistance"));
				        json.put("good_features", goodFeatures);
				        
				        JSONObject edgeDetection = new JSONObject();
				        edgeDetection.put("algorithm", MADN3SController.sharedPrefsGetString("algorithm"));
				        edgeDetection.put("algorithm_index", MADN3SController.sharedPrefsGetInt("algorithmIndex"));
				        JSONObject canny = new JSONObject();
				        canny.put("lower_threshold", MADN3SController.sharedPrefsGetFloat("lowerThreshold"));
				        canny.put("upper_threshold", MADN3SController.sharedPrefsGetFloat("upperThreshold"));
				        edgeDetection.put("canny_config", canny);
				        JSONObject sobel = new JSONObject();
				        sobel.put("d_depth", MADN3SController.sharedPrefsGetInt("dDepth"));
				        sobel.put("d_x", MADN3SController.sharedPrefsGetInt("dX"));
				        sobel.put("d_y", MADN3SController.sharedPrefsGetInt("dY"));
				        edgeDetection.put("sobel_config", sobel);
				        json.put("edge_detection", edgeDetection);
//				        Log.d(tag, json.toString());
//				        bridge.callback(json.toString());
				        
				        if(camera1WeakReference != null){
				        	json.put("side", "left");
				        	json.put("camera_name", camera1.getName());
							HiddenMidgetWriter sendCamera1 = new HiddenMidgetWriter(camera1WeakReference, json.toString());
							sendCamera1.execute();
					        Log.d(tag, "Enviando a Camara1: " + camera1.getName());
					        MADN3SController.readCamera1.set(true);
						} else {
							Log.d(tag, "camera1WeakReference null");
						}
						
						if(camera2WeakReference != null){
							json.put("side", "right");
							json.put("camera_name", camera2.getName());
							HiddenMidgetWriter sendCamera2 = new HiddenMidgetWriter(camera2WeakReference, json.toString());
							sendCamera2.execute();
					        Log.d(tag, "Enviando a Camara2: " + camera2.getName());
					        MADN3SController.readCamera2.set(true);
						} else {
							Log.d(tag, "camera2WeakReference null");
						}
						
						JSONObject nxtJson = new JSONObject();
            	        nxtJson.put("command", "scanner");
            	        nxtJson.put("action", "config");
            	        nxtJson.put("points", MADN3SController.sharedPrefsGetInt("points"));
            	        nxtJson.put("circumference_radius", MADN3SController.sharedPrefsGetFloat("radius"));
            	        nxtJson.put("speed", MADN3SController.sharedPrefsGetInt("speed"));
            	        MADN3SController.talker.write(nxtJson.toString().getBytes());
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					Toast.makeText(getActivity(), "No hay dispositivos a enviar la data. Data Guardada", Toast.LENGTH_LONG).show();
				}
			}
		});
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();
	    inflater.inflate(R.menu.settings, menu);
	    super.onCreateOptionsMenu(menu,inflater);
	}
}
