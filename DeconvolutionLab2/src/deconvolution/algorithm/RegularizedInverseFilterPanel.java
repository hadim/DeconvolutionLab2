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

package deconvolution.algorithm;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import deconvolution.Command;
import deconvolutionlab.Config;
import lab.component.GridPanel;
import lab.component.RegularizationPanel;
import lab.tools.NumFormat;

public class RegularizedInverseFilterPanel extends AbstractAlgorithmPanel implements KeyListener, ChangeListener {

	private RegularizationPanel			reg;
	private RegularizedInverseFilter	algo	= new RegularizedInverseFilter(0.1);

	@Override
	public JPanel getPanelParameters() {
		double[] params = algo.getDefaultParameters();
		reg = new RegularizationPanel(params[0]);
		GridPanel pn = new GridPanel(false);
		pn.place(0, 0, reg);
		Config.register("Algorithm." + algo.getShortname(), "reg", reg.getText(), "0.1");
		reg.getText().addKeyListener(this);
		reg.getSlider().addChangeListener(this);
		return pn;
	}

	@Override
	public String getCommand() {
		return NumFormat.nice(reg.getValue());
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		reg.getText().removeKeyListener(this);
		reg.updateFromSlider();
		Command.command();
		reg.getText().addKeyListener(this);
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
	}

	@Override
	public void keyReleased(KeyEvent e) {
		reg.getSlider().removeChangeListener(this);
		reg.updateFromText();
		Command.command();
		reg.getSlider().addChangeListener(this);
	}

	@Override
	public String getName() {
		return algo.getName();
	}

	@Override
	public String[] getShortname() {
		return new String[] {"RIF", "LRIF"};
	}

	@Override
	public String getDocumentation() {
		String s = "";
		s += "<h1>" + getName() + "</h1>";
		s += "<h2>Shortname RIF or LRIF</h2>";
		s += "<p>Laplacian Regularized Inverse Filter</p>";
		s += "<p>This is a inverse filter with a Laplacian regularization that tends to have an effect on high frequency</p>";
		s += "<p>It is very fast, non-iterative algorithm</p>";
		s += "<p>The regularization blurs the noise and the image. It is controlled by &lambda;</p>";
		return s;
	}

}
