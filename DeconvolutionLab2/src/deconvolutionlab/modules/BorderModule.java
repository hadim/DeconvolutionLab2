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
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import lab.component.GridPanel;
import lab.component.HTMLPane;
import lab.component.SpinnerRangeInteger;
import lab.tools.NumFormat;
import signal.RealSignal;
import signal.apodization.AbstractApodization;
import signal.apodization.Apodization;
import signal.apodization.UniformApodization;
import signal.padding.AbstractPadding;
import signal.padding.NoPadding;
import signal.padding.Padding;
import deconvolution.Command;
import deconvolution.Deconvolution;
import deconvolutionlab.Config;

public class BorderModule extends AbstractModule implements ActionListener, ChangeListener {

	private JComboBox<String>	cmbNormalization;
	private JComboBox<String>	cmbPadXY;
	private JComboBox<String>	cmbPadZ;
	private JComboBox<String>	cmbApoXY;
	private JComboBox<String>	cmbApoZ;
	private SpinnerRangeInteger	spnExtensionXY;
	private SpinnerRangeInteger	spnExtensionZ;
	private HTMLPane	        info;

	private boolean build = false;
	
	public BorderModule(boolean expanded) {
		super("Border", "", "Test", "Default", expanded);
	}

	@Override
	public String getCommand() {
		AbstractPadding pxy = Padding.getByName((String) cmbPadXY.getSelectedItem());
		AbstractPadding paz = Padding.getByName((String) cmbPadZ.getSelectedItem());
		AbstractApodization axy = Apodization.getByName((String) cmbApoXY.getSelectedItem());
		AbstractApodization apz = Apodization.getByName((String) cmbApoZ.getSelectedItem());
		boolean ext = spnExtensionXY.get() + spnExtensionZ.get() > 0;
		String extXY = (ext ? "" + spnExtensionXY.get() : "") + " ";
		String extZ = ext ? "" + spnExtensionZ.get() : "";
		String cmd = "";
		if (!(pxy instanceof NoPadding) || !(paz instanceof NoPadding) || spnExtensionXY.get() > 0 || spnExtensionZ.get() > 0)
			cmd += " -pad " + pxy.getShortname() + " " + paz.getShortname() + " " + extXY + extZ;
		if (!(axy instanceof UniformApodization) || !(apz instanceof UniformApodization))
			cmd += " -apo " + axy.getShortname() + " " + apz.getShortname() + " ";
		if (cmbNormalization.getSelectedIndex() != 0)
			cmd += " -norm  " + NumFormat.parseNumber((String)cmbNormalization.getSelectedItem(), 1);
		return cmd;
	}

	@Override
	public JPanel buildExpandedPanel() {
		info = new HTMLPane(100, 100);

		cmbPadXY = new JComboBox<String>(Padding.getPaddingsAsArray());
		cmbPadZ = new JComboBox<String>(Padding.getPaddingsAsArray());
		cmbApoXY = new JComboBox<String>(Apodization.getApodizationsAsArray());
		cmbApoZ = new JComboBox<String>(Apodization.getApodizationsAsArray());
		spnExtensionXY = new SpinnerRangeInteger(0, 0, 99999, 1);
		spnExtensionZ = new SpinnerRangeInteger(0, 0, 99999, 1);
		cmbNormalization = new JComboBox<String>(new String[] { "1", "10", "1000", "1E+6", "1E+9", "no" });
	
		GridPanel pnBorder = new GridPanel("Border Artifact Cancelation", 3);
		pnBorder.place(0, 2, "Lateral (XY)");
		pnBorder.place(0, 4, "Axial (Z)");
		pnBorder.place(2, 0, "Apodization");
		pnBorder.place(2, 2, cmbApoXY);
		pnBorder.place(2, 4, cmbApoZ);

		pnBorder.place(3, 0, "Padding Extension");
		pnBorder.place(3, 2, spnExtensionXY);
		pnBorder.place(3, 4, spnExtensionZ);

		pnBorder.place(4, 0, "Padding Constraint");
		pnBorder.place(4, 2, cmbPadXY);
		pnBorder.place(4, 4, cmbPadZ);
		
		pnBorder.place(5, 0, "PSF Normalization");
		pnBorder.place(5, 2, cmbNormalization);
		pnBorder.place(5, 4, "1, recommended");
	
		JScrollPane scroll1 = new JScrollPane(pnBorder);
		scroll1.setBorder(BorderFactory.createEmptyBorder());
		scroll1.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroll1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

		JPanel control = new JPanel();
		control.setLayout(new BoxLayout(control, BoxLayout.PAGE_AXIS));
		Border b1 = BorderFactory.createEtchedBorder();
		Border b2 = BorderFactory.createEmptyBorder(10, 10, 10, 10);
		control.setBorder(BorderFactory.createCompoundBorder(b1, b2));
		control.add(scroll1);
		control.add(info.getPane());

		JPanel panel = new JPanel(new BorderLayout());
		panel.add(control, BorderLayout.CENTER);
	
		Config.register(getName(), "padxy", cmbPadXY, Padding.getDefault().getName());
		Config.register(getName(), "padz", cmbPadZ, Padding.getDefault().getName());
		Config.register(getName(), "apoxy", cmbApoXY, Apodization.getDefault().getName());
		Config.register(getName(), "apoz", cmbApoZ, Apodization.getDefault().getName());
		Config.register(getName(), "extxy", spnExtensionXY, "0");
		Config.register(getName(), "extz", spnExtensionZ, "0");
		Config.register(getName(), "normalization", cmbNormalization, cmbNormalization.getItemAt(0));

		cmbNormalization.addActionListener(this);
		spnExtensionXY.addChangeListener(this);
		spnExtensionZ.addChangeListener(this);
		cmbPadXY.addActionListener(this);
		cmbPadZ.addActionListener(this);
		cmbApoXY.addActionListener(this);
		cmbApoZ.addActionListener(this);

		getAction1Button().addActionListener(this);
		getAction2Button().addActionListener(this);
		build = true;
		return panel;
	}

