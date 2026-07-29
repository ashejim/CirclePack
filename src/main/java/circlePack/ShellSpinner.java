package circlePack;

/**
 * @brief Some type of progress indicator when running in standalone
 *
 * Some type of progress indicator when running in standalone
 * @author kens
 *
 */
public class ShellSpinner extends RunProgress {

	/**
	 * @aibrief No-op progress start/stop; standalone mode shows no activity.
	 *
	 * As yet, no display of activity in standalone mode
	 */
	public void startstop(boolean ok) {
		
	}
	
	/** 
	 * @aibrief Always report not running; standalone mode has no running status.
	 *
	 * As yet, no running status; default to 'false'
	 */
	public boolean isRunning() {
		return false;
	}
	
}
