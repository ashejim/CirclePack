package fauxScript;

import java.awt.Dimension;
import java.awt.Rectangle;
import javax.swing.JPanel;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;

/* This is a simple custom JPanel. When added to a 
 * JScrollPane, it will automatically resize itself
 * to the width of the view port of the JScrollPane.
 */
/**
 * @aibrief Scrollable JPanel that tracks its enclosing JScrollPane's viewport width.
 */
public class FWSJPanel extends JPanel implements Scrollable {
	private static final long serialVersionUID = 1L;

	/**
	 * @aibrief Return an arbitrary 100x100 preferred viewport size.
	 */
	@Override
	public Dimension getPreferredScrollableViewportSize() {
		/* This dimension is totally arbitrary. I have no idea
		 * what it does, but it worked fine so long as this function
		 * returns something.
		 */
		return new Dimension(100, 100);
	}

	/**
	 * @aibrief Block-scroll increment: 60px vertical, 120px horizontal.
	 */
	@Override
	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
		/* How many pixels to scroll for large scrolls, like when
		 * clicking a position along the scroll bar track.
		 */
		if (orientation == SwingConstants.VERTICAL) return 60;
		else return 120;
	}

	/**
	 * @aibrief Return true so the panel resizes to the viewport width.
	 */
	@Override
	public boolean getScrollableTracksViewportWidth() {
		/* Returning true in this function causes the JPanel
		 * to automatically resize itself to the width of the
		 * view port of the JScrollPane it is in.
		 */
		return true;
	}
	
	/**
	 * @aibrief Return false so the panel does not track viewport height.
	 */
	@Override
	public boolean getScrollableTracksViewportHeight() {
		/* Return false so it won't do the same for height. */
		return false;
	}

	/**
	 * @aibrief Unit-scroll increment: 20px vertical, 40px horizontal.
	 */
	@Override
	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
		/* How many pixels to scroll for small scrolls, like mouse wheel
		 * scrolling or clicking the arrows on the scroll bar.
		 */
		if (orientation == SwingConstants.VERTICAL) return 20;
		else return 40;
	}
}