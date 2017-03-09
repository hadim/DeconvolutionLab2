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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import bilib.component.GridPanel;
import bilib.component.RegularizationPanel;
import bilib.component.SpinnerRangeDouble;
import bilib.component.SpinnerRangeInteger;
import bilib.tools.NumFormat;
import deconvolution.Command;
import deconvolutionlab.Config;
import deconvolutionlab.Constants;

public class TikhonovMillerPanel extends AbstractAlgorithmPanel implements KeyListener, ActionListener, ChangeListener {

	private SpinnerRangeInteger	spnIter	= new SpinnerRangeInteger(10, 1, 99999, 1, "###");
	private SpinnerRangeDouble	spnStep	= new SpinnerRangeDouble(1, 0, 2, 0.1, "#.#");
	private RegularizationPanel	reg;

	private TikhonovMiller		algo	= new TikhonovMiller(10, 1, 0.1);

	@Override
	public JPanel getPanelParameters() {
		double[] params = algo.getDefaultParameters();
		spnIter.setPreferredSize(Constants.dimParameters);
		spnStep.setPreferredSize(Constants.dimParameters);
		reg = new RegularizationPanel(params[2]);

		GridPanel pn = new GridPanel(false);
		pn.place(1, 0, "<html><span \"nowrap\"><b>Iterations</b></span></html>");
		pn.place(1, 1, "<html><span \"nowrap\"><i>N</i></span></html>");
		pn.place(1, 2, spnIter);
		pn.place(1, 3, "<html><span \"nowrap\">Step <i>&gamma;</i></span></html>");
		pn.place(1, 4, spnStep);
		pn.place(2, 0, 5, 1, reg);

		Config.register("Algorithm." + algo.getShortname(), "iterations", spnIter, params[0]);
		Config.register("Algorithm." + algo.getShortname(), "step", spnStep, params[1]);
		Config.register("Algorithm." + algo.getShortname(), "reg", reg.getText(), "0.1");
		reg.getText().addKeyListener(this);
		reg.getSlider().addChangeListener(this);
		spnIter.addChangeListener(this);
		spnStep.addChangeListener(this);
		return pn;
	}

	@Override
	public String getCommand() {
		double gamma = spnStep.get();
		double lambda = reg.getValue();
		return spnIter.get() + " " + NumFormat.nice(gamma) + " " + NumFormat.nice(lambda);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Command.command();
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
		return new String[] {"TM"};
	}

	@Override
	public String getDocumentation() {
		String s = "";
		s += "<h1>" + getName() + "</h1>";
		s += "<p>Iterative: " + algo.isIterative() + "</p>";
		s += "<p>Step controllable: " + algo.isStepControllable() + "</p>";
		s += "<p>Regularization: " + algo.isRegularized() + "</p>";
		s += "<p>Wavelet-base: " + algo.isWaveletsBased() + "</p>";
		s += "<p>Shortname: " + getShortname() + "</p>";
		return s;
	}
}
