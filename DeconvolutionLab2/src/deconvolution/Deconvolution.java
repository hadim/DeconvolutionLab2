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

package deconvolution;

import java.io.File;
import java.util.ArrayList;

import bilib.tools.NumFormat;
import deconvolution.algorithm.AbstractAlgorithm;
import deconvolution.algorithm.Controller;
import deconvolutionlab.Constants;
import deconvolutionlab.Lab;
import deconvolutionlab.Output;
import deconvolutionlab.OutputCollection;
import deconvolutionlab.TableStats;
import deconvolutionlab.monitor.ConsoleMonitor;
import deconvolutionlab.monitor.Monitors;
import deconvolutionlab.monitor.StatusMonitor;
import deconvolutionlab.monitor.TableMonitor;
import deconvolutionlab.monitor.Verbose;
import deconvolutionlab.system.SystemInfo;
import fft.AbstractFFTLibrary;
import signal.RealSignal;
import signal.SignalCollector;
import signal.apodization.Apodization;
import signal.padding.Padding;

public class Deconvolution implements Runnable {

	public enum Finish {
		DIE, ALIVE, KILL
	};

	public AbstractAlgorithm					algo				= null;

	private String								path;
	public double								norm				= 1.0;
	public Padding								pad					= new Padding();
	public Apodization							apo					= new Apodization();
	private OutputCollection					outs;
	private Verbose								verbose				= Verbose.Log;
	private AbstractFFTLibrary					fft;
	private int									flagMonitor			= 3;
	private int									flagStats			= 0;
	private boolean								flagDisplay			= true;
	private boolean								flagMultithreading	= true;
	private boolean								flagSystem			= true;

	public Monitors								monitors			= new Monitors();

	private String								command				= "";
	private boolean								live				= false;

	private Features							report				= new Features();

	private String								name				= "";

	private ArrayList<DeconvolutionListener>	listeners			= new ArrayList<DeconvolutionListener>();	
	public RealSignal							image;
	public RealSignal							psf;
	private RealSignal							deconvolvedImage;

	private Finish								finish				= Finish.DIE;
	private DeconvolutionDialog					dialog;
	
	private TableStats tableStats;
	
	public Deconvolution(String name, String command) {
		this.name = name; 
		this.finish = Finish.DIE;
		setCommand(command);
		tableStats = new TableStats(name, Constants.widthGUI, 240, path, (flagStats & 2) == 2);
	}

	public Deconvolution(String name, String command, Finish finish) {
		this.name = name;
		this.finish = finish;
		setCommand(command);
		tableStats = new TableStats(name, Constants.widthGUI, 240, path, (flagStats & 2) == 2);
	}

	public void setCommand(String command) {
		this.command = command;
		Command.decode(command, this);
		this.command = command;
		if (name.equals("") && algo != null)
			name = algo.getShortname();
		live = false;
	}

	/**
	 * This method runs the deconvolution without graphical user interface.
	 * 
	 * @param exit
	 *            System.exit call is true
	 */
	public RealSignal deconvolve(RealSignal image, RealSignal psf) {
		this.image = image;
		this.psf = psf;
		runDeconvolve();
		return deconvolvedImage;
	}

	public RealSignal deconvolve() {
		this.image = null;
		this.psf = null;
		runDeconvolve();
		return deconvolvedImage;
	}

	/**
	 * This method runs the deconvolution without graphical user interface.
	 * 
	 * @param exit
	 *            System.exit call is true
	 */
	private void runDeconvolve() {
		if ((flagMonitor & 2) == 2) {
			TableMonitor tableMonitor = new TableMonitor(name , Constants.widthGUI, 240);
			monitors.add(tableMonitor);
			Lab.setVisible(tableMonitor.getPanel(), "Monitor of " + name, 10, 10);
		}

		if ((flagMonitor & 1) == 1) 
			monitors.add(new ConsoleMonitor());

		if ((flagStats & 1) == 1) {
			Lab.setVisible(tableStats.getPanel(), "Stats of " + name, 50, 50);
		}

		if (fft == null) {
			run();
			return;
		}

		if (!fft.isMultithreadable()) {
			run();
			return;
		}

		if (flagMultithreading) {
			Thread thread = new Thread(this);
			thread.setPriority(Thread.MIN_PRIORITY);
			thread.start();
		}
		else {
			run();
		}
		
	}

