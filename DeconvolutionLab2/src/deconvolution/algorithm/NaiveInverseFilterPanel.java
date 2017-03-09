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

import javax.swing.JPanel;

import bilib.component.GridPanel;

public class NaiveInverseFilterPanel extends AbstractAlgorithmPanel {

	private NaiveInverseFilter algo = new NaiveInverseFilter();

	@Override
	public JPanel getPanelParameters() {
		GridPanel pn = new GridPanel(false);
		pn.place(1, 0, "<html><span \"nowrap\">No parameters</span></html>");
		return pn;
	}

	@Override
	public String getCommand() {
		return "";
	}

	@Override
	public String getName() {
		return algo.getName();
	}

	@Override
	public String[] getShortname() {
		return new String[] {"NIF", "IF"};
	}

	@Override
	public String getDocumentation() {
		String s = "";
		s += "<h1>" + getName() + "</h1>";
		s += "<h3>Shortname: NIF or IF</p>";
		s += "<p>This is the classical inverse filter.</p>";
		s += "<p>This algorithm only performs a stabilized division in the Fourier domain.</p>";
		s += "<p>The stabilization is controlled by the machine epsilon parameter &Epsilon; set by default at 1E-6.</p>";
		return s;
	}

}
