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
import fft.AbstractFFT;
import fft.AbstractFFTLibrary;
import lab.tools.NumFormat;
import signal.RealSignal;
import signal.apodization.Apodization;
import signal.factory.SignalFactory;
import signal.padding.Padding;

public class Deconvolution implements Runnable {

	public enum Finish {
		DIE, ALIVE, KILL
	};

	private AbstractAlgorithm					algo				= null;

	private String								path;
	private double								norm				= 1.0;
	private Padding								pad					= new Padding();
	private Apodization							apo					= new Apodization();
	private OutputCollection					outs;
	private Verbose								verbose				= Verbose.Log;
	private AbstractFFTLibrary					fft;
	private int									flagMonitor			= 3;
	private int									flagStats			= 0;
	private boolean								flagDisplay			= true;
	private boolean								flagMultithreading	= true;
	private boolean								flagSystem			= true;

	private Monitors							monitors			= new Monitors();

	private String								command				= "";
	private boolean								live				= false;

	private Features							report				= new Features();

	private String								name				= "";

	private ArrayList<DeconvolutionListener>	listeners			= new ArrayList<DeconvolutionListener>();	
	private boolean								imageLoaded			= false;
	private RealSignal							image;
	private RealSignal							psf;
	private RealSignal							deconvolvedImage;

	private Finish								finish				= Finish.DIE;
	private DeconvolutionDialog					dialog;
	
	private TableStats tableStats;
	private TableMonitor tableMonitor;

	public Deconvolution(String name, String command) {
		this.name = name; 
		tableStats = new TableStats(name, Constants.widthGUI, 240, path, (flagStats & 2) == 2);
		tableMonitor = new TableMonitor(name , Constants.widthGUI, 240);
		this.finish = Finish.DIE;
		setCommand(command);
	}

	public Deconvolution(String name, String command, Finish finish) {
		this.name = name;
		tableStats = new TableStats(name, Constants.widthGUI, 240, path, (flagStats & 2) == 2);
		tableMonitor = new TableMonitor(name , Constants.widthGUI, 240);
		this.finish = finish;
		setCommand(command);
	}

	public void setCommand(String command) {
		this.command = command;
		Command.decode(command, this);
		this.command = command;
		if (name.equals("") && algo != null)
			name = algo.getShortname();
		monitors = Monitors.createDefaultMonitor();
		live = false;
	}

	public void close() {
		if (dialog != null)
			dialog.dispose();
		finalize();
	}
	
	public void finalize() {
		algo = null;
		image = null;
		psf = null;
		monitors = null;
		System.gc();
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
		imageLoaded = true;
		runDeconvolve();
		return deconvolvedImage;
	}

	public RealSignal deconvolve() {
		this.image = null;
		this.psf = null;
		imageLoaded = false;
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
			monitors.add(tableMonitor);
			tableMonitor.show();
		}

		if ((flagStats & 1) == 1) {
			tableStats.show();
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
		dialog = new DeconvolutionDialog(DeconvolutionDialog.Module.ALL, this, tableMonitor, tableStats);

		Lab.setVisible(dialog, false);
		monitors = new Monitors();
		monitors.add(new StatusMonitor(dialog.getProgressBar()));

		if ((flagMonitor & 2) == 2) {
			monitors.add(tableMonitor);
		}

	}

	public Monitors createMonitors1() {
		Monitors monitors = new Monitors();
		return monitors;
	}

	@Override
	public void run() {
		double chrono = System.nanoTime();

		if ((flagMonitor & 1) == 1) 
			monitors.add(new ConsoleMonitor());

		if (flagSystem)
			SystemInfo.activate();

		for (DeconvolutionListener listener : listeners)
			listener.started();

		live = true;
		if (monitors != null)
			monitors.setVerbose(verbose);

		report.add("Path", checkPath(path));
		monitors.log("Path: " + checkPath(path));

		if (!imageLoaded)
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

		if (!imageLoaded)
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

		report.add("End", algo.getName() + " in " + NumFormat.time(System.nanoTime() - chrono));

		if (flagDisplay)
			Lab.show(monitors, deconvolvedImage, "Result of " + algo.getShortname());

		if (finish == Finish.KILL) {
			System.out.println("End");
			System.exit(0);
		}

		if (finish == Finish.DIE)
			finalize();
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
			features.add("FFT", algo.getFFT() == null ? "" : algo.getFFT().getName());
		}
		features.add("Path", path);

