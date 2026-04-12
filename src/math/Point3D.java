package math;
import geometry.SphericalMath;
import packing.PackData;

import complex.Complex;

/**
 * @brief A mutable 3-D point / vector with conversions to spherical coordinates.
 *
 * Represents a point (x, y, z) in Euclidean 3-space.  In the context of
 * CirclePack this class serves a dual purpose:
 *   - As a plain 3-D vector with the usual linear-algebra operations
 *     (dot product, cross product, projection, normalization).
 *   - As a point on the Riemann sphere (unit sphere), with conversions
 *     between Cartesian (x, y, z) and spherical coordinates (theta, phi).
 *
 * @par Spherical-coordinate convention
 * Throughout CirclePack the spherical coordinates are:
 *   - <b>theta</b> — azimuthal angle in the xy-plane, measured from the
 *     positive x-axis (range (−π, π]).
 *   - <b>phi</b> — polar angle from the positive z-axis (north pole),
 *     so phi = 0 is the north pole and phi = π is the south pole.
 *
 * The conversion is
 *   x = sin(phi)·cos(theta),  y = sin(phi)·sin(theta),  z = cos(phi).
 *
 * @note The fields are public and mutable.
 *
 * @author Ken Stephenson
 */
public class Point3D {

  /** x-coordinate (or the first Cartesian component). */
  public double x;

  /** y-coordinate (or the second Cartesian component). */
  public double y;

  /** z-coordinate (or the third Cartesian component). */
  public double z;
  
  // ---------------------------------------------------------------
  //  Constructors
  // ---------------------------------------------------------------

  /**
   * @brief Construct from explicit Cartesian coordinates.
   * @param x  x-coordinate.
   * @param y  y-coordinate.
   * @param z  z-coordinate.
   */
  public Point3D(double x, double y, double z) {
    this.x = x; this.y = y; this.z = z;
  }
  
  /**
   * @brief Default constructor; initializes to the origin (0, 0, 0).
   */
  public Point3D() {
	  this.x=0.0;this.y=0.0;this.z=0.0;
  }
  
  /**
   * @brief Copy constructor.
   * @param pt  The Point3D to copy.
   */
  public Point3D(Point3D pt) {
	  this.x=pt.x;this.y=pt.y;this.z=pt.z;
  }
  
  /**
   * @brief Construct from spherical coordinates (theta, phi).
   *
   * Converts to Cartesian via
   *   x = sin(phi)·cos(theta),  y = sin(phi)·sin(theta),  z = cos(phi).
   *
   * @param theta  Azimuthal angle (radians).
   * @param phi    Polar angle from the north pole (radians).
   */
  public Point3D(double theta, double phi) {
    x = Math.sin(phi) * Math.cos(theta);
    y = Math.sin(phi) * Math.sin(theta);
    z = Math.cos(phi);
  }
  
  /**
   * @brief Construct from a Complex holding (theta, phi).
   *
   * Interprets z.x as theta and z.y as phi, then converts to Cartesian.
   *
   * @param z  Spherical coordinates packaged as a Complex.
   */
  public Point3D(Complex z) { // theta, phi packaged as complex
	  this(z.x,z.y);
  }
  
  /**
   * @brief Construct from a double array of length ≥ 3.
   * @param T  Array with T[0]=x, T[1]=y, T[2]=z.
   */
  public Point3D(double []T) { // given double[3] 
	  this(T[0],T[1],T[2]);
  }
  
  // ---------------------------------------------------------------
  //  Spherical coordinate accessors
  // ---------------------------------------------------------------

  /** 
   * @brief Return the azimuthal angle theta for the projection of this
   *        point onto the unit sphere.
   *
   * Uses atan2(y, x), so the result is in (−π, π].
   *
   * @return theta in radians.
   */
  public double getTheta() {
    return Math.atan2(y,x);
  }
  
