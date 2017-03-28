import ij.plugin.PlugIn;
import signal.ComplexComponent;
import signal.ComplexSignal;
import signal.RealSignal;
import deconvolutionlab.Lab;
import deconvolutionlab.monitor.Monitors;
import fft.AbstractFFT;
import fft.FFT;


public class Test_Airy implements PlugIn {

	@Override
    public void run(String arg0) {
    }
	
	public static void main(String arg[]) {
	    new Test_Airy();
    }
	
	public Test_Airy() {
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
