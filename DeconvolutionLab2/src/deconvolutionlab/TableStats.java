package deconvolutionlab;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableModel;

import bilib.table.CustomizedColumn;
import bilib.table.CustomizedTable;
import deconvolutionlab.monitor.Monitors;

public class TableStats {

	private JPanel					panel;
	private CustomizedTable			table;
	private String 					name;
	private boolean					save;
	private String					path;

	public TableStats(String name, int width, int height, String path, boolean save) {
		this.name = name;
		this.save = save;
		this.path = path;
		ArrayList<CustomizedColumn> columns = new ArrayList<CustomizedColumn>();
		columns.add(new CustomizedColumn("Iterations", String.class, 100, false));
		columns.add(new CustomizedColumn("Mean", String.class, 100, false));
		columns.add(new CustomizedColumn("Minimum", String.class, 100, false));
		columns.add(new CustomizedColumn("Maximum", String.class, 100, false));
		columns.add(new CustomizedColumn("Stdev", String.class, 100, false));
		columns.add(new CustomizedColumn("Energy", String.class, 100, false));
		columns.add(new CustomizedColumn("Time", String.class, 100, false));
		columns.add(new CustomizedColumn("Memory", String.class, 100, false));
		columns.add(new CustomizedColumn("Signal", String.class, 100, false));
		columns.add(new CustomizedColumn("PSNR", String.class, 100, false));
		columns.add(new CustomizedColumn("SNR", String.class, 100, false));
		columns.add(new CustomizedColumn("Residu", String.class, 100, false));
		table = new CustomizedTable(columns, true);

		JScrollPane scroll = new JScrollPane(table);
		scroll.setPreferredSize(new Dimension(width, height));
		JPanel main = new JPanel(new BorderLayout());

		main.add(scroll, BorderLayout.CENTER);
		panel = new JPanel(new BorderLayout());
		panel.add(main);
		panel.setBorder(BorderFactory.createEtchedBorder());
	}
		
	public String getName() {
		return name;
	}

	public void clear() {
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		model.setRowCount(0);
	}

	public void nextStats(Monitors monitors, String[] stats) {
		if (table == null)
			return;
		if (stats == null)
			return;
		table.append(stats);
		monitors.log("Stats ");
	}

	public void lastStats(Monitors monitors, String[] stats) {
		if (stats == null)
			return;
		if (table == null)
			return;
		if (save) {
			String filename = path + File.separator + name + ".csv";
			monitors.log("Stats save " + filename);
			table.saveCSV(filename);
		}
	}

	public JPanel getPanel() {
		return panel;
	}

}
