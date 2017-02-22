package deconvolution.modules;

import javax.swing.JSplitPane;

import deconvolution.Deconvolution;
import deconvolution.algorithm.AbstractAlgorithmPanel;
import deconvolution.algorithm.Algorithm;
import lab.component.CustomizedTable;
import lab.component.HTMLPane;

public class AlgorithmDModule extends AbstractDModule {

	private CustomizedTable	table;
	private HTMLPane		doc;

	public AlgorithmDModule(Deconvolution deconvolution) {
		super(deconvolution);
		doc = new HTMLPane(100, 1000);
		table = new CustomizedTable(new String[] { "Features", "Values" }, false);
		split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, table.getPane(200, 200), doc.getPane());
		update();
	}

	public void update() {
		if (doc == null)
			return;
		if (table == null)
			return;
		table.removeRows();
		for (String[] feature : deconvolution.checkAlgo())
			table.append(feature);
		
		doc.clear();
		String name = deconvolution.getAlgo().getShortname();
		AbstractAlgorithmPanel algo = Algorithm.getPanel(name);
		if (algo != null)
			doc.append(algo.getDocumentation());
		split.setDividerLocation(300);
	}

	@Override
	public String getName() {
		return "Algorithm";
	}

}