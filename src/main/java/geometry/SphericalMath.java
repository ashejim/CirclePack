package geometry;
import allMains.CPBase;
import baryStuff.BaryPoint;
import combinatorics.komplex.RedEdge;
import complex.Complex;
import dcel.PackDCEL;
import math.Mobius;
import math.Point3D;
import packing.PackData;
import util.RadIvdPacket;
import util.UtilPacket;

/** 
 * @brief Static methods for mathematical operations in spherical geometry.
 *
 * Provides the spherical-geometry implementations of circle-packing
 * computations on the unit Riemann sphere x² + y² + z² = 1.
 * Spherical points are stored as Complex numbers in (theta, phi) form,
 * where theta is the azimuthal angle from the positive x-axis and phi
 * is the polar angle down from the north pole.  Radii are spherical
 * (measured in radians, range [0, π]).
 *
 * @see HyperbolicMath  Hyperbolic (Poincaré disc) counterpart.
 * @see EuclMath        Euclidean counterpart.
 * @see CommonMath      Geometry-independent dispatch layer.
 *
 * Static routines for computations in spherical geometry
 * 
 * The unit (Riemann) sphere is x^2 + y^2 + z^2 = 1 in 3-space. 
 * Spherical points are stored as complex numbers in usual polar
 * form (theta,phi), so theta is the angle measured from the 
 * positive x-axis and phi is the angle down to the point from
 * the N pole. Radii are spherical, measured in radians. 
 * To get the correct orientation on the sphere when viewed from 
 * outside, we project from the SOUTH pole (unlike the typical 
 * complex analysis definition projecting from the NORTH pole). 
 * (So a + oriented triple in the plane is + oriented when 
 * looked at from OUTSIDE the sphere).
 * 
 * So for us, the origin is N and infinity is S, the SOUTH pole.
 * Under our stereographic projection, points (u,v) in the plane 
 * and (x,y,z) on the sphere are related by:
 *     u=x/(1+z); v=y/(1+z)
 *     z=(1-(u*u+v*v))/(1+(u*u+v*v)); x=u(1+z); y=v(1+z).
 *     
 * Inversive distance: if spherical circles c1 c2 are radius r1, r2, 
 * and their centers are spherical distance d apart, then 
 *    delta(c1,c2)=(cos(d)-cos(r1)*cos(r2))/(sin(r1)*sin(r2)) 
 * 
 * See also SphereLayout.java. 
 * TODO: in the long run, using hyperboloid model and Minkowski space
 * may have advantages. There the sphere is the boundary of H^3 and
 * we could use "geometric algebra" both for the hyperbolic plane and
 * the Riemann sphere. 
*/
public class SphericalMath{
  public static final double S_TOLER = .0000000000001;
  public static double OKERR=.000000001; 
  public static final int INITIAL_CAPACITY = 100;
  
  /**
   * Finding average of spherical (theta,phi) centers
   * can be tricky because of the 2pi periodicity of
   * theta: eg. (3.14,y) and (-3.14,y) are very close,
   * but the average is (0,y) is far away.
   * Convert to (x,y,z), average, and project. This
   * can also cause problems, but should work if
   * points are restricted, e.g., to a hemisphere.
   * 
   * @param pts Complex[]
   * @return Complex
   */
  public static Complex average_s_pts(Complex[] pts) {
	  int n=pts.length;
	  Point3D sum=new Point3D();
	  for (int j=0;j<n;j++) {
		  sum=sum.add(s_pt_to_p3D(pts[j]));
	  }
	  sum=sum.divide((double)n);
	  return proj_vec_to_sph(sum);
  }
  
  /**
   * Find inversive distance on sphere between circles.
   * @param z1 Complex, (theta,phi) center
   * @param z2 Complex, (theta,phi) center
   * @param r1 double, sph rad
   * @param r2 double, sph rad
   * @return double
   */
  public static double s_inv_dist(Complex z1,Complex z2,double r1,double r2) {
	  double d=s_dist(z1,z2);
	  return (Math.cos(d)-Math.cos(r1)*Math.cos(r2))/(Math.sin(r1)*Math.sin(r2));
  }
  
  /** 
	 * @brief Return sph length of edge with spherical radii r1, r2, 
	 * and inv dist 'ivd'. For two circles, 
	 * 'ivd'=(-cos(phi)+cos(r1)*cos(r2))/(sin(r1)*sin(r2)),
	 * where phi is the angle between the unit position vectors 
	 * p1, p2 of the centers of the two circles. That is, 
	 * cos(phi)=dot(p1,p2).
	 * @param r1 double 
	 * @param r2 double
	 * @param ivd double
	 * @return double  
	*/
  public static double s_ivd_length(double r1,double r2,double ivd) {
	  double cosphi=Math.cos(r1)*Math.cos(r2)-ivd*Math.sin(r1)*Math.sin(r2);
	  return Math.acos(cosphi);
  }
  
