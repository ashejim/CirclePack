package math.group;
import complex.Complex;

public class ComplexTransformation  {
  private int level=0;
  /**
   * @brief Abstract base class for transformations of the complex plane.
   * @param z
   * @return Complex
   */
  public Complex apply(Complex z) { 
	  return z;}
  public String getType() { 
	  return new String("Undefined");}
  public boolean isAffine() {
	  return true;}
  public int getLevel() {
    return level;
  }
  public void setLevel(int l) {
    level = l;
  }
 }
