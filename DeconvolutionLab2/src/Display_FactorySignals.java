import java.io.File;
import java.util.ArrayList;

import bilib.table.CustomizedTable;
import bilib.tools.Files;
import deconvolution.algorithm.Convolution;
import deconvolutionlab.Lab;
import deconvolutionlab.monitor.Monitors;
import fft.AbstractFFT;
import fft.FFT;
import ij.plugin.PlugIn;
import signal.ComplexSignal;
import signal.RealSignal;
import signal.factory.SignalFactory;
import signal.factory.Sphere;
import signal.factory.complex.ComplexSignalFactory;

public class Display_FactorySignals implements PlugIn {

	private String path = Files.getDesktopDirectory() + File.separator + "Deconvolution" + File.separator + "Signals" + File.separator;
	
	@Override
    public void run(String arg0) {
    }
	
	public static void main(String arg[]) {
	    new Display_FactorySignals();
    }
	
	public Display_FactorySignals() {
		new File(path).mkdir();
		int nx = 128;
		int ny = 96;
		int nz = 100;

		ArrayList<SignalFactory> factories = SignalFactory.getAll();
		RealSignal image = new Sphere(20, 0.5).generate(nx, ny, nz);
		Monitors monitors = Monitors.createDefaultMonitor();
		CustomizedTable table = new CustomizedTable(new String[] {"Name", "mean", "min", "max"}, true);
		table.show("Stats", 500, 500);
		for(SignalFactory factory : factories) {
			RealSignal psf = factory.intensity(133).generate(nx, ny, nz);
			Lab.show(psf); 
			float s[] = psf.getStats();
			table.append(new String[] {factory.getName(), ""+s[0], ""+s[1], ""+s[2]});
			Lab.showOrthoview(psf);
			Lab.save(monitors, psf, path + psf.name + ".tif");
			Lab.save(monitors, psf.createMIP(), path + "mip-" + psf.name + ".tif");
			Lab.save(monitors, psf.createOrthoview(), path + "ortho-" + psf.name + ".tif");
			Convolution convolution = new Convolution();
			RealSignal a = convolution.run(image, psf);
			Lab.showMIP(monitors, a, "conv " + factory.getName());
			Lab.save(monitors, a, path + "conv-"+psf.name + ".tif");
			Lab.save(monitors, a.createMIP(), path + "conv-mip-" + psf.name + ".tif");
			Lab.save(monitors, a.createOrthoview(), path + "conv-ortho-" + psf.name + ".tif");
		}
	}
	
	public void d() {
		int nx = 150;
		int ny = 128;
		int nz = 100;
		int xsize = nx / 2;
		int ysize = ny / 2;
		int zsize = nz / 2;
		double wx, wy, wz;
		RealSignal psf = new RealSignal("psf", nx, ny, nz);
		AbstractFFT fft = FFT.getFastestFFT().getDefaultFFT();
		fft.init(Monitors.createDefaultMonitor(), nx, ny, 1);
		double pupil = 10;
		double defocus = 10;
		double wave = 2;

		double defocusTop = 2.0*Math.PI / (defocus*defocus*pupil);
		double defocusCen = 2.0*Math.PI / pupil;
		
		for (int z = 0; z <= zsize; z++) {
			float[][][] real = new float[xsize + 1][ysize + 1][1];
			float[][][] imag = new float[xsize + 1][ysize + 1][1];
			wz = wave*(z-zsize)*2.0*Math.PI / zsize;
			double cosz = Math.cos(wz);
			double sinz = Math.sin(wz);
			double fcz =  z * Math.abs(defocusTop-defocusCen) / zsize+ defocusTop;
			for (int y = 0; y <= ysize; y++)
			for (int x = 0; x <= xsize; x++) {
				wx = Math.PI * x / xsize;
				wy = Math.PI * y / ysize;
				double g = wy*wy+wx*wx >= fcz*fcz ? 0 : 1;
				real[x][y][0] = (float) (g * cosz);
				imag[x][y][0] = (float) (g * sinz);
			}
			ComplexSignal c = ComplexSignalFactory.createHermitian(""+z, nx, ny, 1, real, imag);
			RealSignal pz = fft.inverse(c).circular();
			
			//pz.plus(1);
			//pz.normalize(1);
			psf.setXY(z, pz.getXY(0));
			psf.setXY(nz-1-z, pz.duplicate().getXY(0));
		}
		//psf = new Gaussian(3,4,4).generate(nx, ny, nz);
		float min = psf.getExtrema()[0];
		psf.minus(min);
		float max = psf.getExtrema()[1];
		psf.times(1/max);
		float aft[] = psf.getExtrema();
		System.out.println(" // " + aft[0] + " // " + aft[1]);
		psf.normalize(1);
		Lab.show(psf);
		Lab.showOrthoview(psf);
		Lab.save(Monitors.createDefaultMonitor(), psf, "psfdiffraction.tif");
		/*
		fft.init(Monitors.createDefaultMonitor(), nx, ny, nz);
		ComplexSignal otf = fft.transform(psf);
		Lab.show(otf.getModule().circular().log());
		Lab.showOrthoview(otf.getModule().circular().log());

		
		RealSignal r = new Cube(30, 1).generate(nx, ny, nz);
		RealSignal a  = new Convolution().run(r, psf);
		Lab.showOrthoview(a);
		*/
	}
	public void c() {
		int nx = 128;
		int ny = 128;
		int nz = 128;
		RealSignal otf = new RealSignal("psf", nx, ny, nz);
		
		int cx = nx/2;
		int cy = ny/2;
		int cz = nz/2;
		double pi = Math.PI;
		double periodTop = 5;
		double periodCenter = 15;
		double attenuation = 10;
		double aperture = 60;
		double apernorm = (2.0*aperture)/(nx+ny);
		double diag = Math.sqrt(nx*nx+ny*ny+nz*nz);
		double step = (periodCenter-periodTop)/nz; 
		for(int i=0; i<nx; i++)
		for(int j=0; j<ny; j++) {
			double r = Math.sqrt((i-cx)*(i-cx)+(j-cy)*(j-cy));
			for(int k=0; k<nx; k++) {
				double z = Math.abs(k-cz);
				double p = Math.sqrt(r*r + z*z)/diag;
				double period = Math.max(1, periodCenter-step*z);
				double sz = apernorm*z + period*0.25;
				double s1 = 1.0 / (1.0+Math.exp(-(r+sz)));
				double s2 = 1.0 / (1.0+Math.exp(-(r-sz)));
				double s = Math.cos(2*pi*(r-apernorm*z)/period);
				double g = (attenuation*p+1);
				otf.data[k][i+j*nx] = (float)((s1-s2)*s*s/g);
			}
		}
		Lab.show(otf);
		Lab.showOrthoview(otf, "out", cx, cy, cz);
		float[] stats = otf.getStats();
		for(float s : stats)
			System.out.println("" + s);
		
		AbstractFFT fft = FFT.getFastestFFT().getDefaultFFT();
		fft.init(Monitors.createDefaultMonitor(), nx, ny, nz);
		ComplexSignal psf = fft.transform(otf);
		float[] stats1 = psf.getModule().getStats();
		for(float s : stats1)
			System.out.println("PSF " + s);
		Lab.show(psf.getModule().circular().log());
		Lab.showOrthoview(psf.getModule().circular().log(), "out", cx, cy, cz);
	}
	
