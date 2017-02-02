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

import lab.tools.NumFormat;
import deconvolutionlab.monitor.Monitors;
import fft.AbstractFFT;
import fft.FFT;
import signal.Operations;
import signal.RealSignal;

/**
 * This class is the common part of every algorithm of deconvolution.
 * 
 * @author Daniel Sage
 * 
 */
public abstract class AbstractAlgorithm implements Callable<RealSignal> {

	protected RealSignal	y			= null;
	protected RealSignal	h			= null;
	protected RealSignal	reference	= null;

	protected Controller	controller	= null;
	protected AbstractFFT	fft			= null;

	public AbstractAlgorithm() {
		this.controller = new Controller();
	}

	public abstract String getName();

	public abstract String getShortname();

	public abstract boolean isRegularized();

	public abstract boolean isStepControllable();

	public abstract boolean isIterative();

	public abstract boolean isWaveletsBased();

	public abstract void setParameters(double[] params);

	public abstract double getRegularizationFactor();

	public abstract double getStepFactor();

	public abstract double[] getParameters();

	public abstract double[] getDefaultParameters();

	public RealSignal run(Monitors monitors, RealSignal image, RealSignal psf, boolean threaded) {

		String iterations = (isIterative() ? controller.getIterationMax() + " iterations" : "direct");
		monitors.log(getShortname() + " is starting (" + iterations + ")");
		y = image.duplicate();
		h = psf.duplicate();
		controller.setMonitors(monitors);
		controller.start(y);
		h = Operations.circularShift(h);
		if (fft == null)
			fft = FFT.createDefaultFFT(monitors, y.nx, y.ny, y.nz);
		else
			fft.init(monitors, y.nx, y.ny, y.nz);
		monitors.log(getShortname() + " data ready");

		double params[] = getParameters();
		if (params != null) {
			if (params.length > 0) {
				String s = " ";
				for (double param : params)
					s += "" + param + " ";
				monitors.log(getShortname() + s);
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

		controller.finish(x);
		monitors.log(getName() + " is finished");
		return x;
	}

	public AbstractFFT getFFT() {
		return fft;
	}

	public void setFFT(AbstractFFT fft) {
		this.fft = fft;
	}

	public Controller getController() {
		return controller;
	}

	public void setController(Controller controller) {
		this.controller = controller;
		controller.setAlgorithm(getName());
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
}
