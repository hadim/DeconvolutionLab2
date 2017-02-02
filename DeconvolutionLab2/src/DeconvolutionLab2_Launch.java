import java.io.File;

import deconvolution.Deconvolution;
import deconvolutionlab.Config;
import deconvolutionlab.Lab;
import deconvolutionlab.Platform;
import deconvolutionlab.dialog.LabDialog;
import ij.IJ;
import ij.Macro;
import ij.plugin.PlugIn;

public class DeconvolutionLab2_Launch implements PlugIn {

	@Override
	public void run(String arg) {
		Lab.getInstance(Platform.IMAGEJ);
		String config = IJ.getDirectory("plugins") + File.separator + "DeconvolutionLab2.config"; 
		Config.getInstance(config);
		if (Macro.getOptions() == null)
			new LabDialog().setVisible(true);
		else
			new Deconvolution(Macro.getOptions()).launch("", false);
	}
}
