package org.madn3s.controller.fragments;

import org.madn3s.controller.R;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ScannerFragment extends BaseFragment {

	public static final String tag = "ScannerFragment";
	
	private ScannerFragment mFragment;
	
	public ScannerFragment() {
		mFragment = this;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_scanner, container, false);
	}

	@Override
	public void onViewCreated (View view, Bundle savedInstanceState){
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void onDestroy(){
		super.onDestroy();
	}
}
