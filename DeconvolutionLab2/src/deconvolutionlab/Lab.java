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

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Pattern;

import javax.swing.JFrame;

import deconvolution.algorithm.Controller;
import deconvolutionlab.PlatformImager.ContainerImage;
import deconvolutionlab.monitor.Monitors;
import fft.AbstractFFT;
import fft.AbstractFFTLibrary;
import fft.FFT;
import imagej.IJImager;
import lab.component.CustomizedColumn;
import lab.component.CustomizedTable;
import lab.tools.NumFormat;
import lab.tools.WebBrowser;
import plugins.sage.deconvolutionlab.IcyImager;
import signal.ComplexComponent;
import signal.ComplexSignal;
import signal.RealSignal;
import signal.factory.Sphere;

public class Lab {

	private static PlatformImager			imager;
	private static Lab						instance	= null;
	private static Platform					platform	= Platform.IMAGEJ;

	private static CustomizedTable			tableStats	= null;
	private static JFrame					frameStats	= null;

	static {
		imager = new IJImager();
		createStats();
	}

	private static void createStats() {

		ArrayList<CustomizedColumn> columns = new ArrayList<CustomizedColumn>();
		columns.add(new CustomizedColumn("Name", String.class, 100, false));
		columns.add(new CustomizedColumn("Algorithm", String.class, 100, false));
		columns.add(new CustomizedColumn("Iterations", String.class, 100, false));
		columns.add(new CustomizedColumn("Image Mean", String.class, 100, false));
		columns.add(new CustomizedColumn("Image Minimum", String.class, 100, false));
		columns.add(new CustomizedColumn("Image Maximum", String.class, 100, false));
		columns.add(new CustomizedColumn("Image Stdev", String.class, 100, false));
		columns.add(new CustomizedColumn("Image norm1", String.class, 100, false));
		columns.add(new CustomizedColumn("Image norm2", String.class, 100, false));
		columns.add(new CustomizedColumn("Time", String.class, 100, false));
		columns.add(new CustomizedColumn("Memory", String.class, 100, false));
		columns.add(new CustomizedColumn("Peak", String.class, 100, false));
		columns.add(new CustomizedColumn("PSNR", String.class, 100, false));
		columns.add(new CustomizedColumn("SNR", String.class, 100, false));
		columns.add(new CustomizedColumn("Residu", String.class, 100, false));
		tableStats = new CustomizedTable(columns, true);

	}

	public static Platform getPlatform() {
		return platform;
	}

	public static void getInstance(Platform p) {
		platform = p;
		if (instance == null) {
			instance = new Lab();
			switch (p) {
			case STANDALONE:
				imager = new LabImager();
				break;
			case IMAGEJ:
				imager = new IJImager();
				break;
			case ICY:
				imager = new IcyImager();
				break;
			default:
				imager = new LabImager();
				break;
			}
		}
	}

	public static void help() {
		WebBrowser.open(Constants.url);
	}

	public static void checkFFT(Monitors monitors) {

		ArrayList<AbstractFFTLibrary> libraries = FFT.getInstalledLibraries();
		for (int k = 1; k <= 3; k++)
			for (AbstractFFTLibrary library : libraries) {
				RealSignal y = new Sphere(3, 1).generate(40, 30, 20);
				double chrono = System.nanoTime();
				AbstractFFT fft = library.getDefaultFFT();
				fft.init(monitors, y.nx, y.ny, y.nz);
				RealSignal x = fft.inverse(fft.transform(y));
				chrono = System.nanoTime() - chrono;
				double residu = y.getEnergy() - x.getEnergy();
				monitors.log(fft.getName() + " Test " + k);
				monitors.log("\t residu of reconstruction: " + residu);
				monitors.log("\t computation time (" + x.nx + "x" + x.ny + "x" + x.nz + ") " + NumFormat.time(chrono));
			}
	}
	
	public static ContainerImage createContainer(Monitors monitors, String title) {
		monitors.log("Create Live Real Signal " + title);
		return imager.createContainer(title);
	}

	public static void append(Monitors monitors, ContainerImage container, RealSignal signal, String title) {
		imager.append(container, signal, title);
		monitors.log("Add Live Real Signal " + title);
	}

	public static void append(Monitors monitors, ContainerImage container, RealSignal signal, String title, PlatformImager.Type type) {
		imager.append(container, signal, title, type);
		monitors.log("Add Live Real Signal " + title);
	}

	public static void show(Monitors monitors, ComplexSignal signal, String title) {
		if (signal == null) {
			monitors.error("Show " + title + " this image does not exist.");
			return;
		}
		monitors.log("Show Real Signal " + title);
		imager.show(signal, title);
	}

	public static void show(Monitors monitors, ComplexSignal signal, String title, ComplexComponent complex) {
		if (signal == null) {
			monitors.error("Show " + title + " this image does not exist.");
			return;
		}
		monitors.log("Show Real Signal " + title);
		imager.show(signal, title, complex);
	}

	public static void show(Monitors monitors,  RealSignal signal, String title) {
		if (signal == null) {
			monitors.error("Show " + title + " this image does not exist.");
			return;
		}
		monitors.log("Show Real Signal " + title);
		imager.show(signal, title);
	}