  /**
   * Specialized routine to find the bearing 
   * circle in a quad interstice; needed for 
   * "reflect" command.
   * This quad has reflective symmetry, so there
   * is a bearing circle. We compute its radius
   * with a typical secant method in sph geometry,
   * the compute center using average of the
   * placements using contiguous tangent pairs of 
   * given petal circles. 
   * Caution: this may go wrong. There may be two
   * solutions, one for each interstice. Choose
   * the smallest. Also, monotonicity may be lost
   * due to large initial radii.
   * @param cir_a CircleSimple
   * @param cir_b CircleSimple
   * @param cir_c CircleSimple
   * @param cir_d CircleSimple
   * @param uP UtilPacket, instantiated by calling routine
   * @return CircleSimple, null on error. uP.value contains
   *    2pi-anglesum.
   */
  public static CircleSimple quad_bearing(Complex[] centers,
		  double[] radii,UtilPacket uP) {
	  int N=10; // iterations allowed
	  boolean done=false;
	  
	  	  // expect result to be between 'lower' and 'upper'
	  double uprad=Math.PI;
	  double lowrad=0.0;
	  double upcurv=1.0;
	  double lowcurv=0.0;
	  double[] theta=new double[4];
	  double bestrad=(radii[0]+radii[1]+radii[2]+radii[3])/4;
	  
	  // find upcurv, lowcurv to start
	  for (int j=0;j<4;j++) 
		  theta[j]=Math.acos(s_comp_cos(bestrad,radii[j],radii[(j+1)%4]));
	  double bestcurv=CPBase.pi2-(theta[0]+theta[1]+theta[2]+theta[3]);
	  // if bestrad is too small
	  if (bestcurv<0.0) {
		  lowrad=bestrad;
		  lowcurv=bestcurv;
		  // keep making bestrad larger
		  while (bestcurv<0) {
			  bestrad=(bestrad+uprad)/2.0;
			  for (int j=0;j<4;j++) 
				  theta[j]=Math.acos(s_comp_cos(bestrad,radii[j],radii[(j+1)%4]));
			  bestcurv=CPBase.pi2-(theta[0]+theta[1]+theta[2]+theta[3]);
		  }
		  uprad=bestrad;
		  upcurv=bestcurv;
	  }
	  // else if bestrad is too large
	  else if (bestcurv>0.0) {
		  uprad=bestrad;
		  upcurv=bestcurv;
		  // keep making bestrad smaller
		  while (bestcurv>0.0) {
			  bestrad=(bestrad+lowrad)/2.0;
			  for (int j=0;j<4;j++) 
				  theta[j]=Math.acos(s_comp_cos(bestrad,radii[j],radii[(j+1)%4]));
			  bestcurv=CPBase.pi2-(theta[0]+theta[1]+theta[2]+theta[3]);
		  }
		  lowrad=bestrad;
		  lowcurv=bestcurv;
	  }
	  // unlikely, but might be done
	  else
		  done=true;
	  
	  // apply secant method
	  for (int n=1;(n<=N && !done);n++) {

		  // if bestcurv too large, increase bestrad
		  if (bestcurv<-OKERR) {
			  bestrad=lowrad+(uprad-lowrad)*(-lowcurv)/(upcurv-lowcurv);
			  for (int j=0;j<4;j++) 
				  theta[j]=Math.acos(s_comp_cos(bestrad,radii[j],radii[(j+1)%4]));
			  bestcurv=CPBase.pi2-(theta[0]+theta[1]+theta[2]+theta[3]);
			  // bestcurv still too large
			  if (bestcurv>0) {
				  uprad=bestrad;
				  upcurv=bestcurv;
			  }
			  else if (bestcurv>-OKERR) // done?
				  done=true;
			  else
				  lowrad=bestrad;
		  }
		  // else bestcurv too small, decrease bestrad
		  else if (bestcurv>OKERR) { 
			  bestrad=lowrad+(uprad-lowrad)*(-lowcurv)/(upcurv-lowcurv);			  uprad=bestrad;
			  for (int j=0;j<4;j++) 
				  theta[j]=Math.acos(s_comp_cos(bestrad,radii[j],radii[(j+1)%4]));
			  bestcurv=CPBase.pi2-(theta[0]+theta[1]+theta[2]+theta[3]);
			  // bestcurv still too small
			  if (bestcurv>0) {
				  uprad=bestrad;
				  upcurv=bestcurv;
			  }
			  else if (bestcurv>-OKERR) // done?
				  done=true;
			  else
				  uprad=bestrad;
		  }
		  else
			  done=true;
	  }
	  uP.value=bestcurv;

	  // If we failed, return null
	  if (!done && Math.abs(bestcurv)>0.001) {
		  uP.rtnFlag=-1;
		  return null;
	  }
	  
	  // we now have bestrad
	  CircleSimple cs=new CircleSimple();
	  cs.rad=bestrad;
	  
	  // we need the center; average the 
	  Complex[] z=new Complex[4];
	  for (int j=0;j<4;j++) {
		  z[j]=s_compcenter(centers[j],centers[(j+1)%4],
				  radii[j],radii[(j+1)%4],bestrad).center;
	  }
	  cs.center=average_s_pts(z);
	  return cs;
  }
  
  
  /** 
   * Compute cosine of angle at the first circle in spherical
   * triangle formed by triple of spherical radii. 
   * Increment flag on error: r1+r2+r3>M_PI or denom zero. 
   * TODO: Is this okay if r1+r2+r3 > M_PI? 
  */
  public static double s_comp_cos(double r1,double r2,double r3) {
	  double denom;
	  double sumr;

	  sumr=r1+r2+r3;
	  if (Math.abs(Math.PI-sumr)<S_TOLER) { // three centers lie on great circle
		  return -1.0;
	  }
	  if ((denom=Math.sin(r1+r2)*Math.sin(r1+r3))<S_TOLER) {
		  return denom;
	  }
	  return (Math.cos(r2+r3)-(Math.cos(r1+r2)*Math.cos(r1+r3)))/denom;
  } 
  
  /** 
   * @brief Return area (spherical excess) of Spherical triangle with given 
   * radii and inversive distances; from L'Huilier's Formula.
   * @param r0 double
   * @param r1 double
   * @param r2 double
   * @param ivd0 double
   * @param ivd1 double
   * @param ivd2 double
   * @return double
   */
  public static double s_area(double r0,double r1,double r2,
		  double ivd0,double ivd1,double ivd2) {
	  double l0=s_ivd_length(r0,r1,ivd0);
	  double l1=s_ivd_length(r1,r2,ivd1);
	  double l2=s_ivd_length(r2,r0,ivd2);
	  return s_face_area(l0,l1,l2);
  }
  
