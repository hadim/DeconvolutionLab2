package deconvolution.modules;

import java.awt.Dimension;
import java.awt.image.BufferedImage;

import javax.swing.JSplitPane;

import deconvolution.Deconvolution;
import deconvolutionlab.Lab;
import deconvolutionlab.monitor.Monitors;
import lab.component.CustomizedTable;
import lab.component.JPanelImage;
import signal.Constraint;
import signal.RealSignal;

public class PSFDModule extends AbstractDModule {

	private JPanelImage pnImage;
	private CustomizedTable table;

	public PSFDModule(Deconvolution deconvolution) {
		super(deconvolution);
		pnImage = new JPanelImage();
		table = new CustomizedTable(new String[] { "Features", "Values" }, false);
		pnImage.setPreferredSize(new Dimension(300, 300));
		split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, table.getPane(300, 300), pnImage);
		update();
	}
	
	public void update() {
		split.setDividerLocation(300);
		if (pnImage == null)
			return;
		if (table == null)
			return;
		RealSignal signal = deconvolution.openPSF();

		table.removeRows();
		if (signal == null) {
			table.append(new String[] {"ERROR", "No PSF image"});
			return;
		}
		BufferedImage img = signal.createPreviewMIPZ();
		pnImage.setImage(img);
		for (String[] feature : deconvolution.checkPSF(signal))
			table.append(feature);
	}

	@Override
	public String getName() {
		return "PSF";
	}

}