package deconvolutionlab;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import deconvolutionlab.monitor.Monitors;
import lab.component.CustomizedColumn;
import lab.component.CustomizedTable;

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
		columns.add(new CustomizedColumn("Name", String.class, 100, false));
		columns.add(new CustomizedColumn("Algorithm", String.class, 100, false));
		columns.add(new CustomizedColumn("Iterations", String.class, 100, false));
		columns.add(new CustomizedColumn("Mean", String.class, 100, false));
		columns.add(new CustomizedColumn("Minimum", String.class, 100, false));
		columns.add(new CustomizedColumn("Maximum", String.class, 100, false));
		columns.add(new CustomizedColumn("Stdev", String.class, 100, false));
		columns.add(new CustomizedColumn("Norm1", String.class, 100, false));
		columns.add(new CustomizedColumn("Norm2", String.class, 100, false));
		columns.add(new CustomizedColumn("Time", String.class, 100, false));
		columns.add(new CustomizedColumn("Memory", String.class, 100, false));
		columns.add(new CustomizedColumn("Peak", String.class, 100, false));
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

	public void show() {
		JFrame frame = new JFrame(name);
		frame.getContentPane().add(panel);
		frame.pack();
		frame.setVisible(true);
	}

}
