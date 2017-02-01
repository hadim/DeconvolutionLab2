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

import lab.system.SystemUsage;
import lab.tools.NumFormat;
import signal.Assessment;
import signal.ComplexSignal;
import signal.Constraint;
import signal.RealSignal;
import signal.Signal;
import deconvolution.Deconvolution;
import deconvolutionlab.Lab;
import deconvolutionlab.OutputCollection;
import deconvolutionlab.monitor.Monitors;
import fft.AbstractFFT;
import fft.FFT;

public class Controller {

	private int	             iterationsMax	    = 100;
	private double	         timeMax	        = 1000;
	private double	         residuMin	        = -1;

	private boolean	         doResidu	        = false;
	private boolean	         doTime	            = false;
	private boolean	         doReference	    = false;
	private boolean	         doConstraint	    = false;
	private boolean	         doShowStats	    = false;
	private boolean	         doSaveStats	    = false;
	private boolean	         abort	            = false;

	private int	             snapshotResidu	    = 0;
	private int	             snapshotReference	= 0;
	private int	             snapshotConstraint	= 0;
	private int	             snapshotSaveStats	= 0;
	private int	             snapshotShowStats	= 0;

	private double	         timeStarting	    = 0;
	private double	         memoryStarting	    = 0;
	private double	         residu	            = Double.MAX_VALUE;
	private int	             iterations	        = 0;
	private double	         memoryPeak	        = 0;
	private double	         snr	            = 0;
	private double	         psnr	            = 0;

	private Constraint.Mode	 constraint	        = Constraint.Mode.NO;
	private OutputCollection	outs	        = null;
	private String	         referenceName	    = "";
	private String	         showstatsName	    = "";
	private String	         savestatsName	    = "";
	private RealSignal	     refImage;
	private RealSignal	     prevImage;
	private RealSignal	     x;

	private Timer	         timer;
	private AbstractFFT	     fft;

	private String	         algo	            = "";
	private float	         statsInput[];

	private Monitors	     monitors	        = new Monitors();

	public Controller() {
		constraint = Constraint.Mode.NO;
		doResidu = false;
		doTime = false;
		doReference = false;
		doConstraint = false;
		doShowStats = false;
		doSaveStats = false;
	}

	public void setMonitors(Monitors monitors) {
		this.monitors = monitors;
	}

