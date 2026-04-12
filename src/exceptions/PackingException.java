package exceptions;

/**
 * @brief Exception thrown for PackingException errors in CirclePack.
 */
public class PackingException extends RuntimeException {

	private static final long 
	serialVersionUID = 1L;
	
	public PackingException() {
		super();
	}
	
	public PackingException(String msg) {
		super(msg);
	}
	
}
