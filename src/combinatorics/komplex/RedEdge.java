package combinatorics.komplex;

import complex.Complex;
import geometry.CircleSimple;

/**
 * @brief Red-chain edge: tracks boundary and side-pairing structure in the DCEL.
 *
 * After processing a dcel (see 'CombDCEL.redcookie'), 
 * there is (except in the case of a topological sphere) 
 * a 'redChain', a closed linked list of 'RedEdge's
 * surrounding a simply connected patch of interior faces. 
 * In non-simply connected cases, segments of red edges
 * may be identified. Outside the redChain are one or more 
 * ideal faces.
 * NOTE: the dcel structure still lies with the 'HalfEdge's 
 * via 'myEdge'. This is not persistent data; these edges 
 * change, e.g., when we change the DCEL. (?? 6/2025 ??)
 * TODO: do we need to save data when changing the DCEL?
 * @author kstephe2, 7/2020
 *
 */
public class RedEdge {
	/** The underlying DCEL half-edge this red edge is associated with. */
	public HalfEdge myEdge;
	/** Next red edge in the red chain (circular linked list). */
	public RedEdge nextRed;
	/** Previous red edge in the red chain. */
	public RedEdge prevRed;
	/** Twin red edge across a side-pairing (null if no pairing). */
	public RedEdge twinRed;
	/** Index into the array of side-pairing Möbius transformations. */
	public int mobIndx;
	
	/**
	 * Center for the origin vertex of this red edge.  A vertex may
	 * appear in multiple red edges with different center/radius values
	 * (e.g. across side-pairings in multiply-connected surfaces).
	 */
	Complex center;
	/** Radius for the origin vertex of this red edge. */
	double rad;
	
	/** Temporary utility integer for algorithms. */
	public int redutil;

	// Constructor: note, does not set 'he.myRedEdge', see
	//   'createRedEdge' below
	public RedEdge(HalfEdge he) {
		myEdge=he;
		prevRed=null;
		nextRed=null;
		mobIndx=0;
		if (he.origin!=null) {
			center=new Complex(he.origin.center);
			rad=he.origin.rad;
		}
		else {
			center=new Complex(0.0);
			rad=.04;
		}
		
		// set 'redutil' if ideal face is across myEdge;
		//    this is used in 'd_redChainBuilder'
		redutil=0;
		if (he.twin!=null && he.twin.face!=null && 
				he.twin.face.faceIndx<0)
			redutil=1;
	}
	
	/**
	 * @brief Create a new RedEdge with 'myEdge' so 'myEdge.myRedEdge'
	 * is set as well.
	 * @param edge HalfEdge
	 * @return RedEdge
	 */
	public static RedEdge createRedEdge(HalfEdge edge) {
		RedEdge rded=new RedEdge(edge);
		edge.myRedEdge=rded;
		return rded;
	}

	/**
	 * @brief Get center/radius. Center may be null.
	 * @return CircleSimple
	 */
	public CircleSimple getData() {
		return new CircleSimple(getCenter(),rad,1);
	}
	
	/**
	 * @brief Get center; return null if it's null
	 * @return new Complex
	 */
	public Complex getCenter() {
		if (center==null)
			return null;
		return new Complex(center);
	}
	
	/**
	 * @brief Doesn't set center in 'PackData' or in 'origin' 
	 * @param z Complex
	 */
	public void setCenter(Complex z) {
		center=new Complex(z);
	}
	
	public double getRadius() {
		return rad;
	}
	
	/**
	 * @brief This does not set rad or in 'origin'
	 * @param r double
	 */
	public void setRadius(double r) {
		rad=r;
	}

	/**
	 * @brief Return center/rad data specific to the red edge.
	 * Note: see 'PackData.getCircleSimple' for data from
	 * 'Vertex'. 
	 * @return CircleSimple
	 */
	public CircleSimple getCircleSimple() {
		return new CircleSimple(center,rad);
	}
	
	/**
	 * @brief Set center and radius
	 * @param cS CircleSimple
	 */
	public void setCircleSimple(CircleSimple cS) {
		center=cS.center;
		rad=cS.rad;
	}
	
	/**
	 * @brief clone: CAUTION: pointers may be in conflict or outdated.
	 * @return new RedEdge
	 */
	public RedEdge clone() {
		RedEdge rhe=new RedEdge(this.myEdge);
		rhe.nextRed=nextRed;
		rhe.prevRed=prevRed;
		rhe.twinRed=twinRed;
		rhe.mobIndx=mobIndx;
		rhe.center=new Complex(center);
		rhe.rad=rad;
		rhe.redutil=redutil;
		return rhe;
	}
	
	public String toString() {
		return myEdge.toString();
	}

	
}
