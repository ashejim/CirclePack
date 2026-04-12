package complex;
import allMains.CPBase;
import graphObjects.CPCircle;

/**
 * @brief Static utility functions for complex arithmetic.
 *
 * This companion class to {@link Complex} collects static two-argument
 * operations (add, sub, mult, divide, pow, …) and numerical predicates
 * (isSmall, isInteger, distance) that do not belong on a single Complex
 * instance.  It also defines the fundamental complex constants
 * {@link #IM}, {@link #ZERO}, and {@link #ID}.
 *
 * @note The tolerance used by {@link #isSmall(double)} and related
 *       checks is currently hard-coded at 1 × 10⁻⁷.
 *
 * @author Fedor Andreev (original implementation, 2003)
 * @author Ken Stephenson (additions for CirclePack)
 * @version 1.1
 */

/* TODO: should define many operations by overloading. */
public class MathComplex {

  // ---------------------------------------------------------------
  //  Constants and tolerance
  // ---------------------------------------------------------------

  /**
   * @brief Absolute tolerance for "small-number" checks.
   *
   * A double value whose absolute value is less than {@code tol} is
   * considered negligible (see {@link #isSmall(double)}).  This
   * threshold is also used implicitly by division and reciprocal
   * operations in {@link Complex}.
   */
  private static double tol=1E-7;

  /**
   * @brief Legacy error-message string.
   * @deprecated Error reporting now uses exceptions; retained for
   *             backward compatibility with older code paths.
   */
  private static String Error="";

  /** The imaginary unit i = 0 + 1i. */
  public static final Complex IM = new Complex(0d,1d);

  /** The complex zero 0 + 0i. */
  public static final Complex ZERO = new Complex(0d,0d);

  /** The complex unity (multiplicative identity) 1 + 0i. */
  public static final Complex ID = new Complex(1d,0d);

  // ---------------------------------------------------------------
  //  Transcendental functions
  // ---------------------------------------------------------------

  /**
   * @brief Inverse hyperbolic cosine for a real argument.
   *
   * Computes acosh(x) = ln(x + √(x² − 1)).  The argument must
   * satisfy x ≥ 1 for a real-valued result; no range check is
   * performed.
   *
   * @param x  A real value ≥ 1.
   * @return acosh(x).
   */
  public static double aCosh(double x) {
	  return Math.log(x+Math.sqrt(x*x-1.0));
  }
  
  // ---------------------------------------------------------------
  //  Basic arithmetic (static two-argument forms)
  // ---------------------------------------------------------------

  /**
   * @brief Static addition: return t1 + t2.
   * @param t1  First addend.
   * @param t2  Second addend.
   * @return A new Complex equal to t1 + t2.
   */
  public static Complex add(Complex t1, Complex t2) {
    return t1.add(t2);
  }

  /**
   * @brief Static subtraction of two complex numbers: return t1 − t2.
   * @param t1  Minuend.
   * @param t2  Subtrahend.
   * @return A new Complex equal to t1 − t2.
   */
  public static Complex sub(Complex t1, Complex t2) {
    return t1.sub(t2);
  }

  /**
   * @brief Subtract a real number from a complex number: return t1 − t2.
   * @param t1  A complex minuend.
   * @param t2  A real subtrahend.
   * @return A new Complex equal to t1 − t2.
   */
  public static Complex sub(Complex t1, double t2) {
    return t1.sub(t2);
  }

  /**
   * @brief Subtract a CPCircle center from a complex number.
   *
   * Treats the CPCircle's (x, y) as a complex number and returns the
   * difference t1 − (t2.x + i·t2.y).
   *
   * @param t1  A complex minuend.
   * @param t2  A CPCircle whose center is subtracted.
   * @return A new Complex equal to t1 − center(t2).
   */
  public static Complex sub(Complex t1, CPCircle t2) {
    return new Complex(t1.x-t2.x,t1.y-t2.y);
  }

  /**
   * @brief Subtract one CPCircle center from another.
   * @param t1  First CPCircle.
   * @param t2  Second CPCircle.
   * @return A new Complex equal to center(t1) − center(t2).
   */
  public static Complex sub(CPCircle t1, CPCircle t2) {
    return new Complex(t1.x-t2.x,t1.y-t2.y);
  }

  /**
   * @brief Subtract a complex number from a real number: return t1 − t2.
   * @param t1  A real minuend.
   * @param t2  A complex subtrahend.
   * @return A new Complex equal to (t1 − Re(t2)) − i·Im(t2).
   */
  public static Complex sub(double t1, Complex t2) {
    return new Complex(t1 - t2.real(), -t2.imag());
  }

  /**
   * @brief Return the additive inverse −t.
   * @param t  A complex number.
   * @return A new Complex equal to −t.
   */
  public static Complex uminus(Complex t) {
    return new Complex( -t.real(), -t.imag());
  }

