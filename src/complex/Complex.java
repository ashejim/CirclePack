package complex;

import exceptions.VarException;
import graphObjects.CPCircle;

import java.awt.geom.Point2D;

import math.Point3D;
import util.MathUtil;
import util.StringUtil;
import allMains.CPBase;

/**
 * @brief Mutable complex number with arithmetic operations and format conversion.
 *
 * This class represents an element of the complex field C, stored in
 * Cartesian form as a pair of doubles (x, y) corresponding to x + iy.
 * It provides the arithmetic expected of a complex-number type—addition,
 * multiplication, division, conjugation, square root, exponential, and
 * so on—together with conversions to and from other geometric
 * representations used in CirclePack (Point2D, Point3D, spherical
 * coordinates).
 *
 * @note Instances are <em>mutable</em>: the fields {@link #x} and {@link #y}
 *       are public and can be written directly.  Most arithmetic methods
 *       (plus, times, divide, …) return a <em>new</em> Complex and leave
 *       {@code this} unchanged, but callers should be aware that the
 *       underlying data can still be modified through the fields.
 *
 * @note Several methods have synonym pairs (e.g.&nbsp;{@link #plus} /
 *       {@link #add}, {@link #times} / {@link #mult}).  Both forms
 *       are equivalent; the duplication reflects the code's evolution
 *       from C-style naming to more Java-conventional naming.
 *
 * @see MathComplex  Static utility functions for complex arithmetic
 *                   (tolerance checks, constants, two-argument operations).
 * @see math.Point3D Conversion between (x, y, z) and (theta, phi) on the
 *                   Riemann sphere.
 *
 * @author Fedor Andreev (original implementation, 2003)
 * @author Ken Stephenson (additions and modifications for CirclePack)
 * @version 1.1
 */
public class Complex {

	/** Real part of the complex number (the "x" in x + iy). */
	public double x;

	/** Imaginary part of the complex number (the "y" in x + iy). */
	public double y;

	// ---------------------------------------------------------------
	//  Constructors
	// ---------------------------------------------------------------

	/**
	 * @brief Default constructor; initializes to the origin 0 + 0i.
	 */
	public Complex() {
		x = 0.;
		y = 0.;
	}

	/**
	 * @brief Construct a purely real complex number t + 0i.
	 * @param t  The real value.
	 */
	public Complex(double t) {
		x = t;
		y = 0.;
	}

	/**
	 * @brief Construct a purely real complex number from an integer.
	 * @param t  The integer value (cast to double).
	 */
	public Complex(int t) {
		x = (double) t;
		y = 0.;
	}

	/**
	 * @brief Construct a complex number from explicit real and imaginary parts.
	 *
	 * @warning Java integer division is truncating, so a call such as
	 *          {@code new Complex(1/2, 3.0)} silently rounds the first
	 *          argument to 0.0.  Write {@code new Complex(1.0/2, 3.0)}
	 *          or {@code new Complex(0.5, 3.0)} instead.
	 *
	 * @param t1  Real part.
	 * @param t2  Imaginary part.
	 */
	public Complex(double t1, double t2) {
		x = t1;
		y = t2;
	}

	/**
	 * @brief Construct from boxed Double objects; null values are treated as 0.
	 * @param a  Real part (may be null).
	 * @param b  Imaginary part (may be null).
	 */
	public Complex(Double a, Double b) {
		x = y = 0.0;
		if (a != null) x = a.doubleValue();
		if (b != null) y = b.doubleValue();
	}

	/**
	 * @brief Construct a complex number from two integers (both cast to double).
	 * @param t1  Real part.
	 * @param t2  Imaginary part.
	 */
	public Complex(int t1, int t2) {
		x = (double) t1;
		y = (double) t2;
	}

	/**
	 * @brief Construct a purely real complex number from a float.
	 * @param t  The float value (widened to double).
	 */
	public Complex(float t) {
		x = (double) t;
		y = 0.;
	}

	/**
	 * @brief Construct a purely real complex number from a long.
	 * @param t  The long value (widened to double).
	 */
	public Complex(long t) {
		x = (double) t;
		y = 0.;
	}

