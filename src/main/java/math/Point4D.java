package math;

/**
 * @brief A point in 4-D Lorentz (Minkowski) space (t, x, y, z).
 *
 * In the Lorentz model of hyperbolic geometry, circles on the unit
 * sphere are represented by points satisfying the Lorentz condition
 * t² − x² − y² − z² = −1.  The corresponding spherical circle is
 * {(a,b,c) : a²+b²+c²=1, ax+by+cz = t}.
 *
 * The Lorentz inner product is ⟨u,v⟩ = u_t·v_t − u_x·v_x − u_y·v_y − u_z·v_z
 * (signature (1,3)), so the "norm" can be positive, negative, or zero.
 *
 * @see geometry.LorentzMath  Computational routines using the Lorentz model.
 * @author Ken Stephenson
 */
public class Point4D {
  public double t,x,y,z;
  
  // Constructors
  public Point4D(double t,double x, double y, double z) {
    this.t=t; this.x = x; this.y = y; this.z = z;
  }
  
  public Point4D(Point4D pt) {
	  this.t=pt.t;this.x=pt.x;this.y=pt.y;this.z=pt.z;
  }
  
  /**
   * @brief Lorentz product u.v
   * @param u Point4D
   * @param v Point4D
   * @return double
   */
  public static double LorentzProduct(Point4D u, Point4D v) {
    return u.t*v.t-u.x*v.x-u.y*v.y-u.z*v.z;
  }

  /**
   * @brief vector sum 
   * @param u Point4D
   * @param v Point4D
   * @return new Point4D
   */
  public static Point4D vectorSum(Point4D u, Point4D v) {
	  return new Point4D(u.t+v.t,u.x+v.x,u.y+v.y,u.z+v.z);
  }
  
  /**
   * @brief scalar multiple 
   * @param u Point4D
   * @param c Point4D
   * @return new Point4D
   */
  public static Point4D scalarMult(Point4D u,double c) {
	  return new Point4D(u.t*c,u.x*c,u.y*c,u.z*c);
  }
  
  /**
   * @brief scalar multiple by d
   * @param d double
   * @return new Point4D
   */
  public Point4D mult(double d) {
    return new Point4D(t*d,x*d,y*d,z*d);
  }

  /**
   * @brief scalar multiple by 1/d
   * @param d double
   * @return new Point3D
   */
  public Point4D divide(double d) {
    return new Point4D(t/d,x/d,y/d,z/d);
  }

  /**
   * @brief Lorentz form has signature (1,3), so is indefinite; norm
   * can be positive, negative, or zero
   * @return
   */
  public double norm() {
    return Math.sqrt(t*t-x*x-y*y-z*z);
  }
  
}
