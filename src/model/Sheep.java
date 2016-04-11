package model;

import java.util.Random;

public class Sheep {
	private int xPosition = 0;
	private int yPosition = 0;
	
	public final static int NUM_ROWS = 100;
	public final static int NUM_COLS = 100;

	public final static int SIZE_CELL = 15;
	
	public Sheep() {
            Random randomizer = new Random();
            xPosition = randomizer.nextInt(NUM_COLS);
            yPosition = randomizer.nextInt(NUM_ROWS);
	}
	
	public Sheep(int x, int y) {
            xPosition = x;
            yPosition = y;
	}
	
	public int getxPosition() {
		return xPosition;
	}
	public void setxPosition(int xPosition) {
		this.xPosition = xPosition;
	}
	
	
	public void goUp(){
		if(yPosition > 0){
			yPosition--;
		}
	}
	
	public void goDown(){
		if(yPosition < NUM_ROWS){
			yPosition++;
		}
	}
	
	public void goLeft(){
		if(xPosition > 0){
			xPosition--;
		}
	}
	
	public void goRight(){
		if(xPosition < NUM_COLS){
			xPosition++;
		}
	}
	
	public int getyPosition() {
		return yPosition;
	}
	public void setyPosition(int yPosition) {
		this.yPosition = yPosition;
	}	
	public void setXYPosition(int x, int y) {
		this.xPosition = x;
		this.yPosition = y;
	}
}
