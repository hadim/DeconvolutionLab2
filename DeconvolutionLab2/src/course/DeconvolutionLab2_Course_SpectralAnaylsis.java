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
import deconvolutionlab.Imager;
import deconvolutionlab.Lab;
import deconvolutionlab.monitor.Monitors;
import fft.AbstractFFT;
import fft.FFT;
import ij.plugin.PlugIn;
import signal.ComplexSignal;
import signal.Operations;
import signal.RealSignal;
import signal.factory.DoG;
import signal.factory.Gaussian;
import signal.factory.complex.ComplexSignalFactory;

public class DeconvolutionLab2_Course_SpectralAnaylsis implements PlugIn {

	private String desktop = FileSystemView.getFileSystemView().getHomeDirectory().getAbsolutePath() + File.separator + "Desktop";
	private String root = desktop + File.separator + "Deconvolution" + File.separator;
	private String res = root + "results" + File.separator + "star" + File.separator;
	private String data = root + "data" + File.separator + "star" + File.separator;
	
	public DeconvolutionLab2_Course_SpectralAnaylsis() {
		
		new File(res).mkdir();
		System.setProperty("user.dir", res);
		
		new File(res + "TRIF").mkdir();
		new File(res + "TRIF-FILTER").mkdir();
		new File(res + "RIF").mkdir();
		new File(res + "RIF-FILTER").mkdir();
		new File(res + "LW").mkdir();
		new File(res + "LW-ITER").mkdir();
		new File(res + "LW+").mkdir();
		new File(res + "LW+-ITER").mkdir();
		new File(res + "RL").mkdir();
		new File(res + "RL-ITER").mkdir();
		new File(res + "NOISELESS").mkdir();
		new File(res + "PERTURBATION").mkdir();
		new File(res + "SIMULATION").mkdir();
		new File(res + "ICTM").mkdir();
		new File(res + "ICTM-ITER").mkdir();
		
		Monitors monitors = Monitors.createDefaultMonitor();
		int nx = 256;
		int ny = 256;
		int nz = 1;
		String size = " size " + nx + " " + ny + " " + nz;
		double noise = 0.04;
		double poisson = 0.01;
		double wiener = Math.sqrt(noise * 2.0 / Math.PI);
		System.out.println("Wiener value " + wiener);
		AbstractFFT fft = FFT.createDefaultFFT(monitors, nx, ny, nz);
		ComplexSignal L = ComplexSignalFactory.laplacian(nx, ny, nz);
		RealSignal laplacian = fft.inverse(L).circular().rescale(monitors);
		Lab.save(monitors, laplacian, res + "laplacian.tif", Imager.Type.BYTE);
		
		RealSignal h = new DoG(2, 3.6).generate(nx, ny, nz);
		h.times(0.7f);
		h.plus(new Gaussian(1.5, 1.5, 1.5).generate(nx, ny, nz));
		Lab.save(monitors, h, res + "psf.tif");
		
		h.plus(new Gaussian(0.5, 0.5, 0.5).generate(nx, ny, nz));
		Lab.save(monitors, h, res + "psfPerturbated.tif");
	
		String psf = " -psf file psf.tif -fft FFTW2";
		String impulse = " -psf synthetic impulse 100.0 0.0 " + size;
		String image = " -image file " + data + "ref.tif";
		String constant = " -image constant 0 0 " + size;
		
		// Simulation
		String algo  = " -algorithm CONV " + out("CONV");
		new Deconvolution("run", psf + image + algo).deconvolve();
	
		algo  = " -algorithm CONV " + out("CONV-PERTURBATED");
		new Deconvolution("run", psf + image + algo).deconvolve();
		ComplexSignal H = fft.transform(h);
		ComplexSignal H2 = Operations.multiply(H, H);
		ComplexSignal LP  = ComplexSignalFactory.laplacian(nx, ny, nz);

		algo  = " -algorithm SIM " + (6*noise) + " " + noise + " " + poisson + " " + out("SIM");
		new Deconvolution("run", psf + image + algo).deconvolve();

		algo  = " -algorithm SIM " + (6*noise) + " " + noise + " " + poisson + " " + out("NOISE");
		new Deconvolution("run", impulse + constant + algo).deconvolve();

		// No Noise
		String nonoise = " -image file CONV.tif -psf file psfPerturbated.tif";
		new Deconvolution("run", nonoise + " -algorithm TRIF " + wiener + out("NOISELESS/WIF")).deconvolve();
		new Deconvolution("run", nonoise + " -algorithm NIF -epsilon 1E0 " + out("NOISELESS/NIF0")).deconvolve();
		new Deconvolution("run", nonoise + " -algorithm NIF -epsilon 1E-3  " + out("NOISELESS/NIF-1")).deconvolve();
		new Deconvolution("run", nonoise + " -algorithm NIF -epsilon 1E-6  " + out("NOISELESS/NIF-6")).deconvolve();
		new Deconvolution("run", nonoise + " -algorithm NIF -epsilon 1E-9  " + out("NOISELESS/NIF-9")).deconvolve();
		new Deconvolution("run", nonoise + " -algorithm NIF -epsilon 1E-12  " + out("NOISELESS/NIF-12")).deconvolve();
		new Deconvolution("run", nonoise + " -algorithm DIV " + out("NOISELESS/DIV")).deconvolve();

		// Pertubatation
		String pertubation = " -image file CONV.tif  -psf file psfPerturbated.tif";
		new Deconvolution("run", pertubation + " -algorithm TRIF " + wiener + out("PERTURBATION/WIF")).deconvolve();
		new Deconvolution("run", pertubation + " -algorithm NIF " + out("PERTURBATION/NIF")).deconvolve();
		new Deconvolution("run", pertubation + " -algorithm DIV " + out("PERTURBATION/DIV")).deconvolve();
		
		// Noisy
		String simulation = " -image file SIM.tif " + psf;

		new Deconvolution("run", simulation + " -algorithm TRIF " + wiener + out("SIMULATION/WIF")).deconvolve();
		new Deconvolution("run", simulation + " -algorithm NIF "+ out("SIMULATION/NIF")).deconvolve();
		new Deconvolution("run", simulation + " -algorithm DIV" + out("SIMULATION/DIV")).deconvolve();

		algo  = " -algorithm LW+ 100 0.5 -out mip @1 LW+-ITER/I ";
		new Deconvolution("run", simulation  + algo + out("LW+/LW+")).deconvolve();
		new File(res + "LW+-ITER/I.tif").delete();
		
		for(int i=0; i<=20; i++) {
			double p = Math.pow(5, i-12);
			String name = "RIF/RIF" + String.format("%02d", i);
			new Deconvolution("run", simulation + " -algorithm RIF " + p + out(name)).deconvolve();
			RealSignal fa = fft.inverse(Operations.add(H2, Operations.multiply(p, LP, LP))).circular();
			Lab.save(monitors, fa, res + "RIF-FILTER/RIF" + String.format("%02d", i) + ".tif");
		}
		
		for(int i=0; i<=20; i++) {
			double p = Math.pow(5, i-12);
			String name = "TRIF/TRIF" + String.format("%02d", i);
			new Deconvolution("run", simulation + " -algorithm TRIF " + p + out(name)).deconvolve();
			RealSignal fa = fft.inverse(Operations.add(H2, Operations.multiply(p, LP, LP))).circular();
			Lab.save(monitors, fa, res + "TRIF-FILTER/RIF" + String.format("%02d", i) + ".tif");
		}


		algo  = " -algorithm RL 100 -out mip @1 RL-ITER/I  ";
		new Deconvolution("run", simulation  + algo + out("RL/RL")).deconvolve();
		new File(res + "RL-ITER/I.tif").delete();
		
		algo  = " -algorithm ICTM 100 1.5 0.001 -out mip @1 ICTM-ITER/I  ";
		new Deconvolution("run", simulation  + algo + out("ICTM/ICTM")).deconvolve();
		new File(res + "ICTM-ITER/I.tif").delete();

	}

	private static String out(String name) {
		return " -out stats " + name + 
			 " -out stack " + name + " noshow -out ortho " + name + "-BYTE rescaled byte noshow";
	}
	
	public static void main(String arg[]) {
		new DeconvolutionLab2_Course_SpectralAnaylsis();
	}
	
	@Override
	public void run(String arg) {
		new DeconvolutionLab2_Course_SpectralAnaylsis();
	}

}
