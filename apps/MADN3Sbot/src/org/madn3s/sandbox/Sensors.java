package org.madn3s.sandbox;

import lejos.nxt.Button;
import lejos.nxt.LightSensor;
import lejos.nxt.SensorPort;
import lejos.nxt.TouchSensor;

import org.madn3s.robot.common.Utils;

public class Sensors {
	
	private TouchSensor touch;
	private LightSensor light;
	public Sensors() {
		// TODO Auto-generated constructor stub
		touch = new TouchSensor(SensorPort.S1);
		light = new LightSensor(SensorPort.S4);
	}

	public void test1(){
		while (!touch.isPressed()) {
			light.calibrateHigh();
		}
		Utils.printToScreen("Esperando...");
		Button.waitForAnyPress();
		while (!touch.isPressed()) {
			light.calibrateLow();
		}
		Utils.printToScreen("Esperando...", 0,1, false);
		Button.waitForAnyPress();
		while (!touch.isPressed()) {
			Utils.printToScreen("light.gLV() = " + light.getLightValue(), 0,2, false);
			Utils.printToScreen("light.gNLV() = " + light.getNormalizedLightValue(), 0,3, false);
			Utils.printToScreen("S.S4.rRV() = " + SensorPort.S4.readRawValue(), 0,4, false);
			Utils.printToScreen("S.S4.rV() = " + SensorPort.S4.readValue(), 0,5, false);
		}
	}
}
