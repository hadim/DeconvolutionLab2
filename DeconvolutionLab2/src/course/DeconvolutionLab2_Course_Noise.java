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

import bilib.tools.Files;
import deconvolution.Deconvolution;
import deconvolutionlab.Lab;
import deconvolutionlab.monitor.Monitors;
import ij.plugin.PlugIn;
import signal.RealSignal;
import signal.factory.Cube;

public class DeconvolutionLab2_Course_Noise implements PlugIn {

	private String root = Files.getDesktopDirectory() + "Deconvolution" + File.separator;
	private String res = root + "results" + File.separator + "noise" + File.separator;
	
	public DeconvolutionLab2_Course_Noise() {

		Monitors monitors = Monitors.createDefaultMonitor();
		new File(res).mkdir();
		System.setProperty("user.dir", res);
				
		int nx = 560;
		int ny = 120;
		int nz = 1;
		String size = " size " + nx + " " + ny + " " + nz;
			
		RealSignal im = new Cube(50, 0.25).intensity(100).center(0.2, 0.5, 0).generate(nx, ny, nz);
		RealSignal i1 = new Cube(50, 0.25).intensity(70).center(0.4, 0.5, 0).generate(nx, ny, nz);
		RealSignal i2 = new Cube(50, 0.25).intensity(40).center(0.6, 0.5, 0).generate(nx, ny, nz);
		RealSignal i3 = new Cube(50, 0.25).intensity(10).center(0.8, 0.5, 0).generate(nx, ny, nz);
		im.plus(i1);
		im.plus(i2);
		im.plus(i3);
		Lab.show(monitors, im, "im.tif");
		Lab.save(monitors, im, res + "im.tif");
		
		String psf = " -psf synthetic impulse 1 0 " + size;
		String image = " -image file im.tif";

		// Simulation
		String name = "SIM m 0 s 50 p 0";
		String out = " -stack " + name + " -out stack " + name + "-BYTE rescaled byte noshow";
		new Deconvolution("noise", psf + image + " -algorithm " + name + out).run();
		
		name = "SIM m 0 s 00 p 150";
		out = " -stack " + name + " -out stack " + name + "-BYTE rescaled byte noshow";
		new Deconvolution("noise", psf + image + " -algorithm " + name + out).run();
		
		name = "SIM m 0 s 15 p 30";
		out = " -stack " + name + " -out stack " + name + "-BYTE rescaled byte noshow";
		new Deconvolution("noise", psf + image + " -algorithm " + name + out).run();
	}
	
	
	public static void main(String arg[]) {
		new DeconvolutionLab2_Course_Noise();
	}
	
	@Override
	public void run(String arg) {
		new DeconvolutionLab2_Course_Noise();
	}

}
