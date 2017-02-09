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

import javax.swing.JFrame;

import deconvolution.algorithm.AbstractAlgorithm;
import deconvolution.algorithm.Controller;
import deconvolutionlab.Lab;
import deconvolutionlab.Output;
import deconvolutionlab.OutputCollection;
import deconvolutionlab.monitor.ConsoleMonitor;
import deconvolutionlab.monitor.Monitors;
import deconvolutionlab.monitor.StatusMonitor;
import deconvolutionlab.monitor.TableMonitor;
import deconvolutionlab.monitor.Verbose;
import fft.AbstractFFT;
import fft.AbstractFFTLibrary;
import fft.FFT;
import lab.tools.NumFormat;
import signal.RealSignal;
import signal.apodization.AbstractApodization;
import signal.apodization.Apodization;
import signal.apodization.UniformApodization;
import signal.factory.SignalFactory;
import signal.padding.Padding;

public class Deconvolution implements Runnable {

	private AbstractAlgorithm	algo				= null;

	private String				path;
	private Monitors			monitors			= Monitors.createDefaultMonitor();
	private Verbose				verbose				= Verbose.Log;
	private Controller			controller;
	private OutputCollection	outs;

	private Padding				padding				= new Padding();
	private Apodization			apodization			= new Apodization();
	private double				factorNormalization	= 1.0;
	private AbstractFFTLibrary	fftlib;

	private String				command				= "";
	private boolean				live				= false;

	private ArrayList<String>	report				= new ArrayList<String>();

	private String				name				= "";
	private boolean				exit				= false;

	private boolean				watcherMonitor				= true;
	private boolean				watcherConsole				= true;
	private boolean				watcherDisplay				= true;
	private boolean				watcherMultithreading		= true;
	
	private ArrayList<DeconvolutionListener> listeners = new ArrayList<DeconvolutionListener>();
	
	public Deconvolution(String command) {
		super();
		monitors = Monitors.createDefaultMonitor();
		this.command = command;
		decode();
	}

	public void setCommand(String command) {
		this.command = command;
		decode();
	}

	public String getCommand() {
		return command;
	}

	public Monitors getMonitors() {
		if (monitors == null)
			return Monitors.createDefaultMonitor();
		return monitors;
	}

