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

import ij.ImagePlus;
import ij.io.FileSaver;
import ij.io.Opener;
import ij.plugin.PlugIn;
import ij.process.ColorProcessor;

import java.io.File;

import javax.swing.filechooser.FileSystemView;

import deconvolution.Deconvolution;

public class DeconvolutionLab2_Course_Celegans implements PlugIn {

	private String	desktop	= FileSystemView.getFileSystemView().getHomeDirectory().getAbsolutePath() + File.separator + "Desktop";
	private String	root	= desktop + File.separator + "DeconvolutionLab2-Course" + File.separator;
	private String	res	    = root + "Results" + File.separator + "c-elegans" + File.separator;
	private String	data	= root + "Data" + File.separator + "c-elegans" + File.separator;

	public DeconvolutionLab2_Course_Celegans() {

		new File(res).mkdir();
		System.setProperty("user.dir", res);
		
		run(" -algorithm RIF 0.000001 ", "RIF6_");
		run(" -algorithm RIF 0.0000001 ", "RIF7_");
	//run(" -algorithm RL 100  ", "RL_");
		run(" -algorithm LW+ 200 1 ", "LW+_");
		//run(" -algorithm I ", "IN_");
		//run(" -algorithm RIF 0.001 ", "RIF3_");
		//run(" -algorithm RIF 0.0001 ", "RIF4_");
		//run(" -algorithm RIF 0.00001 ", "RIF5_");
		//run(" -algorithm VC 100 ", "VC");
		//run(" -algorithm RLTV 10 0.1 ", "RLTV");
		//run(" -algorithm FISTA 50 1 0.1 ", "FISTA");
		//run(" -algorithm ISTA 50 1 0.1 ", "ISTA");
	}
	
	private void run(String a, String name) {
		String channels[] = new String[] { "CY3", "FITC", "DAPI"};

		ImagePlus[] ort = new ImagePlus[3];
		ImagePlus[] fig = new ImagePlus[3];
		for (int i=0; i<3; i++) {
			String channel = channels[i];
			String psf = " -psf file " + data + "psf-CElegans-" + channel + ".tif";
			String img = " -image file " + data + "CElegans-" + channel +".tif";
			String param = " -fft JTransforms -disable display multithreading";
			String algo = a + out(name + channel);
			new Deconvolution("deconvolve", img + psf + algo + param).deconvolve();
			ort[i] = new Opener().openImage( res + name + channel + "_ortho_8.tif");
			fig[i] = new Opener().openImage( res + name + channel + "_figure_8.tif");
		}
		new FileSaver(color(ort)).saveAsTiff(res + name + "-ortho-rgb.tif");
		new FileSaver(color(fig)).saveAsTiff(res + name + "-figure-rgb.tif");
	}

	private static String out(String name) {
		return 
			" -out ortho " + name + "_ortho_8 rescaled byte (160,180,50) noshow" + 
			" -out mip " + name + "_mip_8 rescaled byte noshow" + 
			" -out figure " + name + "_figure_8 rescaled byte  (160,180,50) ";

	}

	private static ImagePlus color(ImagePlus imp[]) {
		int nx = imp[0].getWidth();
		int ny = imp[0].getHeight();		
		ColorProcessor cp = new ColorProcessor(nx, ny);
		byte r[] = (byte[])imp[0].getProcessor().getPixels();
		byte g[] = (byte[])imp[1].getProcessor().getPixels();
		byte b[] = (byte[])imp[2].getProcessor().getPixels();
		cp.setRGB(r,  g,  b);
		ImagePlus out =	new ImagePlus( "rgb", cp);
		out.show();
		return out;
	}
	public static void main(String arg[]) {
		new DeconvolutionLab2_Course_Celegans();
	}

	@Override
	public void run(String arg) {
		new DeconvolutionLab2_Course_Celegans();
	}

}