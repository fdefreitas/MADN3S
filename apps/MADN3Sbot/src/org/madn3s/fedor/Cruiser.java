package org.madn3s.fedor;
import org.madn3s.robot.common.Utils;
import org.madn3s.sandbox.HTSensors;

import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.Motor;
import lejos.nxt.MotorPort;
import lejos.nxt.NXTMotor;
import lejos.robotics.Color;
import lejos.util.Delay;

public class Cruiser extends Thread {

	final static int DEFAULT_POWER = 50;

	NXTMotor mA = new NXTMotor(MotorPort.A);
	NXTMotor mB = new NXTMotor(MotorPort.B);
	NXTMotor mC = new NXTMotor(MotorPort.C);	
	int color;
	int turn;
	int bTurn;
	int aTurn;
	int power = 22;
	int threshold;
	int angle = -320;
	
	public Cruiser() {
	}

	@SuppressWarnings("deprecation")
	public void run() {

		LCD.clear();
		LCD.drawString("Started Cruiser", 0, 2);
		Utils.printToScreen("Avg: "+String.valueOf(LFUtils.getAvgColorValue()));
		
		Utils.printToScreen("B: " + LFJfedor.lvh.getBlack(), 0, 1, false);
		Utils.printToScreen("W: " + LFJfedor.lvh.getWhite(), 0, 2, false);
		
		Button.waitForAnyPress(); 

		while (!Button.ESCAPE.isPressed()) {
			color = HTSensors.getInstance().getCentralColor();
			Utils.printToScreen("Read: " + color);	
			switch(color){
				case Color.WHITE:
					mB.setPower((int) Math.round(DEFAULT_POWER*0.75));
					mB.forward();
//						mB.stop();
					
					mA.setPower((int) Math.round(DEFAULT_POWER*0.75));
					mA.backward();
//						mA.stop();
					
					break;
				case Color.RED:
					Delay.msDelay(200);
				
			    	Motor.A.stop(true);
			    	Motor.B.stop(true);
			    	Motor.C.stop(true);
			    	
			    	Motor.A.rotate(angle, true);
			    	Motor.B.rotate(angle, true);
			    	Motor.C.rotate(angle, true);
			    	
			    	while(Motor.A.isMoving() || Motor.B.isMoving() || Motor.C.isMoving()){}
			    	
			    	Motor.A.flt();
			    	Motor.B.flt();
			    	Motor.C.flt();
			    	
			    	Motor.A.backward();
			    	Motor.B.forward();
					
					Delay.msDelay(800);
			    	
			    	break;
				default:
				case Color.BLACK:
					mB.setPower(DEFAULT_POWER);
					mB.forward();
					
					mA.setPower(DEFAULT_POWER);
					mA.backward();
					break;
				case Color.GREEN: 
					mA.stop();
			    	mB.stop();
					break;
			}
			
//			threshold = (LFJfedor.lvh.getBlack() + LFJfedor.lvh.getWhite())/2;
//				
//			bTurn = power - 30 * (threshold-color);
//			mB.setPower(bTurn);
//			mB.forward();
//
//			aTurn = power + 50 * (threshold-color);
//			mA.setPower(aTurn);
//			mA.backward();


		}
	}

}