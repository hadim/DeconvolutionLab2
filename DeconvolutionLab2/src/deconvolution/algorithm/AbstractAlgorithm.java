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

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import bilib.tools.NumFormat;
import deconvolutionlab.monitor.Monitors;
import fft.AbstractFFT;
import fft.AbstractFFTLibrary;
import fft.FFT;
import signal.Operations;
import signal.RealSignal;
import signal.SignalCollector;
import signal.apodization.Apodization;
import signal.padding.Padding;

/**
 * This class is the common part of every algorithm of deconvolution.
 * 
 * @author Daniel Sage
 * 
 */
public abstract class AbstractAlgorithm implements Callable<RealSignal> {

	/** y is the input signal of the deconvolution. */
	protected RealSignal			y;

	/** h is the PSF signal for the deconvolution. */
	protected RealSignal			h;

	/**
	 * The controller takes the control of the algorithm, in particular to do
	 * some operations at the starting, at every iteration and at the end.
	 */
	protected Controller			controller;

	/** This is the FFT used. */
	protected AbstractFFT			fft	;

	protected Monitors				monitors;
	protected AbstractFFTLibrary	fftlib;
	protected boolean				threaded;

	/** Optimized implementation in term of memory footprint */
	protected boolean				optimizedMemoryFootprint;

	public AbstractAlgorithm() {
		this.controller = new Controller();
		monitors = Monitors.createDefaultMonitor();
		fftlib = FFT.getFastestFFT();
		optimizedMemoryFootprint = true;
		threaded = true;
	}

	public void setOptimizedMemoryFootprint(boolean optimizedMemoryFootprint) {
		this.optimizedMemoryFootprint = optimizedMemoryFootprint;
	}

	public abstract String getName();
	public abstract String[] getShortnames();

	public abstract double getMemoryFootprintRatio();

	public abstract int getComplexityNumberofFFT();

	public abstract boolean isRegularized();

	public abstract boolean isStepControllable();

	public abstract boolean isIterative();

	public abstract boolean isWaveletsBased();

	public abstract void setParameters(double[] params);

	public abstract double getRegularizationFactor();

	public abstract double getStepFactor();

	public abstract double[] getParameters();

	public abstract double[] getDefaultParameters();

	public RealSignal run(RealSignal image, RealSignal psf) {
		return run(image, psf, new Padding(), new Apodization(), 1);
	}

	public RealSignal run(RealSignal image, RealSignal psf, Padding pad, Apodization apo) {
		return run(image, psf, pad, apo, 1);
	}

	public RealSignal run(RealSignal image, RealSignal psf, Padding pad, Apodization apo, double norm) {

		if (image == null)
			return null;
		if (psf == null)
			return null;

		if (fftlib != null)
			fft = FFT.createFFT(monitors, fftlib, image.nx, image.ny, image.nz);
		else
			fft = FFT.createDefaultFFT(monitors, image.nx, image.ny, image.nz);
		controller.setFFT(fft);

		y = pad.pad(monitors, image);
		y.setName("y");
		apo.apodize(monitors, y);
		monitors.log("Input: " + y.dimAsString());

		h = psf.changeSizeAs(y);
		h.setName("h");
		h.normalize(norm);
		monitors.log("PSF: " + h.dimAsString() + " normalized " + (norm <= 0 ? "no" : norm));

		String iterations = (isIterative() ? controller.getIterationMax() + " iterations" : "direct");
		monitors.log(getShortnames()[0] + " is starting (" + iterations + ")");
		controller.setMonitors(monitors);
		controller.start(y);

		h.circular();
		if (fft == null)
			fft = FFT.createDefaultFFT(monitors, y.nx, y.ny, y.nz);
		else
			fft.init(monitors, y.nx, y.ny, y.nz);
		monitors.log(getShortnames()[0] + " data ready");

		double params[] = getParameters();
		if (params != null) {
			if (params.length > 0) {
				String s = " ";
				for (double param : params)
					s += "" + param + " ";
				monitors.log(getShortnames()[0] + s);
			}
		}
		RealSignal x = null;

		try {
			if (threaded == true) {
				ExecutorService pool = Executors.newSingleThreadExecutor();
				Future<RealSignal> future = pool.submit(this);
				x = future.get();
			}
			else {
				x = call();
			}
		}
		catch (InterruptedException ex) {
			ex.printStackTrace();
			x = y.duplicate();
		}
		catch (ExecutionException ex) {
			ex.printStackTrace();
			x = y.duplicate();
		}
		catch (Exception e) {
			e.printStackTrace();
			x = y.duplicate();
		}
		x.setName("x");
		controller.finish(x);
		monitors.log(getName() + " is finished");
		SignalCollector.free(y);
		SignalCollector.free(h);

		RealSignal result = pad.crop(monitors, x);
		SignalCollector.free(x);
		result.setName("Output of " + this.getShortnames()[0]);
		return result;
	}

	public Monitors getMonitors() {
		return monitors;
	}

	public void setMonitors(Monitors monitors) {
		this.monitors = monitors;
	}

	public AbstractFFT getFFT() {
		return fft;
	}

	public void setFFT(AbstractFFT fft) {
		this.fft = fft;
	}

	public void setFFT(AbstractFFTLibrary fftlib) {
		this.fftlib = fftlib;
	}

	public Controller getController() {
		return controller;
	}

	public void setController(Controller controller) {
		this.controller = controller;
	}

	public int getIterations() {
		return controller.getIterations();
	}

	public double getTime() {
		return controller.getTimeNano();
	}

	public double getMemory() {
		return controller.getMemory();
	}

	public void setWavelets(String waveletsName) {
	}

	@Override
	public String toString() {
		String s = "";
		s += getName();
		s += (isIterative() ? ", " + controller.getIterationMax() + " iterations" : " (direct)");
		s += (isRegularized() ? ", &lambda=" + NumFormat.nice(getRegularizationFactor()) : "");
		s += (isStepControllable() ? ", &gamma=" + NumFormat.nice(getStepFactor()) : "");
		return s;
	}

	public String getParametersAsString() {
		double p[] = getParameters();
		String param = "";
		for (int i = 0; i < p.length; i++)
			if (i == p.length - 1)
				param += p[i];
			else
				param += p[i] + ", ";
		return param;
	}
}
