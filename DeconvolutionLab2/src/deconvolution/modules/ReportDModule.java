package deconvolution.modules;

import java.awt.Dimension;
import java.awt.image.BufferedImage;

import javax.swing.JSplitPane;

import deconvolution.Deconvolution;
import lab.component.CustomizedTable;
import lab.component.JPanelImage;
import signal.RealSignal;

public class ReportDModule extends AbstractDModule {

	private JPanelImage pnImage;
	private CustomizedTable table;
	private BufferedImage img;
	
	public ReportDModule(Deconvolution deconvolution) {
		super(deconvolution);
		pnImage = new JPanelImage();
		table = new CustomizedTable(new String[] { "Output", "Values" }, false);
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
		table.removeRows();
		
		for (String[] feature : deconvolution.getDeconvolutionReports())
			table.append(feature);
		RealSignal signal = deconvolution.getOutput();
		if (signal == null) {
			table.append(new String[] {"ERROR", "No output image"});
			return;
		}
		img = signal.createPreviewMIPZ();
		pnImage.setImage(img);

		for (String[] feature : deconvolution.checkOutput())
			table.append(feature);
		split.setDividerLocation(300);
	}

	@Override
	public String getName() {
		return "Report";
	}
}
