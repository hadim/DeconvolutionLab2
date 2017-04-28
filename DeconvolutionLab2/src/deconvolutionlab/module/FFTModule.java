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

package deconvolutionlab.module;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import bilib.component.HTMLPane;
import bilib.table.CustomizedColumn;
import bilib.table.CustomizedTable;
import deconvolution.algorithm.Algorithm;
import deconvolutionlab.Config;
import deconvolutionlab.Constants;
import fft.AbstractFFTLibrary;
import fft.FFT;

public class FFTModule extends AbstractModule implements ActionListener {

	private CustomizedTable	table;
	private JComboBox<String>	cmbFFT;
	private JComboBox<String>	cmbEpsilon;
	private HTMLPane			doc;
	private JComboBox<String>	cmbMultithreading;
	
	public FFTModule() {
		super("FFT", "", "Default", "");
	}

	@Override
	public String getCommand() {
		String cmd = " ";
		if (cmbFFT.getSelectedIndex() != 0)
			cmd += " -fft " + FFT.getLibraryByName((String) cmbFFT.getSelectedItem()).getLibraryName();
		if (cmbEpsilon.getSelectedIndex() != 6)
			cmd += " -epsilon " + (String) cmbEpsilon.getSelectedItem();
		if (cmbMultithreading.getSelectedIndex() != 0)
			cmd += " -multithreading no";
		return cmd;
	}

	public void update() {
		AbstractFFTLibrary library = FFT.getLibraryByName((String) cmbFFT.getSelectedItem());
		setCommand(getCommand());
		setSynopsis(library.getLibraryName());
		doc.clear();
		doc.append(library.getCredit());
		doc.append("<hr>");
		doc.append(library.getLicence());
	}

	@Override
	public JPanel buildExpandedPanel() {
		
		doc = new HTMLPane(100, 1000);
		cmbFFT = new JComboBox<String>(FFT.getLibrariesAsArray());
		cmbEpsilon = new JComboBox<String>(new String[] { "1E-0", "1E-1", "1E-2", "1E-3", "1E-4", "1E-5", "1E-6", "1E-7", "1E-8", "1E-9", "1E-10", "1E-11", "1E-12" });
		cmbEpsilon.setSelectedItem("1E-6");
		cmbMultithreading = new JComboBox<String>(new String[] { "yes", "no" });

		ArrayList<CustomizedColumn> columns = new ArrayList<CustomizedColumn>();
		columns.add(new CustomizedColumn("FFT Library", String.class, 100, false));
		columns.add(new CustomizedColumn("Installed", String.class, 40, false));
		columns.add(new CustomizedColumn("Installation", String.class, Constants.widthGUI, false));
		table = new CustomizedTable(columns, true);	
		table.setRowSelectionAllowed(false);
			
		JToolBar tool = new JToolBar("Path");
		tool.setBorder(BorderFactory.createEmptyBorder());
		tool.setLayout(new GridLayout(3, 3));
		tool.setFloatable(false);
		tool.add(new JLabel("fft"));
		tool.add(cmbFFT);
		tool.add(new JLabel("FFT Library"));
		
		tool.add(new JLabel("espilon"));
		tool.add(cmbEpsilon);
		tool.add(new JLabel("Machine epsilon"));
		
		tool.add(new JLabel("multithreading"));
		tool.add(cmbMultithreading);
		tool.add(new JLabel("Activate multithreading"));
		
		
		JPanel pn = new JPanel();
		pn.setLayout(new BorderLayout());
		pn.add(tool, BorderLayout.NORTH);
		pn.add(table.getMinimumPane(100, 100), BorderLayout.CENTER);

		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createEtchedBorder());
		panel.setLayout(new BorderLayout());
		panel.add(pn, BorderLayout.NORTH);
		panel.add(doc.getPane(), BorderLayout.CENTER);
		getAction1Button().addActionListener(this);

		Config.register(getName(), "fft", cmbFFT, Algorithm.getDefaultAlgorithm());
		Config.register(getName(), "epsilon", cmbEpsilon, "1E-6");
		Config.register(getName(), "multithreading", cmbMultithreading, cmbMultithreading.getItemAt(0));

		cmbMultithreading.addActionListener(this);
		cmbFFT.addActionListener(this);
		cmbEpsilon.addActionListener(this);

		for(AbstractFFTLibrary lib : FFT.getInstalledLibraries()) {
			String name = lib.getLibraryName();
				String i = lib.isInstalled() ? "yes" : "no";
			String loc = lib.getLocation();
			table.append(new String[]{name, i, loc});	
		}
		
		update();
		return panel;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		super.actionPerformed(e);
		if (e.getSource() == getAction1Button()) {
			cmbFFT.setSelectedIndex(0);;
			cmbEpsilon.setSelectedItem("1E-6");
			cmbMultithreading.setSelectedIndex(0);
		}
		update();
	}

	@Override
	public void close() {
		cmbFFT.removeActionListener(this);
		cmbEpsilon.removeActionListener(this);
		cmbMultithreading.removeActionListener(this);
		getAction1Button().removeActionListener(this);
	}
}
