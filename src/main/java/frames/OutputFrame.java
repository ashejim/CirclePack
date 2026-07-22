package frames;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JTabbedPane;

import circlePack.PackControl;
import panels.OutPanel;
import panels.PostPanel;
import panels.WritePackPanel;

/**
 * @brief Frame with tabbed panels for saving pack data, tailored data, and images.
 */
public class OutputFrame extends javax.swing.JFrame {

	private static final long 
	serialVersionUID = 1L;
	
	private JTabbedPane OutputTabbing;
	public OutPanel outPanel;
	public PostPanel postPanel;
	public WritePackPanel writePackPanel;

	// Constructor
	public OutputFrame() {
		super();
		addWindowListener(new WAdapter());
		setTitle("Saving Data from CirclePack");
		initGUI();
	}
	
	private void initGUI() {
		try {
//			setPreferredSize(new java.awt.Dimension(PackControl.PopupFrameWidth, 320)); // 250));
			{
				OutputTabbing = new JTabbedPane();
				{
					writePackPanel = new WritePackPanel();
					OutputTabbing.addTab("Write Pack Data", null, writePackPanel, null);
				}
				{
					outPanel = new OutPanel();
					OutputTabbing.addTab("Save tailored Data", null, outPanel, null);
				}
				{
					postPanel = new PostPanel();
					OutputTabbing.addTab("Export Image", null, postPanel, null);
				}
				this.add(OutputTabbing);
			}
			this.setSize(PackControl.PopupFrameWidth,320); // 275);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @brief Select the given output tab by index (bounds checked).
	 */
	public void setTab(int tab) {
		if (tab<0 || tab>OutputTabbing.getComponentCount())
			return;
		OutputTabbing.setSelectedIndex(tab);
	}
	
	/**
	 * @brief Window adapter that hides the output frame on close.
	 */
	class WAdapter extends WindowAdapter {
	   	/**
	   	 * @brief TODO: Document windowClosing.
	   	 * @param wevt
	   	 */
	   	public void windowClosing(WindowEvent wevt) {
	   		if (wevt.getID()==WindowEvent.WINDOW_CLOSING)
	   			PackControl.outputFrame.setVisible(false);
	   	}
	}
	     
}
