import deconvolutionlab.Lab;
import deconvolutionlab.Platform;
import deconvolutionlab.monitor.Monitors;
import ij.plugin.PlugIn;

public class DeconvolutionLab2_FFT implements PlugIn {

	@Override
	public void run(String arg) {
		Lab.getInstance(Platform.IMAGEJ);
		Lab.checkFFT(Monitors.createDefaultMonitor());
	}
}