	/**
	 * @brief Construct a complex number from an AWT Point2D.
	 *
	 * The point's x-coordinate becomes the real part and its
	 * y-coordinate becomes the imaginary part.  A null argument
	 * yields 0 + 0i.
	 *
	 * @param pt  A 2-D point (may be null).
	 */
	public Complex(Point2D pt) {
		if (pt == null)
			x = y = 0.0;
		else {
			x = pt.getX();
			y = pt.getY();
		}
	}

	/**
	 * @brief Construct from a Point3D by projecting to spherical coordinates.
	 *
	 * The resulting Complex holds (theta, phi) where theta is the
	 * azimuthal angle and phi is the polar angle on the unit sphere.
	 * A null argument yields (0, 0).
	 *
	 * @note An earlier heuristic tried to detect whether the Point3D
	 *       was already in (theta, phi) form by checking whether z was
	 *       near zero.  That approach was unreliable and has been
	 *       removed; the projection is now always performed.
	 *
	 * @param pt3  A 3-D point (may be null).
	 */
	public Complex(Point3D pt3) {
		if (pt3 == null)
			x = y = 0.0;
		// this was cheap method to see if already in (theta, phi) form,
		//    and gave problems
//		else if (Math.abs(pt3.z)<.00000001) { // use x,y
//			x = pt3.x;
//			y = pt3.y;
//		}
		else { // project to unit sphere, return (theta,phi) form
			x = pt3.getTheta();
			y = pt3.getPhi();
		}
	}

	/**
	 * @brief Copy constructor; a null argument yields 0 + 0i.
	 * @param p  Complex number to copy (may be null).
	 */
	public Complex(Complex p) {
		if (p == null)
			x = y = 0.0;
		else {
			x = p.x;
			y = p.y;
		}
	}

	// ---------------------------------------------------------------
	//  Accessors
	// ---------------------------------------------------------------

	/**
	 * @brief Return the real part of this complex number.
	 * @return Re(z).
	 */
	public double real() {
		return x;
	}

	/**
	 * @brief Synonym for {@link #real()}.
	 * @return Re(z).
	 */
	public double getReal() {
		return x;
	}

	/**
	 * @brief Return the imaginary part of this complex number.
	 * @return Im(z).
	 */
	public double imag() {
		return y;
	}

	/**
	 * @brief Synonym for {@link #imag()}.
	 * @return Im(z).
	 */
	public double getImaginary() {
		return y;
	}

	/**
	 * @brief Set the real part (x-coordinate).
	 * @param x  New real part.
	 */
	public void setX(double x) {
		this.x = x;
	}

	/**
	 * @brief Synonym for {@link #setX(double)}.
	 * @param x  New real part.
	 */
	public void setReal(double x) {
		this.x = x;
	}

	/**
	 * @brief Set the imaginary part (y-coordinate).
	 * @param y  New imaginary part.
	 */
	public void setY(double y) {
		this.y = y;
	}

	/**
	 * @brief Synonym for {@link #setY(double)}.
	 * @param y  New imaginary part.
	 */
	public void setImaginary(double y) {
		this.y = y;
	}

	/**
	 * @brief Set this complex number's coordinates from a CPCircle's center.
	 * @param c  A CPCircle whose (x, y) center coordinates are copied.
	 */
	public void set(CPCircle c) {
		x = c.x;
		y = c.y;
	}

	// ---------------------------------------------------------------
	//  Predicates
	// ---------------------------------------------------------------

	/**
	 * @brief Test whether either component of a complex number is NaN.
	 * @param z  The complex number to test.
	 * @return {@code true} if Re(z) or Im(z) is NaN.
	 */
	public static boolean isNaN(Complex z) {
		if (java.lang.Double.isNaN(z.x) || java.lang.Double.isNaN(z.y))
			return true;
		return false;
	}

	// ---------------------------------------------------------------
	//  Modulus, argument, and norm
	// ---------------------------------------------------------------

