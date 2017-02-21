package deconvolution.modules;

import java.awt.image.BufferedImage;

import javax.swing.JSplitPane;

import deconvolution.Deconvolution;
import lab.component.CustomizedTable;
import lab.component.JPanelImage;
import signal.RealSignal;

public class ReportDModule extends AbstractDModule {

	private JPanelImage pnImage;
	private CustomizedTable table;

	public ReportDModule(Deconvolution deconvolution) {
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
		table.removeRows();
		for (String[] feature : deconvolution.getDeconvolutionReports())
			table.append(feature);
		split.setDividerLocation(0.5);
		split.repaint();
	}

	@Override
	public String getName() {
		return "Report";
	}


}