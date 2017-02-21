package deconvolution.modules;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import deconvolution.Deconvolution;
import lab.component.CustomizedTable;
import lab.component.JPanelImage;

public class AlgorithmDModule extends AbstractDModule {

	private JPanelImage pnImage;
	private CustomizedTable table;
	

	public AlgorithmDModule(Deconvolution deconvolution) {
		super(deconvolution);
		pnImage = new JPanelImage();
		table = new CustomizedTable(new String[] { "Features", "Values" }, false);
		split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, table.getPane(200, 200), pnImage);
		update();
	}
	
	public void update() {
		if (pnImage == null)
			return;
		if (table == null)
			return;
		table.removeRows();
		for (String[] feature : deconvolution.checkAlgo())
			table.append(feature);
		split.setDividerLocation(0.5);
		split.repaint();
	}

	@Override
	public String getName() {
		return "Algorithm";
	}


}