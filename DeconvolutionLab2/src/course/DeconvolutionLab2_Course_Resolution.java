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

import deconvolution.Deconvolution;
import deconvolutionlab.Lab;
import deconvolutionlab.monitor.Monitors;
import ij.plugin.PlugIn;
import signal.RealSignal;
import signal.factory.GridSpots;

public class DeconvolutionLab2_Course_Resolution implements PlugIn {

	private String desktop = FileSystemView.getFileSystemView().getHomeDirectory().getAbsolutePath() + File.separator + "Desktop";
	private String root = desktop + File.separator + "Deconvolution" + File.separator;
	private String res = root + "results" + File.separator + "resolution" + File.separator;
	
	public DeconvolutionLab2_Course_Resolution() {

		Monitors monitors = Monitors.createDefaultMonitor();
		new File(res).mkdir();
		System.setProperty("user.dir", res);
					
		new File(res + "RIF").mkdir();
		new File(res + "LW").mkdir();
		new File(res + "LW+").mkdir();
		new File(res + "RL").mkdir();
	
		int nx = 128;
		int ny = 128;
		int nz = 128;
		int spacing = 16;
		
		RealSignal x = new GridSpots(3, 0.1, spacing).intensity(0, 255).generate(nx, ny, nz);
		Lab.show(monitors, x, "reference");
		Lab.save(monitors, x, res + "ref.tif");

		String algo  = " ";
		String ground = " -image file " + res + "ref.tif ";
		//String psf   = " -psf file ../../Data/resolution/psfgl.tif";
		String psf = " -psf synthetic gaussian 100.0 0.0 1.2 1.2 3.6 size " + nx + " " + ny + " " + nz;
		String signal  = " -image file signal.tif -reference " + res + "ref.tif -disable monitor";
		
		String paramout = " intact float  (" + spacing + "," + spacing + "," + spacing + ")";
		
		algo  = " -algorithm CONV  -out stats @3 PR -out stack PR -out ortho PRo ";
		new Deconvolution("run", ground + "-reference reference.tif -psf synthetic impulse 100 0 size 128 128 128 " + algo).deconvolve();

		algo  = " -algorithm SIM 0 1.5 0  -out stats @3 SIM -out stack signal -out ortho SIGNALo ";
		new Deconvolution("run", ground + psf + algo).deconvolve();

		algo  = " -algorithm NIF -out ortho NIF " + paramout;
		new Deconvolution("run", signal + psf + algo).deconvolve();

		for(int i=0; i<=24; i++) {
			double p = Math.pow(10, i-18);
			algo  = " -algorithm RIF " + p + " -out ortho @5 RIF/RIF" + i + paramout;
			new Deconvolution("run", signal + psf + algo).deconvolve();
		}
		
		algo  = " -algorithm LW+ 305 1 -out stats @3 LW+ nosave -out ortho @25 LW+/LW+" + paramout;
		new Deconvolution("run", signal + psf + algo).deconvolve();
	}
	
	public static void main(String arg[]) {
		new DeconvolutionLab2_Course_Resolution();
	}
	
	@Override
	public void run(String arg) {
		new DeconvolutionLab2_Course_Resolution();
	}	


}