package org.madn3s.robot.common;

import lejos.nxt.Button;
import lejos.nxt.Motor;
import lejos.robotics.Color;
import lejos.robotics.navigation.OmniPilot;

import org.json.JSONObject;
import org.madn3s.io.BluetoothTunnel;
import org.madn3s.sandbox.HTSensors;





public class Main {
	private static int i = 0;
	private static OmniPilot omniPilot;
	private static int n;
	private static double angle;
	private static float RADIUS = 9f;
	private static float WHEEL_DIAMETER = 4.8f;
	private static double circumferenceRadius;
	private static double distance;
	public static void main(String[] args) {
		
		
		omniPilot = new OmniPilot(RADIUS, WHEEL_DIAMETER, Motor.C, Motor.B, Motor.A, false, false);
		omniPilot.setTravelSpeed(15);
		circumferenceRadius = 45;
		distance = 45;// 2 * Math.PI * circumferenceRadius;
		
		n = 6;
		angle = 360 / n;
		
//		moveToNextPoint("move");
		
		
		Utils.printToScreen("Getting tunnel");
		BluetoothTunnel bTunnel = BluetoothTunnel.getInstance();
		Utils.printToScreen("Done tunnel");
		Utils.printToScreen("");
//		HTSensors inst = HTSensors.getInstance();
//		inst.calibrate();
//		Button.waitForAnyPress();
		boolean isLast = false, move = false, abort = false, finish = false;
		int counter = 0;
		while(!abort){
			Utils.printToScreen("-" + move + " " + isLast + " " + counter, 0,0, false);
			String message = null;
			while(message == null){
				message = bTunnel.readMessage();
				if(message != null && !message.isEmpty()){
//					JSONObject msgJsonObject = new JSONObject(message);
					String action =  message;//msgJsonObject.getString("action");
					Utils.printToScreen(action,0,1,false);
					Utils.printToScreen("YEA ",0,2,false);
					if(action.equalsIgnoreCase("move")){
						move = true;
					} else if(action.equalsIgnoreCase("wait")){
						move = false;
					} else if(action.equalsIgnoreCase("abort")){
						abort = true;
					} else if(action.equalsIgnoreCase("FINISH")){
						finish = true;
					}
				} else {
					Utils.printToScreen("NAY ",0,2,false);
				}
			}
			if(move){
				Utils.printToScreen("YEA",0,3,false);
				isLast = moveToNextPoint(message);
				if(!isLast){
//					JSONObject response = new JSONObject();
//					response.put("error", false);
//					response.put("message", "PICTURE");
//					bTunnel.writeMessage("PICTURE");
					bTunnel.writeMessage("{\"error\":false,\"message\":\"PICTURE\"}");
				} else {
//					JSONObject response = new JSONObject();
//					response.put("error", false);
//					response.put("message", "FINISH");
//					bTunnel.writeMessage("FINISH");
					bTunnel.writeMessage("{\"error\":false,\"message\":\"FINISH\"}");
				}
				move = false;
			} else {
				Utils.printToScreen("NAY",0,3,false);
			}
			if(finish){
//				JSONObject response = new JSONObject();
//				response.put("error", false);
//				response.put("message", "FINISH");
//				bTunnel.writeMessage("FINISH");
				bTunnel.writeMessage("{\"error\":false,\"message\":\"FINISH\"}");
				finish = false;
			}
			counter++;
		}
		Utils.printToScreen("Done",0,4,false);
		Button.waitForAnyPress();
//		
		
		
		

	}

	private static boolean moveToNextPoint(String msg) {
		if(msg.equalsIgnoreCase("FINISH")){
			return true;
		}

		//TODO: medimos y con el radio en 9 funciona casi perfecto, debe haber un pequeño error en la medicion
		omniPilot.travelArc(circumferenceRadius, distance, 90);
		return false;
	}

	private static void moveByColor() {
		long start = System.currentTimeMillis();
		HTSensors inst = HTSensors.getInstance();
		while(true){
			int cColor = inst.getCentralColor();
			switch (cColor) {
				case Color.BLACK:
					Motor.A.backward();
					Motor.B.forward();
					//move
					break;
				case Color.WHITE:
					Motor.A.forward();
			    	Motor.B.backward();
					//return to black
			    	//usando c???
					break;
				case Color.RED: 
					Motor.A.stop();
			    	Motor.B.stop();
					//check alignement
					//send pic signal
					break;
				case Color.GREEN: 
					Motor.A.stop();
			    	Motor.B.stop();
					//final??
					break;
				default:
					Motor.A.stop();
			    	Motor.B.stop();
					//where am i????
			    	//return to black
					break;
			}
			inst.printValues();
			Utils.printToScreen("time = " + (System.currentTimeMillis() - start), 0,4, false);
			if((System.currentTimeMillis() - start) > 100000){
				Motor.A.stop();
		    	Motor.B.stop();
				break;
			}
			//deberiamos poner un break ante una señal de stop recibida desde la tablet
		}
	}
}
