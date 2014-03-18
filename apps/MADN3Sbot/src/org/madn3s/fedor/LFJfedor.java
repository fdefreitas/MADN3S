package org.madn3s.fedor;
import org.madn3s.sandbox.HTSensors;

import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.Sound;

public class LFJfedor {

	static LineValueHolder lvh = new LineValueHolder();

	public LFJfedor() throws InterruptedException {

		waitForUser("white");
		lvh.setWhite(HTSensors.getInstance().getCentralColor());
//		lvh.setWhite(getThreshold());
		waitForUser("after white");

		waitForUser("black");
		lvh.setBlack(HTSensors.getInstance().getCentralColor());
//		lvh.setBlack(getThreshold());
		waitForUser("after black");

	}

	private synchronized void waitForUser(String message)
			throws InterruptedException {
		if (message != null) {
			LCD.drawString(message, 0, 2, false);
		}
		Sound.twoBeeps();
		Button.ESCAPE.waitForPressAndRelease();
	}

	private int getThreshold() {
		int value = LFUtils.getAvgColorValue();
		LCD.drawInt(value, 4, 0, 3);
		return value;
	}

	public void initialize() {
		Thread cruiser = new Thread(new Cruiser());
		cruiser.start();
	}
}