  /**
   * @brief Return area (spherical excess) of Spherical triangle with given
   * corners in (theta,phi) form; from L'Huilier's Formula.
   * @param p1 Complex
   * @param p2 Complex
   * @param p3 Complex
   * @return double
   */
  public static double s_tri_area(Complex p1,Complex p2,Complex p3) {
	  double l1=s_dist(p1,p2);
	  double l2=s_dist(p2,p3);
	  double l3=s_dist(p3,p1);
	  return s_face_area(l1,l2,l3);
  }
  
  /**
   * @brief Return area (spherical excess) of Spherical triangle with given
   * face 'RadIvdPacket' data.
   * @param rip RadIvdPacket
   * @return
   */
  public static double s_face_area(RadIvdPacket rip) {
	  return s_area(rip.rad[0],rip.rad[1],rip.rad[2],
			  rip.ivd[0],rip.ivd[1],rip.ivd[2]);
  }
  
  /**
   * @brief Usual normalization, 'a' at origin, 'b' on positive imaginary axis
   * @param pdcel PackDCEL
   * @param a complex, (theta,phi)
   * @param g complex, (theta,phi)
   * @return the Mobius applied
   */
  public static Mobius s_norm_pack(PackDCEL pdcel,Complex a,Complex g) {
	  Mobius mob=Mobius.mobNormSphere(a, g);
	  if (Mobius.frobeniusNorm(mob)>.0001) {
		  // directly adjust in 'Vertex'
		  for (int v=1;v<=pdcel.vertCount;v++) {
			  Complex z=pdcel.vertices[v].center;
			  pdcel.vertices[v].center=mob.apply_2_s_pt(z);
		  }
		  // directly adjust in red chain
		  if (pdcel.redChain!=null) {
			  RedEdge rtrace=pdcel.redChain;
			  do {
				  rtrace.setCenter(mob.apply_2_s_pt(rtrace.getCenter()));
				  rtrace=rtrace.nextRed;
			  } while(rtrace!=pdcel.redChain);
		  }
	  }
	  return mob;
  }
  
  /**
   * @brief Return area (spherical excess) of Spherical triangle with given
   * edge lengths. From L'Huilier's Formula.
   * @param l1 double
   * @param l2 double
   * @param l3 double
   * @return double
   */
  public static double s_face_area(double l1,double l2,double l3) {
	  double s=(l1+l2+l3)*0.5; // semi-perimeter
	  double t=Math.sqrt(Math.tan(s*0.5)*Math.tan(0.5*(s-l1))*Math.tan(0.5*(s-l2))*Math.tan(0.5*(s-l3)));
	  return 4.0*Math.atan(t);
  }
	
  /**
   * @brief Find the maximum value that the radius of vert can have 
   * based on the current radii of neighbors. (Because no triple
   * can have radii summing to more than PI.)
   * @param p PackData
   * @param v int
   * @return double
   */
  public static double sph_rad_max(PackData p,int v) {
		if (v<1 || v>p.nodeCount) 
			return 0.0; // error
		double mx=0.0;
		int num=p.countFaces(v);
		int[] flower=p.packDCEL.vertices[v].getFlower(true);
		for (int j=0;j<num;j++) {
			double sum =p.getRadius(flower[j])
				+p.getRadius(flower[j+1]);
			mx = (sum>mx) ? sum : mx;
		}
		return Math.PI-mx;
	}
	
  /**
   * @brief Find "incircle", sph center/radius of circle inscribed in 
   * triangular face with given cclw oriented corners. Build 
   * faux circles to find the 3D eucl circle through the points 
   * of tangency. 
   * @param z0 Complex
   * @param z1 Complex
   * @param z2, Complex, sph centers
   * @return CircleSimple
   */
	public static CircleSimple sph_tri_incircle(
			Complex z0,Complex z1,Complex z2) {
		
		// edge lengths
		double a=s_dist(z2,z1);
		double b=s_dist(z0,z2);
		double c=s_dist(z0,z1);
		
		// putative radii (based just on edge lengths)
		double r0=(b+c-a)/2.0;
		double r1=(a+c-b)/2.0;
		double r2=(a+b-c)/2.0;
		
		// pts of tangency 
		Complex t01=sph_tangency(z0,z1,r0,r1);
		Complex t12=sph_tangency(z1,z2,r1,r2);
		Complex t20=sph_tangency(z2,z0,r2,r0);

		CircleSimple cs=circle_3_sph(t01,t12,t20);
		return cs;
	}
	
	/** 
	 * @brief Given three points on the sph, find the sph center/rad
	 * for the circle containing them; choose center to get
	 * proper orientation {A, B, C}.
	 * @param A Complex; sph points (theta,phi)
	 * @param B Complex
	 * @param C Complex
	 * @return CircleSimple with spherical data
	 */
	public static CircleSimple circle_3_sph(Complex A,Complex B,Complex C) {
		// 3D points
		Point3D a=new Point3D(A);
		Point3D b=new Point3D(B);
		Point3D c=new Point3D(C);

		// cross product for normal to plane containing the points
		Point3D AB = new Point3D(b.x-a.x,b.y-a.y,b.z-a.z);
		Point3D AC = new Point3D(c.x-a.x,c.y-a.y,c.z-a.z);
		Point3D Z= Point3D.CrossProduct(AB,AC);
		Complex sz=proj_vec_to_sph(Z);
		double sr=s_dist(sz,A);
		
		return new CircleSimple(sz,sr,1);
	}
	
