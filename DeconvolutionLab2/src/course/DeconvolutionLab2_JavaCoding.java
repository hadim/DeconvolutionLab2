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

import deconvolution.algorithm.Convolution;
import deconvolutionlab.Lab;
import deconvolutionlab.monitor.Monitors;
import ij.plugin.PlugIn;
import signal.RealSignal;

public class DeconvolutionLab2_JavaCoding implements PlugIn {

	private String desktop = FileSystemView.getFileSystemView().getHomeDirectory().getAbsolutePath() + File.separator + "Desktop";
	private String root = desktop + File.separator + "Deconvolution" + File.separator;
	private String res = root + "results" + File.separator + "bigradient" + File.separator;
	private String data = root + "data" + File.separator + "bigradient" + File.separator;
	
	public DeconvolutionLab2_JavaCoding() {
		
		Monitors monitors = new Monitors();
		
		RealSignal image = Lab.openFile(filename);
		
		Convolution convolution = new Convolution();
		
	
		RealSignal a = convolution.run(monitors, image, psf, fftlib, pad, apo, norm, threaded);
		
	}

	private static String out(String root, String name) {
		return "out stats " + root + name  + 
				 " -out stack " + root + name + "_32 -out stack " + root + name + "_8 rescaled byte noshow";
	}
		
	public static void main(String arg[]) {
		new DeconvolutionLab2_JavaCoding();
	}
	
	@Override
	public void run(String arg) {
		new DeconvolutionLab2_JavaCoding();
	}

}
