import java.io.File;

import deconvolutionlab.Config;
import deconvolutionlab.Lab;
import deconvolutionlab.Platform;
import deconvolutionlab.dialog.LabDialog;
import ij.IJ;
import ij.plugin.PlugIn;

public class DeconvolutionLab2_Lab implements PlugIn {

	@Override
	public void run(String arg) {
		Lab.getInstance(Platform.IMAGEJ);
		String config = IJ.getDirectory("plugins") + File.separator + "DeconvolutionLab2.config"; 
		Config.getInstance(config);
		new LabDialog().setVisible(true);
	}
}