  /**
   * Spherical distance between two spherical 
   * (i.e., (theta,phi)) points
   * @param z Complex
   * @param w Complex
   * @return double
   */
  public static double s_dist(Complex z, Complex w){

    if((Math.abs(z.x-w.x) < S_TOLER) && (Math.abs(z.y-w.y) < S_TOLER))
      return (0.0);

    Point3D p1=s_pt_to_p3D(z);
    Point3D p2=s_pt_to_p3D(w);
    double dotprod=Point3D.DotProduct(p1, p2);
    if(Math.abs(dotprod) < S_TOLER)
      return Math.PI/2.0;

    return Math.acos(dotprod);
  }
  
  /** 
   * Stereographic projection of complex number to 
   * complex spherical point, form (theta,phi). 
   * IMPORTANT: note that we project so zero 
   * goes to North pole, infinity to South.
   * @param z Complex
   * @return new Complex, (theta,phi)
   */
  public static Complex proj_pt_to_sph(Complex z) {
	  double zs=z.absSq();
	  if (zs<.00000000001) 
		  return new Complex(0.0);
	  return new Complex(Math.atan2(z.y,z.x),Math.acos((1-zs)/(1+zs)));
  }

  /**
   * Find distance from spherical point to geodesic between two
   * spherical points.
   */
  public static double s_dist_pt_to_line(Complex z,
		  Complex end1,Complex end2) {
  	Point3D pA=s_pt_to_p3D(end1);
  	Point3D pB=s_pt_to_p3D(end2);
  	Point3D pC=s_pt_to_p3D(z);
  	Point3D pAxB=Point3D.CrossProduct(pA,pB); 
  	Point3D pCxAxB=Point3D.CrossProduct(pC,pAxB);
  	double pp=pAxB.norm();
  	double pd=pCxAxB.norm();
  	// d=p*Math.sin(theta)
  	return CPBase.piby2-Math.asin(Math.sqrt(pd/pp)); // distance is Pi/2-theta.
  }

  public static Point3D s_pt_to_p3D(Complex sph_z) {
	  double s=Math.sin(sph_z.y);
	  Point3D ans=new Point3D(
		s * Math.cos(sph_z.x),
	    s * Math.sin(sph_z.x),
	    Math.cos(sph_z.y));
	  return ans;
  }
    
  /**
   * @brief compute the eucl distance in 3D between to (theta,phi)
   * points on the sphere.
   * @param z Complex
   * @param w Complex
   * @return double
   */
  public static double eucl_dist3D(Complex z,Complex w) {
	  Point3D p0=SphericalMath.s_pt_to_p3D(z);
	  Point3D p1=SphericalMath.s_pt_to_p3D(w);
	  Point3D diff=Point3D.displacement(p0, p1);
	  return diff.norm();
  }
  
  /**
   * Return new Complex (theta,phi) representing projection of 
   * given 3D vector to the unit sphere; recall, origin 
   * goes to NORTH pole.
   * @param p3D Point3D
   * @return sph coords (theta,phi), default to N if vector norm is too small.
   */
  public static Complex proj_vec_to_sph(Point3D p3D) {
	  double dist;

	  // default for things near origin 
	  if ((dist=p3D.norm())< S_TOLER) {
		  return new Complex(0.0);
	  }
	  return new Complex(Math.atan2(p3D.y,p3D.x),Math.acos(p3D.z/dist));	
  } 
  
/**
 * @brief Given sph point z=(theta,phi)), return new Complex (y,z) on visual plane. 
 *  return null if on back. 
 * @param z=(theta,phi)
 * @return new Complex (y,z) on visual plane
 */
public static Complex sphToVisualPlane(Complex z) {
	return sphToVisualPlane(z.x,z.y);
}

/**
 * @brief  Given (theta,phi), return new Complex (y,z) on visual plane. 
 *  return null if on back.
 *  @param theta,phi spherical point
 *  @return new Complex (y,z) on visual plane 
 */
public static Complex sphToVisualPlane(double theta,double phi) {
	return new Complex(Math.sin(phi)*Math.sin(theta),Math.cos(phi)); 
}

  // (OBE) Note: simple computation for control point for
  // drawing arc of great circle between two points.
  // 
  // The quadratic curve is formed so the lines from 
  //   the control point to the two ends are tangent
  //   to the curve.
  //
  // Only difficulty is if points are antipodal: return
  // controlpoint as the origin; may eventually need a flag
  // to indicate this.
  
  public static Point3D computeControl(Point3D A,Point3D B) {
	  double AdotB=Point3D.DotProduct(A,B);
	  if (AdotB<-.9999999) { // antipodal
		  return new Point3D(0.0,0.0,0.0);
	  }
	  return Point3D.vectorSum(A,B).times(1/(1+AdotB));
  }
  
  /**
   * Given two sph circles which are (supposed to be) 
   * tangent, find the tangency point on the geodesic 
   * between them. Actually, returns pt with distances 
   * from z1, z2 having proportions r1, r2.
   * If z1 and z2 essentially antipodal, then result 
   * is numerically unstable. If r1+r2 > Pi, then 
   * have to distinguish which way to go from z1 
   * toward z2.
   * @param z1 Complex (theta,phi)
   * @param z2 Complex (theta,phi)
   * @param r1 double
   * @param r2 double
   * @return new Complex, (theta,phi), null on error
   */
  public static Complex sph_tangency(Complex z1,Complex z2,
		  double r1,double r2) {
	  double dratio=(s_dist(z1,z2))*r1/(r1+r2);
	  // try first direction
	  Point3D T3=sph_tang_p3D(z1,z2);
	  Complex tp1=sph_shoot(z1,T3,dratio);
	  double err1=Math.abs(s_dist(z1,tp1)-r1);
	  if (err1<S_TOLER) // looks good 
		  return tp1;
	  
	  // try opposite direction
	  T3=T3.times(-1.0); // flip tangent
	  Complex tp2=sph_shoot(z1,T3,dratio);
	  double err2=Math.abs(s_dist(z1,tp2)-r1);
	  if (err2<S_TOLER) // looks good 
		  return tp2;
	  
	  // Should be tp1
	  if (err1<=10000.0*err2)
		  return tp1;
	  return tp2;
  }

