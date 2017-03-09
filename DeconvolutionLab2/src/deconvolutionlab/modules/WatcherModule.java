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
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

import bilib.component.GridPanel;
import bilib.tools.Files;
import deconvolution.Command;
import deconvolutionlab.Config;

public class WatcherModule extends AbstractModule implements ActionListener, KeyListener {

	private JComboBox<String>	cmbVerbose;
	private JComboBox<String>	cmbMonitor;
	private JComboBox<String>	cmbDisplay;
	private JComboBox<String>	cmbMultithreading;
	private JComboBox<String>	cmbPath;
	private JTextField			txtPath;
	private JButton				bnBrowse;

	public WatcherModule(boolean expanded) {
		super("Path & Watcher", "", "Default", "", expanded);
	}

	@Override
	public String getCommand() {
		String cmd = "";
		if (cmbPath.getSelectedIndex() != 0)
			cmd += " -path " + txtPath.getText();
		if (cmbMultithreading.getSelectedIndex() != 0)
			cmd += " -" + (String)cmbMultithreading.getSelectedItem();
		if (cmbDisplay.getSelectedIndex() != 0)
			cmd += " -" + (String)cmbDisplay.getSelectedItem();
		if (cmbMonitor.getSelectedIndex() != 0) 
			cmd += " -" + (String)cmbMonitor.getSelectedItem();
		if (cmbVerbose.getSelectedIndex() != 0) 
			cmd += " -" + (String)cmbVerbose.getSelectedItem();
		return cmd;
	}

	@Override
	public JPanel buildExpandedPanel() {

		cmbVerbose = new JComboBox<String>(new String[] { "verbose log ", "verbose quiet ", "verbose prolix", "verbose mute" });
		cmbDisplay = new JComboBox<String>(new String[] { "display final", "display no"});
		cmbMonitor = new JComboBox<String>(new String[] { "monitor full", "monitor console", "monitor none"});
		cmbMultithreading = new JComboBox<String>(new String[] {  "multithreading enabled", "multithreading disabled"});
		cmbPath = new JComboBox<String>(new String[] { "Current", "Specify"});
		txtPath = new JTextField("", 30);
		bnBrowse = new JButton("Browse");
		
		GridPanel pn1 = new GridPanel(true, 3);
		pn1.place(0, 0, 2, 1, "Working directory");
		pn1.place(1, 0, cmbPath);
		pn1.place(1, 1, bnBrowse);
		pn1.place(2, 0, 2, 1, txtPath);

		GridPanel pn2 = new GridPanel(true, 3);
		pn2.place(2, 0, cmbVerbose);
		pn2.place(2, 1, cmbMonitor);
		pn2.place(3, 0, cmbDisplay);
		pn2.place(3, 1, cmbMultithreading);
		
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.add(pn1);
		panel.add(pn2);
		String dir = System.getProperty("user.dir");

		Config.register(getName(), "verbose", cmbVerbose, cmbVerbose.getItemAt(0));
		Config.register(getName(), "monitor", cmbMonitor, cmbMonitor.getItemAt(0));
		Config.register(getName(), "display", cmbDisplay, cmbDisplay.getItemAt(0));
		Config.register(getName(), "multithreading", cmbMultithreading, cmbMultithreading.getItemAt(0));
		Config.register(getName(), "current", cmbPath, cmbPath.getItemAt(0));
		Config.register(getName(), "path", txtPath, dir);
		
		cmbPath.addActionListener(this);
		txtPath.addKeyListener(this);
		cmbVerbose.addActionListener(this);
		cmbDisplay.addActionListener(this);
		cmbMultithreading.addActionListener(this);
		cmbMonitor.addActionListener(this);
		bnBrowse.addActionListener(this);
		return panel;
	}

	private void update() {
		
		setCommand(getCommand());
		if (cmbPath.getSelectedIndex() == 0) {
			txtPath.setText(System.getProperty("user.dir"));
			txtPath.setEnabled(false);
			bnBrowse.setEnabled(false);
		}
		else {
			txtPath.setEnabled(true);
			bnBrowse.setEnabled(true);
		}
		setSynopsis(txtPath.getText());
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
		else if (e.getSource() == cmbPath) {
			if (cmbPath.getSelectedIndex() == 0) {
				File f = new File(System.getProperty("user.dir"));
				txtPath.setText(f.getAbsolutePath());
			}
		}
		update();
	}

	@Override
	public void close() {
		cmbVerbose.removeActionListener(this);
		cmbDisplay.removeActionListener(this);
		cmbMultithreading.removeActionListener(this);
		cmbMonitor.removeActionListener(this);
		cmbPath.removeActionListener(this);
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
