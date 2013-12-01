package org.madn3s.robot.common;

import lejos.nxt.Button;

import org.madn3s.sandbox.Movement;
import org.madn3s.sandbox.Sensors;




public class Main {
	public static void main(String[] args) {
		Utils.printToScreen("MADN3S V1");
		Movement m = new Movement();
		Sensors s = new Sensors();
		int button = Button.waitForAnyPress();
		while(!Utils.buttonIsEscape(button)){
			if(Utils.buttonIsRight(button)){
				s.test1();
			} else if(Utils.buttonIsLeft(button)){
				m.test4();
			} else if(Utils.buttonIsEnter(button)){
				m.test5();
			}
			button = Button.waitForAnyPress();
		}
	}
}
