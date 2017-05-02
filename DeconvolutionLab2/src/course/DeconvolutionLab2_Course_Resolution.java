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
import deconvolution.algorithm.Convolution;
import deconvolution.algorithm.NaiveInverseFilter;
import deconvolution.algorithm.Simulation;
import deconvolution.algorithm.TikhonovRegularizedInverseFilter;
import deconvolutionlab.Lab;
import deconvolutionlab.monitor.Monitors;
import deconvolutionlab.output.ShowOrtho;
import ij.gui.Plot;
import ij.plugin.PlugIn;
import signal.Assessment;
import signal.RealSignal;
import signal.factory.BesselJ0;
import signal.factory.CubeSphericalBeads;

public class DeconvolutionLab2_Course_Resolution implements PlugIn {

	private String root = Files.getDesktopDirectory() + "Deconvolution" + File.separator;
	private String path = root + "results" + File.separator + "resolution" + File.separator;
	
	public DeconvolutionLab2_Course_Resolution() {

		Monitors monitors = Monitors.createDefaultMonitor();
		new File(path).mkdir();
		System.setProperty("user.dir", path);
					
		new File(path + "RIF").mkdir();
		new File(path + "LW").mkdir();
		new File(path + "LW+").mkdir();
		new File(path + "RL").mkdir();
	
		int nx = 160;
		int ny = 160;
		int nz = 160;
		int spacing = 8;
		int b = 8;
		
		RealSignal x = new CubeSphericalBeads(2, 0.5, spacing, b).intensity(100).generate(nx, ny, nz);
		x.plus(10);
		Lab.save(monitors, x.createOrthoview(b, b, b), path + "ref.tif");
System.out.println("mean x " + x.getStats()[0]);
		RealSignal h = new BesselJ0(1, 10, 0.001, 0.00000001).generate(nx, ny, nz);
		Lab.save(monitors, h, path + "psf.tif");
		Lab.show(monitors, h, "psf");
		Lab.showOrthoview(h);
		Lab.showMIP(h);
	
		Convolution convolution = new Convolution();
		convolution.disableDisplayFinal().disableSystem();
		convolution.addOutput(new ShowOrtho("convolution"));
		RealSignal y = convolution.run(x, h);
		Lab.save(monitors, y.createOrthoview(b, b, b), path + "conv.tif");
		Lab.showPlanar(y);
System.out.println("mean y " + y.getStats()[0]);	

		Simulation simulation = new Simulation(0, 0.25, 0.25);
		simulation.disableDisplayFinal().disableSystem();
		simulation.addOutput(new ShowOrtho("simualtion").origin(b, b, b));
		RealSignal ys = simulation.run(x, h);
		Lab.save(monitors, ys.createOrthoview(b, b, b), path + "simu.tif");		
		Lab.showPlanar(ys);
		Lab.showMIP(ys);
System.out.println("mean ys " + ys.getStats()[0]);	
		
	
		plotProfile(x, "refY", b, 0, b, b, ny-1, b);
		plotProfile(x, "refX", 0, b, b, nx-1, b, b);
		plotProfile(x, "refZ", b, b, 0, b, b, nz-1);
		
		plotProfile(y, "convY", b, 0, b, b, ny-1, b);
		plotProfile(y, "convX", 0, b, b, nx-1, b, b);
		plotProfile(y, "convZ", b, b, 0, b, b, nz-1);
		plotProfile(ys, "simuY", b, 0, b, b, ny-1, b);
		plotProfile(ys, "simuX", 0, b, b, nx-1, b, b);
		plotProfile(ys, "simuZ", b, b, 0, b, b, nz-1);
	
		NaiveInverseFilter nif = new NaiveInverseFilter();
		nif.addOutput(new ShowOrtho("nif").origin(b, b, b));
		nif.disableDisplayFinal().disableSystem().setReference(path + "ref.tif").setStats();
		RealSignal nic = nif.run(y, h);
		Lab.save(monitors, nic.createOrthoview(b, b, b), path + "nif_conv.tif");
		Lab.showPlanar(monitors, nic, nic + "NIF_CONV //"+Assessment.rmse(nic, x));
		RealSignal nis = nif.run(ys, h);
		Lab.save(monitors, nis.createOrthoview(b, b, b), path + "nif_simu.tif");
		Lab.showPlanar(monitors, nis, nis + "NIF_SIMU //"+Assessment.rmse(nis, x));
		
		plotProfile(nic, "nicY", b, 0, b, b, ny-1, b);
		plotProfile(nic, "nicX", 0, b, b, nx-1, b, b);
		plotProfile(nic, "nicZ", b, b, 0, b, b, nz-1);

		plotProfile(nis, "nisY", b, 0, b, b, ny-1, b);
		plotProfile(nis, "nisX", 0, b, b, nx-1, b, b);
		plotProfile(nis, "nisZ", b, b, 0, b, b, nz-1);

		TikhonovRegularizedInverseFilter trif = new TikhonovRegularizedInverseFilter(1e-3);
		trif.disableDisplayFinal().disableSystem().setReference(path + "ref.tif");
		for(int i=-5; i<-1; i++) {
			trif.setParameters(new double[] {Math.pow(10, i)});
			RealSignal t = trif.run(ys, h);
			Lab.save(monitors, t.createOrthoview(b, b, b), path + "trif" + i + ".tif");
			Lab.showPlanar(monitors, t, "TRIF" + i + " //"+Assessment.rmse(t, x));
			Lab.showOrthoview(monitors, t, "TRIF"+ i + " //"+Assessment.rmse(t, x), b, b, b);
			plotProfile(t, "tirfY" + i, b, 0, b, b, ny-1, b);
			plotProfile(t, "tirfX" + i, 0, b, b, nx-1, b, b);
			plotProfile(t, "tirfZ" + i, b, b, 0, b, b, nz-1);
			System.out.println("mean trif " + i + " "  + t.getStats()[0]);	
		}
		//plotProfile(t, "trifY", b, 0, b, b, ny-1, b);
		//plotProfile(t, "trifX", 0, b, b, nx-1, b, b);
		//plotProfile(t, "trifZ", b, b, 0, b, b, nz-1);
	/*
		RichardsonLucyTV rl = new RichardsonLucyTV(100, 0.00001);
		rl.disableDisplayFinal().disableSystem().setReference(res + "ref.tif").setStats();
		rl.addOutput(new ShowOrtho("rltv").frequency(1).origin(b, b, b));
		RealSignal fli = rl.run(ys, h);

//RLTV 0.0001 100		Signals: 167.2 Mb	14.6724	0.9261	n/a
//RL 		100		Signals: 138.6 Mb	14.6688	0.9224	n/a
//RLTV 0.001	100		Signals: 167.2 Mb	14.6979	0.9515	n/a		
//LW+		5000	Signals: 311.6 Mb	15.4276	1.6812	n/a
/*
		LandweberPositivity lw = new LandweberPositivity(100, 1.95);
		lw.disableDisplayFinal().disableSystem().setReference(res + "ref.tif").setStats();
		lw.addOutput(new ShowOrtho("lw").frequency(20).origin(border, border, border));
		RealSignal lwi = lw.run(ys, h);

		Lab.show(lwi);
		*/
	}
	
	private static void plotProfile(RealSignal signal, String name, int x1, int y1, int z1, int x2, int y2, int z2) {
		double dx = x2 - x1;
		double dy = y2 - y1;
		double dz = z2 - z1;
		double len = Math.sqrt(dx*dx + dy*dy + dz*dz);
		int n = (int)Math.round(len * 2);
		double ds = len / n;
		dx = (double)(x2 - x1) / n;
		dy = (double)(y2 - y1) / n;
		dz = (double)(z2 - z1) / n;
		double value[] = new double[n];
		double dist[] = new double[n];
		for(int s=0; s<n; s++) {
			double x = x1 + s*dx;
			double y = y1 + s*dy;
			double z = z1 + s*dz;
			dist[s] = s*ds;
			value[s] = signal.getInterpolatedPixel(x, y, z);
		}
		Plot plot = new Plot(name, "distance", "intensity", dist, value);
		plot.show();
	}
	
	public static void main(String arg[]) {
		new DeconvolutionLab2_Course_Resolution();
	}
	
	@Override
	public void run(String arg) {
		new DeconvolutionLab2_Course_Resolution();
	}	


}