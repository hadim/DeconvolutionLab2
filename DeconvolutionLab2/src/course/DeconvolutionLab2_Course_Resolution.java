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
import deconvolution.algorithm.LandweberPositivity;
import deconvolution.algorithm.NaiveInverseFilter;
import deconvolution.algorithm.RichardsonLucyTV;
import deconvolution.algorithm.Simulation;
import deconvolutionlab.Lab;
import deconvolutionlab.monitor.Monitors;
import deconvolutionlab.output.ShowOrtho;
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
		
		RealSignal x = new CubeSphericalBeads(4, 0.1, spacing, border).intensity(100).generate(nx, ny, nz);
		//RealSignal x = new Sphere(30, 1).generate(nx, ny, nz);
		//RealSignal x = new Constant().intensity(0, 255).generate(nx, ny, nz);
		//Lab.show(monitors, x, "reference");
		//Lab.showOrthoview(x);
		//Lab.showMIP(x);
		Lab.showOrthoview(monitors, x, "Ref", border, border, border);

		//RealSignal h = new Gaussian(3, 3, 1).generate(nx, ny, nz);
		RealSignal h = new Airy(100, 50, 0.5, 0.1).generate(nx, ny, nz);
		Lab.show(monitors, h, "psf");
		//Lab.showOrthoview(h);
		//Lab.showMIP(h);
	
		Convolution convolution = new Convolution();
		convolution.disableDisplayFinal().disableSystem();
		convolution.addOutput(new ShowOrtho("convolution"));
		RealSignal y = convolution.run(x, h);
		
		Simulation simulation = new Simulation(0, 0.25, 0);
		simulation.disableDisplayFinal().disableSystem();
		simulation.addOutput(new ShowOrtho("simualtion").origin(border, border, border));
		RealSignal ys = simulation.run(x, h);
		Lab.showOrthoview(monitors, ys, "Simulation", border, border, border);
		//Lab.show(y);
		
		NaiveInverseFilter nif = new NaiveInverseFilter();
		nif.addOutput(new ShowOrtho("nif").origin(border, border, border));
		nif.disableDisplayFinal().disableSystem().setReference(res + "ref.tif").setStats();
		nif.run(ys, h);
		//Lab.show(nifo);
/*		
		TikhonovRegularizedInverseFilter rif = new TikhonovRegularizedInverseFilter(1e-8);
		rif.disableDisplayFinal().disableSystem().setReference(res + "ref.tif");
		rif.addOutput(new ShowOrtho("trif").origin(border, border, border));
		for(int i=-8; i<=0; i+=1) {
			rif.setParameters(new double[] {Math.pow(10, i)});
			RealSignal t = rif.run(ys, h);
			System.out.println("" + i + " " +Assessment.rmse(t, x));
		}
*/
		
		RichardsonLucyTV rl = new RichardsonLucyTV(100, 0.00001);
		rl.disableDisplayFinal().disableSystem().setReference(res + "ref.tif").setStats();
		rl.addOutput(new ShowOrtho("rltv").frequency(1).origin(border, border, border));
		RealSignal fli = rl.run(ys, h);

//RLTV 0.0001 100		Signals: 167.2 Mb	14.6724	0.9261	n/a
//RL 		100		Signals: 138.6 Mb	14.6688	0.9224	n/a
//RLTV 0.001	100		Signals: 167.2 Mb	14.6979	0.9515	n/a		
//LW+		5000	Signals: 311.6 Mb	15.4276	1.6812	n/a

		LandweberPositivity lw = new LandweberPositivity(100, 1.95);
		lw.disableDisplayFinal().disableSystem().setReference(res + "ref.tif").setStats();
		lw.addOutput(new ShowOrtho("lw").frequency(20).origin(border, border, border));
		RealSignal lwi = lw.run(ys, h);

		Lab.show(lwi);
	}
	
	public static void main(String arg[]) {
		new DeconvolutionLab2_Course_Resolution();
	}
	
	@Override
	public void run(String arg) {
		new DeconvolutionLab2_Course_Resolution();
	}	


}