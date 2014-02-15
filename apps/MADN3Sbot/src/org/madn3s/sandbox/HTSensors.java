package org.madn3s.sandbox;

import lejos.nxt.Button;
import lejos.nxt.SensorPort;
import lejos.nxt.addon.ColorHTSensor;

import org.madn3s.robot.common.Utils;

public class HTSensors {

	private static HTSensors me ;
	private ColorHTSensor colorCenter;
	private ColorHTSensor colorExt;
		
	private HTSensors() {
		colorCenter = new ColorHTSensor(SensorPort.S1);
		colorExt = new ColorHTSensor(SensorPort.S2);
	}
	
	public static HTSensors getInstance(){
		if(me == null){
			me = new HTSensors();
		}
		return me;
	}

	public ColorHTSensor getColorCenter() {
		return colorCenter;
	}

	public void setColorCenter(ColorHTSensor colorCenter) {
		this.colorCenter = colorCenter;
	}

	public ColorHTSensor getColorExt() {
		return colorExt;
	}

	public void setColorExt(ColorHTSensor colorExt) {
		this.colorExt = colorExt;
	}
	
	public void calibrate(){
		Utils.printToScreen("Calibrar...");
		Button.waitForAnyPress();
		colorCenter.initBlackLevel();
		colorExt.initBlackLevel();
		Utils.printToScreen("Esperando...");
		Button.waitForAnyPress();
		colorCenter.initWhiteBalance();
		colorExt.initWhiteBalance();
		Utils.printToScreen("Esperando...", 0,1, false);
		Button.waitForAnyPress();
	}
	
	public void printValues(){
		Utils.printToScreen("cC.gCID() = " + colorCenter.getColorID(), 0,2, false);
		Utils.printToScreen("cE.gCID() = " + colorExt.getColorID(), 0,3, false);
	}
	
	public int[] getColor(){
		int[] toReturn = new int[2];
		toReturn[0] = colorCenter.getColorID();
		toReturn[1] = colorExt.getColorID();
		return toReturn;
	}
	
	public int getExternalColor(){
		return colorExt.getColorID();
	}
	
	public int getCentralColor(){
		return colorCenter.getColorID();
	}
	
	public boolean isSensorColor(boolean center, int color){
		if(center){
			return colorCenter.getColorID() == color;
		} else {
			return colorExt.getColorID() == color;
		}
	}
	
	
	 
	
}