		String monitor = "";
		if (flagMonitor == 0)
			monitor = " monitor: no ";
		if (flagMonitor == 1)
			monitor = " monitor: console (" + verbose.name().toLowerCase() + ")";
		if (flagMonitor == 2)
			monitor = " monitor: table (" + verbose.name().toLowerCase() + ")";
		if (flagMonitor == 3)
			monitor = " monitor: console table (" + verbose.name().toLowerCase() + ")";
		String stats = "";
		if (flagStats == 0)
			stats = " stats: no ";
		if (flagStats == 1)
			stats = " stats: show ";
		if (flagStats == 2)
			stats = " stats: save ";
		if (flagStats == 3)
			stats = " stats: show save";
		String running = "";
		running += " multithreading: " + (flagMultithreading ? "on " : "off ");
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

	public Features checkAlgo() {
		Features features = new Features();
		RealSignal image = openImage();
		if (image == null) {
			features.add("Image", "No valid input image");
			return features;
		}
		if (pad == null) {
			features.add("Padding", "No valid padding");
			return features;
		}
		if (apo == null) {
			features.add("Apodization", "No valid apodization");
			return features;
		}
		RealSignal psf = openPSF();
		if (psf == null) {
			features.add("Image", "No valid PSF");
			return features;
		}
		if (algo == null) {
			features.add("Algorithm", "No valid algorithm");
			return features;
		}

		Controller controller = algo.getController();
		if (controller == null) {
			features.add("Controller", "No valid controller");
			return features;
		}

		algo.setController(controller);
		double chrono = System.nanoTime(); 
		AbstractFFT f = fft.getDefaultFFT();
		RealSignal slice = image.getSlice(0);
		if (slice != null) {
			f.init(Monitors.createDefaultMonitor(), slice.nx, slice.ny, 1);
			f.transform(slice);
			int n = algo.isIterative() ? controller.getIterationMax() : 1;
			chrono = (System.nanoTime() - chrono) * 2 * slice.nz * n;
		
			features.add("Estimated Time", NumFormat.time(chrono) );
		}
		else 
			features.add("Estimated Time", "Error" );
		features.add("Estimated Memory", controller.getMemoryAsString());
		features.add("Iterative", algo.isIterative()  ? "" + controller.getIterationMax() : "Direct");
		return features;
	}

	public Features checkImage(RealSignal image) {
		Features features = new Features();
		if (image == null) {
			features.add("Image", "No valid input image");
			return features;
		}
		if (pad == null) {
			features.add("Padding", "No valid padding");
			return features;
		}
		if (apo == null) {
			features.add("Apodization", "No valid apodization");
			return features;
		}

		RealSignal signal = pad.pad(monitors, getApodization().apodize(monitors, image));
		float stats[] = signal.getStats();
		float stati[] = image.getStats();
		int sizi = image.nx * image.ny * image.nz;
		int sizs = signal.nx * signal.ny * signal.nz;
		float totali = stati[0] * sizi;
		float totals = stats[0] * sizs;
		features.add("<html><b>Orignal Image</b></html>", "");
		features.add("Size", image.dimAsString() + " " + NumFormat.bytes(sizi*4));
		features.add("Mean (stdev)", NumFormat.nice(stati[0])  + " (" + NumFormat.nice(stati[3]) + ")");
		features.add("Min ... Max", NumFormat.nice(stati[1]) + " ... " + NumFormat.nice(stati[2]));
		features.add("Energy (int)", NumFormat.nice(stati[5])  + " (" + NumFormat.nice(totali) + ")");
		
		features.add("<html><b>Working Image</b></html>", "");
		features.add("Size", signal.dimAsString() + " " + NumFormat.bytes(sizs*4));
		features.add("Mean (stdev)", NumFormat.nice(stats[0])  + " (" + NumFormat.nice(stats[3]) + ")");
		features.add("Min Max", NumFormat.nice(stats[1]) + " " + NumFormat.nice(stats[2]));
		features.add("Energy (int)", NumFormat.nice(stats[5])  + " (" + NumFormat.nice(totals) + ")");
		features.add("<html><b>Information</b></html>", "");
		features.add("Size Increase ", NumFormat.nice((double)(sizs-sizi)/sizi*100.0));
		features.add("Energy Lost", NumFormat.nice((stats[5]-stati[5])/stati[5]*100));
	
		return features;
	}

