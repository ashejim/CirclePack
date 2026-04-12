package math;

import math.group.GroupElement;
import packing.PackData;
import exceptions.DataException;

/**
 * @brief Hamilton quaternion q = q₁ + q₂i + q₃j + q₄k, usable as a
 *        group element for representing rotations in 3-space.
 *
 * Quaternions form a non-commutative group under multiplication and can
 * also be added.  A unit quaternion q encodes a rotation in 3-space via
 * the map v ↦ q v q⁻¹ (where v is a "pure" quaternion with q₁ = 0).
 * A rotation by angle θ about a unit axis (a, b, c) corresponds to
 * q = (cos θ/2, a sin θ/2, b sin θ/2, c sin θ/2).
 *
 * @note Currently used sparingly in CirclePack; most rotations use
 *       {@link Mobius} or {@link Matrix3D} instead.
 *
 * @author Ken Stephenson
 */
public class Quaternion  implements GroupElement {
	
	double q1;
	double q2;
	double q3;
	double q4; // scalar term
	int level; // needed for GroupElements
	
    @SuppressWarnings("unused")
	private static Quaternion multIdentity = 
        	new Quaternion(1.0,0.0,0.0,0.0);

    @SuppressWarnings("unused")
	private static Quaternion addIdentity = 
        	new Quaternion(0.0,0.0,0.0,0.0);

	// Constructor(s)
	public Quaternion() {
		q1=1.0;
		q2=q3=q4=0.0;
		level=1;
	}
	
	public Quaternion(double k1,double k2,double k3, double k4) {
		q1=k1;
		q2=k2;
		q3=k3;
		q4=k4;
		level=1;
	}			
		
	public double norm() {
		return Math.sqrt(q1*q1+q2*q2+q3*q3+q4*q4);
	}

	public void normalize() {
		double len=norm();
		if (len<PackData.TOLER)
			return;
		q1 /= len;
		q2 /= len;
		q3 /= len;
		q4 /= len;
	}

	public double scalarPart() {
		return q1;
	}
	
	/**
	 * @brief The 'vector' part is also called the imaginary part,
	 * namely q2*i + q3*j + q4*k.
	 * @return new Point3D
	 */
	public Point3D vectorPart() {
		return new Point3D(q2,q3,q4);
	}
	
	/**
	 * @brief For "conjugate", change sign of vector part
	 * @return new Quaternion
	 */
	public Quaternion conj() {
		return new Quaternion(q1,-q2,-q3,-q4);
	}
	
	public Quaternion add(Quaternion qtn) {
		return new Quaternion(q1+qtn.q1,q2+qtn.q2,q3+qtn.q3,q4+qtn.q4);
	}

	/**
	 * @brief return 'this' minus qtn
	 * @param qtn Quaternion
	 * @return new Quaternion
	 */
	public Quaternion minus(Quaternion qtn) {
		return new Quaternion(q1-qtn.q1,q2-qtn.q2,q3-qtn.q3,q4-qtn.q4);
	}

	@Override
	/**
	 * multiply 'this' on the left by g
	 */
	public GroupElement lmultby(GroupElement g) {
		Quaternion G=(Quaternion)g;
		return new Quaternion(
				G.q1*q1-G.q2*q2-G.q3*q3-G.q4*q4,
				G.q1*q2+G.q2*q1+G.q3*q4-G.q4*q3,
				G.q1*q3-G.q2*q4+G.q3*q1+G.q4*q2,
				G.q1*q4+G.q2*q3-G.q3*q2+G.q4*q1);
	}

	@Override
	/**
	 * multiply 'this' on right by g
	 */
	public GroupElement rmultby(GroupElement g) {
		Quaternion G=(Quaternion)g;
		return new Quaternion(
				q1*G.q1-q2*G.q2-q3*G.q3-q4*G.q4,
				q1*G.q2+q2*G.q1+q3*G.q4-q4*G.q3,
				q1*G.q3-q2*G.q4+q3*G.q1+q4*G.q2,
				q1*G.q4+q2*G.q3-q3*G.q2+q4*G.q1);
	}

	@Override
	
	/**
	 * @brief return inverse
	 * @return new Quaternion
	 */
	public GroupElement inverse() {
		double ns=norm();
		ns=ns*ns;
		if (ns<PackData.TOLER)
			throw new DataException("quaternion norm is too small to invert");
		return new Quaternion(q1/ns,-q2/ns,-q3/ns,-q4/ns);
	}

	@Override
	/**
	 * Currently 'level' has no meaning for quaternions
	 */
	public void setLevel(int t) {
		// TODO Auto-generated method stub
		level=t;
	}

	@Override
	/**
	 * @brief Currently 'level' has no meaning for quaternions
	 * @return 1
	 */
	public int getLevel() {
		// TODO Auto-generated method stub
		return level;
	}

}
