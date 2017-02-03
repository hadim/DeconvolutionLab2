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

package deconvolutionlab;

import java.util.ArrayList;

import deconvolution.algorithm.Controller;
import deconvolutionlab.monitor.Monitors;
import signal.RealSignal;

public class OutputCollection {

	private ArrayList<Output> list = new ArrayList<Output>();

	public void setPath(String path) {
		for (Output out : list)
			out.setPath(path);
	}

	public void add(Output out) {
		if (out != null) {
			list.add(out);
		}
	}

	public boolean hasShow(int iterations) {
		boolean flag = false;
		for (Output out : list)
			flag = flag | out.is(iterations);
		return flag;
	}

	public void executeFinal(Monitors monitors, RealSignal signal, Controller controller) {
		for (Output out : list)
			if (out != null)
				out.execute(monitors, signal, controller, 0, false);
	}

	public void executeIterative(Monitors monitors, RealSignal signal, int iter, Controller controller) {
		for (Output out : list)
			if (out != null)
				out.execute(monitors, signal, controller, iter, true);
	}

	public ArrayList<String> getInformation() {
		ArrayList<String> lines = new ArrayList<String>();
		for (Output out : list) {
			if (out == null)
				lines.add("ERR>" + list.size());
			else
				lines.add("" + out.toString());
		}
		return lines;

	}

}
