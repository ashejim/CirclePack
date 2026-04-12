package complex;

import math.Matrix3D;
import math.Point3D;

/**
 * @brief A point on the Riemann sphere with both "real" and "apparent" coordinates.
 *
 * Extends {@link Complex} to represent a point on the unit sphere that
 * carries two coordinate systems simultaneously:
 *   - <b>Real</b> spherical coordinates (theta, phi) — the mathematical
 *     position on the sphere.
 *   - <b>Apparent</b> coordinates — the screen-projected position after
 *     applying a {@link Matrix3D} orthogonal transformation that encodes
 *     the current viewing orientation.
 *
 * The inherited (x, y) fields hold the <em>apparent</em> (theta, phi)
 * after the viewing transformation is applied in the constructor.
 *
 * @note The apparent-sphere coordinates are ephemeral; they must be
 *       recomputed whenever the viewing orientation changes.  The
 *       {@link #onFront} flag indicates whether the point is on the
 *       hemisphere facing the viewer.
 *
 * @author Ken Stephenson
 */
public class SphPoint extends Complex {

	/** True if this point is on the front (viewer-facing) hemisphere. */
	boolean onFront;

	// ---------------------------------------------------------------
	//  Constructors
	// ---------------------------------------------------------------

	/**
	 * @brief Construct from real spherical coordinates and a viewing transform.
	 *
	 * Applies the orthogonal transformation {@code trans} to convert the
	 * real (theta, phi) into apparent-sphere coordinates, which are stored
	 * in the inherited (x, y) fields.  Sets {@link #onFront} based on
	 * whether the apparent point faces the viewer (cos(theta_apparent) ≥ 0).
	 *
	 * @param tp     Real spherical coordinates as a Complex (theta, phi).
	 * @param trans  The 3 × 3 orthogonal matrix encoding the current
	 *               sphere orientation.
	 */
	public SphPoint(Complex tp,Matrix3D trans) {
		onFront=true;
		// convert to point on apparent sphere 
		Complex z=matrix3Dz(true,trans,tp);
		x=z.x;
		y=z.y;
		if (Math.cos(x)<0) onFront=false;
	}
	
	/**
	 * @brief Construct from raw (theta, phi) doubles and a viewing transform.
	 * @param th     Azimuthal angle theta.
	 * @param ph     Polar angle phi.
	 * @param trans  Viewing orientation matrix.
	 */
	public SphPoint(double th,double ph,Matrix3D trans) {
		this(new Complex(th,ph),trans);
	}
	
	/**
	 * @brief Default constructor: north pole with identity viewing transform.
	 */
	public SphPoint() {
		this(0.0,0.0,Matrix3D.Identity());
	}
	
	/**
	 * @brief Construct from a Complex (theta, phi) with identity viewing transform.
	 * @param z  Spherical coordinates (theta, phi).
	 */
	public SphPoint(Complex z) {
		this(z.x,z.y,Matrix3D.Identity());
	}
	
	// ---------------------------------------------------------------
	//  Projection to the visual plane
	// ---------------------------------------------------------------

	/**
	 * @brief Project this point to 2-D canvas coordinates.
	 *
	 * Assumes the inherited (x, y) fields are up-to-date apparent
	 * spherical coordinates.  Returns the (Y, Z) components of the
	 * 3-D point on the apparent unit sphere, which correspond to
	 * the horizontal and vertical screen axes.
	 *
	 * @return A new Complex (sin(phi)·sin(theta), cos(phi)).
	 */
	public Complex toCanvas() {
		return new Complex(Math.sin(y)*Math.sin(x),Math.cos(y));
	}
	
	/**
	 * @brief Project to the visual plane and update the {@link #onFront} flag.
	 *
	 * Same projection as {@link #toCanvas()}, but also rechecks
	 * whether the point faces the viewer.
	 *
	 * @return A new Complex representing the screen (Y, Z) position.
	 */
	public Complex toVisualPlane() {
		if (Math.cos(x)<0) onFront=false;
		return new Complex (Math.sin(y)*Math.sin(x),Math.cos(y)); 
	}
	
	// ---------------------------------------------------------------
	//  Coordinate transformation
	// ---------------------------------------------------------------

	/**
	 * @brief Apply an orthogonal transformation to convert between real and
	 *        apparent spherical coordinates.
	 *
	 * When {@code toA == true}, the transformation maps a real (theta, phi)
	 * point to its apparent-sphere (theta, phi) using the orthogonal matrix
	 * {@code trans}.  When {@code toA == false}, the inverse (transpose)
	 * is applied, mapping apparent → real coordinates.
	 *
	 * The algorithm proceeds in three steps:
	 *   1. Convert (theta, phi) → Cartesian (x, y, z) on the unit sphere.
	 *   2. Multiply by {@code trans} (or its transpose).
	 *   3. Convert the resulting (x, y, z) back to (theta, phi).
	 *
	 * @note This method may no longer be called directly; see
	 *       {@code toApparentSph} and {@code toRealSph} in SphView.java.
	 *
	 * @param toA     {@code true} to go <em>to</em> the apparent sphere;
	 *                {@code false} for the reverse.
	 * @param trans   The 3 × 3 orthogonal viewing-orientation matrix.
	 * @param sph_pt  Input spherical coordinates as a Complex (theta, phi).
	 * @return A new Complex holding the transformed (theta, phi).
	 */
	public static Complex matrix3Dz(boolean toA,Matrix3D trans,Complex sph_pt) {
		Point3D new_pt;
		// Step 1: spherical → Cartesian
		Point3D pt=new Point3D(Math.sin(sph_pt.y)*Math.cos(sph_pt.x),
						Math.sin(sph_pt.y)*Math.sin(sph_pt.x),
								Math.cos(sph_pt.y));
		// Step 2: apply the transformation (or its inverse/transpose)
		if (toA) 
			new_pt=Matrix3D.times(trans,pt);
		else
			new_pt=Matrix3D.times(trans.Transpose(),pt);
		// Step 3: Cartesian → spherical
		return new Complex(Math.atan2(new_pt.y,new_pt.x),Math.acos(new_pt.z)); 
	}
}