  /** 
   * @brief Return the polar angle phi for the projection of this point
   *        onto the unit sphere.
   *
   * First normalizes to unit length, then returns acos(z/|p|).
   * The result is in [0, π].
   *
   * @return phi in radians.
   */
  public double getPhi() {
    double dist = Math.sqrt(x * x + y * y + z * z);
    return Math.acos(z/dist);
  }
  
  // ---------------------------------------------------------------
  //  Vector arithmetic
  // ---------------------------------------------------------------

  /**
   * @brief Return the displacement vector from p1 to p2: p2 − p1.
   * @param p1  Starting point.
   * @param p2  Ending point.
   * @return A new Point3D equal to p2 − p1.
   */
  public static Point3D displacement(Point3D p1,Point3D p2) {
	  return new Point3D(p2.x-p1.x,p2.y-p1.y,p2.z-p1.z);
  }
  
  /** 
   * @brief Subtract another point: return this − p2.
   * @param p2  The point to subtract.
   * @return A new Point3D equal to this − p2.
   */
  public Point3D sub(Point3D p2) {
	  return displacement(p2,this);
  }

  /**
   * @brief Compute the cross product u × v.
   *
   * The result is a vector perpendicular to both u and v, with
   * magnitude |u||v|sin(angle between them), following the
   * right-hand rule.
   *
   * @param u  First vector.
   * @param v  Second vector.
   * @return A new Point3D equal to u × v.
   */
  public static Point3D CrossProduct(Point3D u, Point3D v) {
    return new Point3D (u.y * v.z - u.z * v.y,
                        u.z * v.x - u.x * v.z,
                        u.x * v.y - u.y * v.x);
  }
  
  /**
   * @brief Cross product of two Complex numbers treated as 3-D vectors
   *        with z = 0.
   *
   * Embeds z = (x, y) and w = (u, v) into 3-space as (x, y, 0) and
   * (u, v, 0), then returns their cross product.  The result lies
   * along the z-axis: (0, 0, xv − yu).
   *
   * @param z  First 2-D vector as a Complex.
   * @param w  Second 2-D vector as a Complex.
   * @return A new Point3D equal to (z, 0) × (w, 0).
   */
  public static Point3D CrossProduct(Complex z,Complex w) {
	  Point3D u=new Point3D(z.x,z.y,0.0);
	  Point3D v=new Point3D(w.x,w.y,0.0);
	  return CrossProduct(u,v);
  }
  
  /**
   * @brief Compute the dot product u · v.
   * @param u  First vector.
   * @param v  Second vector.
   * @return u.x·v.x + u.y·v.y + u.z·v.z.
   */
  public static double DotProduct(Point3D u, Point3D v) {
    return u.x*v.x+u.y*v.y+u.z*v.z;
  }

  /**
   * @brief Return the vector sum u + v.
   * @param u  First vector.
   * @param v  Second vector.
   * @return A new Point3D equal to u + v.
   */
  public static Point3D vectorSum(Point3D u, Point3D v) {
	  return new Point3D(u.x+v.x,u.y+v.y,u.z+v.z);
  }
  
  /**
   * @brief Return the scalar multiple c · u.
   * @param u  A vector.
   * @param c  A scalar.
   * @return A new Point3D equal to c · u.
   */
  public static Point3D scalarMult(Point3D u,double c) {
	  return new Point3D(u.x*c,u.y*c,u.z*c);
  }
  
  /**
   * @brief Return the angle between two vectors (in radians).
   *
   * Computes acos(a·b / (|a|·|b|)).  If either vector has near-zero
   * length, returns 0.
   *
   * @param a  First vector.
   * @param b  Second vector.
   * @return The angle in [0, π], or 0 if a degenerate case is detected.
   */
  public static double intersectAng(Point3D a,Point3D b) {
	  double na=a.norm();
	  double nb=b.norm();
	  if (na<.000000000001 || nb<.000000000001)
		  return 0;
	  return Math.acos(DotProduct(a,b)/(na*nb));
  }
  
