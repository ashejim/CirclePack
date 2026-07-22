package frames;

import images.CPIcon;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import mytools.MyTool;
import mytools.MyToolEditor;
import mytools.MyToolHandler;
import util.GlobResources;

/**
 * @brief Popup editor for creating command tools (command, icon, tooltip).
 *
 * CmdToolFrame is a popup frame to allow the user
 * to create tools to add to MyTools. Each tool
 * involves a string of commands for CirclePack,
 * an icon, and a tooltip. These can be saved in
 * XML format and are read into 'hashedTools' on
 * startup,
 * @author kens
 */
public class CmdToolEditor extends MyToolEditor {

	private static final long 
	serialVersionUID = 1L;

	static ArrayList<String> patterns;
	static {
		patterns=new ArrayList<String>();
		patterns.add("*.jpg");
		patterns.add("*.png");
		patterns.add("*.JPG");
		patterns.add("*.PNG");
	}
	private Set<String> appropIcons;

	// Constructor
	public CmdToolEditor(String tool_type,MyToolHandler par) {
		super(tool_type,par);
		if (tool_type==null || tool_type.length()==0) tool_type="MISC:";
		if (tool_type.startsWith("MAIN:")) {
			this.setTitle("Create a Tool for the main toolbar");
			iconDir=new String("main");
		}
		else if (tool_type.equals("BASIC:")) {
			this.setTitle("Create a Tool for the 'basic' toolbar");
			iconDir=new String("basic");
		}
		else if (tool_type.equals("MYTOOL:")) {
			this.setTitle("Create a Tool for a personal toolbar");
			iconDir=new String("tool");
		}
		try {
			appropIcons=GlobResources.glob("/Icons/"+iconDir,
					(List<String>)patterns);
		} catch(Exception ex) {
			System.err.println(ex.getMessage());
		}
		for (String n : appropIcons) {
			theCPIcons.addElement(new CPIcon(iconDir+"/"+n));
		}
		resetIconList();
	}

	/**
	 * @brief Build the top panel holding the command text area.
	 */
	public JPanel topPanel() { // based on 'prototypePanel'
		JPanel panel=new JPanel();
		try {
			panel.setPreferredSize(new java.awt.Dimension(400, 137));
			panel.setBorder(BorderFactory.createTitledBorder("Command"));
			{
				JScrollPane jScrollPane1 = new JScrollPane();
				panel.add(jScrollPane1);
				jScrollPane1.setPreferredSize(new java.awt.Dimension(377, 72));
				jScrollPane1.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
				{
					cmdArea = new JTextArea();
				    cmdArea.setLineWrap(true);
				    util.EmacsBindings.addEmacsBindings(cmdArea);
				    cmdArea.setToolTipText("Construct a command for CirclePack; see 'Help -> Command Details'");
					jScrollPane1.setViewportView(cmdArea);
					cmdArea.setPreferredSize(new java.awt.Dimension(377, 167));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return panel;
	}
	/**
	 * @brief Default dropability for tools (true).
	 *
	 * default dropability for tools
	 */
	public boolean setDropDefault() {
		return true;
	}
	
	/**
	 * @brief Description of the tool's substance ("a command string.").
	 */
	public String substanceText() {
		return new String("a command string.");
	}
	
	/**
	 * @brief Return the command string from the text area.
	 */
	public String formulateCmd() {
		return cmdArea.getText();
	}
	
	/**
	 * @brief Enable the dropable checkbox and set its default.
	 *
	 * Want a checkbox? set default
	 */
	public void dropableCheckBox() {
		wantDropBox=true; 
		dropMode=setDropDefault();
	}
	
	/**
	 * @brief Clear the command text area.
	 */
	public void resetMoreFields() {
		cmdArea.setText("");
	}
	
	/**
	 * @brief Populate the command text area from an existing tool.
	 */
	public void initMoreFields(MyTool theTool) {
		cmdArea.setText(theTool.getCommand());
	}

}
