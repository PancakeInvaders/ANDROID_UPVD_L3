package org.opencv.samples.imagemanipulations;

import org.opencv.core.Point;

public class PointValue {
	
	Point point;
	
	int value;
	
	public PointValue(Point p, int v) {

		point = p;
		
		value = v;		
		
	}
	
	public Point getPoint() {
		return point;
	}
	
	public int getValue() {
		return value;
	}
	
	public void setPoint(Point point) {
		this.point = point;
	}
	
	public void setValue(int value) {
		this.value = value;
	}
	

}