	/**
	 * This method runs the deconvolution without graphical user interface.
	 * 
	 * @param exit
	 *            System.exit call is true
	 */
	public void deconvolve(boolean exit) {
		this.exit = exit;
		
		monitors = new Monitors();
		if (watcherConsole)
			monitors.add(new ConsoleMonitor());
		
		if (watcherMonitor) {
			TableMonitor m = new TableMonitor(440, 440);
			monitors.add(m);
			String t = algo == null ? "Monitor " + name : name + " " + algo.getName();
			JFrame frame = new JFrame(t);
			frame.add(m.getPanel());
			frame.pack();
			frame.setVisible(true);
		}

		if (fftlib == null) {
			run();
			return;
		}
		if (!fftlib.isMultithreadable()) {
			run();
			return;
		}
		
		if (watcherMultithreading) {
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
	 * @param exit
	 *            System.exit call is true
	 */
	public void launch(String job, boolean exit) {
		this.name = job;
		this.exit = exit;
		DeconvolutionDialog d = new DeconvolutionDialog(this);
		monitors = new Monitors();
		monitors.add(new StatusMonitor(d.getProgressBar()));
		if (watcherConsole)
			monitors.add(new ConsoleMonitor());
		
		if (watcherMonitor) {
			TableMonitor m = new TableMonitor(440, 440);
			monitors.add(m);
			d.addTableMonitor("Monitor", m);
		}

	}

	public void decode() {

		algo = null;

		path = System.getProperty("user.dir");
		if (monitors==null)
			monitors = Monitors.createDefaultMonitor();
		verbose = Verbose.Log;

		controller = new Controller();
		outs = new OutputCollection();

		padding = new Padding();
		apodization = new Apodization();
		factorNormalization = 1.0;
		fftlib = FFT.getLibraryByName("Academic");

		live = false;

		ArrayList<Token> tokens = Command.parse(command);
		for (Token token : tokens) {
			if (token.keyword.equalsIgnoreCase("-algorithm"))
				algo = Command.decodeAlgorithm(token, controller);

			if (token.keyword.equalsIgnoreCase("-disable")) {
				watcherMonitor = Command.decodeDisable(token, "monitor");
				watcherConsole = Command.decodeDisable(token, "console");
				watcherDisplay = Command.decodeDisable(token, "display");
				watcherMultithreading = Command.decodeDisable(token, "multithreading");
			}

			if (token.keyword.equalsIgnoreCase("-verbose"))
				verbose = Verbose.getByName(token.parameters);

			if (token.keyword.equalsIgnoreCase("-path") && !token.parameters.equalsIgnoreCase("current")) {
				path = token.parameters;
			}

			if (token.keyword.equalsIgnoreCase("-fft"))
				fftlib = FFT.getLibraryByName(token.parameters);

			if (token.keyword.equalsIgnoreCase("-pad"))
				padding = Command.decodePadding(token);
			if (token.keyword.equalsIgnoreCase("-apo"))
				apodization = Command.decodeApodization(token);
			if (token.keyword.equalsIgnoreCase("-norm"))
				factorNormalization = Command.decodeNormalization(token);
			if (token.keyword.equalsIgnoreCase("-constraint"))
				Command.decodeController(token, controller);
			if (token.keyword.equalsIgnoreCase("-time"))
				Command.decodeController(token, controller);
			if (token.keyword.equalsIgnoreCase("-residu"))
				Command.decodeController(token, controller);
			if (token.keyword.equalsIgnoreCase("-reference"))
				Command.decodeController(token, controller);
			if (token.keyword.equalsIgnoreCase("-savestats"))
				Command.decodeController(token, controller);
			if (token.keyword.equalsIgnoreCase("-showstats"))
				Command.decodeController(token, controller);

			if (token.keyword.equals("-out")) {
				Output out = Command.decodeOut(token);
				if (out != null)
					outs.add(out);
			}
		}

		if (name.equals("") && algo != null)
			name = algo.getShortname();

	}

	public void setApodization(ArrayList<AbstractApodization> apos) {
		AbstractApodization apoXY = new UniformApodization();
		AbstractApodization apoZ = new UniformApodization();
		if (apos.size() >= 1)
			apoXY = apos.get(0);
		if (apos.size() >= 2)
			apoZ = apos.get(1);
		this.apodization = new Apodization(apoXY, apoXY, apoZ);
	}

	@Override
	public void run() {
		double chrono = System.nanoTime();
		
		for(DeconvolutionListener listener : listeners)
			listener.started();
		
		live = true;
		if (monitors != null)
			monitors.setVerbose(verbose);
		
		report.add("Path: " + checkPath(path));
		monitors.log("Path: " + checkPath(path));
		
		RealSignal image = openImage();
		if (image == null) {
			monitors.error("Image: Not valid " + command);
			report.add("Image: Not valid");
			if (exit)
				System.exit(-101);
			return;
		}
		report.add(  "Image: " + image.dimAsString());
		monitors.log("Image: " + image.dimAsString());
		
		RealSignal y = padding.pad(monitors, getApodization().apodize(monitors, image));

		RealSignal psf = openPSF();
		if (psf == null) {
			monitors.error("PSF: not valid");
			report.add("PSF: Not valid");
			if (exit)
				System.exit(-102);
			return;
		}
		report.add(  "PSF: " + psf.dimAsString());
		monitors.log("PSF: " + psf.dimAsString());
		
		monitors.log("PSF: normalization " + (factorNormalization <= 0 ? "no" : factorNormalization));
		RealSignal h = psf.changeSizeAs(y).normalize(factorNormalization);

		if (algo == null) {
			monitors.error("Algorithm: not valid");
			if (exit)
				System.exit(-103);
			return;
		}

		if (controller == null) {
			monitors.error("Controller: not valid");
			if (exit)
				System.exit(-104);
			return;
		}

		AbstractFFT fft;
		if (fftlib != null)
			fft = FFT.createFFT(monitors, fftlib, image.nx, image.ny, image.nz);
		else
			fft = FFT.createDefaultFFT(monitors, image.nx, image.ny, image.nz);
		report.add("FFT: " + fft.getName());

		algo.setFFT(fft);
		controller.setFFT(fft);

		algo.setController(controller);
		if (outs != null) {
			outs.setPath(path);
			controller.setOutputs(outs);
		}

		monitors.log("Algorithm: " + algo.getName());
		report.add("Algorithm: " + algo.getName());

		RealSignal x = algo.run(monitors, y, h, true);

		RealSignal result = padding.crop(monitors, x);

		if (outs != null)
			outs.executeFinal(monitors, result, controller);

		live = false;
		for(DeconvolutionListener listener : listeners)
			listener.finish();
		
		report.add("End " + algo.getName() + " in " + NumFormat.time(System.nanoTime()-chrono));
		
		if (watcherDisplay)
			Lab.show(monitors, result, "Result of " + algo.getShortname());

		if (exit) {
			System.out.println("End");
			System.exit(0);
		}
	}

	/**
	 * This methods make a recap of the deconvolution. Useful before starting the processing.
	 * 
	 * @return list of messages to print
	 */
	public ArrayList<String> recap() {
		ArrayList<String> lines = new ArrayList<String>();
		Token image = Command.extract(command, "-image");
		if (image == null)
			lines.add("<b>Image</b>: <span color=\"red\">keyword -image not found</span>");
		else
			lines.add("<b>Image</b>: " + image.parameters);

		String norm = (factorNormalization < 0 ? " (no normalization)" : " (normalization to " + factorNormalization + ")");
		Token psf = Command.extract(command, "-psf");
		if (psf == null)
			lines.add("<b>PSF</b>: <span color=\"red\">keyword -psf not found</span>");
		else
			lines.add("<b>PSF</b>: " + psf.parameters + norm);

		if (algo == null) {
			lines.add("<b>Algorithm</b>:  <span color=\"red\">not valid</span>");
		}
		else {
			Controller controller = algo.getController();
			String con = ", " + controller.getConstraintAsString() + " constraint";
			lines.add("<b>Algorithm</b>: " + algo.toString() + con);
			lines.add("<b>Stopping Criteria</b>: " + controller.getStoppingCriteria(algo));
			lines.add("<b>Reference</b>: " + controller.getReference());
			lines.add("<b>Stats</b>: " + controller.getShowStats() + " " + controller.getSaveStats());
			lines.add("<b>Padding</b>: " + padding.toString());
			lines.add("<b>Apodization</b>: " + apodization.toString());
			if (algo.getFFT() != null)
				lines.add("<b>FFT</b>: " + algo.getFFT().getName());
		}
		lines.add("<b>Path</b>: " + path);

		lines.add("<b>Verbose</b>: " + verbose.name().toLowerCase());
		lines.add("<b>Monitor</b>: " + (watcherMonitor ? "on" : "off"));
		lines.add("<b>Console</b>: " + (watcherConsole ? "on" : "off"));
		lines.add("<b>Final Display</b>: " + (watcherDisplay ? "on" : "off"));
		lines.add("<b>Multithreading</b>: " + (watcherMultithreading ? "on" : "off"));
	
		if (outs == null)
			lines.add("<b>Outputs</b>: not valid");
		else
			lines.addAll(outs.getInformation());
		return lines;
	}

	public ArrayList<String> checkAlgo() {
		ArrayList<String> lines = new ArrayList<String>();
		RealSignal image = openImage();
		if (image == null) {
			lines.add("No valid input image");
			return lines;
		}
		if (padding == null) {
			lines.add("No valid padding");
			return lines;
		}
		if (apodization == null) {
			lines.add("No valid apodization");
			return lines;
		}
		RealSignal psf = openPSF();
		if (psf == null) {
			lines.add("No valid PSF");
			return lines;
		}
		if (algo == null) {
			lines.add("No valid algorithm");
			return lines;
		}

		Controller controller = algo.getController();
		RealSignal y = padding.pad(monitors, getApodization().apodize(monitors, image));
		RealSignal h = psf.changeSizeAs(y).normalize(factorNormalization);

		int iter = controller.getIterationMax();
		algo.getController().setIterationMax(1);
		RealSignal x = algo.run(monitors, y, h, true);
		Lab.show(monitors, x, "Estimate after 1 iteration");
		lines.add("Time: " + NumFormat.seconds(controller.getTimeNano()));
		lines.add("Peak Memory: " + controller.getMemoryAsString());
		controller.setIterationMax(iter);
		return lines;
	}

	public ArrayList<String> checkImage() {
		ArrayList<String> lines = new ArrayList<String>();
		RealSignal image = openImage();
		if (image == null) {
			lines.add("No valid input image");
			return lines;
		}
		if (padding == null) {
			lines.add("No valid padding");
			return lines;
		}
		if (apodization == null) {
			lines.add("No valid apodization");
			return lines;
		}

		RealSignal signal = padding.pad(monitors, getApodization().apodize(monitors, image));
		lines.add("<b>Image</b>");
		lines.add("Original size " + image.dimAsString() + " padded to " + signal.dimAsString());
		lines.add("Original: " + formatStats(image));
		lines.add("Preprocessing: " + formatStats(signal));

		Lab.show(monitors, signal, "Image");
		return lines;
	}

	public ArrayList<String> checkPSF() {
		ArrayList<String> lines = new ArrayList<String>();
		RealSignal image = openImage();
		if (image == null) {
			lines.add("No valid input image");
			return lines;
		}
		if (padding == null) {
			lines.add("No valid padding");
			return lines;
		}
		if (apodization == null) {
			lines.add("No valid apodization");
			return lines;
		}

		RealSignal psf = openPSF();
		if (psf == null) {
			lines.add("No valid PSF");
			return lines;
		}

		RealSignal signal = padding.pad(monitors, getApodization().apodize(monitors, image));
		RealSignal h = psf.changeSizeAs(signal);
		lines.add("<b>PSF</b>");
		lines.add("Original size " + psf.dimAsString() + " padded to " + h.dimAsString());

		String e = NumFormat.nice(h.getEnergy());

		h.normalize(factorNormalization);
		lines.add("Original: " + formatStats(psf));
		lines.add("Preprocessing: " + formatStats(h));
		lines.add("Energy = " + e + " and after normalization=" + NumFormat.nice(h.getEnergy()));
		Lab.show(monitors, h, "Padded and Normalized PSF");
		return lines;
	}

	public ArrayList<String> getDeconvolutionReports() {
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

	public Padding getPadding1() {
		return padding;
	}

	public Apodization getApodization() {
		return apodization;
	}

	public OutputCollection getOuts() {
		return outs;
	}

	public AbstractAlgorithm getAlgo() {
		return algo;
	}

	public String getPath() {
		return path;
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

	private static String formatStats(RealSignal x) {
		float stats[] = x.getStats();
		String s = " mean=" + NumFormat.nice(stats[0]);
		s += " stdev=" + NumFormat.nice(stats[1]);
		s += " min=" + NumFormat.nice(stats[3]);
		s += " max=" + NumFormat.nice(stats[2]);
		return s;
	}
	
	public void addDeconvolutionListener(DeconvolutionListener listener) {
		listeners.add(listener);
	}

}
