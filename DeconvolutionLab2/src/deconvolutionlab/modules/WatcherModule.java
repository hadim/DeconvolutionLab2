/*
 * DeconvolutionLab2
 * 
 * Conditions of use: You are free to use this software for research or
 * educational purposes. In addition, we expect you to include adequate
 * citations and acknowledgments whenever you present or publish results that
 * are based on it.
 * 
 * Reference: DeconvolutionLab2: An Open-Source Software for Deconvolution
 * Microscopy D. Sage, L. Donati, F. Soulez, D. Fortun, G. Schmit, A. Seitz,
 * R. Guiet, C. Vonesch, M Unser, Methods of Elsevier, 2017.
 */

/*
 * Copyright 2010-2017 Biomedical Imaging Group at the EPFL.
 * 
 * This file is part of DeconvolutionLab2 (DL2).
 * 
 * DL2 is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * DL2 is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * DL2. If not, see <http://www.gnu.org/licenses/>.
 */

package deconvolutionlab.modules;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

import deconvolution.Command;
import deconvolutionlab.Config;
import lab.component.GridPanel;
import lab.tools.Files;

public class WatcherModule extends AbstractModule implements ActionListener, KeyListener {

	private JTextField			txtTime;
	private JTextField			txtPath;
	private JButton				bnBrowse;
	private JComboBox<String>	cmbTime;
	private JComboBox<String>	cmbPath;
	private JComboBox<String>	cmbVerbose;
	private JCheckBox			chkDisableTable;
	private JCheckBox			chkDisableConsole;
	private JCheckBox			chkDisableDisplay;
	private JCheckBox			chkDisableMultithreading;

	public WatcherModule(boolean expanded) {
		super("Watcher", "", "Default", "", expanded);
	}

	@Override
	public String getCommand() {
		String cmd = "";
		boolean t = chkDisableTable.isSelected();
		boolean c = chkDisableConsole.isSelected();
		boolean d = chkDisableDisplay.isSelected();
		boolean m = chkDisableMultithreading.isSelected();
		if (cmbPath.getSelectedIndex() != 0)
			cmd += " -path " + txtPath.getText();
		if (cmbTime.getSelectedIndex() != 0)
			cmd += " -time " + txtTime.getText();
		if (c || t || d || m)
			cmd += " -disable " + (t ? "monitor " : "") + 
					(c ? "console " : "") + (d ? "display " : "") + (m ? "multithreading " : "");
		if (cmbVerbose.getSelectedIndex() != 0) {
			String parts[] = ((String)cmbVerbose.getSelectedItem()).split(" ");
			cmd += " -verbose " + parts[1];
		}
		return cmd;
	}

	@Override
	public JPanel buildExpandedPanel() {

		cmbTime = new JComboBox<String>(new String[] { "No time limitation", "Specify limit (s) ..." });
		cmbPath = new JComboBox<String>(new String[] { "Current", "Specify ..." });
		cmbVerbose = new JComboBox<String>(new String[] { "Verbose: log", "Verbose: quiet ", "Verbose: prolix", "Verbose: mute" });
		chkDisableTable = new JCheckBox("No Monitor");
		chkDisableConsole = new JCheckBox("No Console");
		chkDisableDisplay = new JCheckBox("No Display");
		chkDisableMultithreading = new JCheckBox("No Multithreading");

		txtTime = new JTextField("3600");
		txtPath = new JTextField("...", 30);
		bnBrowse = new JButton("Browse");

		GridPanel pn = new GridPanel("Output Directory", 3);
		pn.place(0, 1, cmbPath);
		pn.place(0, 2, bnBrowse);
		pn.place(1, 0, 4, 1, txtPath);

		GridPanel pn2 = new GridPanel("Settings", 3);
		pn2.place(2, 0, 2, 1, cmbTime);
		pn2.place(2, 2, txtTime);
		pn2.place(2, 3, cmbVerbose);
		pn2.place(4, 0, chkDisableTable);
		pn2.place(4, 1, chkDisableConsole);
		pn2.place(4, 2, chkDisableDisplay);
		pn2.place(4, 3, chkDisableMultithreading);
		
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.add(pn);
		panel.add(pn2);

		Config.register(getName(), "verbose", cmbVerbose, cmbVerbose.getItemAt(0));
		Config.register(getName(), "path", cmbPath, cmbPath.getItemAt(0));
		Config.register(getName(), "time", cmbTime, cmbTime.getItemAt(0));
		Config.register(getName(), "time.value", txtTime, "3600");
		Config.register(getName(), "path", txtPath, new File(System.getProperty("user.dir")).getAbsolutePath());
		Config.register(getName(), "disable.table", chkDisableTable, "false");
		Config.register(getName(), "disable.console", chkDisableConsole, "false");
		Config.register(getName(), "disable.display", chkDisableDisplay, "false");
		Config.register(getName(), "disable.multithreading", chkDisableMultithreading, "false");

		cmbVerbose.addActionListener(this);
		cmbPath.addActionListener(this);
		cmbTime.addActionListener(this);
		chkDisableTable.addActionListener(this);
		chkDisableConsole.addActionListener(this);
		chkDisableDisplay.addActionListener(this);
		chkDisableMultithreading.addActionListener(this);

		txtTime.addKeyListener(this);
		bnBrowse.addActionListener(this);
		txtPath.addKeyListener(this);

		return panel;
	}

	private void update() {
		setCommand(getCommand());
		setSynopsis(txtPath.getText());
		if (cmbPath != null) {
			boolean b = cmbPath.getSelectedIndex() != 0;
			bnBrowse.setEnabled(b);
			txtPath.setEnabled(b);
		}
		if (txtTime != null) {
			boolean b = cmbTime.getSelectedIndex() != 0;
			txtTime.setEnabled(b);
		}
		Command.command();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		super.actionPerformed(e);
		if (e.getSource() == bnBrowse) {
			File f = Files.browseDirectory(txtPath.getText());
			if (f != null) {
				txtPath.setText(f.getAbsolutePath());
			}
		}
		if (e.getSource() == cmbPath) {
			if (cmbPath.getSelectedIndex() == 0) {
				File f = new File(System.getProperty("user.dir"));
				txtPath.setText(f.getAbsolutePath());
			}

		}
		update();
	}

	@Override
	public void close() {
		chkDisableTable.removeActionListener(this);
		chkDisableConsole.removeActionListener(this);
		chkDisableDisplay.removeActionListener(this);
		chkDisableMultithreading.removeActionListener(this);
		cmbVerbose.removeActionListener(this);
		cmbTime.removeActionListener(this);
		txtTime.removeActionListener(this);
		txtPath.removeKeyListener(this);
		bnBrowse.removeActionListener(this);
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
	}

	@Override
	public void keyReleased(KeyEvent e) {
		update();
	}
}