	/**
	 * @brief Return the modulus (absolute value) |z| = sqrt(x² + y²).
	 * @return |z|.
	 */
	public double abs() {
		return Math.sqrt(x * x + y * y);
	}

	/**
	 * @brief Static convenience: compute sqrt(nx² + ny²) without creating
	 *        a Complex object.
	 * @param nx  Real component.
	 * @param ny  Imaginary component.
	 * @return The modulus.
	 */
	public static double abs(double nx, double ny) {
		return Math.sqrt(nx * nx + ny * ny);
	}

	/**
	 * @brief Return the squared modulus |z|² = x² + y².
	 *
	 * Useful when only relative magnitude matters, avoiding the cost
	 * of a square root.
	 *
	 * @return |z|².
	 */
	public double absSq() {
		return x * x + y * y;
	}

	/**
	 * @brief Return the principal argument arg(z) in the range (−π, π].
	 *
	 * Uses {@link Math#atan2(double, double)}, which handles the
	 * quadrant logic and returns 0 for the origin.
	 *
	 * @return arg(z) in radians.
	 */
	public double arg() {
		return Math.atan2(y, x);
	}

	// ---------------------------------------------------------------
	//  Arithmetic — addition
	// ---------------------------------------------------------------

	/**
	 * @brief Add a complex number to this one: z + t.
	 * @param t  The addend.
	 * @return A new Complex equal to (this + t).
	 */
	public Complex plus(Complex t) {
		return new Complex(x + t.x, y + t.y);
	}

	/**
	 * @brief Synonym for {@link #plus(Complex)}.
	 * @param t  The addend.
	 * @return A new Complex equal to (this + t).
	 */
	public Complex add(Complex t) {
		return this.plus(t);
	}

	/**
	 * @brief Add a real number to this complex number: z + t.
	 * @param t  A real addend.
	 * @return A new Complex equal to (this + t).
	 */
	public Complex plus(double t) {
		return new Complex(x + t, y);
	}

	/**
	 * @brief Synonym for {@link #plus(double)}.
	 * @param t  A real addend.
	 * @return A new Complex equal to (this + t).
	 */
	public Complex add(double t) {
		return this.plus(t);
	}

	// ---------------------------------------------------------------
	//  Arithmetic — subtraction
	// ---------------------------------------------------------------

	/**
	 * @brief Subtract a complex number from this one: z − t.
	 * @param t  The subtrahend.
	 * @return A new Complex equal to (this − t).
	 */
	public Complex minus(Complex t) {
		return new Complex(x - t.x, y - t.y);
	}

	/**
	 * @brief Subtract a real number from this complex number: z − d.
	 * @param d  A real subtrahend.
	 * @return A new Complex equal to (this − d).
	 */
	public Complex minus(double d) {
		return new Complex(x - d, y);
	}

	/**
	 * @brief Synonym for {@link #minus(Complex)}.
	 * @param t  The subtrahend.
	 * @return A new Complex equal to (this − t).
	 */
	public Complex sub(Complex t) {
		return this.minus(t);
	}

	/**
	 * @brief Synonym for {@link #minus(double)}.
	 * @param d  A real subtrahend.
	 * @return A new Complex equal to (this − d).
	 */
	public Complex sub(double d) {
		return this.minus(d);
	}

	// ---------------------------------------------------------------
	//  Arithmetic — multiplication
	// ---------------------------------------------------------------

	/**
	 * @brief Multiply this complex number by another: z · t.
	 *
	 * Uses the standard formula
	 *   (a + bi)(c + di) = (ac − bd) + (ad + bc)i.
	 *
	 * @param t  The multiplier.
	 * @return A new Complex equal to (this · t).
	 */
	public Complex times(Complex t) {
		return new Complex(x * t.x - y * t.y, x * t.y + y * t.x);
	}

	/**
	 * @brief Synonym for {@link #times(Complex)}.
	 * @param t  The multiplier.
	 * @return A new Complex equal to (this · t).
	 */
	public Complex mult(Complex t) {
		return this.times(t);
	}