  /** 
   * @brief Find center of third circle in ordered triple. Note: 
   * orientation is counterclockwise looking at sphere from outside.
   * ivdj is inv distance for edge <j,j+1>. 
   * TODO: inv distances not yet used; there just to parallel other geoms.
   * @param z0 Complex, (theta, phi)
   * @param z1 Complex
   * @param r0 double
   * @param r1 double
   * @param r2 double
   * @param ivd0 double
   * @param ivd1 double
   * @param ivd2 double
   * @return CircleSimple
  */ 
  public static CircleSimple s_compcenter(Complex z0,Complex z1,
  		double r0,double r1,double r2,double ivd0,double ivd1,double ivd2) {
    Point3D v0=s_pt_to_p3D(z0);
    
    // side lengths
    double s0=s_ivd_length(r0,r1,ivd0);
    double s1=s_ivd_length(r1,r2,ivd1);
    double s2=s_ivd_length(r2,r0,ivd2);
    
    // pT is a tangent vector at z0
    Point3D pT=sph_tang_p3D(z0,z1);
    // angle is how far around from pT we will rotate 
    double angle=Math.acos(( Math.cos(s1)-Math.cos(s2)*Math.cos(s0) )/
  	     ( Math.sin(s2)*Math.sin(s0) ));

    // pN = v0 x pT
    Point3D pN=Point3D.CrossProduct(v0, pT);

    // pP will point toward the new center
    Point3D pP=pT.times(Math.cos(angle)).add(pN.times(Math.sin(angle)));
    
    Point3D pmt=v0.times(Math.cos(r0+r2)).add(pP.times(Math.sin(r0+r2)));
    Complex z=new Complex(0.0);
    if (pmt.z<=(1.0-S_TOLER)) {
    	z=new Complex(Math.atan2(pmt.y, pmt.x),Math.acos(pmt.z));
    }
    return new CircleSimple(z,r2,1);
  }
  
  /** 
   * Find center of third circle in ordered triple 
   * in tangency case. 
   * @param z0 Complex, (theta, phi)
   * @param z1 Complex
   * @param r0 double
   * @param r1 double
   * @param r2 double
   * @return CircleSimple
  */ 
  public static CircleSimple s_compcenter(Complex z0,Complex z1,
	  		double r0,double r1,double r2) {
	  	return s_compcenter(z0,z1,r0,r1,r2,1.0,1.0,1.0);
  }
  
  /**
   * @brief Given z, w on sphere, return the sph point 
   * which is distance 'dist' (in radians) from 
   * z in direction of w. 
   * @param ctr1 (theta,phi)
   * @param ctr2 (theta,phi)
   * @param dist double (radians)
   * @return new Complex
   */
  public static Complex s_shoot(Complex z,Complex w,double dist) {
	  // if very close, just return z
	  if (Math.abs(dist)<S_TOLER) 
		  return new Complex(z);
	  
	  // adjust mod 2*pi until dist lies in [0,2*pi]
	  while (dist<0) 
		  dist+=CPBase.pi2;
	  while (dist>CPBase.pi2) 
		  dist -=CPBase.pi2;
	  double cosd=Math.cos(dist);
	  double sind=Math.sin(dist);
	  
	  Point3D pT=sph_tang_p3D(z,w);
	  Point3D pV=s_pt_to_p3D(z);
	  pV=pV.times(cosd);
	  pT=pT.times(sind);
	  Point3D pA=pV.add(pT);
	  return proj_vec_to_sph(pA);
  }

  /**
   * @brief Given sph point z, and T, unit vector tangent at z, compute
   * the sph point which is distance 'dist' (in radians) from 
   * z in direction T. 
   * @param z Complex, (theta,phi)
   * @param T Point3D, unit tangent
   * @param dist double (radians)
   * @return new Complex (theta,phi)
   */
  public static Complex sph_shoot(Complex z,Point3D T,double dist) {
	  double cosd=Math.cos(dist);
	  double sind=Math.sin(dist);
	  Point3D pV=s_pt_to_p3D(z);
	  Point3D pA=pV.times(cosd).add(T.times(sind));
	  return proj_vec_to_sph(pA);
  }
  
  /** 
   * @brief Given 2 points on sphere, return unit length 3-vector in 
   * tangent space of first pt, pointing toward second. Result is
   * always a unit vector perp to the vector to the first point. 
   * Ambiguities: 
   *  + If pts are essentially equal or antipodal, result
   *    will be numerically unstable. 
   *  + In general, two directions point toward second point.
   *    Calling routine must judge, accepting the result here
   *    or using its negative.
   * @param ctr1 (theta,phi)
   * @param ctr2 (theta,phi)
   * @return unit Point3D
   */
  public static Point3D sph_tang_p3D(Complex ctr1,Complex ctr2) {
    Point3D pT=new Point3D();

    Point3D pA=s_pt_to_p3D(ctr1);
    Point3D pB=s_pt_to_p3D(ctr2);
    double pd=Point3D.DotProduct(pA,pB);
    Point3D pdA=pA.times(pd);
    Point3D pP=Point3D.displacement(pdA,pB);

    // pA and pB essentially parallel?
    double vn=pP.norm();
    if (vn<S_TOLER)
    {
    	// if pA is not N or S, point in horizontal direction
    	double pn=Math.sqrt(pA.x*pA.x+pA.y*pA.y);
    	if (pn>.0000001) {
    		// get orthogonal, X coord 0
    		pT.x=pA.x/pn;
    		pT.y=-pA.x/pn;
    		pT.z=0;
    	}
    	// otherwise, point toward (0,1,0)
    	else 
    		pT=new Point3D(0.0,1.0,0.0);
        return pT;
    }
    pT=pP.divide((double)vn);
    return pT;
  } 

