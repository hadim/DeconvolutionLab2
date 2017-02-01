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

package lab.component;

public class CustomizedColumn {
	public Class<?>	classe;
	public String	header;
	public int		width;
	public boolean	editable;
	public String[]	choices;	// Combobox
	public String	button;		// Button
	public String	tooltip;	

	public CustomizedColumn(String header, Class<?> classe, int width, boolean editable) {
		this.classe = classe;
		this.header = header;
		this.width = width;
		this.editable = editable;
	}

	public CustomizedColumn(String header, Class<?> classe, int width, String[] choices, String tooltip) {
		this.classe = classe;
		this.header = header;
		this.width = width;
		this.editable = true;
		this.choices = choices;
		this.tooltip = tooltip;
	}

	public CustomizedColumn(String header, Class<?> classe, int width, String button, String tooltip) {
		this.classe = classe;
		this.header = header;
		this.width = width;
		this.editable = false;
		this.button = button;
		this.tooltip = tooltip;
	}
}
