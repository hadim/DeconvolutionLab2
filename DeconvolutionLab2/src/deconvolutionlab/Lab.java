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

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

import bilib.tools.NumFormat;
import bilib.tools.WebBrowser;
import deconvolutionlab.Imaging.ContainerImage;
import deconvolutionlab.monitor.Monitors;
import fft.AbstractFFT;
import fft.AbstractFFTLibrary;
import fft.FFT;
import imagej.IJImager;
import plugins.sage.deconvolutionlab.IcyImager;
import signal.ComplexComponent;
import signal.ComplexSignal;
import signal.RealSignal;
import signal.factory.SignalFactory;
import signal.factory.Sphere;

public class Lab {

	private static Imaging imaging;
	private static ArrayList<JFrame> frames;
	private static ArrayList<JDialog> dialogs;

	static {
		frames = new ArrayList<JFrame>();
		dialogs = new ArrayList<JDialog>();
		imaging = new IJImager();
		Config.init(System.getProperty("user.dir") + File.separator + "DeconvolutionLab2.config");
	}

	public static Imaging.Platform getPlatform() {
		return imaging.getPlatform();
	}

	public static void init(Imaging.Platform p) {
		init(p, System.getProperty("user.dir") + File.separator + "DeconvolutionLab2.config");
	}

	public static void init(Imaging.Platform platform, String config) {
		switch (platform) {
		case IMAGEJ:
			imaging = new IJImager();
			break;
		case ICY:
			imaging = new IcyImager();
			break;
		default:
			imaging = new IJImager();
			break;
		}
		
		Config.init(System.getProperty("user.dir")+File.separator+"DeconvolutionLab2.config");
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
		return imaging.createContainer(title);
	}

	public static void append(Monitors monitors, ContainerImage container, RealSignal signal, String title) {
		imaging.append(container, signal, title, Imaging.Type.FLOAT);
		monitors.log("Add Live Real Signal " + title);
	}

	public static void append(Monitors monitors, ContainerImage container, RealSignal signal, String title, Imaging.Type type) {
		imaging.append(container, signal, title, type);
		monitors.log("Add Live Real Signal " + title);
	}

	public static void show(Monitors monitors, ComplexSignal signal, String title) {
		if (signal == null) {
			monitors.error("Show " + title + " this image does not exist.");
			return;
		}
		monitors.log("Show Real Signal " + title);
		imaging.show(signal, title, ComplexComponent.MODULE);
	}

	public static void show(Monitors monitors, ComplexSignal signal, String title, ComplexComponent complex) {
		if (signal == null) {
			monitors.error("Show " + title + " this image does not exist.");
			return;
		}
		monitors.log("Show Real Signal " + title);
		imaging.show(signal, title, complex);
	}

	public static void show(Monitors monitors, RealSignal signal, String title) {
		if (signal == null) {
			monitors.error("Show " + title + " this image does not exist.");
			return;
		}
		monitors.log("Show Real Signal " + title);
		imaging.show(signal, title, Imaging.Type.FLOAT, signal.nz / 2);
	}

	public static void show(Monitors monitors, RealSignal signal, String title, Imaging.Type type) {
		if (signal == null) {
			monitors.error("Show " + title + " this image does not exist.");
			return;
		}
		monitors.log("Show Real Signal " + title);
		imaging.show(signal, title, type, signal.nz / 2);
	}

	public static void show(Monitors monitors, RealSignal signal, String title, Imaging.Type type, int z) {
		if (signal == null) {
			monitors.error("Show " + title + " this image does not exist.");
			return;
		}
		monitors.log("Show Real Signal " + title);
		imaging.show(signal, title, type, z);
	}

	public static void save(Monitors monitors, RealSignal signal, String filename) {
		imaging.save(signal, filename, Imaging.Type.FLOAT);
		monitors.log("Save Real Signal " + filename);
	}

