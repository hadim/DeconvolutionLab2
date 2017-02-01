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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import lab.component.GridPanel;
import lab.component.SpinnerRangeInteger;
import lab.tools.Files;
import signal.Constraint;
import deconvolution.Command;
import deconvolution.Deconvolution;
import deconvolutionlab.Config;

public class ControllerModule extends AbstractModule implements ActionListener, ChangeListener, KeyListener {

	private JButton				bnBrowse;

	private JTextField			txtReference;
	private JTextField			txtResidu;
	private JTextField			txtSaveStats;
	private JTextField			txtShowStats;
	private JComboBox<String>	cmbConstraint;

	private JCheckBox			chkResidu;
	private JCheckBox			chkReference;
	private JCheckBox			chkConstraint;
	private JCheckBox			chkSaveStats;
	private JCheckBox			chkShowStats;

	private SpinnerRangeInteger	snapshotResidu;
	private SpinnerRangeInteger	snapshotConstraint;
	private SpinnerRangeInteger	snapshotReference;
	private SpinnerRangeInteger	snapshotSaveStats;
	private SpinnerRangeInteger	snapshotShowStats;

	public ControllerModule(boolean expanded) {
		super("Controller", "", "Default", "", expanded);
	}

	@Override
	public String getCommand() {
		String cmd = "";
		int sr = snapshotResidu.get();
		int sc = snapshotConstraint.get();
		int sg = snapshotReference.get();
		int ss = snapshotSaveStats.get();
		int sd = snapshotShowStats.get();
		
		if (chkConstraint.isSelected())
			cmd += "-constraint " +  (sc > 1 ? "@" + sc + " ": "") + cmbConstraint.getSelectedItem() + " ";
		if (chkReference.isSelected())
			cmd += "-reference " +  (sg > 1 ? "@" + sg + " " : "") +  txtReference.getText() + " ";
		if (chkResidu.isSelected())
			cmd += "-residu " +  (sr > 1 ? "@" + sr + " " : "") + txtResidu.getText() + " ";
		if (chkSaveStats.isSelected())
			cmd += "-savestats " +  (ss > 1 ? "@" + ss + " " : "") + txtSaveStats.getText() + " ";
		if (chkShowStats.isSelected())
			cmd += "-showstats " +  (sd > 1 ? "@" + sd + " " : "") + txtShowStats.getText() + " ";
		return cmd;
	}

	@Override
	public JPanel buildExpandedPanel() {

		snapshotResidu	= new SpinnerRangeInteger(10, 1, 99999, 1, "###");
		snapshotConstraint	= new SpinnerRangeInteger(10, 1, 99999, 1, "###");
		snapshotReference	= new SpinnerRangeInteger(10, 1, 99999, 1, "###");
		snapshotSaveStats	= new SpinnerRangeInteger(10, 1, 99999, 1, "###");
		snapshotShowStats	= new SpinnerRangeInteger(10, 1, 99999, 1, "###");
		chkConstraint = new JCheckBox("Constraint");
		chkResidu = new JCheckBox("Residu Minimun");
		chkReference = new JCheckBox("Reference");
		chkSaveStats = new JCheckBox("Save Stats");
		chkShowStats = new JCheckBox("Show Stats");

		bnBrowse = new JButton("Browse");
		txtReference = new JTextField("");
		txtResidu = new JTextField("0.01");
		txtSaveStats = new JTextField("stats");
		txtShowStats = new JTextField("stats");
		
		cmbConstraint = new JComboBox<String>(Constraint.getContraintsAsArray());
		txtReference.setPreferredSize(new Dimension(200, 20));

		GridPanel pn = new GridPanel(true);
	
		pn.place(1, 0, chkResidu);
		pn.place(1, 1, txtResidu);
		pn.place(1, 2, snapshotResidu);

		pn.place(4, 0, chkConstraint);
		pn.place(4, 1, cmbConstraint);
		pn.place(4, 2, snapshotConstraint);

		pn.place(5, 0, chkSaveStats);
		pn.place(5, 1, txtSaveStats);
		pn.place(5, 2, snapshotSaveStats);

		pn.place(6, 0, chkShowStats);
		pn.place(6, 1, txtShowStats);
		pn.place(6, 2, snapshotShowStats);

		pn.place(7, 0, chkReference);
		pn.place(7, 1, txtReference);
		pn.place(7, 2, snapshotReference);
		pn.place(8, 0, "Ground-truth file");
		pn.place(8, 1, bnBrowse);

		Border b1 = BorderFactory.createEtchedBorder();
		Border b2 = BorderFactory.createEmptyBorder(10, 10, 10, 10);
		pn.setBorder(BorderFactory.createCompoundBorder(b1, b2));
		
		JScrollPane scroll = new JScrollPane(pn);
		scroll.setBorder(BorderFactory.createEmptyBorder());
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createEtchedBorder());
		panel.add(scroll, BorderLayout.NORTH);