	private void update() {
		setCommand(getCommand());
		boolean ext = spnExtensionXY.get() + spnExtensionZ.get() > 0;
		boolean pad = cmbPadXY.getSelectedIndex() + cmbPadZ.getSelectedIndex() > 0;
		boolean apo = cmbApoXY.getSelectedIndex() + cmbApoZ.getSelectedIndex() > 0;
		if (pad || apo || ext) {
			setSynopsis("" + " " + (pad ? "Padding" : "") + " " + (ext ? "Extension" : "") + " " + (apo ? "Apodization" : ""));
		}
		else {
			setSynopsis("Default options");
		}

		Command.command();
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		testInfo();
		update();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		super.actionPerformed(e);
		if (e.getSource() == getAction2Button()) {
			cmbPadXY.removeActionListener(this);
			cmbPadZ.removeActionListener(this);
			cmbApoXY.removeActionListener(this);
			cmbApoZ.removeActionListener(this);
			cmbPadXY.setSelectedIndex(0);
			cmbPadZ.setSelectedIndex(0);
			cmbApoXY.setSelectedIndex(0);
			cmbApoZ.setSelectedIndex(0);
			spnExtensionXY.set(0);
			spnExtensionZ.set(0);
			cmbNormalization.setSelectedIndex(0);
			cmbPadXY.addActionListener(this);
			cmbPadZ.addActionListener(this);
			cmbApoXY.addActionListener(this);
			cmbApoZ.addActionListener(this);
			update();
			return;
		}
		if (e.getSource() == getAction1Button()) {
			Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					getAction1Button().setEnabled(false);
					getAction2Button().setEnabled(false);
					info.clear();
					info.append("p", "Test running...");
					info.clear();
					ArrayList<String> lines = new Deconvolution(Command.command()).checkImage();
					for (String line : lines)
						info.append("p", line);
					ArrayList<String> linesPSF = new Deconvolution(Command.command()).checkPSF();
					for (String line : linesPSF)
						info.append("p", line);
					getAction1Button().setEnabled(true);
					getAction2Button().setEnabled(true);
				}
			});
			thread.setPriority(Thread.MIN_PRIORITY);
			thread.start();
			return;
		}
		testInfo();
		update();
	}

	@Override
	public void close() {
		cmbNormalization.removeActionListener(this);
		cmbPadXY.removeActionListener(this);
		cmbPadZ.removeActionListener(this);
		cmbApoXY.removeActionListener(this);
		cmbApoZ.removeActionListener(this);
		getAction1Button().removeActionListener(this);
		getAction2Button().removeActionListener(this);
		spnExtensionXY.removeChangeListener(this);
		spnExtensionZ.removeChangeListener(this);
	}

	private void testInfo() {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				if (build)
					info();
			}
		});
		thread.setPriority(Thread.MIN_PRIORITY);
		thread.start();
	}

	private Padding getPadding() {
		AbstractPadding padXY = Padding.getByName((String) cmbPadXY.getSelectedItem());
		AbstractPadding padZ = Padding.getByName((String) cmbPadZ.getSelectedItem());
		int extXY = spnExtensionXY.get();
		int extZ = spnExtensionZ.get();
		return new Padding(padXY, padXY, padZ, extXY, extXY, extZ);
	}

	private Apodization getApodization() {
		AbstractApodization apoXY = Apodization.getByName((String) cmbApoXY.getSelectedItem());
		AbstractApodization apoZ = Apodization.getByName((String) cmbApoZ.getSelectedItem());
		return new Apodization(apoXY, apoXY, apoZ);
	}

	private void info() {
		int nx = 600;
		int ny = 400;
		int nz = 100;
		RealSignal image = new Deconvolution(Command.command()).openImage();
		if (image != null) {
			nx = image.nx;
			ny = image.ny;
			nz = image.nz;
		}
		try {
			info.clear();
		}
		catch (Exception ex) {
		}
		int[] p = getPadding().pad(nx, ny, nz);
		String in = "[" + nx + " x" + ny + " x " + nz + "]";
		String out = "[" + p[0] + " x" + p[1] + " x " + p[2] + "]";
		String bin = NumFormat.bytes(nx * ny * nz * 4);
		String pout = NumFormat.bytes(p[0] * p[1] * p[2] * 4);
		if (info != null) {
			info.append("p", "Input size: " + in + " (" + bin + ")");
			info.append("p", "Padded size: " + out + " (" + pout + ")");
			double lost = getApodization().estimateLostEnergy(10);
			info.append("p", "Estimation of lost energy by apodization: " + (lost * 100) + "%");
		}
	}
}
