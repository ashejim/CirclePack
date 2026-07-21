package util;

import complex.Complex;

/**
 * @brief Z Rhold.
 */
public class ZRhold {

	Complex center;
	Double radius;
		
	public ZRhold(Complex z,double r) {
		center = new Complex(z);
		radius=Double.valueOf(r);
	}
		
	public Double getRadius() {
		return radius;
	}
		
	public Complex getCenter() {
		return new Complex(center);
	}
}
