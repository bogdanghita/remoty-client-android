package com.example.transf;

import com.example.ac1.Point;

/**
 * Created by Bogdan on 7/30/2015.
 */
public class SteeringWheelTransf implements ITransfStrategy {

	@Override
	public void transform(Point p) {

		p.x = 0;
		p.y = p.y * 50;
		p.z = 0;
	}
}
