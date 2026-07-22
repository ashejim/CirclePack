package ftnTheory;

import complex.Complex;
import geometry.SphericalMath;
import input.CommandStrParser;
import math.Point3D;
import packing.PackData;

/**
 * @brief A class for computing various energies of point distributions,
 *
 * A class for computing various energies of point distributions, 
 * a principal one being the 'coulomb' energy of points on the 
 * sphere. The class is created when a '?energy' of 'energy' 
 * call is made and vanishes after the computation.
 * @author kens
 */
public class PointEnergies {
	
	/**
	 * @brief Compute a pairwise point-distribution energy (Coulomb, L2, or log).
	 */
	public static double comp_energy(PackData packData,
			CommandStrParser.Energy eng) {
		double sum=0.0,pwr=0.0,d;
		Complex z;

		if (eng==CommandStrParser.Energy.COULOMB) pwr=-1;
		if (eng==CommandStrParser.Energy.L2) pwr=-2;
		if (packData.hes>0) { 
			for(int i=1;i<packData.nodeCount;i++) {
				Point3D pZ=SphericalMath.s_pt_to_p3D(packData.getCenter(i));
		  		for(int j=i+1;j<=packData.nodeCount;j++) {
					Point3D pW=SphericalMath.s_pt_to_p3D(packData.getCenter(j)); // load W
					Point3D pD=Point3D.displacement(pW,pZ);
					if (eng==CommandStrParser.Energy.LOG) 
						sum += Math.log(1/pD.norm());
					else sum += Math.pow(pD.norm(),pwr);
			    }
			}
			return sum;
		}
		
		// else eucl/hyperbolic
		for(int i=1;i<packData.nodeCount;i++) {
			z=packData.getCenter(i);
	  		for(int j=i+1;j<=packData.nodeCount;j++) {
	  			d=z.minus(packData.getCenter(j)).abs();
				if (eng==CommandStrParser.Energy.LOG) 
					sum += Math.log(1/d);
				else sum += Math.pow(d,pwr);
		    }
		}
		return sum;
		
/*	TODO: Is these energy worth computing? 
        // hyperbolic and 'green' function case

		if (packData.hes<0 && type==-1) {	
			Complex w,num,denom;
			for(int i=1;i<packData.nodeCount;i++) {
				z=packData.rData[i].center;
				if (z.abs()<=.999) { // non-horocycle ??
			  		for(int j=i+1;j<=packData.nodeCount;j++) {
			  			w=packData.rData[j].center;
		  				if (w.abs()<=.999) { // non-horocycle ??
		  					denom=z.minus(w);
		  					num=z.conj().times(w);
		  					num.x -= 1.0;
			  				sum +=Math.log(num.divide(denom).abs()); // log(abs((1-conj(z)w)/(z-w)))
			  			}
		  			}
				}
			}
			return sum;
		}
*/
	} 

	/**
	 * @brief Compute the minimum distance between centers of the packing.
	 * @param packData
	 * @return
	 */
	public static double comp_min_dist(PackData packData) {
		double d, min_s_dist = 5.0;

		if (packData.hes > 0) { // sphere
			for (int i = 1; i < packData.nodeCount; i++) {
				for (int j = i + 1; j <= packData.nodeCount; j++) {
					d = SphericalMath.s_dist(packData.getCenter(i),
							packData.getCenter(j));
					min_s_dist = (d < min_s_dist) ? d : min_s_dist;
				}
			}
			return min_s_dist;
		}
		for (int i = 1; i < packData.nodeCount; i++) {
			for (int j = i + 1; j <= packData.nodeCount; j++) {
				d = packData.getCenter(i).minus(packData.getCenter(j))
						.abs();
				min_s_dist = (d < min_s_dist) ? d : min_s_dist;
			}
		}
		return min_s_dist;
	} 

}