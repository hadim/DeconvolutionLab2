package plugins.sage.deconvolutionlab;

import java.io.File;

import deconvolutionlab.Imaging;
import deconvolutionlab.Lab;
import deconvolutionlab.Output.View;
import deconvolutionlab.dialog.OutputDialog;
import icy.gui.frame.IcyFrame;
import icy.plugin.abstract_.PluginActionable;

public class Test extends PluginActionable {

	@Override
	public void run() {
		
		Lab.init(Imaging.Platform.ICY, System.getProperty("user.dir") + File.separator + "DeconvolutionLab2.config");
		IcyFrame icf = new IcyFrame();
		icf.add(new OutputDialog(View.FIGURE).getContentPane());	
		icf.pack();
		icf.toFront();
		icf.addToDesktopPane();
		icf.setVisible(true);
		//icf.setResizable(true);

		
	}

}
