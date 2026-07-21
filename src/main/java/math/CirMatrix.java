package math;

import allMains.CPBase;
import allMains.CirclePack;
import complex.Complex;
import exceptions.MiscException;
import geometry.CircleSimple;
import geometry.HyperbolicMath;
import geometry.SphericalMath;

/**
 * CirMatrix holds the representation of a circle as a 2x2
 * Hermitian complex matrix. This extends 'Mobius' because 
 * circles in this form can be manipulated via compositions 
 * with Mobius transformations. See, e.g., 'mobius_of_circle'.
 * 
 * Note: for purposes of drawing, all straight lines are 
 * stored in 'CircleSimple' objects as very large circles,
 * but with 'lineflag' true: the center is FAUX_RAD times 
 * the unit normal pointing to the interior, radius is 
 * adjusted to give proper distance from the origin. 
 * 
 * -------------------------------
 * Math of 'CirMatrix':
 * 
 * * Straight line a*x+b*y+c=0 can be rewritten 
 *   using B=(a-i*b)/2 as
 *     
 *         B*z+conj(B)*conj(z)+c=0
 *         
 *   We can normalize by replacing (a,b) by 
 *   (a,b)/sqrt(a^2+b^2) and c by c/sqrt(a^2+b^2), 
 *   so |B|=1/2. 
 * 
 * * Circle with center z0, radius r is (z-z0)*conj(z-z0)=r^2.
 *   This can be written using B=-conj(z0) and 
 *   g=z0*conj(z0)-r^2 = |z0|^2-r^2 as this expression:
 *   
 *         z*conj(z)+B*z+conj(B)*conj(z)+g=0.
 * 
 * * Both together: A*z*conj(z)+B*z+conj(B)*conj(z)+g=0 
 *   where A=+-1 for circle, a=0 for line.
 * 
 * So we represent all circles/lines by matrix M=[a,b;c,d], with
 * entry a is +-1 or 0.
 * 
 * When a=+-1:
 *  +1 ==> normal circle (interior inside circle)
 * 		entry b: -conj(z0)
 * 		entry c: -z0 = conj(b)  (hence hermitian)
 * 		entry d: always real = |z0|^2 - r^2 (real); 
 * 				 0 ==> circle goes through origin
 *	Conventions on inside/outside: 
 *      a=1,  inside of normal circle, note det = -r*r
 *	    a=-1, outside (normal circle but with entries 
 *    	      multiplied by -1)
 *    
 * When a=0:
 *   This means circle is a line through infinity.
 *   
 *   Normalization first: multiply all entries by 1/|b| so
 *      that cross diagonal entries have magnitude 1.
 *      Thus, get into form [0 B; conj(B) d] with |B|=1.
 *   
 *   Now:
 *   	n = conj(B) is a unit normal to the line.
 *   
 *   Conventions on inside/outside:
 *      n points towards "interior" of the circle
 *      -d*n/2 is a point on the line, thus 
 *           d=0: line goes through the origin
 *           d>0: origin is on the interior
 *           d<0: origin is exterior
 * (These are not automatic, code has to keep track 
 * case-by-case and adjust the matrix.)   
 * 
 * @author kens
 *
 * A circle or line can be written as
 *     A|z|² + Bz + B̄z̄ + g = 0,
 * where A = ±1 for a circle and A = 0 for a straight line.  This equation
 * is encoded as a 2 × 2 Hermitian matrix [A, B; B̄, g].  This class extends
 * {@link Mobius} because the image of a circle under a Möbius transformation
 * M can be computed via matrix conjugation: C' = Gᵀ C G̅, where G = M⁻¹·det(M).
 *
 * @par Circle case (A = ±1)
 * For a circle with center z₀ and radius r:
 *   - B = −z̄₀
 *   - g = |z₀|² − r²
 *   - A = +1 means the interior is the disc; A = −1 means the exterior.
 *
 * @par Line case (A = 0)
 * After normalizing so |B| = 1:
 *   - n = B̄ is the unit normal pointing toward the "interior."
 *   - −g·n/2 is a point on the line.
 *   - g > 0 means the origin is interior; g < 0 means exterior.
 *
 * @note For drawing purposes, straight lines are stored in
 *       {@link CircleSimple} objects as very large circles (radius
 *       near {@link allMains.CPBase#FAUX_RAD}) with {@code lineFlag = true}.
 *
 * @see Mobius#mobius_of_circle  Applies a Möbius to a circle via CirMatrix.
 * @author Ken Stephenson
 */
public class CirMatrix extends Mobius {
	
	public static final double CM_TOLER = .000000001;

	// Constructor(s)
	public CirMatrix() {
		super(); // start identity matrix
	}
	
