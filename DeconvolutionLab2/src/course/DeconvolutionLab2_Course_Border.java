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

import ij.plugin.PlugIn;

import java.io.File;

import javax.swing.filechooser.FileSystemView;

import signal.RealSignal;
import signal.factory.Cube;
import signal.factory.Gaussian;
import deconvolution.Deconvolution;
import deconvolutionlab.Lab;
import deconvolutionlab.monitor.Monitors;

public class DeconvolutionLab2_Course_Border implements PlugIn {

	private String desktop = FileSystemView.getFileSystemView().getHomeDirectory().getAbsolutePath() + File.separator + "Desktop";
	private String root = desktop + File.separator + "Deconvolution" + File.separator;
	private String res = root + "results" + File.separator + "border" + File.separator;

	public DeconvolutionLab2_Course_Border() {
		
		Monitors monitors = Monitors.createDefaultMonitor();
		new File(res).mkdir();
		System.setProperty("user.dir", res);
		
		int nx = 200;
		int ny = 200;
		int nz = 40;
	
		RealSignal im = new Cube(22, .1).intensity(100).center(0.25, 0.00, 0.05).generate(nx, ny, nz);
		RealSignal i0 = new Cube(22, .1).intensity(100).center(0.25, 0.05, 0.05).generate(nx, ny, nz);
		RealSignal i1 = new Cube(22, .1).intensity(100).center(0.25, 0.10, 0.05).generate(nx, ny, nz);
		RealSignal i2 = new Cube(22, .1).intensity(100).center(0.25, 0.15, 0.05).generate(nx, ny, nz);
		im.max(i1.max(i2).max(i0));
		
		RealSignal g = new Gaussian(10, 10, 10).intensity(101).generate(nx, ny, nz);

		Lab.save(monitors, im, res + "ref.tif");
		Lab.save(monitors, g, res + "psf.tif");
		
		String psf = " -psf file " + "psf.tif";
		String ref = " -image file " + "ref.tif";
		String cst = " -image synthetic constant 250 0 size 200 200 40";

		String algo  = " -algorithm CONV -out ortho REFo (64,32,16)"; 
		new Deconvolution("run", ref + " -psf synthetic impulse " + algo).deconvolve();

		algo  = " -algorithm CONV -stack CONV -out ortho CONVo rescaled byte (64,32,16) -out mip CONVp rescaled byte"; 
		new Deconvolution("run", ref + psf + algo).deconvolve();

		algo  = " -algorithm CONV -pad NO NO 200 200 -out ortho PADo200 rescaled byte (64,32,16) -out mip PADp200 rescaled byte"; 
		new Deconvolution("run", ref + psf + algo).deconvolve();

		algo  = " -algorithm CONV -pad NO NO 100 100 -out ortho PADo100 rescaled byte (64,32,16) -out mip PADp100 rescaled byte"; 
		new Deconvolution("run", ref + psf + algo).deconvolve();

		algo  = " -algorithm CONV -pad NO NO 40 40 -out ortho PADo40 rescaled byte (64,32,16) -out mip PADp40 rescaled byte"; 
		new Deconvolution("run", ref + psf + algo).deconvolve();

		algo  = " -algorithm CONV -pad NO NO 20 20 -out ortho PADo20 rescaled byte (64,32,16) -out mip PADp20 rescaled byte"; 
		new Deconvolution("run", ref + psf + algo).deconvolve();

		algo  = " -algorithm CONV -pad NO NO 10 10 -out ortho PADo10 rescaled byte (64,32,16) -out mip PADp10 rescaled byte"; 
		new Deconvolution("run", ref + psf + algo).deconvolve();

		algo  = " -algorithm CONV -pad NO NO 5 5 -out ortho PADo2 rescaled byte (64,32,16) -out mip PADp2 rescaled byte"; 
		new Deconvolution("run", ref + psf + algo).deconvolve();

		algo  = " -algorithm CONV -apo HANN HANN -out ortho HANNo rescaled byte (64,32,16) -out mip HANNp rescaled byte"; 
		new Deconvolution("run", ref + psf + algo).deconvolve();
		
		algo  = " -algorithm CONV -apo TUKEY TUKEY -out ortho TUKEYo rescaled byte (64,32,16) -out mip TUKEYp rescaled byte"; 
		new Deconvolution("run", ref + psf + algo).deconvolve();

		algo  = " -algorithm CONV --pad NO NO 8 8 apo HANN HANN -out ortho PAD8_HANNo rescaled byte (64,32,16)  -out mip PAD8_HANNp rescaled byte"; 
		new Deconvolution("run", ref + psf + algo).deconvolve();

		
		algo  = " -algorithm CONV -apo HANN HANN -out ortho HANN_CSTo rescaled byte -out mip HANN_CSTp rescaled byte"; 
		new Deconvolution("run", cst + psf + algo).deconvolve();
		
		algo  = " -algorithm CONV -apo TUKEY TUKEY -out ortho TUKEY_CSTo rescaled byte -out mip TUKEY_CSTp rescaled byte"; 
		new Deconvolution("run", cst + psf + algo).deconvolve();

		
		algo  = " -algorithm CONV -pad E2 E2 -out ortho PADpPower2FFTW rescaled byte (64,32,16) -out mip PADpPower2FFTW rescaled byte"; 
		
		
		new Deconvolution("run", cst + psf + algo + " -fft FFTW2 ").deconvolve();
		new Deconvolution("run", cst + psf + algo + " -fft Academic ").deconvolve();
		new Deconvolution("run", cst + psf + algo + " -fft JTransforms ").deconvolve();
		
	}
	
	public static void main(String arg[]) {
		new DeconvolutionLab2_Course_Border();
	}
	
	@Override
	public void run(String arg) {
		new DeconvolutionLab2_Course_Border();
	}	


}