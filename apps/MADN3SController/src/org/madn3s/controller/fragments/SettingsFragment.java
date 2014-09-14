package org.madn3s.controller.fragments;

import org.madn3s.controller.MADN3SController;
import org.madn3s.controller.R;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SettingsFragment extends BaseFragment {
	private static final String tag = "SettingsFragment";

	private EditText pointsEditText;
	
	private EditText p1xEditText;
	private EditText p1yEditText;
	private EditText p2xEditText;
	private EditText p2yEditText;
	
	private EditText maxCornersEditText;
	private EditText qualityLevelEditText;
	private EditText minDistanceEditText;
	
	private EditText upperThresholdEditText;
	private EditText lowerThresholdEditText;
	
	private EditText dDepthEditText;
	private EditText dXEditText;
	private EditText dYEditText;
	
	private Button saveButton;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_settings, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		pointsEditText = (EditText) getView().findViewById(R.id.points_text_edit);
		
		
		p1xEditText = (EditText) getView().findViewById(R.id.p1_x_text_edit);
		p1yEditText = (EditText) getView().findViewById(R.id.p1_y_text_edit);
		p2xEditText = (EditText) getView().findViewById(R.id.p2_x_text_edit);
		p2yEditText = (EditText) getView().findViewById(R.id.p2_y_text_edit);
		
		maxCornersEditText = (EditText) getView().findViewById(R.id.max_corner_text_edit);
		qualityLevelEditText = (EditText) getView().findViewById(R.id.quality_level_text_edit);
		minDistanceEditText = (EditText) getView().findViewById(R.id.min_distance_text_edit);
		
		upperThresholdEditText = (EditText) getView().findViewById(R.id.upper_threshold_text_edit);
		lowerThresholdEditText = (EditText) getView().findViewById(R.id.lower_threshold_edit_text);
		
		dDepthEditText = (EditText) getView().findViewById(R.id.d_depth_text_edit);
		dXEditText = (EditText) getView().findViewById(R.id.d_x_text_edit);
		dYEditText = (EditText) getView().findViewById(R.id.d_y__text_edit);
		
		saveButton = (Button) getView().findViewById(R.id.settings_save_button);
		saveButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(tag, pointsEditText.getText().toString());
				
			}
		});
	}
	
	
	
}
