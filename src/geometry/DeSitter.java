package geometry;

import complex.Complex;
import math.Mobius;
import math.Point3D;

/**
 * Using de Sitter space is a potential alternative
 * to (theta,phi,rad) representation of spherical
 * circles. Investigating this is inspired by the
 * work of Phil Bowers and collaborators. The 
 * de Sitter unit sphere lies in Minkowski space
 * R^{3,1}. 
 * 
 * (NOTE: old code in 'SphereLayout' uses different
 * representation of points, R^[1,-3}.)
 * 
 * Points are 4-tuples {x,y,z,t} with Minkowski
 * inner product. Points on the unit sphere satisfy
 * x^2+y^2+z^2 - t^2 == 1. A 3D point outside the 
 * sphere has a right angled cone that is tangent
 * to the sphere in a circle. This point will be 
 * on a ray through a unique point of the de Sitter
 * unit sphere.
 * 
 */
public class DeSitter {
	public Point3D spacept; // space coords
	public double t; // time coord
	public boolean positive; // oriented

	// constructors
	public DeSitter() {
		spacept=new Point3D(1.0,0.0,0.0);
		positive=true;
	}
	
	public DeSitter(double a,double b,double c,double d) {
		spacept=new Point3D(a,b,c);
		t=d;
		positive=true;
	}

	public DeSitter(CircleSimple cs) {
		spacept=Point3D.sph_2_p3D(cs.center);
		t=Math.cos(cs.rad);
		positive=true;
	}

	/**
	 * Convert a point in de Sitter space to a
	 * spherical circle, center in (theta,phi) form.
	 * @param dS, DeSitter
	 * @return CircleSimple
	 */
	public static CircleSimple dSitter_2_cs(DeSitter dS) {
		double norm=dS.spacept.norm();
		return new CircleSimple(
			SphericalMath.proj_vec_to_sph(dS.spacept),Math.acos(dS.t/norm));
	}

	/**
	 * normalize so that x^2+y^2+z^2-t^2 = 1.
	 * @param dS DeSitter
	 * @return new DeSitter
	 */
	public static DeSitter dS_normalize(DeSitter dS) {
		double n=dS.spacept.norm()-dS.t*dS.t;
		if (n<0)
			return null;
		DeSitter ans=new DeSitter(dS.spacept.x/n,dS.spacept.y/n,dS.spacept.z/n,dS.t/n);
		return ans;
	}
	
	/**
	 * Lorentz inner product or normalized DsSitters
	 * gives the inversive distance between the associated
	 * circles.
	 * @param dS1 DeSitter
	 * @param dS2 DeSitter
	 * @return double
	 */
	public static double invDistance(DeSitter dS1,DeSitter dS2) {
		DeSitter nS1=dS_normalize(dS1);
		DeSitter nS2=dS_normalize(dS2);
		return Point3D.DotProduct(nS1.spacept,nS2.spacept)-nS1.t*nS2.t;
	}

	/**
	 * The matrix representation of point (x,y,z,t) in
	 * Lorentz space is 
	 *    [t+x , y+iz; y-iz , t-x1].
	 * Its determinant is the negative of Lorentz norm.
	 */ 
	public static Mobius matrixForm(DeSitter dS) {
		Mobius dS_mob=new Mobius(
				new Complex(dS.t+dS.spacept.x),
				new Complex(dS.spacept.y,dS.spacept.z),
				new Complex(dS.spacept.y,-dS.spacept.z),
				new Complex(dS.t-dS.spacept.x));
		return dS_mob;
	}
	
	/**
	 * We act on a point in de Sitter space with a Mobius
	 * transformation: m*dS*conj(m). 
	 * Since we normalize m to have det(m)=1, the determinant
	 * of the point is unchanged by the action; that is, 
	 * this action by m preserves the Lorentz inner product.     
	 * @param dS DeSitter
	 * @param m Mobius
	 * @return new DeSitter
	 */
	public static DeSitter applyMob(DeSitter dS,Mobius m) {
		Mobius mob=m.cloneMe();
		mob.normalize();
		Mobius mobconj=mob.cloneMe();
		mobconj.conj();
		Mobius dS_mob=matrixForm(dS);
		Mobius om=(Mobius)mob.rmultby(dS_mob).rmultby(mobconj);
		DeSitter ans=new DeSitter(
				(om.a.x-om.d.x)/2.0,
				(om.b.x+om.c.x)/2.0,
				(om.b.y-om.c.y)/2.0,
				(om.a.x+om.d.x)/2.0);
		return ans;
	}
	
}