	public Features checkPSF(RealSignal psf) {
		Features features = new Features();
		if (!imageLoaded)
			image = openImage();

		if (image == null) {
			features.add("Image", "No valid input image");
			return features;
		}
		if (pad == null) {
			features.add("Padding", "No valid padding");
			return features;
		}
		if (apo == null) {
			features.add("Apodization", "No valid apodization");
			return features;
		}

		if (psf == null) {
			features.add("PSF", "No valid PSF");
			return features;
		}

		RealSignal h = psf.changeSizeAs(image);
		h.normalize(norm);

		float stats[] = h.getStats();
		float stati[] = psf.getStats();
		int sizi = psf.nx * psf.ny * psf.nz;
		int sizs = h.nx * h.ny * h.nz;
		float totali = stati[0] * sizi;
		float totals = stats[0] * sizs;
		features.add("<html><b>Orignal PSF</b></html>", "");
		features.add("Size", psf.dimAsString() + " " + NumFormat.bytes(sizi*4));
		features.add("Mean (stdev)", NumFormat.nice(stati[0])  + " (" + NumFormat.nice(stati[3]) + ")");
		features.add("Min ... Max", NumFormat.nice(stati[1]) + " ... " + NumFormat.nice(stati[2]));
		features.add("Energy (int)", NumFormat.nice(stati[5])  + " (" + NumFormat.nice(totali) + ")");
		
		features.add("<html><b>Working PSF</b></html>", "");
		features.add("Size", h.dimAsString() + " " + NumFormat.bytes(sizs*4));
		features.add("Mean (stdev)", NumFormat.nice(stats[0])  + " (" + NumFormat.nice(stats[3]) + ")");
		features.add("Min Max", NumFormat.nice(stats[1]) + " " + NumFormat.nice(stats[2]));
		features.add("Energy (int)", NumFormat.nice(stats[5])  + " (" + NumFormat.nice(totals) + ")");
		features.add("<html><b>Information</b></html>", "");
		features.add("Size Increase ", NumFormat.nice((double)(sizs-sizi)/sizi*100.0));
		features.add("Energy Lost", NumFormat.nice((stats[5]-stati[5])/stati[5]*100));
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
		features.add("Size", image.dimAsString() + " " + NumFormat.bytes(sizi*4));
		features.add("Mean (stdev)", NumFormat.nice(stati[0])  + " (" + NumFormat.nice(stati[3]) + ")");
		features.add("Min ... Max", NumFormat.nice(stati[1]) + " ... " + NumFormat.nice(stati[2]));
		features.add("Energy (int)", NumFormat.nice(stati[5])  + " (" + NumFormat.nice(totali) + ")");
	
		return features;
	}
	
	public RealSignal getOutput() {
		return deconvolvedImage;
	}
	
	public Features getDeconvolutionReports() {
		return report;
	}

	public String getName() {
		return name;
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
		return getImage(monitors, token);
	}

	public RealSignal openPSF() {
		Token token = Command.extract(command, "-psf");
		if (token == null)
			return null;
		if (token.parameters.startsWith(">>>"))
			return null;
		return getImage(monitors, token);
	}

	private RealSignal getImage(Monitors monitors, Token token) {
		String arg = token.option.trim();
		String cmd = token.parameters.substring(arg.length(), token.parameters.length()).trim();

		if (arg.equalsIgnoreCase("synthetic")) {
			String parts[] = cmd.split(" ");
			if (parts.length <= 0)
				return null;
			String shape = parts[0];
			for (String name : SignalFactory.getAllName()) {
				if (shape.equalsIgnoreCase(name.toLowerCase())) {
					double params[] = Command.parseNumeric(cmd);
					SignalFactory factory = SignalFactory.getFactoryByName(shape);
					if (factory == null)
						return null;
					double amplitude = params.length > 0 ? params[0] : 1;
					double background = params.length > 1 ? params[1] : 0;
					factory.intensity(background, amplitude);
					int np = factory.getParameters().length;
					double[] features = new double[np];
					for (int i = 0; i < Math.min(np, params.length); i++)
						features[i] = params[i + 2];
					factory.setParameters(features);
					int nx = params.length > np + 2 ? (int) Math.round(params[np + 2]) : 128;
					int ny = params.length > np + 3 ? (int) Math.round(params[np + 3]) : 128;
					int nz = params.length > np + 4 ? (int) Math.round(params[np + 4]) : 128;
					double cx = params.length > np + 5 ? params[np + 5] : 0.5;
					double cy = params.length > np + 6 ? params[np + 6] : 0.5;
					double cz = params.length > np + 7 ? params[np + 7] : 0.5;
					factory = factory.center(cx, cy, cz);
					RealSignal x = factory.generate(nx, ny, nz);
					return x;
				}
			}
		}
		if (arg.equalsIgnoreCase("file") || arg.equalsIgnoreCase("dir") || arg.equalsIgnoreCase("directory")) {
			RealSignal signal = null;
			File file = new File(path + File.separator + cmd);
			if (file != null) {
				if (file.isFile())
					signal = Lab.open(monitors, path + File.separator + cmd);
				if (file.isDirectory())
					signal = Lab.openDir(monitors, path + File.separator + cmd);
			}
			if (signal == null) {
				File local = new File(cmd);
				if (local != null) {
					if (local.isFile())
						signal = Lab.open(monitors, cmd);
					if (local.isDirectory())
						signal = Lab.openDir(monitors, cmd);
				}
			}
			return signal;

		}

		if (arg.equalsIgnoreCase("platform")) {
			return Lab.getImager().create(cmd);
		}

		return null;
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
