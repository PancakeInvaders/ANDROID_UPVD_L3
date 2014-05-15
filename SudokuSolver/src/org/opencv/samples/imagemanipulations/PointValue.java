package org.opencv.samples.imagemanipulations;

import org.opencv.core.Point;

public class PointValue {
	
	/**
	 * Attribut
	 */
	Point point;
	int value;

	/**
	 * Constructeur surchargé
	 * @param p
	 * @param v
	 */
	public PointValue(Point p, int v) {
		point = p;
		value = v;		
	}
	
	/**
	 * Accesseur GET Point
	 * @return point
	 */
	public Point getPoint() {
		return point;
	}
	
	/**
	 * Accesseur GET Value
	 * @return value
	 */
	public int getValue() {
		return value;
	}
	
	/**
	 * Accesseur SET Point
	 * @param point
	 */
	public void setPoint(Point point) {
		this.point = point;
	}
	
	/**
	 * Accesseur SET Value
	 * @param value
	 */
	public void setValue(int value) {
		this.value = value;
	}
}
