package exceptions;

/**
 * @brief Exception thrown for LayoutException errors in CirclePack.
 */
public class LayoutException extends RuntimeException {

	private static final long 
	serialVersionUID = 1L;

	public LayoutException() {
		super();
	}
	
	public LayoutException(String msg) {
		super(msg);
	}
	
}
