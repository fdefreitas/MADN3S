package org.madn3s.robot.common;

import lejos.nxt.Button;
import lejos.nxt.Motor;
import lejos.robotics.Color;

import org.madn3s.sandbox.HTSensors;




public class Main {
	public static void main(String[] args) {
		Utils.printToScreen("MADN3S");
		
		Button.waitForAnyPress();
//		Movement m = new Movement();
//		Sensors s = new Sensors();
//		Comms c = Comms.getInstance();
//		c.establishBTConnection();
		HTSensors inst = HTSensors.getInstance();
		inst.calibrate();
		int button = Button.waitForAnyPress();
		long start = System.currentTimeMillis();
		while(!Utils.buttonIsEscape(button)){
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
			button = Button.waitForAnyPress();
		}
	}
}
