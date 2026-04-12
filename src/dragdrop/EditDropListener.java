package dragdrop;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;

import script.StackBox;

/**
 * @brief For MyTool edit tools drag/drop operation.
 *
 * For MyTool edit tools drag/drop operation. This is the listener for
 * the StackBox targets. It gets the action command string, which is in
 * the mytool name, and sends it to the action listener of the stackbox.
 * @author kens
 *
 */
public class EditDropListener implements DropTargetListener {
	private StackBox theBox;
	private String theActionCmd;

	// Constructor
	public EditDropListener(StackBox box) {
		theBox=box;
	}
	
	/**
	 * @brief TODO: Document dragEnter.
	 * @param event
	 */
	public void dragEnter(DropTargetDragEvent event) {}
	
	/**
	 * @brief TODO: Document dragExit.
	 * @param event
	 */
	public void dragExit(DropTargetEvent event) {}
	
	/**
	 * @brief TODO: Document dragOver.
	 * @param event
	 */
	public void dragOver(DropTargetDragEvent event) {}
	
	/**
	 * @brief TODO: Document dropActionChanged.
	 * @param event
	 */
	public void dropActionChanged(DropTargetDragEvent event) {}
	
	/**
	 * @brief TODO: Document drop.
	 * @param event
	 */
	public void drop(DropTargetDropEvent event) {
		if (!isDropOK(event)) {
			event.rejectDrop();
			return;
		}
		event.acceptDrop(DnDConstants.ACTION_LINK);
		Transferable transferable = event.getTransferable();
		theActionCmd=null;
		try {
			theActionCmd=(String)transferable.getTransferData(DataFlavor.stringFlavor);
		} catch(Exception e) {}
		if (theActionCmd==null) return; // some failure
		// send action command to stackbox, which creates event
		theBox.editAction(theActionCmd);
	}
	
	public boolean isDropOK(DropTargetDropEvent event) {
		return (event.getDropAction() & DnDConstants.ACTION_LINK)!=0;
	}
}