	public void setAlgorithm(String algo) {
		this.algo = algo;
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

	public void setResiduStop(int snapshot, double residuMin) {
		this.doResidu = true;
		this.snapshotResidu = snapshot;
		this.residuMin = residuMin;
	}

	public void setReference(int snapshot, String referenceName) {
		this.doReference = true;
		this.snapshotReference = snapshot;
		this.referenceName = referenceName;
	}

	public void setConstraint(int snapshot, Constraint.Mode constraint) {
		this.doConstraint = true;
		this.snapshotConstraint = snapshot;
		this.constraint = constraint;
	}

	public void setSaveStats(int snapshot, String name) {
		this.doSaveStats = true;
		this.snapshotSaveStats = snapshot;
		this.savestatsName = name;
	}

	public void setShowStats(int snapshot, String name) {
		this.doShowStats = true;
		this.snapshotShowStats = snapshot;
		this.showstatsName = name;
	}

	public void setOutputs(OutputCollection outs) {
		this.outs = outs;
	}

	public void start(RealSignal x) {
		this.x = x;
		statsInput = x.getStats();
		iterations = 0;
		timer = new Timer();
		timer.schedule(new Updater(), 0, 100);
		timeStarting = System.nanoTime();
		memoryStarting = SystemUsage.getHeapUsed();
		Signal.bytes = 0;

		if (doConstraint && x != null)
			Constraint.setModel(x);

		if (doReference && snapshotReference >= 1) {
			refImage = new Deconvolution("-image file " + referenceName).openImage();
			if (refImage == null)
				monitors.error("Impossible to load the reference image " + referenceName);
			else
				monitors.log("Reference image loaded");
		}

		if (doShowStats || doSaveStats)
			if (monitors != null)
				Lab.firstStats(monitors, showstatsName, this, doShowStats, doSaveStats);
		this.prevImage = x;
	}

	public boolean ends(ComplexSignal X) {
		boolean res = doResidu && snapshotResidu >= 1 ? (iterations % snapshotResidu == 0) : false;
		boolean con = doConstraint && snapshotConstraint >= 1 ? (iterations % snapshotConstraint == 0) : false;
		boolean ref = doReference && snapshotReference >= 1 ? (iterations % snapshotReference == 0) : false;
		boolean sav = doSaveStats && snapshotSaveStats >= 1 ? (iterations % snapshotSaveStats == 0) : false;
		boolean shw = doShowStats && snapshotShowStats >= 1 ? (iterations % snapshotShowStats == 0) : false;
		boolean out = outs == null ? false : outs.hasShow(iterations);

		if (con || res || ref || sav || shw || out) {
			if (fft == null)
				fft = FFT.createDefaultFFT(monitors, X.nx, X.ny, X.nz);
			x = new RealSignal(X.nx, X.ny, X.nz, false);
			fft.inverse(X, x);
			return ends(x);
		}

		return ends((RealSignal) null);
	}

	public boolean ends(RealSignal x) {
		this.x = x;
		boolean res = doResidu && snapshotResidu >= 1 ? (iterations % snapshotResidu == 0) : false;
		boolean con = doConstraint && snapshotConstraint >= 1 ? (iterations % snapshotConstraint == 0) : false;
		boolean ref = doReference && snapshotReference >= 1 ? (iterations % snapshotReference == 0) : false;
		boolean sav = doSaveStats && snapshotSaveStats >= 1 ? (iterations % snapshotSaveStats == 0) : false;
		boolean shw = doShowStats && snapshotShowStats >= 1 ? (iterations % snapshotShowStats == 0) : false;

		if (con || res || ref)
			compute(iterations, x, con, res, ref);

		if (sav || shw)
			Lab.nextStats(monitors, showstatsName, this, sav, shw);

		if (outs != null)
			outs.executeIterative(monitors, x, this);

		iterations++;
		double p = iterations * 100.0 / iterationsMax;
		monitors.progress("Iterative " + iterations + "/" + iterationsMax, p);
		double timeElapsed = getTimeNano();
		boolean stopIter = (iterations >= iterationsMax);
		boolean stopTime = doTime && (timeElapsed >= timeMax);
		boolean stopResd = doResidu && (residu <= residuMin);
		monitors.log("@" + iterations + " Time: " + NumFormat.seconds(timeElapsed) + " Memory: " + NumFormat.bytes(Signal.bytes));

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

		if (doShowStats || doSaveStats)
			Lab.lastStats(monitors, savestatsName, this, doShowStats, doSaveStats);

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

	public String[] stats(String name) {
		float params[] = null;
		if (x != null)
			params = x.getStats();
		String[] row = new String[15];
		row[0] = name;
		row[1] = algo;
		row[2] = "" + iterations;
		row[3] = (params == null ? "-" : "" + params[0]);
		row[4] = (params == null ? "-" : "" + params[1]);
		row[5] = (params == null ? "-" : "" + params[2]);
		row[6] = (params == null ? "-" : "" + params[3]);
		row[7] = (params == null ? "-" : "" + params[4]);
		row[8] = (params == null ? "-" : "" + params[5]);
		row[9] = NumFormat.seconds(getTimeNano());
		row[10] = NumFormat.bytes(SystemUsage.getHeapUsed());
		row[11] = NumFormat.bytes(memoryPeak);
		row[12] = doReference ? NumFormat.nice(psnr) : "n/a";
		row[13] = doReference ? NumFormat.nice(snr) : "n/a";
		row[14] = doResidu ? NumFormat.nice(residu) : "n/a";
		return row;
	}

	public double getTimeNano() {
		return (System.nanoTime() - timeStarting);
	}

	public Constraint.Mode getConstraint() {
		return constraint;
	}

	public String getConstraintAsString() {
		if (!doConstraint)
			return "no";
		if (constraint == Constraint.Mode.NO)
			return "no";
		else
			return constraint.name().toLowerCase();
	}

	public String getReference() {
		return doReference ? referenceName : "no ground-truth";
	}

	public String getShowStats() {
		return doShowStats ? showstatsName : "no stats";
	}

	public String getSaveStats() {
		return doSaveStats ? savestatsName : "no stats";
	}

	public String getStoppingCriteria(AbstractAlgorithm algo) {
		String stop = algo.isIterative() ? " iterations limit=" + getIterationMax() + " " : ", ";
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