	// for eucl circle (or straight line if 'lineflag' true)
	public CirMatrix(CircleSimple cs) { 
		super(); // a=1.0
		Complex cent=new Complex(cs.center);
		double rad=cs.rad;
		if (cs.lineFlag) {
			Complex unitnormal=cent.divide(CPBase.FAUX_RAD);
			// diff > 0 means origin is "interior"
			double diff=2.0*(rad-CPBase.FAUX_RAD);
			this.a=new Complex(0.0);
			this.b=unitnormal.conj();
			this.c=unitnormal;
			this.d=new Complex(-diff);
		}
		else {
			this.b=cent.conj().times(-1.0);
			this.c=cent.times(-1.0);
			this.d=new Complex((double)cent.absSq()-rad*rad);
		}
	}
	
	// clone
	public CirMatrix(Mobius mb) {
		super(); 
		a=mb.a;
		b=mb.b;
		c=mb.c;
		d=mb.d;
		oriented=mb.oriented;
	}

	/**
	 * Create 2x2 matrix representation of circle given 
	 * sph radius/center; normalize for inside/outside 
	 * conventions. Recall that centers are not preserved
	 * under stereographic projection.
	 * @param center Complex, (theta,phi) form
	 * @param rad double
	 * @param C CirMatrix, instantiated by calling routine
	 * @return int 1
	 */
	public static CirMatrix sph2CirMatrix(Complex center, double rad) {

		CirMatrix C=new CirMatrix();
		C.oriented =true;

		// goes through S pole, so projects to straight line
		if (Math.abs(center.y + rad - Math.PI) < CM_TOLER) {
			
			C.a=new Complex(0.0);
			// set b unit length so conj(b) is towards center
			double theta=center.x;
			C.b=new Complex(Math.cos(theta),-Math.sin(theta));
			C.c=C.b.conj();

			// contains N pole? line through origin
			if (Math.abs(rad - Math.PI / 2.0) < CM_TOLER) 
				return C;

			// straight line, but NOT through origin
			double R = Math.sin(center.y - rad) / (1 + Math.cos(center.y - rad));
			// R = signed eucl distance from origin to the line, 
			C.d=new Complex(R/(-2.0),0);
			// when C.d.x>0, origin is inside the halfplane, else outside
			return C;
		} 

		// project to circle
		CircleSimple sc = SphericalMath.s_to_e_data(center, rad);
		Complex ez = sc.center;
		double R = sc.rad;
		if (sc.flag==-1)
			R *=-1.0; // set negative
			
		// this represents the circle
		C.a=new Complex(1.0);
		C.b = new Complex(-ez.x, ez.y); // B= - conj(z)
		C.d = new Complex(ez.abs() * ez.abs() - R * R);
		C.c=C.b.conj();
			
		// if we want exterior, multiply through by -1
		if (R<0) {
			C.a=C.a.times(-1.0);
			C.b=C.b.times(-1.0);
			C.c=C.c.times(-1.0);
			C.d=C.d.times(-1.0);
		}
		return C;
	}
 
	/**
	 * @brief Determine if pt is inside, on, or outside of C.
	 * if |pt|>10^{8}, consider it to be infinity.
	 * @param C CirMatrix, in normalized form
	 * @param pt Complex 
	 * @return double: >0 for inside, 0 for on, <0 for outside
	 */
	public static int pt_inside(CirMatrix C,Complex pt) {
		
		if (Double.isNaN(pt.x) || Double.isNaN(pt.y))
			pt.x=pt.y=1000000000.0;

		if (C.a.abs()!=0 && Math.abs(C.a.abs()-1.0)>.000000000001) 
			throw new MiscException("'C' does not seem to be normalized.");

		int ans=1;
		
		// regular circle? C.a=+-1
		if (C.a.abs()>CM_TOLER) {
			double rad=Math.sqrt(C.a.x*(C.b.absSq()-C.d.x));
			if (pt.abs()>100000000.0)
				ans=-1;
			else {
				double dist=pt.add(C.c).abs(); // C.c= -center
				if (dist>(rad+CM_TOLER)) // outside
					ans=-1;
				else if (dist>(rad-CM_TOLER)) // on
					ans=0;
			}
			return (int)(C.a.x*ans); // swap if C bounds its outside
		}
		
		// line? C.c is unit normal into interior, so see if vector
		//    to point has dot product with it which is > +1
		if (pt.abs()>10000000.0)
			return 0; // infinity is on the line
		double dot=pt.x*C.c.x+pt.y*C.c.y;
		double transdist=-C.d.x/2.0;
		if (dot<transdist-CM_TOLER) // outside
			ans=-1;
		else if (dot<transdist+CM_TOLER) // on the line
			ans=0;
		return ans;
	}

