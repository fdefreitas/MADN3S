package org.madn3s.controller.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.text.method.CharacterPickerDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.madn3s.controller.R;
import org.madn3s.controller.io.BTConnection;

/**
 * Created by inaki on 12/7/13.
 */
public class MainFragment extends Fragment{

    public MainFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated (View view, Bundle savedInstanceState){
        Button goButton = (Button) view.findViewById(R.id.button);
        goButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BTConnection btc = BTConnection.getInstance();
                btc.startMindstormConnection();
                byte b = "hallo!".getBytes()[0];
                try {
                    long start = System.currentTimeMillis();
                    while(!btc.isConnected() && System.currentTimeMillis()-start < 10000){}
                    btc.writeMessage(b);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
