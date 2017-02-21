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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import deconvolution.Command;
import deconvolution.algorithm.Algorithm;
import deconvolutionlab.Config;
import fft.FFT;
import lab.component.GridPanel;
import lab.tools.NumFormat;

public class FFTModule extends AbstractModule implements ActionListener, ChangeListener {

	private JComboBox<String>	cmbFFT;
	private JComboBox<String>	cmbType;
	private JComboBox<String>	cmbSep;
	private JComboBox<String>	cmbEpsilon;
	private JComboBox<String>	cmbNormalization;
	boolean init = false;
	
	public FFTModule(boolean expanded) {
		super("Computation", "", "", "Default", expanded);
	}

	@Override
	public String getCommand() {
		String cmd = "";
		if (cmbNormalization.getSelectedIndex() != 0)
			cmd += " -norm  " + NumFormat.parseNumber((String)cmbNormalization.getSelectedItem(), 1);
		if (cmbFFT.getSelectedIndex() != 0) {
			cmd += " -fft " + FFT.getLibraryByName((String) cmbFFT.getSelectedItem()).getLibraryName();
		}
		if (cmbEpsilon.getSelectedIndex() != 6)
			cmd += " -epsilon " + (String) cmbEpsilon.getSelectedItem();
		return cmd;
	}

	@Override
	public JPanel buildExpandedPanel() {
		cmbFFT = new JComboBox<String>(FFT.getLibrariesAsArray());
		cmbType = new JComboBox<String>(new String[] { "float" });
		cmbSep = new JComboBox<String>(new String[] { "XYZ" });
		cmbEpsilon = new JComboBox<String>(new String[] { "1E-0", "1E-1", "1E-2", "1E-3", "1E-4", "1E-5", "1E-6", "1E-7", "1E-8", "1E-9", "1E-10", "1E-11", "1E-12" });
		cmbEpsilon.setSelectedItem("1E-6");
		cmbNormalization = new JComboBox<String>(new String[] { "1", "10", "1000", "1E+6", "1E+9", "no" });

		cmbNormalization.addActionListener(this);
		cmbNormalization.setSelectedIndex(0);
		cmbNormalization.removeActionListener(this);
	
		GridPanel pnNumeric = new GridPanel(false, 3);
		pnNumeric.place(1, 0, "PSF Normalization");
		pnNumeric.place(1, 1, cmbNormalization);
		pnNumeric.place(1, 2, "1, recommended");
		pnNumeric.place(3, 0, new JLabel("FFT Library"));
		pnNumeric.place(3, 1, cmbFFT);
		pnNumeric.place(3, 2, new JLabel("Fourier"));
		pnNumeric.place(6, 0, new JLabel("FFT Processing"));
		pnNumeric.place(6, 1, cmbSep);
		pnNumeric.place(6, 2, new JLabel("Dimension Ordering"));
		
		pnNumeric.place(7, 0, new JLabel("Machine Epsilon"));
		pnNumeric.place(7, 1, cmbEpsilon);
		pnNumeric.place(7, 2, new JLabel("<html>&epsilon;</html>"));
	
		pnNumeric.place(8, 0, new JLabel("Data Type"));
		pnNumeric.place(8, 1, cmbType);
		pnNumeric.place(8, 2, new JLabel("(Only float)"));
	
		JScrollPane scroll = new JScrollPane(pnNumeric);
		scroll.setBorder(BorderFactory.createEmptyBorder());
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createEtchedBorder());
		panel.add(scroll, BorderLayout.CENTER);

		Config.register(getName(), "normalization", cmbNormalization, cmbNormalization.getItemAt(0));
		Config.register(getName(), "fft", cmbFFT, Algorithm.getDefaultAlgorithm());
		Config.register(getName(), "dim", cmbSep, "XYZ");
		Config.register(getName(), "epsilon", cmbEpsilon, "1E-6");
	
		cmbFFT.addActionListener(this);
		cmbType.addActionListener(this);
		cmbSep.addActionListener(this);
		cmbEpsilon.addActionListener(this);
		cmbNormalization.addActionListener(this);
		getAction1Button().addActionListener(this);
		init = true;
		return panel;
	}

	private void update() {
		setCommand(getCommand());
		if (init)
			setSynopsis("Norm " + cmbNormalization.getSelectedItem() + " " + FFT.getLibraryByName((String) cmbFFT.getSelectedItem()).getLibraryName());
		Command.command();
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		update();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
System.out.println("" + e);
		super.actionPerformed(e);
		if (e.getSource() == getAction1Button()) {
			cmbFFT.setSelectedIndex(0);
			cmbSep.setSelectedIndex(0);
			cmbType.setSelectedIndex(0);
			cmbEpsilon.setSelectedIndex(0);
			cmbNormalization.setSelectedIndex(0);
		}
		update();
	}

	@Override
	public void close() {
		getAction1Button().removeActionListener(this);
		cmbFFT.removeActionListener(this);
		cmbSep.removeActionListener(this);
		cmbType.removeActionListener(this);
		cmbEpsilon.removeActionListener(this);
		cmbNormalization.removeActionListener(this);
	}
}
