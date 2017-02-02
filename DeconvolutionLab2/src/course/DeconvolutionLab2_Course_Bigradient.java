package course;
import ij.plugin.PlugIn;

import java.io.File;

import javax.swing.filechooser.FileSystemView;

import deconvolution.Deconvolution;
import deconvolutionlab.monitor.Monitors;


public class DeconvolutionLab2_Course_Bigradient implements PlugIn {

	private String desktop = FileSystemView.getFileSystemView().getHomeDirectory().getAbsolutePath() + File.separator + "Desktop";
	private String root = desktop + File.separator + "DeconvolutionLab2-Course" + File.separator;
	private String res = root + "Results" + File.separator + "bigradient" + File.separator;
	private String data = root + "Data" + File.separator + "bigradient" + File.separator;
	
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
	
		new Deconvolution("-image file " + data + "ref.tif" + psf + " -algorithm SIM 0 1 1 -out stack convnoise -out stack conbnoise_8 rescaled byte noshow").deconvolve(false);
	
		new Deconvolution(noisy + psf + " -algorithm NIF -out stack NIF").deconvolve(false);
		new Deconvolution(noisy + psf + " -algorithm DIV -out stack DIV").deconvolve(false);
		
		for(int i=0; i<=3; i++) {
			double p = Math.pow(5, i-10);
			String name = "RIF" + String.format("%02d", i);
			new Deconvolution(noisy + psf + " -algorithm RIF " + p + out("RIF" + File.separator, name)).deconvolve(false);
		}
		for(int i=0; i<=3; i++) {
			double p = Math.pow(5, i-10);
			String name = "TRIF" + String.format("%02d", i);
			new Deconvolution(noisy + psf + " -algorithm TRIF " + p + out("TRIF" + File.separator, name)).deconvolve(false);
		}

		String lw  = " -algorithm LW 20 1 -out mip @2 LW-ITER/I -showstats @1 LW";
		new Deconvolution(noisy  + psf + lw).deconvolve(false);
		new File(res + "LW-ITER/I.tif").delete();
		
		
		String lwp  = " -algorithm LW+ 20 1 -out mip @2 LW+-ITER/I -showstats @1 LW+";
		new Deconvolution(noisy  + psf + lwp).deconvolve(false);
		new File(res + "LW+-ITER/I.tif").delete();


		String rl  = " -algorithm RL 20 -out mip @2 RL-ITER/I -showstats @1 RL";
		new Deconvolution(noisy  + psf + rl).deconvolve(false);
		new File(res + "RL-ITER/I.tif").delete();

		String rltv  = " -algorithm RLRV 20 10 -out mip @2 RLTV-ITER/I -showstats @1 RLTV";
		new Deconvolution(noisy  + psf + rltv).deconvolve(false);
		new File(res + "RLTV-ITER/I.tif").delete();
		
		String fista  = " -algorithm FISTA 20 1 1 Spline3 3 -mip @2 FISTA-ITER/I -showstats @1 FISTA";
		new Deconvolution(noisy + psf + fista).deconvolve(false);
		new File(res + "FISTA-ITER/I.tif").delete();

	}

	private static String out(String root, String name) {
		return "showstats " + root + name  + " -savestats  " + root + name  + 
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
