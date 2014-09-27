package org.madn3s.robot.common;

import lejos.nxt.Button;
import lejos.nxt.Motor;
import lejos.nxt.SensorPort;
import lejos.nxt.UltrasonicSensor;
import lejos.robotics.navigation.OmniPilot;

import org.json.JSONException;
import org.json.JSONObject;
import org.madn3s.io.BluetoothTunnel;

public class Main {
	private static float RADIUS = 9f;
	private static float WHEEL_DIAMETER = 4.8f;
	public static void main(String[] args) {
		
		Utils.printToScreen("Getting tunnel");
		BluetoothTunnel bTunnel = BluetoothTunnel.getInstance();
		Utils.printToScreen("Done tunnel");
		Utils.printToScreen("");
//		
		int points = 10;
		int travelSpeed = 15;

		OmniPilot omniPilot = new OmniPilot(RADIUS, WHEEL_DIAMETER, Motor.C, Motor.B, Motor.A, false, false);
		omniPilot.setTravelSpeed(travelSpeed);
//		double circumferenceRadius = 45;
		
		UltrasonicSensor uSensor = new UltrasonicSensor(SensorPort.S1);
		Scanner scanner = new Scanner(omniPilot, points, travelSpeed, RADIUS, WHEEL_DIAMETER, uSensor);

//		try {
//			scanner.processMsg(new JSONObject("{'command' : 'scan','action' : 'config'}"));
//			for(int i=0; i<points-1; i++){
//				scanner.processMsg(new JSONObject("{'command' : 'scan','action' : 'move'}"));
//				Button.waitForAnyPress();
//			}
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
		BluetoothControlled controlled = new BluetoothControlled(omniPilot, uSensor);
		
		boolean abort = false;
		while(!abort){
			Utils.printToScreen(scanner.getPoints() + " " + scanner.getCircumferenceRadius() + " " + scanner.getDistance(), 0,0, false);
			String message = null;
			while(message == null){
				message = bTunnel.readMessage();
				if(message != null && !message.isEmpty()){
					try{
						Utils.printToScreen(message, 0,1, true);
						JSONObject msg = new JSONObject(message);
						String command = msg.getString("command");
						if(command.equalsIgnoreCase("scanner")){
							scanner.processMsg(msg);
						} if(command.equalsIgnoreCase("rc")){
							controlled.processMsg(msg);
						} if(command.equalsIgnoreCase("abort")){
							abort = true;
						}
					} catch (Exception e){
						Utils.printToScreen(e.getMessage());
					}
				} 
			}
		}
	}
}
