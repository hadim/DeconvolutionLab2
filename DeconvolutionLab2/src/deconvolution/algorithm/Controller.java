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

import java.util.Timer;
import java.util.TimerTask;

import bilib.tools.NumFormat;
import deconvolution.Deconvolution;
import deconvolutionlab.OutputCollection;
import deconvolutionlab.TableStats;
import deconvolutionlab.monitor.Monitors;
import deconvolutionlab.system.SystemUsage;
import fft.AbstractFFT;
import fft.FFT;
import signal.Assessment;
import signal.ComplexSignal;
import signal.Constraint;
import signal.RealSignal;
import signal.SignalCollector;

/**
 * This is an important class to manage all the common task
 * of the algorithm. 
 * The method start() is called before at the starting of the
 * algorithm.
 * The method ends() is called at the end of every iterations
 * for the iterative algorithm. It returns true if one the
 * stopping criteria is true.
 * The method finish() is called when the algorithm is completely
 * terminated.
 * 
 * @author sage
 *
 */
public class Controller {

	private int					iterationsMax		= 100;
	private double				timeMax				= 1000;
	private double				residuMin			= -1;

	private boolean				doResidu			= false;
	private boolean				doTime				= false;
	private boolean				doReference			= false;
	private boolean				doConstraint		= false;
	private boolean				abort				= false;

	private double				timeStarting		= 0;
	private double				memoryStarting		= 0;
	private double				residu				= Double.MAX_VALUE;
	private int					iterations			= 0;
	private double				memoryPeak			= 0;
	private double				snr					= 0;
	private double				psnr				= 0;

	private Constraint.Mode		constraint			= Constraint.Mode.NO;
	private OutputCollection	outs				= null;
	private String				referenceName		= "";
	private RealSignal			refImage;
	private RealSignal			prevImage;
	private RealSignal			x;

	private Timer				timer;
	private AbstractFFT			fft;

	private TableStats			tableStats;
	private float[]				statsInput;
	
	private Monitors			monitors			= new Monitors();

	public Controller() {
		constraint = Constraint.Mode.NO;
		doResidu = false;
		doTime = false;
		doReference = false;
		doConstraint = false;
	}

	public void setTableStats(TableStats tableStats) {
		this.tableStats = tableStats;
	}

	public void setMonitors(Monitors monitors) {
		this.monitors = monitors;
	}

	public void setFFT(AbstractFFT fft) {
		this.fft = fft;
	}

	public void abort() {
		this.abort = true;
	}

	public int getIterationMax() {
		return iterationsMax;
	}

	public void setIterationMax(int iterationsMax) {
		this.iterationsMax = iterationsMax;
	}

	public void setTimeStop(double timeMax) {
		this.doTime = true;
		this.timeMax = timeMax * 1e9;
	}

	public void setResiduStop(double residuMin) {
		this.doResidu = true;
		this.residuMin = residuMin;
	}

	public void setReference(String referenceName) {
		this.doReference = true;
		this.referenceName = referenceName;
	}

	public void setConstraint(Constraint.Mode constraint) {
		this.doConstraint = true;
		this.constraint = constraint;
	}
	
	public void setOutputs(OutputCollection outs) {
		this.outs = outs;
	}

	public boolean needSpatialComputation() {
		return doConstraint || doResidu || doReference;
	}
	
	public void start(RealSignal x) {
		this.x = x;
		statsInput = x.getStats();
		if (tableStats != null)
			tableStats.nextStats(monitors, stats());
		iterations = 0;
		timer = new Timer();
		timer.schedule(new Updater(), 0, 100);
		timeStarting = System.nanoTime();
		memoryStarting = SystemUsage.getHeapUsed();

		if (doConstraint && x != null)
			Constraint.setModel(x);

		if (doReference) {
			refImage = new Deconvolution("Reference", "-image file " + referenceName).openImage();
			if (refImage == null)
				monitors.error("Impossible to load the reference image " + referenceName);
			else
				monitors.log("Reference image loaded");
		}

		if (outs != null)
			outs.executeStarting(monitors, x, this);
		this.prevImage = x;
	}

	public boolean ends(ComplexSignal X) {
		boolean out = outs == null ? false : outs.hasShow(iterations);

		if (doConstraint || doResidu || doReference || out) {
			if (fft == null)
				fft = FFT.createDefaultFFT(monitors, X.nx, X.ny, X.nz);
			x = fft.inverse(X, x);
			return ends(x);
		}

		return ends((RealSignal) null);
	}

