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
	private String data = root + "Data" + File.separator + "resolution" + File.separator;
	
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
		
		algo  = " -algorithm CONV  -showstats @3 PR -out stack PR -out ortho PRo ";
		new Deconvolution(ground + "-reference reference.tif -psf synthetic impulse 100 0 size 128 128 128 " + algo).deconvolve(false);

		algo  = " -algorithm SIM 0 1.5 0  -showstats @3 SIM -out stack signal -out ortho SIGNALo ";
		new Deconvolution(ground + psf + algo).deconvolve(false);

		algo  = " -algorithm NIF -out ortho NIF " + paramout;
		new Deconvolution(signal + psf + algo).deconvolve(false);

		for(int i=0; i<=24; i++) {
			double p = Math.pow(10, i-18);
			algo  = " -algorithm RIF " + p + " -out ortho @5 RIF/RIF" + i + paramout;
			new Deconvolution(signal + psf + algo).deconvolve(false);
		}
		
		algo  = " -algorithm LW+ 305 1 -showstats @3 LW+  -out ortho @25 LW+/LW+" + paramout;
		new Deconvolution(signal + psf + algo).deconvolve(false);
		
//		algo  = " -algorithm LW 205 1 -showstats @3 LW  -out ortho @25 LW/LW" + paramout;
//		new Deconvolution(signal + psf + algo).deconvolve(false);
		
//	algo  = " -algorithm RL 205 1 -showstats @3 RL -constraint Non-negativity -out ortho @25 RL/RL" + paramout;
//		new Deconvolution(signal + psf + algo).deconvolve(false);
		
	}
	
	public static void main(String arg[]) {
		new DeconvolutionLab2_Course_Resolution();
	}
	
	@Override
	public void run(String arg) {
		new DeconvolutionLab2_Course_Resolution();
	}	


}