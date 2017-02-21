package deconvolution.modules;

import java.awt.image.BufferedImage;

import javax.swing.JSplitPane;

import deconvolution.Deconvolution;
import lab.component.CustomizedTable;
import lab.component.JPanelImage;
import signal.RealSignal;

public class PSFDModule extends AbstractDModule {

	private JPanelImage pnImage;
	private CustomizedTable table;

	public PSFDModule(Deconvolution deconvolution) {
		super(deconvolution);
		pnImage = new JPanelImage();
		table = new CustomizedTable(new String[] { "Features", "Values" }, false);
		split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, table.getPane(200, 200), pnImage);
	}
	
	public void update() {
		if (pnImage == null)
			return;
		if (table == null)
			return;
		RealSignal signal = deconvolution.openPSF();
		table.removeRows();
		if (signal == null) {
			table.append(new String[] {"ERROR", "No open image"});
			return;
		}
		BufferedImage img = signal.createPreviewMIPZ();
		pnImage.setImage(img);
		for (String[] feature : deconvolution.checkPSF())
			table.append(feature);
		split.setDividerLocation(0.5);
		split.repaint();
	}

	@Override
	public String getName() {
		return "PSF";
	}

}