  /**
   * @brief Multiply a real scalar by a complex number: return t · c.
   * @param t  A real scalar.
   * @param c  A complex number.
   * @return A new Complex equal to t · c.
   */
  public static Complex mult(double t,Complex c) {
    return c.times(t);
  }

  /**
   * @brief Static division: return a / b.
   * @param a  The dividend.
   * @param b  The divisor.
   * @return A new Complex equal to a / b.
   */
  public static Complex divide(Complex a, Complex b) {
    return a.divide(b);
  }

  // ---------------------------------------------------------------
  //  Norms, moduli, and distances
  // ---------------------------------------------------------------

  /**
   * @brief Return the squared modulus |c|² = Re(c)² + Im(c)².
   *
   * Useful for proximity checks where only relative distance matters,
   * since it avoids the cost of a square root.
   *
   * @param c  A complex number.
   * @return |c|².
   */
  public static double absSq(Complex c) {
    return (c.real() * c.real() + c.imag() * c.imag());
  }

  /**
   * @brief Synonym for {@link #absSq(Complex)}.
   * @param c  A complex number.
   * @return |c|².
   */
  public static double normSq(Complex c) {
    return (c.real() * c.real() + c.imag() * c.imag());
  }

  /**
   * @brief Return the modulus (absolute value) |c|.
   * @param c  A complex number.
   * @return |c| = sqrt(Re(c)² + Im(c)²).
   */
  public static double norm(Complex c) {
    return Math.sqrt(normSq(c));
  }

  /**
   * @brief Euclidean distance between two complex numbers |a − b|.
   * @param a  First complex number.
   * @param b  Second complex number.
   * @return |a − b|.
   */
  public static double distance(Complex a, Complex b) {
    return norm(sub(a,b));
  }

  /**
   * @brief Squared Euclidean distance |a − b|².
   *
   * Faster than {@link #distance(Complex, Complex)} when only
   * relative comparisons are needed.
   *
   * @param a  First complex number.
   * @param b  Second complex number.
   * @return |a − b|².
   */
  public static double distanceSq(Complex a, Complex b) {
    return normSq(sub(a,b));
  }
  
  /**
   * @brief Squared distance from the point (x, y) to a complex number a.
   * @param x  Real coordinate of the point.
   * @param y  Imaginary coordinate of the point.
   * @param a  A complex number.
   * @return (x − Re(a))² + (y − Im(a))².
   */
  public static double distanceSq(double x, double y, Complex a) {
    return (x-a.real())*(x-a.real())+(y-a.imag())*(y-a.imag());
  }
  
  /**
   * @brief Squared distance between two points given as raw doubles.
   * @param x   Real coordinate of first point.
   * @param y   Imaginary coordinate of first point.
   * @param x1  Real coordinate of second point.
   * @param y1  Imaginary coordinate of second point.
   * @return (x − x1)² + (y − y1)².
   */
  public static double distanceSq(double x, double y, double x1, double y1) {
    return (x-x1)*(x-x1)+(y-y1)*(y-y1);
  }

  /**
   * @brief Synonym for {@link #norm(Complex)}: return |c|.
   * @param c  A complex number.
   * @return |c|.
   */
  public static double abs(Complex c) {
    return norm(c);
  }
  
  // ---------------------------------------------------------------
  //  Exponential, logarithm, and power
  // ---------------------------------------------------------------

  /**
   * @brief Complex exponential: return e^t.
   *
   * Uses Euler's formula: e^(a+bi) = e^a (cos b + i sin b).
   *
   * @param t  A complex exponent.
   * @return A new Complex equal to e^t.
   */
  public static Complex exp(Complex t) {
    return new Complex(Math.exp(t.real()) * Math.cos(t.imag()),
                       Math.exp(t.real()) * Math.sin(t.imag()));
  }
  
  /**
   * @brief Complex natural logarithm: return ln(t).
   *
   * Computes the principal value of the complex logarithm,
   * ln(t) = ln|t| + i·arg(t), where the argument is taken in
   * the range [0, 2π).
   *
   * @note The branch convention here places the cut along the
   *       negative real axis but returns values in [0, 2π) rather
   *       than the more common (−π, π].  This differs from
   *       {@link Complex#arg()}.
   *
   * @param t  A complex number (should be nonzero).
   * @return A new Complex equal to ln(t).
   */
  public static Complex ln(Complex t) {
    double phi = 0d;
    double rho = abs(t);
    double tempx = Math.log(rho);
    // Determine the argument in [0, 2π) by quadrant analysis
    if (Math.abs(t.real()) > tol)
      if(t.real()>0)
        if(t.imag()>=0)
          phi = Math.atan(t.imag() / t.real());
        else
          phi = 2*Math.PI+Math.atan(t.imag() / t.real());
      else
          phi = Math.atan(t.imag()/t.real()) + Math.PI;
    else {
      if (t.real() > 0)
        phi = CPBase.piby2;
      if (t.real() < 0)
        phi = 3 * CPBase.piby2;
    }
    return new Complex(tempx, phi);
  }
  