	/**
	 * @brief Convert 'CirMatrix' to 'CircleSimple' in requested geometry.
	 * If a straight line in eucl case: set lineflag, multiply 
	 * conj(b) by FAUX_RAD for 'center', and set 'rad' to 
	 * FAUX_RAD+d/2. 
	 * @param CC CirMatrix, 2x2 representation of a circle
	 * @param hes int, geometry
	 * @return CircleSimple, null on error (e.g. improper hyp case)
	 */
	public static CircleSimple cirMatrix_to_geom(CirMatrix CC,int hes) {
		if (CC==null)
			return null;
		CircleSimple outCS=new CircleSimple();
		
		// typical data
		Complex ecent=CC.c.times(-1.0);  // c entry is -center
		double reald=CC.d.x; // throw out any extraneous imaginary part
		if (CC.a.x<-.5) { // a.x=-1 ==> all entries were multiplied by -1 
			ecent=ecent.times(-1.0);
			reald *=-1.0;
		} 
		double rsq=ecent.absSq()-reald; // rad^2
		if (CC.a.x!=0 && rsq<=0) {
			CirclePack.cpb.errMsg("error in a 'CirMatrix'");
			return null;
		}
		// positive eucl radius
		double erad=Math.sqrt(rsq); 

		// sph case, radius/center. See in/out conventions. 
		if (hes>0) {
			// circle is a straight line (goes through south pole)
			if (CC.a.abs() < CirMatrix.CM_TOLER) {
				// through origin? Hence a hemisphere
				if (CC.d.abs() < CirMatrix.CM_TOLER) {
					outCS.center.y = outCS.rad = Math.PI / 2.0;
					outCS.center.x=CC.b.conj().arg();
					return outCS;
				} 

				// straight line, but NOT through origin
				double R=CC.d.abs(); // distance to origin
				double theta=Math.atan2(-1.0*CC.b.y,CC.b.x);
				double atn=Math.atan(R);
				double rho=Math.PI/2.0-atn; 
				if (CC.d.x<0) // encloses origin (north pole)?
					rho+=2.0*atn;
				outCS.rad=rho;
				outCS.center=new Complex(theta,Math.PI-rho);

				return outCS;
			} // end of 'straight line' cases

			// else a circle
			CircleSimple sc=SphericalMath.e_to_s_data(ecent, erad);
			outCS.center=sc.center;
			outCS.rad=sc.rad;
			if (CC.a.x<-.5) { // want outside of euclidean circle
				outCS.center=SphericalMath.getAntipodal(sc.center);
				outCS.rad=Math.PI-sc.rad;
			}
			return outCS; // 
		} // done with sph
		
		// hyp case: return null if circle is not in unit disc
		if (hes<0) { 
			if (CC.a.x<=0) { // straight line or outside
				CirclePack.cpb.errMsg("Improper hyp conversion of 'CirMatrix'");
				return null;
			}
			if (ecent.abs()+erad>1.0) // not in disc
				return null;
			
			return HyperbolicMath.e_to_h_data(ecent, erad);
		}
		
		// else eucl; watch for line
		if (CC.a.x==0) { // yes, is a line
			outCS.lineFlag=true;
			Complex unitnormal=CC.c;
			outCS.center=unitnormal.times(CPBase.FAUX_RAD);  // unit normal toward interior
			
			outCS.rad=CPBase.FAUX_RAD+(CC.d.x/2.0); // FAUX_RAD - signed distance from origin
			return outCS;
		}
		outCS.center=ecent; 
		outCS.rad=erad*CC.a.x; // may be negative if a=-1
		return outCS;
	}
	
	/**
	 * @brief If a.x==0, this is a straight line and we use large
	 * circle using FAUX_RAD. Also, recall, a.x=-1 ==> all 
	 * entries were multiplied by -1.
	 * @return double
	 */
	public double getRadius() {
		Complex ecent=c.times(-1.0*a.x); // c entry is -center
		double reald=d.x*a.x; // throw out any extraneous imaginary part
		double radius=Math.sqrt(ecent.absSq()-reald); // rad^2
		if (a.x==0) {
			// FAUX_RAD - signed distance from origin
			radius=CPBase.FAUX_RAD+(d.x/2.0); 
		}
		return radius;
	}
	
	/**
	 * @brief If a.x==0, this is a straight line and we use a fake
	 * center FAUX_RAD distance out. Also, recall, a.x=-1 ==> all 
	 * entries were multiplied by -1.  
	 * @return
	 */
	public Complex getCenter() {
		Complex center=c.times(-1.0*a.x);
		if (a.x==0) 
			center=c.times(CPBase.FAUX_RAD);  // unit normal toward interior
		return center;
	}
	
}
