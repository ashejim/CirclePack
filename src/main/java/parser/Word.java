package parser;

/**
 * @brief Word class.
 *
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */public interface Word {
  /**
   * @aibrief Evaluate this word to a complex value using the evaluator.
   */
  public complex.Complex evaluate(Evaluator ev);
}
