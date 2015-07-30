package com.example.transf;

import com.example.ac1.Point;

/**
 * Created by Bogdan on 7/30/2015.
 */
public class BasicPointTransf implements ITransfStrategy {

	@Override
	public void transform(Point p) {

		p.x = p.x * 25;
		p.y = p.y * 50;
		p.z = p.z * 10;
	}
}
