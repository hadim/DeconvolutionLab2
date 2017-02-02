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
	private String root = desktop + File.separator + "DeconvolutionLab2-Course" + File.separator;
	private String res = root + "Results" + File.separator + "resolution" + File.separator;
	
	public DeconvolutionLab2_Course_Resolution() {

		Monitors monitors = Monitors.createDefaultMonitor();
		new File(res).mkdir();
		System.setProperty("user.dir", res);
					
		new File(res + "RIF").mkdir();
		new File(res + "LW").mkdir();
		new File(res + "LW+").mkdir();
	
		int nx = 128;
		int ny = 128;
		int nz = 128;
		int spacing = 16;
		
		RealSignal x = new GridSpots(3, 1, spacing).intensity(0, 10).generate(nx, ny, nz);
		Lab.show(monitors, x, "reference");
		Lab.save(monitors, x, res + "reference.tif");

		String algo  = " ";
		String ground = " -image file reference.tif ";
		String psf   = " -psf file ../../Data/resolution/psf-bw-500.tif ";
		//String psf = " -psf synthetic gaussian 100.0 0.0 1 1 1 size " + nx + " " + ny + " " + nz;
		String signal  = " -image file signal.tif -reference reference.tif -monitor table";
		
		String paramout = " intact float  (" + spacing + "," + spacing + "," + spacing + ")";
		
		algo  = " -algorithm CONV  -showstats @3 PR -out stack PR -out ortho PRo ";
		new Deconvolution(ground + "-reference reference.tif -psf synthetic impulse 100 0 size 128 128 128 " + algo).deconvolve(false);

		algo  = " -algorithm SIM 0 0.1 0  -showstats @3 SIM -out stack SIGNAL -out ortho SIGNALo ";
		new Deconvolution(ground + psf + algo).deconvolve(false);

		algo  = " -algorithm NIF -out ortho NIF " + paramout;
		new Deconvolution(signal + psf + algo).deconvolve(false);

		algo  = " -algorithm LW+ 17 1 -showstats @3 LW  -out ortho @10 LW+/LW+" + paramout;
		new Deconvolution(signal + psf + algo).deconvolve(false);

		for(int i=0; i<=15; i++) {
			double p = Math.pow(10, i-15);
			algo  = " -algorithm RIF " + p + " -out ortho RIF/RIF" + String.format("%02d", i) + paramout;
			new Deconvolution(signal + psf + algo).deconvolve(false);
		}
	
		
		algo  = " -algorithm LW 11 1 -showstats @3 LW  -out ortho @10 LW/LW" + paramout;
		new Deconvolution(signal + psf + algo).deconvolve(false);
		
	
	}
	
	public static void main(String arg[]) {
		new DeconvolutionLab2_Course_Resolution();
	}
	
	@Override
	public void run(String arg) {
		new DeconvolutionLab2_Course_Resolution();
	}	


}