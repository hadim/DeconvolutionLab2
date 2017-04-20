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
import ij.plugin.PlugIn;

public class DeconvolutionLab2_Course_Bigradient implements PlugIn {

	private String root = Files.getDesktop() + File.separator + "Deconvolution" + File.separator;
	private String res = root + "results" + File.separator + "bigradient" + File.separator;
	private String data = root + "data" + File.separator + "bigradient" + File.separator;
	
	public DeconvolutionLab2_Course_Bigradient() {
		
		new File(res).mkdir();
		System.setProperty("user.dir", res);
		
		new File(res + "TRIF").mkdir();
		new File(res + "RIF").mkdir();
		new File(res + "LW").mkdir();
		new File(res + "LW-ITER").mkdir();
		new File(res + "LW+").mkdir();
		new File(res + "LW+-ITER").mkdir();
		new File(res + "RL").mkdir();
		new File(res + "RL-ITER").mkdir();
		new File(res + "RLTV").mkdir();
		new File(res + "RLTV-ITER").mkdir();
		new File(res + "FISTA").mkdir();
		new File(res + "FISTA-ITER").mkdir();
		
		String psf = " -psf file " + data + "psf.tif  -reference " + data + "ref.tif ";
		String noisy = " -image file convnoise.tif";
	
		new Deconvolution("run", "-image file " + data + "ref.tif" + psf + " -algorithm SIM 0 1 1 -out stack convnoise -out stack conbnoise_8 rescaled byte noshow").deconvolve();
	
		new Deconvolution("run", noisy + psf + " -algorithm NIF -out stack NIF").deconvolve();
		new Deconvolution("run", noisy + psf + " -algorithm DIV -out stack DIV").deconvolve();
		
		for(int i=0; i<=3; i++) {
			double p = Math.pow(5, i-10);
			String name = "RIF" + String.format("%02d", i);
			new Deconvolution("run", noisy + psf + " -algorithm RIF " + p + out("RIF" + File.separator, name)).deconvolve();
		}
		for(int i=0; i<=3; i++) {
			double p = Math.pow(5, i-10);
			String name = "TRIF" + String.format("%02d", i);
			new Deconvolution("run", noisy + psf + " -algorithm TRIF " + p + out("TRIF" + File.separator, name)).deconvolve();
		}

		String lw  = " -algorithm LW 20 1 -out mip @2 LW-ITER/I -out stats @1 LW nosave";
		new Deconvolution("run", noisy  + psf + lw).deconvolve();
		new File(res + "LW-ITER/I.tif").delete();
		
		
		String lwp  = " -algorithm LW+ 20 1 -out mip @2 LW+-ITER/I -out stats @1 LW+ nosave";
		new Deconvolution("run", noisy  + psf + lwp).deconvolve();
		new File(res + "LW+-ITER/I.tif").delete();


		String rl  = " -algorithm RL 20 -out mip @2 RL-ITER/I -out stats @1 RL nosave";
		new Deconvolution("run", noisy  + psf + rl).deconvolve();
		new File(res + "RL-ITER/I.tif").delete();

		String rltv  = " -algorithm RLTV 20 10 -out mip @2 RLTV-ITER/I -out stats @1 RLTV nosave";
		new Deconvolution("run", noisy  + psf + rltv).deconvolve();
		new File(res + "RLTV-ITER/I.tif").delete();
		
		String fista  = " -algorithm FISTA 20 1 1 Spline3 3 -mip @2 FISTA-ITER/I -out stats @1 FISTA nosave";
		new Deconvolution("run", noisy + psf + fista).deconvolve();
		new File(res + "FISTA-ITER/I.tif").delete();

	}

	private static String out(String root, String name) {
		return "out stats " + root + name  + 
				 " -out stack " + root + name + "_32 -out stack " + root + name + "_8 rescaled byte noshow";
	}
		
	public static void main(String arg[]) {
		new DeconvolutionLab2_Course_Bigradient();
	}
	
	@Override
	public void run(String arg) {
		new DeconvolutionLab2_Course_Bigradient();
	}

}
