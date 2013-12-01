package org.madn3s.sandbox;

import org.madn3s.robot.common.Utils;

import lejos.nxt.Motor;
import lejos.util.Delay;

public class Movement {
	
	public void test1(){
    	Motor.A.resetTachoCount();
    	Utils.printToScreen("LOOK AT ME! I'M MOVING!" , 0, 0);
        Motor.A.forward();
        Motor.B.forward();
        Utils.printToScreen("FORWARD" , 0, 0);
        Utils.printToScreen("FORWARD SPINS: "+Motor.A.getTachoCount(), 0, 0);
        Utils.printToScreen("BACKWARD" , 0, 0);
        //Motor.A.resetTachoCount();
        Motor.A.backward();
        Motor.B.backward();
        Utils.printToScreen("BACKWARD SPINS: "+Motor.A.getTachoCount() , 0, 0);
        Motor.A.stop();
        Motor.B.stop();
        Utils.printToScreen("STOPPING" , 0, 0);
        Utils.printToScreen("all done here");
    }
    
    public void test2(){
    	Utils.printToScreen("Let's Go Fasta!" , 0, 0);
    	Motor.A.setSpeed(720);
    	Motor.B.setSpeed(720);
    	Motor.A.forward();
    	Motor.B.forward();
    	Utils.printToScreen("ACCELERATION: "+Motor.A.getAcceleration() , 0, 0);
    	Delay.msDelay(2000);
    	Motor.A.stop();
    	Motor.B.stop();
    	Motor.A.backward();
        Motor.B.forward();
        while(Motor.A.getTachoCount() > 0){
        	Utils.printToScreen("Waiting" , 0, 0);
        }
        Motor.A.stop();
        Motor.B.stop();
        Utils.printToScreen("all done here");
    }
    
    public void test3(){
    	//Motor.A.resetTachoCount();
    	//Motor.B.resetTachoCount();
    	Utils.printToScreen("Test 3" , 0, 0);
    	Utils.printToScreen("Moving" , 0, 0);
    	Motor.A.forward();
    	Motor.B.forward();
    	while(Motor.A.getTachoCount() < 11){}
    	Motor.A.stop();
        Motor.B.stop();
    	Utils.printToScreen("Done" , 0, 0);
        Utils.printToScreen("A TACHO: "+Motor.A.getTachoCount(), 0, 1);
        Utils.printToScreen("B TACHO: "+Motor.B.getTachoCount(), 0, 2);
        Motor.A.rotateTo(90);
        Motor.A.resetTachoCount();
        Motor.B.resetTachoCount();
        Motor.A.forward();
    	Motor.B.forward();
    	Utils.printToScreen("Waiting" , 0, 0);
    	while(Motor.A.getTachoCount() <11){}
    	Utils.printToScreen("Done" , 0, 0);
        Motor.A.stop();
        Motor.B.stop();
        Utils.printToScreen("A TACHO: "+Motor.A.getTachoCount(), 0, 1);
        Utils.printToScreen("B TACHO: "+Motor.B.getTachoCount(), 0, 2);
        Utils.printToScreen("all done here");
    }
    
    public void test4(){
    	int angle = 810;
    	Utils.printToScreen("Test 4" , 0, 0);
    	Utils.printToScreen("Moving" , 0, 0);
    	long start = System.currentTimeMillis();
    	Motor.A.resetTachoCount();
        Motor.B.resetTachoCount();
        Motor.A.forward();
    	Motor.B.forward();
    	while((System.currentTimeMillis() - start) < 3000){}
    	Motor.A.stop();
        Motor.B.stop();
        Motor.A.rotate(angle);
    	start = System.currentTimeMillis();
    	Motor.A.forward();
    	Motor.B.forward();
    	while((System.currentTimeMillis() - start) < 3000){}
    	Motor.A.stop();
        Motor.B.stop();
        Motor.B.rotate(angle);
        start = System.currentTimeMillis();
        Motor.A.forward();
    	Motor.B.forward();
    	while((System.currentTimeMillis() - start) < 3000){}
    	Motor.A.stop();
        Motor.B.stop();
        Utils.printToScreen("all done here");
    }

}