	/**
	 * @brief Multiply this complex number by a real scalar: z · t.
	 * @param t  A real scalar.
	 * @return A new Complex equal to (this · t).
	 */
	public Complex times(double t) {
		Complex t1 = new Complex(x * t, y * t);
		return t1;
	}

	/**
	 * @brief Synonym for {@link #times(double)}.
	 * @param t  A real scalar.
	 * @return A new Complex equal to (this · t).
	 */
	public Complex mult(double t) {
		return this.times(t);
	}

	// ---------------------------------------------------------------
	//  Arithmetic — division
	// ---------------------------------------------------------------

	/**
	 * @brief Divide this complex number by a real scalar: z / t.
	 *
	 * @note No zero-check is performed; the caller is responsible for
	 *       ensuring t ≠ 0.
	 *
	 * @param t  A real divisor.
	 * @return A new Complex equal to (this / t).
	 */
	public Complex divide(double t) {
		Complex t1 = new Complex(x / t, y / t);
		return t1;
	}

	/**
	 * @brief Divide this complex number by another: z / t.
	 *
	 * Computes (a + bi) / (c + di) via the identity
	 *   (a + bi) / (c + di) = [(ac + bd) + (bc − ad)i] / (c² + d²).
	 *
	 * If the divisor's modulus is below the tolerance defined by
	 * {@link MathComplex#isSmall(double)}, an error is recorded via
	 * {@link MathComplex#setError(String)} but execution continues
	 * (the result will contain infinities or NaN).
	 *
	 * @param t  The complex divisor.
	 * @return A new Complex equal to (this / t).
	 */
	public Complex divide(Complex t) {
		if (MathComplex.isSmall(t.x) && MathComplex.isSmall(t.y))
			MathComplex.setError("The denominator is too small!");
		double f = 1 / (t.x * t.x + t.y * t.y);
		return new Complex((x * t.x + y * t.y) * f, (y * t.x - x * t.y) * f);
	}

	// ---------------------------------------------------------------
	//  Unary operations
	// ---------------------------------------------------------------

	/**
	 * @brief Return the complex conjugate z̄ = x − iy.
	 * @return A new Complex equal to conj(this).
	 */
	public Complex conj() {
		return new Complex(x, -y);
	}

	/**
	 * @brief Return the complex exponential e^z.
	 *
	 * Uses Euler's formula: e^(x+iy) = e^x (cos y + i sin y).
	 *
	 * @return A new Complex equal to e^(this).
	 */
	public Complex exp() {
		Complex tmp = new Complex(Math.cos(y), Math.sin(y));
		return tmp.times(Math.exp(x));
	}

	/**
	 * @brief Return the multiplicative inverse 1/z.
	 *
	 * Computed as z̄ / |z|²  =  (x − iy) / (x² + y²).
	 *
	 * If the modulus is below the tolerance defined by
	 * {@link MathComplex#isSmall(double)}, an error is recorded but
	 * computation proceeds (yielding infinities or NaN).
	 *
	 * @return A new Complex equal to 1/(this).
	 */
	public Complex reciprocal() {
		if (MathComplex.isSmall(x) && MathComplex.isSmall(y))
			MathComplex.setError("The denominator is too small!");
		double f = 1 / (x * x + y * y);
		return new Complex(x * f, -y * f);
	}

	/**
	 * @brief Return the additive inverse −z = (−x) + (−y)i.
	 *
	 * The name "uminus" follows a common convention for the unary
	 * minus operator.
	 *
	 * @return A new Complex equal to −(this).
	 */
	public Complex uminus() {
		return new Complex(-x, -y);
	}

	// ---------------------------------------------------------------
	//  String representations
	// ---------------------------------------------------------------

