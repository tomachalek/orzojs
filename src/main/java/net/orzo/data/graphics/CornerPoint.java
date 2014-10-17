/*
 * Copyright (C) 2010 Tomas Machalek
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.orzo.data.graphics;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;

/**
 * Represents image corner point.
 * 
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 */
public class CornerPoint implements Comparable<CornerPoint> {
	/**
	 * the X coordinate of the point
	 */
	private final int x;

	/**
	 * the Y coordinate of the point
	 */
	private final int y;

	/**
	 * value of the corner response function
	 */
	private final float q;

	/**
	 * Constructs new corner point
	 * 
	 * @param x
	 *            the X coordinate
	 * @param y
	 *            the Y coordinate
	 * @param q
	 *            the corner response function value
	 */
	public CornerPoint(int x, int y, float q) {
		this.x = x;
		this.y = y;
		this.q = q;
	}

	/**
	 * String representation
	 * 
	 * @return description of the point
	 */
	@Override
	public String toString() {
		return String.format("CornerPoint[%d, %d], Q = %01.2f", this.x, this.y,
				this.q);
	}

	/**
	 * Compares value of this corner response function value with other point's
	 * one.
	 * 
	 * @param otherPoint
	 *            other compared point
	 * @return -1 if this 'q' is greater then the other, 1 if this 'q' is less
	 *         then the other and 0 otherwise
	 */
	@Override
	public int compareTo(CornerPoint otherPoint) {
		if (this.q > otherPoint.getQ()) {
			return -1;

		} else if (this.q < otherPoint.getQ()) {
			return 1;

		} else {
			return 0;
		}
	}

	/**
	 * 
	 */
	public int squareDistanceFrom(CornerPoint otherPoint) {
		return (this.x - otherPoint.getX()) * (this.x - otherPoint.getX())
				+ (this.y - otherPoint.getY()) * (this.y - otherPoint.getY());
	}

	/**
	 * X coordinate getter
	 * 
	 * @return value of the Y coordinate
	 */
	public int getX() {
		return this.x;
	}

	/**
	 * Y coordinate getter
	 * 
	 * @return value of the Y coordinate
	 */
	public int getY() {
		return this.y;
	}

	/**
	 * Returns coordinates as a Point
	 * 
	 * @return AWT point object
	 */
	public Point getPoint() {
		return new Point(this.x, this.y);
	}

	/**
	 * Corner response function value getter
	 * 
	 * @param value
	 *            of corner response function
	 */
	public float getQ() {
		return this.q;
	}

	/**
	 * Draws this point to a provided graphics object.
	 * 
	 * @param graphics
	 *            object to be drawed into
	 */
	public void draw(Graphics2D graphics) {
		graphics.setPaint(Color.red);
		graphics.setStroke(new BasicStroke(2));
		graphics.drawLine(this.x - 2, this.y, this.x + 2, this.y);
		graphics.drawLine(this.x, this.y - 2, this.x, this.y + 2);
	}

}