  /**
   * Returns new Complex giving stereographic projection 
   * (recall, we project from the south pole) of spherical 
   * point z to complex point w in plane. Key is 
   * |w| = sin(phi)/(1+cos(phi)).
   * 
   * If z is essentially sorth pole, project to distance 
   * 10000 from origin.
   * @param z Complex (theta,phi)
   * @return new Complex
   */
  public static Complex s_pt_to_plane(Complex z) {
	  double cosphi=Math.cos(z.y);
	  
	  // at south pole?
	  if (cosphi<-.99999999) {
		  return new Complex(10000*Math.cos(z.x),10000*Math.sin(z.x));
	  }
	  double r=Math.sin(z.y)/(1.0+cosphi);
	  double x=r*Math.cos(z.x);
	  double y=r*Math.sin(z.x);
	  return new Complex(x,y);
  }

  /**
   * @param cS CircleSimple
   * @return CircleSimple
   */
  public static CircleSimple s_to_e_data(CircleSimple cs) {
	  return s_to_e_data(cs.center,cs.rad);
  }
  
  /** 
   * @brief Project circles from sph to plane. (Recall, our 
   * 'stereographic projection' is from south pole.)
   * Circles properly enclosing infinity (south pole) 
   * gets fake eucl data: start with antipodal point 
   * of z as fake sph center and (Math.PI-rad) as fake 
   * sph radius, then convert to eucl. 'flipflag==-1'
   * means the outside of resulting circle is actually 
   * the intended disc; the user must use the flipflag
   * info when available or make provisions to save it.
   * Also, a circle essentially passing through infinity will 
   * be given a small expansion to get fake eucl data.
   * (Often, calling routine expects negative radii,
   * but now the calling routine must change sign if
   * 'flipflag==-1'.)
   * @param z Complex, (theta,phi) center
   * @param r double, sph radius
   * @return CircleSimple, 'flag'='flipflag'
  */
  public static CircleSimple s_to_e_data(Complex z,double r) {
    int flipflag=1; // set to -1 if south pole enclosed
    Point3D pV=s_pt_to_p3D(z);

    // essentially passes through infinity? 
    if (Math.abs(z.y+r-Math.PI)<S_TOLER) // increment r slightly, proceed
    	r += 2.5*S_TOLER;
    
    // encloses infinity?
    if ((z.y+r)>=(Math.PI+S_TOLER)) {
        r=Math.PI-r; // fake radius
        pV=pV.times(-1.0);
        z=proj_vec_to_sph(pV); // fake center
        flipflag=-1;
    }
    
    // z-coords of bdry points below/above center
    double up=z.y+r; // below
    double down=z.y-r; // above
    
    // essentially centered at np 
    double er; // new radius
    Complex e; // new center
    if (Math.abs(z.y)<S_TOLER) {
        er=Math.sin(up)/(1.0+Math.cos(up));
        e=new Complex(0.0);
        return new CircleSimple(e,er,flipflag);
    }
    
    // essentially centered at sp, but not flipped.
    if (Math.abs(z.y-Math.PI)< S_TOLER && flipflag!=-1) {
    	// radius must be extremely small, treat as though
    	// it encloses sp, so fake center and huge fake radius.
        er=100000;
        e=new Complex(0.0);
        flipflag=-1;
        return new CircleSimple(e,er,flipflag);
    }
    
    // essentially passes through infinity, so decrease 'up'.
    if (Math.abs(up-Math.PI)< .00001)
        up -= .000015;
    
    // proceed
    double RR=Math.sin(up)/(1.0+Math.cos(up));
    double rr=Math.sin(down)/(1.0+Math.cos(down));
    er=Math.abs(RR-rr)/2.0;
    double m=(RR+rr)/2.0;
    double sny=Math.sin(z.y);
    e=new Complex(pV.x*m/sny,pV.y*m/sny);
    
    return new CircleSimple(e,er,flipflag);
  }
  
  public static CircleSimple e_to_s_data(CircleSimple cs) {
	  return e_to_s_data(cs.center,cs.rad);
  }
  		