	public static void show(Monitors monitors, RealSignal signal, String title, PlatformImager.Type type) {
		if (signal == null) {
			monitors.error("Show " + title + " this image does not exist.");
			return;
		}
		monitors.log("Show Real Signal " + title);
		imager.show(signal, title, type);
	}

	public static void show(Monitors monitors, RealSignal signal, String title, PlatformImager.Type type, int z) {
		if (signal == null) {
			monitors.error("Show " + title + " this image does not exist.");
			return;
		}
		monitors.log("Show Real Signal " + title);
		imager.show(signal, title, type);
	}

	public static void save(Monitors monitors, RealSignal signal, String filename) {
		imager.save(signal, filename);
		monitors.log("Save Real Signal " + filename);
	}

	public static void save(Monitors monitors, RealSignal signal, String filename, PlatformImager.Type type) {
		imager.save(signal, filename, type);
		monitors.log("Save Real Signal " + filename);
	}

	public static void firstStats(Monitors monitors, String name, String[] stats, boolean show, boolean save) {
		if (stats == null)
			return;

		Lab.createStats();

		if (show) {
			frameStats = new JFrame(name);
			frameStats.getContentPane().add(tableStats.getPane(600, 200));
			frameStats.pack();
			frameStats.setVisible(true);
		}
	}

	public static void nextStats(Monitors monitors, String name, String[] stats, boolean show, boolean save) {
		if (tableStats == null)
			return;
		if (stats == null)
			return;
		tableStats.append(stats);
		monitors.log("Stats " + name);
		if (show && frameStats != null) {
			if (!frameStats.isVisible())
				frameStats.setVisible(true);
			frameStats.requestFocus();
		}
	}

	public static void lastStats(Monitors monitors, String filename, String[] stats, boolean show, boolean save) {
		if (stats == null)
			return;
		if (tableStats == null)
			return;
		if (save) {
			monitors.log("Stats " + filename);
			tableStats.saveCSV(filename + ".csv");
		}
	}

	public static RealSignal open(Monitors monitors, String filename) {
		RealSignal signal = imager.open(filename);
		if (signal == null)
			monitors.error("Unable to open " + filename);
		else
			monitors.log("Load " + filename);
		return signal;
	}

	public static RealSignal openDir(Monitors monitors, String path) {

		String parts[] = path.split(" pattern ");
		String dirname = path;
		String regex = "";
		if (parts.length == 2) {
			dirname = parts[0].trim();
			regex = parts[1].trim();
		}

		File file = new File(dirname + File.separator);

		if (!file.isDirectory()) {
			monitors.error("Dir " + dirname + " is not a directory.");
			return null;
		}
		String[] list = file.list();
		ArrayList<RealSignal> slices = new ArrayList<RealSignal>();
		int nx = 0;
		int ny = 0;
		Pattern pattern = Pattern.compile(regex);
		for (String filename : list) {
			if (pattern.matcher(filename).find()) {
				RealSignal slice = imager.open(dirname + File.separator + filename);
				if (slice != null) {
					slices.add(slice);
					nx = Math.max(nx, slice.nx);
					ny = Math.max(ny, slice.ny);
					monitors.log("Image " + path + File.separator + filename + " is loaded.");
				}
			}
			else {
				monitors.error("Error in loading image " + path + File.separator + filename);
			}
		}
		int nz = slices.size();
		if (nz <= 0) {
			monitors.error("Dir " + path + " do no contain valid images.");
			return null;
		}
		RealSignal signal = new RealSignal(nx, ny, nz);
		for (int z = 0; z < slices.size(); z++)
			signal.setSlice(z, slices.get(z));
		return signal;
	}

	public static void showOrthoview(Monitors monitors, RealSignal signal, String title, int hx, int hy, int hz) {
		if (signal == null) {
			monitors.error("Show Orthoview " + title + " this image does not exist.");
			return;
		}
		imager.show(signal.createOrthoview(hx, hy, hz), title);
	}

	public static void showOrthoview(Monitors monitors, RealSignal signal, String title) {
		if (signal == null) {
			monitors.error("Show Orthoview " + title + " this image does not exist.");
			return;
		}
		int hx = signal.nx / 2;
		int hy = signal.ny / 2;
		int hz = signal.nz / 2;
		imager.show(signal.createOrthoview(hx, hy, hz), title);
	}

	public static void showMIP(Monitors monitors, RealSignal signal, String title) {
		if (signal == null) {
			monitors.error("Show MIP " + title + " this image does not exist.");
			return;
		}
		imager.show(signal.createMIP(), title);
	}

	public static void showMontage(Monitors monitors, RealSignal signal, String title) {
		if (signal == null) {
			monitors.error("Show Montage " + title + " this image does not exist.");
			return;
		}
		imager.show(signal.createMontage(), title);
	}

	public static RealSignal create(Monitors monitors, String name) {
		RealSignal signal = imager.create(name);
		if (signal != null)
			monitors.log("Created the real signal " + name + " " + signal.toString());
		else
			monitors.error("Impossible to create the real signal " + name);
		return signal;
	}

	public static RealSignal create(Monitors monitors) {
		RealSignal signal = imager.create();
		if (signal != null)
			monitors.log("Created the real signal from the active window " + signal.toString());
		else
			monitors.error("Impossible to create the real signal from the active window");
		return signal;
	}

	public static PlatformImager getImager() {
		return imager;
	}


}
