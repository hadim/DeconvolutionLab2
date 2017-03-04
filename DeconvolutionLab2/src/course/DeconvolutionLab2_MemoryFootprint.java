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
package course;

import deconvolution.Deconvolution;
import ij.plugin.PlugIn;
import lab.component.CustomizedTable;
import lab.tools.NumFormat;
import signal.SignalCollector;

public class DeconvolutionLab2_MemoryFootprint implements PlugIn {

	public DeconvolutionLab2_MemoryFootprint() {

		CustomizedTable table = new CustomizedTable(new String[] { "Name", "Algo", "Optimized", "Time", "Energy", "Peak Count", "Peak Bytes", "End Count", "End Byte", "Ratio" }, true);
		table.show("Memory Footprint", 1100, 300);

		run(table, "CONV");
		run(table, "FISTA 10 1 1");
		run(table, "ICTM 10 1 0.1");
		run(table, "I");
		run(table, "ISTA 10 1 1");
		run(table, "LW 10 1");
		run(table, "LLS 10 1");
		run(table, "NLLS 10 1");
		run(table, "NIF");
		run(table, "DIV");
		run(table, "RIF 1");
		run(table, "RL 10");
		run(table, "RLTV 10 1");
		run(table, "SIM 1 1 1");
		run(table, "BVLS 10 1");
		run(table, "TM 10 1 0.1");
		run(table, "TRIF 1");
		run(table, "VC 10 1");
	}

	private void run(CustomizedTable table, String cmd) {
		int nx = 64;
		int ny = 32;
		int nz = 12;
		String size = " size " + nx + " " + ny + " " + nz;
		String image = " -image synthetic Cross 110 0 1 1 80.0 " + size;
		String psf = " -psf synthetic Double-Helix 100 0 3 10 10 " + size;

		for (int i = 0; i <= 1; i++) {
			SignalCollector.resetSignals();
			SignalCollector.clear();
			Deconvolution d = new Deconvolution("noise", " -algorithm " + cmd + psf + image + " -display no");
			boolean optimized = i == 1;
			d.getAlgo().setOptimizedMemoryFootprint(optimized);
			String n = d.getAlgo().getName();
			double chrono = System.nanoTime();
			d.run();
			String energy = "" + d.getOutput().getEnergy();
			String time = NumFormat.time(System.nanoTime() - chrono);
			int cp = SignalCollector.getCountPeakSignals();
			int cs = SignalCollector.getCountSignals();
			long bp = SignalCollector.getBytesPeakSignals();
			long bs = SignalCollector.getBytesSignals();
			double ratio = (bp + bs) / (nx * ny * nz * 4);
			table.append(new String[] { n, cmd, "" + optimized, time, energy, "" + cp, NumFormat.bytes(bp), "" + cs, NumFormat.bytes(bs), NumFormat.nice(ratio) });
		}
	}

	public static void main(String arg[]) {
		new DeconvolutionLab2_MemoryFootprint();
	}

	@Override
	public void run(String arg) {
		new DeconvolutionLab2_MemoryFootprint();
	}

}