	/**
	 * This method runs the deconvolution with a graphical user interface.
	 * 
	 * @param job
	 *            Name of the job of deconvolution
	 */
	public void launch() {
		monitors = new Monitors();
		TableMonitor tableMonitor = null;
		if ((flagMonitor & 2) == 2) {
			tableMonitor = new TableMonitor(name , Constants.widthGUI, 240);
			monitors.add(tableMonitor);
		}
		if ((flagMonitor & 1) == 1)
			monitors.add(new ConsoleMonitor());
		if (flagStats == 0) {
			tableStats = null;
		}

		dialog = new DeconvolutionDialog(DeconvolutionDialog.Module.ALL, this, tableMonitor, tableStats);
		monitors.add(new StatusMonitor(dialog.getProgressBar()));

		Lab.setVisible(dialog, false);
	}

	@Override
	public void run() {
		double chrono = System.nanoTime();

		if (flagSystem)
			SystemInfo.activate();

		for (DeconvolutionListener listener : listeners)
			listener.started();

		live = true;
		if (monitors != null)
			monitors.setVerbose(verbose);

		report.add("Path", checkPath(path));
		monitors.log("Path: " + checkPath(path));
		
		if (image == null)
			image = openImage();

		if (image == null) {
			monitors.error("Image: Not valid " + command);
			report.add("Image", "Not valid");
			if (finish == Finish.KILL)
				System.exit(-101);
			return;
		}
		report.add("Image", image.dimAsString());
		monitors.log("Image: " + image.dimAsString());

		psf = openPSF();

		if (psf == null) {
			monitors.error("PSF: not valid");
			report.add("PSF", "Not valid");
			if (finish == Finish.KILL)
				System.exit(-102);
			return;
		}
		report.add("PSF", psf.dimAsString());
		monitors.log("PSF: " + psf.dimAsString());

		if (algo == null) {
			monitors.error("Algorithm: not valid");
			if (finish == Finish.KILL)
				System.exit(-103);
			return;
		}

		Controller controller = algo.getController();
		if (controller == null) {
			monitors.error("Controller: not valid");
			if (finish == Finish.KILL)
				System.exit(-104);
			return;
		}
		
		if (flagStats > 0)
			controller.setTableStats(tableStats);
		
		report.add("FFT", fft.getLibraryName());

		if (outs != null) {
			outs.setPath(path);
			controller.setOutputs(outs);
		}

		monitors.log("Algorithm: " + algo.getName());
		report.add("Algorithm", algo.getName());
		algo.setController(controller);
		
		deconvolvedImage = algo.run(monitors, image, psf, fft, pad, apo, norm, true);

		live = false;
		for (DeconvolutionListener listener : listeners)
			listener.finish();

		report.add("End", NumFormat.time(System.nanoTime() - chrono));

		if (flagDisplay)
			Lab.show(monitors, deconvolvedImage, "Result of " + algo.getShortname());

		if (finish == Finish.KILL) {
			System.out.println("End");
			System.exit(0);
		}

		if (finish == Finish.DIE)
			die();
	}

	
	public void close() {
		if (dialog != null)
			dialog.dispose();
		SignalCollector.free(image);
		SignalCollector.free(psf);
		SignalCollector.free(deconvolvedImage);
		algo = null;
		image = null;
		psf = null;
		deconvolvedImage = null;
		monitors = null;
		System.gc();
	}
	
	public void die() {
		SignalCollector.free(image);
		SignalCollector.free(psf);
	}


	/**
	 * This methods make a recap of the deconvolution. Useful before starting
	 * the processing.
	 * 
	 * @return list of messages to print
	 */
	public Features recap() {
		Features features = new Features();
		Token image = Command.extract(command, "-image");
		features.add("Image", image == null ? "keyword -image not found" : image.parameters);

		String normf = (norm < 0 ? " (no normalization)" : " (normalization to " + norm + ")");
		Token psf = Command.extract(command, "-psf");
		features.add("PSF", psf == null ? "keyword -psf not found" : psf.parameters + " norm:" + normf);

		if (algo == null) {
			features.add("Algorithm", "not valid>");
		}
		else {
			Controller controller = algo.getController();
			features.add("Algorithm", algo.toString());
			features.add("Stopping Criteria", controller.getStoppingCriteriaAsString(algo));
			features.add("Reference", controller.getReference());
			features.add("Constraint", controller.getConstraintAsString());
			features.add("Padding", pad.toString());
			features.add("Apodization", apo.toString());
			features.add("FFT", fft == null ? "null" : fft.getLibraryName());
		}
		features.add("Path", path);

		String monitor = "";
		if (flagMonitor == 0)
			monitor = "no ";
		if (flagMonitor == 1)
			monitor = "console (" + verbose.name().toLowerCase() + ")";
		if (flagMonitor == 2)
			monitor = "table (" + verbose.name().toLowerCase() + ")";
		if (flagMonitor == 3)
			monitor = "console table (" + verbose.name().toLowerCase() + ")";
		String stats = "";
		if (flagStats == 0)
			stats = "no ";
		if (flagStats == 1)
			stats = "show ";
		if (flagStats == 2)
			stats = "save ";
		if (flagStats == 3)
			stats = "show save";
		String running = "";
		running += "multithreading: " + (flagMultithreading ? "on " : "off ");
		running += " display: " + (flagDisplay ? "on " : "off ");
		running += " system: " + (flagSystem ? "shown" : "hidden ");

		features.add("Monitor", monitor);
		features.add("Stats", stats);
		features.add("Running", running);
		if (outs == null)
			features.add("Output", "not valid");
		else
			for (Output out : outs)
				features.add("Output " + out.getName(), out.toString());
		return features;
	}