	/**
	 * @brief Return a human-readable string in standard algebraic form x ± yi.
	 *
	 * Special cases are handled for readability: purely real numbers
	 * omit the imaginary part, purely imaginary numbers omit the real
	 * part, and a unit imaginary coefficient is rendered as "i" rather
	 * than "1i".  Formatting of the double values is delegated to
	 * {@link MathUtil#d2String(double)}.
	 *
	 * @return A String such as "3.14", "−2i", or "1.0+0.5i".
	 */
	public String toString() {

		// essentially real?
		if (MathComplex.isSmall(y))
			return MathUtil.d2String(x);

		// essentially pure imaginary?
		if (MathComplex.isSmall(x)) {
			if (y == 1d)
				return "i";
			if (y == -1d)
				return "-i";
			else
				return MathUtil.d2String(y) + "i";
		}

		if (y == 1d) // y==1?
			return MathUtil.d2String(x) + "+i";
		if (y == -1d) // y== -1?
			return MathUtil.d2String(x) + "-i";

		// which sign?
		if (y > 0)
			return MathUtil.d2String(x) + "+" + MathUtil.d2String(y) + "i";
		else
			return MathUtil.d2String(x) + "-" + MathUtil.d2String(Math.abs(y))
					+ "i";
	}

	/**
	 * @brief Return a string in ordered-pair form "(x, y)".
	 * @return A String such as "(3.14, −2.0)".
	 */
	public String toString2() {
		return "(" + MathUtil.d2String(x) + "," + MathUtil.d2String(y) + ")";
	}

	/**
	 * @brief Return a string with the two components separated by a space.
	 *
	 * Useful for whitespace-delimited file output.
	 *
	 * @return A String such as "3.14 −2.0".
	 */
	public String toString3() {
		return x + " " + y;
	}

	// ---------------------------------------------------------------
	//  Further mathematical operations
	// ---------------------------------------------------------------

	/**
	 * @brief Return the principal square root of this complex number.
	 *
	 * Converts to polar form (r, φ) and computes
	 *   √z = √r · [cos(φ/2) + i sin(φ/2)],
	 * which always yields the root with non-negative real part
	 * (the "principal" branch).
	 *
	 * @return A new Complex equal to √(this).
	 */
	public Complex sqrt() {
		double r = this.abs();
		double phi = this.arg();
		return new Complex(Math.sqrt(r) * Math.cos(phi / 2.0), Math.sqrt(r)
				* Math.sin(phi / 2.0));
	}

	// paints the complex number
	// public void paint(Graphics g) {
	// g.setColor(theColor);
	// int xc = (int) (World.M2Gx(this));
	// int yc = (int) (World.M2Gy(this));
	// g.drawLine(xc - 3, yc - 3, xc + 3, yc + 3);
	// g.drawLine(xc - 3, yc + 3, xc + 3, yc - 3);
	// }

	// public Complex rotate(double theta) {
	// double xt = x * Math.cos(theta) - y * Math.sin(theta);
	// double yt = x * Math.sin(theta) + y * Math.cos(theta);
	// return new Complex(xt, yt);
	// }

	/**
	 * @brief Rotate this complex number counter-clockwise by the angle theta.
	 *
	 * Equivalent to multiplication by e^{i·theta}:
	 *   z' = z · (cos θ + i sin θ).
	 *
	 * @param theta  Rotation angle in radians.
	 * @return A new Complex representing the rotated value.
	 */
	public Complex rotate(double theta) {
		double xt = x * Math.cos(theta) - y * Math.sin(theta);
		double yt = x * Math.sin(theta) + y * Math.cos(theta);
		return new Complex(xt, yt);
	}

	/**
	 * @brief Compute the Euclidean dot product treating complex numbers
	 *        as 2-D real vectors.
	 *
	 * Returns Re(z)·Re(c) + Im(z)·Im(c), which equals Re(z · c̄).
	 *
	 * @param c  The other complex number.
	 * @return The real-valued dot product.
	 */
	public double dotProduct(Complex c) {
		return x * c.x + y * c.y;
	}

	// ---------------------------------------------------------------
	//  Spherical geometry conversions
	// ---------------------------------------------------------------

