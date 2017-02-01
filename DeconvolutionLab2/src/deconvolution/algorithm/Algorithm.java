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

import java.util.ArrayList;

public class Algorithm {

	private static ArrayList<AbstractAlgorithmPanel> list;
	
	static {
		list = new ArrayList<AbstractAlgorithmPanel>();
		list.add(new RegularizedInverseFilterPanel());
		list.add(new TikhonovRegularizationInverseFilterPanel());
		list.add(new NaiveInverseFilterPanel());
		list.add(new FISTAPanel());
		list.add(new ISTAPanel());
		list.add(new LandweberPanel());
		list.add(new LandweberPositivityPanel());
		list.add(new RichardsonLucyPanel());
		list.add(new RichardsonLucyTVPanel());
		list.add(new TikhonovMillerPanel());
		list.add(new ICTMPanel());
		list.add(new VanCittertPanel());
		list.add(new IdentityPanel());
		list.add(new ConvolutionPanel());
		list.add(new SimulationPanel());
		list.add(new NonStabilizedDivisionPanel());		
	}
	
	public static ArrayList<AbstractAlgorithmPanel> getAvailableAlgorithms() {
		return list;
	}

	public static AbstractAlgorithm getDefaultAlgorithm() {
		return new Identity();
	}
	
	public static AbstractAlgorithm createAlgorithm(String name) {
		if (name == null)
			return getDefaultAlgorithm();
		String n = name.trim().toLowerCase();
		int i = 0;
		
		if (list.get(i++).isNamed(n))
			return new RegularizedInverseFilter(0.1);
		if (list.get(i++).isNamed(n))
			return new TikhonovRegularizationInverseFilter(1.0);
		if (list.get(i++).isNamed(n))
			return new NaiveInverseFilter();
		if (list.get(i++).isNamed(n))
			return new FISTA(10, 1, 1, "Haar", 3);
		if (list.get(i++).isNamed(n))
			return new ISTA(10, 1, 1, "Haar", 3);
		if (list.get(i++).isNamed(n))
			return new Landweber(10, 1);
		if (list.get(i++).isNamed(n))
			return new LandweberPositivity(10, 1);
		if (list.get(i++).isNamed(n))
			return new RichardsonLucy(10);
		if (list.get(i++).isNamed(n))
			return new RichardsonLucyTV(10, 1);
		if (list.get(i++).isNamed(n))
			return new TikhonovMiller(10, 1, 0.1);
		if (list.get(i++).isNamed(n))
			return new ICTM(10, 1, 0.1);
		if (list.get(i++).isNamed(n))
			return new VanCittert(10, 1);
		if (list.get(i++).isNamed(n))
			return new Identity();
		if (list.get(i++).isNamed(n))
			return new Convolution();
		if (list.get(i++).isNamed(n))
			return new Simulation(0, 1, 0);
		if (list.get(i++).isNamed(n))
			return new NonStabilizedDivision();
		return getDefaultAlgorithm();
	}

	public static AbstractAlgorithmPanel getPanel(String name) { 
		for (AbstractAlgorithmPanel panel : getAvailableAlgorithms()) {
			if (panel.getShortname().equals(name.trim()))
				return panel;
			if (panel.getName().equals(name.trim()))
				return panel;
			
		}
		return null;
	}

	public static ArrayList<String> getShortnames() { 
		ArrayList<String> list = new ArrayList<String>();
		for (AbstractAlgorithmPanel algo : getAvailableAlgorithms()) {
			list.add(algo.getShortname());
		}
		return list;
	}

	public static String getDocumentation(String name) {
		for (AbstractAlgorithmPanel algo : getAvailableAlgorithms()) {
			if (name.equals(algo.getName()))
				return algo.getDocumentation();
		}
		return "Unknown Algorithm";
	}
}
