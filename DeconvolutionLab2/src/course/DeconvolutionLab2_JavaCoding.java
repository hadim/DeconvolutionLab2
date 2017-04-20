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

package course;

import java.io.File;

import javax.swing.filechooser.FileSystemView;

import deconvolution.algorithm.Simulation;
import deconvolutionlab.Lab;
import deconvolutionlab.monitor.Monitors;
import ij.plugin.PlugIn;
import signal.RealSignal;

public class DeconvolutionLab2_JavaCoding implements PlugIn {

	private String desktop = FileSystemView.getFileSystemView().getHomeDirectory().getAbsolutePath() + File.separator + "Desktop" + File.separator;
	
	public DeconvolutionLab2_JavaCoding() {
		String path = desktop + "Deconvolution" + File.separator + "data" + File.separator + "bars" + File.separator;
		Monitors monitors = Monitors.createDefaultMonitor();
		RealSignal image = Lab.openFile(monitors, path + "bars.tif");
		RealSignal psf = Lab.openFile(monitors, path + "psf.tif");
		Simulation convolution = new Simulation(100, 100, 10);
		RealSignal a = convolution.run(image, psf);
		Lab.showMIP(monitors, a, "a");
	}
		
	public static void main(String arg[]) {
		new DeconvolutionLab2_JavaCoding();
	}
	
	@Override
	public void run(String arg) {
		new DeconvolutionLab2_JavaCoding();
	}

}