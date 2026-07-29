package circlePack;

/**
 * @brief Abstract class for indicating progress during computations.
 *
 * Abstract class for indicating progress during computations.
 * E.g. use differs between standalone and GUI runs.
 * @author kens
 *
 */
public abstract class RunProgress {
	
	/**
	 * @aibrief Start or stop the progress indicator.
	 */
	public abstract void startstop(boolean ok);
	/**
	 * @aibrief Report whether a computation is currently running.
	 */
	public abstract boolean isRunning();

}
