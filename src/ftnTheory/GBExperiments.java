package ftnTheory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Vector;

import allMains.CirclePack;
import branching.ChapBrModPt;
import branching.GenBrModPt;
import branching.SingBrModPt;
import branching.TradBrModPt;
import complex.Complex;
import input.CommandStrParser;
import listManip.HalfLink;
import math.Mobius;
import packing.PackData;
import util.StringUtil;

/**
 * Experimental routines for the |GB| extension. Separated from
 * GenModBranching for clarity. All methods take a reference to
 * the owning GenModBranching instance so they can access
 * refPack, extenderPD, holoBorder, branchPts, revert(),
 * installBrPt(), getClickData(), and setLayoutOrder().
 * 
 * Current experiments:
 *   sweep — grid search over (x,y) positions for a second
 *           branch point, recording holonomy error at each.
 * 
 * @author Jim Ashe using Claude Opus 4.6 (experiments)
 */
public class GBExperiments {

	GenModBranching gmb;

	/**
	 * Constructor — holds reference to the |GB| extension.
	 * @param gmb GenModBranching instance
	 */
	public GBExperiments(GenModBranching gmb) {
		this.gmb = gmb;
	}

	/**
	 * Sweep a grid of (x,y) positions for a second branch point,
	 * measuring holonomy error at each. A fixed branch point
	 * (traditional, at a specified vertex) is placed first, then
	 * for each grid point the second branch point is placed via
	 * the click mechanism (auto-detecting type), the packing is
	 * repacked and laid out, and the holonomy is measured.
	 *
	 * Command syntax:
	 *   |GB| sweep -v {vertex} -a {aim1} -A {aim2} 
	 *              -b {xmin xmax ymin ymax} -n {gridsize}
	 *              -o {filename}
	 *
	 *   -v {vertex}   : vertex index for fixed traditional BP
	 *   -a {aim1}     : aim for fixed BP (multiple of pi, default 4.0)
	 *   -A {aim2}     : aim for sweep BP (multiple of pi, default 4.0)
	 *   -b {xmin xmax ymin ymax} : bounding box for sweep grid
	 *   -n {gridsize} : number of grid points per axis (default 20)
	 *   -o {filename} : output file path (default: sweep_output.csv)
	 *
	 * Output: CSV file with columns x, y, holonomy_error
	 *         Points where branching fails are skipped.
	 *
	 * @param flagSegs parsed flag segments from command
	 * @return count of successful grid points, 0 on error
	 */
	public int runSweep(Vector<Vector<String>> flagSegs) {

		// --- Parse arguments with defaults ---
		int fixedVertex = -1;
		double aim1 = 4.0;      // fixed BP aim (multiple of pi)
		double aim2 = 4.0;      // sweep BP aim (multiple of pi)
		double xmin = -1.0, xmax = 1.0, ymin = -1.0, ymax = 1.0;
		int gridSize = 20;
		String outputFile = "sweep_output.csv";

		try {
			for (Vector<String> items : flagSegs) {
				if (items.isEmpty())
					continue;
				String str = items.get(0);
				if (StringUtil.isFlag(str)) {
					char c = str.charAt(1);
					switch (c) {
					case 'v': // fixed vertex
						fixedVertex = Integer.parseInt(items.get(1));
						break;
					case 'a': // aim for fixed BP
						aim1 = Double.parseDouble(items.get(1));
						break;
					case 'A': // aim for sweep BP
						aim2 = Double.parseDouble(items.get(1));
						break;
					case 'b': // bounding box
						xmin = Double.parseDouble(items.get(1));
						xmax = Double.parseDouble(items.get(2));
						ymin = Double.parseDouble(items.get(3));
						ymax = Double.parseDouble(items.get(4));
						break;
					case 'n': // grid size
						gridSize = Integer.parseInt(items.get(1));
						break;
					case 'o': // output file
						outputFile = items.get(1);
						break;
					}
				}
			}
		} catch (Exception ex) {
			CirclePack.cpb.errMsg("sweep: error parsing arguments: "
					+ ex.getMessage());
			return 0;
		}

		// --- Validate ---
		if (fixedVertex <= 0) {
			CirclePack.cpb.errMsg(
					"sweep: must specify fixed vertex with -v {vertex}");
			return 0;
		}
		if (fixedVertex > gmb.refPack.nodeCount) {
			CirclePack.cpb.errMsg(
					"sweep: vertex " + fixedVertex + " out of range");
			return 0;
		}
		if (gmb.refPack.isBdry(fixedVertex)) {
			CirclePack.cpb.errMsg(
					"sweep: vertex " + fixedVertex + " is a boundary vertex");
			return 0;
		}

		gmb.msg("sweep: fixed vertex=" + fixedVertex
				+ " aim1=" + aim1 + "pi"
				+ " aim2=" + aim2 + "pi"
				+ " grid=" + gridSize + "x" + gridSize
				+ " bounds=[" + xmin + "," + xmax + "]x["
				+ ymin + "," + ymax + "]");

		// --- Run the sweep ---
		double dx = (gridSize > 1) ? (xmax - xmin) / (gridSize - 1) : 0;
		double dy = (gridSize > 1) ? (ymax - ymin) / (gridSize - 1) : 0;
		int successCount = 0;
		int failCount = 0;
		int totalPoints = gridSize * gridSize;

		StringBuilder csvData = new StringBuilder();
		csvData.append("x,y,holonomy_error\n");

		for (int i = 0; i < gridSize; i++) {
			double x = xmin + i * dx;
			for (int j = 0; j < gridSize; j++) {
				double y = ymin + j * dy;

				double hError = evaluatePoint(fixedVertex, aim1,
						aim2, x, y);

				if (hError >= 0) {
					csvData.append(String.format("%.8f,%.8f,%.8f%n",
							x, y, hError));
					successCount++;
				} else {
					failCount++;
				}
			}

			// Progress update every row
			if ((i + 1) % 5 == 0 || i == gridSize - 1) {
				gmb.msg("sweep progress: row " + (i + 1) + "/"
						+ gridSize + " (" + successCount
						+ " ok, " + failCount + " failed)");
			}
		}

		// --- Write output ---
		try {
			File outFile = new File(outputFile);
			BufferedWriter writer = new BufferedWriter(
					new FileWriter(outFile));
			writer.write(csvData.toString());
			writer.close();
			gmb.msg("sweep: wrote " + successCount
					+ " points to " + outFile.getAbsolutePath());
		} catch (Exception ex) {
			CirclePack.cpb.errMsg("sweep: failed to write output: "
					+ ex.getMessage());
			// Print to console as fallback
			System.out.println(csvData.toString());
		}

		// --- Revert to clean state ---
		gmb.revert();
		gmb.holoBorder = HalfLink.HoloHalfLink(
				gmb.extenderPD.packDCEL, -1);

		gmb.msg("sweep complete: " + successCount + "/"
				+ totalPoints + " points succeeded"
				+ " (" + failCount + " failed)");

		return successCount;
	}