	/**
	 * @brief Interpret this complex number as (theta, phi) spherical
	 *        coordinates and return the corresponding Cartesian 3-D point
	 *        on the unit sphere.
	 *
	 * The convention follows standard spherical coordinates used
	 * throughout CirclePack:
	 *   - x-component (this.x) = theta (azimuthal angle),
	 *   - y-component (this.y) = phi   (polar angle from the north pole).
	 *
	 * The mapping is
	 *   (X, Y, Z) = (sin φ cos θ,  sin φ sin θ,  cos φ).
	 *
	 * @return A Point3D on the unit sphere.
	 */
	public Point3D getAsPoint() {
		return new Point3D(Math.sin(y) * Math.cos(x),
				Math.sin(y) * Math.sin(x), Math.cos(y));
	}

	// ---------------------------------------------------------------
	//  String parsing
	// ---------------------------------------------------------------

	/**
	 * @brief Parse a string into a Complex number.
	 *
	 * This factory method handles the many formats a user might type
	 * when entering a complex number interactively in CirclePack.
	 * Accepted formats include (among others):
	 *   - A plain real number or integer, e.g. "3.14", "-7".
	 *   - A pure imaginary, e.g. "2i", "i", "i*3", "-i".
	 *   - A full complex number, e.g. "1+2i", "1 + 2i", "3 - 4*i",
	 *     "i*2 + 3", etc.
	 *   - CirclePack variable references (identifiers starting with '_'),
	 *     which are resolved at parse time via {@link CPBase#varControl}.
	 *
	 * @par Parsing difficulties
	 * The parser must contend with several ambiguities:
	 *   - 'i' or 'I' can appear before or after the coefficient.
	 *   - A leading '-' might be a negative sign or a CirclePack flag.
	 *   - '-' also appears in scientific notation (e.g. "3E-01"); the
	 *     parser takes care not to split on the exponent sign.
	 *   - Optional '*' and '+' tokens between parts.
	 *
	 * @note The method does not yet accept parenthesized formats such
	 *       as "(x, y)".
	 *
	 * @param str  A trimmed string representing a complex number.
	 * @return A new Complex parsed from the string.
	 * @throws VarException  If the string cannot be interpreted as a
	 *                       complex number (wrong format, unresolvable
	 *                       variable, etc.).
	 */
	public static Complex string2Complex(String str)
			throws VarException {
		if (str.charAt(0) == '-' && StringUtil.isFlag(str))
			throw new VarException(
				"this seems to be a flag, not a complex number");
		if (str.charAt(0) == '+') // this should preclude '_' at start
			str = str.substring(1).trim();
		boolean minusflag = false;
		int spot_i = str.indexOf('i');
		int spot_I = str.indexOf('I');

		// Tokenize on whitespace to identify real and imaginary parts
		String[] pieces = str.split(" ");
		int numPieces = pieces.length;
		if (numPieces == 0)
			throw new VarException("empty string");
		if ((spot_i < 0 && spot_I < 0 && numPieces > 2) || (spot_i >= 0 && spot_I >= 0))
			throw new VarException("improper complex number format");

		// --- Single token: might need splitting at an internal '+' or '-' ---
		// Care is taken to skip the '-' in scientific-notation exponents
		// like "3.5E-02".
		if (numPieces == 1) {
			int splitspot = -1;
			int tick = 1;
			while (splitspot < 0 && tick < str.length() - 2) {
				if (str.charAt(tick) == '+') {
					char c = str.charAt(tick - 1);
					if (c != 'e' && c != 'E') {
						splitspot = tick;
						break;
					}
				}
				if (str.charAt(tick) == '-') {
					char c = str.charAt(tick - 1);
					if (c != 'e' && c != 'E') {
						splitspot = tick;
						break;
					}
				}
				tick++;
			}

			if (splitspot > 0) {
				pieces = new String[2];
				pieces[0] = str.substring(0, splitspot);
				pieces[1] = str.substring(splitspot);
				if (pieces[1].charAt(0) == '+') // shuck '+' sign
					pieces[1] = pieces[1].substring(1);
				numPieces = 2;
			}

			// Remaining single token: either a real number or pure imaginary
			else {
				if (spot_i >= 0 || spot_I >= 0) {
					Double imag = getImagPart(pieces[0]); // this also looks for variable
					if (imag == null)
						throw new VarException("pure imaginary format error");
					return new Complex(0.0, (double) imag);
				}
				// Check for a CirclePack variable reference
				if (str.charAt(0) == '_') {
					Double real = null;
					try {
						real = Double.parseDouble(CPBase.varControl.getValue(str));
						return new Complex((double) real);
					} catch (Exception ex) {
						throw new VarException("not read: " + ex.getMessage());
					}
				}
				try {
					return new Complex(Double.parseDouble(str));
				} catch (Exception ex) {
					throw new VarException("not a number :" + ex.getMessage());
				}
			}
		}

		// --- More than 2 tokens: reduce by consuming separators ---
		// Strips stray '*', '+', and '-' tokens and repositions 'i'/'I'
		// until exactly two meaningful tokens remain.
		while (numPieces > 2) {
//			String endone=pieces[numPieces-1];

			// unneeded '*'
			if (pieces[1].equals("*")) {
				for (int j = 1; j < numPieces - 1; j++)
					pieces[j] = pieces[j + 1];
				numPieces--;
			}

			// unneeded ' + '
			if (pieces[1].equals("+")) {
				for (int j = 1; j < numPieces - 1; j++)
					pieces[j] = pieces[j + 1];
				numPieces--;
			}

			// ' - ', hold for later
			else if (pieces[1].equals("-")) {
				minusflag = true;
				for (int j = 1; j < numPieces - 1; j++)
					pieces[j] = pieces[j + 1];
				numPieces--;
			}

			else if (pieces[1].startsWith("i") || pieces[1].startsWith("I")) {
				// Rewrite so 'i' is a suffix (e.g. "i*3" -> "3i")
				if (pieces[1].charAt(1) == '*') {
					if (pieces[1].length() > 2)
						pieces[1] = new String(pieces[1].substring(2) + "i");
				}
				else if (pieces[1].length() > 1) {
					pieces[1] = new String("-" + pieces[1].substring(1) + "i");
				}
				else { // or remove this piece and put 'i' at end of next one
					for (int j = 1; j < numPieces - 1; j++)
						pieces[j] = pieces[j + 1];
					numPieces--;
					pieces[1] = new String(pieces[1] + "i");
				}
			}

			// if last string is just 'i' or '*i', add 'i' to previous (should be number)
			else if (pieces[numPieces - 1].equalsIgnoreCase("*i") ||
					pieces[numPieces - 1].equalsIgnoreCase("i")) {
				pieces[numPieces - 2] = new String(pieces[numPieces - 1] + "i");
				pieces[numPieces - 1] = null;
				numPieces--;
			}

			else
				throw new VarException("improper format");
		}

		// --- Down to exactly two tokens: assign real and imaginary parts ---
		Double realpart = null;
		Double impart = null;

		spot_i = pieces[0].indexOf("i");
		spot_I = pieces[0].indexOf("I");

		// Imaginary part could be first
		if (spot_i >= 0 || spot_I >= 0) {
			impart = getImagPart(pieces[0]);
			if (impart == null) {
				throw new VarException("error in getting first segment as imaginary part");
			}
		}
		// else try to interpret as the real part
		else {
			try {
				if (pieces[0].charAt(0) == '_')
					realpart = Double.parseDouble(CPBase.varControl.getValue(pieces[0]));
				else realpart = Double.parseDouble(pieces[0]);
			} catch (Exception ex) {
				throw new VarException("error in real part: " + ex.getMessage());
			}
		}

		spot_i = pieces[1].indexOf("i");
		spot_I = pieces[1].indexOf("I");
		if ((spot_i >= 0 || spot_I >= 0)) {
			if (impart != null)
				throw new VarException("both parts think they are the imaginary part");
			impart = getImagPart(pieces[1]);
			if (impart == null) {
				throw new VarException("missing real or imaginary part");
			}
		}

		// If one part is still unresolved, try parsing the remaining token
		// as a plain number
		if (realpart == null || impart == null) {
			try {
				if (realpart != null)
					impart = Double.parseDouble(pieces[1]);
				else if (impart != null) {
					if (pieces[1].charAt(0) == '_')
						realpart = Double.parseDouble(CPBase.varControl.getValue(pieces[1]));
					else realpart = Double.parseDouble(pieces[1]);
				}
			} catch (Exception ex) {
				throw new VarException("failed to get real or imaginary part");
			}
		}

		if (realpart == null || impart == null)
			throw new VarException("missing real or imaginary part");

		double real = (double) realpart;
		double img = (double) impart;
		if (minusflag) img *= -1.0;
		return new Complex(real, img);
	}