	public Features checkOutput() {
		Features features = new Features();
		if (deconvolvedImage == null) {
			features.add("Image", "No valid output image");
			return features;
		}
		float stati[] = deconvolvedImage.getStats();
		int sizi = deconvolvedImage.nx * deconvolvedImage.ny * deconvolvedImage.nz;
		float totali = stati[0] * sizi;
		features.add("<html><b>Deconvolved Image</b></html>", "");
		features.add("Size", deconvolvedImage.dimAsString() + " " + NumFormat.bytes(sizi*4));
		features.add("Mean (stdev)", NumFormat.nice(stati[0])  + " (" + NumFormat.nice(stati[3]) + ")");
		features.add("Min ... Max", NumFormat.nice(stati[1]) + " ... " + NumFormat.nice(stati[2]));
		features.add("Energy (int)", NumFormat.nice(stati[5])  + " (" + NumFormat.nice(totali) + ")");
		return features;
	}
	
	public boolean isLive() {
		return live;
	}

	public void abort() {
		live = false;
		algo.getController().abort();
	}

	public String checkPath(String path) {
		File dir = new File(path);
		if (dir.exists()) {
			if (dir.isDirectory()) {
				if (dir.canWrite())
					return path + " (writable)";
				else
					return path + " (non-writable)";
			}
			else {
				return path + " (non-directory)";
			}
		}
		else {
			return path + " (not-valid)";
		}
	}

	public RealSignal openImage() {
		Token token = Command.extract(command, "-image");
		if (token == null)
			return null;
		if (token.parameters.startsWith(">>>"))
			return null;
		String arg = token.option.trim();
		String cmd = token.parameters.substring(arg.length(), token.parameters.length()).trim();
		image = Lab.createRealSignal(monitors, arg, cmd, path);
		return image;
	}

	public RealSignal openPSF() {
		Token token = Command.extract(command, "-psf");
		if (token == null)
			return null;
		if (token.parameters.startsWith(">>>"))
			return null;
		String arg = token.option.trim();
		String cmd = token.parameters.substring(arg.length(), token.parameters.length()).trim();
		psf = Lab.createRealSignal(monitors, arg, cmd, path);
		return psf;
	}


	public void addDeconvolutionListener(DeconvolutionListener listener) {
		listeners.add(listener);
	}

	public OutputCollection getOuts() {
		return outs;
	}

	public void setAlgorithm(AbstractAlgorithm algo, Controller controller) {
		this.algo = algo;
		if (algo != null && controller != null) {
			algo.setController(controller);
		}
	}

	public AbstractAlgorithm getAlgo() {
		return algo;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getPath() {
		return path;
	}

	public void setNormalization(double norm) {
		this.norm = norm;
	}

	public void setPadding(Padding pad) {
		this.pad = pad;
	}
	
	public Padding getPadding() {
		return pad;
	}
	
	public RealSignal getOutput() {
		return deconvolvedImage;
	}

	public RealSignal getImage() {
		return image;
	}

	public RealSignal getPSF() {
		return psf;
	}

	public Features getDeconvolutionReports() {
		return report;
	}

	public String getName() {
		return name;
	}
	
	public void setApodization(Apodization apo) {
		this.apo = apo;
	}

	public Apodization getApodization() {
		return apo;
	}

	public void setOutput(OutputCollection outs) {
		this.outs = outs;
	}

	public void setVerbose(Verbose verbose) {
		this.verbose = verbose;
	}

	public void setFFT(AbstractFFTLibrary fft) {
		this.fft = fft;
	}

	public void setMonitor(int flagMonitor) {
		this.flagMonitor = flagMonitor;
	}

	public void setStats(int flagStats) {
		this.flagStats = flagStats;
	}

	public void setFlags(boolean flagDisplay, boolean flagMultithreading, boolean flagSystem) {
		this.flagDisplay = flagDisplay;
		this.flagMultithreading = flagMultithreading;
		this.flagSystem = flagSystem;
	}

	public String getCommand() {
		return command;
	}


}