  /** 
   * @brief Converts circle data to the sphere in a new 'CircleSimple'.
   * Caution: projection is NOT the standard "stereographic"; see
   * comments at the beginning of this file.
   * Note that r<0 means the circle bounds the outside disc, which 
   * affects the spherical center/radius.
   * @param ez Complex eucl center
   * @param er double eucl radius
   * @return CircleSimple
  */
  public static CircleSimple e_to_s_data(Complex ez,double er) {
	  
      Complex sz=new Complex(0.0); // will be sph center
      double rr=Math.abs(er); // note, r negative handled later
      
      // if er too small, project center, set sr=er unchanged.
      if (rr<S_TOLER) {
    	  double denom=ez.absSq()+1.0;
    	  Point3D p3D=new Point3D(2.0*ez.x,2.0*ez.y,(2.0-denom));
    	  p3D=p3D.divide(denom);
    	  if(p3D.z>(1.0-S_TOLER)) { // near N pole
    		  ez.x=ez.y=0.0;
    		  return new CircleSimple(sz,er,0);
    	  }
    	  if (p3D.z<(S_TOLER-1.0)) { // near S pole
    		  sz.x=0.0;
    		  sz.y=Math.PI;
    		  return new CircleSimple(sz,er,0);
    	  }
    	  sz.y=Math.acos(p3D.z);
    	  sz.x=Math.atan2(p3D.y,p3D.z);
    	  return new CircleSimple(sz,er,1); 
      }
      
      // General strategy: project 3 equally spaced points of the
      //   euclidean circle to 3 points on the sphere.
      
      Complex[] epts=new Complex[3];
      epts[0]=ez.plus(rr);
      epts[1]=ez.plus(CPBase.omega3[1].times(rr));
      epts[2]=ez.plus(CPBase.omega3[2].times(rr));
      
      Complex[] spts=new Complex[3];
      for (int j=0;j<3;j++) 
    	  spts[j]=proj_pt_to_sph(epts[j]);
      
      // for er<0, reverse orientation
      if (er<0) {  
    	  Complex h=spts[0];
    	  spts[0]=spts[2];
    	  spts[2]=h;
      }
      
      // find the circle through the 3 spherical points
      CircleSimple cS=circle_3_sph(spts[0],spts[1],spts[2]);
      return cS;
  }
  
  /** 
   * True if sph_pt (i.e., (theta,phi)) lies in 
   * triangle with given spherical points as 
   * corners for a CONVEX triangle. 
   * TODO: handle non-convex triangles
   * @param sph_pt Complex (theta,phi)
   * @param z1 Complex (theta,phi)
   * @param z2 Complex (theta,phi)
   * @param z3 Complex (theta,phi)
   * @return boolean
   */
  public static boolean pt_in_sph_tri(Complex sph_pt,
		  Complex z1,Complex z2,Complex z3) {
	    Point3D pP=s_pt_to_p3D(sph_pt);
	    Point3D pX=s_pt_to_p3D(z1);
	    Point3D pY=s_pt_to_p3D(z2);
	    Point3D pZ=s_pt_to_p3D(z3);
	    
	    // is it on wrong side of plane through X,Y,Z?
	    Point3D pXY=Point3D.displacement(pX,pY);
	    Point3D pXZ=Point3D.displacement(pX,pZ);
	    Point3D pXP=Point3D.displacement(pX,pP);
	    Point3D pC=Point3D.CrossProduct(pXY,pXZ);
	    // wrong direction? can't be in triangle
	    if (Point3D.DotProduct(pC,pXP)<0.0)
	    	return false;

	    // on wrong side of one of planes through origin?
	    pC=Point3D.CrossProduct(pY, pX);
	    if (Point3D.DotProduct(pP,pC)>0) 
	    	return false;
	    pC=Point3D.CrossProduct(pZ, pY);
	    if (Point3D.DotProduct(pP,pC)>0) 
	    	return false;
	    pC=Point3D.CrossProduct(pX, pZ);
	    if (Point3D.DotProduct(pP,pC)>0) 
	    	return false;
	    return true;
  }
      
  /** 
   * @brief Given a complex point (y,z) on the viewing screen, find the
   * corresponding spherical point (theta,phi) on the front. Return 
   * in UtilPacket. 'rtnFlag' is 0 on failure (visual point is not 
   * within unit disc). '(value,errval)' is sph point (re, im).
   * @param pt (y,z)
   * @return UtilPacket
   */
  public static UtilPacket screen_to_s_pt(Complex pt) {
    double xx=1.0 - pt.absSq();
    if (xx<0) {
        return new UtilPacket(0,0.0,0.0);
    }
    Point3D p3D=new Point3D(xx,pt.x,pt.y);
    Complex z=proj_vec_to_sph(p3D);
    return new UtilPacket(1,z.x,z.y);
  }
  
  /** 
   * Given points on sphere, return new Complex 
   * barycenter (theta,phi) of triangle they form; 
   * inside determined by orientation.
   * @param z1 (theta,phi)
   * @param z2 (theta,phi)
   * @param z3 (theta,phi)
   * @return new Complex barycenter (theta,phi)
  */
  public static Complex sph_tri_center(Complex z1,Complex z2,Complex z3) {
    Point3D pX=s_pt_to_p3D(z1);
    Point3D pY=s_pt_to_p3D(z2);
    Point3D pZ=s_pt_to_p3D(z3);
    
    // centroid
    Point3D pM=pX.add(pY).add(pZ).divide(3.0);
    
    // pC= pYxpX, pvn = |C| 
    Point3D pC=Point3D.CrossProduct(pY,pX);
    double pvn=pC.norm();
    if (pvn<S_TOLER) { // almost parallel
    	Point3D pD=Point3D.CrossProduct(pZ,pY);
        pvn=pD.norm();
        if (pvn<S_TOLER || Point3D.DotProduct(pD,pX)<0) // M should be good. 
        	return proj_vec_to_sph(pM);
        return proj_vec_to_sph(pM.times(-1.0));
    }
    if (pM.norm()<S_TOLER) // almost coplanar 
      return proj_vec_to_sph(pC.times(-1.0));
    if (Point3D.DotProduct(pC,pZ)<0) {
    	Complex ans=proj_vec_to_sph(pM);
    	return ans;
    }
    return proj_vec_to_sph(pM.times(-1.0));
  } 
  
