import ij.plugin.PlugIn;
import deconvolutionlab.Lab;
import deconvolutionlab.Platform;

public class DeconvolutionLab2_Help implements PlugIn {

	@Override
	public void run(String arg) {
		Lab.getInstance(Platform.IMAGEJ);
		Lab.help();
	}
}