	/**
	 * Evaluate holonomy error for a single (x,y) position
	 * of the second branch point.
	 *
	 * Flow:
	 *   1. Revert to clean reference packing
	 *   2. Place fixed traditional BP at specified vertex
	 *   3. Repack and layout
	 *   4. Place sweep BP at (x,y) via click mechanism
	 *   5. Repack and layout
	 *   6. Compute and return holonomy Frobenius norm
	 *
	 * @param fixedVertex vertex index for traditional BP
	 * @param aim1 aim for fixed BP (multiple of pi)
	 * @param aim2 aim for sweep BP (multiple of pi)
	 * @param x x-coordinate for sweep BP
	 * @param y y-coordinate for sweep BP
	 * @return holonomy Frobenius norm, or -1.0 on failure
	 */
	public double evaluatePoint(int fixedVertex, double aim1,
			double aim2, double x, double y) {
		try {
			// 1. Revert to clean packing
			gmb.revert();

			// Reinitialize holoBorder for the new DCEL
			// (revert swaps extenderPD, so old holoBorder
			//  references stale half-edges)
			gmb.holoBorder = HalfLink.HoloHalfLink(
					gmb.extenderPD.packDCEL, -1);

			// 2. Place fixed traditional branch point
			TradBrModPt tbp = new TradBrModPt(gmb,
					gmb.branchPts.size(),
					aim1 * Math.PI, fixedVertex);
			if (gmb.installBrPt(tbp) == 0)
				return -1.0;

			// 3. Repack and layout
			if (!repackAndLayout())
				return -1.0;

			// 4. Place sweep branch point at (x,y) via click
			Complex pt = new Complex(x, y);
			double[] data = gmb.getClickData(gmb.refPack, pt);
			if (data == null)
				return -1.0;

			int mode = (int) data[0];
			int indx = (int) data[1];

			if (mode < 0 || mode > 3 || indx <= 0
					|| indx > gmb.extenderPD.nodeCount)
				return -1.0;

			GenBrModPt bpt = null;
			if (mode == 2) { // chaperone (inside a circle)
				bpt = new ChapBrModPt(gmb,
						gmb.branchPts.size(),
						aim2 * Math.PI, indx,
						(int) data[2], (int) data[3],
						data[4], data[5]);
			} else if (mode == 3) { // traditional (near center)
				bpt = new TradBrModPt(gmb,
						gmb.branchPts.size(),
						aim2 * Math.PI, indx);
			} else if (mode == 1) { // singular (interstice)
				bpt = new SingBrModPt(gmb,
						gmb.branchPts.size(),
						aim2 * Math.PI, indx,
						data[2], data[3]);
			}

			if (bpt == null || !bpt.success)
				return -1.0;

			if (gmb.installBrPt(bpt) == 0)
				return -1.0;

			// 5. Repack and layout
			if (!repackAndLayout())
				return -1.0;

			// 6. Compute holonomy
			Mobius holomob = PackData.holonomyMobius(
					gmb.extenderPD, gmb.holoBorder);
			if (holomob == null)
				return -1.0;

			double frobNorm = Mobius.frobeniusNorm(holomob);
			return frobNorm;

		} catch (Exception ex) {
			// Silently skip points that cause exceptions
			return -1.0;
		}
	}

	/**
	 * Repack and layout the current packing.
	 * Uses the command parser for layout since
	 * that is how CirclePack handles it internally.
	 *
	 * @return true on success
	 */
	private boolean repackAndLayout() {
		try {
			int repackResult = gmb.extenderPD.repack_call(1000);
			if (repackResult < 0)
				return false;
			CommandStrParser.jexecute(gmb.extenderPD, "layout");
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

}
