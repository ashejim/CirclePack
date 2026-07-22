package frames;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import allMains.CirclePack;
import circlePack.PackControl;
import input.CmdSource;
import input.FileDialogs;
import input.MyConsole;
import input.ShellManager;

/**
 * @brief A standalone, PowerShell-style terminal window for CirclePack.
 *
 * A standalone, persistent terminal window for entering CirclePack
 * commands. It combines two pieces that already exist in the GUI:
 * <ul>
 * <li>a read-only scroll-back transcript ({@link #transcriptPane}) that
 *     renders {@link ShellManager#runHistory} -- the same HTML history of
 *     executed commands (prefixed with "&gt; "), messages (blue) and
 *     errors (red) shown by the hovering shell; and</li>
 * <li>a {@link MyConsole} command line pinned at the bottom. Commands run
 *     on ENTER (no "Run" button), with the usual up/down command history,
 *     TAB completion and emacs key bindings.</li>
 * </ul>
 * A small toolbar across the top lets the user save the session's commands
 * as a runnable '.cps' script or clear the transcript.
 *
 * The transcript is refreshed by {@link #refreshTranscript()}, which is
 * invoked from {@link ShellManager#processCmdResults} after every command
 * so output appears inline, just like a real terminal.
 *
 * @author Claude, based on existing MessageHover/MyConsole work by kens and Alex Fawkes
 */
public class TerminalFrame extends JFrame {

	private static final long
	serialVersionUID = 1L;

	final static int WIDE = 640;
	final static int HIGH = 420;

	public JTextPane transcriptPane;   // read-only scroll-back history
	public MyConsole cmdConsole;       // command entry line at the bottom
	private JScrollPane transcriptScroller;

	// Constructor
	public TerminalFrame() {
		super();
		setTitle("CirclePack Terminal");
		addWindowListener(new WAdapter());
		initGUI();
	}

