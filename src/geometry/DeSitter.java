package geometry;

import complex.Complex;
import math.Mobius;
import math.Point3D;

/**
 * Using de Sitter space is a potential alternative
 * to spherical representation of circles. This 
 * is inspired by the work of Phil Bowers and 
 * collaborators. The de Sitter unit sphere lies 
 * in Minkowski space R^{3,1}. 
 * 
 * The sphere centered at (theta,phi) with radius
 * rad is represented by the vector <x,y,z> through
 * (theta,phi) with length |1/cos(phi)| and the
 * time variable t satisfying t=sqrt(x^2+y^2+z^2-1),
 * which thus has de Sitter norm 1, i.e., is on the
 * de Sitter unit sphere.
 * 
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

	// constructors
	public DeSitter() {
		spacept=new Point3D(1.0,0.0,0.0);
	}
	
	public DeSitter(double a,double b,double c,double d) {
		spacept=new Point3D(a,b,c);
		t=d;
	}

	public DeSitter(CircleSimple cs,int hes) {
		CircleSimple ncs=cs;
		if (hes<0)
			ncs=HyperbolicMath.h_to_e_data(cs);
		if (hes==0)
			ncs=SphericalMath.e_to_s_data(cs);
		spacept=Point3D.sph_2_p3D(ncs.center);
		spacept=spacept.divide(Math.cos(ncs.rad)); // cone point
		double n=Math.sqrt(spacept.x*spacept.x+spacept.y*spacept.y+spacept.z*spacept.z-1.0);
		spacept=spacept.divide(n);
		t=1/n;
	}
	
	public double norm() {
		double nsq=this.spacept.norm();
		return Math.sqrt(Math.abs(nsq*nsq-this.t*this.t));
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
		double n=Math.sqrt(Math.abs(
				dS.spacept.x*dS.spacept.x+
				dS.spacept.y*dS.spacept.y+
				dS.spacept.z*dS.spacept.z-dS.t*dS.t));
		if (n<0)
			return null;
		DeSitter ans=new DeSitter(dS.spacept.x/n,dS.spacept.y/n,dS.spacept.z/n,dS.t/n);
		return ans;
	}
	
	/**
	 * Negative of Lorentz inner product of normalized 
	 * DsSitters gives the inversive distance between 
	 * the associated circles.
	 * @param dS1 DeSitter
	 * @param dS2 DeSitter
	 * @return double
	 */
	public static double invDistance(DeSitter dS1,DeSitter dS2) {
		DeSitter nS1=dS_normalize(dS1);// dS2.norm();
		DeSitter nS2=dS_normalize(dS2); // nS2.norm();
		return nS1.t*nS2.t-Point3D.DotProduct(nS1.spacept,nS2.spacept);
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
	 * Ortho circle for any geometry; Given a triple of 
	 * circles (typically forming a face), find the 
	 * common orthogonal circle. 
	 * @param c0 CircleSimple
	 * @param c1 CircleSimple
	 * @param c2 CircleSimple
	 * @return new CircleSimple, null on error
	 */
	public static CircleSimple orthoCircle(CircleSimple c0,CircleSimple c1,CircleSimple c2,int hes) {
		DeSitter[] dS= {new DeSitter(c0,hes),new DeSitter(c1,hes),new DeSitter(c2,hes)};
		Point3D[] pts= {dS[0].spacept,dS[1].spacept,dS[2].spacept};
		Point3D edge1=Point3D.displacement(pts[0],pts[1]);
		Point3D edge2=Point3D.displacement(pts[1],pts[2]);
		// unit normal and signed distance to origin
		Point3D n=Point3D.CrossProduct(edge1,edge2).normalize(); 
		double dist=Point3D.DotProduct(dS[0].spacept,n); 
		double phi=Math.acos(dist);
		if (java.lang.Double.isNaN(phi))
			return null;
		CircleSimple cs=new CircleSimple(SphericalMath.proj_vec_to_sph(n),phi);
		if (hes>0)
			return cs;
		
		// convert to eucliean
		cs=SphericalMath.s_to_e_data(cs);
		if (hes<0) // convert further to hyp
			return HyperbolicMath.e_to_h_data(cs);
		return cs;
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
