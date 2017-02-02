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
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import lab.component.CustomizedColumn;
import lab.component.CustomizedTable;
import lab.component.GridPanel;
import lab.component.HTMLPane;
import lab.tools.NumFormat;
import signal.Signal;
import deconvolution.Command;
import deconvolution.Deconvolution;
import deconvolution.algorithm.Algorithm;
import deconvolutionlab.Config;
import deconvolutionlab.Constants;
import fft.AbstractFFTLibrary;
import fft.FFT;

public class FFTModule extends AbstractModule implements ActionListener, ChangeListener {

	private HTMLPane			info;

	private JComboBox<String>	cmbFFT;
	private JComboBox<String>	cmbType;
	private JComboBox<String>	cmbSep;
	private JComboBox<String>	cmbEpsilon;

	private CustomizedTable		table;

	public FFTModule(boolean expanded) {
		super("Fourier", "", "", "Default", expanded);
	}

	@Override
	public String getCommand() {
		String cmd = "";
		if (cmbFFT.getSelectedIndex() != 0)
			cmd += " -fft " + (String) cmbFFT.getSelectedItem();
		return cmd;
	}

	@Override
	public JPanel buildExpandedPanel() {

		ArrayList<CustomizedColumn> columns = new ArrayList<CustomizedColumn>();
		columns.add(new CustomizedColumn("Name", String.class, 120, false));
		columns.add(new CustomizedColumn("Installed", String.class, 120, false));
		columns.add(new CustomizedColumn("Location", String.class, Constants.widthGUI, false));
		table = new CustomizedTable(columns, true);
		table.setRowSelectionAllowed(false);

		info = new HTMLPane(100, 100);
		cmbFFT = new JComboBox<String>(FFT.getLibrariesAsArray());
		cmbType = new JComboBox<String>(new String[] { "float" });
		cmbSep = new JComboBox<String>(new String[] { "XYZ" });
		cmbEpsilon = new JComboBox<String>(new String[] { "1E-0", "1E-1", "1E-2", "1E-3", "1E-4", "1E-5", "1E-6", "1E-7", "1E-8", "1E-9", "1E-10", "1E-11", "1E-12" });
		cmbEpsilon.setSelectedItem("1E-6");

		GridPanel pnNumeric = new GridPanel(false, 3);
		pnNumeric.place(3, 0, new JLabel("FFT Fourier Library"));
		pnNumeric.place(3, 1, cmbFFT);
		pnNumeric.place(6, 0, new JLabel("FFT Dimension"));
		pnNumeric.place(6, 1, cmbSep);
		pnNumeric.place(7, 0, new JLabel("<html>Machine Epsilon &epsilon;</html>"));
		pnNumeric.place(7, 1, cmbEpsilon);
		pnNumeric.place(8, 0, new JLabel("Data Type"));
		pnNumeric.place(8, 1, cmbType);

		JScrollPane scroll2 = new JScrollPane(pnNumeric);
		scroll2.setBorder(BorderFactory.createEmptyBorder());
		scroll2.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroll2.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

		JPanel control = new JPanel(new BorderLayout());
		Border b1 = BorderFactory.createEtchedBorder();
		Border b2 = BorderFactory.createEmptyBorder(10, 10, 10, 10);

		control.setBorder(BorderFactory.createCompoundBorder(b1, b2));
		control.add(scroll2, BorderLayout.NORTH);
		control.add(info.getPane(), BorderLayout.CENTER);
		control.add(table.getPane(80, 80), BorderLayout.SOUTH);

		JPanel panel = new JPanel(new BorderLayout());
		panel.add(control, BorderLayout.CENTER);

		Config.register(getName(), "epsilon", cmbEpsilon, "1E-6");
		Config.register(getName(), "fft", cmbFFT, Algorithm.getDefaultAlgorithm());
		Config.register(getName(), "dim", cmbSep, "XYZ");

		cmbFFT.addActionListener(this);
		cmbType.addActionListener(this);
		cmbEpsilon.addActionListener(this);

		getAction1Button().addActionListener(this);
		getAction2Button().addActionListener(this);
		fillInstallation();
		return panel;
	}

	private void fillInstallation() {

		ArrayList<AbstractFFTLibrary> libs = FFT.getLibraries();
		for (AbstractFFTLibrary lib : libs) {
			String name = lib.getLibraryName();
			String installed = lib.isInstalled() ? " Yes" : "No";
			String location = lib.getLocation();
			table.append(new String[] { name, installed, location });
		}
		AbstractFFTLibrary fftlib = FFT.getLibraryByName((String) cmbFFT.getSelectedItem());
		info.clear();
		info.append("p", fftlib.getLicence());
	}

	private void update() {
		setCommand(getCommand());
		Signal.epsilon = NumFormat.parseNumber((String) cmbEpsilon.getSelectedItem(), 1e-6);
		Command.command();
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		update();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		super.actionPerformed(e);
		if (e.getSource() == cmbFFT || e.getSource() == cmbSep) {
			AbstractFFTLibrary fftlib = FFT.getLibraryByName((String) cmbFFT.getSelectedItem());
			info.clear();
			info.append("p", fftlib.getLicence());
		}

		if (e.getSource() == getAction1Button()) {
			cmbFFT.removeActionListener(this);
			cmbType.removeActionListener(this);
			cmbEpsilon.removeActionListener(this);
			cmbFFT.setSelectedIndex(0);
			cmbType.setSelectedIndex(0);
			cmbEpsilon.setSelectedIndex(0);
			cmbFFT.addActionListener(this);
			cmbType.addActionListener(this);
			cmbEpsilon.addActionListener(this);
		}
		update();
	}

	@Override
	public void close() {
		getAction1Button().removeActionListener(this);
		getAction2Button().removeActionListener(this);
		cmbFFT.removeActionListener(this);
		cmbType.removeActionListener(this);
		cmbEpsilon.removeActionListener(this);
	}
}