  // ---------------------------------------------------------------
  //  Norms
  // ---------------------------------------------------------------

  /**
   * @brief Return the squared Euclidean length |p|² = x² + y² + z².
   * @return |this|².
   */
  public double normSq() {
    return x*x+y*y+z*z;
  }
  
  /**
   * @brief Return the Euclidean length |p| = √(x² + y² + z²).
   * @return |this|.
   */
  public double norm() {
    return Math.sqrt(x*x+y*y+z*z);
  }
  
  /**
   * @brief Return the L1 (Manhattan) norm: |x| + |y| + |z|.
   * @return The L1 norm of this vector.
   */
  public double L1Norm() {
		return (Math.abs(x) + Math.abs(y) + Math.abs(z));
  }
  
  // ---------------------------------------------------------------
  //  Normalization and projection
  // ---------------------------------------------------------------

  /**
   * @brief Return a unit vector in the same direction as this vector.
   *
   * If the vector is shorter than {@link PackData#TOLER}, returns a
   * copy of {@code this} unmodified (to avoid division by near-zero).
   *
   * @return A new Point3D of unit length, or a copy if too short.
   */
  public Point3D normalize() {
	  double d=this.norm();
	  if (d<PackData.TOLER)
		  return new Point3D(this);
	  Point3D n3d=new Point3D(this);
	  return n3d.divide(d);
  }
  
  /**
   * @brief Divide each component by a scalar: return this / d.
   * @param d  The divisor.
   * @return A new Point3D equal to this / d.
   */
  public Point3D divide(double d) {
    return new Point3D(x/d,y/d,z/d);
  }
  
  /**
   * @brief Multiply each component by a scalar: return this · d.
   * @param d  The scalar.
   * @return A new Point3D equal to d · this.
   */
  public Point3D times(double d) {
    return new Point3D(x*d,y*d,z*d);
  }
  
  /**
   * @brief Return this vector with its component along v removed.
   *
   * Computes this − proj_v(this), i.e. the rejection of this vector
   * from the direction of v.
   *
   * @param v  The direction to project out.
   * @return A new Point3D perpendicular to v.
   */
  public Point3D mod(Point3D v) {
	  Point3D V=proj_vector(this,v);
	  return displacement(V,this);
  }

  /**
   * @brief Project this point to the horizon circle on the apparent sphere.
   *
   * Projects the (y, z) components onto the unit circle in the
   * yz-plane, then converts to spherical coordinates.  Points near
   * the yz-origin default to the north pole.
   *
   * @return A new Complex (theta, phi) on the horizon.
   */
  public Complex projToHorizon() {
	  double norm=Math.sqrt(y*y+z*z);
	  if (norm < .000000001) return new Complex(0,0);
	  double Y=y/norm;
	  double Z=z/norm;
	  if (Math.abs(Y)<.0000001) { // north or south pole
		  if (Z>0) return new Complex(0,0);
		  else return new Complex(0,Math.PI/2.0);
	  }
	  if (Y<0) return new Complex(-Math.PI/2,Math.acos(Z));
	  return new Complex(Math.PI/2,Math.acos(Z));
  }
  
  // ---------------------------------------------------------------
  //  Distance and area
  // ---------------------------------------------------------------

  /**
   * @brief Euclidean distance between two 3-D points.
   * @param A  First point.
   * @param B  Second point.
   * @return |A − B|.
   */
  public static double distance(Point3D A, Point3D B) {
    return Math.sqrt((A.x-B.x)*(A.x-B.x)+(A.y-B.y)*(A.y-B.y)+(A.z-B.z)*(A.z-B.z));
  }
  
  /**
   * @brief Convert this Cartesian point to spherical (theta, phi) directly.
   *
   * No normalization or manipulation is performed beyond the
   * atan2 / acos conversion.
   *
   * @return A new Complex (theta, phi).
   */
  public Complex getAsSphPoint() {
    return new Complex(this.getTheta(),this.getPhi());
  }
  
