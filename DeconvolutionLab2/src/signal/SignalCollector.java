package signal;

import java.util.ArrayList;

import javax.swing.JScrollPane;

import deconvolutionlab.system.SystemUsage;
import lab.component.CustomizedColumn;
import lab.component.CustomizedTable;
import lab.tools.NumFormat;

public class SignalCollector {

	private static long bytesReal = 0;
	private static int countReal = 0;
	private static long bytesComplex = 0;
	private static int countComplex = 0;
	private static double chrono = 0;
	private static CustomizedTable table;
	private static double progress = 0;

	protected final static int NOTIFICATION_RATE = 25;

	static {
		bytesReal = 0;
		countReal = 0;
		bytesComplex = 0;
		countComplex = 0;
		chrono = System.nanoTime();
		
		ArrayList<CustomizedColumn> columns = new ArrayList<CustomizedColumn>();
		columns.add(new CustomizedColumn("Time", String.class, 100, false));
		columns.add(new CustomizedColumn("Name", String.class, 600, false));
		columns.add(new CustomizedColumn("Dimension", String.class, 60, false));
		columns.add(new CustomizedColumn("Count", String.class, 100, false));
		columns.add(new CustomizedColumn("Total", String.class, 100, false));
		columns.add(new CustomizedColumn("Memory", String.class, 100, false));
		table = new CustomizedTable(columns, true);
		table.getColumnModel().getColumn(4).setMaxWidth(100);
		table.getColumnModel().getColumn(4).setMinWidth(100);
	}
	
	public static JScrollPane getPanel(int w, int h) {
		return table.getPane(w, h);
	}
	
	public static String sumarize() {
		String r =  "Signals: " + NumFormat.bytes(bytesReal + bytesComplex);
		return r;
	}
	
	public static void clear() {
		table.removeRows();
	}
	
	public static double getProgress() {
		return progress;
	}
	
	public static void setProgress(double p) {
		progress = p;
	}
	
	public static void marker(String name) {
		String t = NumFormat.time(System.nanoTime()-chrono);
		String m = NumFormat.bytes(SystemUsage.getHeapUsed());
		String row[] = {t, name, "", "", "", m};
		table.append(row);
	}
	
	public static void alloc(String name, int nx, int ny, int nz, boolean complex) {
		long b = nx * ny * nz * 4 * (complex ? 2 : 1);
		if (complex) {
			bytesComplex += b;
			countComplex++;
		}
		else {
			bytesReal += b;
			countReal++;
		}
		String m = NumFormat.bytes(SystemUsage.getHeapUsed());
		String t = NumFormat.time(System.nanoTime()-chrono);
		String dim = "" + nx + "x" + ny + "x" + nz;
		String c = "" + (countReal + countComplex);
		String a = NumFormat.bytes(bytesReal + bytesComplex);
		String row[] = {t, name, dim, c, a, m};
		table.append(row);
	}
	
	public static void free(String name, int nx, int ny, int nz, boolean complex) {
		long b = nx * ny * nz * 4 * (complex ? 2 : 1);
		if (complex) {
			bytesComplex -= b;
			countComplex--;
		}
		else {
			bytesReal -= b;
			countReal--;
		}
		String m =  NumFormat.bytes(SystemUsage.getHeapUsed());
		String t = NumFormat.time(System.nanoTime()-chrono);
		String dim = "" + nx + "x" + ny + "x" + nz;
		String c = "" + (countReal + countComplex);
		String a = NumFormat.bytes(bytesReal + bytesComplex);
		String row[] = {t, name, dim, c, a, m};
		table.append(row);
	}

}
