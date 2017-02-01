import java.io.File;

import javax.swing.filechooser.FileSystemView;

import deconvolution.Deconvolution;
import deconvolutionlab.Lab;
import deconvolutionlab.monitor.Monitors;
import ij.plugin.PlugIn;
import signal.RealSignal;
import signal.factory.Cube;

public class DeconvolutionLab2_Course_Noise implements PlugIn {

	private String desktop = FileSystemView.getFileSystemView().getHomeDirectory().getAbsolutePath() + File.separator + "Desktop";
	private String root = desktop + File.separator + "DeconvolutionLab2-Course" + File.separator;
	private String res = root + "Results" + File.separator + "noise" + File.separator;
	
	public DeconvolutionLab2_Course_Noise() {

		Monitors monitors = Monitors.createDefaultMonitor();
				new File(res).mkdir();
		System.setProperty("user.dir", res);
		
		int nx = 560;
		int ny = 120;
		int nz = 1;
		String size = " size " + nx + " " + ny + " " + nz;
			
		RealSignal im = new Cube(50, 0.25).intensity(0, 100).center(0.2, 0.5, 0).generate(nx, ny, nz);
		RealSignal i1 = new Cube(50, 0.25).intensity(0, 70).center(0.4, 0.5, 0).generate(nx, ny, nz);
		RealSignal i2 = new Cube(50, 0.25).intensity(0, 40).center(0.6, 0.5, 0).generate(nx, ny, nz);
		RealSignal i3 = new Cube(50, 0.25).intensity(0, 10).center(0.8, 0.5, 0).generate(nx, ny, nz);
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
		new Deconvolution(psf + image + " -algorithm " + name + out).run();
		
		name = "SIM m 0 s 00 p 150";
		out = " -stack " + name + " -out stack " + name + "-BYTE rescaled byte noshow";
		new Deconvolution(psf + image + " -algorithm " + name + out).run();
		
		name = "SIM m 0 s 15 p 30";
		out = " -stack " + name + " -out stack " + name + "-BYTE rescaled byte noshow";
		new Deconvolution(psf + image + " -algorithm " + name + out).run();
	}
	
	
	public static void main(String arg[]) {
		new DeconvolutionLab2_Course_Noise();
	}
	
	@Override
	public void run(String arg) {
		new DeconvolutionLab2_Course_Noise();
	}

}
