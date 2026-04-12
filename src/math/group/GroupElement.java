package math.group;

/**
 * @brief Interface for elements of a mathematical group (Möbius transformations, quaternions, etc.).
 *
 * <p>Title: Element of an abstract group</p>
 * <p>Description: </p>
 * <p>Copyright: Fedor Andreev, Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author Fedor Andreev
 * @version 1.0
 */public interface GroupElement {
  /**
   * @brief Multiplies the current element by g from the left
   *
   * @return GroupElement
   * @param g GroupElement
   */
  public GroupElement lmultby(GroupElement g);

  /**
   * @brief Multiplies the current element by g from the right
   *
   * @return GroupElement
   * @param g GroupElement
   */
  public GroupElement rmultby(GroupElement g);
 /**
  * @brief Returns the inverse of the element
  * @return GroupElement
  */
 public GroupElement inverse();
 public void setLevel(int t);
 public int getLevel();
}