	private void initGUI() {
		try {
			setLayout(new BorderLayout());

			// toolbar: save / clear
			JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
			toolBar.setBackground(new Color(253, 253, 224));

			JButton saveButton = new JButton("Save as .cps");
			saveButton.setToolTipText("Save this terminal's commands as a runnable CirclePack script");
			saveButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					saveAsCps();
				}
			});

			JButton clearButton = new JButton("Clear");
			clearButton.setToolTipText("Clear the terminal transcript and saved command list");
			clearButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					clearTranscript();
				}
			});

			toolBar.add(saveButton);
			toolBar.add(clearButton);
			add(toolBar, BorderLayout.NORTH);

			// transcript: same content/source the hovering shell uses
			transcriptPane = new JTextPane();
			transcriptPane.setContentType("text/html");
			transcriptPane.setEditable(false);
			transcriptPane.setBackground(new Color(253, 253, 224));
			if (ShellManager.runHistory != null)
				transcriptPane.setText(ShellManager.runHistory.toString());

			transcriptScroller = new JScrollPane(transcriptPane);
			transcriptScroller.setHorizontalScrollBarPolicy(
					JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			transcriptScroller.setVerticalScrollBarPolicy(
					JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
			transcriptScroller.setMinimumSize(new Dimension(WIDE / 4, 50));
			add(transcriptScroller, BorderLayout.CENTER);

			// command line, reusing the standard console machinery
			cmdConsole = new MyConsole(CmdSource.MESSAGE_FRAME, "terminal");
			cmdConsole.initGUI(WIDE);
			cmdConsole.box.setBackground(new Color(253, 253, 224));
			add(cmdConsole.box, BorderLayout.SOUTH);

			setSize(WIDE, HIGH);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @brief Reload the transcript from {@link ShellManager#runHistory} and
	 * scroll to the bottom. Called after each command so output appears inline.
	 */
	public void refreshTranscript() {
		if (transcriptPane == null || ShellManager.runHistory == null)
			return;
		try {
			synchronized (transcriptPane) {
				transcriptPane.setText(ShellManager.runHistory.toString());
				transcriptPane.setCaretPosition(
						transcriptPane.getDocument().getLength());
				transcriptPane.revalidate();
			}
		} catch (Exception ex) {
			System.err.println("terminal writing problem: " + ex.getMessage());
		}
	}

	/**
	 * @brief Show the terminal, bring it forward, refresh, and put the
	 * caret in the command line ready for typing.
	 */
	public void showTerminal() {
		refreshTranscript();
		setVisible(true);
		setState(Frame.NORMAL);
		toFront();
		if (cmdConsole != null)
			cmdConsole.focusToCmdline();
	}

	/**
	 * @brief Clear the transcript and saved command list, then refresh
	 * this terminal and the hovering shell.
	 */
	private void clearTranscript() {
		if (PackControl.shellManager != null)
			PackControl.shellManager.clearHistory();
		refreshTranscript();
		MessageHover.updateShellPane();
	}

	/**
	 * @brief Save the terminal session's commands as a runnable '.cps'
	 * CirclePack script. The commands come from {@link ShellManager#scriptCmds}
	 * (the visible transcript order); each becomes one &lt;cmd&gt; element.
	 */
	private void saveAsCps() {
		List<String> cmds = ShellManager.scriptCmds;
		if (cmds == null || cmds.isEmpty()) {
			CirclePack.cpb.errMsg("Terminal has no commands to save.");
			return;
		}
		File f = FileDialogs.saveDialog(FileDialogs.SCRIPT, true,
				"Save terminal commands as a script");
		if (f == null)
			return;
		// ensure a script extension
		String low = f.getPath().toLowerCase();
		if (!low.endsWith(".cps") && !low.endsWith(".xmd"))
			f = new File(f.getPath() + ".cps");

		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(f));
			writeCpsScript(bw, f.getName(), cmds);
			bw.close();
			CirclePack.cpb.msg("Saved terminal script (" + cmds.size()
					+ " commands): " + f.getPath());
		} catch (Exception ex) {
			CirclePack.cpb.errMsg("Failed to save script: " + ex.getMessage());
			try {
				if (bw != null)
					bw.close();
			} catch (IOException iox) {
			}
		}
	}

	/**
	 * @brief Write a minimal, valid CP_Scriptfile with one &lt;cmd&gt; per command.
	 */
	private void writeCpsScript(BufferedWriter bw, String fileName, List<String> cmds)
			throws IOException {
		String date = DateFormat.getDateInstance(DateFormat.MEDIUM).format(new Date());
		String title = fileName;
		int dot = title.lastIndexOf('.');
		if (dot > 0)
			title = title.substring(0, dot);

		bw.write("<?xml version=\"1.0\"?>");
		bw.newLine();
		bw.write("<CP_Scriptfile date=\"" + xml(date) + "\">");
		bw.newLine();
		bw.write("<CPscript title=\"" + xml(title) + "\">");
		bw.newLine();
		bw.write("<text> Commands exported from the CirclePack terminal. </text>");
		bw.newLine();
		bw.write("<Section title=\"Terminal commands\">");
		bw.newLine();
		for (String c : cmds) {
			if (c == null)
				continue;
			String cc = c.trim();
			if (cc.length() == 0)
				continue;
			if (!cc.endsWith(";"))
				cc = cc + ";";
			bw.write("<cmd>" + xml(cc) + " </cmd>");
			bw.newLine();
		}
		bw.write("</Section>");
		bw.newLine();
		bw.write("</CPscript>");
		bw.newLine();
		bw.write("</CP_Scriptfile>");
		bw.newLine();
	}

	/** minimal XML escaping for attribute/element content */
	private static String xml(String s) {
		return s.replace("&", "&amp;").replace("<", "&lt;")
				.replace(">", "&gt;").replace("\"", "&quot;");
	}

	class WAdapter extends WindowAdapter {
		public void windowClosing(WindowEvent wevt) {
			if (wevt.getID() == WindowEvent.WINDOW_CLOSING)
				PackControl.terminalFrame.setVisible(false);
		}
	}
}