  /**
   * @brief Complex power: return a^b = e^{b·ln(a)}.
   * @param a  The base (complex).
   * @param b  The exponent (complex).
   * @return A new Complex equal to a^b using the principal logarithm.
   */
  public static Complex pow(Complex a, Complex b) {
    // a^b = e^{ln(a)*b}
    return exp(ln(a).times(b));
  }
  
  /**
   * @brief Integer power: return a^l by repeated multiplication.
   *
   * For negative exponents, computes by repeated division (i.e.
   * a^{−n} = 1/a/a/…/a, n times).
   *
   * @param a  The base (complex).
   * @param l  The integer exponent (may be negative or zero).
   * @return A new Complex equal to a^l.
   */
  public static Complex pow(Complex a, int l) {
    Complex s = new Complex(1.0);
    if (l==0) return s;
    if (l<0) {
    	for (int i = 0; i < -l; i++)
    		s = s.divide(a);
    }
    else { 
    	for (int i = 0; i < l; i++)
    		s = s.times(a);
    }
    return s;
  }
  
  // ---------------------------------------------------------------
  //  Trigonometric functions
  // ---------------------------------------------------------------

  /**
   * @brief Complex cosine: return cos(c).
   *
   * Uses the identity cos(c) = (e^{ic} + e^{−ic}) / 2.
   *
   * @param c  A complex argument.
   * @return A new Complex equal to cos(c).
   */
  public static Complex cos(Complex c) {
    Complex d = exp(c.mult(MathComplex.IM));
    return (d.add(d.reciprocal())).divide(2.0);
  }
  
  /**
   * @brief Complex sine: return sin(c).
   *
   * Uses the identity sin(c) = (e^{ic} − e^{−ic}) / (2i).
   *
   * @param c  A complex argument.
   * @return A new Complex equal to sin(c).
   */
  public static Complex sin(Complex c) {
    Complex d = exp(c.mult(MathComplex.IM));
    return (d.sub(d.reciprocal())).divide(MathComplex.IM.mult(2d));
  }
  
  // ---------------------------------------------------------------
  //  Predicates
  // ---------------------------------------------------------------

  /**
   * @brief Test whether a complex number is (approximately) a real integer.
   *
   * Returns true if the imaginary part is small (below tolerance) and
   * the real part is within tolerance of a whole number.
   *
   * @param c  A complex number.
   * @return {@code true} if c ≈ n + 0i for some integer n.
   */
  public static boolean isInteger(Complex c) {
    if(!isSmall(c.imag()))
      return false;
    return MathComplex.isInteger(c.real());
  }
  
  /**
   * @brief Test whether a real number is approximately an integer.
   * @param d  A double value.
   * @return {@code true} if |d − round(d)| < tolerance.
   */
  public static boolean isInteger(double d) {
    return isSmall(d-Math.round(d));
  }
  
  /**
   * @brief Return the current small-number tolerance.
   * @return The tolerance threshold (default 1 × 10⁻⁷).
   */
  public static double getTolerance() {
    return tol;
  }
  
  /**
   * @brief Test whether a real number is negligibly small.
   * @param x  A double value.
   * @return {@code true} if |x| < tolerance.
   */
  public static boolean isSmall(double x) {
    if (Math.abs(x) < tol)
      return true;
    else
      return false;
  }
  
  /**
   * @brief Test whether a complex number has negligible modulus.
   * @param x  A complex number.
   * @return {@code true} if |x| < tolerance.
   */
  public static boolean isSmall(Complex x) {
    if (x.abs() < tol)
      return true;
    else
      return false;
  }

  // ---------------------------------------------------------------
  //  Geometric utilities
  // ---------------------------------------------------------------