  /**
	 * Given points z1,z2,z3 and z on the sphere (theta,phi), 
	 * find barycentric coords of z relative to spherical 
	 * triangle {z1,z2,z3}. This applies only to triangles
	 * of limited size due to the difficulty of defining 
	 * them in general and the difficulty in converting back
	 * in BaryPoint.bp2Complex. 
	 * 
	 * Stick to triangles lying in plane far enough from
	 * the origin. If triangle is too close to the origin
	 * (too "large") or p is outside the triangle, return 
	 * null. 
	 * 
	 * In planar oriented triangle <a,b,c> and point p,
	 * coords are area<b,c,p>/A, area<a,c,p>/A, and 
	 * area<a,b,p>/A, where A=area<a,b,c>. 
	 * 
	 * Note: I've tried the formula from "Spherical 
	 * Barycentric Coordinates", (2006) by Langer, 
	 * Belyaev, and Seidel, but is too hard to compute 
	 * and hard to reverse.
	 * 
	 * @param p Complex, (theta,phi)
	 * @param a (theta,phi)
	 * @param b (theta,phi)
	 * @param c (theta,phi)
	 * @return BaryPoint, null
	 */
	public static BaryPoint s_pt_to_bary(Complex p,
			Complex a, Complex b, Complex c) {
		
		Point3D pa=Point3D.sph_2_p3D(a);
		
		// three edges
		Point3D ab=Point3D.displacement(pa,
				Point3D.sph_2_p3D(b));
		Point3D bc=Point3D.displacement(Point3D.sph_2_p3D(b),
				Point3D.sph_2_p3D(c));
		Point3D ca=Point3D.displacement(
				Point3D.sph_2_p3D(c),pa);
		
		// intersection pip of line through p with plane
		//    defined by a, b, c
		Point3D pp = Point3D.sph_2_p3D(p); 
		Point3D n = Point3D.CrossProduct(ab,bc); // normal to plane
		// signed distance of origin to plane
		double t=Point3D.DotProduct(n,pa);
		if (t<.1) // plane too close to origin
			return null;
		
		t /= Point3D.DotProduct(n,pp);
		Point3D pip=pp.times(t); // point on plane
		
		// vectors to pip
		Point3D ap=Point3D.displacement(pa,pip);
		Point3D bp=Point3D.displacement(
				Point3D.sph_2_p3D(b),pip);
		Point3D cp=Point3D.displacement(
				Point3D.sph_2_p3D(c),pip);
		
		BaryPoint bpt = new BaryPoint();
		double A=n.norm();
		bpt.b0=Point3D.CrossProduct(bc,bp).norm()/A;
		bpt.b1=Point3D.CrossProduct(ca,cp).norm()/A;
		bpt.b2=Point3D.CrossProduct(ab,ap).norm()/A;
		
		// if pip is outside the triangle, it is outside
		//    the face, return null;
		if (bpt.b0<0 || bpt.b1<0 || bpt.b2<0)
			return null;
		return bpt;
	}
	
	/**
	 * @brief Given a spherical point (theta, phi), return a new Complex 
	 * antipodal point in (theta,phi) form
	 * @param s_pt (theta,phi)
	 * @return new Complex
	 */
	public static Complex getAntipodal(Complex s_pt) {
		
		// handle theta
		double x=s_pt.x-Math.PI;
		if (s_pt.x<0)
			x += 2.0*Math.PI;
		Complex pole=new Complex(x);
		double pmp=Math.PI-s_pt.y;
		if (Math.abs(pmp)<S_TOLER) // south pole?
			pole.y=Math.abs(pmp);
		else
			pole.y=pmp;
		return pole;
	}
	
	/**
	 * @brief Find centroid in 3-space of points in the plane 
	 * stereo projected to the sphere after application 
	 * of a transformation z --> a*z+b+c*i. If 'sPole' 
	 * is true, assume one more point located at infinity.
	 * @param P Complex[], points in the plane (indexed from 1)
	 * @param trans Point3D, {a,b,c} coeff for transfomation
	 * @param sPole boolean: include south pole (pt at infinity)?
	 * @return Point3D, centroid location
	 */
	public static Point3D transCentroid(Complex[] P,
			Point3D trans,boolean sPole) {
		int N=P.length-1;
		Point3D pt=new Point3D();
		if (sPole)
			pt.z=-1.0;
		
		for (int n=1;n<=N;n++) {
			double u=trans.x*P[n].x+trans.y;
			double v=trans.x*P[n].y+trans.z;
			double sq=u*u+v*v;
			double denom=1.0+sq;
			pt.x+=2.0*u/denom;
			pt.y+=2.0*v/denom;
			pt.z+=(1.0-sq)/denom;
		}

		double dn=(double)N;
		if (sPole)
			dn+=1.0;
		pt=pt.divide(dn);
		return pt;
	}
			
	/**
	 * Given a list of sph points, (theta,phi), find 
	 * their 3-space centroid
	 * @param pts Complex[] (theta,phi) form
	 * @return Point3D, null on error
	 */
	public static Point3D getCentroid(Complex[] pts) {
		
		// find the centroid
		int sz=pts.length-1;
		Point3D sum=new Point3D();
		for (int j=1;j<=sz;j++) {
			sum=sum.add(s_pt_to_p3D(pts[j]));
		}
		sum=sum.divide((double)sz);
		return sum;
	}
	
	/**
	 * Stereographic projection: (u,v) --> (x,y,z) 
	 * on unit sphere
	 *     z=(1-(u*u+v*v))/(1+(u*u+v*v));
	 *     x=u*(1+z);
	 *     y=v*(1+z).
	 * @param ez Complex
	 * @return Point3D
	 */
	public static Point3D e_to_sph_vec(Complex ez) {
		double z=ez.absSq();
		double x=ez.x*(1.0+z);
		double y=ez.y*(1.0+z);
		return new Point3D(x,y,z);
	}

}