  /**
   * @brief Return a vector perpendicular to this one.
   *
   * Attempts to construct a perpendicular by shuffling coordinates,
   * projecting out the component along {@code this}, and normalizing.
   * If the result is degenerate (NaN), falls back to (1, 0, 0).
   *
   * @return A new unit-length Point3D perpendicular to this vector.
   */
  public Point3D perp() {
	Point3D perp = new Point3D(z, -x, y); 
	perp.mod(this);
	perp.normalize();
	if (Double.isNaN(perp.L1Norm()))
		return new Point3D(1.0, 0.0, 0.0);
	return perp;
  }

  /**
   * @brief Compute the Euclidean area of a triangle in 3-space via
   *        Heron's formula.
   *
   * Given three vertices, computes edge lengths a, b, c and returns
   *   A = √[s(s−a)(s−b)(s−c)] where s = (a+b+c)/2.
   *
   * Returns 0 if the result would be NaN (degenerate triangle).
   *
   * @param p0  First vertex.
   * @param p1  Second vertex.
   * @param p2  Third vertex.
   * @return The triangle area (≥ 0).
   */
  public static double triArea(Point3D p0,Point3D p1,Point3D p2) {
		double a=distance(p0,p1);
		double b=distance(p1,p2);
		double c=distance(p2,p0);
		// Heron's formula in the numerically friendlier factored form
		double ans=Math.sqrt((a+b+c)*(a+b-c)*(a+c-b)*(b+c-a))/4.0;
		if (Double.isNaN(ans))
			ans=0.0;
		return ans;
  }

  /**
   * @brief Return the vector projection of v onto the direction of w.
   *
   * Computes proj_w(v) = w · (v·w / |w|²).  If w is shorter than
   * {@link PackData#TOLER}, returns the zero vector.
   *
   * @param v  The vector to project.
   * @param w  The direction to project onto.
   * @return A new Point3D equal to proj_w(v).
   */
  public static Point3D proj_vector(Point3D v,Point3D w) {
	  if (w.norm()<PackData.TOLER)
		  return new Point3D(0.0,0.0,0.0);
	  return w.times(DotProduct(v,w)/w.norm());
  }
  
  // ---------------------------------------------------------------
  //  Static coordinate conversion utilities
  // ---------------------------------------------------------------

  /**
   * @brief Convert spherical coordinates (theta, phi) to a Cartesian Point3D.
   *
   * Uses the standard CirclePack convention:
   *   x = sin(phi)·cos(theta),  y = sin(phi)·sin(theta),  z = cos(phi).
   *
   * @param sph_z  A Complex with x = theta, y = phi.
   * @return A new Point3D on the unit sphere.
   */
  public static Point3D sph_2_p3D(Complex sph_z){
	  return new Point3D(Math.sin(sph_z.y) * Math.cos(sph_z.x),
			  Math.sin(sph_z.y) * Math.sin(sph_z.x),
			  Math.cos(sph_z.y));
  }

  /**
   * @brief Convert a Cartesian Point3D to spherical coordinates (theta, phi).
   *
   * Projects the point onto the unit sphere.  Points very close to the
   * origin (norm < {@link SphericalMath#S_TOLER}) default to the north
   * pole (0, 0).
   *
   * @param pt  A Point3D in Cartesian coordinates.
   * @return A new Complex (theta, phi).
   */
  public static Complex p3D_2_sph(Point3D pt) {
	  // default to north pole for things too near origin 
	  if (pt.norm()<SphericalMath.S_TOLER) {
		  return new Complex(0.0);
	  }
	  return new Complex(Math.atan2(pt.y,pt.x),Math.acos(pt.z/pt.norm()));	
  } 
  
  /**
   * @brief Return a string "[x, y, z]".
   * @return Bracket-delimited coordinate string.
   */
  public String toString() {
	  return new String("["+x+","+y+","+z+"]");
  }
  
}