  /**
   * @brief Distance from a point to a line segment.
   *
   * Computes the shortest Euclidean distance from point p0 to the
   * line segment from p1 to p2.  If the perpendicular foot lies
   * between p1 and p2, the perpendicular distance is returned;
   * otherwise the distance to the nearer endpoint is returned.
   *
   * @note There appears to be a minor bug: the variable {@code yopt}
   *       is computed using {@code p1.real()} instead of
   *       {@code p1.imag()}.
   *
   * @param p0  The query point.
   * @param p1  First endpoint of the segment.
   * @param p2  Second endpoint of the segment.
   * @return The shortest distance from p0 to segment [p1, p2].
   */
  public static double distancePL(Complex p0, Complex p1, Complex p2) {
    // Direction vector of the segment
    double ax = p2.real() - p1.real();
    double ay = p2.imag() - p1.imag();
    // Parameter t for the foot of the perpendicular from p0 onto the line
    double t = ( (p0.real() - p1.real()) * ax + (p0.imag() - p1.imag()) * ay) /
        (ax * ax + ay * ay);
    // Coordinates of the foot
    double xopt = p1.real() + ax * t;
    double yopt = p1.real() + ay * t;
    // Check whether the foot lies between p1 and p2 by examining
    // whether the vectors from the endpoints to the foot point in
    // opposite directions
    // atemp1 from p1 to opt
    double atemp1x = xopt - p1.real();
    double atemp1y = yopt - p1.real();
    // atemp2 from p2 to opt
    double atemp2x = xopt - p2.real();
    double atemp2y = yopt - p2.imag();

    // atemp1 and atemp2 should have opposite sign if foot is between endpoints
    if ( (atemp1x * atemp2x <= 0) && (atemp1y * atemp2y <= 0))
      return Math.sqrt( (p0.real() - xopt) * (p0.real() - xopt) +
                       (p0.imag() - yopt) * (p0.imag() - yopt));
    else
      return Math.min(Math.sqrt( (p0.real() - p1.real()) * (p0.real() - p1.real()) +
                                (p0.imag() - p1.imag()) * (p0.imag() - p1.imag())),
                      Math.sqrt( (p0.real() - p2.real()) * (p0.real() - p2.real()) +
                                (p0.imag() - p2.imag()) * (p0.imag() - p2.imag())));
  }

  /**
   * @brief Return the stored error message string.
   * @deprecated No longer actively used; error reporting has moved
   *             to exceptions.
   * @return The error string (empty if no error has been recorded).
   */
  public static String getError() {
    return Error;
  }
  
  /**
   * @brief Record an error message string.
   * @deprecated No longer actively used; see {@link #getError()}.
   * @param s  The error message to store.
   */
  public static void setError(String s) {
    Error = s;
  }
  
  /**
   * @brief Return the principal square root of a complex number.
   *
   * Delegates to {@link Complex#sqrt()}.
   *
   * @param c  A complex number.
   * @return A new Complex equal to √c.
   */
  public static Complex sqrt(Complex c) {
    return c.sqrt();
  }
  
  /**
   * @brief Test whether a complex point lies inside an axis-aligned rectangle.
   *
   * The rectangle is defined by its top-left corner (in the standard
   * mathematical plane, where y increases upward), a width extending
   * to the right, and a height extending downward.
   *
   * @param c        The point to test.
   * @param leftTop  The top-left corner of the rectangle.
   * @param width    Width of the rectangle (positive, extending right).
   * @param height   Height of the rectangle (positive, extending down).
   * @return {@code true} if c lies strictly inside the rectangle.
   */
  public static boolean isInRectangle(Complex c, Complex leftTop, double width,
                                      double height) {
    if ( (c.real() >= leftTop.real() + width) ||
        (c.real() <= leftTop.real()) ||
        (c.imag() <= leftTop.imag() - height) ||
        (c.imag() >= leftTop.imag()))
      return false;
    else
      return true;
  }
  
  /**
   * @brief Generate a random complex number with both components in [0, 1].
   * @return A new Complex with x, y each uniformly distributed in [0, 1].
   */
  public static Complex smallRandomComplex() {
    return new Complex(Math.random(),Math.random());
  }
  
  /**
   * @brief Return the principal argument of a complex number in (−π, π].
   *
   * Delegates to {@link Math#atan2(double, double)}.
   *
   * @param c  A complex number.
   * @return arg(c) in radians, in the range (−π, π].
   */
  public static double Arg (Complex c) {
	  return Math.atan2(c.getImaginary(),c.getReal());
  }
  
  /**
   * @brief Compute the positive angular difference from start to end.
   *
   * Given two angles in radians (typically in [−π, π] as returned by
   * {@link Math#atan2}), returns the counter-clockwise sweep from
   * {@code start} to {@code end}, always in the range [0, 2π).
   *
   * @param start  Starting angle in radians.
   * @param end    Ending angle in radians.
   * @return The positive angular difference in [0, 2π).
   */
  public static double radAngDiff(double start,double end) {
	  double diff=end-start;
	  if (diff<0) diff += 2*Math.PI;
	  return diff;
  }
  
  /**
   * @brief Static convenience: compute sqrt(x² + y²).
   * @param x  First component.
   * @param y  Second component.
   * @return The Euclidean norm of (x, y).
   */
  public static double abs(double x, double y) {
    return Math.sqrt(x*x+y*y);
  }
}
