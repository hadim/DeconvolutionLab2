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

import deconvolution.Stats;
import deconvolution.algorithm.Controller;
import deconvolution.algorithm.Convolution;
import deconvolutionlab.Lab;
import deconvolutionlab.monitor.Monitors;
import ij.plugin.PlugIn;
import signal.RealSignal;
import signal.factory.Airy;
import signal.factory.CubeSphericalBeads;

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
		int ny = 120;
		int nz = 122;
		int spacing = 12;
		int border = 6;
		
		RealSignal x = new CubeSphericalBeads(4, 0.1, spacing, border).intensity(400).generate(nx, ny, nz);
		//RealSignal x = new Sphere(30, 1).generate(nx, ny, nz);
		//RealSignal x = new Constant().intensity(0, 255).generate(nx, ny, nz);
		//Lab.show(monitors, x, "reference");
		//Lab.showOrthoview(x);
		//Lab.showMIP(x);
		//Lab.save(monitors, x, res + "ref.tif");

		//RealSignal h = new Gaussian(3, 3, 1).generate(nx, ny, nz);
		RealSignal h = new Airy(100, 50, 0.5, 0.1).generate(nx, ny, nz);
		Lab.show(monitors, h, "psf");
		Lab.showOrthoview(h);
		Lab.showMIP(h);
		Lab.save(monitors, h, res + "psf.tif");
		Lab.save(monitors, h.createOrthoview(), res + "psfo.tif");
		Lab.save(monitors, h.createMIP(), res + "psfp.tif");

		String algo  = " ";
		String param = " -reference " + res + "ref.tif -stats show -display no -monitor no -system no";
		String conv = " -image file " + res + "conv.tif ";
		String ref = " -image file " + res + "ref.tif ";
		String psf = " -psf file " + res + "psf.tif ";

		Controller controller = new Controller();
		controller.setSystem(true);
		controller.setReference(res + "ref.tif");
		controller.setStatsMode(Stats.Mode.SHOW);
		
		Convolution convo = new Convolution();
		convo.setSystem(true);
		convo.setReference(res + "ref.tif");
		
		RealSignal y = convo.run(x, h);
		Lab.show(y);
		
		
		/*
		algo  = " -algorithm NIF -out mip NIFp ";
		new Deconvolution("nif", conv + psf + algo + param).deconvolve();
/*
//		algo  = " -algorithm TRIF 10e-5 -pad NO NO 0 32 -out mip trifppad -out ortho trifopad ";
//		new Deconvolution("trif", conv + psf + algo + param).deconvolve();

		algo  = " -algorithm TRIF 10e-5 -out mip TRIFo ";
		new Deconvolution("trif", conv + psf + algo + param).deconvolve();
		
		algo  = " -algorithm TRIF 10e-5 -pad NO NO 100 100 -out mip TRIFoapo ";
		new Deconvolution("TRIF apo", conv + psf + algo + param).deconvolve();


//		algo  = " -algorithm TRIF 10e-4  -pad NO NO 0 20 -out mip trif_ppad -out ortho trif_opad";
//		new Deconvolution("trif", conv + psf + algo + param).deconvolve();

		//algo  = " -algorithm TRIF 10e-6 -out mip trif_p -out ortho trif_o -stats show";
		//new Deconvolution("trif", conv + psf + algo + param).deconvolve();
		/*

		for(int i=-8; i<=-2; i+=20) {
			algo  = " -algorithm TRIF " + Math.pow(10, i) + " -out mip nifp" + i + " -out ortho nifo" + i + " -stats show";
			new Deconvolution("trif", conv + psf + algo + param).deconvolve();
		}

		algo  = " -algorithm SIM 0 1 0  -out ortho simo  -system no -monitor no";
		new Deconvolution("run", ref + psf + algo).deconvolve();

		algo  = " -algorithm NIF -out ortho nifo  -system -monitor";
		new Deconvolution("run", signal + psf + algo).deconvolve();
		for(int i=0; i<=24; i++) {
			double p = Math.pow(10, i-18);
			algo  = " -algorithm RIF " + p + " -out ortho @5 RIF/RIF" + i + paramout;
			new Deconvolution("run", signal + psf + algo).deconvolve();
		}
		algo  = " -algorithm LW+ 30 1 -out stats @3 LW+ nosave -out ortho @s5 LW+/LW+" + paramout;
		new Deconvolution("run", signal + psf + algo).deconvolve();
	*/	
	}
	
	public static void main(String arg[]) {
		new DeconvolutionLab2_Course_Resolution();
	}
	
	@Override
	public void run(String arg) {
		new DeconvolutionLab2_Course_Resolution();
	}	


}