	public static void save(Monitors monitors, RealSignal signal, String filename, Imaging.Type type) {
		imaging.save(signal, filename, type);
		monitors.log("Save Real Signal " + filename);
	}

	
	public static RealSignal createSynthetic(Monitors monitors, String cmd) {
		RealSignal signal = SignalFactory.createFromCommand(cmd);
		if (signal == null)
			monitors.error("Unable to create " + cmd);
		else
			monitors.log("Create " + cmd);
		return signal;
	}

	public static RealSignal getImage(Monitors monitors, String name) {
		RealSignal signal = getImager().create(name);
		if (signal == null)
			monitors.error("Unable to get " + name);
		else
			monitors.log("Load " + name);
		return signal;
	}

	public static RealSignal openFile(Monitors monitors, String filename) {
		RealSignal signal = imaging.open(filename);
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
				RealSignal slice = imaging.open(dirname + File.separator + filename);
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
		RealSignal signal = new RealSignal(file.getName(), nx, ny, nz);
		for (int z = 0; z < slices.size(); z++)
			signal.setSlice(z, slices.get(z));
		return signal;
	}

	public static void showOrthoview(Monitors monitors, RealSignal signal, String title, int hx, int hy, int hz) {
		if (signal == null) {
			monitors.error("Show Orthoview " + title + " this image does not exist.");
			return;
		}
		imaging.show(signal.createOrthoview(hx, hy, hz), title, Imaging.Type.FLOAT, 0);
	}

	public static void showOrthoview(Monitors monitors, RealSignal signal, String title) {
		if (signal == null) {
			monitors.error("Show Orthoview " + title + " this image does not exist.");
			return;
		}
		int hx = signal.nx / 2;
		int hy = signal.ny / 2;
		int hz = signal.nz / 2;
		imaging.show(signal.createOrthoview(hx, hy, hz), title, Imaging.Type.FLOAT, 0);
	}

	public static void showMIP(Monitors monitors, RealSignal signal, String title) {
		if (signal == null) {
			monitors.error("Show MIP " + title + " this image does not exist.");
			return;
		}
		imaging.show(signal.createMIP(), title, Imaging.Type.FLOAT, 0);
	}

	public static void showMontage(Monitors monitors, RealSignal signal, String title) {
		if (signal == null) {
			monitors.error("Show Montage " + title + " this image does not exist.");
			return;
		}
		imaging.show(signal.createMontage(), title, Imaging.Type.FLOAT, 0);
	}

	public static RealSignal create(Monitors monitors, String name) {
		RealSignal signal = imaging.create(name);
		if (signal != null)
			monitors.log("Created the real signal " + name + " " + signal.toString());
		else
			monitors.error("Impossible to create the real signal " + name);
		return signal;
	}

	public static RealSignal create(Monitors monitors) {
		RealSignal signal = imaging.create();
		if (signal != null)
			monitors.log("Created the real signal from the active window " + signal.toString());
		else
			monitors.error("Impossible to create the real signal from the active window");
		return signal;
	}

	public static Imaging getImager() {
		return imaging;
	}

	public static String getActiveImage() {
		if (imaging.isSelectable())
			return imaging.getSelectedImage();
		return "";
	}
	
	public static void setVisible(JDialog dialog, boolean modal) {
		if (dialog == null)
			return;
		dialogs.add(dialog);
		imaging.setVisible(dialog, modal);
	}
	
	public static void setVisible(JPanel panel, String name, int x, int y) {
		JFrame frame = new JFrame(name);
		frame.getContentPane().add(panel);
		frame.pack();
		frame.setLocation(x, y);
		frame.setVisible(true);
		frames.add(frame);
	}

	public static void setVisible(JFrame frame) {
		frames.add(frame);
		frame.setVisible(true);
	}
	
	public static void close() {
		for(JFrame frame : frames)
			if (frame != null)
				frame.dispose();
		for(JDialog dialog : dialogs)
			if (dialog != null)
				dialog.dispose();
	}


	

}