	/**
	 * @brief Extract the imaginary coefficient from a string token that
	 *        contains 'i' or 'I'.
	 *
	 * Handles the varied positions of the imaginary marker:
	 *   - Bare "i" or "I" → coefficient is 1.0.
	 *   - Leading marker: "i*3.5", "i3.5", "I*_var".
	 *   - Trailing marker: "3.5i", "3.5*i", "_var*I".
	 *
	 * A leading '-' is consumed and its effect applied to the
	 * returned value.
	 *
	 * @param imag  A whitespace-free string containing exactly one 'i' or 'I'.
	 * @return The imaginary coefficient as a Double, or {@code null} if
	 *         the format is unrecognizable.
	 */
	public static Double getImagPart(String imag) {

		// first, check for leading negative sign
		boolean negSign = false;
		if (imag.startsWith("-")) {
			negSign = true;
			imag = new String(imag.substring(1));
		}

		int spot_i = imag.indexOf('i');
		int spot_I = imag.indexOf('I');

		// just 'i' or 'I' alone?
		if (imag.length() == 1) {
			if (negSign)
				return Double.valueOf(-1.0);
			return Double.valueOf(1.0);
		}

		// check for '*' separating the coefficient from 'i'
		int st = imag.indexOf("*");
		if (st < 0) { // no '*'
			// starts with 'i' or 'I' — coefficient follows
			if (spot_i == 0 || spot_I == 0) {
				try {
					if (imag.charAt(1) == '_') // variable?
						return Double.parseDouble(CPBase.varControl.getValue(imag.substring(1)));
					return Double.parseDouble(imag.substring(1));
				} catch (Exception ex) {
					return null;
				}
			}
			// ends with 'i' or 'I' — coefficient precedes
			if (spot_i == imag.length() - 1 || spot_I == imag.length() - 1) {
				try {
					return Double.parseDouble(imag.substring(0, imag.length() - 1));
				} catch (Exception ex) {
					return null;
				}
			}
		}

		// There is a '*': expect "i*<coeff>", "I*<coeff>", "<coeff>*i", or "<coeff>*I"
		if (imag.startsWith("i*") || imag.startsWith("I*")) {
			try {
				double nd;
				if (imag.charAt(2) == '_')  // variable?
					nd = Double.parseDouble(CPBase.varControl.getValue(imag.substring(2)));
				else nd = Double.parseDouble(imag.substring(2));
				if (negSign)
					nd *= -1.0;
				return Double.valueOf(nd);
			} catch (Exception ex) {
				return null;
			}
		}
		if (imag.endsWith("*i") || imag.endsWith("*I")) {
			double nd;
			try {
				nd = Double.parseDouble(imag.substring(0, imag.length() - 2));
			} catch (Exception ex) {
				return null;
			}
			if (negSign)
				nd *= -1.0;
			return Double.valueOf(nd);
		}

		// should have exhausted all properly formatted cases
		return null;
	}

	// ---------------------------------------------------------------
	//  Miscellaneous static utilities
	// ---------------------------------------------------------------

	/**
	 * @brief Inverse hyperbolic cosine for a real argument.
	 *
	 * Computes acosh(x) = ln(x + √(x² − 1)).  The argument must
	 * satisfy x ≥ 1 for a real-valued result; no range check is
	 * performed.
	 *
	 * @note This method duplicates {@link MathComplex#aCosh(double)}.
	 *
	 * @param x  A real value ≥ 1.
	 * @return acosh(x).
	 */
	public static double aCosh(double x) {
		return Math.log(x + Math.sqrt(x * x - 1.0));
	}

}
