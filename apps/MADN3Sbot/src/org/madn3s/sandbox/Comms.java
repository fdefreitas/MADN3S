package org.madn3s.sandbox;

import java.util.ArrayList;

import javax.bluetooth.DeviceClass;
import javax.bluetooth.RemoteDevice;

import org.madn3s.robot.common.Utils;

import lejos.nxt.Button;
import lejos.nxt.comm.Bluetooth;
import lejos.nxt.comm.NXTConnection;

public class Comms {

	private NXTConnection bluetoothConnection;
	private static Comms instance;
	int timeout = 5000;
	
	private Comms(){
	}
	
	public static Comms getInstance(){
		if(instance == null){
			instance = new Comms();
		}
		
		return instance;
	}
	
	public boolean establishBTConnection(){
		bluetoothConnection  = Bluetooth.waitForConnection();
		bluetoothConnection.setIOMode(NXTConnection.RAW);
//		RemoteDevice rd = new RemoteDevice("PlesseTablet", "AC:22:0B:61:7A:4F", 0x04);
//		Utils.printToScreen(rd.getDeviceClass()+rd.getFriendlyName(false));
//		Button.waitForAnyPress();
//		try {
//			Bluetooth.addDevice(rd);
//		} catch (Exception e){
//			Utils.printToScreen("booom");
//		}
//		Button.waitForAnyPress();
//		
//		ArrayList<RemoteDevice> devices = Bluetooth.getKnownDevicesList();
//		int i =0;
//		Utils.printToScreen(String.valueOf(devices.size()), 0, 0);
//		
//		if(null != devices && devices.size()>0){
//			for(RemoteDevice rdf:devices){
//				Utils.printToScreen(rdf.getDeviceClass()+rdf.getFriendlyName(false), 0, i, false);
//				i++;
//			}
//		}
		
		
		if(Bluetooth.getStatus() == 1){
			Utils.printToScreen("Bluetooth Conectado");
			Button.waitForAnyPress();
			return true;
		}else{
			Utils.printToScreen("Bluetooth NO se pudo conectar");
			Button.waitForAnyPress();
			return false;
		}
	}
	
	public String getBTDevice(){
		return Bluetooth.getName();
	}
}