		bnBrowse.addActionListener(this);
		
		chkResidu.addChangeListener(this);
		chkReference.addChangeListener(this);
		chkConstraint.addChangeListener(this);
		chkSaveStats.addChangeListener(this);
		chkShowStats.addChangeListener(this);
		
		snapshotResidu.addChangeListener(this);
		snapshotConstraint.addChangeListener(this);
		snapshotReference.addChangeListener(this);
		snapshotShowStats.addChangeListener(this);
		snapshotSaveStats.addChangeListener(this);

		txtResidu.addKeyListener(this);
		txtReference.addKeyListener(this);
		txtSaveStats.addKeyListener(this);
		txtShowStats.addKeyListener(this);
		cmbConstraint.addActionListener(this);
		getAction1Button().addActionListener(this);
		
		Config.register(getName(), "residu.enable", chkResidu, false);
		Config.register(getName(), "reference.enable", chkReference, false);
		Config.register(getName(), "constraint.enable", chkConstraint, false);
		Config.register(getName(), "showstats.enable", chkShowStats, false);
		Config.register(getName(), "savestats.enable", chkSaveStats, false);
		
		Config.register(getName(), "reference.value", txtReference, "");
		Config.register(getName(), "residu.value", txtResidu, "0.01");
		Config.register(getName(), "showstats.value", txtShowStats, "Stats");
		Config.register(getName(), "savestats.value", txtSaveStats, "Stats");
		Config.register(getName(), "constraint.value", cmbConstraint, "No");
		
		Config.register(getName(), "residu.snapshot", snapshotResidu, "1");
		Config.register(getName(), "reference.snapshot", snapshotConstraint, "1");
		Config.register(getName(), "constraint.snapshot", snapshotReference, "1");
		Config.register(getName(), "showstats.snapshot", snapshotShowStats, "1");
		Config.register(getName(), "savestats.snapshot", snapshotSaveStats, "1");
		return panel;
	}

	private void update() {
		setCommand(getCommand());
		int count = 0;
		count += (chkResidu.isSelected() ? 1 : 0);
		count += (chkReference.isSelected() ? 1 : 0);
		count += (chkConstraint.isSelected() ? 1 : 0);
		count += (chkSaveStats.isSelected() ? 1 : 0);
		count += (chkShowStats.isSelected() ? 1 : 0);
		setSynopsis("" + count + (count >= 2 ? " controls" : " control "));
		Command.command();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		super.actionPerformed(e);
		if (e.getSource() == bnBrowse) {
			Deconvolution deconvolution = new Deconvolution(Command.command());		
			File file = Files.browseFile(deconvolution.getPath());
			if (file != null)
				txtReference.setText(file.getAbsolutePath());
		}
		if (e.getSource() == getAction1Button()) {
			chkResidu.removeChangeListener(this);
			chkReference.removeChangeListener(this);
			chkConstraint.removeChangeListener(this);
			chkSaveStats.removeChangeListener(this);
			chkShowStats.removeChangeListener(this);
			
			snapshotResidu.removeChangeListener(this);
			snapshotConstraint.removeChangeListener(this);
			snapshotReference.removeChangeListener(this);
			snapshotShowStats.removeChangeListener(this);
			snapshotSaveStats.removeChangeListener(this);
			
			chkResidu.setSelected(false);
			chkReference.setSelected(false);
			chkConstraint.setSelected(false);
			chkShowStats.setSelected(false);
			chkSaveStats.setSelected(false);
			
			txtReference.setText("");
			txtResidu.setText("0.01");
			txtShowStats.setText("Stats");
			txtSaveStats.setText("Stats");
			cmbConstraint.setSelectedIndex(0);
			
			snapshotResidu.set(1);
			snapshotConstraint.set(1);
			snapshotReference.set(1);
			snapshotShowStats.set(1);
			snapshotSaveStats.set(1);

				chkResidu.addChangeListener(this);
			chkReference.addChangeListener(this);
			chkConstraint.addChangeListener(this);
			chkSaveStats.addChangeListener(this);
			chkShowStats.addChangeListener(this);
			
			snapshotResidu.addChangeListener(this);
			snapshotConstraint.addChangeListener(this);
			snapshotReference.addChangeListener(this);
			snapshotShowStats.addChangeListener(this);
			snapshotSaveStats.addChangeListener(this);

		}
		update();
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		update();
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
	
	@Override
	public void close() {
		bnBrowse.removeActionListener(this);
		chkReference.removeChangeListener(this);
		chkResidu.removeChangeListener(this);
			chkConstraint.removeChangeListener(this);
		chkShowStats.removeChangeListener(this);
		chkSaveStats.removeChangeListener(this);
		getAction1Button().removeChangeListener(this);
	}


}
