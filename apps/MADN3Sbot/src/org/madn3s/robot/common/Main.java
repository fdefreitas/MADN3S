package org.madn3s.robot.common;

import lejos.nxt.Button;
import lejos.nxt.Motor;
import lejos.robotics.Color;

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
		
		Utils.printToScreen("Getting tunnel");
		BluetoothTunnel bTunnel = BluetoothTunnel.getInstance();
		Utils.printToScreen("Done tunnel");
		Utils.printToScreen("");
		boolean isLast = false, move = false;
		int counter = 0;
		while(!isLast){
			Utils.printToScreen("-" + move + " " + isLast + " " + counter, 0,0, false);
			String message = null;
			while(message == null){
				message = bTunnel.readMessage();
				if(message != null && !message.isEmpty()){
					Utils.printToScreen(message,0,1,false);
					Utils.printToScreen("YEA ",0,2,false);
					move = true;
				} else {
					Utils.printToScreen("NAY ",0,2,false);
				}
			}
			if(move){
				Utils.printToScreen("YEA",0,3,false);
				isLast = moveToNextPoint(message);
				if(!isLast){
					bTunnel.writeMessage("{\"error\":false,\"message\":\"PICTURE\"}");
				} else {
					bTunnel.writeMessage("{\"error\":false,\"message\":\"FINISH\"}");
				}
				move = false;
			} else {
				Utils.printToScreen("NAY",0,3,false);
			}
			counter++;
		}
		Utils.printToScreen("Done",0,4,false);
		Button.waitForAnyPress();
		
		
		
		
//		while(!Utils.buttonIsEscape(button)){
//			while(true){
//				int cColor = inst.getCentralColor();
//				switch (cColor) {
//					case Color.BLACK:
//						Motor.A.backward();
//						Motor.B.forward();
//						//move
//						break;
//					case Color.WHITE:
//						Motor.A.forward();
//				    	Motor.B.backward();
//						//return to black
//				    	//usando c???
//						break;
//					case Color.RED: 
//						Motor.A.stop();
//				    	Motor.B.stop();
//						//check alignement
//						//send pic signal
//						break;
//					case Color.GREEN: 
//						Motor.A.stop();
//				    	Motor.B.stop();
//						//final??
//						break;
//					default:
//						Motor.A.stop();
//				    	Motor.B.stop();
//						//where am i????
//				    	//return to black
//						break;
//				}
//				inst.printValues();
//				Utils.printToScreen("time = " + (System.currentTimeMillis() - start), 0,4, false);
//				if((System.currentTimeMillis() - start) > 100000){
//					Motor.A.stop();
//			    	Motor.B.stop();
//					break;
//				}
//				//deberiamos poner un break ante una señal de stop recibida desde la tablet
//			}
//			button = Button.waitForAnyPress();
//		}
	}

	private static boolean moveToNextPoint(String msg) {
		if(msg.equalsIgnoreCase("FINISH")){
			return true;
		}
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return false;
	}
}
