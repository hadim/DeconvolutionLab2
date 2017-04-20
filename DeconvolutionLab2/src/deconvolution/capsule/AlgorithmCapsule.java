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

package deconvolution.capsule;

import javax.swing.JSplitPane;

import bilib.component.HTMLPane;
import bilib.table.CustomizedTable;
import bilib.tools.NumFormat;
import deconvolution.Deconvolution;
import deconvolution.Features;
import deconvolution.algorithm.AbstractAlgorithm;
import deconvolution.algorithm.AbstractAlgorithmPanel;
import deconvolution.algorithm.Algorithm;
import deconvolutionlab.monitor.Monitors;
import fft.AbstractFFT;
import fft.FFT;
import signal.ComplexSignal;
import signal.RealSignal;
import signal.SignalCollector;

/**
 * This class is a information module for the algorithm.
 * 
 * @author Daniel Sage
 *
 */
public class AlgorithmCapsule extends AbstractCapsule implements Runnable {

	private CustomizedTable	table;
	private HTMLPane		doc;

	public AlgorithmCapsule(Deconvolution deconvolution) {
		super(deconvolution);
		doc = new HTMLPane(100, 1000);
		table = new CustomizedTable(new String[] { "Features", "Values" }, false);
		split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, table.getPane(200, 200), doc.getPane());
	}

	public void update() {
		if (doc == null)
			return;
		if (table == null)
			return;
		
		table.removeRows();
		table.append(new String[] { "PSF", "Waiting for loading ..." });
		Thread thread = new Thread(this);
		thread.setPriority(Thread.MIN_PRIORITY);
		thread.start();
		split.setDividerLocation(300);
	}

	@Override
	public String getName() {
		return "Algorithm";
	}
	
	@Override
	public void run() {
		Features features = new Features();
		
		if (deconvolution.getAlgorithm() == null) {
			features.add("Algorithm", "No valid algorithm");
			return;
		}
		AbstractAlgorithm algo = deconvolution.getAlgorithm();
		doc.clear();
		String name = algo.getShortnames()[0];
		AbstractAlgorithmPanel algoPanel = Algorithm.getPanel(name);
		if (algoPanel != null)
			doc.append(algoPanel.getDocumentation());
		
		if (deconvolution.image == null) {
			startAsynchronousTimer("Open image", 200);
			deconvolution.image = deconvolution.openImage();
			stopAsynchronousTimer();
		}
		
		if (deconvolution.image == null) {
			features.add("Image", "No valid input image");
			return;
		}
		if (deconvolution.getController().getPadding() == null) {
			features.add("Padding", "No valid padding");
			return;
		}
		if (deconvolution.getController().getApodization() == null) {
			features.add("Apodization", "No valid apodization");
			return;
		}
		
		if (deconvolution.psf == null) {
			startAsynchronousTimer("Open PSF", 200);
			deconvolution.psf = deconvolution.openPSF();
			stopAsynchronousTimer();
		}
		
		if (deconvolution.psf == null) {
			features.add("Image", "No valid PSF");
			return;
		}

		startAsynchronousTimer("Run FFT", 200);
		
		AbstractFFT f = FFT.getFastestFFT().getDefaultFFT();
		double Q = Math.sqrt(2);
		if (deconvolution.image != null) {
			int mx = deconvolution.image.nx;
			int my = deconvolution.image.ny;
			int mz = deconvolution.image.nz;
			
			while (mx * my * mz > Math.pow(2, 15)) {
				mx = (int)(mx / Q);
				my = (int)(my / Q);
				mz = (int)(mz / Q);
			}
			double N = deconvolution.image.nx * deconvolution.image.ny * deconvolution.image.nz;
			double M = mx * my * mz;
			double ratio = 1;
			if (M != 0)
				ratio = (N * Math.log(N)) / (M * Math.log(M));
			
			double chrono = System.nanoTime(); 
			RealSignal x = new RealSignal("test", mx, my, mz);
			ComplexSignal c = new ComplexSignal("test", mx, my, mz);
			f.init(Monitors.createDefaultMonitor(), mx, my, mz);
			f.transform(x, c);
			SignalCollector.free(x);
			SignalCollector.free(c);
			
			chrono = (System.nanoTime() - chrono);
			features.add("Tested on", mx + "x" + my + "x" + mz);
			features.add("Estimated Time on small", NumFormat.time(chrono) );
			
			chrono = chrono * ratio * algo.getComplexityNumberofFFT();
			
			int n = algo.isIterative() ? algo.getIterationsMax() : 1;
			features.add("Estimated Time", NumFormat.time(chrono) );
			features.add("Estimated Number of FFT / Transform", ""+algo.getComplexityNumberofFFT());
		}
		else 
			features.add("Estimated Time", "Error" );
		double mem = (algo.getMemoryFootprintRatio() * deconvolution.image.nx * deconvolution.image.ny * deconvolution.image.nz * 4);
		features.add("Estimated Memory", NumFormat.bytes(mem));
		features.add("Iterative", algo.isIterative()  ? "" + algo.getIterationsMax() : "Direct");
		
		table.removeRows();
		for (String[] feature : features)
			table.append(feature);
		stopAsynchronousTimer();
	}

}