	public boolean ends(RealSignal x) {
		this.x = x;

		if (doConstraint || doResidu || doReference)
			compute(iterations, x, doConstraint, doResidu, doReference);

		if (outs != null)
			outs.executeIterative(monitors, x, this, iterations);

		iterations++;
		double p = iterations * 100.0 / iterationsMax;
		monitors.progress("Iterative " + iterations + "/" + iterationsMax, p);
		double timeElapsed = getTimeNano();
		boolean stopIter = (iterations >= iterationsMax);
		boolean stopTime = doTime && (timeElapsed >= timeMax);
		boolean stopResd = doResidu && (residu <= residuMin);
		monitors.log("@" + iterations + " Time: " + NumFormat.seconds(timeElapsed));

		if (tableStats != null)
			tableStats.nextStats(monitors, stats());

		String prefix = "Stopped>> by ";
		if (abort)
			monitors.log(prefix + "abort");
		if (stopIter)
			monitors.log(prefix + "iteration " + iterations + " > " + iterationsMax);
		if (stopTime)
			monitors.log(prefix + "time " + timeElapsed + " > " + timeMax);
		if (stopResd)
			monitors.log(prefix + "residu " + NumFormat.nice(residu) + " < " + NumFormat.nice(residuMin));

		return abort | stopIter | stopTime | stopResd;
	}

	public void finish(RealSignal x) {
		this.x = x;

		boolean ref = doReference;
		boolean con = doConstraint;
		boolean res = doResidu;
		if (con || res || ref)
			compute(iterations, x, con, res, ref);

		if (tableStats != null)
			tableStats.lastStats(monitors, stats());

		if (outs != null)
			outs.executeFinal(monitors, x, this);

		monitors.log("Time: " + NumFormat.seconds(getTimeNano()) + " Peak:" + getMemoryAsString());
		if (timer != null)
			timer.cancel();
	}

	private void compute(int iterations, RealSignal x, boolean con, boolean res, boolean ref) {
		if (x == null)
			return;

		if (con && constraint != null)
			new Constraint(monitors).apply(x, constraint);

		if (ref && refImage != null) {
			String s = "";
			psnr = Assessment.psnr(x, refImage);
			snr = Assessment.snr(x, refImage);
			s += " PSNR: " + NumFormat.nice(psnr);
			s += " SNR: " + NumFormat.nice(snr);
			monitors.log("@" + iterations + " " + s);
		}

		residu = Double.MAX_VALUE;
		if (res && prevImage != null) {
			residu = Assessment.relativeResidu(x, prevImage);
			prevImage = x.duplicate();
			monitors.log("@" + iterations + " Residu: " + NumFormat.nice(residu));
		}
	}

	public String[] stats() {
		if (tableStats == null)
			return null;
		float params[] = null;
		if (x != null)
			params = x.getStats();
		String[] row = new String[12];
		row[0] = "" + iterations;
		row[1] = (params == null ? "-" : "" + params[0]);
		row[2] = (params == null ? "-" : "" + params[1]);
		row[3] = (params == null ? "-" : "" + params[2]);
		row[4] = (params == null ? "-" : "" + params[3]);
		row[5] = (params == null ? "-" : "" + params[5]);
		row[6] = NumFormat.seconds(getTimeNano());
		row[7] = NumFormat.bytes(SystemUsage.getHeapUsed());
		row[8] = SignalCollector.sumarize();
		row[9] = doReference ? NumFormat.nice(psnr) : "n/a";
		row[10] = doReference ? NumFormat.nice(snr) : "n/a";
		row[11] = doResidu ? NumFormat.nice(residu) : "n/a";
		return row;
	}

	public double getTimeNano() {
		return (System.nanoTime() - timeStarting);
	}

	public Constraint.Mode getConstraint() {
		return constraint;
	}

	public String getReference() {
		return doReference ? referenceName : "no ground-truth";
	}

	public String getConstraintAsString() {
		if (!doConstraint)
			return "no";
		if (constraint == null)
			return "null";
		return constraint.name().toLowerCase();
	}

	public String getStoppingCriteriaAsString(AbstractAlgorithm algo) {
		String stop = algo.isIterative() ? "iterations limit=" + getIterationMax() + ", " : "direct, ";
		stop += doTime ? ", time limit=" + NumFormat.nice(timeMax * 1e-9) : " no time limit" + ", ";
		stop += doResidu ? ", residu limit=" + NumFormat.nice(residuMin) : " no residu limit";
		return stop;
	}

	public float[] getStatsInput() {
		return statsInput;
	}

	public double getMemory() {
		return memoryPeak - memoryStarting;
	}

	public String getMemoryAsString() {
		return NumFormat.bytes(getMemory());
	}

	public int getIterations() {
		return iterations;
	}

	private void update() {
		memoryPeak = Math.max(memoryPeak, SystemUsage.getHeapUsed());
	}

	private class Updater extends TimerTask {
		@Override
		public void run() {
			update();
		}
	}
}
