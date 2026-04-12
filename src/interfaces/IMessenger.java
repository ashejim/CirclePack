package interfaces;

public interface IMessenger {
	/**
	 * @brief TODO: Document sendDebugMessage.
	 * @param message
	 */
	public void sendDebugMessage(String message);
	/**
	 * @brief TODO: Document sendErrorMessage.
	 * @param message
	 */
	public void sendErrorMessage(String message);
	/**
	 * @brief TODO: Document sendOutputMessage.
	 * @param message
	 */
	public void sendOutputMessage(String message);
}