	public void b() {
		int nx = 128;
		int ny = 128;
		int nz = 128;
		RealSignal otf = new RealSignal("psf", nx, ny, nz);
		
		int cx = nx/2;
		int cy = ny/2;
		int cz = nz/2;
		
		for(int i=0; i<nx; i++)
		for(int j=0; j<ny; j++) {
			double r = Math.sqrt((i-cx)*(i-cx)+(j-cy)*(j-cy));
			for(int k=0; k<nx; k++) {
				double df = Math.abs(k-cz);
				double dd = df - 6;
				double sig = 3 + df * 0.2;
				double sigd = 0.1 + dd * 0.2;
				double norm = 1.0 / (Math.PI*sig*sig);
				double g = norm * Math.exp(-r*r/(2.0*sig*sig));
				double gd = (dd < 0 ? 0 : norm * Math.exp(-r*r/(2*sigd*sigd)));
				otf.data[k][i+j*nx] = (float)(g-gd); //(Math.exp(-r*0.1));
			}
		}
		Lab.show(otf);
		Lab.showOrthoview(otf, "out", cx, cy, cz);
	
		float[] stats = otf.getStats();
		for(float s : stats)
			System.out.println("" + s);
		
		AbstractFFT fft = FFT.getFastestFFT().getDefaultFFT();
		fft.init(Monitors.createDefaultMonitor(), nx, ny, nz);
		ComplexSignal psf = fft.transform(otf);
		float[] stats1 = psf.getModule().getStats();
		for(float s : stats1)
			System.out.println("PSF " + s);
		Lab.show(Monitors.createDefaultMonitor(), psf.getModule().circular(), "psf");
	}	
	
	public void va() {
		int nx = 128;
		int ny = 128;
		int nz = 128;
		RealSignal otf = new RealSignal("psf", nx, ny, nz);
		
		int cx = nx/2;
		int cy = ny/2;
		int cz = nz/2;
		double p = 1.3;
		for(int i=0; i<nx; i++)
		for(int j=0; j<ny; j++) {
			double d = Math.sqrt((i-cx)*(i-cx) + (j-cy)*(j-cy));
			double v = (p-Math.exp(-d*0.1));
			for(int k=0; k<nx; k++) {				
				double dz = ((double)Math.abs(k-cz))/nz;
				double g = Math.exp(-dz*dz*2*nx);
				double vz = g*v*Math.cos(d*Math.PI*2/nx);
				if (vz > 0) {
					double r = Math.sqrt((i-cx)*(i-cx) + (j-cy)*(j-cy) + (k-cz)*(k-cz));
					if (r < cx) 
					otf.data[k][i+j*nx] = (float)vz; //(Math.exp(-r*0.1));
				
				}
			}
		}
		Lab.show(otf);
		Lab.showOrthoview(otf, "out", cx, cy, cz);
	
		float[] stats = otf.getStats();
		for(float s : stats)
			System.out.println("" + s);
		
		AbstractFFT fft = FFT.getFastestFFT().getDefaultFFT();
		fft.init(Monitors.createDefaultMonitor(), nx, ny, nz);
		ComplexSignal psf = fft.transform(otf);
		float[] stats1 = psf.getModule().getStats();
		for(float s : stats1)
			System.out.println("PSF " + s);
		Lab.show(Monitors.createDefaultMonitor(), psf.getModule().circular(), "psf");
	
	}

}
