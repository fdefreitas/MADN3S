package org.madn3s.robot.common;

import lejos.nxt.Button;
import lejos.nxt.Motor;
import lejos.nxt.MotorPort;
import lejos.nxt.NXTMotor;
import lejos.robotics.Color;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.navigation.OmniPilot;

import org.json.JSONObject;
import org.madn3s.fedor.LFJfedor;
import org.madn3s.io.BluetoothTunnel;
import org.madn3s.sandbox.HTSensors;





public class Main {
	private static int i = 0;
	public static void main(String[] args) {
		Utils.printToScreen("MADN3S");
		
//		Button.waitForAnyPress();
//		Movement m = new Movement();
//		Sensors s = new Sensors();
//		Comms c = Comms.getInstance();
//		c.establishBTConnection();
//		HTSensors inst = HTSensors.getInstance();
//		inst.calibrate();
//		int button = Button.waitForAnyPress();
//		long start = System.currentTimeMillis();
		
//		LFJfedor lfJfedor;
//		try {
//			lfJfedor = new LFJfedor();
//			lfJfedor.initialize();
//		} catch (InterruptedException e) {
//			Utils.printToScreen(e.getMessage());
//		}
//		moveToNextPoint("");
//		Button.waitForAnyPress();
		
		lejos.robotics.navigation.OmniPilot omniPilot;
		omniPilot = new OmniPilot(105, 4.8f, Motor.C, Motor.B, Motor.A, false, false);
		omniPilot.setTravelSpeed(15);
//		omniPilot.forward();
//		try {
//			Thread.sleep(3000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		omniPilot.stop();
		
		omniPilot.travel(100,0);
		omniPilot.travel(-100,0);
//		omniPilot.rotate(9);
		
//		Utils.printToScreen("Getting tunnel");
//		BluetoothTunnel bTunnel = BluetoothTunnel.getInstance();
//		Utils.printToScreen("Done tunnel");
//		Utils.printToScreen("");
//		HTSensors inst = HTSensors.getInstance();
//		inst.calibrate();
//		Button.waitForAnyPress();
//		boolean isLast = false, move = false, abort = false, finish = false;
//		int counter = 0;
//		while(!abort){
//			Utils.printToScreen("-" + move + " " + isLast + " " + counter, 0,0, false);
//			String message = null;
//			while(message == null){
//				message = bTunnel.readMessage();
//				if(message != null && !message.isEmpty()){
//					JSONObject msgJsonObject = new JSONObject(message);
//					String action =  msgJsonObject.getString("action");
//					Utils.printToScreen(action,0,1,false);
//					Utils.printToScreen("YEA ",0,2,false);
//					if(action.equalsIgnoreCase("move")){
//						move = true;
//					} else if(action.equalsIgnoreCase("wait")){
//						move = false;
//					} else if(action.equalsIgnoreCase("abort")){
//						abort = true;
//					} else if(action.equalsIgnoreCase("FINISH")){
//						finish = true;
//					}
//				} else {
//					Utils.printToScreen("NAY ",0,2,false);
//				}
//			}
//			if(move){
//				Utils.printToScreen("YEA",0,3,false);
//				isLast = moveToNextPoint(message);
//				if(!isLast){
//					JSONObject response = new JSONObject();
//					response.put("error", false);
//					response.put("message", "PICTURE");
//					bTunnel.writeMessage(msg.toString());
//				} else {
//					JSONObject response = new JSONObject();
//					response.put("error", false);
//					response.put("message", "FINISH");
//					bTunnel.writeMessage(msg.toString());
//					bTunnel.writeMessage("{\"error\":false,\"message\":\"FINISH\"}");
//				}
//				move = false;
//			} else {
//				Utils.printToScreen("NAY",0,3,false);
//			}
//			if(finish){
//				JSONObject response = new JSONObject();
//				response.put("error", false);
//				response.put("message", "FINISH");
//				bTunnel.writeMessage(msg.toString());
//				finish = false;
//			}
//			counter++;
//		}
//		Utils.printToScreen("Done",0,4,false);
//		Button.waitForAnyPress();
		
		
		
		

	}

	private static boolean moveToNextPoint(String msg) {
		if(msg.equalsIgnoreCase("FINISH")){
			return true;
		}
		try {
			moveByColor();
			Motor.C.forward();
	    	Motor.A.backward();
			Thread.sleep(10000);
			Motor.C.stop();
	    	Motor.A.stop();
//	    	Motor.A.forward();
//	    	Motor.B.forward();
//	    	Motor.C.forward();
//	    	Thread.sleep(5000);
//	    	Motor.A.stop();
//	    	Motor.B.stop();
//	    	Motor.C.stop();
//	    	Motor.A.resetTachoCount();
//	        Motor.B.resetTachoCount();
//	        Motor.C.resetTachoCount();
	        Motor.A.rotate(90);
	    	Motor.B.rotate(90);
	    	Motor.C.rotate(90);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
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
