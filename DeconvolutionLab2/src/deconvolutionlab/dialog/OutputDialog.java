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

package deconvolutionlab.dialog;

import ij.gui.GUI;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import lab.component.GridPanel;
import lab.component.SpinnerRangeInteger;
import deconvolutionlab.Output;
import deconvolutionlab.PlatformImager;
import deconvolutionlab.Output.Dynamic;
import deconvolutionlab.Output.View;

public class OutputDialog extends JDialog implements ActionListener, ChangeListener {

	private JComboBox<String>	cmbDynamic	= new JComboBox<String>(new String[] { "intact", "rescaled", "normalized", "clipped" });
	private JComboBox<String>	cmbType		= new JComboBox<String>(new String[] { "float", "short", "byte" });

	private JCheckBox			chkSave		= new JCheckBox("Save output", true);
	private JCheckBox			chkShow		= new JCheckBox("Show output", true);

	private SpinnerRangeInteger	snpSnapshot	= new SpinnerRangeInteger(0, 0, 99999, 1);
	private JComboBox<String>	cmbSnapshot	= new JComboBox<String>(new String[] { "Final Output", "Specify Iterations..." });

	private SpinnerRangeInteger	spnX		= new SpinnerRangeInteger(128, 0, 99999, 1);
	private SpinnerRangeInteger	spnY		= new SpinnerRangeInteger(128, 0, 99999, 1);
	private SpinnerRangeInteger	spnZ		= new SpinnerRangeInteger(32, 0, 99999, 1);
	private JTextField			txtName		= new JTextField("Noname", 18);

	private JCheckBox			chkCenter	= new JCheckBox("Center of the volume", true);
	private JButton				bnOK		= new JButton("OK");
	private JButton				bnCancel	= new JButton("Cancel");
	private boolean				cancel		= false;
	private JLabel				lblBit		= new JLabel("32-bit");
	private JLabel				lblIter		= new JLabel("iterations");
	private JLabel				lblSnapshot	= new JLabel("Snapshot");
	private Output				out;
	private View				view;
	private GridPanel			pnOrtho;

	public OutputDialog(View view) {
		super(new JFrame(), "Output");
		this.view = view;
		lblBit.setBorder(BorderFactory.createEtchedBorder());
		lblIter.setBorder(BorderFactory.createEtchedBorder());

		GridPanel pn = new GridPanel(view.name());
		pn.place(0, 0, "Name");
		pn.place(0, 1, 2, 1, txtName);
	
		pn.place(1, 0, "Dynamic");
		pn.place(1, 1, cmbDynamic);
		pn.place(2, 0, "Type");
		pn.place(2, 1, cmbType);
		pn.place(2, 2, lblBit);
		
		if (view != View.SERIES && view != View.STACK) {
			pn.place(4, 0,  "Snapshot");
			pn.place(4, 1,  cmbSnapshot);
			pn.place(5, 0, lblSnapshot);
			pn.place(5, 1, snpSnapshot);
			pn.place(5, 2, lblIter);
		}
		pn.place(6, 0, 3, 1, chkShow);
		pn.place(7, 0, 3, 1, chkSave);

		GridPanel main = new GridPanel(false);
		main.place(1, 0, 2, 1, pn);

		if (view == View.ORTHO || view == View.FIGURE) {
			pn.place(7, 0, 3, 1, chkCenter);
			pnOrtho = new GridPanel("Keypoint");
			pnOrtho.place(4, 0, "Position in X");
			pnOrtho.place(4, 1, spnX);
			pnOrtho.place(4, 2, "[pixel]");

			pnOrtho.place(5, 0, "Position in Y");
			pnOrtho.place(5, 1, spnY);
			pnOrtho.place(5, 2, "[pixel]");

			pnOrtho.place(6, 0, "Position in Z");
			pnOrtho.place(6, 1, spnZ);
			pnOrtho.place(5, 2, "[pixel]");
			main.place(2, 0, 2, 1, pnOrtho);
		}

		main.place(3, 0, bnCancel);
		main.place(3, 1, bnOK);

		cmbSnapshot.addActionListener(this);
		snpSnapshot.addChangeListener(this);
		chkCenter.addActionListener(this);
		cmbType.addActionListener(this);
		bnOK.addActionListener(this);
		bnCancel.addActionListener(this);
		add(main);
		update();
		pack();
		GUI.center(this);
		setModal(true);
		setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == chkCenter) {
			update();
		}
		else if (e.getSource() == cmbSnapshot) {
			update();
		}
		else if (e.getSource() == cmbType) {
			if (cmbType.getSelectedIndex() == 0)
				lblBit.setText("32-bits");
			if (cmbType.getSelectedIndex() == 1)
				lblBit.setText("16-bits");
			if (cmbType.getSelectedIndex() == 2)
				lblBit.setText("8-bits");
		}
		else if (e.getSource() == bnCancel) {
			dispose();
			cancel = true;
			return;
		}
		else if (e.getSource() == bnOK) {
			int freq = snpSnapshot.get();
			Dynamic dynamic = Output.Dynamic.values()[cmbDynamic.getSelectedIndex()];
			PlatformImager.Type type = PlatformImager.Type.values()[cmbType.getSelectedIndex()];
			boolean show = chkShow.isSelected();
			boolean save = chkSave.isSelected();
			String name = txtName.getText();
			if (chkCenter.isSelected()) {
				out = new Output(view, show, save, freq, name, dynamic, type, true);
			}
			else {
				int px = spnX.get();
				int py = spnY.get();
				int pz = spnZ.get();
				out = new Output(view, show, save, freq, name, dynamic, type, px, py, pz);
			}
			dispose();
			cancel = false;
		}
	}

	private void update() {
		
		if (cmbSnapshot.getSelectedIndex() == 0) {
			snpSnapshot.set(0);
			lblSnapshot.setEnabled(false);
			lblIter.setEnabled(false);
			lblSnapshot.setEnabled(false);
		}
		else {
			lblSnapshot.setEnabled(true);
			lblIter.setEnabled(true);
			lblSnapshot.setEnabled(true);
		}
		if (snpSnapshot.get() == 0)
			lblIter.setText("at the end (default)");
		else
			lblIter.setText("every " + snpSnapshot.get() + " iterations");

		if (snpSnapshot.get() == 0)
			lblIter.setText("at the end (default)");
		else
			lblIter.setText("every " + snpSnapshot.get() + " iterations");

		boolean b = !chkCenter.isSelected();
		if (pnOrtho != null) {
			pnOrtho.setEnabled(b);
			for (Component c : pnOrtho.getComponents())
				c.setEnabled(b);
		}
		pack();
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == snpSnapshot)
			update();
	}

	public Output getOut() {
		return out;
	}

	public boolean wasCancel() {
		return cancel;
	}

}
