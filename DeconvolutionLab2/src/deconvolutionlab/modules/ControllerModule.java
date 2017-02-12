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

import deconvolution.Command;
import deconvolution.Deconvolution;
import deconvolutionlab.Config;
import lab.component.GridPanel;
import lab.tools.Files;
import signal.Constraint;

public class ControllerModule extends AbstractModule implements ActionListener, ChangeListener, KeyListener {

	private JButton				bnBrowse;

	private JTextField			txtReference;
	private JTextField			txtResidu;
	private JTextField			txtTime;
	private JTextField			txtIterations;

	private JComboBox<String>	cmbConstraint;

	private JCheckBox			chkResidu;
	private JCheckBox			chkReference;
	private JCheckBox			chkConstraint;
	private JCheckBox			chkTime;
	private JCheckBox			chkItermax;

	public ControllerModule(boolean expanded) {
		super("Controller", "", "Default", "", expanded);
	}

	@Override
	public String getCommand() {
		String cmd = "";
		if (chkConstraint.isSelected())
			cmd += "-constraint " + cmbConstraint.getSelectedItem() + " ";
		if (chkReference.isSelected())
			cmd += "-reference " +  txtReference.getText() + " ";
		if (chkResidu.isSelected())
			cmd += "-residu " + txtResidu.getText() + " ";
		if (chkTime.isSelected())
			cmd += "-time " + txtTime.getText() + " ";
		return cmd;
	}

	@Override
	public JPanel buildExpandedPanel() {

		chkTime = new JCheckBox("Time Limitation (s)");
		chkItermax = new JCheckBox("Early Stopping");
		chkConstraint = new JCheckBox("Constraint");
		chkResidu = new JCheckBox("Residu Minimun");
		chkReference = new JCheckBox("Reference");

		bnBrowse = new JButton("Browse");
		txtReference = new JTextField("");
		txtResidu = new JTextField("0.01");
		txtTime = new JTextField("3600");
		txtIterations = new JTextField("Iteration max (mandatory)");
		txtIterations.setEditable(false);
			
		cmbConstraint = new JComboBox<String>(Constraint.getContraintsAsArray());
		txtReference.setPreferredSize(new Dimension(200, 20));

		GridPanel pn = new GridPanel(true);

		pn.place(0, 0, chkItermax);
		pn.place(0, 1, txtIterations);
		pn.place(1, 0, chkResidu);
		pn.place(1, 1, txtResidu);
		pn.place(4, 0, chkConstraint);
		pn.place(4, 1, cmbConstraint);
		pn.place(5, 0, chkTime);
		pn.place(5, 1, txtTime);
		pn.place(7, 0, chkReference);
		pn.place(7, 1, txtReference);
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

			
		Config.register(getName(), "residu.enable", chkResidu, false);
		Config.register(getName(), "reference.enable", chkReference, false);
		Config.register(getName(), "constraint.enable", chkConstraint, false);
		Config.register(getName(), "time.enable", chkTime, false);
		Config.register(getName(), "itmax.enable", chkItermax, true);
		
		Config.register(getName(), "reference.value", txtReference, "");
		Config.register(getName(), "residu.value", txtResidu, "0.01");
		Config.register(getName(), "time.value", txtTime, "3600");
		Config.register(getName(), "constraint.value", cmbConstraint, "No");
		
		chkItermax.setSelected(true);

		bnBrowse.addActionListener(this);
		chkResidu.addChangeListener(this);
		chkReference.addChangeListener(this);
		chkConstraint.addChangeListener(this);
		chkTime.addChangeListener(this);
		chkItermax.addChangeListener(this);

		txtResidu.addKeyListener(this);
		txtReference.addKeyListener(this);
		txtTime.addKeyListener(this);
		cmbConstraint.addActionListener(this);
		getAction1Button().addActionListener(this);

		return panel;
	}

	private void update() {
		chkItermax.setSelected(true);
		setCommand(getCommand());
		int count = 0;
		count += (chkResidu.isSelected() ? 1 : 0);
		count += (chkConstraint.isSelected() ? 1 : 0);
		count += (chkTime.isSelected() ? 1 : 0);
		count += (chkItermax.isSelected() ? 1 : 0);
		setSynopsis("" + count + " stopping criteria");
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
			chkTime.removeChangeListener(this);
			chkItermax.removeChangeListener(this);
			
			chkResidu.setSelected(false);
			chkReference.setSelected(false);
			chkConstraint.setSelected(false);
			chkTime.setSelected(false);
			chkItermax.setSelected(true);
			
			txtReference.setText("");
			txtResidu.setText("0.01");
			txtTime.setText("3600");
			cmbConstraint.setSelectedIndex(0);
			
	
			chkResidu.addChangeListener(this);
			chkReference.addChangeListener(this);
			chkConstraint.addChangeListener(this);
			chkTime.addChangeListener(this);
			chkItermax.addChangeListener(this);

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
		chkItermax.removeChangeListener(this);
		chkTime.removeChangeListener(this);
		getAction1Button().removeChangeListener(this);
	}


}
